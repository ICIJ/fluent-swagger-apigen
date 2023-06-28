package org.icij.swagger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.swagger.v3.core.util.ObjectMapperFactory;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.Test;

import static org.icij.swagger.ClassUtils.findAllClassesUsingClassLoader;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class AcceptanceTest {
    @Test
    public void test_populate_openapi() throws Exception {
        ObjectMapper mapper = createMapper();
        JsonNode expectedValue = mapper.readTree(getClass().getClassLoader().getResource("petstore/FullPetResource.json"));
        final FluentReader reader = new FluentReader();
        final OpenAPI openAPI = reader.read(findAllClassesUsingClassLoader("org.icij.swagger.petstore"));
        assertNotNull(openAPI);
        assertEquals(mapper.valueToTree(expectedValue).toString(), mapper.valueToTree(openAPI).toString());
    }

    private static ObjectMapper createMapper() {
        JsonFactory factory = new JsonFactory() {
            @Override
            public boolean requiresPropertyOrdering() {
                return true;
            }
        };
        return ObjectMapperFactory.create(factory, true);
    }
}
