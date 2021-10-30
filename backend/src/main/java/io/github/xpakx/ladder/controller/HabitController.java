package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.entity.Habit;
import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.AddTaskRequest;
import io.github.xpakx.ladder.entity.dto.HabitRequest;
import io.github.xpakx.ladder.service.HabitService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/{userId}")
@AllArgsConstructor
public class HabitController {
    private final HabitService habitService;

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("projects/{projectId}/tasks")
    public ResponseEntity<Habit> addHabitToProject(@RequestBody HabitRequest request, @PathVariable Integer projectId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(habitService.addHabit(request, userId, projectId), HttpStatus.CREATED);
    }
}
