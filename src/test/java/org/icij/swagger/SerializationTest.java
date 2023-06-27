package org.icij.swagger;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.core.util.ParameterProcessor;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.TestCase.assertEquals;

public class SerializationTest {
    @Test
    public void test_map_order() {
        Components components = new Components();
        Parameter parameter = ParameterProcessor.applyAnnotations(
                null,
                MyJavaBean.class,
                new ArrayList<>(),
                components,
                new String[0],
                new String[0],
                null);

        OpenAPI openAPI = new OpenAPI();
        openAPI.setComponents(components);
        assertEquals(Yaml.mapper().convertValue(openAPI, ObjectNode.class).toString(),
                        "{\"openapi\":\"3.0.1\",\"components\":{\"schemas\":{\"MyJavaBean\":{\"type\":\"object\"," +
                                "\"properties\":{\"a\":{\"type\":\"integer\",\"format\":\"int32\"}," +
                                "\"b\":{\"type\":\"boolean\"}}}}}}");
    }


    private static class MyJavaBean {
        private int a;
        private boolean b;

        public int getA() {
            return a;
        }

        public boolean isB() {
            return b;
        }
    }
}
