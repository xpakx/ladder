package io.github.xpakx.ladder.label;

import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.common.dto.IdRequest;
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

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.CREATED;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LabelMovableControllerTest {

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

    private LabelRequest getValidAddLabelRequest() {
        LabelRequest request = new LabelRequest();
        request.setName("Added Label");
        return request;
    }

    @Test
    void shouldRespondWith401ToMoveLabelAsFirstIfUserUnauthorized() {
        given()
                .log()
                .uri()
                .when()
                .put(baseUrl + "/{userId}/labels/{labelId}/move/asFirst", 1, 1)
                .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldMoveLabelAsFirst() {
        List<Integer> ids = add3LabelsInOrderAndReturnListOfIds();

        Integer labelId = ids.get(2);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .when()
                .put(baseUrl + "/{userId}/labels/{labelId}/move/asFirst", userId, labelId)
                .then()
                .statusCode(OK.value());

        List<Label> labels = labelRepository.findAll();
        assertThat(labels, hasSize(3));
        assertThat(labels, hasItem(allOf(
                hasProperty("name", is("Label 1")),
                hasProperty("generalOrder", is(2))
        )));
        assertThat(labels, hasItem(allOf(
                hasProperty("name", is("Label 2")),
                hasProperty("generalOrder", is(3))
        )));
        assertThat(labels, hasItem(allOf(
                hasProperty("name", is("Label 3")),
                hasProperty("generalOrder", is(1))
        )));
    }

    private List<Integer> add3LabelsInOrderAndReturnListOfIds() {
        Label label1 = Label.builder()
                .owner(userRepository.getById(userId))
                .generalOrder(1)
                .name("Label 1")
                .build();
        Label label2 = Label.builder()
                .owner(userRepository.getById(userId))
                .generalOrder(2)
                .name("Label 2")
                .build();
        Label label3 = Label.builder()
                .owner(userRepository.getById(userId))
                .generalOrder(3)
                .name("Label 3")
                .build();
        return labelRepository.saveAll(List.of(label1, label2, label3)).stream()
                .sorted(Comparator.comparingInt(Label::getGeneralOrder))
                .map(Label::getId)
                .collect(Collectors.toList());
    }

    @Test
    void shouldRespondWith401ToMoveLabelAfterIfUserUnauthorized() {
        given()
                .log()
                .uri()
                .when()
                .put(baseUrl + "/{userId}/labels/{labelId}/move/after", 1, 1)
                .then()
                .statusCode(UNAUTHORIZED.value());
    }

    private IdRequest getValidIdRequest(Integer id) {
        IdRequest request = new IdRequest();
        request.setId(id);
        return request;
    }

    @Test
    void shouldMoveLabelAfter() {
        List<Integer> ids = add3LabelsInOrderAndReturnListOfIds();
        IdRequest request = getValidIdRequest(ids.get(0));
        Integer labelId = ids.get(2);

        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(baseUrl + "/{userId}/labels/{labelId}/move/after", userId, labelId)
                .then()
                .statusCode(OK.value());


        List<Label> labels = labelRepository.findAll();
        assertThat(labels, hasSize(3));
        assertThat(labels, hasItem(allOf(
                hasProperty("name", is("Label 1")),
                hasProperty("generalOrder", is(1))
        )));
        assertThat(labels, hasItem(allOf(
                hasProperty("name", is("Label 2")),
                hasProperty("generalOrder", is(3))
        )));
        assertThat(labels, hasItem(allOf(
                hasProperty("name", is("Label 3")),
                hasProperty("generalOrder", is(2))
        )));
    }

    @Test
    void shouldRespondWith401ToAddLabelAfterIfUserUnauthorized() {
        given()
                .log()
                .uri()
                .when()
                .post(baseUrl + "/{userId}/labels/{labelId}/after", 1, 1)
                .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldAddLabelAfter() {
        LabelRequest request = getValidAddLabelRequest();
        Integer labelId = add3LabelsInOrderAndReturnListOfIds().get(1);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(baseUrl + "/{userId}/labels/{labelId}/after", userId, labelId)
                .then()
                .statusCode(CREATED.value())
                .body("name", equalTo(request.getName()))
                .body("color", equalTo(request.getColor()))
                .body("favorite", equalTo(false));

        List<Label> labels = labelRepository.findAll();
        assertThat(labels, hasSize(4));
        assertThat(labels, hasItem(allOf(
                hasProperty("name", is("Added Label")),
                hasProperty("generalOrder", is(3))
        )));
        assertThat(labels, hasItem(allOf(
                hasProperty("name", is("Label 1")),
                hasProperty("generalOrder", is(1))
        )));
        assertThat(labels, hasItem(allOf(
                hasProperty("name", is("Label 2")),
                hasProperty("generalOrder", is(2))
        )));
        assertThat(labels, hasItem(allOf(
                hasProperty("name", is("Label 3")),
                hasProperty("generalOrder", is(4))
        )));
    }

    @Test
    void shouldRespondWith401ToAddLabelBeforeIfUserUnauthorized() {
        given()
                .log()
                .uri()
                .when()
                .post(baseUrl + "/{userId}/labels/{labelId}/before", 1, 1)
                .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldAddLabelBefore() {
        LabelRequest request = getValidAddLabelRequest();
        Integer labelId = add3LabelsInOrderAndReturnListOfIds().get(1);
        given()
                .log()
                .uri()
                .auth()
                .oauth2(tokenFor("user1"))
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(baseUrl + "/{userId}/labels/{labelId}/before", userId, labelId)
                .then()
                .statusCode(CREATED.value())
                .body("name", equalTo(request.getName()))
                .body("color", equalTo(request.getColor()))
                .body("favorite", equalTo(false));

        List<Label> labels = labelRepository.findAll();
        assertThat(labels, hasSize(4));
        assertThat(labels, hasItem(allOf(
                hasProperty("name", is("Added Label")),
                hasProperty("generalOrder", is(2))
        )));
        assertThat(labels, hasItem(allOf(
                hasProperty("name", is("Label 1")),
                hasProperty("generalOrder", is(1))
        )));
        assertThat(labels, hasItem(allOf(
                hasProperty("name", is("Label 2")),
                hasProperty("generalOrder", is(3))
        )));
        assertThat(labels, hasItem(allOf(
                hasProperty("name", is("Label 3")),
                hasProperty("generalOrder", is(4))
        )));
    }
}
