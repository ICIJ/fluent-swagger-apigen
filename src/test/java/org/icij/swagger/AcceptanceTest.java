package org.icij.swagger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.ObjectMapperFactory;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.Test;

import static org.icij.swagger.ClassUtils.findAllClassesUsingClassLoader;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class AcceptanceTest {
    @Test
    public void test_populate_openapi() throws Exception {
        ObjectMapper mapper = ObjectMapperFactory.createYaml31();
        JsonNode expectedValue = mapper.readTree(getClass().getClassLoader().getResource("petstore/FullPetResource.yaml"));
        final FluentReader reader = new FluentReader();
        final OpenAPI openAPI = reader.read(findAllClassesUsingClassLoader("org.icij.swagger.petstore"));

        assertNotNull(openAPI);
        assertEquals(mapper.valueToTree(openAPI).toString(), expectedValue.toString());
    }
}
