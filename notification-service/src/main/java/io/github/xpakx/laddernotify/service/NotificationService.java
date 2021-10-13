package io.github.xpakx.laddernotify.service;

import io.github.xpakx.laddernotify.entity.Notification;
import io.github.xpakx.laddernotify.entity.NotificationRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {
    List<SseEmitter> emitters = new ArrayList<>();

    public void addEmitter(SseEmitter emitter) {
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitters.add(emitter);
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
        emitter.send(SseEmitter
                .event()
                .name(String.valueOf(username))
                .data(payload));
    }
}
