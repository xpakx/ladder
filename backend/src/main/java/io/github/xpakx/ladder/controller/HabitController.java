package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.entity.Habit;
import io.github.xpakx.ladder.entity.Label;
import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.AddTaskRequest;
import io.github.xpakx.ladder.entity.dto.HabitRequest;
import io.github.xpakx.ladder.entity.dto.IdRequest;
import io.github.xpakx.ladder.entity.dto.PriorityRequest;
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
    @PostMapping("projects/{projectId}/habits")
    public ResponseEntity<Habit> addHabitToProject(@RequestBody HabitRequest request, @PathVariable Integer projectId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(habitService.addHabit(request, userId, projectId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @DeleteMapping("/habits/{habitId}")
    public ResponseEntity<?> deleteLabel(@PathVariable Integer habitId, @PathVariable Integer userId) {
        habitService.deleteHabit(habitId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/habits/{habitId}/move/asFirst")
    public ResponseEntity<Habit> moveHabitAsFirst(@PathVariable Integer userId, @PathVariable Integer habitId) {
        return  new ResponseEntity<>(habitService.moveHabitAsFirst(userId, habitId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/habits/{habitId}/move/after")
    public ResponseEntity<Habit> moveLabelAfter(@RequestBody IdRequest request, @PathVariable Integer userId,
                                                @PathVariable Integer habitId) {
        return new ResponseEntity<>(habitService.moveHabitAfter(request, userId, habitId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/habits/{habitId}/priority")
    public ResponseEntity<Habit> updateHabitPriority(@RequestBody PriorityRequest request, @PathVariable Integer habitId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(habitService.updateHabitPriority(request, habitId, userId), HttpStatus.OK);
    }
}
