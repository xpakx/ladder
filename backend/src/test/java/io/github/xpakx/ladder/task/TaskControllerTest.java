package io.github.xpakx.ladder.task;

import io.github.xpakx.ladder.collaboration.CollaborationRepository;
import io.github.xpakx.ladder.label.LabelRepository;
import io.github.xpakx.ladder.project.Project;
import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.task.dto.AddTaskRequest;
import io.github.xpakx.ladder.project.ProjectRepository;
import io.github.xpakx.ladder.security.JwtTokenUtil;
import io.github.xpakx.ladder.user.UserService;
import io.github.xpakx.ladder.user.UserAccountRepository;
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
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskControllerTest {
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
    @Autowired
    LabelRepository labelRepository;

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
        labelRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String tokenFor(String username) {
        return jwtTokenUtil.generateToken(userService.loadUserToLogin(username));
    }

    private Integer addTaskAndReturnId() {
        Task task = Task.builder()
                .owner(userRepository.getById(userId))
                .title("Test Task")
                .build();
        return taskRepository.save(task).getId();
    }

    private Integer addProjectAndReturnId() {
        Project project = Project.builder()
                .owner(userRepository.getById(userId))
                .name("Test Project")
                .build();
        return projectRepository.save(project).getId();
    }

    private Integer addTaskWith2SubtasksAndReturnId() {
        Task task = Task.builder()
                .owner(userRepository.getById(userId))
                .title("First Task")
                .completed(false)
                .projectOrder(1)
                .dailyViewOrder(0)
                .build();
        Task subtask1 = Task.builder()
                .owner(userRepository.getById(userId))
                .title("First Subtask")
                .completed(false)
                .parent(task)
                .projectOrder(1)
                .dailyViewOrder(0)
                .build();
        Task subtask2 = Task.builder()
                .owner(userRepository.getById(userId))
                .title("Second Subtask")
                .completed(false)
                .parent(task)
                .projectOrder(2)
                .dailyViewOrder(0)
                .build();
        task.setChildren(List.of(subtask1, subtask2));

        return taskRepository.save(task).getId();
    }

    @Test
    void shouldRespondWith401ToDeleteTaskRequestIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .delete(baseUrl + "/{userId}/tasks/{taskId}", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldDeleteTask() {
        Integer taskId = addTaskAndReturnId();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .delete(baseUrl + "/{userId}/tasks/{taskId}", userId, taskId)
        .then()
                .statusCode(OK.value());
    }

    @Test
    void shouldDeleteTaskWithSubtasks() {
        Integer taskId = addTaskWith2SubtasksAndReturnId();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .delete(baseUrl + "/{userId}/tasks/{taskId}", userId, taskId)
        .then()
                .statusCode(OK.value());
        assertEquals(0, taskRepository.findAll().size());
    }

    @Test
    void shouldRespondWith401ToGetTaskDetailsIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .get(baseUrl + "/{userId}/tasks/{taskId}", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToGetTaskDetailsIfTaskNotFound() {
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .get(baseUrl + "/{userId}/tasks/{taskId}", userId, 1)
        .then()
                .statusCode(NOT_FOUND.value());
    }

    @Test
    void shouldRespondWithTaskDetails() {
        Integer taskId = addTaskAndReturnId();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .get(baseUrl + "/{userId}/tasks/{taskId}", userId, taskId)
        .then()
                .statusCode(OK.value())
                .body("id", equalTo(taskId))
                .body("title", equalTo("Test Task"));
    }

    @Test
    void shouldNotProduceSubtasksWhenGettingTaskDetails() {
        Integer taskId = addTaskWith2SubtasksAndReturnId();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .get(baseUrl + "/{userId}/tasks/{taskId}", userId, taskId)
        .then()
                .statusCode(OK.value())
                .body("id", equalTo(taskId))
                .body("title", equalTo("First Task"))
                .body("$", not(hasKey("children")));
    }

    private AddTaskRequest getValidAddTaskRequest(Integer projectId) {
        AddTaskRequest request = new AddTaskRequest();
        request.setTitle("Added Task");
        request.setDescription("Newly added task.");
        request.setProjectOrder(0);
        request.setProjectId(projectId);
        request.setPriority(0);
        return request;
    }

    @Test
    void shouldRespondWith401ToUpdateTaskIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToUpdateTaskIfTaskNotFound() {
        AddTaskRequest request = getValidAddTaskRequest(150);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}", userId, 1)
        .then()
                .statusCode(NOT_FOUND.value());
    }

    @Test
    void shouldUpdateTask() {
        Integer taskId = addTaskAndReturnId();
        AddTaskRequest request = getValidAddTaskRequest(
                addProjectAndReturnId()
        );
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}", userId, taskId)
        .then()
                .statusCode(OK.value())
                .body("title", equalTo(request.getTitle()))
                .body("projectOrder", equalTo(request.getProjectOrder()))
                .body("priority", equalTo(request.getPriority()));

        checkIfProjectIsEdited(taskId, request.getProjectId());
    }

    private void checkIfProjectIsEdited(Integer taskId, Integer parentId) {
        given()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .get(baseUrl + "/{userId}/tasks/{taskId}", userId, taskId)
        .then()
                .body("project.id", equalTo(parentId));
    }

    @Test
    void shouldRespondWith401ToDuplicateTaskIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .post(baseUrl + "/{userId}/tasks/{taskId}/duplicate", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToDuplicateTaskIfTaskNotFound() {
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .post(baseUrl + "/{userId}/tasks/{taskId}/duplicate", userId, 1)
        .then()
                .statusCode(NOT_FOUND.value());
    }

    @Test
    void shouldDuplicateTask() {
        Integer taskId = addTaskWith2SubtasksAndReturnId();
        int tasks = taskRepository.findAll().size();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .post(baseUrl + "/{userId}/tasks/{taskId}/duplicate", userId, taskId)
        .then()
                .statusCode(CREATED.value());
        assertEquals(2*tasks, taskRepository.findAll().size());
    }
}
