package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.dto.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

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

    public void sendNotification(Integer userId, LocalDateTime time) {
        try {
            NotificationRequest notification = NotificationRequest.builder()
                    .userId(userId)
                    .time(time)
                    .build();
            ResponseEntity<?> r = template
                    .postForEntity(uri  + "/notification", notification, Object.class);
            LOG.info("Notification service response: {}", r.getStatusCode());
        } catch(Exception e) {
            LOG.error("Problem with sending notification.", e);
        }
    }
}
