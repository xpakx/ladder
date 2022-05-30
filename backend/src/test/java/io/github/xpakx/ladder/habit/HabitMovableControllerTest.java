package io.github.xpakx.ladder.habit;

import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.habit.dto.HabitRequest;
import io.github.xpakx.ladder.common.dto.IdRequest;
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

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.CREATED;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HabitMovableControllerTest {
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

    private HabitRequest getValidAddHabitRequest() {
        HabitRequest request = new HabitRequest();
        request.setTitle("Added Habit");
        return request;
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
    void shouldRespondWith401ToAddHabitAfterIfUserUnauthorized() {
        given()
                .log()
                .uri()
                .when()
                .post(baseUrl + "/{userId}/habits/{habitId}/after", 1, 1)
                .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldAddHabitAfter() {
        HabitRequest request = getValidAddHabitRequest();
        Integer habitId = add3HabitsInOrderAndReturnListOfIds().get(1);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(baseUrl + "/{userId}/habits/{habitId}/after", userId, habitId)
                .then()
                .statusCode(CREATED.value())
                .body("title", equalTo(request.getTitle()))
                .body("description", equalTo(request.getDescription()));

        List<Habit> habits = habitRepository.findAll();
        assertThat(habits, hasSize(4));
        assertThat(habits, hasItem(allOf(
                hasProperty("title", is("Added Habit")),
                hasProperty("generalOrder", is(3))
        )));
        assertThat(habits, hasItem(allOf(
                hasProperty("title", is("Habit 1")),
                hasProperty("generalOrder", is(1))
        )));
        assertThat(habits, hasItem(allOf(
                hasProperty("title", is("Habit 2")),
                hasProperty("generalOrder", is(2))
        )));
        assertThat(habits, hasItem(allOf(
                hasProperty("title", is("Habit 3")),
                hasProperty("generalOrder", is(4))
        )));
    }

    @Test
    void shouldRespondWith401ToAddHabitBeforeIfUserUnauthorized() {
        given()
                .log()
                .uri()
                .when()
                .post(baseUrl + "/{userId}/habits/{habitId}/before", 1, 1)
                .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldAddHabitBefore() {
        HabitRequest request = getValidAddHabitRequest();
        Integer habitId = add3HabitsInOrderAndReturnListOfIds().get(1);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(baseUrl + "/{userId}/habits/{habitId}/before", userId, habitId)
                .then()
                .statusCode(CREATED.value())
                .body("title", equalTo(request.getTitle()))
                .body("description", equalTo(request.getDescription()));

        List<Habit> habits = habitRepository.findAll();
        assertThat(habits, hasSize(4));
        assertThat(habits, hasItem(allOf(
                hasProperty("title", is("Added Habit")),
                hasProperty("generalOrder", is(2))
        )));
        assertThat(habits, hasItem(allOf(
                hasProperty("title", is("Habit 1")),
                hasProperty("generalOrder", is(1))
        )));
        assertThat(habits, hasItem(allOf(
                hasProperty("title", is("Habit 2")),
                hasProperty("generalOrder", is(3))
        )));
        assertThat(habits, hasItem(allOf(
                hasProperty("title", is("Habit 3")),
                hasProperty("generalOrder", is(4))
        )));
    }

    @Test
    void shouldRespondWith401ToDuplicateHabitRequestIfUserUnauthorized() {
        given()
                .log()
                .uri()
                .when()
                .post(baseUrl + "/{userId}/habits/{habitId}/duplicate", 1, 1)
                .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToDuplicateHabitRequestIfHabitNotFound() {
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .when()
                .post(baseUrl + "/{userId}/habits/{habitId}/duplicate", userId, 1)
                .then()
                .statusCode(NOT_FOUND.value());
    }

    @Test
    void shouldDuplicateHabit() {
        Integer habitId = addHabitAndReturnId();
        int habits = habitRepository.findAll().size();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .when()
                .post(baseUrl + "/{userId}/habits/{habitId}/duplicate", userId, habitId)
                .then()
                .statusCode(CREATED.value());
        assertEquals(2*habits, habitRepository.findAll().size());
    }
}
