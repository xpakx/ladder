package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.dto.ProjectDetails;
import io.github.xpakx.ladder.entity.dto.ProjectRequest;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.repository.LabelRepository;
import io.github.xpakx.ladder.repository.ProjectRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserAccountRepository userRepository;
    @Mock
    private LabelRepository labelRepository;

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    private void injectMocks() {
        projectService = new ProjectService(projectRepository, taskRepository, userRepository, labelRepository);
    }

    @Test
    void shouldAskRepositoryForProjectWithCorrectIds() {
        given(projectRepository.findProjectedByIdAndOwnerId(anyInt(), anyInt(), any(Class.class)))
                .willReturn(Optional.of(mock(ProjectDetails.class)));
        injectMocks();

        projectService.getProjectById(2, 5);

        then(projectRepository)
                .should(times(1))
                .findProjectedByIdAndOwnerId(eq(2),eq(5), any(Class.class));
    }

    @Test
    void shouldThrowExceptionIfProjectNotFound() {
        given(projectRepository.findProjectedByIdAndOwnerId(anyInt(), anyInt(), any(Class.class)))
                .willReturn(Optional.empty());
        injectMocks();

        assertThrows(NotFoundException.class, () ->projectService.getProjectById(2, 5));
    }

    @Test
    void serviceShouldNotInteractWithReturningProject() {
        ProjectDetails projectReturned = mock(ProjectDetails.class);
        given(projectRepository.findProjectedByIdAndOwnerId(anyInt(), anyInt(), any(Class.class)))
                .willReturn(Optional.of(projectReturned));
        injectMocks();

        projectService.getProjectById(2, 5);

        then(projectReturned)
                .shouldHaveNoInteractions();
    }

    @Test
    void serviceShouldPassProjectReturnedFromRepoToController() {
        ProjectDetails projectReturned = mock(ProjectDetails.class);
        given(projectRepository.findProjectedByIdAndOwnerId(anyInt(), anyInt(), any(Class.class)))
                .willReturn(Optional.of(projectReturned));
        injectMocks();

        ProjectDetails result = projectService.getProjectById(2, 5);

        assertSame(projectReturned, result);
    }
}