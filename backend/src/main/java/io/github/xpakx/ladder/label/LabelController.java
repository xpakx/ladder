package io.github.xpakx.ladder.label;

import io.github.xpakx.ladder.common.dto.BooleanRequest;
import io.github.xpakx.ladder.label.dto.LabelRequest;
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
}
