package io.github.xpakx.ladder.habit;

import io.github.xpakx.ladder.project.Project;
import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.habit.dto.HabitRequest;
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

import java.util.HashSet;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HabitControllerTest {
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
    HabitRepository habitRepository;
    @Autowired
    HabitCompletionRepository habitCompletionRepository;
    @Autowired
    ProjectRepository projectRepository;

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
        habitCompletionRepository.deleteAll();
        habitRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String tokenFor(String username) {
        return jwtTokenUtil.generateToken(userService.loadUserToLogin(username));
    }

    private Integer addHabitAndReturnId() {
        Habit habit = Habit.builder()
                .owner(userRepository.getById(userId))
                .title("Test Habit")
                .generalOrder(0)
                .build();
        return habitRepository.save(habit).getId();
    }

    private Integer addProjectAndReturnId() {
        Project project = Project.builder()
                .owner(userRepository.getById(userId))
                .name("Test Project")
                .build();
        return projectRepository.save(project).getId();
    }

    @Test
    void shouldRespondWith401ToAddHabitIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .post(baseUrl + "/{userId}/projects/{projectId}/habits", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    private HabitRequest getValidAddHabitRequest() {
        HabitRequest request = new HabitRequest();
        request.setTitle("Added Habit");
        return request;
    }

    @Test
    void shouldAddHabit() {
        HabitRequest request = getValidAddHabitRequest();
        Integer projectId = addProjectAndReturnId();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/{userId}/projects/{projectId}/habits", userId, projectId)
        .then()
                .statusCode(CREATED.value())
                .body("title", equalTo(request.getTitle()));
    }

    @Test
    void shouldRespondWith401ToUpdateHabitIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .put(baseUrl + "/{userId}/habits/{habitId}", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToUpdateHabitIfLabelNotFound() {
        HabitRequest request = getValidAddHabitRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/habits/{habitId}", userId, 1)
        .then()
                .statusCode(NOT_FOUND.value());
    }

    @Test
    void shouldUpdateHabit() {
        Integer habitId = addHabitAndReturnId();
        HabitRequest request = getValidAddHabitRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/habits/{habitId}", userId, habitId)
        .then()
                .statusCode(OK.value())
                .body("title", equalTo(request.getTitle()));
    }

    @Test
    void shouldRespondWith401ToDeleteHabitRequestIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .delete(baseUrl + "/{userId}/habits/{habitId}", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldDeleteHabit() {
        Integer habitId = addHabitAndReturnId();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .delete(baseUrl + "/{userId}/habits/{habitId}", userId, habitId)
        .then()
                .statusCode(OK.value());
    }

    @Test
    void shouldRespondWith401ToGetHabitDetailsIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .get(baseUrl + "/{userId}/habits/{habitId}", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToGetHabitDetailsIfTaskNotFound() {
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .get(baseUrl + "/{userId}/habits/{habitId}", userId, 1)
        .then()
                .statusCode(NOT_FOUND.value());
    }

    @Test
    void shouldRespondWithHabitDetails() {
        Integer habitId = addHabitAndReturnId();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .get(baseUrl + "/{userId}/habits/{habitId}", userId, habitId)
        .then()
                .statusCode(OK.value())
                .body("id", equalTo(habitId))
                .body("title", equalTo("Test Habit"));
    }
}