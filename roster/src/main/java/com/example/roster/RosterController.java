package com.example.roster;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/api")
public class RosterController {

    @Autowired
    private RosterService rosterService;

    @Autowired
    private RosterRepository rosterRepository;

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @GetMapping("/generate")
    @ResponseBody
    public String generateRoster() {
        rosterService.generateMonthlyRoster(2025, 7);
        return "Roster generated successfully!";
    }

    @GetMapping("/employee-roster")
    public String employeeRosterForm() {
        return "employee-roster"; // ✅ This loads the form
    }
    @GetMapping("/admin/force-leave-form")
    public String showForceLeaveForm() {
        return "force-leave";
    }

    @GetMapping("/employee-dashboard")
    public String showEmployeeDashboard(@RequestParam(value="empId", required=false) String empId, Model model, HttpSession session) {
    	 if (empId == null) {
    	        empId = (String) session.getAttribute("empId");
    	    }
    	 if (empId == null) {
             model.addAttribute("error", "Invalid Employee ID");
             return "redirect:/employee-roster";
         }
        Employee employee = employeeRepository.findByEmpId(empId);
        if (employee == null) {
            model.addAttribute("error", "Invalid Employee ID");
            return "redirect:/employee-roster";
        }
        session.setAttribute("empId", empId);
        session.setAttribute("empName", employee.getName());
        return "employee-dashboard";
    }
    @GetMapping("/employee/view/roster")
    public String viewRoster(HttpSession session, Model model) {
        String empId = (String) session.getAttribute("empId");
        List<Roster> rosterList = rosterRepository.findByEmpId(empId);
        model.addAttribute("rosters", rosterList);
        return "employee-view-roster"; // separate view to be loaded in iframe
    }
    @GetMapping("/employee/leave-status")
    public String leaveStatusPage(HttpSession session, Model model) {
        String empId = (String) session.getAttribute("empId");
        List<LeaveRequest> leaves = leaveRequestRepository.findByEmpId(empId);
        model.addAttribute("leaves", leaves);
        return "employee-leave-status";
    }

    @PostMapping("/admin-login")
    public String adminLogin(
        @RequestParam String username,
        @RequestParam String password,
        HttpSession session,
        Model model
    ) {
        // Hardcoded admin credentials check
        if ("manager".equals(username) && "Admin@123".equals(password)) {
            session.setAttribute("adminLoggedIn", true);
            session.setAttribute("adminName", "Manager"); // Or any display name
            return "admin-dashboard";  // Your admin dashboard URL
        } else {
            model.addAttribute("adminError", "Invalid admin credentials");
            return "admin-login";  // Show login page again with error
        }
    }
    @PostMapping("/admin/force-leave")
    public String forceLeave(
            @RequestParam String empId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam String status) {

        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            Roster roster = rosterRepository.findByEmpIdAndDate(empId, date)
                    .orElse(new Roster(empId, date));
            roster.setStatus(status);
            roster.setShift("NA"); // optional
            rosterRepository.save(roster);
        }

        return "redirect:/some-confirmation-or-dashboard";
    }
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:employee-roster";
    }




  
}
