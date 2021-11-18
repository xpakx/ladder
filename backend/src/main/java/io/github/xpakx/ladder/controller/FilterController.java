package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.entity.Filter;
import io.github.xpakx.ladder.entity.Label;
import io.github.xpakx.ladder.entity.dto.FilterRequest;
import io.github.xpakx.ladder.service.FilterService;
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
}
