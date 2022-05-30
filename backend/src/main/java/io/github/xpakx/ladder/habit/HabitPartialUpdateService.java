package io.github.xpakx.ladder.habit;

import io.github.xpakx.ladder.notification.NotifyOnHabitChange;
import io.github.xpakx.ladder.notification.NotifyOnHabitCompletion;
import io.github.xpakx.ladder.project.Project;
import io.github.xpakx.ladder.common.dto.BooleanRequest;
import io.github.xpakx.ladder.common.dto.IdRequest;
import io.github.xpakx.ladder.common.dto.PriorityRequest;
import io.github.xpakx.ladder.common.error.NotFoundException;
import io.github.xpakx.ladder.common.error.WrongCompletionTypeException;
import io.github.xpakx.ladder.project.ProjectRepository;
import io.github.xpakx.ladder.user.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
public class HabitPartialUpdateService {
    private final HabitRepository habitRepository;
    private final HabitCompletionRepository habitCompletionRepository;
    private final UserAccountRepository userRepository;
    private final ProjectRepository projectRepository;

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
        return habitCompletionRepository.save(createCompletionForHabit(request, userId, habit));
    }

    private HabitCompletion createCompletionForHabit(BooleanRequest request, Integer userId, Habit habit) {
        return HabitCompletion.builder()
                .habit(habit)
                .date(LocalDateTime.now())
                .positive(request.isFlag())
                .owner(userRepository.getById(userId))
                .build();
    }

    private boolean isCompletionTypeNotAllowed(BooleanRequest request, Habit habit) {
        return (!habit.isAllowNegative() && !request.isFlag()) ||
                (!habit.isAllowPositive() && request.isFlag());
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
        Project project = getProjectFromRequest(request, userId);
        habitToUpdate.setProject(project);
        habitToUpdate.setGeneralOrder(getMaxProjectOrder(request, userId)+1);
        habitToUpdate.setModifiedAt(LocalDateTime.now());
        return habitRepository.save(habitToUpdate);
    }

    private Project getProjectFromRequest(IdRequest request, Integer userId) {
        return nonNull(request.getId()) ? projectRepository.findByIdAndOwnerId(request.getId(), userId)
                .orElseThrow(() -> new NotFoundException("No such project!")) : null;
    }

    private Integer getMaxProjectOrder(IdRequest request, Integer userId) {
        if(hasId(request)) {
            return habitRepository.getMaxOrderByOwnerIdAndProjectId(userId, request.getId());
        } else {
            return habitRepository.getMaxOrderByOwnerId(userId);
        }
    }

    private boolean hasId(IdRequest request) {
        return request.getId() != null;
    }
}
