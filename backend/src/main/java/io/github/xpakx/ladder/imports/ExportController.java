package io.github.xpakx.ladder.imports;

import io.github.xpakx.ladder.imports.ExportCSVService;
import io.github.xpakx.ladder.imports.ExportTXTService;
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
    private ExportTXTService txtService;

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping(value = "/csv/projects", produces="text/csv")
    public ResponseEntity<Resource> exportProjectListToCSV(@PathVariable Integer userId) {
        return new ResponseEntity<>(service.exportProjectList(userId),
                getHttpHeadersForFile("projects.csv", "csv"),
                HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping(value = "/csv/projects/{projectId}/tasks", produces="text/csv")
    public ResponseEntity<Resource> exportProjectsTaskListToCSV(@PathVariable Integer userId, @PathVariable Integer projectId) {
        return new ResponseEntity<>(service.exportTasksFromProjectById(userId, projectId),
                getHttpHeadersForFile("tasks.csv", "csv"),
                HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping(value = "/csv/tasks", produces="text/csv")
    public ResponseEntity<Resource> exportTaskListToCSV(@PathVariable Integer userId) {
        return new ResponseEntity<>(service.exportTasks(userId),
                getHttpHeadersForFile("tasks.csv", "csv"),
                HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping(value = "/txt/projects", produces="text/txt")
    public ResponseEntity<Resource> exportProjectListToTXT(@PathVariable Integer userId) {
        return new ResponseEntity<>(txtService.exportProjectList(userId),
                getHttpHeadersForFile("projects.txt", "txt"),
                HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping(value = "/txt/projects/{projectId}/tasks", produces="text/txt")
    public ResponseEntity<Resource> exportProjectsTaskListToTXT(@PathVariable Integer userId, @PathVariable Integer projectId) {
        return new ResponseEntity<>(txtService.exportTasksFromProjectById(userId, projectId),
                getHttpHeadersForFile("tasks.txt", "txt"),
                HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping(value = "/txt/tasks", produces="text/txt")
    public ResponseEntity<Resource> exportTaskListToTXT(@PathVariable Integer userId) {
        return new ResponseEntity<>(txtService.exportTasks(userId),
                getHttpHeadersForFile("tasks.txt", "txt"),
                HttpStatus.OK);
    }

    private HttpHeaders getHttpHeadersForFile(String fileName, String fileType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+fileName);
        headers.set(HttpHeaders.CONTENT_TYPE, "text/"+fileType);
        return headers;
    }
}
