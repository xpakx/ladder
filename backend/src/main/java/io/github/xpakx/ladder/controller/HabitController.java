package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.entity.Habit;
import io.github.xpakx.ladder.entity.dto.*;
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
    public ResponseEntity<Habit> moveHabitAfter(@RequestBody IdRequest request, @PathVariable Integer userId,
                                                @PathVariable Integer habitId) {
        return new ResponseEntity<>(habitService.moveHabitAfter(request, userId, habitId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/habits/{habitId}")
    public ResponseEntity<HabitDetails> getHabit(@PathVariable Integer habitId, @PathVariable Integer userId) {
        return new ResponseEntity<>(habitService.getHabitById(habitId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/habits/{habitId}")
    public ResponseEntity<Habit> updateHabit(@RequestBody HabitRequest request, @PathVariable Integer habitId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(habitService.updateHabit(request, habitId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/habits/{habitId}/after")
    public ResponseEntity<Habit> addHabitAfter(@RequestBody HabitRequest request, @PathVariable Integer userId,
                                               @PathVariable Integer habitId) {
        return  new ResponseEntity<>(habitService.addHabitAfter(request, userId, habitId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/habits/{habitId}/before")
    public ResponseEntity<Habit> addHabitBefore(@RequestBody HabitRequest request, @PathVariable Integer userId,
                                                @PathVariable Integer habitId) {
        return  new ResponseEntity<>(habitService.addHabitBefore(request, userId, habitId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/habits/{habitId}/duplicate")
    public ResponseEntity<Habit> duplicateTask(@PathVariable Integer habitId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(habitService.duplicate(habitId, userId), HttpStatus.CREATED);
    }
}
