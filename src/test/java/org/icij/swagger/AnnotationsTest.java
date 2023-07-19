package org.icij.swagger;

import io.swagger.v3.core.util.Json31;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.models.OpenAPI;
import net.codestory.http.Context;
import net.codestory.http.annotations.Post;
import net.codestory.http.annotations.Prefix;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

import java.util.HashSet;

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

    @Prefix("/parameters")
    private static class ResourceWithRequestParameters {

        @Operation(description = "foo",
                parameters = {@Parameter(name = "bar", in = ParameterIn.PATH,schema = @Schema(implementation = Integer.class))}
        )
        @Post("/:bar")
        public void setFoo(Integer bar, Context context) {}
    }

    private static String getPretty(Class<?> resource) {
        OpenAPI openAPI = new FluentReader().read(new HashSet<>() {{add(resource);}});
        return Json31.pretty(openAPI.getPaths());
    }
}
