package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.*;
import io.github.xpakx.ladder.entity.dto.*;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.error.WrongCompletionTypeException;
import io.github.xpakx.ladder.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class HabitService {
    private final HabitRepository habitRepository;
    private final HabitCompletionRepository habitCompletionRepository;
    private final UserAccountRepository userRepository;
    private final ProjectRepository projectRepository;
    private final LabelRepository labelRepository;

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
                .allowPositive(request.isPositive())
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

    public HabitDetails getHabitById(Integer habitId, Integer userId) {
        return habitRepository.findProjectedByIdAndOwnerId(habitId, userId, HabitDetails.class)
                .orElseThrow(() -> new NotFoundException("No such habit!"));
    }

    public Habit updateHabit(HabitRequest request, Integer habitId, Integer userId) {
        Project project = request.getProjectId() != null ? projectRepository.findByIdAndOwnerId(request.getProjectId(), userId)
                .orElseThrow(() -> new NotFoundException("No such project!")) : null;
        Habit habitToUpdate = habitRepository.findByIdAndOwnerId(habitId, userId)
                .orElseThrow(() -> new NotFoundException("No such task!"));
        habitToUpdate.setTitle(request.getTitle());
        habitToUpdate.setDescription(request.getDescription());
        habitToUpdate.setGeneralOrder(request.getGeneralOrder());
        //habitToUpdate.setParent(parent);
        habitToUpdate.setProject(project);
        habitToUpdate.setPriority(request.getPriority());
        habitToUpdate.setPriority(request.getPriority());
        habitToUpdate.setOwner(userRepository.getById(userId));
        habitToUpdate.setLabels(transformLabelIdsToLabelReferences(request.getLabelIds(), userId));
        habitToUpdate.setModifiedAt(LocalDateTime.now());
        return habitRepository.save(habitToUpdate);
    }

    private Set<Label> transformLabelIdsToLabelReferences(List<Integer> labelIds, Integer userId) {
        if(labelsWithDiffOwner(labelIds, userId)) {
            throw new NotFoundException("Cannot add labels you don't own!");
        }
        return labelIds != null ? labelIds.stream()
                .map(labelRepository::getById)
                .collect(Collectors.toSet()) : new HashSet<>();
    }

    private boolean labelsWithDiffOwner(List<Integer> labelIds, Integer userId) {
        if(labelIds == null || labelIds.size() == 0) {
            return false;
        }
        Long labelsWithDifferentOwner = labelRepository.findOwnerIdById(labelIds).stream()
                .filter((a) -> !a.equals(userId))
                .count();
        return !labelsWithDifferentOwner.equals(0L);
    }

    public HabitCompletion completeHabit(BooleanRequest request, Integer taskId, Integer userId) {
        Habit habit = habitRepository.findByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("No habit with id " + taskId));
        if(isCompletionTypeAllowed(request, habit)) {
            throw new WrongCompletionTypeException("Wrong type of completion!");
        }
        HabitCompletion habitCompletion = HabitCompletion.builder()
                .habit(habit)
                .date(LocalDateTime.now())
                .positive(request.isFlag())
                .build();
        return habitCompletionRepository.save(habitCompletion);
    }

    private boolean isCompletionTypeAllowed(BooleanRequest request, Habit habit) {
        return (!habit.isAllowNegative() && !request.isFlag()) ||
                (!habit.isAllowPositive() && request.isFlag());
    }
}
