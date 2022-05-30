package io.github.xpakx.ladder.habit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.label.Label;
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
@NamedEntityGraph(name = "habit-with-labels",
            attributeNodes = {@NamedAttributeNode("labels"),
				              @NamedAttributeNode("project")}
    )
public class Habit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;
    private String description;
    private LocalDateTime modifiedAt;

    private Integer priority;
    private Integer generalOrder;
    private boolean allowPositive;
    private boolean allowNegative;
    private boolean archived;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_account_id")
    private UserAccount owner;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @JsonIgnore
    @ManyToMany(cascade={CascadeType.MERGE})
    @JoinTable(name="habit_label",
            joinColumns={@JoinColumn(name="habit_id")},
            inverseJoinColumns={@JoinColumn(name="label_id")})
    private Set<Label> labels;

    @JsonIgnore
    @OneToMany(mappedBy = "habit",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.REMOVE, CascadeType.PERSIST, CascadeType.MERGE})
    private List<HabitCompletion> completions;
}
