package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.entity.dto.DateRequest;
import io.github.xpakx.ladder.entity.dto.SyncData;
import io.github.xpakx.ladder.entity.dto.UserWithData;
import io.github.xpakx.ladder.service.MainService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
}
