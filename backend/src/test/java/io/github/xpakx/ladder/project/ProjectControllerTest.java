package io.github.xpakx.ladder.project;

import io.github.xpakx.ladder.task.Task;
import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.task.dto.AddTaskRequest;
import io.github.xpakx.ladder.project.dto.ProjectRequest;
import io.github.xpakx.ladder.task.TaskRepository;
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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        Project project = Project.builder()
                .owner(userRepository.getById(userId))
                .name("Test Project")
                .parent(parent)
                .build();
        parent.setChildren(Collections.singletonList(project));
        return projectRepository.save(parent).getChildren().get(0).getId();
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

    private Integer addProjectWith2TasksAndReturnId() {
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

        projectRepository.save(project);
        return project.getId();
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

    @Test
    void shouldRespondWith401ToAddProjectIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .post(baseUrl + "/{userId}/projects", 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldAddProject() {
        ProjectRequest request = getValidAddProjectRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/{userId}/projects", userId)
        .then()
                .statusCode(CREATED.value())
                .body("name", equalTo(request.getName()))
                .body("color", equalTo(request.getColor()))
                .body("favorite", equalTo(false));
    }

    @Test
    void shouldAddProjectToParent() {
        ProjectRequest request = getAddProjectWithParentRequest(
                addProjectWith1TaskWith2SubtasksAndReturnId()
        );
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/{userId}/projects", userId)
        .then()
                .statusCode(CREATED.value())
                .body("name", equalTo(request.getName()))
                .body("color", equalTo(request.getColor()))
                .body("favorite", equalTo(false));
    }

    @Test
    void shouldNotAddProjectWithNonexistentParent() {
        ProjectRequest request = getAddProjectWithParentRequest(150);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/{userId}/projects", userId)
        .then()
                .statusCode(BAD_REQUEST.value());
    }

    private ProjectRequest getValidAddProjectRequest() {
        ProjectRequest request = new ProjectRequest();
        request.setName("Added Project");
        request.setColor("#ffffff");
        return request;
    }

    private ProjectRequest getValidUpdateProjectRequest() {
        ProjectRequest request = new ProjectRequest();
        request.setName("Updated Project");
        request.setColor("#ffffff");
        return request;
    }

    private ProjectRequest getAddProjectWithParentRequest(Integer parentId) {
        ProjectRequest request = new ProjectRequest();
        request.setName("Added Project");
        request.setColor("#ffffff");
        request.setParentId(parentId);
        return request;
    }

    @Test
    void shouldRespondWith401ToUpdateProjectIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .put(baseUrl + "/{userId}/projects/{projectId}", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToUpdateProjectIfProjectNotFound() {
        ProjectRequest request = getValidUpdateProjectRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/projects/{projectId}", userId, 1)
        .then()
                .statusCode(NOT_FOUND.value());
    }

    @Test
    void shouldUpdateProject() {
        ProjectRequest request = getValidUpdateProjectRequest();
        Integer projectId = addProjectWith2ChildrenAnd2TasksAndReturnId();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/projects/{projectId}", userId, projectId)
        .then()
                .statusCode(OK.value())
                .body("name", equalTo(request.getName()))
                .body("color", equalTo(request.getColor()))
                .body("favorite", equalTo(false));
    }


    @Test
    void shouldRespondWith401ToDeleteProjectIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .delete(baseUrl + "/{userId}/projects/{projectId}", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldDeleteProject() {
        Integer projectId = addProjectWithParentAndReturnId();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .delete(baseUrl + "/{userId}/projects/{projectId}", userId, projectId)
        .then()
                .statusCode(OK.value());
    }

    @Test
    void shouldDeleteProjectWithTasks() {
        Integer projectId = addProjectWith2TasksAndReturnId();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .delete(baseUrl + "/{userId}/projects/{projectId}", userId, projectId)
        .then()
                .statusCode(OK.value());

        assertEquals(0, taskRepository.findAll().size());
    }

    @Test
    void shouldDeleteProjectWithTasksAndSubprojects() {
        Integer projectId = addProjectWith2ChildrenAnd2TasksAndReturnId();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .delete(baseUrl + "/{userId}/projects/{projectId}", userId, projectId)
        .then()
                .statusCode(OK.value());

        assertEquals(0, taskRepository.findAll().size());
        assertEquals(0, projectRepository.findAll().size());
    }

    @Test
    void shouldRespondWith401ToAddTaskIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .post(baseUrl + "/{userId}/projects/{projectId}/tasks", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToAddTaskIfProjectNotFound() {
        AddTaskRequest request = getValidAddTaskRequest(150);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/{userId}/projects/{projectId}/tasks", userId, 1)
        .then()
                .statusCode(NOT_FOUND.value());
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
    void shouldAddTaskToProject() {
        Integer projectId = addProjectWithParentAndReturnId();
        AddTaskRequest request = getValidAddTaskRequest(projectId);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/{userId}/projects/{projectId}/tasks", userId, projectId)
        .then()
                .statusCode(CREATED.value())
                .body("title", equalTo(request.getTitle()));
    }

    @Test
    void shouldRespondWith401ToAddTaskToInboxIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .post(baseUrl + "/{userId}/projects/inbox/tasks", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldAddTaskToInbox() {
        Integer projectId = addProjectWithParentAndReturnId();
        AddTaskRequest request = getValidAddTaskRequest(projectId);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/{userId}/projects/inbox/tasks", userId)
        .then()
                .statusCode(CREATED.value())
                .body("title", equalTo(request.getTitle()));
    }
}