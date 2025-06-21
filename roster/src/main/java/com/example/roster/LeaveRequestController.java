package com.example.roster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/leave")
public class LeaveRequestController {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    @Autowired
    private RosterService rosterService;

    // Show employee leave request form
    @GetMapping("/request")
    public String showLeaveForm(Model model) {
        model.addAttribute("leaveRequest", new LeaveRequest());
        return "leave-request";
    }

    // Submit leave request
    @PostMapping("/request")
    public String submitLeaveRequest(@ModelAttribute LeaveRequest leaveRequest) {
        leaveRequest.setStatus("PENDING");
        leaveRequestRepository.save(leaveRequest);
        return "redirect:/leave/request?success";
    }

    // Show all pending requests for manager to approve/reject
    @GetMapping("/manage")
    public String showPendingLeaves(Model model) {
        List<LeaveRequest> pending = leaveRequestRepository.findByStatus("PENDING");
        model.addAttribute("pendingLeaves", pending);
        return "manage-leaves";
    }

    // Manager approves or rejects
    @PostMapping("/updateStatus")
    public String updateLeaveStatus(@RequestParam Long id, @RequestParam String action) {
        LeaveRequest leave = leaveRequestRepository.findById(id).orElse(null);
        if (leave != null) {
            leave.setStatus(action.equals("approve") ? "APPROVED" : "REJECTED");
            leaveRequestRepository.save(leave);
        }
        if (action.equals("approve")) {
            LocalDate leaveDate = leave.getFromDate();
            rosterService.generateMonthlyRoster(leaveDate.getYear(), leaveDate.getMonthValue());
        }
        return "redirect:/leave/manage";
    }
    
}
