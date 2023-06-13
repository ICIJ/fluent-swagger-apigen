package org.icij.swagger;

import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiReader;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * from <a href="https://github.com/swagger-api/swagger-core/blob/master/modules/swagger-jaxrs2/src/main/java/io/swagger/v3/jaxrs2/Reader.java">Swagger Reader</a>
 */
public class FluentReader implements OpenApiReader {
    public FluentReader(OpenAPI openAPI) {
    }

    public FluentReader(SwaggerConfiguration openAPI31) {
    }

    @Override
    public void setConfiguration(OpenAPIConfiguration openAPIConfiguration) {
    }

    @Override
    public OpenAPI read(Set<Class<?>> set, Map<String, Object> map) {
        return null;
    }

    /**
     * Scans a single class for Swagger annotations - does not invoke ReaderListeners
     */
    public OpenAPI read(Class<?> cls) {
        return read(cls, resolveApplicationPath(), null, false, null, null, new LinkedHashSet<String>(), new ArrayList<Parameter>(), new HashSet<Class<?>>());
    }

    private OpenAPI read(Class<?> cls, String resolveApplicationPath, Object o, boolean b, Object o1, Object o2, LinkedHashSet<String> strings, ArrayList<Parameter> parameters, HashSet<Class<?>> classes) {
        return null;
    }

    private String resolveApplicationPath() {
        return "";
    }

    public OpenAPI read(Set<Class<?>> set) {
        return null;
    }

}
