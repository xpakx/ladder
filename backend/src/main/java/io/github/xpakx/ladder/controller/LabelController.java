package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.entity.Label;
import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.dto.LabelRequest;
import io.github.xpakx.ladder.entity.dto.ProjectRequest;
import io.github.xpakx.ladder.service.LabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/{userId}/labels")
public class LabelController {
    private final LabelService labelService;

    @Autowired
    public LabelController(LabelService labelService) {
        this.labelService = labelService;
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping
    public ResponseEntity<Label> addLabel(@RequestBody LabelRequest request, @PathVariable Integer userId) {
        return  new ResponseEntity<>(labelService.addLabel(request, userId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/{labelId}")
    public ResponseEntity<Label> updateLabel(@RequestBody LabelRequest request, @PathVariable Integer userId, @PathVariable Integer labelId) {
        return  new ResponseEntity<>(labelService.updateLabel(request, userId, labelId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @DeleteMapping("/{labelId}")
    public ResponseEntity<?> deleteProject(@PathVariable Integer labelId, @PathVariable Integer userId) {
        labelService.deleteLabel(labelId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
