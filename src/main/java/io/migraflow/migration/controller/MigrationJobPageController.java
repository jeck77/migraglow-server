package io.migraflow.migration.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class MigrationJobPageController {

    /**
     * 프로젝트의 이관 작업 목록 화면(JSP)을 렌더링한다.
     *
     * @param projectId 조회할 프로젝트 ID
     * @param model     JSP에서 사용할 projectId를 담을 모델
     * @return 뷰 이름 ("jobs/list")
     */
    @GetMapping("/projects/{projectId}/jobs")
    public String list(@PathVariable Long projectId, Model model) {
        model.addAttribute("projectId", projectId);
        return "jobs/list";
    }

    /**
     * 이관 작업의 컬럼 매핑 화면(JSP)을 렌더링한다.
     *
     * @param projectId 소속 프로젝트 ID
     * @param jobId     매핑을 설정할 이관 작업 ID
     * @param model     JSP에서 사용할 projectId/jobId를 담을 모델
     * @return 뷰 이름 ("jobs/mapping")
     */
    @GetMapping("/projects/{projectId}/jobs/{jobId}/mapping")
    public String mapping(@PathVariable Long projectId, @PathVariable Long jobId, Model model) {
        model.addAttribute("projectId", projectId);
        model.addAttribute("jobId", jobId);
        return "jobs/mapping";
    }
}
