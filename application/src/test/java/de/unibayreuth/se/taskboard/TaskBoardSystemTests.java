package de.unibayreuth.se.taskboard;

import de.unibayreuth.se.taskboard.api.dtos.TaskDto;
import de.unibayreuth.se.taskboard.api.dtos.UserDto;
import de.unibayreuth.se.taskboard.api.mapper.TaskDtoMapper;
import de.unibayreuth.se.taskboard.business.domain.Task;
import de.unibayreuth.se.taskboard.business.domain.User;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.equalTo;


public class TaskBoardSystemTests extends AbstractSystemTest {

    @Autowired
    private TaskDtoMapper taskDtoMapper;

    @Test
    void getAllCreatedTasks() {
        List<Task> createdTasks = TestFixtures.createTasks(taskService);

        List<Task> retrievedTasks = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/tasks")
                .then()
                .statusCode(200)
                .body(".", hasSize(createdTasks.size()))
                .and()
                .extract().jsonPath().getList("$", TaskDto.class)
                .stream()
                .map(taskDtoMapper::toBusiness)
                .toList();

        assertThat(retrievedTasks)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("createdAt", "updatedAt") // prevent issues due to differing timestamps after conversions
                .containsExactlyInAnyOrderElementsOf(createdTasks);
    }

    @Test
    void createAndDeleteTask() {
        Task createdTask = taskService.create(
                TestFixtures.getTasks().get(0)
        );

        when()
                .get("/api/tasks/{id}", createdTask.getId())
                .then()
                .statusCode(200);

        when()
                .delete("/api/tasks/{id}", createdTask.getId())
                .then()
                .statusCode(200);

        when()
                .get("/api/tasks/{id}", createdTask.getId())
                .then()
                .statusCode(400);

    }

    @Test
    void getAllUsers() {
        // Create users using TestFixtures
        List<User> createdUsers = TestFixtures.createUsers(userService);

        // Get all users and verify
        given()
            .contentType(ContentType.JSON)
            .when()
            .get("/api/users")
            .then()
            .statusCode(200)
            .body(".", hasSize(createdUsers.size()))
            .body("[0].name", org.hamcrest.Matchers.anyOf(
                org.hamcrest.Matchers.equalTo("Alice"),
                org.hamcrest.Matchers.equalTo("Bob"),
                org.hamcrest.Matchers.equalTo("Charlie")))
            .body("[1].name", org.hamcrest.Matchers.anyOf(
                org.hamcrest.Matchers.equalTo("Alice"),
                org.hamcrest.Matchers.equalTo("Bob"),
                org.hamcrest.Matchers.equalTo("Charlie")));
    }

    @Test
    void getUserById() {
        // Create a user using TestFixtures
        User user = userService.create(TestFixtures.getUsers().get(0));
        
        // Get the user by ID and verify
        given()
            .contentType(ContentType.JSON)
            .when()
            .get("/api/users/{id}", user.getId())
            .then()
            .statusCode(200)
            .body("name", equalTo("Alice"));

        // Try to get non-existent user
        given()
            .contentType(ContentType.JSON)
            .when()
            .get("/api/users/{id}", UUID.randomUUID())
            .then()
            .statusCode(400);
    }

    @Test
    void createUser() {
        // Get a test user from TestFixtures
        User testUser = TestFixtures.getUsers().get(0);

        // Create user and verify response
        UserDto createdUser = given()
            .contentType(ContentType.JSON)
            .body(testUser)
            .when()
            .post("/api/users")
            .then()
            .statusCode(200)
            .extract()
            .as(UserDto.class);

        assertThat(createdUser.getName()).isEqualTo(testUser.getName());
        assertThat(createdUser.getId()).isNotNull();

        // Verify user exists in the system
        given()
            .contentType(ContentType.JSON)
            .when()
            .get("/api/users/{id}", createdUser.getId())
            .then()
            .statusCode(200)
            .body("name", equalTo(testUser.getName()));
    }
}