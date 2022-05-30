package io.github.xpakx.ladder.habit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.xpakx.ladder.user.UserAccount;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitCompletion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDateTime date;
    private boolean positive;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "habit_id")
    private Habit habit;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_account_id")
    private UserAccount owner;
}
