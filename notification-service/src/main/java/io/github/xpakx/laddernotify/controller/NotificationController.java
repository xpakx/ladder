package io.github.xpakx.laddernotify.controller;

import io.github.xpakx.laddernotify.entity.NotificationRequest;
import io.github.xpakx.laddernotify.service.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Controller
@CrossOrigin("*")
@AllArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/subscription")
    public SseEmitter subscribe() {
        SseEmitter sseEmitter = new SseEmitter(24 * 60 * 60 * 1000L);
        notificationService.addEmitter(sseEmitter);
        return sseEmitter;
    }

    @PostMapping("/notification")
    public ResponseEntity<?> send(@RequestBody NotificationRequest request) {
        notificationService.pushNotification(request);
        return ResponseEntity.ok().build();
    }
}
