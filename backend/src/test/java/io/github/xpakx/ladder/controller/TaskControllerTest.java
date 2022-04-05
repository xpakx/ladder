package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.UserAccount;
import io.github.xpakx.ladder.entity.dto.*;
import io.github.xpakx.ladder.repository.ProjectRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
import io.github.xpakx.ladder.security.JwtTokenUtil;
import io.github.xpakx.ladder.service.UserService;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
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

    private DateRequest getValidDateRequest() {
        DateRequest request = new DateRequest();
        request.setDate(LocalDateTime.of(2020, 12, 12, 12, 12, 12));
        return request;
    }

    private PriorityRequest getValidPriorityRequest() {
        PriorityRequest request = new PriorityRequest();
        request.setPriority(3);
        return request;
    }

    @Test
    void shouldRespondWith401ToUpdateTaskDueDateIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/due", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToUpdateTaskDueDateIfTaskNotFound() {
        DateRequest request = getValidDateRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/due", userId, 1)
        .then()
                .statusCode(NOT_FOUND.value());
    }

    @Test
    @Disabled
    void shouldUpdateTaskDueDate() {
        Integer taskId = addTaskAndReturnId();
        DateRequest request = getValidDateRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/due", userId, taskId)
        .then()
                .statusCode(OK.value())
                .body("due", equalTo(request.getDate().toString()));
    }

    @Test
    void shouldRespondWith401ToUpdateTaskPriorityIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/priority", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToUpdateTaskPriorityIfTaskNotFound() {
        PriorityRequest request = getValidPriorityRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/priority", userId, 1)
        .then()
                .statusCode(NOT_FOUND.value());
    }

    @Test
    void shouldUpdateTaskPriority() {
        Integer taskId = addTaskAndReturnId();
        PriorityRequest request = getValidPriorityRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/priority", userId, taskId)
        .then()
                .statusCode(OK.value())
                .body("priority", equalTo(request.getPriority()));
    }

    @Test
    void shouldRespondWith401ToUpdateTaskProjectUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/project", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToUpdateTaskProjectIfTaskNotFound() {
        IdRequest request = getValidIdRequest(5);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/project", userId, 1)
        .then()
                .statusCode(NOT_FOUND.value());
    }

    private IdRequest getValidIdRequest(Integer id) {
        IdRequest request = new IdRequest();
        request.setId(id);
        return request;
    }

    @Test
    void shouldUpdateTaskProject() {
        Integer taskId = addTaskAndReturnId();
        IdRequest request = getValidIdRequest(
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
                .put(baseUrl + "/{userId}/tasks/{taskId}/project", userId, taskId)
        .then()
                .statusCode(OK.value());

        checkIfProjectIsEdited(taskId, request.getId());
    }

    @Test
    void shouldRespondWith401ToUpdateTaskCompletionIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/completed", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToUpdateTaskCompletionIfTaskNotFound() {
        BooleanRequest request = getValidBooleanRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/completed", userId, 1)
        .then()
                .statusCode(NOT_FOUND.value());
    }

    private BooleanRequest getValidBooleanRequest() {
        BooleanRequest request = new BooleanRequest();
        request.setFlag(true);
        return request;
    }

    @Test
    void shouldUpdateTaskCompletion() {
        Integer taskId = addTaskAndReturnId();
        BooleanRequest request = getValidBooleanRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/completed", userId, taskId)
        .then()
                .statusCode(OK.value())
                .body("completed", equalTo(true));
    }

    @Test
    void shouldUpdateSubtaskOnUpdatingTaskCompletion() {
        Integer taskId = addTaskWith2SubtasksAndReturnId();
        BooleanRequest request = getValidBooleanRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/completed", userId, taskId)
        .then()
                .statusCode(OK.value());

        List<Task> tasks = taskRepository.findByOwnerIdAndParentId(userId, taskId);
        assertThat(tasks, everyItem(hasProperty("completed", equalTo(true))));
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
        Integer tasks = taskRepository.findAll().size();
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

    @Test
    void shouldRespondWith401ToUpdateTaskAssignationIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/assigned", userId, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToUpdateTaskAssignationIfTaskNotFound() {
        IdRequest request = getValidIdRequest(0);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/assigned", userId, 1)
        .then()
                .statusCode(NOT_FOUND.value());
    }

    @Test
    void shouldAssignOwnerToTask() {
        Integer taskId = addTaskAndReturnId();
        IdRequest request = getValidIdRequest(userId);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/assigned", userId, taskId)
        .then()
                .statusCode(OK.value());
        Task task = taskRepository.findById(taskId).orElse(null);
        assertNotNull(task);
        assertNotNull(task.getAssigned());
        assertThat(task.getAssigned().getUsername(), is(equalTo("user1")));
    }

    @Test
    void shouldNotAssignNonCollaboratorToTask() {
        Integer taskId = addTaskAndReturnId();
        IdRequest request = getValidIdRequest(addUser("user2"));
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/assigned", userId, taskId)
                .then()
                .statusCode(BAD_REQUEST.value());
        Task task = taskRepository.findById(taskId).orElse(null);
        assertNotNull(task);
        assertNull(task.getAssigned());
    }

    private Integer addUser(String username) {
        UserAccount user = UserAccount.builder()
                .username(username)
                .password("password")
                .roles(new HashSet<>())
                .build();
        return userRepository.save(user).getId();
    }
}