package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.aspect.NotifyOnHabitChange;
import io.github.xpakx.ladder.entity.Habit;
import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.dto.HabitRequest;
import io.github.xpakx.ladder.entity.dto.IdRequest;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.repository.HabitRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
public class HabitMovableService {
    private final HabitRepository habitRepository;
    private final UserAccountRepository userRepository;

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
        if(nonNull(project)) {
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
        if(nonNull(project)) {
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
        if(nonNull(project)) {
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
        return nonNull(request.getId());
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
}
