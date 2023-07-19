package org.icij.swagger;

import io.swagger.v3.core.util.Json31;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.models.OpenAPI;
import net.codestory.http.Context;
import net.codestory.http.annotations.Post;
import net.codestory.http.annotations.Prefix;
import org.junit.Test;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class AnnotationsTest {
    @Test
    public void test_operation_parameter() {
        assertThat(getPretty(ResourceWithRequestParameters.class)).isEqualTo(
                "{\n" +
                        "  \"/parameters/:bar\" : {\n" +
                        "    \"post\" : {\n" +
                        "      \"description\" : \"foo\",\n" +
                        "      \"operationId\" : \"setFoo\",\n" +
                        "      \"parameters\" : [ {\n" +
                        "        \"name\" : \"bar\",\n" +
                        "        \"in\" : \"path\",\n" +
                        "        \"required\" : true,\n" +
                        "        \"schema\" : {\n" +
                        "          \"type\" : \"integer\",\n" +
                        "          \"format\" : \"int32\"\n" +
                        "        }\n" +
                        "      } ],\n" +
                        "      \"responses\" : {\n" +
                        "        \"default\" : {\n" +
                        "          \"description\" : \"default response\",\n" +
                        "          \"content\" : {\n" +
                        "            \"*/*\" : { }\n" +
                        "          }\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "}"
        );
    }

    @Test
    public void test_request_body_multipart_parameters() {
        assertThat(getPretty(MultipartParameters.class)).isEqualTo(
                "{\n" +
                        "  \"/multipart\" : {\n" +
                        "    \"post\" : {\n" +
                        "      \"description\" : \"foo\",\n" +
                        "      \"operationId\" : \"setFoo\",\n" +
                        "      \"requestBody\" : {\n" +
                        "        \"description\" : \"multipart form\",\n" +
                        "        \"content\" : {\n" +
                        "          \"multipart/form-data\" : {\n" +
                        "            \"schema\" : {\n" +
                        "              \"properties\" : {\n" +
                        "                \"baz\" : {\n" +
                        "                  \"type\" : \"string\"\n" +
                        "                },\n" +
                        "                \"qux\" : {\n" +
                        "                  \"type\" : \"integer\",\n" +
                        "                  \"format\" : \"int32\"\n" +
                        "                }\n" +
                        "              }\n" +
                        "            }\n" +
                        "          }\n" +
                        "        },\n" +
                        "        \"required\" : true\n" +
                        "      },\n" +
                        "      \"responses\" : {\n" +
                        "        \"default\" : {\n" +
                        "          \"description\" : \"default response\",\n" +
                        "          \"content\" : {\n" +
                        "            \"*/*\" : { }\n" +
                        "          }\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "}"
        );
    }

    @Prefix("/parameters")
    private static class ResourceWithRequestParameters {
        @Operation(description = "foo",
                parameters = {@Parameter(name = "bar", in = ParameterIn.PATH,schema = @Schema(implementation = Integer.class))}
        )
        @Post("/:bar")
        public void setFoo(Integer bar, Context context) {}
    }

    @Prefix("/multipart")
    private static class MultipartParameters {
        @Operation(description = "foo",
                requestBody = @RequestBody(description = "multipart form", required = true,
                        content = @Content(mediaType = "multipart/form-data",
                                schemaProperties = {
                                    @SchemaProperty(name = "baz", schema = @Schema(implementation = String.class)),
                                    @SchemaProperty(name = "qux", schema = @Schema(implementation = Integer.class))
                                }
                        )
                )
        )
        @Post
        public void setFoo(Context context) {}
    }

    private static String getPretty(Class<?> resource) {
        OpenAPI openAPI = new FluentReader().read(new HashSet<>() {{add(resource);}});
        return Json31.pretty(openAPI.getPaths());
    }
}
