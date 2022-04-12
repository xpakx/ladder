package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.entity.Habit;
import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.UserAccount;
import io.github.xpakx.ladder.entity.dto.BooleanRequest;
import io.github.xpakx.ladder.entity.dto.HabitRequest;
import io.github.xpakx.ladder.entity.dto.IdRequest;
import io.github.xpakx.ladder.entity.dto.PriorityRequest;
import io.github.xpakx.ladder.repository.*;
import io.github.xpakx.ladder.security.JwtTokenUtil;
import io.github.xpakx.ladder.service.UserService;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.OK;

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
    void shouldRespondWith401ToMoveHabitAsFirstIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .put(baseUrl + "/{userId}/habits/{habitId}/move/asFirst", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldMoveHabitAsFirst() {
        List<Integer> ids = add3HabitsInOrderAndReturnListOfIds();

        Integer habitId = ids.get(2);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
        .when()
                .put(baseUrl + "/{userId}/habits/{habitId}/move/asFirst", userId, habitId)
        .then()
                .statusCode(OK.value());

        List<Habit> habits = habitRepository.findAll();
        assertThat(habits, hasSize(3));
        assertThat(habits, hasItem(allOf(
                hasProperty("title", is("Habit 1")),
                hasProperty("generalOrder", is(2))
        )));
        assertThat(habits, hasItem(allOf(
                hasProperty("title", is("Habit 2")),
                hasProperty("generalOrder", is(3))
        )));
        assertThat(habits, hasItem(allOf(
                hasProperty("title", is("Habit 3")),
                hasProperty("generalOrder", is(1))
        )));
    }

    private List<Integer> add3HabitsInOrderAndReturnListOfIds() {
        Habit label1 = Habit.builder()
                .owner(userRepository.getById(userId))
                .generalOrder(1)
                .title("Habit 1")
                .build();
        Habit label2 = Habit.builder()
                .owner(userRepository.getById(userId))
                .generalOrder(2)
                .title("Habit 2")
                .build();
        Habit label3 = Habit.builder()
                .owner(userRepository.getById(userId))
                .generalOrder(3)
                .title("Habit 3")
                .build();
        return habitRepository.saveAll(List.of(label1, label2, label3)).stream()
                .sorted(Comparator.comparingInt(Habit::getGeneralOrder))
                .map(Habit::getId)
                .collect(Collectors.toList());
    }

    @Test
    void shouldRespondWith401ToMoveHabitAfterIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .put(baseUrl + "/{userId}/habits/{habitId}/move/after", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    private IdRequest getValidIdRequest(Integer id) {
        IdRequest request = new IdRequest();
        request.setId(id);
        return request;
    }

    @Test
    void shouldMoveHabitAfter() {
        List<Integer> ids = add3HabitsInOrderAndReturnListOfIds();
        IdRequest request = getValidIdRequest(ids.get(0));
        Integer habitId = ids.get(2);

        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/habits/{habitId}/move/after", userId, habitId)
        .then()
                .statusCode(OK.value());


        List<Habit> labels = habitRepository.findAll();
        assertThat(labels, hasSize(3));
        assertThat(labels, hasItem(allOf(
                hasProperty("title", is("Habit 1")),
                hasProperty("generalOrder", is(1))
        )));
        assertThat(labels, hasItem(allOf(
                hasProperty("title", is("Habit 2")),
                hasProperty("generalOrder", is(3))
        )));
        assertThat(labels, hasItem(allOf(
                hasProperty("title", is("Habit 3")),
                hasProperty("generalOrder", is(2))
        )));
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
    void shouldUpdateTaskHabit() {
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