package io.github.xpakx.ladder.collaboration;

import io.github.xpakx.ladder.collaboration.dto.CollaborationDetails;
import io.github.xpakx.ladder.common.dto.BooleanRequest;
import io.github.xpakx.ladder.common.dto.DateRequest;
import io.github.xpakx.ladder.common.dto.IdRequest;
import io.github.xpakx.ladder.common.dto.PriorityRequest;
import io.github.xpakx.ladder.task.Task;
import io.github.xpakx.ladder.task.dto.AddTaskRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/{userId}/collab")
@AllArgsConstructor
public class CollabController {
    private final CollabService collabService;

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<Task> addTask(@RequestBody AddTaskRequest request,
                                                 @PathVariable Integer projectId,
                                                 @PathVariable Integer userId) {
        return  new ResponseEntity<>(collabService.addTask(request, projectId, userId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/tasks/{taskId}/after")
    public ResponseEntity<Task> addTaskAfter(@RequestBody AddTaskRequest request,
                                             @PathVariable Integer userId,
                                             @PathVariable Integer taskId) {
        return  new ResponseEntity<>(collabService.addTaskAfter(request, userId, taskId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/tasks/{taskId}/children")
    public ResponseEntity<Task> addTaskAsChild(@RequestBody AddTaskRequest request,
                                               @PathVariable Integer userId,
                                               @PathVariable Integer taskId) {
        return  new ResponseEntity<>(collabService.addTaskAsChild(request, userId, taskId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/tasks/{taskId}/before")
    public ResponseEntity<Task> addTaskBefore(@RequestBody AddTaskRequest request,
                                              @PathVariable Integer userId,
                                              @PathVariable Integer taskId) {
        return  new ResponseEntity<>(collabService.addTaskBefore(request, userId, taskId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable Integer taskId, @PathVariable Integer userId) {
        collabService.deleteTask(taskId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<Task> updateTask(@RequestBody AddTaskRequest request,
                                           @PathVariable Integer taskId,
                                           @PathVariable Integer userId) {
        return  new ResponseEntity<>(collabService.updateTask(request, taskId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/due")
    public ResponseEntity<Task> updateTaskDueDate(@RequestBody DateRequest request,
                                                  @PathVariable Integer taskId,
                                                  @PathVariable Integer userId) {
        return  new ResponseEntity<>(collabService.updateTaskDueDate(request, taskId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/priority")
    public ResponseEntity<Task> updateTaskPriority(@RequestBody PriorityRequest request,
                                                   @PathVariable Integer taskId,
                                                   @PathVariable Integer userId) {
        return  new ResponseEntity<>(collabService.updateTaskPriority(request, taskId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/completed")
    public ResponseEntity<Task> completeTask(@RequestBody BooleanRequest request,
                                             @PathVariable Integer taskId,
                                             @PathVariable Integer userId) {
        return  new ResponseEntity<>(collabService.completeTask(request, taskId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/move/after")
    public ResponseEntity<Task> moveTaskAfter(@RequestBody IdRequest request,
                                              @PathVariable Integer userId,
                                              @PathVariable Integer taskId) {
        return new ResponseEntity<>(collabService.moveTaskAfter(request, userId, taskId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/move/asChild")
    public ResponseEntity<Task> moveTaskAsFirstChild(@RequestBody IdRequest request,
                                                     @PathVariable Integer userId,
                                                     @PathVariable Integer taskId) {
        return new ResponseEntity<>(collabService.moveTaskAsFirstChild(request, userId, taskId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/collapse")
    public ResponseEntity<Task> updateTaskCollapsion(@RequestBody BooleanRequest request,
                                                     @PathVariable Integer taskId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(collabService.updateTaskCollapsion(request, taskId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/move/asFirst")
    public ResponseEntity<Task> moveTaskAsFirst(@PathVariable Integer userId, @PathVariable Integer taskId) {
        return  new ResponseEntity<>(collabService.moveTaskAsFirst(userId, taskId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/invitations")
    public ResponseEntity<List<CollaborationDetails>> getInvitations(@PathVariable Integer userId) {
        return  new ResponseEntity<>(collabService.getNotAcceptedCollaborations(userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/{collabId}/invitation")
    public ResponseEntity<Collaboration> updateCollabAcceptation(@RequestBody BooleanRequest request,
                                                                 @PathVariable Integer collabId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(collabService.updateAcceptation(request, userId, collabId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/projects/{projectId}/subscription")
    public ResponseEntity<List<Collaboration>> unsubscribe(@RequestBody BooleanRequest request,
                                                                 @PathVariable Integer projectId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(collabService.unsubscribe(request, userId, projectId), HttpStatus.OK);
    }
}
