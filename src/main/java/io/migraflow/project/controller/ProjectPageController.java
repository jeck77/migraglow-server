package io.migraflow.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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

    /**
     * 프로젝트 정보 수정 화면(JSP)을 렌더링한다.
     *
     * @param projectId 수정할 프로젝트 ID
     * @param model     JSP에서 사용할 projectId를 담을 모델
     * @return 뷰 이름 ("projects/edit")
     */
    @GetMapping("/projects/{projectId}/edit")
    public String edit(@PathVariable Long projectId, Model model) {
        model.addAttribute("projectId", projectId);
        return "projects/edit";
    }
}
