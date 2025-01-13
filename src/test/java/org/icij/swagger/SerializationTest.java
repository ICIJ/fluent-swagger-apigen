package org.icij.swagger;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.core.util.ParameterProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import net.codestory.http.annotations.Get;
import net.codestory.http.annotations.Prefix;
import net.codestory.http.payload.Payload;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.TestCase.assertEquals;
import static org.icij.swagger.Main.createObjectMapper;

public class SerializationTest {
    @Test
    public void test_java_bean_properties_order() {
        Components components = new Components();
        ParameterProcessor.applyAnnotations(
                null,
                MyJavaBean.class,
                new ArrayList<>(),
                components,
                new String[0],
                new String[0],
                null);

        OpenAPI openAPI = new OpenAPI();
        openAPI.setComponents(components);
        assertEquals(createObjectMapper(Main.Output.JSON).convertValue(openAPI, ObjectNode.class).toString(),
                        "{\"openapi\":\"3.0.1\",\"components\":{\"schemas\":{\"MyJavaBean\":{" +
                                "\"properties\":{\"a\":{\"type\":\"integer\",\"format\":\"int32\"}," +
                                "\"b\":{\"type\":\"boolean\"}}}}}}");
    }

    @Test
    public void test_payload_properties_order() {
        Components components = new Components();
        ParameterProcessor.applyAnnotations(
                null,
                Payload.class,
                new ArrayList<>(),
                components,
                new String[0],
                new String[0],
                null);

        OpenAPI openAPI = new OpenAPI();
        openAPI.setComponents(components);
        assertEquals("{\"openapi\":\"3.0.1\",\"components\":{\"schemas\":{\"Payload\":" +
                        "{\"properties\":{\"error\":{\"type\":\"boolean\"},\"success\":{\"type\":\"boolean\"}}}}}}",
                createObjectMapper(Main.Output.JSON).convertValue(openAPI, ObjectNode.class).toString());
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
