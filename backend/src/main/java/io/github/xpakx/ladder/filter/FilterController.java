package io.github.xpakx.ladder.filter;

import io.github.xpakx.ladder.common.dto.BooleanRequest;
import io.github.xpakx.ladder.filter.dto.FilterRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/{userId}/filters")
@AllArgsConstructor
public class FilterController {
    private final FilterService filterService;

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping
    public ResponseEntity<Filter> addFilter(@RequestBody FilterRequest request, @PathVariable Integer userId) {
        return  new ResponseEntity<>(filterService.addFilter(request, userId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/{filterId}")
    public ResponseEntity<Filter> updateFilter(@RequestBody FilterRequest request, @PathVariable Integer userId, @PathVariable Integer filterId) {
        return  new ResponseEntity<>(filterService.updateFilter(request, userId, filterId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @DeleteMapping("/{filterId}")
    public ResponseEntity<?> deleteFilter(@PathVariable Integer filterId, @PathVariable Integer userId) {
        filterService.deleteFilter(filterId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/{filterId}/favorite")
    public ResponseEntity<Filter> updateLabelFav(@RequestBody BooleanRequest request, @PathVariable Integer filterId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(filterService.updateFilterFav(request, filterId, userId), HttpStatus.OK);
    }
}
