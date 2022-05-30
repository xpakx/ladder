package io.github.xpakx.ladder.label;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.task.Task;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Label {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String color;
    private Integer generalOrder;
    private boolean favorite;

    private LocalDateTime modifiedAt;

    @JsonIgnore
    @ManyToMany(mappedBy = "labels")
    private Set<Task> tasks;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_account_id")
    private UserAccount owner;
}
