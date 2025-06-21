package com.example.roster;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RosterRepository extends JpaRepository<Roster, Long> {

    List<Roster> findByEmpId(String empId);

    Optional<Roster> findByEmpIdAndDate(String empId, LocalDate date); // ✅ Keep this one

    List<Roster> findByDate(LocalDate date);
}
