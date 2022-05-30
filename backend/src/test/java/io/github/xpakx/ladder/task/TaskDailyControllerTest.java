package io.github.xpakx.ladder.task;

import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.common.dto.DateRequest;
import io.github.xpakx.ladder.common.dto.IdRequest;
import io.github.xpakx.ladder.collaboration.CollaborationRepository;
import io.github.xpakx.ladder.project.ProjectRepository;
import io.github.xpakx.ladder.user.UserAccountRepository;
import io.github.xpakx.ladder.security.JwtTokenUtil;
import io.github.xpakx.ladder.user.UserService;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TaskDailyControllerTest {
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
    CollaborationRepository collaborationRepository;

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
        collaborationRepository.deleteAll();
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String tokenFor(String username) {
        return jwtTokenUtil.generateToken(userService.loadUserToLogin(username));
    }

    @Test
    void shouldRespondWith401ToMoveTaskAsFirstIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/daily/move/asFirst", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    @Disabled
    void shouldMoveTaskAsFirst() {
        List<Integer> ids = add3TasksWithDueDateInOrderAndReturnListOfIds(LocalDateTime.now());

        Integer taskId = ids.get(2);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/daily/move/asFirst", userId, taskId)
        .then()
                .statusCode(OK.value());

        List<Task> tasks = taskRepository.findAll();
        assertThat(tasks, hasSize(3));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 1")),
                hasProperty("dailyViewOrder", is(2))
        )));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 2")),
                hasProperty("dailyViewOrder", is(3))
        )));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 3")),
                hasProperty("dailyViewOrder", is(1))
        )));
    }

    private List<Integer> add3TasksWithDueDateInOrderAndReturnListOfIds(LocalDateTime date) {
        Task project1 = Task.builder()
                .owner(userRepository.getById(userId))
                .dailyViewOrder(1)
                .due(date)
                .title("Task 1")
                .build();
        Task project2 = Task.builder()
                .owner(userRepository.getById(userId))
                .dailyViewOrder(2)
                .due(date)
                .title("Task 2")
                .build();
        Task project3 = Task.builder()
                .owner(userRepository.getById(userId))
                .dailyViewOrder(3)
                .due(date)
                .title("Task 3")
                .build();
        return taskRepository.saveAll(List.of(project1, project2, project3)).stream()
                .sorted(Comparator.comparingInt(Task::getDailyViewOrder))
                .map(Task::getId)
                .collect(Collectors.toList());
    }

    private Integer addTaskAndReturnId(LocalDateTime date) {
        Task task = Task.builder()
                .owner(userRepository.getById(userId))
                .dailyViewOrder(1)
                .due(date)
                .title("Future Task")
                .build();
        return taskRepository.save(task).getId();
    }

    @Test
    void shouldRespondWith401ToMoveTaskAsFirstForDateIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/daily/move/asFirstWithDate", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    @Disabled
    void shouldMoveTaskAsFirstForDate() {
        DateRequest request = getValidDateRequest();
        List<Integer> ids = add3TasksWithDueDateInOrderAndReturnListOfIds(request.getDate());

        Integer taskId = ids.get(2);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/daily/move/asFirstWithDate", userId, taskId)
        .then()
                .statusCode(OK.value());

        List<Task> tasks = taskRepository.findAll();
        assertThat(tasks, hasSize(3));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 1")),
                hasProperty("dailyViewOrder", is(2))
        )));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 2")),
                hasProperty("dailyViewOrder", is(3))
        )));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 3")),
                hasProperty("dailyViewOrder", is(1))
        )));
    }

    private DateRequest getValidDateRequest() {
        DateRequest request = new DateRequest();
        request.setDate(LocalDateTime.of(2022, 1, 12, 12, 32));
        return request;
    }

    @Test
    void shouldRespondWith401ToMoveTaskAfterIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/daily/move/after", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    @Disabled
    void shouldMoveTaskAfter() {
        List<Integer> ids = add3TasksWithDueDateInOrderAndReturnListOfIds(LocalDateTime.now());
        IdRequest request = getValidIdRequest(ids.get(0));
        Integer taskId = ids.get(2);

        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/daily/move/after", userId, taskId)
        .then()
                .statusCode(OK.value());


        List<Task> tasks = taskRepository.findAll();
        assertThat(tasks, hasSize(3));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 1")),
                hasProperty("dailyViewOrder", is(1))
        )));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 2")),
                hasProperty("dailyViewOrder", is(3))
        )));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 3")),
                hasProperty("dailyViewOrder", is(2))
        )));
    }

    private IdRequest getValidIdRequest(Integer id) {
        IdRequest request = new IdRequest();
        request.setId(id);
        return request;
    }

    @Test
    void shouldRespondWith401ToUpdateOverdueTasksDueDateIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .put(baseUrl + "/{userId}/tasks/overdue/due", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    @Disabled
    void shouldUpdateOverdueTasksDueDate() {
        add3TasksWithDueDateInOrderAndReturnListOfIds(LocalDateTime.now().minusMonths(1));
        Integer futureTaskId = addTaskAndReturnId(LocalDateTime.now().plusDays(1));
        DateRequest request = getValidDateRequest();

        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/tasks/overdue/due", userId)
        .then()
                .statusCode(OK.value());


        List<Task> tasks = taskRepository.findAll();
        assertThat(tasks, hasSize(3));
        assertThat(tasks, everyItem(
                either(hasProperty("due", is(equalTo(request.getDate()))))
                .or(hasProperty("id", is(equalTo(futureTaskId))
        ))));
    }
}
