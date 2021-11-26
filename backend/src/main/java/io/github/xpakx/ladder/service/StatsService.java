package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.HabitCompletion;
import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.HeatMap;
import io.github.xpakx.ladder.entity.dto.HeatMapElem;
import io.github.xpakx.ladder.repository.HabitCompletionRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StatsService {
    private final TaskRepository taskRepository;
    private final HabitCompletionRepository habitCompletionRepository;

    public HeatMap getTaskHeatMapByYear(Integer year, Integer projectId, Integer userId) {
        List<Task> tasks = taskRepository.getByOwnerIdAndProjectIdAndYear(userId, projectId, year);
        Map<Integer, List<Task>> map =  tasks.stream()
                .collect(Collectors.groupingBy((t) -> t.getCompletedAt().getDayOfYear()));
        List<HeatMapElem> heatMapElems = map.keySet().stream()
                .map((t) -> new HeatMapElem(map.get(t).get(0).getCompletedAt(), map.get(t).size()))
                .collect(Collectors.toList());

        return new HeatMap(heatMapElems);
    }

    public HeatMap getHabitHeatMapByYear(Integer year, Integer projectId, Integer userId) {
        List<HabitCompletion> habits = habitCompletionRepository.getByOwnerIdAndProjectIdAndYear(userId, projectId, year);
        Map<Integer, List<HabitCompletion>> map =  habits.stream()
                .collect(Collectors.groupingBy((t) -> t.getDate().getDayOfYear()));
        List<HeatMapElem> heatMapElems = map.keySet().stream()
                .map((t) -> new HeatMapElem(map.get(t).get(0).getDate(), map.get(t).size()))
                .collect(Collectors.toList());

        return new HeatMap(heatMapElems);
    }
}
