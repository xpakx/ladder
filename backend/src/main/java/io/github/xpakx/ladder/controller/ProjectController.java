package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.aspect.LogResponse;
import io.github.xpakx.ladder.entity.dto.*;
import io.github.xpakx.ladder.service.ProjectService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/{userId}/projects")
@AllArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDetails> getProject(@PathVariable Integer projectId, @PathVariable Integer userId) {
        return new ResponseEntity<>(projectService.getProjectById(projectId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping
    @LogResponse
    public ResponseEntity<ProjectUpdateDto> addProject(@RequestBody ProjectRequest request, @PathVariable Integer userId) {
        return new ResponseEntity<>(
                ProjectUpdateDto.from(projectService.addProject(request, userId)),
                HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectUpdateDto> updateProject(@RequestBody ProjectRequest request, @PathVariable Integer projectId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(
                ProjectUpdateDto.from(projectService.updateProject(request, projectId, userId)),
                HttpStatus.OK
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> deleteProject(@PathVariable Integer projectId, @PathVariable Integer userId) {
        projectService.deleteProject(projectId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/{projectId}/archive")
    public ResponseEntity<ProjectUpdateDto> archiveProject(@RequestBody BooleanRequest request, @PathVariable Integer projectId, @PathVariable Integer userId) {
        return new ResponseEntity<>(
                ProjectUpdateDto.from(projectService.archiveProject(request, projectId, userId)),
                HttpStatus.OK
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/{projectId}/tasks/completed/archive")
    public ResponseEntity<ProjectUpdateDto> archiveCompletedTasks(@RequestBody BooleanRequest request, @PathVariable Integer projectId, @PathVariable Integer userId) {
        return new ResponseEntity<>(
                ProjectUpdateDto.from(projectService.archiveCompletedTasks(request, projectId, userId)),
                HttpStatus.OK
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/archived")
    public ResponseEntity<List<ProjectDetails>> getArchivedProjects(@PathVariable Integer userId) {
        return new ResponseEntity<>(projectService.getArchivedProjects(userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/{projectId}/tasks/archived")
    public ResponseEntity<List<TaskDetails>> getArchivedTasks(@PathVariable Integer userId, @PathVariable Integer projectId) {
        return new ResponseEntity<>(projectService.getArchivedTasks(userId, projectId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/{projectId}/collaborators")
    public ResponseEntity<CollaborationWithOwner> addCollaborator(@RequestBody CollaborationRequest request, @PathVariable Integer projectId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(projectService.addCollaborator(request, projectId, userId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @DeleteMapping("/{projectId}/collaborators/{collabId}")
    public ResponseEntity<?> addCollaborator(@PathVariable Integer collabId, @PathVariable Integer projectId, @PathVariable Integer userId) {
        projectService.deleteCollaborator(collabId, projectId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/{projectId}/collaborators")
    public ResponseEntity<List<CollaborationWithOwner>> getCollaborators(@PathVariable Integer projectId, @PathVariable Integer userId) {
        return new ResponseEntity<>(projectService.getCollaborators(projectId, userId), HttpStatus.OK);
    }
}
