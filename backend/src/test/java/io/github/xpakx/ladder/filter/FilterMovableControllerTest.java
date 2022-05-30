package io.github.xpakx.ladder.filter;

import io.github.xpakx.ladder.filter.Filter;
import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.filter.dto.FilterRequest;
import io.github.xpakx.ladder.common.dto.IdRequest;
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

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FilterMovableControllerTest {
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

    private FilterRequest getValidAddFilterRequest() {
        FilterRequest request = new FilterRequest();
        request.setName("Added Filter");
        request.setSearchString("test");
        return request;
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
