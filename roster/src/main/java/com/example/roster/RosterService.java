package com.example.roster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Service
public class RosterService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RosterRepository rosterRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    public void generateMonthlyRoster(int year, int month) {
        List<Employee> employees = employeeRepository.findAll();
        List<LeaveRequest> approvedLeaves = leaveRequestRepository.findByStatus("APPROVED");

        int totalEmployees = employees.size();
        int daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();

        // Leave tracking
        Map<String, Set<LocalDate>> empLeaves = new HashMap<>();
        Map<String, Integer> empPlannedLeaveCount = new HashMap<>();
        Map<String, Integer> empSickLeaveCount = new HashMap<>();
        Map<String, Map<LocalDate, String>> empLeaveTypes = new HashMap<>();

        for (LeaveRequest leave : approvedLeaves) {
            String empId = leave.getEmpId();
            empLeaves.putIfAbsent(empId, new HashSet<>());
            empLeaveTypes.putIfAbsent(empId, new HashMap<>());

            for (LocalDate date = leave.getFromDate(); !date.isAfter(leave.getToDate()); date = date.plusDays(1)) {
                if (date.getMonthValue() == month && date.getYear() == year) {
                    empLeaves.get(empId).add(date);
                    empLeaveTypes.get(empId).put(date, leave.getLeaveType());

                    if (leave.getLeaveType().equals("EL")) {
                        empPlannedLeaveCount.put(empId, empPlannedLeaveCount.getOrDefault(empId, 0) + 1);
                    } else if (leave.getLeaveType().equals("SL")) {
                        empSickLeaveCount.put(empId, empSickLeaveCount.getOrDefault(empId, 0) + 1);
                    }
                }
            }
        }

        // Off tracking
        Map<String, Integer> sundayOffCount = new HashMap<>();
        Map<String, Integer> totalWeekOffCount = new HashMap<>();
        Map<String, Integer> workStreak = new HashMap<>();
        Map<String, LocalDate> lastOffDate = new HashMap<>();

        for (Employee emp : employees) {
            sundayOffCount.put(emp.getEmpId(), 0);
            totalWeekOffCount.put(emp.getEmpId(), 0);
            workStreak.put(emp.getEmpId(), 0);
            lastOffDate.put(emp.getEmpId(), null);
        }

        for (int i = 1; i <= daysInMonth; i++) {
            LocalDate date = LocalDate.of(year, month, i);
            boolean isSunday = date.getDayOfWeek() == DayOfWeek.SUNDAY;
            int maxOffsToday = (int) (totalEmployees * (isSunday ? 0.5 : 0.2));
            int offCount = 0;

            for (Employee emp : employees) {
                String empId = emp.getEmpId();
                String status = "WORKING";
                int staggerOffset = Math.abs(empId.hashCode()) % 7;
                LocalDate lastOff = lastOffDate.get(empId);
                boolean hasWorkedEnough = lastOff == null || java.time.temporal.ChronoUnit.DAYS.between(lastOff, date) >= 5;
                boolean onLeave = empLeaves.getOrDefault(empId, Collections.emptySet()).contains(date);
                String leaveType = empLeaveTypes.getOrDefault(empId, new HashMap<>()).get(date);

                // --- Planned or Sick Leave ---
                if (onLeave &&
                    empPlannedLeaveCount.getOrDefault(empId, 0) <= 2 &&
                    empSickLeaveCount.getOrDefault(empId, 0) <= 1) {

                    long sameShiftSkillWorking = employees.stream()
                        .filter(e -> !e.getEmpId().equals(empId))
                        .filter(e -> e.getShift().equals(emp.getShift()) && e.getSkillset().equals(emp.getSkillset()))
                        .filter(e -> {
                            Roster r = rosterRepository.findByEmpIdAndDate(e.getEmpId(), date).orElse(null);
                            return r == null || r.getStatus().equals("WORKING");
                        }).count();

                    double requiredPresent = isSunday ? totalEmployees * 0.5 : totalEmployees * 0.8;

                    if (sameShiftSkillWorking >= 1 && (totalEmployees - (offCount + 1)) >= requiredPresent) {
                        status = "EL".equals(leaveType) ? "PLANNED_LEAVE" : "SICK_LEAVE";
                        offCount++;
                        lastOffDate.put(empId, date);
                        workStreak.put(empId, 0);
                    } else {
                        workStreak.put(empId, workStreak.get(empId) + 1);
                    }
                }

                // --- Sunday Week Off ---
                else if (isSunday &&
                         sundayOffCount.get(empId) < 2 &&
                         offCount < maxOffsToday &&
                         hasWorkedEnough) {

                    long sameShiftSkillWorking = employees.stream()
                        .filter(e -> !e.getEmpId().equals(empId))
                        .filter(e -> e.getShift().equals(emp.getShift()) && e.getSkillset().equals(emp.getSkillset()))
                        .filter(e -> {
                            Roster r = rosterRepository.findByEmpIdAndDate(e.getEmpId(), date).orElse(null);
                            return r == null || r.getStatus().equals("WORKING");
                        }).count();

                    if (sameShiftSkillWorking >= 1) {
                        status = "WEEK_OFF";
                        sundayOffCount.put(empId, sundayOffCount.get(empId) + 1);
                        totalWeekOffCount.put(empId, totalWeekOffCount.get(empId) + 1);
                        lastOffDate.put(empId, date);
                        workStreak.put(empId, 0);
                        offCount++;
                    } else {
                        workStreak.put(empId, workStreak.get(empId) + 1);
                    }
                }

                // --- Rotational Weekday Off ---
                else if (!isSunday &&
                         totalWeekOffCount.get(empId) < 4 &&
                         hasWorkedEnough &&
                         (date.getDayOfMonth() + staggerOffset) % 7 == 0 &&
                         offCount < maxOffsToday) {

                    status = "WEEK_OFF";
                    totalWeekOffCount.put(empId, totalWeekOffCount.get(empId) + 1);
                    lastOffDate.put(empId, date);
                    workStreak.put(empId, 0);
                    offCount++;
                }

                // --- Default ---
                else {
                    workStreak.put(empId, workStreak.get(empId) + 1);
                }

                // Save or Update
                Roster existing = rosterRepository.findByEmpIdAndDate(empId, date).orElse(null);
                if (existing == null) {
                    existing = new Roster();
                    existing.setEmpId(empId);
                    existing.setDate(date);
                    existing.setShift(emp.getShift());
                    existing.setSkillset(emp.getSkillset());
                }

                if (onLeave ||!status.equals(existing.getStatus())) {
                    existing.setStatus(status);
                    rosterRepository.save(existing);
                }
            }
        }
    }
}
