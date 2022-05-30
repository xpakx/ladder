package io.github.xpakx.ladder.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.xpakx.ladder.label.Label;
import io.github.xpakx.ladder.comment.TaskComment;
import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.project.Project;
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
@NamedEntityGraphs({
    @NamedEntityGraph(name = "task-with-children",
            attributeNodes = {@NamedAttributeNode("children")}
    ),
    @NamedEntityGraph(name = "task-with-labels",
            attributeNodes = {@NamedAttributeNode("labels"),
				              @NamedAttributeNode("project"),
				              @NamedAttributeNode("assigned")}
    )
})
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;
    private String description;

    private LocalDateTime due;
    private boolean timeboxed;
    private LocalDateTime completedAt;
    private boolean completed;
    private boolean collapsed;
    private boolean archived;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    private Integer projectOrder;
    private Integer dailyViewOrder;
    private Integer priority;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Task parent;

    @JsonIgnore
    @OneToMany(mappedBy = "parent",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.REMOVE, CascadeType.PERSIST, CascadeType.MERGE})
    private List<Task> children;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @JsonIgnore
    @ManyToMany(cascade={CascadeType.MERGE})
    @JoinTable(name="task_label",
            joinColumns={@JoinColumn(name="task_id")},
            inverseJoinColumns={@JoinColumn(name="label_id")})
    private Set<Label> labels;

    @JsonIgnore
    @OneToMany(mappedBy = "task",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.REMOVE, CascadeType.MERGE})
    private List<TaskComment> comments;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_account_id")
    private UserAccount owner;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "assigned_user_id")
    private UserAccount assigned;
}
