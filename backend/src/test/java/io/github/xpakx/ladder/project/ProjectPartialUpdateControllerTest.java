package io.github.xpakx.ladder.project;

import io.github.xpakx.ladder.task.Task;
import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.common.dto.BooleanRequest;
import io.github.xpakx.ladder.common.dto.IdRequest;
import io.github.xpakx.ladder.common.dto.NameRequest;
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
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProjectPartialUpdateControllerTest {
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
                .put(baseUrl + "/{userId}/projects/{projectId}/parent", userId, 403)
                .then()
                .statusCode(NOT_FOUND.value());
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
    void shouldRespondWith401ToUpdateProjectCollapseStatusIfUserUnauthorized() {
        given()
                .log()
                .uri()
                .when()
                .put(baseUrl + "/{userId}/projects/{projectId}/collapse", 1, 1)
                .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToUpdateProjectCollapseStatusIfProjectNotFound() {
        BooleanRequest request = getTrueBooleanRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(baseUrl + "/{userId}/projects/{projectId}/collapse", userId, 1)
                .then()
                .statusCode(NOT_FOUND.value());
    }

    @Test
    void shouldUpdateProjectCollapseStatus() {
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
                .put(baseUrl + "/{userId}/projects/{projectId}/collapse", userId, projectId)
                .then()
                .statusCode(OK.value())
                .body("collapsed", equalTo(true));
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

    private IdRequest getValidIdRequest(Integer id) {
        IdRequest request = new IdRequest();
        request.setId(id);
        return request;
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
