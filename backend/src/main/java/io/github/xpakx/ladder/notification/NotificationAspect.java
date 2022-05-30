package io.github.xpakx.ladder.notification;

import io.github.xpakx.ladder.collaboration.Collaboration;
import io.github.xpakx.ladder.common.dto.BooleanRequest;
import io.github.xpakx.ladder.notification.dto.CollabNotificationRequest;
import io.github.xpakx.ladder.notification.dto.NotificationRequest;
import io.github.xpakx.ladder.filter.Filter;
import io.github.xpakx.ladder.habit.Habit;
import io.github.xpakx.ladder.habit.HabitCompletion;
import io.github.xpakx.ladder.label.Label;
import io.github.xpakx.ladder.project.Project;
import io.github.xpakx.ladder.project.ProjectRepository;
import io.github.xpakx.ladder.user.UserAccountRepository;
import io.github.xpakx.ladder.task.Task;
import lombok.AllArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
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

    @AfterReturning(value="@annotation(NotifyOnProjectDeletion) && args(projectId, userId)", argNames = "projectId,userId")
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

    @AfterReturning(value="@annotation(NotifyOnLabelDeletion) && args(labelId, userId)", argNames = "labelId,userId")
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

    @AfterReturning(value="@annotation(NotifyOnTaskDeletion) && args(taskId, userId)", argNames = "taskId,userId")
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

    @AfterReturning(value="@annotation(NotifyOnHabitDeletion) && args(habitId, userId)", argNames = "habitId,userId")
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

    @AfterReturning(value="@annotation(NotifyOnFilterDeletion) && args(filterId, userId)", argNames = "filterId,userId")
    public void notifyOnFilterDeletion(Integer filterId, Integer userId) throws Throwable {
        NotificationRequest notification = NotificationRequest.builder()
                .userId(userId)
                .time(LocalDateTime.now())
                .type("DELETE_FILTER")
                .id(filterId)
                .build();
        notificationService.sendNotification(notification);
    }

    @AfterReturning(value="@annotation(NotifyOnImport) && args(userId, ..)", argNames = "userId")
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
        notificationService.sendCollabNotification(notification);
    }

    private void sendCollabDeleteNotification(LocalDateTime modifiedAt, Integer id, String type, List<Integer> collaborators) {
        CollabNotificationRequest notification = CollabNotificationRequest.builder()
                .collabId(collaborators)
                .time(modifiedAt)
                .type("DELETE_C"+type)
                .id(id)
                .build();
        notificationService.sendCollabNotification(notification);
    }

    @AfterReturning(value="@annotation(NotifyOnCollaborationDeletion) && args(collabId, projectId, ownerId)", argNames = "collabId, projectId, ownerId")
    public void notifyOnCollabDeletion(Integer collabId, Integer projectId, Integer ownerId) throws Throwable {
        LocalDateTime now = LocalDateTime.now();
        NotificationRequest notification = NotificationRequest.builder()
                .userId(ownerId)
                .time(now)
                .type("UPDATE")
                .build();
        notificationService.sendNotification(notification);
        NotificationRequest collabNotification = NotificationRequest.builder()
                .userId(collabId)
                .time(now)
                .type("DELETE_CPROJ")
                .id(projectId)
                .build();
        notificationService.sendNotification(collabNotification);
    }

    @AfterReturning(value="@annotation(NotifyOnCollaborationAcceptation) && args(request, userId, collabId)", argNames = "request, userId, collabId")
    public void notifyOnCollabAcceptation(BooleanRequest request, Integer userId, Integer collabId) throws Throwable {
        LocalDateTime now = LocalDateTime.now();
        if(request.isFlag()) {
            NotificationRequest notification = NotificationRequest.builder()
                    .userId(userId)
                    .time(now)
                    .type("UPDATE")
                    .build();
            notificationService.sendNotification(notification);
        } else {
            Optional<Integer> projectId = projectRepository.getIdByCollaborationId(collabId);
            if(projectId.isPresent()) {
                NotificationRequest collabNotification = NotificationRequest.builder()
                        .userId(userId)
                        .time(now)
                        .type("DELETE_CPROJ")
                        .id(projectId.get())
                        .build();
                notificationService.sendNotification(collabNotification);
            }

        }
    }

    @AfterReturning(value="@annotation(NotifyOnCollaborationUnsubscription) && args(request, userId, projectId)", argNames = "request, userId, projectId")
    public void notifyOnCollabUnsubscription(BooleanRequest request, Integer userId, Integer projectId) throws Throwable {
        LocalDateTime now = LocalDateTime.now();
        if(request.isFlag()) {
            NotificationRequest notification = NotificationRequest.builder()
                    .userId(userId)
                    .time(now)
                    .type("UPDATE")
                    .build();
            notificationService.sendNotification(notification);
        } else {
            NotificationRequest collabNotification = NotificationRequest.builder()
                    .userId(userId)
                    .time(now)
                    .type("DELETE_CPROJ")
                    .id(projectId)
                    .build();
            notificationService.sendNotification(collabNotification);
        }
    }

    @AfterReturning(value="@annotation(Notify)", returning = "response")
    public void textNotificationAfterCollabInvitation(JoinPoint jp, Collaboration response) {
        MethodSignature signature = (MethodSignature) jp.getSignature();
        Method method = signature.getMethod();
        Notify annotation = method.getAnnotation(Notify.class);
        NotificationRequest notification = NotificationRequest.builder()
                .userId(response.getOwner().getId())
                .time(LocalDateTime.now())
                .type("MSG " + annotation.message())
                .build();
        notificationService.sendNotification(notification);
    }

    @AfterReturning(value="@annotation(NotifyOnCollaborationChange)", returning = "result")
    public void notifyOnCollaborationUpdate(Collaboration result) throws Throwable {
        NotificationRequest notification = NotificationRequest.builder()
                .userId(result.getOwner().getId())
                .time(result.getModifiedAt())
                .type("UPDATE")
                .build();
        notificationService.sendNotification(notification);
    }
}
