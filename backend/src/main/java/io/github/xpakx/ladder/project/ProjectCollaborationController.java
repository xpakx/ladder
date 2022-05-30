package io.github.xpakx.ladder.project;

import io.github.xpakx.ladder.collaboration.dto.CollaborationRequest;
import io.github.xpakx.ladder.collaboration.dto.CollaborationWithOwner;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/{userId}/projects")
@AllArgsConstructor
public class ProjectCollaborationController {
    private final ProjectCollaborationService projectService;

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/{projectId}/collaborators")
    public ResponseEntity<CollaborationWithOwner> addCollaborator(@RequestBody CollaborationRequest request, @PathVariable Integer projectId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(projectService.addCollaborator(request, projectId, userId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @DeleteMapping("/{projectId}/collaborators/{collaboratorId}")
    public ResponseEntity<?> addCollaborator(@PathVariable Integer collaboratorId, @PathVariable Integer projectId, @PathVariable Integer userId) {
        projectService.deleteCollaborator(collaboratorId, projectId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/{projectId}/collaborators")
    public ResponseEntity<List<CollaborationWithOwner>> getCollaborators(@PathVariable Integer projectId, @PathVariable Integer userId) {
        return new ResponseEntity<>(projectService.getCollaborators(projectId, userId), HttpStatus.OK);
    }
}
