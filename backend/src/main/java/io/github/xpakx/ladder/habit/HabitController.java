package io.github.xpakx.ladder.habit;

import io.github.xpakx.ladder.habit.Habit;
import io.github.xpakx.ladder.habit.dto.HabitDetails;
import io.github.xpakx.ladder.habit.dto.HabitRequest;
import io.github.xpakx.ladder.habit.HabitService;
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
    @GetMapping("/habits/{habitId}")
    public ResponseEntity<HabitDetails> getHabit(@PathVariable Integer habitId, @PathVariable Integer userId) {
        return new ResponseEntity<>(habitService.getHabitById(habitId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/habits/{habitId}")
    public ResponseEntity<Habit> updateHabit(@RequestBody HabitRequest request, @PathVariable Integer habitId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(habitService.updateHabit(request, habitId, userId), HttpStatus.OK);
    }
}
