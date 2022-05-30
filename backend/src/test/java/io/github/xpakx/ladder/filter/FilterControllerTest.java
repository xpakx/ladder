package io.github.xpakx.ladder.filter;

import io.github.xpakx.ladder.filter.Filter;
import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.common.dto.BooleanRequest;
import io.github.xpakx.ladder.filter.dto.FilterRequest;
import io.github.xpakx.ladder.filter.FilterRepository;
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
class FilterControllerTest {
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
    FilterRepository filterRepository;

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
        filterRepository.deleteAll();
        taskRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String tokenFor(String username) {
        return jwtTokenUtil.generateToken(userService.loadUserToLogin(username));
    }

    private Integer addFilterAndReturnId() {
        Filter filter = Filter.builder()
                .owner(userRepository.getById(userId))
                .name("Test Label")
                .searchString("p1 test")
                .build();
        return filterRepository.save(filter).getId();
    }

    @Test
    void shouldRespondWith401ToAddFilterIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .post(baseUrl + "/{userId}/filters", 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    private FilterRequest getValidAddFilterRequest() {
        FilterRequest request = new FilterRequest();
        request.setName("Added Filter");
        request.setSearchString("test");
        return request;
    }

    @Test
    void shouldAddFilter() {
        FilterRequest request = getValidAddFilterRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/{userId}/filters", userId)
        .then()
                .statusCode(CREATED.value())
                .body("name", equalTo(request.getName()))
                .body("searchString", equalTo(request.getSearchString()));
    }

    @Test
    void shouldRespondWith401ToUpdateFilterIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .put(baseUrl + "/{userId}/filters/{filterId}", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToUpdateFilterIfFilterNotFound() {
        FilterRequest request = getValidAddFilterRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/filters/{filterId}", userId, 1)
        .then()
                .statusCode(NOT_FOUND.value());
    }

    @Test
    void shouldUpdateLabel() {
        Integer filterId = addFilterAndReturnId();
        FilterRequest request = getValidAddFilterRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/filters/{filterId}", userId, filterId)
        .then()
                .statusCode(OK.value())
                .body("name", equalTo(request.getName()))
                .body("searchString", equalTo(request.getSearchString()));
    }

    @Test
    void shouldRespondWith401ToDeleteFilterRequestIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .delete(baseUrl + "/{userId}/filters/{filterId}", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldDeleteFilter() {
        Integer filterId = addFilterAndReturnId();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
        .when()
                .delete(baseUrl + "/{userId}/filters/{filterId}", userId, filterId)
        .then()
                .statusCode(OK.value());
    }

    @Test
    void shouldRespondWith401ToUpdateFilterFavoriteStatusIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .put(baseUrl + "/{userId}/filters/{filterId}/favorite", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldRespondWith404ToUpdateFilterFavoriteStatusIfFilterNotFound() {
        BooleanRequest request = getTrueBooleanRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/filters/{filterId}/favorite", userId, 1)
        .then()
                .statusCode(NOT_FOUND.value());
    }

    private BooleanRequest getTrueBooleanRequest() {
        BooleanRequest request = new BooleanRequest();
        request.setFlag(true);
        return request;
    }

    private Integer addNonFavoriteFilter() {
        Filter filter = Filter.builder()
                .owner(userRepository.getById(userId))
                .name("Test Label")
                .favorite(false)
                .build();
        filter = filterRepository.save(filter);
        return filter.getId();
    }

    @Test
    void shouldUpdateFilterFavoriteStatus() {
        Integer filterId = addNonFavoriteFilter();
        BooleanRequest request = getTrueBooleanRequest();
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/filters/{filterId}/favorite", userId, filterId)
        .then()
                .statusCode(OK.value())
                .body("favorite", equalTo(true));
    }
}