package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.UserAccount;
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
import org.mockito.ArgumentCaptor;
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

    @Test
    void shouldSaveNewProject()  {
        injectMocks();

        projectService.addProject(mock(ProjectRequest.class), 3);

        then(projectRepository)
                .should(times(1))
                .save(any(Project.class));
    }

    @Test
    void createdObjectShouldHaveIncrementedOrderHasParent()  {
        final int MAX_ORDER = 5;
        ProjectRequest request = mock(ProjectRequest.class);
        given(request.getParentId())
                .willReturn(3);
        given(projectRepository.getMaxOrderByOwnerIdAndParentId(anyInt(), anyInt()))
                .willReturn(MAX_ORDER);
        injectMocks();

        projectService.addProject(request, 3);

        ArgumentCaptor<Project> projectCaptor= ArgumentCaptor.forClass(Project.class);

        then(projectRepository)
                .should()
                .save(projectCaptor.capture());
        assertNotNull(projectCaptor.getValue());
        assertEquals(MAX_ORDER+1, projectCaptor.getValue().getGeneralOrder());
    }

    @Test
    void createdObjectShouldHaveIncrementedOrderHasNoParent() {
        final int MAX_ORDER = 5;
        ProjectRequest request = mock(ProjectRequest.class);
        given(request.getParentId())
                .willReturn(null);
        given(projectRepository.getMaxOrderByOwnerId(anyInt()))
                .willReturn(MAX_ORDER);
        injectMocks();

        projectService.addProject(request, 3);

        ArgumentCaptor<Project> projectCaptor= ArgumentCaptor.forClass(Project.class);

        then(projectRepository)
                .should()
                .save(projectCaptor.capture());
        assertNotNull(projectCaptor.getValue());
        assertEquals(MAX_ORDER+1, projectCaptor.getValue().getGeneralOrder());
    }

    @Test
    void shouldGetProjectOwner() {
        final Integer OWNER_ID = 7;
        injectMocks();

        projectService.addProject(mock(ProjectRequest.class), OWNER_ID);

        then(userRepository)
                .should(times(1))
                .getById(eq(OWNER_ID));
    }

    @Test
    void shouldSaveProjectWithOwner() {
        final Integer OWNER_ID = 7;
        UserAccount userReference = mock(UserAccount.class);
        given(userRepository.getById(OWNER_ID))
                .willReturn(userReference);
        injectMocks();

        projectService.addProject(mock(ProjectRequest.class), 7);

        then(userReference)
                .shouldHaveNoInteractions();
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        then(projectRepository)
                .should()
                .save(projectCaptor.capture());
        assertSame(userReference, projectCaptor.getValue().getOwner());
    }

    @Test
    void shouldUseProjectNameFromRequest() {
        final String NAME = "Project 1";
        ProjectRequest request = mock(ProjectRequest.class);
        given(request.getName())
                .willReturn(NAME);
        injectMocks();

        projectService.addProject(request, 4);

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        then(projectRepository)
                .should()
                .save(projectCaptor.capture());
        assertEquals(NAME, projectCaptor.getValue().getName());
    }

    @Test
    void shouldUseProjectFavFromRequest() {
        final boolean FAV = true;
        ProjectRequest request = mock(ProjectRequest.class);
        given(request.isFavorite())
                .willReturn(FAV);
        injectMocks();

        projectService.addProject(request, 4);

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        then(projectRepository)
                .should()
                .save(projectCaptor.capture());
        assertEquals(FAV, projectCaptor.getValue().isFavorite());
    }

    @Test
    void shouldUseProjectColorFromRequest() {
        final String COLOR = "#eeddaa";
        ProjectRequest request = mock(ProjectRequest.class);
        given(request.getColor())
                .willReturn(COLOR);
        injectMocks();

        projectService.addProject(request, 4);

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        then(projectRepository)
                .should()
                .save(projectCaptor.capture());
        assertEquals(COLOR, projectCaptor.getValue().getColor());
    }

    @Test
    void createdProjectShouldBeCollapsed() {
        injectMocks();

        projectService.addProject(mock(ProjectRequest.class), 4);

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        then(projectRepository)
                .should()
                .save(projectCaptor.capture());
        assertTrue(projectCaptor.getValue().isCollapsed());
    }

    @Test
    void projectParentShouldBeNull() {
        ProjectRequest request = mock(ProjectRequest.class);
        given(request.getParentId())
                .willReturn(null);
        injectMocks();

        projectService.addProject(request, 4);

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        then(projectRepository)
                .should()
                .save(projectCaptor.capture());
        assertNull(projectCaptor.getValue().getParent());
    }

    @Test
    void shouldGetProjectParent() {
        final Integer PARENT_ID = 7;
        ProjectRequest request = mock(ProjectRequest.class);
        given(request.getParentId())
                .willReturn(PARENT_ID);
        injectMocks();

        projectService.addProject(request, 7);

        then(projectRepository)
                .should(times(1))
                .getById(eq(PARENT_ID));
    }

    @Test
    void shouldSaveProjectWithParent() {
        final Integer PARENT_ID = 7;
        ProjectRequest request = mock(ProjectRequest.class);
        given(request.getParentId())
                .willReturn(PARENT_ID);
        Project parentReference = mock(Project.class);
        given(projectRepository.getById(PARENT_ID))
                .willReturn(parentReference);
        injectMocks();

        projectService.addProject(request, 7);

        then(parentReference)
                .shouldHaveNoInteractions();
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        then(projectRepository)
                .should()
                .save(projectCaptor.capture());
        assertSame(parentReference, projectCaptor.getValue().getParent());
    }

}