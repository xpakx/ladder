package io.github.xpakx.ladder.habit;

import io.github.xpakx.ladder.notification.NotifyOnHabitChange;
import io.github.xpakx.ladder.notification.NotifyOnHabitDeletion;
import io.github.xpakx.ladder.common.error.NotFoundException;
import io.github.xpakx.ladder.habit.dto.HabitDetails;
import io.github.xpakx.ladder.habit.dto.HabitRequest;
import io.github.xpakx.ladder.label.Label;
import io.github.xpakx.ladder.label.LabelRepository;
import io.github.xpakx.ladder.project.Project;
import io.github.xpakx.ladder.project.ProjectRepository;
import io.github.xpakx.ladder.user.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
public class HabitService {
    private final HabitRepository habitRepository;
    private final UserAccountRepository userRepository;
    private final ProjectRepository projectRepository;
    private final LabelRepository labelRepository;

    /**
     * Get habit details by ID.
     * @param habitId ID of the habit
     * @param userId ID of an owner of the habit
     * @return Habit details
     */
    public HabitDetails getHabitById(Integer habitId, Integer userId) {
        return habitRepository.findProjectedByIdAndOwnerId(habitId, userId, HabitDetails.class)
                .orElseThrow(() -> new NotFoundException("No such habit!"));
    }

    /**
     * Add new habit.
     * @param request Request with data to build new habit
     * @param userId ID of an owner of the newly created habit
     * @return Newly created habit
     */
    @NotifyOnHabitChange
    public Habit addHabit(HabitRequest request, Integer userId, Integer projectId) {
        return habitRepository.save(
                buildHabitToAddFromRequest(request, userId, getProjectFromRequest(userId, projectId))
        );
    }

    private Project getProjectFromRequest(Integer userId, Integer projectId) {
        return nonNull(projectId) ? projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("No such project!")) : null;
    }

    private Habit buildHabitToAddFromRequest(HabitRequest request, Integer userId, Project project) {
        return Habit.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .owner(userRepository.getById(userId))
                .priority(request.getPriority())
                .allowPositive(request.isAllowPositive())
                .allowNegative(request.isAllowNegative())
                .modifiedAt(LocalDateTime.now())
                .archived(false)
                .project(project)
                .generalOrder(habitRepository.getMaxOrderByOwnerId(userId)+1)
                .build();
    }

    /**
     * Delete habit from repository.
     * @param habitId ID of the habit to delete
     * @param userId ID of an owner of the habit
     */
    @Transactional
    @NotifyOnHabitDeletion
    public void deleteHabit(Integer habitId, Integer userId) {
        habitRepository.deleteByIdAndOwnerId(habitId, userId);
    }

    /**
     * Updating habit in repository.
     * @param request Data to update the habit
     * @param habitId ID of the habit to update
     * @param userId ID of an owner of the habit
     * @return Habit with updated data
     */
    @NotifyOnHabitChange
    public Habit updateHabit(HabitRequest request, Integer habitId, Integer userId) {
        Project project = getProjectFromRequest(userId, request.getProjectId());
        Habit habitToUpdate = habitRepository.findByIdAndOwnerId(habitId, userId)
                .orElseThrow(() -> new NotFoundException("No such task!"));
        habitToUpdate.setTitle(request.getTitle());
        habitToUpdate.setDescription(request.getDescription());
        habitToUpdate.setProject(project);
        habitToUpdate.setPriority(request.getPriority());
        habitToUpdate.setAllowNegative(request.isAllowNegative());
        habitToUpdate.setAllowPositive(request.isAllowPositive());
        habitToUpdate.setOwner(userRepository.getById(userId));
        habitToUpdate.setLabels(transformLabelIdsToLabelReferences(request.getLabelIds(), userId));
        habitToUpdate.setModifiedAt(LocalDateTime.now());
        return habitRepository.save(habitToUpdate);
    }

    private Set<Label> transformLabelIdsToLabelReferences(List<Integer> labelIds, Integer userId) {
        if(labelsWithDiffOwner(labelIds, userId)) {
            throw new NotFoundException("Cannot add labels you don't own!");
        }
        return nonNull(labelIds) ? labelIds.stream()
                .map(labelRepository::getById)
                .collect(Collectors.toSet()) : new HashSet<>();
    }

    private boolean labelsWithDiffOwner(List<Integer> labelIds, Integer userId) {
        if(isNull(labelIds) || labelIds.isEmpty()) {
            return false;
        }
        Long labelsWithDifferentOwner = labelRepository.findOwnerIdById(labelIds).stream()
                .filter((a) -> !a.equals(userId))
                .count();
        return !labelsWithDifferentOwner.equals(0L);
    }
}
