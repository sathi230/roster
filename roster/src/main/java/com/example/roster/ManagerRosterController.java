package com.example.roster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class ManagerRosterController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RosterRepository rosterRepository;

    @GetMapping("/manager/view-roster")
    public String viewFullRoster(Model model) {
        List<Employee> employees = employeeRepository.findAll();
        int year = 2025, month = 7;
        int days = LocalDate.of(year, month, 1).lengthOfMonth();

        List<String> formattedDays = new ArrayList<>();
        Map<LocalDate, String> dateToFormattedMap = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM");

        for (int i = 1; i <= days; i++) {
            LocalDate date = LocalDate.of(year, month, i);
            String formatted = date.format(formatter);
            formattedDays.add(formatted);
            dateToFormattedMap.put(date, formatted);
        }

        class EmpRosterInfo {
            String name;
            String shift;
            String skillset;
            Map<String, String> dailyStatus;

            public EmpRosterInfo(String name, String shift, String skillset, Map<String, String> dailyStatus) {
                this.name = name;
                this.shift = shift;
                this.skillset = skillset;
                this.dailyStatus = dailyStatus;
            }

            public String getName() { return name; }
            public String getShift() { return shift; }
            public String getSkillset() { return skillset; }
            public Map<String, String> getDailyStatus() { return dailyStatus; }
        }

        List<EmpRosterInfo> matrix = new ArrayList<>();

        for (Employee emp : employees) {
            List<Roster> empRoster = rosterRepository.findByEmpId(emp.getEmpId());
            Map<String, String> statusMap = new LinkedHashMap<>();

            for (Map.Entry<LocalDate, String> entry : dateToFormattedMap.entrySet()) {
                String formattedDate = entry.getValue();
                statusMap.put(formattedDate, ""); // default blank
            }

            for (Roster r : empRoster) {
                String formattedDate = dateToFormattedMap.get(r.getDate());
                if (formattedDate != null) {
                    statusMap.put(formattedDate, r.getStatus());
                }
            }

            matrix.add(new EmpRosterInfo(emp.getName(), emp.getShift(), emp.getSkillset(), statusMap));
        }

        model.addAttribute("formattedDays", formattedDays);
        model.addAttribute("matrix", matrix);

        return "view-roster";  // maps to templates/view-roster.html
    }
}
