package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.entity.Label;
import io.github.xpakx.ladder.entity.dto.BooleanRequest;
import io.github.xpakx.ladder.entity.dto.IdRequest;
import io.github.xpakx.ladder.entity.dto.LabelRequest;
import io.github.xpakx.ladder.service.LabelService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/{userId}/labels")
@AllArgsConstructor
public class LabelController {
    private final LabelService labelService;

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping
    public ResponseEntity<Label> addLabel(@RequestBody LabelRequest request, @PathVariable Integer userId) {
        return  new ResponseEntity<>(labelService.addLabel(request, userId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/{labelId}")
    public ResponseEntity<Label> updateLabel(@RequestBody LabelRequest request, @PathVariable Integer userId, @PathVariable Integer labelId) {
        return  new ResponseEntity<>(labelService.updateLabel(request, userId, labelId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @DeleteMapping("/{labelId}")
    public ResponseEntity<?> deleteLabel(@PathVariable Integer labelId, @PathVariable Integer userId) {
        labelService.deleteLabel(labelId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/{labelId}/favorite")
    public ResponseEntity<Label> updateLabelFav(@RequestBody BooleanRequest request, @PathVariable Integer labelId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(labelService.updateLabelFav(request, labelId, userId), HttpStatus.OK);
    }

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
