package io.github.xpakx.ladder.task;

import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.task.dto.AddTaskRequest;
import io.github.xpakx.ladder.common.dto.IdRequest;
import io.github.xpakx.ladder.collaboration.CollaborationRepository;
import io.github.xpakx.ladder.project.ProjectRepository;
import io.github.xpakx.ladder.user.UserAccountRepository;
import io.github.xpakx.ladder.security.JwtTokenUtil;
import io.github.xpakx.ladder.user.UserService;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TaskMovableControllerTest {
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

    private IdRequest getValidIdRequest(Integer id) {
        IdRequest request = new IdRequest();
        request.setId(id);
        return request;
    }

    @Test
    void shouldRespondWith401ToMoveProjectAfterIfUserUnauthorized() {
        given()
                .log()
                .uri()
                .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/move/after", 1, 1)
                .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldMoveTaskAfter() {
        List<Integer> ids = add3TasksInOrderAndReturnListOfIds();
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
                .put(baseUrl + "/{userId}/tasks/{taskId}/move/after", userId, taskId)
        .then()
                .statusCode(OK.value());


        List<Task> tasks = taskRepository.findAll();
        assertThat(tasks, hasSize(3));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 1")),
                hasProperty("projectOrder", is(1))
        )));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 2")),
                hasProperty("projectOrder", is(3))
        )));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 3")),
                hasProperty("projectOrder", is(2))
        )));
    }

    private List<Integer> add3TasksInOrderAndReturnListOfIds() {
        Task task1 = Task.builder()
                .owner(userRepository.getById(userId))
                .projectOrder(1)
                .title("Task 1")
                .build();
        Task task2 = Task.builder()
                .owner(userRepository.getById(userId))
                .projectOrder(2)
                .title("Task 2")
                .build();
        Task task3 = Task.builder()
                .owner(userRepository.getById(userId))
                .projectOrder(3)
                .title("Task 3")
                .build();
        return taskRepository.saveAll(List.of(task1, task2, task3)).stream()
                .sorted(Comparator.comparingInt(Task::getProjectOrder))
                .map(Task::getId)
                .collect(Collectors.toList());
    }

    @Test
    void shouldMoveTaskAfterWithParent() {
        List<Integer> ids = add3TasksWithParentInOrderAndReturnListOfIds();
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
                .put(baseUrl + "/{userId}/tasks/{taskId}/move/after", userId, taskId)
        .then()
                .statusCode(OK.value());

        List<Task> tasks = taskRepository.findAll();
        assertThat(tasks, hasSize(4));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 1")),
                hasProperty("projectOrder", is(1))
        )));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 2")),
                hasProperty("projectOrder", is(3))
        )));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 3")),
                hasProperty("projectOrder", is(2))
        )));
    }

    private List<Integer> add3TasksWithParentInOrderAndReturnListOfIds() {
        Task parent = Task.builder()
                .owner(userRepository.getById(userId))
                .projectOrder(1)
                .title("Parent")
                .build();
        parent = taskRepository.save(parent);
        Task project1 = Task.builder()
                .owner(userRepository.getById(userId))
                .projectOrder(1)
                .parent(parent)
                .title("Task 1")
                .build();
        Task project2 = Task.builder()
                .owner(userRepository.getById(userId))
                .projectOrder(2)
                .parent(parent)
                .title("Task 2")
                .build();
        Task project3 = Task.builder()
                .owner(userRepository.getById(userId))
                .projectOrder(3)
                .parent(parent)
                .title("Task 3")
                .build();
        return taskRepository.saveAll(List.of(project1, project2, project3)).stream()
                .sorted(Comparator.comparingInt(Task::getProjectOrder))
                .map(Task::getId)
                .collect(Collectors.toList());
    }

    @Test
    void shouldRespondWith401ToMoveTaskAsFirstChildIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/move/asChild", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldMoveTaskAsFirstChild() {
        List<Integer> ids = add3TasksWithParentInOrderAndReturnListOfIds();
        IdRequest request = getValidIdRequest(
                taskRepository.findById(ids.get(0)).get().getParent().getId()
        );
        Integer taskId = ids.get(2);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/move/asChild", userId, taskId)
        .then()
                .statusCode(OK.value());

        List<Task> tasks = taskRepository.findAll();
        assertThat(tasks, hasSize(4));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 1")),
                hasProperty("projectOrder", is(2))
        )));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 2")),
                hasProperty("projectOrder", is(3))
        )));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 3")),
                hasProperty("projectOrder", is(1))
        )));
    }

    @Test
    void shouldRespondWith401ToMoveTaskAsFirstIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/move/asFirst", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldMoveTaskAsFirst() {
        List<Integer> ids = add3TasksInOrderAndReturnListOfIds();

        Integer taskId = ids.get(2);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/move/asFirst", userId, taskId)
        .then()
                .statusCode(OK.value());

        List<Task> tasks = taskRepository.findAll();
        assertThat(tasks, hasSize(3));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 1")),
                hasProperty("projectOrder", is(2))
        )));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 2")),
                hasProperty("projectOrder", is(3))
        )));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 3")),
                hasProperty("projectOrder", is(1))
        )));
    }

    @Test
    void shouldRespondWith401ToAddTaskAfterIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .post(baseUrl + "/{userId}/tasks/{taskId}/after", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldAddTaskAfter() {
        AddTaskRequest request = getValidAddTaskRequest();
        Integer taskId = add3TasksInOrderAndReturnIdOfMiddleOne();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/{userId}/tasks/{taskId}/after", userId, taskId)
        .then()
                .statusCode(CREATED.value())
                .body("title", equalTo(request.getTitle()))
                .body("description", equalTo(request.getDescription()))
                .body("priority", equalTo(request.getPriority()));

        List<Task> tasks = taskRepository.findAll();
        assertThat(tasks, hasSize(4));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Added Task")),
                hasProperty("projectOrder", is(2))
        )));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 1")),
                hasProperty("projectOrder", is(0))
        )));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 2")),
                hasProperty("projectOrder", is(1))
        )));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 3")),
                hasProperty("projectOrder", is(3))
        )));
    }

    private Integer add3TasksInOrderAndReturnIdOfMiddleOne() {
        Task task1 = Task.builder()
                .owner(userRepository.getById(userId))
                .projectOrder(0)
                .title("Task 1")
                .build();
        Task task2 = Task.builder()
                .owner(userRepository.getById(userId))
                .projectOrder(1)
                .title("Task 2")
                .build();
        Task task3 = Task.builder()
                .owner(userRepository.getById(userId))
                .projectOrder(2)
                .title("Task 3")
                .build();
        return taskRepository.saveAll(List.of(task1, task2, task3)).stream()
                .filter(a -> a.getProjectOrder().equals(1))
                .map(Task::getId)
                .findAny()
                .orElse(-1);
    }

    private AddTaskRequest getValidAddTaskRequest() {
        AddTaskRequest request = new AddTaskRequest();
        request.setTitle("Added Task");
        request.setDescription("Newly added task.");
        request.setProjectOrder(0);
        request.setPriority(0);
        return request;
    }

    @Test
    void shouldRespondWith401ToAddTaskBeforeIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .post(baseUrl + "/{userId}/tasks/{taskId}/before", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldAddTaskBefore() {
        AddTaskRequest request = getValidAddTaskRequest();
        Integer taskId = add3TasksInOrderAndReturnIdOfMiddleOne();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/{userId}/tasks/{taskId}/before", userId, taskId)
        .then()
                .statusCode(CREATED.value())
                .body("title", equalTo(request.getTitle()))
                .body("description", equalTo(request.getDescription()));

        List<Task> tasks = taskRepository.findAll();
        assertThat(tasks, hasSize(4));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Added Task")),
                hasProperty("projectOrder", is(1))
        )));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 1")),
                hasProperty("projectOrder", is(0))
        )));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 2")),
                hasProperty("projectOrder", is(2))
        )));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 3")),
                hasProperty("projectOrder", is(3))
        )));
    }

    @Test
    void shouldRespondWith401ToAddTaskAsChildIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .post(baseUrl + "/{userId}/tasks/{taskId}/children", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldAddTaskAsChild() {
        AddTaskRequest request = getValidAddTaskRequest();
        Integer parentId = addTaskWithParentAndReturnParentId();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/{userId}/tasks/{parentId}/children", userId, parentId)
        .then()
                .statusCode(CREATED.value())
                .body("title", equalTo(request.getTitle()))
                .body("description", equalTo(request.getDescription()));

        List<Task> tasks = taskRepository.findAll();
        assertThat(tasks, hasSize(3));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Added Task")),
                hasProperty("projectOrder", is(2)),
                hasProperty("parent", hasProperty("id", is(parentId)))
        )));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Task 1")),
                hasProperty("projectOrder", is(1))
        )));
        assertThat(tasks, hasItem(allOf(
                hasProperty("title", is("Parent")),
                hasProperty("projectOrder", is(1))
        )));
    }

    private Integer addTaskWithParentAndReturnParentId() {
        Task parent = Task.builder()
                .owner(userRepository.getById(userId))
                .projectOrder(1)
                .title("Parent")
                .build();
        parent = taskRepository.save(parent);
        Task task1 = Task.builder()
                .owner(userRepository.getById(userId))
                .projectOrder(1)
                .parent(parent)
                .title("Task 1")
                .build();
        taskRepository.save(task1);
        return parent.getId();
    }
}
