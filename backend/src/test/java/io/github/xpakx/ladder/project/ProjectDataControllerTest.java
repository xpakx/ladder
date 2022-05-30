package io.github.xpakx.ladder.project;

import io.github.xpakx.ladder.task.Task;
import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.task.TaskRepository;
import io.github.xpakx.ladder.user.UserAccountRepository;
import io.github.xpakx.ladder.security.JwtTokenUtil;
import io.github.xpakx.ladder.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import java.util.HashSet;
import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.withArgs;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProjectDataControllerTest {
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
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String tokenFor(String username) {
        return jwtTokenUtil.generateToken(userService.loadUserToLogin(username));
    }

    @Test
    void shouldRespondWith401ToGetFullProjectIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .get(baseUrl + "/{userId}/projects/{projectId}/full", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWithFullProject() {
        Integer projectId = addProjectWith2ChildrenAnd2TasksAndReturnId();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .get(baseUrl + "/{userId}/projects/{projectId}/full", userId, projectId)
        .then()
                .log()
                .body()
                .statusCode(OK.value())
                .body("id", equalTo(projectId))
                .body("name", equalTo("Test Project"))
                .body("children", hasSize(2))
                .body("tasks", hasSize(2));
    }

    @Test
    void shouldRespondWithFullProjectWithSubtasks() {
        Integer projectId = addProjectWith1TaskWith2SubtasksAndReturnId();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .get(baseUrl + "/{userId}/projects/{projectId}/full", userId, projectId)
        .then()
                .log()
                .body()
                .statusCode(OK.value())
                .body("id", equalTo(projectId))
                .body("name", equalTo("Test Project"))
                .body("children", hasSize(0))
                .body("tasks", hasSize(1))
                .body("tasks[0].children", hasSize(2));
    }

    @Test
    void shouldRespondWith404IfFullProjectNotFound() {
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .get(baseUrl + "/{userId}/projects/{projectId}/full", userId, 1)
        .then()
                .statusCode(NOT_FOUND.value());
    }

    @Test
    void shouldRespondWith401ToGetFullTreeIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .get(baseUrl + "/{userId}/projects/all", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWithFullProjectTree() {
        Integer project1Id = addProjectWith1TaskWith2SubtasksAndReturnId();
        Integer project2Id = addProjectWith2ChildrenAnd2TasksAndReturnId();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .get(baseUrl + "/{userId}/projects/all", userId)
        .then()
                .log()
                .body()
                .statusCode(OK.value())
                .body("$", hasSize(2))
                .body("$", hasItem(hasEntry("id", project1Id)))
                .body("$", hasItem(hasEntry("id", project2Id)))
                .rootPath("find{it.id == %s}", withArgs(project1Id))
                .body("name", equalTo("Test Project"))
                .body("children", hasSize(0))
                .body("tasks", hasSize(1))
                .body("tasks[0].children", hasSize(2))
                .rootPath("find{it.id == %s}", withArgs(project2Id))
                .body("name", equalTo("Test Project"))
                .body("children", hasSize(2))
                .body("tasks", hasSize(2));
    }

    private Integer addProjectWith1TaskWith2SubtasksAndReturnId() {
        Project project = Project.builder()
                .owner(userRepository.getById(userId))
                .name("Test Project")
                .generalOrder(1)
                .build();
        Task task1 = Task.builder()
                .owner(userRepository.getById(userId))
                .title("First Task")
                .completed(false)
                .project(project)
                .projectOrder(1)
                .dailyViewOrder(0)
                .build();
        Task subtask1 = Task.builder()
                .owner(userRepository.getById(userId))
                .title("First Subtask")
                .completed(false)
                .parent(task1)
                .project(project)
                .projectOrder(1)
                .dailyViewOrder(0)
                .build();
        Task subtask2 = Task.builder()
                .owner(userRepository.getById(userId))
                .title("Second Subtask")
                .completed(false)
                .parent(task1)
                .project(project)
                .projectOrder(2)
                .dailyViewOrder(0)
                .build();
        task1.setChildren(List.of(subtask1, subtask2));
        project.setTasks(List.of(task1));

        projectRepository.save(project);
        return project.getId();
    }

    private Integer addProjectWith2ChildrenAnd2TasksAndReturnId() {
        Project project = Project.builder()
                .owner(userRepository.getById(userId))
                .name("Test Project")
                .generalOrder(1)
                .build();
        Task task1 = Task.builder()
                .owner(userRepository.getById(userId))
                .title("First Task")
                .completed(false)
                .project(project)
                .projectOrder(1)
                .dailyViewOrder(0)
                .build();
        Task task2 = Task.builder()
                .owner(userRepository.getById(userId))
                .title("Second Task")
                .completed(false)
                .project(project)
                .projectOrder(2)
                .dailyViewOrder(0)
                .build();
        project.setTasks(List.of(task1, task2));

        Project subProject1 = Project.builder()
                .owner(userRepository.getById(userId))
                .name("Sub Project 1")
                .parent(project)
                .build();
        Project subProject2 = Project.builder()
                .owner(userRepository.getById(userId))
                .name("Sub Project 1")
                .parent(project)
                .build();
        project.setChildren(List.of(subProject1, subProject2));

        projectRepository.save(project);
        return project.getId();
    }
}
