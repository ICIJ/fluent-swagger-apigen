package org.icij.swagger;

import io.swagger.v3.core.util.Json31;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
                """
                        {
                          "/parameters/{bar}" : {
                            "post" : {
                              "description" : "foo",
                              "operationId" : "setFoo",
                              "parameters" : [ {
                                "name" : "bar",
                                "in" : "path",
                                "required" : true,
                                "schema" : {
                                  "type" : "integer",
                                  "format" : "int32"
                                }
                              } ],
                              "requestBody" : {
                                "content" : {
                                  "*/*" : {
                                    "schema" : {
                                      "type" : "integer",
                                      "format" : "int32"
                                    }
                                  }
                                }
                              },
                              "responses" : {
                                "default" : {
                                  "description" : "default response",
                                  "content" : {
                                    "*/*" : { }
                                  }
                                }
                              }
                            }
                          }
                        }"""
        );
    }

    @Test
    public void test_operation_3parameters() {
        assertThat(getPretty(ResourceWith3Parameters.class)).isEqualTo(
                """
                        {
                          "/3parameters/{bar}/{baz}/{qux}" : {
                            "post" : {
                              "description" : "foo",
                              "operationId" : "setFooWithBarBazQux",
                              "parameters" : [ {
                                "name" : "bar",
                                "in" : "path",
                                "required" : true,
                                "schema" : {
                                  "type" : "integer",
                                  "format" : "int32"
                                }
                              }, {
                                "name" : "baz",
                                "in" : "path",
                                "required" : true,
                                "schema" : {
                                  "type" : "string"
                                }
                              }, {
                                "name" : "qux",
                                "in" : "path",
                                "required" : true,
                                "schema" : {
                                  "type" : "integer",
                                  "format" : "int32"
                                }
                              } ],
                              "requestBody" : {
                                "content" : {
                                  "*/*" : {
                                    "schema" : {
                                      "type" : "integer",
                                      "format" : "int32"
                                    }
                                  }
                                }
                              },
                              "responses" : {
                                "default" : {
                                  "description" : "default response",
                                  "content" : {
                                    "*/*" : { }
                                  }
                                }
                              }
                            }
                          }
                        }"""
        );
    }

    @Test
    public void test_request_body_multipart_parameters() {
        assertThat(getPretty(MultipartParameters.class)).isEqualTo(
                """
                        {
                          "/multipart" : {
                            "post" : {
                              "description" : "foo",
                              "operationId" : "setFoo",
                              "requestBody" : {
                                "description" : "multipart form",
                                "content" : {
                                  "multipart/form-data" : {
                                    "schema" : {
                                      "properties" : {
                                        "baz" : {
                                          "type" : "string"
                                        },
                                        "qux" : {
                                          "type" : "integer",
                                          "format" : "int32"
                                        }
                                      }
                                    }
                                  }
                                },
                                "required" : true
                              },
                              "responses" : {
                                "default" : {
                                  "description" : "default response",
                                  "content" : {
                                    "*/*" : { }
                                  }
                                }
                              }
                            }
                          }
                        }"""
        );
    }

    @Test
    public void test_request_with_query_parameters() {
        assertThat(getPretty(ResourceWithRequestQueryParameters.class)).isEqualTo("""
        {
          "/parameters/foo?bar={bar}" : {
            "post" : {
              "description" : "foo",
              "operationId" : "getFooWithBar",
              "parameters" : [ {
                "name" : "bar",
                "in" : "query",
                "description" : "description",
                "schema" : {
                  "type" : "string"
                }
              } ],
              "requestBody" : {
                "content" : {
                  "*/*" : {
                    "schema" : {
                      "type" : "string"
                    }
                  }
                }
              },
              "responses" : {
                "default" : {
                  "description" : "default response",
                  "content" : {
                    "*/*" : { }
                  }
                }
              }
            }
          }
        }""");
    }
    @Test
    public void test_request_with_custom_type_parameter() {
        assertThat(getPretty(ResourceWithCustomTypeParameter.class)).isEqualTo("""
        {
          "/foo/bar" : {
            "post" : {
              "description" : "bar",
              "operationId" : "postFooBar",
              "requestBody" : {
                "description" : "json bar",
                "content" : {
                  "application/json" : {
                    "schema" : {
                      "$ref" : "#/components/schemas/Bar"
                    }
                  }
                },
                "required" : true
              },
              "responses" : {
                "default" : {
                  "description" : "default response",
                  "content" : {
                    "*/*" : {
                      "schema" : {
                        "type" : "string"
                      }
                    }
                  }
                }
              }
            }
          }
        }""");
    }


    @Test
    public void test_info() {
        OpenAPI openAPI = new FluentReader().read(new HashSet<>() {{add(WithInfo.class);}});
        assertThat(Json31.pretty(openAPI)).isEqualTo("{\n" +
                "  \"openapi\" : \"3.0.1\",\n" +
                "  \"info\" : {\n" +
                "    \"title\" : \"this is the info\",\n" +
                "    \"version\" : \"1.0.1\"\n" +
                "  }\n" +
                "}");
    }

    @Prefix("/parameters")
    private static class ResourceWithRequestParameters {
        @Operation(description = "foo",
                parameters = {@Parameter(name = "bar", in = ParameterIn.PATH,schema = @Schema(implementation = Integer.class))}
        )
        @Post("/:bar")
        public void setFoo(Integer bar, Context context) {}
    }

    @Prefix("/3parameters")
    private static class ResourceWith3Parameters {
        @Operation(description = "foo",
                parameters = {
                    @Parameter(name = "bar", in = ParameterIn.PATH,schema = @Schema(implementation = Integer.class)),
                    @Parameter(name = "baz", in = ParameterIn.PATH,schema = @Schema(implementation = String.class)),
                    @Parameter(name = "qux", in = ParameterIn.PATH,schema = @Schema(implementation = Integer.class))
                }
        )
        @Post("/:bar/:baz/:qux")
        public void setFooWithBarBazQux(Integer bar, String baz, Integer qux) {}
    }

    @Prefix("/parameters")
    private static class ResourceWithRequestQueryParameters {
        @Operation(description = "foo",
                parameters = {
                        @Parameter(name = "bar",
                                description = """
                                description""", in = ParameterIn.QUERY, schema = @Schema(implementation = String.class))
                }
        )
        @Post("/foo?bar=:bar")
        public void getFooWithBar(String bar) {}
    }

    @Prefix("/foo")
    private static class ResourceWithCustomTypeParameter {
        @Operation(description = "bar",
                requestBody = @RequestBody(description = "json bar", required = true,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Bar.class))
                )
        )
        @ApiResponse(responseCode = "200")
        @Post("/bar")
        public String postFooBar(Bar bar) {return "OK";}
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

    @OpenAPIDefinition(info = @Info(title = "this is the info", version = "1.0.1"))
    private static class WithInfo { }

    private record Bar(String baz) { }

    private static String getPretty(Class<?> resource) {
        OpenAPI openAPI = new FluentReader().read(new HashSet<>() {{add(resource);}});
        return Json31.pretty(openAPI.getPaths());
    }
}
