package io.github.xpakx.ladder.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Label {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String color;
    private Integer generalOrder;
    private boolean favorite;

    @JsonIgnore
    @ManyToMany(mappedBy = "labels")
    private Set<Task> tasks;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_account_id")
    private UserAccount owner;
}
