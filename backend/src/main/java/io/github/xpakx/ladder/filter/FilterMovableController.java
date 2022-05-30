package io.github.xpakx.ladder.filter;

import io.github.xpakx.ladder.filter.dto.FilterRequest;
import io.github.xpakx.ladder.common.dto.IdRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/{userId}/filters")
@AllArgsConstructor
public class FilterMovableController {
    private final FilterMovableService filterService;

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/{filterId}/move/asFirst")
    public ResponseEntity<Filter> moveFilterAsFirst(@PathVariable Integer userId, @PathVariable Integer filterId) {
        return  new ResponseEntity<>(filterService.moveFilterAsFirst(userId, filterId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/{filterId}/after")
    public ResponseEntity<Filter> addFilterAfter(@RequestBody FilterRequest request, @PathVariable Integer userId,
                                                 @PathVariable Integer filterId) {
        return  new ResponseEntity<>(filterService.addFilterAfter(request, userId, filterId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/{filterId}/before")
    public ResponseEntity<Filter> addFilterBefore(@RequestBody FilterRequest request, @PathVariable Integer userId,
                                                  @PathVariable Integer filterId) {
        return  new ResponseEntity<>(filterService.addFilterBefore(request, userId, filterId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/{filterId}/move/after")
    public ResponseEntity<Filter> moveFilterAfter(@RequestBody IdRequest request, @PathVariable Integer userId,
                                                  @PathVariable Integer filterId) {
        return new ResponseEntity<>(filterService.moveFilterAfter(request, userId, filterId), HttpStatus.OK);
    }
}
