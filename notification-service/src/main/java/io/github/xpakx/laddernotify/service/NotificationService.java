package io.github.xpakx.laddernotify.service;

import io.github.xpakx.laddernotify.entity.CollabNotificationRequest;
import io.github.xpakx.laddernotify.entity.Notification;
import io.github.xpakx.laddernotify.entity.NotificationRequest;
import io.github.xpakx.laddernotify.utils.CustomEmitter;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class NotificationService {
    List<CustomEmitter> emitters = new ArrayList<>();

    public SseEmitter subscribe(Integer userId) {
        CustomEmitter emitter = new CustomEmitter(userId, 24 * 60 * 60 * 1000L);
        emitter.onCompletion(() -> {
			emitters.remove(emitter);
			});
        emitter.onTimeout(emitter::complete);
        emitters.add(emitter);
        return emitter;
    }

    public void pushNotification(NotificationRequest request) {
        List<CustomEmitter> deadEmitters = new ArrayList<>();

        Notification notification = new Notification(request.getTime(), request.getType(), request.getId());

        emitters.stream()
                .filter((a) -> Objects.equals(a.getUserId(), request.getUserId()))
                .forEach(emitter -> {
                    try {
                        sendNotification(notification, emitter);

                    } catch (IOException e) {
                        deadEmitters.add(emitter);
                    }
        });

        emitters.removeAll(deadEmitters);
    }

    public void pushNotification(CollabNotificationRequest request) {
        List<CustomEmitter> deadEmitters = new ArrayList<>();

        Notification notification = new Notification(request.getTime(), request.getType(), request.getId());

        emitters.stream()
                .filter((a) -> request.getCollabId().contains(a.getUserId()))
                .forEach(emitter -> {
                    try {
                        sendNotification(notification, emitter);

                    } catch (IOException e) {
                        deadEmitters.add(emitter);
                    }
                });

        emitters.removeAll(deadEmitters);
    }

    private void sendNotification(Notification payload, CustomEmitter emitter) throws IOException {
        emitter.send(payload, MediaType.APPLICATION_JSON);
    }
}
