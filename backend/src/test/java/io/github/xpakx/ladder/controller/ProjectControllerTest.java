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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
                .build();
        Task task1 = Task.builder()
                .owner(userRepository.getById(userId))
                .title("First Task")
                .completed(false)
                .project(project)
                .build();
        Task task2 = Task.builder()
                .owner(userRepository.getById(userId))
                .title("Second Task")
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

    private Integer addNonFavoriteProject() {
        Project project = Project.builder()
                .owner(userRepository.getById(userId))
                .name("Test Project")
                .favorite(false)
                .build();
        project = projectRepository.save(project);
        return project.getId();
    }

    private Integer addProjectWith1TaskWith2SubtasksAndReturnId() {
        Project project = Project.builder()
                .owner(userRepository.getById(userId))
                .name("Test Project")
                .build();
        Task task1 = Task.builder()
                .owner(userRepository.getById(userId))
                .title("First Task")
                .completed(false)
                .project(project)
                .build();
        Task subtask1 = Task.builder()
                .owner(userRepository.getById(userId))
                .title("First Subtask")
                .completed(false)
                .parent(task1)
                .project(project)
                .build();
        Task subtask2 = Task.builder()
                .owner(userRepository.getById(userId))
                .title("Second Subtask")
                .completed(false)
                .parent(task1)
                .project(project)
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

    private ProjectRequest getUpdateProjectWithParentRequest(Integer parentId) {
        ProjectRequest request = new ProjectRequest();
        request.setName("Updated Project");
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
    void shouldChangeProjectParentWhileUpdating() {
        Integer projectId = addProjectWithParentAndReturnId();
        ProjectRequest request = getUpdateProjectWithParentRequest(
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
                .put(baseUrl + "/{userId}/projects/{projectId}", userId, projectId)
        .then()
                .statusCode(OK.value());

        checkIfParentIsEdited(projectId, request.getParentId());
    }

    private void checkIfParentIsEdited(Integer projectId, Integer parentId) {
        given()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .get(baseUrl + "/{userId}/projects/{projectId}", userId, projectId)
        .then()
                .body("parent.id", equalTo(parentId));
    }

    @Test
    void shouldRespondWith401ToUpdateProjectNameIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .put(baseUrl + "/{userId}/projects/{projectId}/name", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToUpdateProjectNameIfProjectNotFound() {
        NameRequest request = getValidUpdateProjectNameRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/projects/{projectId}/name", userId, 1)
        .then()
                .statusCode(NOT_FOUND.value());
    }

    private NameRequest getValidUpdateProjectNameRequest() {
        NameRequest request = new NameRequest();
        request.setName("Project with updated name");
        return request;
    }

    @Test
    void shouldUpdateProjectName() {
        Integer projectId = addProjectWithParentAndReturnId();
        NameRequest request = getValidUpdateProjectNameRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/projects/{projectId}/name", userId, projectId)
        .then()
                .statusCode(OK.value())
                .body("name", equalTo(request.getName()));
    }

    @Test
    void shouldRespondWith401ToUpdateProjectParentIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .put(baseUrl + "/{userId}/projects/{projectId}/parent", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToUpdateProjectParentIfProjectNotFound() {
        IdRequest request = getValidIdRequest(
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
                .put(baseUrl + "/{userId}/projects/{projectId}/parent", userId, 1)
        .then()
                .statusCode(NOT_FOUND.value());
    }

    private IdRequest getValidIdRequest(Integer id) {
        IdRequest request = new IdRequest();
        request.setId(id);
        return request;
    }

    @Test
    void shouldUpdateProjectParent() {
        Integer projectId = addProjectWithParentAndReturnId();
        IdRequest request = getValidIdRequest(
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
                .put(baseUrl + "/{userId}/projects/{projectId}/parent", userId, projectId)
        .then()
                .statusCode(OK.value());

        checkIfParentIsEdited(projectId, request.getId());
    }
    @Test
    void shouldNotUpdateProjectParentIfParentDoesNotExist() {
        Integer projectId = addProjectWithParentAndReturnId();
        IdRequest request = getValidIdRequest(150);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/projects/{projectId}/parent", userId, projectId)
        .then()
                .statusCode(BAD_REQUEST.value());
    }

    @Test
    void shouldRespondWith401ToUpdateProjectFavoriteStatusIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .put(baseUrl + "/{userId}/projects/{projectId}/favorite", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToUpdateProjectFavoriteStatusIfProjectNotFound() {
        BooleanRequest request = getTrueBooleanRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/projects/{projectId}/favorite", userId, 1)
        .then()
                .statusCode(NOT_FOUND.value());
    }

    private BooleanRequest getTrueBooleanRequest() {
        BooleanRequest request = new BooleanRequest();
        request.setFlag(true);
        return request;
    }

    @Test
    void shouldUpdateProjectFavoriteStatus() {
        Integer projectId = addNonFavoriteProject();
        BooleanRequest request = getTrueBooleanRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/projects/{projectId}/favorite", userId, projectId)
        .then()
                .statusCode(OK.value())
                .body("favorite", equalTo(true));
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
    void shouldRespondWith401ToDuplicateProjectRequestIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .post(baseUrl + "/{userId}/projects/{projectId}/duplicate", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToDuplicateProjectRequestIfProjectNotFound() {
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .post(baseUrl + "/{userId}/projects/{projectId}/duplicate", userId, 1)
        .then()
                .statusCode(NOT_FOUND.value());
    }

    @Test
    void shouldDuplicateWholeProjectTree() {
        Integer projectId = addProjectWith2ChildrenAnd2TasksAndReturnId();
        Integer projects = projectRepository.findAll().size();
        Integer tasks = taskRepository.findAll().size();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .post(baseUrl + "/{userId}/projects/{projectId}/duplicate", userId, projectId)
        .then()
                .statusCode(CREATED.value());
        assertEquals(2*projects, projectRepository.findAll().size());
        assertEquals(2*tasks, taskRepository.findAll().size());
    }

    @Test
    void shouldDuplicateWholeProjectWithSubtasksTree() {
        Integer projectId = addProjectWith1TaskWith2SubtasksAndReturnId();
        Integer projects = projectRepository.findAll().size();
        Integer tasks = taskRepository.findAll().size();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .when()
                .post(baseUrl + "/{userId}/projects/{projectId}/duplicate", userId, projectId)
                .then()
                .statusCode(CREATED.value());
        assertEquals(2*projects, projectRepository.findAll().size());
        assertEquals(2*tasks, taskRepository.findAll().size());
    }
}