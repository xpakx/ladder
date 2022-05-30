package io.github.xpakx.ladder.project;

import io.github.xpakx.ladder.project.dto.FullProjectTree;
import io.github.xpakx.ladder.project.dto.ProjectData;
import io.github.xpakx.ladder.project.ProjectDataService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/{userId}/projects")
@AllArgsConstructor
public class ProjectDataController {
    private final ProjectDataService projectService;

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/{projectId}/full")
    public ResponseEntity<FullProjectTree> getFullProject(@PathVariable Integer projectId, @PathVariable Integer userId) {
        return new ResponseEntity<>(projectService.getFullProject(projectId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/all")
    public ResponseEntity<List<FullProjectTree>> getFullTree(@PathVariable Integer userId) {
        return new ResponseEntity<>(projectService.getFullTree(userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/{projectId}/data")
    public ResponseEntity<ProjectData> getData(@PathVariable Integer projectId, @PathVariable Integer userId) {
        return new ResponseEntity<>(projectService.getProjectData(projectId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/{projectId}/data/archived")
    public ResponseEntity<ProjectData> getArchivedData(@PathVariable Integer projectId, @PathVariable Integer userId) {
        return new ResponseEntity<>(projectService.getProjectDataWithArchived(projectId, userId), HttpStatus.OK);
    }
}
