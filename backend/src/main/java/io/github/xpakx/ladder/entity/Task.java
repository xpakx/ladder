package io.github.xpakx.ladder.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
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
    private String description;

    private LocalDateTime due;
    private LocalDateTime completedAt;
    private boolean completed;
    private LocalDateTime createdAt;

    private Integer order;
    private Integer dailyViewOrder;
    private Integer priority;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task parent;

    @JsonIgnore
    @OneToMany(mappedBy = "parent")
    private List<Task> children;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @JsonIgnore
    @ManyToMany(mappedBy = "tasks")
    private Set<Label> labels;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_account_id")
    private UserAccount owner;
}
