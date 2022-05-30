package io.github.xpakx.ladder.project;

import io.github.xpakx.ladder.notification.LogResponse;
import io.github.xpakx.ladder.project.dto.ProjectDetails;
import io.github.xpakx.ladder.project.dto.ProjectRequest;
import io.github.xpakx.ladder.project.dto.ProjectUpdateDto;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
}
