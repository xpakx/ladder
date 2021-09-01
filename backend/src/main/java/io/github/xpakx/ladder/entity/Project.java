package io.github.xpakx.ladder.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@NamedEntityGraph(name = "project-with-children",
        attributeNodes = {@NamedAttributeNode("children"),
                @NamedAttributeNode(value = "tasks", subgraph = "task-with-children")},
        subgraphs = {
        @NamedSubgraph(name = "task-with-children", attributeNodes = @NamedAttributeNode("children"))
        }
)
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private boolean favorite;
    private String color;

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
    @ManyToOne
    @JoinColumn(name = "user_account_id")
    private UserAccount owner;
}
