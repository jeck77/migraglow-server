package io.migraflow.migration.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class MigrationJobPageController {

    @GetMapping("/projects/{projectId}/jobs")
    public String list(@PathVariable Long projectId, Model model) {
        model.addAttribute("projectId", projectId);
        return "jobs/list";
    }
}
