package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.UserAccount;
import io.github.xpakx.ladder.repository.ProjectRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
import io.github.xpakx.ladder.security.JwtTokenUtil;
import io.github.xpakx.ladder.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProjectControllerTest {

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

    private Integer addProjectWithParentAndReturnId() {
        Project parent = Project.builder()
                .owner(userRepository.getById(userId))
                .name("Test Parent Project")
                .build();
        parent = projectRepository.save(parent);
        Project project = Project.builder()
                .owner(userRepository.getById(userId))
                .name("Test Project")
                .parent(parent)
                .build();
        parent.setChildren(Collections.singletonList(project));
        return projectRepository.save(project).getId();
    }

    private Integer addProjectWith2ChildrenAnd2TasksAndReturnId() {
        Project project = Project.builder()
                .owner(userRepository.getById(userId))
                .name("Test Project")
                .build();
        project = projectRepository.save(project);
        Task task1 = Task.builder()
                .owner(userRepository.getById(userId))
                .title("First Task")
                .completed(false)
                .project(project)
                .build();
        Task task2 = Task.builder()
                .owner(userRepository.getById(userId))
                .title("First Task")
                .completed(false)
                .project(project)
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


    @Test
    void shouldRespondWith401ToGetProjectIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .get(baseUrl + "/{userId}/projects/{projectId}", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWithProjectDetails() {
        Integer projectId = addProjectWithParentAndReturnId();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .get(baseUrl + "/{userId}/projects/{projectId}", userId, projectId)
        .then()
                .statusCode(OK.value())
                .body("id", equalTo(projectId))
                .body("name", equalTo("Test Project"))
                .body("parent.name", equalTo("Test Parent Project"));
    }

    @Test
    void shouldNotProduceTasksAndSubProjectsWhenGettingProjectDetails() {
        Integer projectId = addProjectWith2ChildrenAnd2TasksAndReturnId();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .get(baseUrl + "/{userId}/projects/{projectId}", userId, projectId)
        .then()
                .statusCode(OK.value())
                .body("id", equalTo(projectId))
                .body("name", equalTo("Test Project"))
                .body("$", not(hasKey("children")))
                .body("$", not(hasKey("tasks")));
    }

    @Test
    void shouldRespondWith404IfProjectNotFound() {
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .get(baseUrl + "/{userId}/projects/{projectId}", userId, 1)
        .then()
                .statusCode(NOT_FOUND.value());
    }
}