package io.migraflow.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProjectPageController {

    @GetMapping("/")
    public String list() {
        return "projects/list";
    }
}
