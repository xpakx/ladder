package io.github.xpakx.ladder.notification;

import io.github.xpakx.ladder.notification.dto.CollabNotificationRequest;
import io.github.xpakx.ladder.notification.dto.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificationService {
    private final RestTemplate template;
    private final String uri;
    private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

    public NotificationService(RestTemplateBuilder templateBuilder, @Value("${service.notification.host}") final
                               String uri) {
        this.template = templateBuilder.build();
        this.uri = uri;
    }

    public void sendNotification(NotificationRequest notification) {
        try {
            ResponseEntity<?> r = template
                    .postForEntity(uri  + "/notification", notification, Object.class);
            LOG.info("Notification service response: {}", r.getStatusCode());
        } catch(Exception e) {
            LOG.error("Problem with sending notification.", e);
        }
    }

    public void sendCollabNotification(CollabNotificationRequest notification) {
        try {
            ResponseEntity<?> r = template
                    .postForEntity(uri  + "/collab/notification", notification, Object.class);
            LOG.info("Notification service response: {}", r.getStatusCode());
        } catch(Exception e) {
            LOG.error("Problem with sending notification.", e);
        }
    }
}
