package io.github.xpakx.ladder.label;

import io.github.xpakx.ladder.common.dto.IdRequest;
import io.github.xpakx.ladder.label.dto.LabelRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/{userId}/labels")
@AllArgsConstructor
public class LabelMovableController {
    private final LabelMovableService labelService;

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/{labelId}/move/asFirst")
    public ResponseEntity<Label> moveLabelAsFirst(@PathVariable Integer userId, @PathVariable Integer labelId) {
        return  new ResponseEntity<>(labelService.moveLabelAsFirst(userId, labelId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/{labelId}/after")
    public ResponseEntity<Label> addLabelAfter(@RequestBody LabelRequest request, @PathVariable Integer userId,
                                               @PathVariable Integer labelId) {
        return  new ResponseEntity<>(labelService.addLabelAfter(request, userId, labelId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/{labelId}/before")
    public ResponseEntity<Label> addLabelBefore(@RequestBody LabelRequest request, @PathVariable Integer userId,
                                                @PathVariable Integer labelId) {
        return  new ResponseEntity<>(labelService.addLabelBefore(request, userId, labelId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/{labelId}/move/after")
    public ResponseEntity<Label> moveLabelAfter(@RequestBody IdRequest request, @PathVariable Integer userId,
                                                @PathVariable Integer labelId) {
        return new ResponseEntity<>(labelService.moveLabelAfter(request, userId, labelId), HttpStatus.OK);
    }
}
