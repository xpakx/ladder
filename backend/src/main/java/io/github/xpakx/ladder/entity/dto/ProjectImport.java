package io.github.xpakx.ladder.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.xpakx.ladder.entity.Habit;
import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.UserAccount;
import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectImport {
    private Integer id;
    private String name;
    private boolean favorite;
    private String color;
    private Integer generalOrder;
    private boolean collapsed;
    private boolean archived;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Integer parentId;
}
