package io.github.xpakx.ladder.collaboration;

import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.common.dto.BooleanRequest;
import io.github.xpakx.ladder.collaboration.dto.CollabTokenResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/{userId}/collab")
@AllArgsConstructor
public class CollabManagementController {
    private final CollabManagementService collabService;

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/{collabId}/edit")
    public ResponseEntity<Collaboration> updateEdit(@RequestBody BooleanRequest request, @PathVariable Integer collabId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(collabService.updateCollabEdit(request, collabId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/{collabId}/complete")
    public ResponseEntity<Collaboration> updateComplete(@RequestBody BooleanRequest request, @PathVariable Integer collabId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(collabService.updateCollabComplete(request, collabId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/token")
    public ResponseEntity<UserAccount> getNewToken(@PathVariable Integer userId) {
        return  new ResponseEntity<>(collabService.getNewToken(userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/token")
    public ResponseEntity<CollabTokenResponse> getToken(@PathVariable Integer userId) {
        return  new ResponseEntity<>(collabService.getToken(userId), HttpStatus.OK);
    }
}
