package io.github.xpakx.ladder.sync;

import io.github.xpakx.ladder.label.Label;
import io.github.xpakx.ladder.project.Project;
import io.github.xpakx.ladder.task.Task;
import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.label.LabelRepository;
import io.github.xpakx.ladder.project.ProjectRepository;
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
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MainControllerTest {
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
    TaskRepository taskRepository;
    @Autowired
    LabelRepository labelRepository;
    @Autowired
    ProjectRepository projectRepository;

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
        labelRepository.deleteAll();
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    void addUser2() {
        UserAccount user = UserAccount.builder()
                .username("user2")
                .password("password")
                .roles(new HashSet<>())
                .build();
        userRepository.save(user);
    }

    void add3Projects2Labels2TasksToUser1() {
        Label label1 = Label.builder()
                .owner(userRepository.getById(userId))
                .name("Label 1")
                .build();
        Label label2 = Label.builder()
                .owner(userRepository.getById(userId))
                .name("Label 2")
                .build();
        labelRepository.saveAll(List.of(label1, label2));
        Task task1 = Task.builder()
                .owner(userRepository.getById(userId))
                .title("Task 1")
                .completed(false)
                .build();
        Task task2 = Task.builder()
                .owner(userRepository.getById(userId))
                .title("Task 2")
                .completed(false)
                .build();
        taskRepository.saveAll(List.of(task1, task2));
        Project project1 = Project.builder()
                .owner(userRepository.getById(userId))
                .name("Project 1")
                .build();
        Project project2 = Project.builder()
                .owner(userRepository.getById(userId))
                .name("Project 2")
                .build();
        Project project3 = Project.builder()
                .owner(userRepository.getById(userId))
                .name("Project 3")
                .build();
        projectRepository.saveAll(List.of(project1, project2, project3));
    }

    private String tokenFor(String username) {
        return jwtTokenUtil.generateToken(userService.loadUserToLogin(username));
    }

    @Test
    void shouldRespondWith401ToGetUserInfoIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .get(baseUrl + "/{userId}/all", 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith403ToGetUserInfoIfWrongUser() {
        addUser2();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user2"))
        .when()
                .get(baseUrl + "/{userId}/all", userId)
        .then()
                .statusCode(FORBIDDEN.value());
    }

    @Test
    void shouldProduceUserInfo() {
        add3Projects2Labels2TasksToUser1();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .get(baseUrl + "/{userId}/all", userId)
        .then()
                .statusCode(OK.value())
                .body("id", equalTo(userId))
                .body("username", equalTo("user1"))
                .body("$", hasKey("projects"))
                .body("projects", hasSize(3))
                .body("$", hasKey("tasks"))
                .body("tasks", hasSize(2))
                .body("$", hasKey("labels"))
                .body("labels", hasSize(2));
    }
}