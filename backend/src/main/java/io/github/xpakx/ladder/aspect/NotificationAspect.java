package io.github.xpakx.ladder.aspect;

import io.github.xpakx.ladder.entity.*;
import io.github.xpakx.ladder.entity.dto.CollabNotificationRequest;
import io.github.xpakx.ladder.entity.dto.NotificationRequest;
import io.github.xpakx.ladder.repository.ProjectRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
import io.github.xpakx.ladder.service.NotificationService;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Aspect
@Service
@AllArgsConstructor
public class NotificationAspect {
    private final NotificationService notificationService;
    private final UserAccountRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    @AfterReturning(value="@annotation(NotifyOnProjectChange)", returning="response")
    public void notifyOnProjectChange(Project response) throws Throwable {
        NotificationRequest notification = NotificationRequest.builder()
                .userId(response.getOwner().getId())
                .time(response.getModifiedAt())
                .type("UPDATE")
                .build();
        if(response.isCollaborative()) {
            sendCollabUpdateNotification(
                    response.getModifiedAt(),
                    userRepository.getCollaboratorsIdByProjectId(response.getId())
            );
        }
        notificationService.sendNotification(notification);
    }

    @After(value="@annotation(NotifyOnProjectDeletion) && args(projectId, userId)", argNames = "projectId,userId")
    public void notifyOnProjectDeletion(Integer projectId, Integer userId) throws Throwable {
        NotificationRequest notification = NotificationRequest.builder()
                .userId(userId)
                .time(LocalDateTime.now())
                .type("DELETE_PROJ")
                .id(projectId)
                .build();
        notificationService.sendNotification(notification);
    }

    @Around(value="@annotation(NotifyOnProjectDeletion) && args(projectId, ..)", argNames = "projectId")
    public void notifyCollabOnProjectDeletion(ProceedingJoinPoint joinPoint, Integer projectId) throws Throwable {
        List<Integer> collab = new ArrayList<>();
        if(projectRepository.existsByIdAndCollaborative(projectId, true)) {
            collab = userRepository.getCollaboratorsIdByProjectId(projectId);
        }
        joinPoint.proceed();
        if(collab.size() > 0) {
            sendCollabDeleteNotification(LocalDateTime.now(), projectId, "PROJ", collab);
        }
    }

    @AfterReturning(value="@annotation(NotifyOnLabelChange)", returning="response")
    public void notifyOnLabelChange(Label response) throws Throwable {
        NotificationRequest notification = NotificationRequest.builder()
                .userId(response.getOwner().getId())
                .time(response.getModifiedAt())
                .type("UPDATE")
                .build();
        notificationService.sendNotification(notification);
    }

    @After(value="@annotation(NotifyOnLabelDeletion) && args(labelId, userId)", argNames = "labelId,userId")
    public void notifyOnLabelDeletion(Integer labelId, Integer userId) throws Throwable {
        NotificationRequest notification = NotificationRequest.builder()
                .userId(userId)
                .time(LocalDateTime.now())
                .type("DELETE_LABEL")
                .id(labelId)
                .build();
        notificationService.sendNotification(notification);
    }

    @AfterReturning(value="@annotation(NotifyOnTaskChange)", returning="response")
    public void notifyOnTaskChange(Task response) throws Throwable {
        NotificationRequest notification = NotificationRequest.builder()
                .userId(response.getOwner().getId())
                .time(response.getModifiedAt())
                .type("UPDATE")
                .build();
        List<Integer> collab = userRepository.getCollaboratorsIdByTaskId(response.getId());
        if(collab.size() > 0) {
            sendCollabUpdateNotification(LocalDateTime.now(), collab);
        }
        notificationService.sendNotification(notification);
    }

    @AfterReturning(value="@annotation(NotifyOnTasksChange)", returning="response")
    public void notifyOnTasksChange(List<Task> response) throws Throwable {
        if(response.size() > 0) {
            Task task = response.stream()
                    .max((a,b) -> a.getModifiedAt().compareTo(b.getModifiedAt()))
                    .get();
            NotificationRequest notification = NotificationRequest.builder()
                    .userId(task.getOwner().getId())
                    .time(task.getModifiedAt())
                    .type("UPDATE")
                    .build();
            List<Integer> collab = userRepository.getCollaboratorsIdByTaskId(task.getId());
            if(collab.size() > 0) {
                sendCollabUpdateNotification(task.getModifiedAt(), collab);
            }
            notificationService.sendNotification(notification);
        }
    }

