package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.aspect.NotifyOnHabitChange;
import io.github.xpakx.ladder.aspect.NotifyOnHabitCompletion;
import io.github.xpakx.ladder.aspect.NotifyOnHabitDeletion;
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

    /**
     * Add new habit.
     * @param request Request with data to build new habit
     * @param userId ID of an owner of the newly created habit
     * @return Newly created habit
     */
    @NotifyOnHabitChange
    public Habit addHabit(HabitRequest request, Integer userId, Integer projectId) {
        Project project = projectId != null ? checkProjectOwnerAndGetReference(projectId, userId)
                .orElseThrow(() -> new NotFoundException("No such project!")) : null;
        Habit habitToAdd = buildHabitToAddFromRequest(request, userId);
        habitToAdd.setProject(project);
        habitToAdd.setGeneralOrder(habitRepository.getMaxOrderByOwnerId(userId)+1);
        return habitRepository.save(habitToAdd);
    }

    private Habit buildHabitToAddFromRequest(HabitRequest request, Integer userId) {
        return Habit.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .owner(userRepository.getById(userId))
                .priority(request.getPriority())
                .allowPositive(request.isAllowPositive())
                .allowNegative(request.isAllowNegative())
                .modifiedAt(LocalDateTime.now())
                .archived(false)
                .build();
    }

    private Optional<Project> checkProjectOwnerAndGetReference(Integer projectId, Integer userId) {
        if(!userId.equals(projectRepository.findOwnerIdById(projectId))) {
            return Optional.empty();
        }
        return Optional.of(projectRepository.getById(projectId));
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
     * Move habit at first position
     * @param userId ID of an owner of habits
     * @param habitToMoveId ID of the habit to move
     * @return Moved habit
     */
    @NotifyOnHabitChange
    public Habit moveHabitAsFirst(Integer userId, Integer habitToMoveId) {
        Habit habitToMove = habitRepository.findByIdAndOwnerId(habitToMoveId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot move non-existent habit!"));
        incrementOrderForProject(userId, habitToMove.getProject());
        habitToMove.setGeneralOrder(1);
        habitToMove.setModifiedAt(LocalDateTime.now());
        return habitRepository.save(habitToMove);
    }

    private void incrementOrderForProject(Integer userId, Project project) {
        if(project != null) {
            habitRepository.incrementGeneralOrderByOwnerIdAndProjectId(
                    userId,
                    project.getId(),
                    LocalDateTime.now()
            );
        } else {
            habitRepository.incrementGeneralOrderByOwnerIdAndProjectIsNull(
                    userId,
                    LocalDateTime.now()
            );
        }
    }

    /**
     * Move habit after given habit
     * @param request Request with id of the habit which should be before moved habit
     * @param userId ID of an owner of habits
     * @param habitToMoveId ID of the habit to move
     * @return Moved habit
     */
    @NotifyOnHabitChange
    public Habit moveHabitAfter(IdRequest request, Integer userId, Integer habitToMoveId) {
        Habit habitToMove = habitRepository.findByIdAndOwnerId(habitToMoveId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot move non-existent habit!"));
        Habit afterHabit = findIdFromIdRequest(request);
        incrementOrderForProjectGreaterThan(userId, afterHabit.getProject(), afterHabit.getGeneralOrder());
        habitToMove.setGeneralOrder(afterHabit.getGeneralOrder() + 1);
        habitToMove.setModifiedAt(LocalDateTime.now());
        return habitRepository.save(habitToMove);
    }

    private void incrementOrderForProjectGreaterThan(Integer userId, Project project, Integer generalOrder) {
        if(project != null) {
            habitRepository.incrementGeneralOrderByOwnerIdAndProjectIdAndGeneralOrderGreaterThan(
                    userId,
                    project.getId(),
                    generalOrder,
                    LocalDateTime.now()
            );
        } else {
            habitRepository.incrementGeneralOrderByOwnerIdAndProjectIsNullAndGeneralOrderGreaterThan(
                    userId,
                    generalOrder,
                    LocalDateTime.now()
            );
        }
    }

    private void incrementOrderForProjectGreaterThanEqual(Integer userId, Project project, Integer generalOrder) {
        if(project != null) {
            habitRepository.incrementGeneralOrderByOwnerIdAndProjectIdAndGeneralOrderGreaterThanEqual(
                    userId,
                    project.getId(),
                    generalOrder,
                    LocalDateTime.now()
            );
        } else {
            habitRepository.incrementGeneralOrderByOwnerIdAndProjectIsNullAndGeneralOrderGreaterThanEqual(
                    userId,
                    generalOrder,
                    LocalDateTime.now()
            );
        }
    }

    private Habit findIdFromIdRequest(IdRequest request) {
        return hasId(request) ? habitRepository.findById(request.getId()).orElse(null) : null;
    }

    private boolean hasId(IdRequest request) {
        return request.getId() != null;
    }

    /**
     * Change habit priority
     * @param request Request with new priority
     * @param habitId ID of the habit to update
     * @param userId ID of an owner of the habit
     * @return Updated habit
     */
    @NotifyOnHabitChange
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

    /**
     * Updating habit in repository.
     * @param request Data to update the habit
     * @param habitId ID of the habit to update
     * @param userId ID of an owner of the habit
     * @return Habit with updated data
     */
    @NotifyOnHabitChange
    public Habit updateHabit(HabitRequest request, Integer habitId, Integer userId) {
        Project project = request.getProjectId() != null ? projectRepository.findByIdAndOwnerId(request.getProjectId(), userId)
                .orElseThrow(() -> new NotFoundException("No such project!")) : null;
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

    /**
     * Add completion to the habit
     * @param request Request with completion state
     * @param habitId ID of the habit to update
     * @param userId ID of an owner of the habit
     * @return Added habit completion
     */
    @NotifyOnHabitCompletion
    public HabitCompletion completeHabit(BooleanRequest request, Integer habitId, Integer userId) {
        Habit habit = habitRepository.findByIdAndOwnerId(habitId, userId)
                .orElseThrow(() -> new NotFoundException("No habit with id " + habitId));
        if(isCompletionTypeNotAllowed(request, habit)) {
            throw new WrongCompletionTypeException("Wrong type of completion!");
        }
        HabitCompletion habitCompletion = HabitCompletion.builder()
                .habit(habit)
                .date(LocalDateTime.now())
                .positive(request.isFlag())
                .owner(userRepository.getById(userId))
                .build();
        return habitCompletionRepository.save(habitCompletion);
    }

    private boolean isCompletionTypeNotAllowed(BooleanRequest request, Habit habit) {
        return (!habit.isAllowNegative() && !request.isFlag()) ||
                (!habit.isAllowPositive() && request.isFlag());
    }

    /**
     * Add new habit with order after given habit
     * @param request Request with data to build new habit
     * @param userId ID of an owner of habits
     * @param habitId ID of the habit which should be before newly created habit
     * @return Newly created habit
     */
    @NotifyOnHabitChange
    public Habit addHabitAfter(HabitRequest request, Integer userId, Integer habitId) {
        Habit habitToAdd = buildHabitToAddFromRequest(request, userId);
        Habit habit = habitRepository.findByIdAndOwnerId(habitId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot add nothing after non-existent habit!"));
        habitToAdd.setGeneralOrder(habit.getGeneralOrder()+1);
        habitToAdd.setProject(habit.getProject());
        habitToAdd.setModifiedAt(LocalDateTime.now());
        incrementOrderForProjectGreaterThan(userId, habit.getProject(), habit.getGeneralOrder());
        return habitRepository.save(habitToAdd);
    }

    /**
     * Add new habit with order before given habit
     * @param request Request with data to build new habit
     * @param userId ID of an owner of habits
     * @param habitId ID of the habit which should be after newly created habit
     * @return Newly created habit
     */
    @NotifyOnHabitChange
    public Habit addHabitBefore(HabitRequest request, Integer userId, Integer habitId) {
        Habit habitToAdd = buildHabitToAddFromRequest(request, userId);
        Habit habit = habitRepository.findByIdAndOwnerId(habitId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot add nothing before non-existent habit!"));
        habitToAdd.setGeneralOrder(habit.getGeneralOrder());
        habitToAdd.setProject(habit.getProject());
        habitToAdd.setModifiedAt(LocalDateTime.now());
        incrementOrderForProjectGreaterThanEqual(userId, habit.getProject(), habit.getGeneralOrder());
        return habitRepository.save(habitToAdd);
    }

    /**
     * Change habit's project and add at the end of project's habit list.
     * @param request Request with new project ID
     * @param habitId ID of the habit to update
     * @param userId ID of an owner of the habit
     * @return Updated habit
     */
    @NotifyOnHabitChange
    public Habit updateHabitProject(IdRequest request, Integer habitId, Integer userId) {
        Habit habitToUpdate = habitRepository.findByIdAndOwnerId(habitId, userId)
                .orElseThrow(() -> new NotFoundException("No such task!"));
        Project project = request.getId() != null ? projectRepository.findByIdAndOwnerId(request.getId(), userId)
                .orElseThrow(() -> new NotFoundException("No such project!")) : null;

        habitToUpdate.setProject(project);
        habitToUpdate.setGeneralOrder(getMaxProjectOrder(request, userId)+1);
        habitToUpdate.setModifiedAt(LocalDateTime.now());
        return habitRepository.save(habitToUpdate);
    }

    private Integer getMaxProjectOrder(IdRequest request, Integer userId) {
        if(hasId(request)) {
            return habitRepository.getMaxOrderByOwnerIdAndProjectId(userId, request.getId());
        } else {
            return habitRepository.getMaxOrderByOwnerId(userId);
        }
    }

    /**
     * Duplicate given habit
     * @param habitId ID of the task to duplicate
     * @param userId ID of an owner of the task
     * @return All created tasks
     */
    @NotifyOnHabitChange
    public Habit duplicate(Integer habitId, Integer userId) {
        Habit habitToDuplicate = habitRepository.findByIdAndOwnerId(habitId, userId)
                .orElseThrow(() -> new NotFoundException("No task with id " + habitId));
        Habit habitToAdd = Habit.builder()
                .title(habitToDuplicate.getTitle())
                .description(habitToDuplicate.getDescription())
                .owner(userRepository.getById(userId))
                .priority(habitToDuplicate.getPriority())
                .allowPositive(habitToDuplicate.isAllowPositive())
                .allowNegative(habitToDuplicate.isAllowNegative())
                .modifiedAt(LocalDateTime.now())
                .archived(false)
                .build();
        habitToAdd.setProject(habitToDuplicate.getProject());
        habitToAdd.setGeneralOrder(habitToDuplicate.getGeneralOrder()+1);
        incrementOrderForProjectGreaterThan(userId, habitToDuplicate.getProject(), habitToDuplicate.getGeneralOrder());
        return habitRepository.save(habitToAdd);
    }
}
