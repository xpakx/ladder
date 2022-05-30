package io.github.xpakx.ladder.filter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.xpakx.ladder.user.UserAccount;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Filter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String searchString;
    private LocalDateTime modifiedAt;
    private String color;
    private boolean favorite;
    private Integer generalOrder;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_account_id")
    private UserAccount owner;
}
