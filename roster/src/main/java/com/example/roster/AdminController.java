package com.example.roster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class AdminController {

    @Autowired
    private CSVService csvService;

    @GetMapping("/upload")
    public String uploadPage() {
        return "upload"; // upload.html page
    }

    @PostMapping("/upload")
    @ResponseBody
    public String uploadCSV(@RequestParam("file") MultipartFile file) {
        try {
            csvService.saveCSVData(file);
            return "CSV uploaded and saved successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to upload CSV.";
        }
    }
}
