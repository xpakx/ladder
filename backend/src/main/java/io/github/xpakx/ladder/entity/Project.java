package io.github.xpakx.ladder.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;

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
}
