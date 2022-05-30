package io.github.xpakx.ladder.project;

import io.github.xpakx.ladder.common.dto.IdRequest;
import io.github.xpakx.ladder.project.dto.ProjectRequest;
import io.github.xpakx.ladder.project.dto.ProjectUpdateDto;
import io.github.xpakx.ladder.project.dto.TasksAndProjects;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/{userId}/projects/{projectId}")
@AllArgsConstructor
public class ProjectMovableController {
    private final ProjectMovableService projectService;


    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/after")
    public ResponseEntity<ProjectUpdateDto> addProjectAfter(@RequestBody ProjectRequest request, @PathVariable Integer userId,
                                                            @PathVariable Integer projectId) {
        return new ResponseEntity<>(
                ProjectUpdateDto.from(projectService.addProjectAfter(request, userId, projectId)),
                HttpStatus.CREATED
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/before")
    public ResponseEntity<ProjectUpdateDto> addProjectBefore(@RequestBody ProjectRequest request, @PathVariable Integer userId,
                                                             @PathVariable Integer projectId) {
        return new ResponseEntity<>(
                ProjectUpdateDto.from(projectService.addProjectBefore(request, userId, projectId)),
                HttpStatus.CREATED
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/duplicate")
    public ResponseEntity<TasksAndProjects> duplicateProject(@PathVariable Integer projectId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(projectService.duplicate(projectId, userId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/move/after")
    public ResponseEntity<ProjectUpdateDto> moveProjectAfter(@RequestBody IdRequest request, @PathVariable Integer userId,
                                                             @PathVariable Integer projectId) {
        return new ResponseEntity<>(
                ProjectUpdateDto.from(projectService.moveProjectAfter(request, userId, projectId)),
                HttpStatus.OK
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/move/asChild")
    public ResponseEntity<ProjectUpdateDto> moveProjectAsFirstChild(@RequestBody IdRequest request, @PathVariable Integer userId,
                                                                    @PathVariable Integer projectId) {
        return new ResponseEntity<>(
                ProjectUpdateDto.from(projectService.moveProjectAsFirstChild(request, userId, projectId)),
                HttpStatus.OK
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/move/asFirst")
    public ResponseEntity<ProjectUpdateDto> moveProjectAsFirst(@PathVariable Integer userId, @PathVariable Integer projectId) {
        return new ResponseEntity<>(
                ProjectUpdateDto.from(projectService.moveProjectAsFirst(userId, projectId)),
                HttpStatus.OK
        );
    }
}