    @After(value="@annotation(NotifyOnTaskDeletion) && args(taskId, userId)", argNames = "taskId,userId")
    public void notifyOnTaskDeletion(Integer taskId, Integer userId) throws Throwable {
        NotificationRequest notification = NotificationRequest.builder()
                .userId(userId)
                .time(LocalDateTime.now())
                .type("DELETE_TASK")
                .id(taskId)
                .build();
        notificationService.sendNotification(notification);
    }

    @Around(value="@annotation(NotifyOnTaskDeletion) && args(taskId, ..)", argNames = "taskId")
    public void notifyCollabOnTaskDeletion(ProceedingJoinPoint joinPoint, Integer taskId) throws Throwable {
        List<Integer> collab = userRepository.getCollaboratorsIdByTaskId(taskId);
        joinPoint.proceed();
        if(collab.size() > 0) {
            sendCollabDeleteNotification(LocalDateTime.now(), taskId, "TASK", collab);
        }
    }

    @AfterReturning(value="@annotation(NotifyOnHabitChange)", returning="response")
    public void notifyOnHabitChange(Habit response) throws Throwable {
        NotificationRequest notification = NotificationRequest.builder()
                .userId(response.getOwner().getId())
                .time(response.getModifiedAt())
                .type("UPDATE")
                .build();
        notificationService.sendNotification(notification);
    }

    @After(value="@annotation(NotifyOnHabitDeletion) && args(habitId, userId)", argNames = "habitId,userId")
    public void notifyOnHabitDeletion(Integer habitId, Integer userId) throws Throwable {
        NotificationRequest notification = NotificationRequest.builder()
                .userId(userId)
                .time(LocalDateTime.now())
                .type("DELETE_HABIT")
                .id(habitId)
                .build();
        notificationService.sendNotification(notification);
    }

    @AfterReturning(value="@annotation(NotifyOnHabitCompletion)", returning="response")
    public void notifyOnHabitCompletion(HabitCompletion response) throws Throwable {
        NotificationRequest notification = NotificationRequest.builder()
                .userId(response.getOwner().getId())
                .time(response.getDate())
                .type("UPDATE")
                .build();
        notificationService.sendNotification(notification);
    }

    @AfterReturning(value="@annotation(NotifyOnFilterChange)", returning="response")
    public void notifyOnFilterChange(Filter response) throws Throwable {
        NotificationRequest notification = NotificationRequest.builder()
                .userId(response.getOwner().getId())
                .time(response.getModifiedAt())
                .type("UPDATE")
                .build();
        notificationService.sendNotification(notification);
    }

    @After(value="@annotation(NotifyOnFilterDeletion) && args(filterId, userId)", argNames = "filterId,userId")
    public void notifyOnFilterDeletion(Integer filterId, Integer userId) throws Throwable {
        NotificationRequest notification = NotificationRequest.builder()
                .userId(userId)
                .time(LocalDateTime.now())
                .type("DELETE_FILTER")
                .id(filterId)
                .build();
        notificationService.sendNotification(notification);
    }

    @After(value="@annotation(NotifyOnImport) && args(userId, ..)", argNames = "userId")
    public void notifyOnImport(Integer userId) throws Throwable {
        NotificationRequest notification = NotificationRequest.builder()
                .userId(userId)
                .time(LocalDateTime.now())
                .type("UPDATE")
                .build();
        notificationService.sendNotification(notification);
    }

    private void sendCollabUpdateNotification(LocalDateTime modifiedAt, List<Integer> collaborators) {
        CollabNotificationRequest notification = CollabNotificationRequest.builder()
                .collabId(collaborators)
                .time(modifiedAt)
                .type("UPDATE")
                .build();
    }

    private void sendCollabDeleteNotification(LocalDateTime modifiedAt, Integer id, String type, List<Integer> collaborators) {
        CollabNotificationRequest notification = CollabNotificationRequest.builder()
                .collabId(collaborators)
                .time(modifiedAt)
                .type("DELETE_"+type)
                .id(id)
                .build();
    }
}
