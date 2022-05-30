package io.github.xpakx.ladder.project;

import io.github.xpakx.ladder.common.dto.BooleanRequest;
import io.github.xpakx.ladder.common.dto.NameRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class ProjectPartialUpdateServiceTest {
    @Mock
    private ProjectRepository projectRepository;

    private ProjectPartialUpdateService projectService;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    private void injectMocks() {
        projectService = new ProjectPartialUpdateService(projectRepository);
    }

    @Test
    void shouldUseProjectNameFromUpdateNameRequest() {
        final String NAME = "Project 1";
        NameRequest request = mock(NameRequest.class);
        given(request.getName())
                .willReturn(NAME);
        Project projectInDb = mock(Project.class);
        given(projectRepository.findByIdAndOwnerId(anyInt(), anyInt()))
                .willReturn(Optional.of(projectInDb));
        injectMocks();

        projectService.updateProjectName(request, 4, 5);

        then(projectInDb)
                .should(times(1))
                .setName(eq(NAME));
        then(projectInDb)
                .should(times(1))
                .setModifiedAt(any(LocalDateTime.class));
        then(projectInDb)
                .shouldHaveNoMoreInteractions();;
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        then(projectRepository)
                .should()
                .save(projectCaptor.capture());
        assertSame(projectInDb, projectCaptor.getValue());
    }

    @Test
    void shouldUseProjectFavFromUpdateFavRequest() {
        final boolean FAV = true;
        BooleanRequest request = mock(BooleanRequest.class);
        given(request.isFlag())
                .willReturn(FAV);
        Project projectInDb = mock(Project.class);
        given(projectRepository.findByIdAndOwnerId(anyInt(), anyInt()))
                .willReturn(Optional.of(projectInDb));
        injectMocks();

        projectService.updateProjectFav(request, 4, 5);

        then(projectInDb)
                .should(times(1))
                .setFavorite(eq(FAV));
        then(projectInDb)
                .should(times(1))
                .setModifiedAt(any(LocalDateTime.class));
        then(projectInDb)
                .shouldHaveNoMoreInteractions();;
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        then(projectRepository)
                .should()
                .save(projectCaptor.capture());
        assertSame(projectInDb, projectCaptor.getValue());
    }

    @Test
    void shouldUseProjectCollapsion() {
        final boolean COLLAPSE = true;
        BooleanRequest request = mock(BooleanRequest.class);
        given(request.isFlag())
                .willReturn(COLLAPSE);
        Project projectInDb = mock(Project.class);
        given(projectRepository.findByIdAndOwnerId(anyInt(), anyInt()))
                .willReturn(Optional.of(projectInDb));
        injectMocks();

        projectService.updateProjectCollapsedState(request, 4, 5);

        then(projectInDb)
                .should(times(1))
                .setCollapsed(eq(COLLAPSE));
        then(projectInDb)
                .should(times(1))
                .setModifiedAt(any(LocalDateTime.class));
        then(projectInDb)
                .shouldHaveNoMoreInteractions();;
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        then(projectRepository)
                .should()
                .save(projectCaptor.capture());
        assertSame(projectInDb, projectCaptor.getValue());
    }
}
