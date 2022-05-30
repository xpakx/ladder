package io.github.xpakx.ladder.label;

import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.common.dto.BooleanRequest;
import io.github.xpakx.ladder.label.dto.LabelRequest;
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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LabelControllerTest {

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
    TaskRepository taskRepository;
    @Autowired
    LabelRepository labelRepository;

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
        labelRepository.deleteAll();
        taskRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String tokenFor(String username) {
        return jwtTokenUtil.generateToken(userService.loadUserToLogin(username));
    }

    private Integer addLabelAndReturnId() {
        Label label = Label.builder()
                .owner(userRepository.getById(userId))
                .name("Test Label")
                .build();
        return labelRepository.save(label).getId();
    }

    @Test
    void shouldRespondWith401ToAddLabelIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .post(baseUrl + "/{userId}/labels", 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    private LabelRequest getValidAddLabelRequest() {
        LabelRequest request = new LabelRequest();
        request.setName("Added Label");
        return request;
    }

    @Test
    void shouldAddLabel() {
        LabelRequest request = getValidAddLabelRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/{userId}/labels", userId)
        .then()
                .statusCode(CREATED.value())
                .body("name", equalTo(request.getName()));
    }

    @Test
    void shouldRespondWith401ToUpdateLabelIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .put(baseUrl + "/{userId}/labels/{labelId}", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToUpdateLabelIfLabelNotFound() {
        LabelRequest request = getValidAddLabelRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/labels/{labelId}", userId, 1)
        .then()
                .statusCode(NOT_FOUND.value());
    }

    @Test
    void shouldUpdateLabel() {
        Integer labelId = addLabelAndReturnId();
        LabelRequest request = getValidAddLabelRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/labels/{labelId}", userId, labelId)
        .then()
                .statusCode(OK.value())
                .body("name", equalTo(request.getName()));
    }

    @Test
    void shouldRespondWith401ToDeleteLabelRequestIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .delete(baseUrl + "/{userId}/labels/{labelId}", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldDeleteLabel() {
        Integer labelId = addLabelAndReturnId();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .delete(baseUrl + "/{userId}/labels/{labelId}", userId, labelId)
        .then()
                .statusCode(OK.value());
    }

    @Test
    void shouldRespondWith401ToUpdateLabelFavoriteStatusIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .put(baseUrl + "/{userId}/labels/{labelId}/favorite", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToUpdateLabelFavoriteStatusIfProjectNotFound() {
        BooleanRequest request = getTrueBooleanRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/labels/{labelId}/favorite", userId, 1)
        .then()
                .statusCode(NOT_FOUND.value());
    }

    private BooleanRequest getTrueBooleanRequest() {
        BooleanRequest request = new BooleanRequest();
        request.setFlag(true);
        return request;
    }

    private Integer addNonFavoriteLabel() {
        Label label = Label.builder()
                .owner(userRepository.getById(userId))
                .name("Test Label")
                .favorite(false)
                .build();
        label = labelRepository.save(label);
        return label.getId();
    }

    @Test
    void shouldUpdateProjectFavoriteStatus() {
        Integer labelId = addNonFavoriteLabel();
        BooleanRequest request = getTrueBooleanRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/labels/{labelId}/favorite", userId, labelId)
        .then()
                .statusCode(OK.value())
                .body("favorite", equalTo(true));
    }
}