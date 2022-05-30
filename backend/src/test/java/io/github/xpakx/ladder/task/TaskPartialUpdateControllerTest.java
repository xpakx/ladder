package io.github.xpakx.ladder.task;

import io.github.xpakx.ladder.collaboration.Collaboration;
import io.github.xpakx.ladder.collaboration.CollaborationRepository;
import io.github.xpakx.ladder.common.dto.*;
import io.github.xpakx.ladder.label.Label;
import io.github.xpakx.ladder.label.LabelRepository;
import io.github.xpakx.ladder.project.Project;
import io.github.xpakx.ladder.project.ProjectRepository;
import io.github.xpakx.ladder.security.JwtTokenUtil;
import io.github.xpakx.ladder.user.UserService;
import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.user.UserAccountRepository;
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
import java.util.Set;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TaskPartialUpdateControllerTest {
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

    @Test
    void shouldAssignCollaboratorToTask() {
        Integer collaboratorId = addUser("collaborator");
        Integer taskId = addTaskAndProjectWithCollaboratorAndReturnTaskId(collaboratorId);
        IdRequest request = getValidIdRequest(collaboratorId);
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
        assertThat(task.getAssigned().getUsername(), is(equalTo("collaborator")));
    }

    private Integer addTaskAndProjectWithCollaboratorAndReturnTaskId(Integer collaboratorId) {
        Project project = Project.builder()
                .owner(userRepository.getById(this.userId))
                .name("Test Project")
                .generalOrder(1)
                .build();
        project = projectRepository.save(project);
        Collaboration collaboration = Collaboration.builder()
                .owner(userRepository.getById(collaboratorId))
                .project(project)
                .editionAllowed(true)
                .taskCompletionAllowed(true)
                .build();
        collaborationRepository.save(collaboration);
        project.setCollaborative(true);
        project.setCollaborators(List.of(collaboration));
        Integer projectId = projectRepository.save(project).getId();
        Task task = Task.builder()
                .owner(userRepository.getById(userId))
                .project(projectRepository.getById(projectId))
                .title("Test Task")
                .build();
        return taskRepository.save(task).getId();
    }

    @Test
    void shouldRespondWith401ToUpdateTaskCollapsedStateIfUserUnauthorized() {
        given()
                .log()
                .uri()
                .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/collapse", 1, 1)
                .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToUpdateTaskCollapsedStateIfTaskNotFound() {
        BooleanRequest request = getValidBooleanRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/collapse", userId, 1)
                .then()
                .statusCode(NOT_FOUND.value());
    }

    @Test
    void shouldCollapseTask() {
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
                .put(baseUrl + "/{userId}/tasks/{taskId}/collapse", userId, taskId)
                .then()
                .statusCode(OK.value())
                .body("collapsed", equalTo(true));
    }

    @Test
    void shouldRespondWith401ToUpdateTaskLabelsIfUserUnauthorized() {
        given()
                .log()
                .uri()
                .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/labels", 1, 1)
                .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToUpdateTaskLabelsIfTaskNotFound() {
        IdCollectionRequest request = getValidIdCollectionRequest(List.of(1,2));
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/labels", userId, 1)
                .then()
                .statusCode(NOT_FOUND.value());
    }

    private IdCollectionRequest getValidIdCollectionRequest(List<Integer> ids) {
        IdCollectionRequest request = new IdCollectionRequest();
        request.setIds(ids);
        return request;
    }

    @Test
    @Disabled
    void shouldUpdateTaskLabels() {
        Integer taskId = addTaskAndReturnId();
        IdCollectionRequest request = getValidIdCollectionRequest(add2Labels());
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(baseUrl + "/{userId}/tasks/{taskId}/labels", userId, taskId)
                .then()
                .statusCode(OK.value());

        Task task = taskRepository.findByOwnerId(userId, Task.class).get(0);
        Set<Label> labels = task.getLabels();
        assertThat(labels, hasSize(2));
        assertThat(labels, hasItem(hasProperty("name", is("label1"))));
        assertThat(labels, hasItem(hasProperty("name", is("label2"))));
    }

    private List<Integer> add2Labels() {
        Label label1 = Label.builder()
                .name("label1")
                .owner(userRepository.getById(userId))
                .build();
        Label label2 = Label.builder()
                .name("label2")
                .owner(userRepository.getById(userId))
                .build();
        return labelRepository.saveAll(List.of(label1, label2)).stream()
                .map(Label::getId)
                .collect(Collectors.toList());
    }
}
