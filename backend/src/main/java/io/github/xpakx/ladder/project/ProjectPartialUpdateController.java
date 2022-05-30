package io.github.xpakx.ladder.project;

import io.github.xpakx.ladder.common.dto.BooleanRequest;
import io.github.xpakx.ladder.common.dto.IdRequest;
import io.github.xpakx.ladder.common.dto.NameRequest;
import io.github.xpakx.ladder.project.dto.ProjectUpdateDto;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/{userId}/projects/{projectId}")
@AllArgsConstructor
public class ProjectPartialUpdateController {
    private final ProjectPartialUpdateService projectService;

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/name")
    public ResponseEntity<ProjectUpdateDto> updateProjectName(@RequestBody NameRequest request, @PathVariable Integer projectId, @PathVariable Integer userId) {
        return new ResponseEntity<>(
                ProjectUpdateDto.from(projectService.updateProjectName(request, projectId, userId)),
                HttpStatus.OK
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/parent")
    public ResponseEntity<ProjectUpdateDto> updateProjectParent(@RequestBody IdRequest request, @PathVariable Integer projectId, @PathVariable Integer userId) {
        return new ResponseEntity<>(
                ProjectUpdateDto.from(projectService.updateProjectParent(request, projectId, userId)),
                HttpStatus.OK
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/favorite")
    public ResponseEntity<ProjectUpdateDto> updateProjectFav(@RequestBody BooleanRequest request, @PathVariable Integer projectId, @PathVariable Integer userId) {
        return new ResponseEntity<>(
                ProjectUpdateDto.from(projectService.updateProjectFav(request, projectId, userId)),
                HttpStatus.OK
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/collapse")
    public ResponseEntity<ProjectUpdateDto> updateProjectCollapsion(@RequestBody BooleanRequest request, @PathVariable Integer projectId, @PathVariable Integer userId) {
        return new ResponseEntity<>(
                ProjectUpdateDto.from(projectService.updateProjectCollapsedState(request, projectId, userId)),
                HttpStatus.OK
        );
    }
}
