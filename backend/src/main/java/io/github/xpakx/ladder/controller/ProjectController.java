package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.*;
import io.github.xpakx.ladder.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("{/{userId}/projects")
public class ProjectController {
    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDetails> getProject(@PathVariable Integer projectId, @PathVariable Integer userId) {
        return new ResponseEntity<>(projectService.getProjectById(projectId, userId), HttpStatus.OK);
    }

    @GetMapping("/{projectId}/full")
    public ResponseEntity<FullProjectTree> getFullProject(@PathVariable Integer projectId, @PathVariable Integer userId) {
        return new ResponseEntity<>(projectService.getFullProject(projectId, userId), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Project> addProject(@RequestBody ProjectRequest request, @PathVariable Integer userId) {
        return  new ResponseEntity<>(projectService.addProject(request), HttpStatus.CREATED);
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<Project> updateProject(@RequestBody ProjectRequest request, @PathVariable Integer projectId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(projectService.updateProject(request, projectId), HttpStatus.OK);
    }

    @PutMapping("/{projectId}/name")
    public ResponseEntity<Project> updateProjectName(@RequestBody NameRequest request, @PathVariable Integer projectId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(projectService.updateProjectName(request, projectId), HttpStatus.OK);
    }

    @PutMapping("/{projectId}/name")
    public ResponseEntity<Project> updateProjectParent(@RequestBody IdRequest request, @PathVariable Integer projectId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(projectService.updateProjectParent(request, projectId), HttpStatus.OK);
    }

    @PutMapping("/{projectId}/favorite")
    public ResponseEntity<Project> updateProjectFav(@RequestBody BooleanRequest request, @PathVariable Integer projectId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(projectService.updateProjectFav(request, projectId), HttpStatus.OK);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> deleteProject(@PathVariable Integer projectId, @PathVariable Integer userId) {
        projectService.deleteProject(projectId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{projectId}/tasks")
    public ResponseEntity<Task> addTaskToProject(@RequestBody UpdateTaskRequest request, @PathVariable Integer projectId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(projectService.addTask(request, projectId), HttpStatus.CREATED);
    }
}
