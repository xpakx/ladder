package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.entity.dto.*;
import io.github.xpakx.ladder.service.MainService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/{userId}")
@AllArgsConstructor
public class MainController {
    private final MainService service;

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/all")
    public ResponseEntity<UserWithData> getListOfProjectsTasksAndLabels(@PathVariable Integer userId) {
        return new ResponseEntity<>(service.getAll(userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/sync")
    public ResponseEntity<SyncData> sync(@RequestBody DateRequest time, @PathVariable Integer userId) {
        return new ResponseEntity<>(service.sync(time, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/sync/collab/tasks")
    public ResponseEntity<List<CollabTaskDetails>> sync(@RequestBody IdCollectionRequest projectIds, @PathVariable Integer userId) {
        return new ResponseEntity<>(service.syncCollabTasks(projectIds, userId), HttpStatus.OK);
    }
}
