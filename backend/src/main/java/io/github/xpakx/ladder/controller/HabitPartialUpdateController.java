package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.entity.Habit;
import io.github.xpakx.ladder.entity.HabitCompletion;
import io.github.xpakx.ladder.entity.dto.BooleanRequest;
import io.github.xpakx.ladder.entity.dto.IdRequest;
import io.github.xpakx.ladder.entity.dto.PriorityRequest;
import io.github.xpakx.ladder.service.HabitPartialUpdateService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/{userId}")
@AllArgsConstructor
public class HabitPartialUpdateController {
    private final HabitPartialUpdateService habitService;

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/habits/{habitId}/priority")
    public ResponseEntity<Habit> updateHabitPriority(@RequestBody PriorityRequest request, @PathVariable Integer habitId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(habitService.updateHabitPriority(request, habitId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/habits/{habitId}/complete")
    public ResponseEntity<HabitCompletion> completeHabit(@RequestBody BooleanRequest request, @PathVariable Integer habitId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(habitService.completeHabit(request, habitId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/habits/{habitId}/project")
    public ResponseEntity<Habit> updateHabitProject(@RequestBody IdRequest request, @PathVariable Integer habitId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(habitService.updateHabitProject(request, habitId, userId), HttpStatus.OK);
    }
}
