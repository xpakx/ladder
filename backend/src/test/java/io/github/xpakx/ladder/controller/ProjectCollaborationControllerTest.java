package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.entity.Collaboration;
import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.UserAccount;
import io.github.xpakx.ladder.repository.CollaborationRepository;
import io.github.xpakx.ladder.repository.ProjectRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
import io.github.xpakx.ladder.security.JwtTokenUtil;
import io.github.xpakx.ladder.service.UserService;
import io.restassured.http.ContentType;
import org.hamcrest.beans.HasProperty;
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
                .get(baseUrl + "/{userId}/projects/{projectId}/collaborators", 1, 1)
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
                .body("$", hasSize(2));
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
}
