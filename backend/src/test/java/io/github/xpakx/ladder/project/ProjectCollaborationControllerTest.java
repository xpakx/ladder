package io.github.xpakx.ladder.project;

import io.github.xpakx.ladder.collaboration.Collaboration;
import io.github.xpakx.ladder.task.Task;
import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.collaboration.dto.CollaborationRequest;
import io.github.xpakx.ladder.collaboration.CollaborationRepository;
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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProjectCollaborationControllerTest {
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
    void shouldRespondWith401ToGetCollaboratorsIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .get(baseUrl + "/{userId}/projects/{projectId}/collaborators", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWithEmptyListToGetCollaboratorsIfProjectNotFound() {
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
        .when()
                .get(baseUrl + "/{userId}/projects/{projectId}/collaborators", userId, 1)
        .then()
                .statusCode(OK.value())
                .body("$", hasSize(0));
    }

    @Test
    void shouldRespondWithCollaborators() {
        Integer projectId = addProjectWith2CollaboratorsAndReturnId();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .get(baseUrl + "/{userId}/projects/{projectId}/collaborators", userId, projectId)
        .then()
                .log()
                .body()
                .statusCode(OK.value())
                .body("$", hasSize(2))
                .body("owner.username", hasItem(equalTo("user2")))
                .body("owner.username", hasItem(equalTo("user3")));
    }

    private Integer addProjectWith2CollaboratorsAndReturnId() {
        Project project = Project.builder()
                .owner(userRepository.getById(userId))
                .name("Test Project")
                .generalOrder(1)
                .build();
        Integer id = projectRepository.save(project).getId();
        UserAccount user2 = UserAccount.builder()
                .username("user2")
                .password("password")
                .roles(new HashSet<>())
                .build();
        user2 = userRepository.save(user2);
        UserAccount user3 = UserAccount.builder()
                .username("user3")
                .password("password")
                .roles(new HashSet<>())
                .build();
        user3 = userRepository.save(user3);

        Collaboration collaboration1 = Collaboration.builder()
                        .project(projectRepository.getById(id))
                        .owner(user2)
                        .accepted(true)
                        .editionAllowed(true)
                        .taskCompletionAllowed(true)
                        .build();
        Collaboration collaboration2 = Collaboration.builder()
                .project(projectRepository.getById(id))
                .owner(user3)
                .accepted(true)
                .editionAllowed(true)
                .taskCompletionAllowed(true)
                .build();
        collaborationRepository.saveAll(List.of(collaboration1, collaboration2));

        return id;
    }

    private Integer addProjectAndReturnId() {
        Project project = Project.builder()
                .owner(userRepository.getById(userId))
                .name("Test Project")
                .generalOrder(1)
                .build();
        return projectRepository.save(project).getId();
    }

    @Test
    void shouldRespondWith401ToAddCollaboratorIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .post(baseUrl + "/{userId}/projects/{projectId}/collaborators", userId, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToAddCollaboratorIfProjectNotFound() {
        CollaborationRequest request = getCollaborationRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/{userId}/projects/{projectId}/collaborators", userId, 1)
        .then()
                .statusCode(NOT_FOUND.value());
    }

    private CollaborationRequest getCollaborationRequest(String token, boolean completion, boolean edition) {
        CollaborationRequest request = new CollaborationRequest();
        request.setCollaborationToken(token);
        request.setCompletionAllowed(completion);
        request.setEditionAllowed(edition);
        return request;
    }
    private CollaborationRequest getCollaborationRequest() {
        return getCollaborationRequest("token", true, true);
    }

    @Test
    void shouldRespondWith404ToAddCollaboratorIfWrongToken() {
        CollaborationRequest request = getCollaborationRequest();
        Integer projectId = addProjectAndReturnId();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/{userId}/projects/{projectId}/collaborators", userId, projectId)
        .then()
                .statusCode(NOT_FOUND.value());
    }

    @Test
    void shouldAddCollaboration() {
        String token = addUserAndReturnToken();
        CollaborationRequest request = getCollaborationRequest(token, true, true);
        Integer projectId = addProjectAndReturnId();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/{userId}/projects/{projectId}/collaborators", userId, projectId)
        .then()
                .statusCode(CREATED.value())
                .body("taskCompletionAllowed", is(true))
                .body("editionAllowed", is(true))
                .body("owner.username", is("user2"));
    }

    private String addUserAndReturnToken() {
        UserAccount user = UserAccount.builder()
                .username("user2")
                .password("password")
                .collaborationToken("token")
                .roles(new HashSet<>())
                .build();
        return userRepository.save(user).getCollaborationToken();
    }

    @Test
    void shouldRespondWith401ToDeleteCollaboratorIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .delete(baseUrl + "/{userId}/projects/{projectId}/collaborators/{collaboratorId}", userId, 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToDeleteCollaboratorIfProjectNotFound() {
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
        .when()
                .delete(baseUrl + "/{userId}/projects/{projectId}/collaborators/{collaboratorId}", userId, 1, 1)
        .then()
                .statusCode(NOT_FOUND.value());
    }

    @Test
    void shouldDeleteCollaboration() {
        Integer collaboratorId = addUserAndReturnId();
        Integer projectId = addProjectWithCollaboratorAndReturnId(collaboratorId);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
        .when()
                .delete(baseUrl + "/{userId}/projects/{projectId}/collaborators/{collaboratorId}", userId, projectId, collaboratorId)
        .then()
                .statusCode(OK.value());
        Integer collaborationsInDb = collaborationRepository.findAll().size();
        Optional<Project> project = projectRepository.findById(projectId);
        assertThat(collaborationsInDb, is(equalTo(0)));
        assertThat(project.isPresent(), is(true));
        assertThat(project.get().isCollaborative(), is(false));
    }

    private Integer addUserAndReturnId() {
        UserAccount user = UserAccount.builder()
                .username("user2")
                .password("password")
                .collaborationToken("token")
                .roles(new HashSet<>())
                .build();
        return userRepository.save(user).getId();
    }

    private Integer addProjectWithCollaboratorAndReturnId(Integer userId) {
        Project project = Project.builder()
                .owner(userRepository.getById(this.userId))
                .name("Test Project")
                .generalOrder(1)
                .build();
        project = projectRepository.save(project);
        Collaboration collaboration = Collaboration.builder()
                .owner(userRepository.getById(userId))
                .project(project)
                .editionAllowed(true)
                .taskCompletionAllowed(true)
                .build();
        collaborationRepository.save(collaboration);
        project.setCollaborative(true);
        project.setCollaborators(List.of(collaboration));
        projectRepository.save(project);
        return project.getId();
    }

    @Test
    void shouldDeassignTasksWhileDeletingCollaboration() {
        Integer collaboratorId = addUserAndReturnId();
        Integer projectId = addProjectWithCollaboratorAndReturnId(collaboratorId);
        add2AssignedTasks(projectId, collaboratorId);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .when()
                .delete(baseUrl + "/{userId}/projects/{projectId}/collaborators/{collaboratorId}", userId, projectId, collaboratorId)
                .then()
                .statusCode(OK.value());
        List<Task> tasks = taskRepository.findAll();
        assertThat(tasks, everyItem(hasProperty("assigned", is(equalTo(null)))));
    }

    private void add2AssignedTasks(Integer projectId, Integer collaboratorId) {
        Task task1 = Task.builder()
                .project(projectRepository.getById(projectId))
                .owner(userRepository.getById(userId))
                .title("Task 1")
                .assigned(userRepository.getById(collaboratorId))
                .build();
        Task task2 = Task.builder()
                .project(projectRepository.getById(projectId))
                .owner(userRepository.getById(userId))
                .title("Task 2")
                .assigned(userRepository.getById(collaboratorId))
                .build();
        taskRepository.saveAll(List.of(task1, task2));
    }
}
