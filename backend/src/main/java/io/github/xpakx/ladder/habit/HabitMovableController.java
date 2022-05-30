package io.github.xpakx.ladder.habit;

import io.github.xpakx.ladder.habit.dto.HabitRequest;
import io.github.xpakx.ladder.common.dto.IdRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/{userId}")
@AllArgsConstructor
public class HabitMovableController {
    private final HabitMovableService habitService;

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
    @PostMapping("/habits/{habitId}/after")
    public ResponseEntity<Habit> addHabitAfter(@RequestBody HabitRequest request, @PathVariable Integer userId,
                                               @PathVariable Integer habitId) {
        return  new ResponseEntity<>(habitService.addHabitAfter(request, userId, habitId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/habits/{habitId}/before")
    public ResponseEntity<Habit> addHabitBefore(@RequestBody HabitRequest request, @PathVariable Integer userId,
                                                @PathVariable Integer habitId) {
        return new ResponseEntity<>(habitService.addHabitBefore(request, userId, habitId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/habits/{habitId}/duplicate")
    public ResponseEntity<Habit> duplicateTask(@PathVariable Integer habitId, @PathVariable Integer userId) {
        return new ResponseEntity<>(habitService.duplicate(habitId, userId), HttpStatus.CREATED);
    }
}
