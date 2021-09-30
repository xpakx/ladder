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
@NamedEntityGraphs({
    @NamedEntityGraph(name = "task-with-children",
            attributeNodes = {@NamedAttributeNode("children")}
    ),
    @NamedEntityGraph(name = "task-with-labels",
            attributeNodes = {@NamedAttributeNode("labels"),
				              @NamedAttributeNode("project")}
    )
})
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;
    private String description;

    private LocalDateTime due;
    private LocalDateTime completedAt;
    private boolean completed;
    private boolean collapsed;
    private LocalDateTime createdAt;

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
    @ManyToMany(mappedBy = "tasks")
    private Set<Label> labels;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_account_id")
    private UserAccount owner;
}
