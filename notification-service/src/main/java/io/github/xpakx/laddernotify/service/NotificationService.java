package io.github.xpakx.laddernotify.service;

import io.github.xpakx.laddernotify.entity.Notification;
import io.github.xpakx.laddernotify.entity.NotificationRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {
    List<SseEmitter> emitters = new ArrayList<>();

    public SseEmitter subscribe(Integer userId) {
        SseEmitter emitter = new SseEmitter(24 * 60 * 60 * 1000L);
        emitter.onCompletion(() -> {
			emitters.remove(emitter);
			});
        emitter.onTimeout(emitter::complete);
        emitters.add(emitter);
        return emitter;
    }

    public void pushNotification(NotificationRequest request) {
        List<SseEmitter> deadEmitters = new ArrayList<>();

        Notification payload = new Notification(request.getTime());

        emitters.forEach(emitter -> {
            try {
                sendNotification(request.getUserId(), payload, emitter);

            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        });

        emitters.removeAll(deadEmitters);
    }

    private void sendNotification(Integer username, Notification payload, SseEmitter emitter) throws IOException {
        emitter.send(payload, MediaType.APPLICATION_JSON);

    }
}
