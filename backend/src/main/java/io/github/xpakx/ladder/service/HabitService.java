package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.Habit;
import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.HabitRequest;
import io.github.xpakx.ladder.entity.dto.IdRequest;
import io.github.xpakx.ladder.entity.dto.PriorityRequest;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.repository.HabitCompletionRepository;
import io.github.xpakx.ladder.repository.HabitRepository;
import io.github.xpakx.ladder.repository.ProjectRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class HabitService {
    private final HabitRepository habitRepository;
    private final HabitCompletionRepository habitCompletionRepository;
    private final UserAccountRepository userRepository;
    private final ProjectRepository projectRepository;

    public Habit addHabit(HabitRequest request, Integer userId, Integer projectId) {
        Project project = projectId != null ? checkProjectOwnerAndGetReference(projectId, userId)
                .orElseThrow(() -> new NotFoundException("No such project!")) : null;
        Habit habitToAdd = buildLabelToAddFromRequest(request, userId, project);
        habitToAdd.setGeneralOrder(habitRepository.getMaxOrderByOwnerId(userId)+1);
        return habitRepository.save(habitToAdd);
    }

    private Habit buildLabelToAddFromRequest(HabitRequest request, Integer userId, Project project) {
        return Habit.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .owner(userRepository.getById(userId))
                .priority(request.getPriority())
                .positive(request.isPositive())
                .project(project)
                .modifiedAt(LocalDateTime.now())
                .build();
    }

    private Optional<Project> checkProjectOwnerAndGetReference(Integer projectId, Integer userId) {
        if(!userId.equals(projectRepository.findOwnerIdById(projectId))) {
            return Optional.empty();
        }
        return Optional.of(projectRepository.getById(projectId));
    }

    @Transactional
    public void deleteHabit(Integer habitId, Integer userId) {
        habitRepository.deleteByIdAndOwnerId(habitId, userId);
    }

    public Habit moveHabitAsFirst(Integer userId, Integer habitToMoveId) {
        Habit habitToMove = habitRepository.findByIdAndOwnerId(habitToMoveId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot move non-existent habit!"));
        habitRepository.incrementGeneralOrderByOwnerId(
                userId,
                LocalDateTime.now()
        );
        habitToMove.setGeneralOrder(1);
        habitToMove.setModifiedAt(LocalDateTime.now());
        return habitRepository.save(habitToMove);
    }

    public Habit moveHabitAfter(IdRequest request, Integer userId, Integer habitToMoveId) {
        Habit habitToMove = habitRepository.findByIdAndOwnerId(habitToMoveId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot move non-existent habit!"));
        Habit afterHabit = findIdFromIdRequest(request);
        habitRepository.incrementGeneralOrderByOwnerIdAndGeneralOrderGreaterThan(
                userId,
                afterHabit.getGeneralOrder(),
                LocalDateTime.now()
        );
        habitToMove.setGeneralOrder(afterHabit.getGeneralOrder() + 1);
        habitToMove.setModifiedAt(LocalDateTime.now());
        return habitRepository.save(habitToMove);
    }

    private Habit findIdFromIdRequest(IdRequest request) {
        return hasId(request) ? habitRepository.findById(request.getId()).orElse(null) : null;
    }

    private boolean hasId(IdRequest request) {
        return request.getId() != null;
    }

    public Habit updateHabitPriority(PriorityRequest request, Integer habitId, Integer userId) {
        Habit habitToUpdate = habitRepository.findByIdAndOwnerId(habitId, userId)
                .orElseThrow(() -> new NotFoundException("No such habit!"));
        habitToUpdate.setPriority(request.getPriority());
        habitToUpdate.setModifiedAt(LocalDateTime.now());
        return habitRepository.save(habitToUpdate);
    }
}
