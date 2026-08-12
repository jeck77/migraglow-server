package io.migraflow.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProjectPageController {

    /**
     * 프로젝트 목록 화면(JSP)을 렌더링한다.
     *
     * @return 뷰 이름 ("projects/list")
     */
    @GetMapping("/")
    public String list() {
        return "projects/list";
    }
}
