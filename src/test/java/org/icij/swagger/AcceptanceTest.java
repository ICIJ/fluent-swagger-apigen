package org.icij.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import org.icij.swagger.petstore.SerializationMatchers;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.icij.swagger.ClassUtils.findAllClassesUsingClassLoader;
import static org.junit.Assert.assertNotNull;


public class AcceptanceTest {
    @Test
    public void test_populate_openapi() throws Exception {
        final FluentReader reader = new FluentReader();
        final OpenAPI openAPI = reader.read(findAllClassesUsingClassLoader("org.icij.swagger.petstore"));
        assertNotNull(openAPI);
        SerializationMatchers.assertEqualsToYaml(openAPI, Files.readString(Paths.get(getClass().getClassLoader().getResource("petstore/FullPetResource.yaml").toURI())));
    }
}
