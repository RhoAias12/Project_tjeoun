package com.tjoeun.controller;

import com.tjoeun.entity.Recruitment;
import org.springframework.ui.Model;
import com.tjoeun.repository.JobPostingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

  @Autowired
  private JobPostingRepository repository;

  @GetMapping("/")
  public String index(Model model) {
    List<Recruitment> jobs = repository.findAll();
    model.addAttribute("jobs", jobs);
    return "main";
  }
}
