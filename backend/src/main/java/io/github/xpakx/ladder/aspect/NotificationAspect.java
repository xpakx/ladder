package io.github.xpakx.ladder.aspect;

import io.github.xpakx.ladder.entity.Label;
import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.dto.NotificationRequest;
import lombok.AllArgsConstructor;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Aspect
@Service
@AllArgsConstructor
public class NotificationAspect {
    private final io.github.xpakx.ladder.service.NotificationService notificationService;


    @AfterReturning(value="@annotation(NotifyOnProjectChange)", returning="response")
    public void notifyOnProjectChange(Project response) throws Throwable {
        NotificationRequest notification = NotificationRequest.builder()
                .userId(response.getOwner().getId())
                .time(response.getModifiedAt())
                .type("UPDATE")
                .build();
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
}
