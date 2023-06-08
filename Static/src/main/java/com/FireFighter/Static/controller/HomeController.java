package com.firefighter.Static.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/infos")
    public String map() {
        return "infos";
    }

    @GetMapping("/manager")
    public String manager() {
        return "manager";
    }

}