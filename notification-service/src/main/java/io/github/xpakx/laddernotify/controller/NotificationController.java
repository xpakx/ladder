package io.github.xpakx.laddernotify.controller;

import io.github.xpakx.laddernotify.entity.CollabNotificationRequest;
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

    @GetMapping("/subscription/{userId}")
    public SseEmitter subscribe(@PathVariable Integer userId) {
        return notificationService.subscribe(userId);
    }

    @PostMapping("/notification")
    public ResponseEntity<?> send(@RequestBody NotificationRequest request) {
        notificationService.pushNotification(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/collab/notification")
    public ResponseEntity<?> sendCollab(@RequestBody CollabNotificationRequest request) {
        notificationService.pushNotification(request);
        return ResponseEntity.ok().build();
    }
}
