package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.entity.Habit;
import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.UserAccount;
import io.github.xpakx.ladder.entity.dto.BooleanRequest;
import io.github.xpakx.ladder.repository.HabitRepository;
import io.github.xpakx.ladder.repository.ProjectRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
import io.github.xpakx.ladder.security.JwtTokenUtil;
import io.github.xpakx.ladder.service.UserService;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import java.util.HashSet;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ArchiveControllerTest {
    @LocalServerPort
    private int port;

    private String baseUrl;
    private Integer userId;

    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @Autowired
    UserService userService;
    @Autowired
    UserAccountRepository userRepository;

    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    HabitRepository habitRepository;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost".concat(":").concat(port + "");
        UserAccount user = UserAccount.builder()
                .username("user1")
                .password("password")
                .roles(new HashSet<>())
                .build();
        user = userRepository.save(user);
        this.userId = user.getId();
    }

    @AfterEach
    void tearDown() {
        taskRepository.deleteAll();
        habitRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String tokenFor(String username) {
        return jwtTokenUtil.generateToken(userService.loadUserToLogin(username));
    }

    @Test
    void shouldRespondWith401ToArchiveProjectIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .put(baseUrl + "/{userId}/projects/{projectId}/archive", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToArchiveProjectIfProjectNotFound() {
        BooleanRequest request = getTrueBooleanRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/projects/{projectId}/archive", userId, 1)
        .then()
                .statusCode(NOT_FOUND.value());
    }

    private BooleanRequest getTrueBooleanRequest() {
        BooleanRequest request = new BooleanRequest();
        request.setFlag(true);
        return request;
    }

    private BooleanRequest getFalseBooleanRequest() {
        BooleanRequest request = new BooleanRequest();
        request.setFlag(false);
        return request;
    }

    private Integer addProjectWith2ChildrenAnd2TasksAnd1HabitAndReturnId(boolean archived) {
        Project project = Project.builder()
                .owner(userRepository.getById(userId))
                .name("Test Project")
                .generalOrder(1)
                .archived(archived)
                .build();
        Task task1 = Task.builder()
                .owner(userRepository.getById(userId))
                .title("First Task")
                .completed(false)
                .project(project)
                .projectOrder(1)
                .dailyViewOrder(0)
                .archived(archived)
                .build();
        Task task2 = Task.builder()
                .owner(userRepository.getById(userId))
                .title("Second Task")
                .completed(false)
                .project(project)
                .projectOrder(2)
                .dailyViewOrder(0)
                .archived(archived)
                .build();
        Habit habit = Habit.builder()
                .owner(userRepository.getById(userId))
                .title("Habit")
                .project(project)
                .generalOrder(2)
                .archived(archived)
                .build();

        project.setTasks(List.of(task1, task2));
        project.setHabits(List.of(habit));

        Project subProject1 = Project.builder()
                .owner(userRepository.getById(userId))
                .name("Sub Project 1")
                .parent(project)
                .archived(archived)
                .build();
        Project subProject2 = Project.builder()
                .owner(userRepository.getById(userId))
                .name("Sub Project 1")
                .parent(project)
                .archived(archived)
                .build();
        project.setChildren(List.of(subProject1, subProject2));

        projectRepository.save(project);
        return project.getId();
    }

    private Integer addProjectAndReturnId(boolean archived) {
        Project project = Project.builder()
                .owner(userRepository.getById(userId))
                .name("Test Project")
                .generalOrder(1)
                .archived(archived)
                .build();

        projectRepository.save(project);
        return project.getId();
    }

    @Test
    void shouldArchiveProject() {
        BooleanRequest request = getTrueBooleanRequest();
        Integer projectId = addProjectAndReturnId(false);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/projects/{projectId}/archive", userId, projectId)
        .then()
                .statusCode(OK.value());
        List<Project> projects = projectRepository.findAll();
        assertThat(projects, everyItem(
                hasProperty("archived", is(true))
        ));
    }

    @Test
    void shouldArchiveProjectWithTasksHabitsAndSubprojects() {
        BooleanRequest request = getTrueBooleanRequest();
        Integer projectId = addProjectWith2ChildrenAnd2TasksAnd1HabitAndReturnId(false);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(baseUrl + "/{userId}/projects/{projectId}/archive", userId, projectId)
                .then()
                .statusCode(OK.value());
        List<Project> projects = projectRepository.findAll();
        List<Habit> habits = habitRepository.findAll();
        List<Task> tasks = taskRepository.findAll();
        assertThat(projects, everyItem(either(
                hasProperty("archived", is(false)))
                .or(hasProperty("id", is(projectId)))
        ));
        assertThat(habits, everyItem(
                hasProperty("archived", is(true))
        ));
        assertThat(tasks, everyItem(
                hasProperty("archived", is(true))
        ));
    }

    @Test
    void shouldRestoreProject() {
        BooleanRequest request = getTrueBooleanRequest();
        Integer projectId = addProjectAndReturnId(true);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(baseUrl + "/{userId}/projects/{projectId}/archive", userId, projectId)
                .then()
                .statusCode(OK.value());
        List<Project> projects = projectRepository.findAll();
        assertThat(projects, everyItem(
                hasProperty("archived", is(false))
        ));
    }
}
