package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.BooleanRequest;
import io.github.xpakx.ladder.entity.dto.ProjectDetails;
import io.github.xpakx.ladder.entity.dto.ProjectUpdateDto;
import io.github.xpakx.ladder.entity.dto.TaskDetails;
import io.github.xpakx.ladder.service.ArchiveService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/{userId}")
@AllArgsConstructor
public class ArchiveController {
    private final ArchiveService archiveService;

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/projects/{projectId}/archive")
    public ResponseEntity<ProjectUpdateDto> archiveProject(@RequestBody BooleanRequest request, @PathVariable Integer projectId, @PathVariable Integer userId) {
        return new ResponseEntity<>(
                ProjectUpdateDto.from(archiveService.archiveProject(request, projectId, userId)),
                HttpStatus.OK
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/projects/{projectId}/tasks/completed/archive")
    public ResponseEntity<ProjectUpdateDto> archiveCompletedTasks(@RequestBody BooleanRequest request, @PathVariable Integer projectId, @PathVariable Integer userId) {
        return new ResponseEntity<>(
                ProjectUpdateDto.from(archiveService.archiveCompletedTasks(request, projectId, userId)),
                HttpStatus.OK
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/projects/archived")
    public ResponseEntity<List<ProjectDetails>> getArchivedProjects(@PathVariable Integer userId) {
        return new ResponseEntity<>(archiveService.getArchivedProjects(userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/projects/{projectId}/tasks/archived")
    public ResponseEntity<List<TaskDetails>> getArchivedTasks(@PathVariable Integer userId, @PathVariable Integer projectId) {
        return new ResponseEntity<>(archiveService.getArchivedTasks(userId, projectId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/archive")
    public ResponseEntity<Task> archiveTask(@RequestBody BooleanRequest request, @PathVariable Integer taskId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(archiveService.archiveTask(request, taskId, userId), HttpStatus.OK);
    }
}
