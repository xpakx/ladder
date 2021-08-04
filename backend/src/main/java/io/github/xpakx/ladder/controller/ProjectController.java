package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.dto.ProjectRequest;
import io.github.xpakx.ladder.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<Project> getProject(@PathVariable Integer projectId) {
        return new ResponseEntity<>(projectService.getProjectById(projectId), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Project> addProject(@RequestBody ProjectRequest request) {
        return  new ResponseEntity<>(projectService.addProject(request), HttpStatus.CREATED);
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<Project> updateProject(@RequestBody ProjectRequest request, @PathVariable Integer projectId) {
        return  new ResponseEntity<>(projectService.updateProject(request, projectId), HttpStatus.OK);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> deleteProject(@PathVariable Integer projectId) {
        projectService.deleteProject(projectId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
