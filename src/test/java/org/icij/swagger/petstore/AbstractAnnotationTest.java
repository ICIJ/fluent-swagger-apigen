package org.icij.swagger.petstore;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.io.IOUtils;
import org.icij.swagger.FluentReader;
import org.icij.swagger.SerializationMatchers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public abstract class AbstractAnnotationTest {
    public String readIntoYaml(final Class<?> cls) {
        FluentReader reader = new FluentReader(new OpenAPI());
        OpenAPI openAPI = reader.read(cls);

        try {
            Yaml.mapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
            // parse JSON
            JsonNode jsonNodeTree = Yaml.mapper().readTree(Yaml.mapper().writeValueAsString(openAPI));
            // return it as YAML
            return Yaml.mapper().writeValueAsString(jsonNodeTree);
        } catch (Exception e) {
            return "Empty YAML";
        }
    }

    public void compareToYamlFile(final Class<?> cls, String source) throws IOException {
        final String file = source + cls.getSimpleName() + ".yaml";
        compareAsYaml(cls, getOpenAPIAsString(file));
    }

    public void compareAsYaml(final Class<?> cls, final String yaml) throws IOException {
        FluentReader reader = new FluentReader(new OpenAPI());
        OpenAPI openAPI = reader.read(cls);
        SerializationMatchers.assertEqualsToYaml(openAPI, yaml);
    }

    public void compareAsYaml(final String actualYaml, final String expectedYaml) throws IOException {
        SerializationMatchers.assertEqualsToYaml(Yaml.mapper().readValue(actualYaml, OpenAPI.class), expectedYaml);
    }

    public void compareAsJson(final String actualJson, final String expectedJson) throws IOException {
        SerializationMatchers.assertEqualsToJson(Yaml.mapper().readValue(actualJson, OpenAPI.class), expectedJson);
    }

    protected String getOpenAPIAsString(final String file) throws IOException {
        InputStream in = null;
        try {
            in = getClass().getClassLoader().getResourceAsStream(file);
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public void compareAsYamlOAS31(final Class<?> cls, final String yaml) throws IOException {
        FluentReader reader = new FluentReader(new SwaggerConfiguration()
                .openAPI(new OpenAPI())
                .openAPI31(true));
        OpenAPI openAPI = reader.read(cls);
        SerializationMatchers.assertEqualsToYaml31(openAPI, yaml);
    }
}