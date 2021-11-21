package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.entity.Filter;
import io.github.xpakx.ladder.entity.Label;
import io.github.xpakx.ladder.entity.UserAccount;
import io.github.xpakx.ladder.entity.dto.BooleanRequest;
import io.github.xpakx.ladder.entity.dto.FilterRequest;
import io.github.xpakx.ladder.entity.dto.IdRequest;
import io.github.xpakx.ladder.entity.dto.LabelRequest;
import io.github.xpakx.ladder.repository.FilterRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
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
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.OK;

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

    @Test
    void shouldRespondWith401ToMoveFilterAsFirstIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .put(baseUrl + "/{userId}/filters/{filterId}/move/asFirst", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldMoveFilterAsFirst() {
        List<Integer> ids = add3FiltersInOrderAndReturnListOfIds();

        Integer filterId = ids.get(2);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
        .when()
                .put(baseUrl + "/{userId}/filters/{filterId}/move/asFirst", userId, filterId)
        .then()
                .statusCode(OK.value());

        List<Filter> filters = filterRepository.findAll();
        assertThat(filters, hasSize(3));
        assertThat(filters, hasItem(allOf(
                hasProperty("name", is("Filter 1")),
                hasProperty("generalOrder", is(2))
        )));
        assertThat(filters, hasItem(allOf(
                hasProperty("name", is("Filter 2")),
                hasProperty("generalOrder", is(3))
        )));
        assertThat(filters, hasItem(allOf(
                hasProperty("name", is("Filter 3")),
                hasProperty("generalOrder", is(1))
        )));
    }

    private List<Integer> add3FiltersInOrderAndReturnListOfIds() {
        Filter filter1 = Filter.builder()
                .owner(userRepository.getById(userId))
                .generalOrder(1)
                .name("Filter 1")
                .build();
        Filter filter2 = Filter.builder()
                .owner(userRepository.getById(userId))
                .generalOrder(2)
                .name("Filter 2")
                .build();
        Filter filter3 = Filter.builder()
                .owner(userRepository.getById(userId))
                .generalOrder(3)
                .name("Filter 3")
                .build();
        return filterRepository.saveAll(List.of(filter1, filter2, filter3)).stream()
                .sorted(Comparator.comparingInt(Filter::getGeneralOrder))
                .map(Filter::getId)
                .collect(Collectors.toList());
    }

    @Test
    void shouldRespondWith401ToMoveFilterAfterIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .put(baseUrl + "/{userId}/filters/{filterId}/move/after", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    private IdRequest getValidIdRequest(Integer id) {
        IdRequest request = new IdRequest();
        request.setId(id);
        return request;
    }

    @Test
    void shouldMoveFilterAfter() {
        List<Integer> ids = add3FiltersInOrderAndReturnListOfIds();
        IdRequest request = getValidIdRequest(ids.get(0));
        Integer filterId = ids.get(2);

        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .put(baseUrl + "/{userId}/filters/{filterId}/move/after", userId, filterId)
        .then()
                .statusCode(OK.value());


        List<Filter> filters = filterRepository.findAll();
        assertThat(filters, hasSize(3));
        assertThat(filters, hasItem(allOf(
                hasProperty("name", is("Filter 1")),
                hasProperty("generalOrder", is(1))
        )));
        assertThat(filters, hasItem(allOf(
                hasProperty("name", is("Filter 2")),
                hasProperty("generalOrder", is(3))
        )));
        assertThat(filters, hasItem(allOf(
                hasProperty("name", is("Filter 3")),
                hasProperty("generalOrder", is(2))
        )));
    }

    @Test
    void shouldRespondWith401ToAddFilterAfterIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .post(baseUrl + "/{userId}/filters/{filterId}/after", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldAddFilterAfter() {
        FilterRequest request = getValidAddFilterRequest();
        Integer filterId = add3FiltersInOrderAndReturnListOfIds().get(1);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/{userId}/filters/{filterId}/after", userId, filterId)
        .then()
                .statusCode(CREATED.value())
                .body("name", equalTo(request.getName()))
                .body("searchString", equalTo(request.getSearchString()));

        List<Filter> filters = filterRepository.findAll();
        assertThat(filters, hasSize(4));
        assertThat(filters, hasItem(allOf(
                hasProperty("name", is("Added Filter")),
                hasProperty("generalOrder", is(3))
        )));
        assertThat(filters, hasItem(allOf(
                hasProperty("name", is("Filter 1")),
                hasProperty("generalOrder", is(1))
        )));
        assertThat(filters, hasItem(allOf(
                hasProperty("name", is("Filter 2")),
                hasProperty("generalOrder", is(2))
        )));
        assertThat(filters, hasItem(allOf(
                hasProperty("name", is("Filter 3")),
                hasProperty("generalOrder", is(4))
        )));
    }

    @Test
    void shouldRespondWith401ToAddFilterBeforeIfUserUnauthorized() {
        given()
                .log()
                .uri()
        .when()
                .post(baseUrl + "/{userId}/filters/{filterId}/before", 1, 1)
        .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldAddFilterBefore() {
        FilterRequest request = getValidAddFilterRequest();
        Integer filterId = add3FiltersInOrderAndReturnListOfIds().get(1);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post(baseUrl + "/{userId}/filters/{filterId}/before", userId, filterId)
        .then()
                .statusCode(CREATED.value())
                .body("name", equalTo(request.getName()))
                .body("color", equalTo(request.getColor()))
                .body("favorite", equalTo(false));

        List<Filter> filters = filterRepository.findAll();
        assertThat(filters, hasSize(4));
        assertThat(filters, hasItem(allOf(
                hasProperty("name", is("Added Filter")),
                hasProperty("generalOrder", is(2))
        )));
        assertThat(filters, hasItem(allOf(
                hasProperty("name", is("Filter 1")),
                hasProperty("generalOrder", is(1))
        )));
        assertThat(filters, hasItem(allOf(
                hasProperty("name", is("Filter 2")),
                hasProperty("generalOrder", is(3))
        )));
        assertThat(filters, hasItem(allOf(
                hasProperty("name", is("Filter 3")),
                hasProperty("generalOrder", is(4))
        )));
    }
}