package io.github.xpakx.ladder.imports;

import io.github.xpakx.ladder.imports.ImportCSVService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@AllArgsConstructor
@RequestMapping("{userId}/import")
public class ImportController {
    private ImportCSVService service;

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/csv/projects")
    public ResponseEntity<?> importProjectListFromCSV(@RequestParam("file") MultipartFile file, @PathVariable Integer userId) {
        try {
            String content = new String(file.getBytes());
            service.importProjectList(userId, content);
        } catch (IOException ex) {
            throw new IllegalArgumentException();
        }
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/csv/projects/{projectId}/tasks")
    public ResponseEntity<Resource> importProjectsTaskListFromCSV(@RequestParam("file") MultipartFile file, @PathVariable Integer userId, @PathVariable Integer projectId) {
        try {
            String content = new String(file.getBytes());
            service.importTasksToProjectById(userId, projectId, content);
        } catch (IOException ex) {
            throw new IllegalArgumentException();
        }
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/csv/tasks")
    public ResponseEntity<Resource> importTaskListFromCSV(@RequestParam("file") MultipartFile file, @PathVariable Integer userId) {
        try {
            String content = new String(file.getBytes());
            service.importTasks(userId, content);
        } catch (IOException ex) {
            throw new IllegalArgumentException();
        }
        return ResponseEntity.ok().build();
    }
}
