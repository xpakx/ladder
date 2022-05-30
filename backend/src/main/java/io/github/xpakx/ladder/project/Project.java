package io.github.xpakx.ladder.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.xpakx.ladder.collaboration.Collaboration;
import io.github.xpakx.ladder.habit.Habit;
import io.github.xpakx.ladder.task.Task;
import io.github.xpakx.ladder.user.UserAccount;
import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@NamedEntityGraphs({
        @NamedEntityGraph(name = "project-with-children",
                attributeNodes = {@NamedAttributeNode("children"),
                        @NamedAttributeNode(value = "tasks")}
        ),
        @NamedEntityGraph(name = "project-with-collaborators",
                attributeNodes = {@NamedAttributeNode("collaborators")}
        )
})
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private boolean favorite;
    private String color;
    private Integer generalOrder;
    private boolean collapsed;
    private boolean archived;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project parent;

    @JsonIgnore
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE, CascadeType.PERSIST, CascadeType.MERGE})
    private List<Project> children;

    @JsonIgnore
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE, CascadeType.PERSIST, CascadeType.MERGE})
    @Where(clause = "parent_id is NULL")
    private List<Task> tasks;

    @JsonIgnore
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE, CascadeType.PERSIST, CascadeType.MERGE})
    private List<Habit> habits;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_account_id")
    private UserAccount owner;

    @JsonIgnore
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade={CascadeType.REMOVE, CascadeType.MERGE})
    private List<Collaboration> collaborators;
    private boolean collaborative;
}
