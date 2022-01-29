package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.service.ExportService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@RestController
@AllArgsConstructor
@RequestMapping("{userId}/export")
public class ExportController {
    private ExportService service;

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping(value = "/csv/projects", produces="text/csv")
    public ResponseEntity<Resource> exportProjectListToCSV(@PathVariable Integer userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=projects.csv");
        headers.set(HttpHeaders.CONTENT_TYPE, "text/csv");
        return new ResponseEntity<>(service.exportProjectList(userId), headers, HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/csv/projects/{projectId}/tasks")
    public ResponseEntity<Resource> exportProjectsTaskListToCSV(@PathVariable Integer userId, @PathVariable Integer projectId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tasks.csv");
        headers.set(HttpHeaders.CONTENT_TYPE, "text/csv");
        return new ResponseEntity<>(service.exportTasksFromProjectById(userId, projectId), headers, HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/csv/tasks")
    public ResponseEntity<Resource> exportTaskListToCSV(@PathVariable Integer userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tasks.csv");
        headers.set(HttpHeaders.CONTENT_TYPE, "text/csv");
        return new ResponseEntity<>(service.exportTasks(userId), headers, HttpStatus.OK);
    }
}
