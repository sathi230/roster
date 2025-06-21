package com.example.roster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class CSVService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public void saveCSVData(MultipartFile file) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // Skip header
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] data = line.split(",", -1); // retain empty columns

                if (data.length >= 9) {
                    Employee emp = new Employee();
                    emp.setEmpId(data[0].trim());
                    emp.setName(data[1].trim());
                    emp.setContact(data[2].trim());
                    emp.setRole(data[3].trim());
                    emp.setWorkLocation(data[4].trim());
                    emp.setTenure(data[5].trim());
                    emp.setSkillset(data[6].trim());
                    emp.setManagerName(data[7].trim());
                    emp.setShift(data[8].trim());
                    emp.setLunch(data.length > 9 ? data[9].trim() : ""); // handle missing lunch

                    employeeRepository.save(emp);
                }
            }
        }
    }
}
