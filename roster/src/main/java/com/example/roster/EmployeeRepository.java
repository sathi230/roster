package com.example.roster;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, String> {
	Employee findByEmpId(String empId);
	Employee findByName(String name);
}
