package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.entity.dto.HeatMap;
import io.github.xpakx.ladder.service.StatsService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/{userId}/statistics/")
@AllArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/project/{projectId}/year/{year}/tasks")
    public ResponseEntity<HeatMap> getYearlyHeatMap(@PathVariable Integer userId,
                                                    @PathVariable Integer projectId,
                                                    @PathVariable Integer year) {
        return new ResponseEntity<>(statsService.getTaskHeatMapByYear(year, projectId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/project/{projectId}/year/{year}/habits")
    public ResponseEntity<HeatMap> getYearlyHabitHeatMap(@PathVariable Integer userId,
                                                    @PathVariable Integer projectId,
                                                    @PathVariable Integer year) {
        return new ResponseEntity<>(statsService.getHabitHeatMapByYear(year, projectId, userId), HttpStatus.OK);
    }
}
