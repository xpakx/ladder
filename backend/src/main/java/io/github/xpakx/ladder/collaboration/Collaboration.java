package io.github.xpakx.ladder.collaboration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.project.Project;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_account_id", "project_id"}))
public class Collaboration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private boolean accepted;
    private boolean taskCompletionAllowed;
    private boolean editionAllowed;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_account_id")
    private UserAccount owner;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    private LocalDateTime modifiedAt;
}