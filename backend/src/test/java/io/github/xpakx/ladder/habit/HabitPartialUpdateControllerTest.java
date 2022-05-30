package io.github.xpakx.ladder.habit;

import io.github.xpakx.ladder.project.Project;
import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.common.dto.BooleanRequest;
import io.github.xpakx.ladder.common.dto.IdRequest;
import io.github.xpakx.ladder.common.dto.PriorityRequest;
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
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HabitPartialUpdateControllerTest {
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

    private IdRequest getValidIdRequest(Integer id) {
        IdRequest request = new IdRequest();
        request.setId(id);
        return request;
    }

    @Test
    void shouldRespondWith401ToUpdateHabitPriorityIfUserUnauthorized() {
        given()
                .log()
                .uri()
                .when()
                .put(baseUrl + "/{userId}/habits/{habitId}/priority", 1, 1)
                .then()
                .statusCode(UNAUTHORIZED.value());
    }

    private PriorityRequest getValidPriorityRequest() {
        PriorityRequest request = new PriorityRequest();
        request.setPriority(3);
        return request;
    }

    @Test
    void shouldRespondWith404ToUpdateHabitPriorityIfTaskNotFound() {
        PriorityRequest request = getValidPriorityRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(baseUrl + "/{userId}/habits/{habitId}/priority", userId, 1)
                .then()
                .statusCode(NOT_FOUND.value());
    }

    @Test
    void shouldUpdateHabitPriority() {
        Integer habitId = addHabitAndReturnId();
        PriorityRequest request = getValidPriorityRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(baseUrl + "/{userId}/habits/{habitId}/priority", userId, habitId)
                .then()
                .statusCode(OK.value())
                .body("priority", equalTo(request.getPriority()));
    }

    @Test
    void shouldRespondWith401ToCompleteHabitIfUserUnauthorized() {
        given()
                .log()
                .uri()
                .when()
                .put(baseUrl + "/{userId}/habits/{habitId}/complete", 1, 1)
                .then()
                .statusCode(UNAUTHORIZED.value());
    }

    private BooleanRequest getBooleanRequest(boolean value) {
        BooleanRequest request = new BooleanRequest();
        request.setFlag(value);
        return request;
    }

    @Test
    void shouldRespondWith404ToCompleteHabitIfTaskNotFound() {
        BooleanRequest request = getBooleanRequest(true);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(baseUrl + "/{userId}/habits/{habitId}/complete", userId, 1)
                .then()
                .statusCode(NOT_FOUND.value());
    }

    private Integer addPositiveHabitAndReturnId() {
        Habit habit = Habit.builder()
                .owner(userRepository.getById(userId))
                .title("Test Habit")
                .allowPositive(true)
                .allowNegative(false)
                .build();
        return habitRepository.save(habit).getId();
    }

    @Test
    void shouldCompleteHabit() {
        Integer habitId = addPositiveHabitAndReturnId();
        BooleanRequest request = getBooleanRequest(true);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(baseUrl + "/{userId}/habits/{habitId}/complete", userId, habitId)
                .then()
                .statusCode(OK.value());

        Integer completions = habitCompletionRepository.findAll().size();
        assertThat(completions, equalTo(1));
    }

    @Test
    void shouldNotCompletePositiveHabitIfRequestIsNegative() {
        Integer habitId = addPositiveHabitAndReturnId();
        BooleanRequest request = getBooleanRequest(false);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(baseUrl + "/{userId}/habits/{habitId}/complete", userId, habitId)
                .then()
                .statusCode(BAD_REQUEST.value());

        Integer completions = habitCompletionRepository.findAll().size();
        assertThat(completions, equalTo(0));
    }

    @Test
    void shouldRespondWith401ToUpdateHabitProjectUnauthorized() {
        given()
                .log()
                .uri()
                .when()
                .put(baseUrl + "/{userId}/habits/{habitId}/project", 1, 1)
                .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToUpdateHabitProjectIfTaskNotFound() {
        IdRequest request = getValidIdRequest(5);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(baseUrl + "/{userId}/habits/{habitId}/project", userId, 1)
                .then()
                .statusCode(NOT_FOUND.value());
    }

    @Test
    void shouldUpdateHabitProject() {
        Integer habitId = addHabitAndReturnId();
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
                .put(baseUrl + "/{userId}/habits/{habitId}/project", userId, habitId)
                .then()
                .statusCode(OK.value());

        Optional<Habit> habit = habitRepository.findByIdAndOwnerId(habitId, userId);
        assertThat(habit.isPresent(), is(true));
        assertThat(habit.get().getProject().getId(), is(equalTo(request.getId())));
    }
}
