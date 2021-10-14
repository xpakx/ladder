package io.github.xpakx.ladder.aspect;

import io.github.xpakx.ladder.entity.Project;
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
        notificationService.sendNotification(response.getOwner().getId(), response.getModifiedAt());
    }
}
