package io.github.xpakx.ladder.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.xpakx.ladder.label.Label;
import io.github.xpakx.ladder.project.Project;
import io.github.xpakx.ladder.task.Task;
import lombok.*;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true)
    private String username;
    @JsonIgnore
    private String password;
    
    private boolean projectCollapsed;
    private String collaborationToken;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "user_roles",
            joinColumns = {
                    @JoinColumn(name = "user_account_id", referencedColumnName = "id",
                            nullable = false, updatable = false)},
            inverseJoinColumns = {
                    @JoinColumn(name = "user_role_id", referencedColumnName = "id",
                            nullable = false, updatable = false)})
    private Set<UserRole> roles;

    @JsonIgnore
    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private List<Project> projects;

    @JsonIgnore
    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private List<Task> tasks;

    @JsonIgnore
    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private List<Label> labels;
}
