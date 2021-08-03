package io.github.xpakx.ladder.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;

    private LocalDateTime due;
    private LocalDateTime completedAt;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task parent;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "task_id")
    private Project project;

    @JsonIgnore
    @ManyToMany(mappedBy = "tasks")
    private Set<Label> labels;
}
