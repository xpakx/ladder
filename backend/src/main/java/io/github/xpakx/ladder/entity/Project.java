package io.github.xpakx.ladder.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
    private List<Project> children;

    @JsonIgnore
    @OneToMany(mappedBy = "project", fetch = FetchType.EAGER)
    private List<Task> tasks;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_account_id")
    private UserAccount owner;
}
