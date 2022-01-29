package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.service.ExportCSVService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("{userId}/export")
public class ExportController {
    private ExportCSVService service;

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping(value = "/csv/projects", produces="text/csv")
    public ResponseEntity<Resource> exportProjectListToCSV(@PathVariable Integer userId) {
        return new ResponseEntity<>(service.exportProjectList(userId),
                getHttpHeadersForFile("projects.csv"),
                HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/csv/projects/{projectId}/tasks")
    public ResponseEntity<Resource> exportProjectsTaskListToCSV(@PathVariable Integer userId, @PathVariable Integer projectId) {
        return new ResponseEntity<>(service.exportTasksFromProjectById(userId, projectId),
                getHttpHeadersForFile("tasks.csv"),
                HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/csv/tasks")
    public ResponseEntity<Resource> exportTaskListToCSV(@PathVariable Integer userId) {
        return new ResponseEntity<>(service.exportTasks(userId),
                getHttpHeadersForFile("tasks.csv"),
                HttpStatus.OK);
    }

    private HttpHeaders getHttpHeadersForFile(String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+fileName);
        headers.set(HttpHeaders.CONTENT_TYPE, "text/csv");
        return headers;
    }
}
