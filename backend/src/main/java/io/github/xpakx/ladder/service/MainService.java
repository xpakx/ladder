package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.dto.*;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MainService {
    private final UserAccountRepository userAccountRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final LabelRepository labelRepository;
    private final HabitRepository habitRepository;
    private final FilterRepository filterRepository;
    private final HabitCompletionRepository habitCompletionRepository;
    private final CollaborationRepository collaborationRepository;

    public UserWithData getAll(Integer userId) {
        UserWithData result = new UserWithData();
        result.setId(userId);
        result.setUsername(userAccountRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("No user with id " + userId)).getUsername());
        result.setProjectCollapsed(true);
        result.setProjects(projectRepository.findByOwnerIdAndArchived(userId, false, ProjectDetails.class));
        result.setTasks(taskRepository.findByOwnerIdAndArchived(userId, false, TaskDetails.class));
        result.setLabels(labelRepository.findByOwnerId(userId, LabelDetails.class));
        result.setHabits(habitRepository.findByOwnerIdAndArchived(userId, false, HabitDetails.class));
        result.setFilters(filterRepository.findByOwnerId(userId, FilterDetails.class));

        List<CollaborationWithProject> collabProjects = collaborationRepository.findCollabsByUserIdAndNotArchived(userId, CollaborationWithProject.class);
        result.setCollabs(collabProjects);
        result.setCollabTasks(taskRepository.findByProjectIdInAndArchived(
                collabProjects.stream()
                        .filter((a) -> a.getProject() != null)
                        .map((a) -> a.getProject().getId())
                        .collect(Collectors.toList()),
                false,
                CollabTaskDetails.class
        ));

        LocalDateTime today = LocalDateTime.now();
        today = today.minusHours(today.getHour())
                    .minusMinutes(today.getMinute())
                    .minusSeconds(today.getSecond());
        result.setTodayHabitCompletions(habitCompletionRepository.findByOwnerIdAndDateAfter(userId, today, HabitCompletionDetails.class));

        return result;
    }

    public SyncData sync(NotificationDateRequest time, Integer userId) {
        SyncData result = new SyncData();
        result.setProjects(
                projectRepository.findByOwnerIdAndModifiedAtAfter(userId, time.getDate(), ProjectDetails.class)
        );
        result.setTasks(
                taskRepository.findByOwnerIdAndModifiedAtAfter(userId, time.getDate(), TaskDetails.class)
        );
        result.setLabels(
                labelRepository.findByOwnerIdAndModifiedAtAfter(userId, time.getDate(), LabelDetails.class)
        );
        result.setHabits(
                habitRepository.findByOwnerIdAndModifiedAtAfter(userId, time.getDate(), HabitDetails.class)
        );
        result.setHabitCompletions(
                habitCompletionRepository.findByOwnerIdAndDateAfter(userId, time.getDate(), HabitCompletionDetails.class)
        );
        result.setFilters(
                filterRepository.findByOwnerIdAndModifiedAtAfter(userId, time.getDate(), FilterDetails.class)
        );
        List<CollaborationWithProject> collabProjects = collaborationRepository.findCollabsByUserIdAndNotArchivedAndModifiedAtAfter(
                userId,
                CollaborationWithProject.class,
                time.getDate()
        );
        result.setCollabs(collabProjects);
        result.setCollabTasks(taskRepository.findByProjectIdInAndArchivedAndModifiedAtAfter(
                collabProjects.stream()
                        .filter((a) -> a.getProject() != null)
                        .map((a) -> a.getProject().getId())
                        .collect(Collectors.toList()),
                false,
                CollabTaskDetails.class,
                time.getDate()
        ));

        return result;
    }

    public List<CollabTaskDetails> syncCollabTasks(IdCollectionRequest ids, Integer userId) {
        return  taskRepository.getTasksInProjectsForCollaborator(
                ids.getIds(),
                userId,
                CollabTaskDetails.class
        );
    }
}
