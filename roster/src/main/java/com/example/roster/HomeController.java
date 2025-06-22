package com.example.roster;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {

   @GetMapping
    public String home() {
	   System.out.println(">>> HomeController reached <<<");
    	return "redirect:/api/employee-roster"; // Change this if your landing page is different
    }
}
