package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.entity.dto.UserWithData;
import io.github.xpakx.ladder.service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/{userId}")
public class MainController {
    private final MainService service;

    @Autowired
    public MainController(MainService service) {
        this.service = service;
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/all")
    public ResponseEntity<UserWithData> getListOfProjectsTasksAndLabels(@PathVariable Integer userId) {
        return new ResponseEntity<>(service.getAll(userId), HttpStatus.OK);
    }
}