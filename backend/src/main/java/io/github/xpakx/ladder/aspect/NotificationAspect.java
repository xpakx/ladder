package io.github.xpakx.ladder.aspect;

import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.dto.NotificationRequest;
import lombok.AllArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Service;

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

    @AfterReturning(value="@annotation(NotifyOnProjectDeletion)", returning="response")
    public void notifyOnProjectDeletion(Project response) throws Throwable {
        NotificationRequest notification = NotificationRequest.builder()
                .userId(response.getOwner().getId())
                .time(response.getModifiedAt())
                .type("DELETE")
                .id(response.getId())
                .build();
        notificationService.sendNotification(notification);
    }
}
