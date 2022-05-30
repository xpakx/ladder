package io.github.xpakx.ladder.habit.dto;

import java.time.LocalDateTime;

public interface HabitCompletionDetails {
    Integer getId();
    LocalDateTime getDate();
    boolean getPositive();
    HabitWithId getHabit();
}
