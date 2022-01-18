package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.service.ExportService;
import lombok.AllArgsConstructor;
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
    private ExportService service;

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/csv/projects")
    public ResponseEntity<String> exportProjectListToCSV(@PathVariable Integer userId) {
        return new ResponseEntity<>(service.exportProjectList(userId), HttpStatus.OK);
    }
}
