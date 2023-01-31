package dev.dashaun.service.calendar;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.util.StringUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Date;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.restdocs.cli.CliDocumentation.httpieRequest;
import static org.springframework.restdocs.http.HttpDocumentation.httpResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.snippet.Attributes.key;


@ExtendWith({RestDocumentationExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApplicationTests {

    @Test
    void contextLoads() {
    }


    private RequestSpecification documentationSpec;

    @LocalServerPort
    private int port;

    private static final UUID test = UUID.randomUUID();

    @BeforeAll
    public static void startContainer() {
        GenericContainer<?> redis = new GenericContainer<>(
                DockerImageName.parse("redis/redis-stack:latest"))
                .withExposedPorts(6379);
        redis.start();
        System.setProperty("spring.data.redis.host", redis.getHost());
        System.setProperty("spring.data.redis.port", redis.getMappedPort(6379).toString());
    }


    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        this.documentationSpec = new RequestSpecBuilder()
                .addFilter(documentationConfiguration(restDocumentation).snippets()
                        .withDefaults(httpieRequest(), httpResponse()))
                .addFilter(document("{method-name}",
                        preprocessRequest(modifyUris()
                                .scheme("https")
                                .host("dashaun.dev")
                                .removePort())))
//                        requestFields(attributes(key("title").value("Fields for Event creation")),
//                                fieldWithPath("name").description("The name of the event")
//                                        .attributes(key("constraints").value("Must not be null")),
//                                fieldWithPath("startDate").description("The event start date")
//                                        .attributes(key("constraints").value("Most not be null, must be a valid date")))))
                .build();
    }

    @Test
    @Order(1)
    public void create() {

        ConstrainedFields fields = new ConstrainedFields(Event.class);

        given(this.documentationSpec)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .port(this.port)
                .with()
                .body(new Event(test, "Example Event",
                        new Date(),
                        new Date(),
                        "https://dashaun.com",
                        "nothing special"))
                .post("/api/events/")
                .then()
                .assertThat().statusCode(is(200));
    }

    @Test
    @Order(2)
    public void read() {
        given(this.documentationSpec)
                .when()
                .port(this.port)
                .get("/api/events/" + test.toString())
                .then()
                .assertThat().statusCode(is(200));
    }

    @Test
    @Order(3)
    public void list() {
        given(this.documentationSpec)
                .when()
                .port(this.port)
                .get("/api/events/")
                .then()
                .assertThat().statusCode(is(200));
    }

    @Test
    @Order(4)
    public void delete() {
        given(this.documentationSpec)
                .when()
                .port(this.port)
                .delete("/api/events/" + test.toString())
                .then()
                .assertThat().statusCode(is(200));
    }

    private static class ConstrainedFields {

        private final ConstraintDescriptions constraintDescriptions;

        ConstrainedFields(Class<?> input) {
            this.constraintDescriptions = new ConstraintDescriptions(input);
        }

        private FieldDescriptor withPath(String path) {
            return fieldWithPath(path).attributes(key("constraints").value(StringUtils
                    .collectionToDelimitedString(this.constraintDescriptions
                            .descriptionsForProperty(path), ". ")));
        }
    }

}
