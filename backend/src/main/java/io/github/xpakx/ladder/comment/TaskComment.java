package io.github.xpakx.ladder.comment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.task.Task;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String content;
    private LocalDateTime createdAt;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_account_id")
    private UserAccount owner;
}
