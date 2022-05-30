package io.github.xpakx.ladder.project;

import io.github.xpakx.ladder.task.Task;
import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.common.dto.IdRequest;
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProjectMovableControllerTest {
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
        int projects = projectRepository.findAll().size();
        int tasks = taskRepository.findAll().size();
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
        int projects = projectRepository.findAll().size();
        int tasks = taskRepository.findAll().size();
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
    void shouldRespondWith401ToAddProjectAfterIfUserUnauthorized() {
        given()
                .log()
                .uri()
                .when()
                .post(baseUrl + "/{userId}/projects/{projectId}/after", 1, 1)
                .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldAddProjectAfter() {
        ProjectRequest request = getValidAddProjectRequest();
        Integer projectId = add3ProjectsInOrderAndReturnIdOfMiddleOne();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(baseUrl + "/{userId}/projects/{projectId}/after", userId, projectId)
                .then()
                .statusCode(CREATED.value())
                .body("name", equalTo(request.getName()))
                .body("color", equalTo(request.getColor()))
                .body("favorite", equalTo(false));

        List<Project> projects = projectRepository.findAll();
        assertThat(projects, hasSize(4));
        assertThat(projects, hasItem(allOf(
                hasProperty("name", is("Added Project")),
                hasProperty("generalOrder", is(2))
        )));
        assertThat(projects, hasItem(allOf(
                hasProperty("name", is("Project 1")),
                hasProperty("generalOrder", is(0))
        )));
        assertThat(projects, hasItem(allOf(
                hasProperty("name", is("Project 2")),
                hasProperty("generalOrder", is(1))
        )));
        assertThat(projects, hasItem(allOf(
                hasProperty("name", is("Project 3")),
                hasProperty("generalOrder", is(3))
        )));
    }

    private ProjectRequest getValidAddProjectRequest() {
        ProjectRequest request = new ProjectRequest();
        request.setName("Added Project");
        request.setColor("#ffffff");
        return request;
    }

    @Test
    void shouldRespondWith401ToAddProjectBeforeIfUserUnauthorized() {
        given()
                .log()
                .uri()
                .when()
                .post(baseUrl + "/{userId}/projects/{projectId}/before", 1, 1)
                .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldAddProjectBefore() {
        ProjectRequest request = getValidAddProjectRequest();
        Integer projectId = add3ProjectsInOrderAndReturnIdOfMiddleOne();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(baseUrl + "/{userId}/projects/{projectId}/before", userId, projectId)
                .then()
                .statusCode(CREATED.value())
                .body("name", equalTo(request.getName()))
                .body("color", equalTo(request.getColor()))
                .body("favorite", equalTo(false));

        List<Project> projects = projectRepository.findAll();
        assertThat(projects, hasSize(4));
        assertThat(projects, hasItem(allOf(
                hasProperty("name", is("Added Project")),
                hasProperty("generalOrder", is(1))
        )));
        assertThat(projects, hasItem(allOf(
                hasProperty("name", is("Project 1")),
                hasProperty("generalOrder", is(0))
        )));
        assertThat(projects, hasItem(allOf(
                hasProperty("name", is("Project 2")),
                hasProperty("generalOrder", is(2))
        )));
        assertThat(projects, hasItem(allOf(
                hasProperty("name", is("Project 3")),
                hasProperty("generalOrder", is(3))
        )));
    }

    @Test
    void shouldChangeProjectParentWhileUpdating() {
        Integer projectId = addProjectWithParentAndReturnId();
        IdRequest request = getUpdateProjectWithParentIdRequest(
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
                .put(baseUrl + "/{userId}/projects/{projectId}/move/asChild", userId, projectId)
                .then()
                .statusCode(OK.value());

        checkIfParentIsEdited(projectId, request.getId());
    }

    private IdRequest getUpdateProjectWithParentIdRequest(Integer parentId) {
        IdRequest request = new IdRequest();
        request.setId(parentId);
        return request;
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
    void shouldRespondWith401ToMoveProjectAfterIfUserUnauthorized() {
        given()
                .log()
                .uri()
                .when()
                .put(baseUrl + "/{userId}/projects/{projectId}/move/after", 1, 1)
                .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldMoveProjectAfter() {
        List<Integer> ids = add3ProjectsInOrderAndReturnListOfIds();
        IdRequest request = getValidIdRequest(ids.get(0));
        Integer projectId = ids.get(2);

        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(baseUrl + "/{userId}/projects/{projectId}/move/after", userId, projectId)
                .then()
                .statusCode(OK.value());


        List<Project> projects = projectRepository.findAll();
        assertThat(projects, hasSize(3));
        assertThat(projects, hasItem(allOf(
                hasProperty("name", is("Project 1")),
                hasProperty("generalOrder", is(1))
        )));
        assertThat(projects, hasItem(allOf(
                hasProperty("name", is("Project 2")),
                hasProperty("generalOrder", is(3))
        )));
        assertThat(projects, hasItem(allOf(
                hasProperty("name", is("Project 3")),
                hasProperty("generalOrder", is(2))
        )));
    }

    @Test
    void shouldMoveProjectAfterWithParent() {
        List<Integer> ids = add3ProjectsWithParentInOrderAndReturnListOfIds();
        IdRequest request = getValidIdRequest(ids.get(0));
        Integer projectId = ids.get(2);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(baseUrl + "/{userId}/projects/{projectId}/move/after", userId, projectId)
                .then()
                .statusCode(OK.value());

        List<Project> projects = projectRepository.findAll();
        assertThat(projects, hasSize(4));
        assertThat(projects, hasItem(allOf(
                hasProperty("name", is("Project 1")),
                hasProperty("generalOrder", is(1))
        )));
        assertThat(projects, hasItem(allOf(
                hasProperty("name", is("Project 2")),
                hasProperty("generalOrder", is(3))
        )));
        assertThat(projects, hasItem(allOf(
                hasProperty("name", is("Project 3")),
                hasProperty("generalOrder", is(2))
        )));
    }

    @Test
    void shouldRespondWith401ToMoveProjectAsFirstChildIfUserUnauthorized() {
        given()
                .log()
                .uri()
                .when()
                .put(baseUrl + "/{userId}/projects/{projectId}/move/asChild", 1, 1)
                .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldMoveProjectAsFirstChild() {
        List<Integer> ids = add3ProjectsWithParentInOrderAndReturnListOfIds();
        IdRequest request = getValidIdRequest(
                projectRepository.findById(ids.get(0)).get().getParent().getId()
        );
        Integer projectId = ids.get(2);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(baseUrl + "/{userId}/projects/{projectId}/move/asChild", userId, projectId)
                .then()
                .statusCode(OK.value());

        List<Project> projects = projectRepository.findAll();
        assertThat(projects, hasSize(4));
        assertThat(projects, hasItem(allOf(
                hasProperty("name", is("Project 1")),
                hasProperty("generalOrder", is(2))
        )));
        assertThat(projects, hasItem(allOf(
                hasProperty("name", is("Project 2")),
                hasProperty("generalOrder", is(3))
        )));
        assertThat(projects, hasItem(allOf(
                hasProperty("name", is("Project 3")),
                hasProperty("generalOrder", is(1))
        )));
    }

    private IdRequest getValidIdRequest(Integer id) {
        IdRequest request = new IdRequest();
        request.setId(id);
        return request;
    }

    @Test
    void shouldRespondWith401ToMoveProjectAsFirstIfUserUnauthorized() {
        given()
                .log()
                .uri()
                .when()
                .put(baseUrl + "/{userId}/projects/{projectId}/move/asFirst", 1, 1)
                .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldMoveProjectAsFirst() {
        List<Integer> ids = add3ProjectsInOrderAndReturnListOfIds();

        Integer projectId = ids.get(2);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .when()
                .put(baseUrl + "/{userId}/projects/{projectId}/move/asFirst", userId, projectId)
                .then()
                .statusCode(OK.value());

        List<Project> projects = projectRepository.findAll();
        assertThat(projects, hasSize(3));
        assertThat(projects, hasItem(allOf(
                hasProperty("name", is("Project 1")),
                hasProperty("generalOrder", is(2))
        )));
        assertThat(projects, hasItem(allOf(
                hasProperty("name", is("Project 2")),
                hasProperty("generalOrder", is(3))
        )));
        assertThat(projects, hasItem(allOf(
                hasProperty("name", is("Project 3")),
                hasProperty("generalOrder", is(1))
        )));
    }

    private Integer add3ProjectsInOrderAndReturnIdOfMiddleOne() {
        Project project1 = Project.builder()
                .owner(userRepository.getById(userId))
                .generalOrder(0)
                .name("Project 1")
                .build();
        Project project2 = Project.builder()
                .owner(userRepository.getById(userId))
                .generalOrder(1)
                .name("Project 2")
                .build();
        Project project3 = Project.builder()
                .owner(userRepository.getById(userId))
                .generalOrder(2)
                .name("Project 3")
                .build();
        return projectRepository.saveAll(List.of(project1, project2, project3)).stream()
                .filter(a -> a.getGeneralOrder().equals(1))
                .map(Project::getId)
                .findAny()
                .orElse(-1);
    }

    private List<Integer> add3ProjectsInOrderAndReturnListOfIds() {
        Project project1 = Project.builder()
                .owner(userRepository.getById(userId))
                .generalOrder(1)
                .name("Project 1")
                .build();
        Project project2 = Project.builder()
                .owner(userRepository.getById(userId))
                .generalOrder(2)
                .name("Project 2")
                .build();
        Project project3 = Project.builder()
                .owner(userRepository.getById(userId))
                .generalOrder(3)
                .name("Project 3")
                .build();
        return projectRepository.saveAll(List.of(project1, project2, project3)).stream()
                .sorted(Comparator.comparingInt(Project::getGeneralOrder))
                .map(Project::getId)
                .collect(Collectors.toList());
    }

    private List<Integer> add3ProjectsWithParentInOrderAndReturnListOfIds() {
        Project parent = Project.builder()
                .owner(userRepository.getById(userId))
                .generalOrder(1)
                .name("Parent")
                .build();
        parent = projectRepository.save(parent);
        Project project1 = Project.builder()
                .owner(userRepository.getById(userId))
                .generalOrder(1)
                .parent(parent)
                .name("Project 1")
                .build();
        Project project2 = Project.builder()
                .owner(userRepository.getById(userId))
                .generalOrder(2)
                .parent(parent)
                .name("Project 2")
                .build();
        Project project3 = Project.builder()
                .owner(userRepository.getById(userId))
                .generalOrder(3)
                .parent(parent)
                .name("Project 3")
                .build();
        return projectRepository.saveAll(List.of(project1, project2, project3)).stream()
                .sorted(Comparator.comparingInt(Project::getGeneralOrder))
                .map(Project::getId)
                .collect(Collectors.toList());
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
}
