package org.icij.swagger;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.ParameterProcessor;
import io.swagger.v3.core.util.ReflectionUtils;
import io.swagger.v3.jaxrs2.OperationParser;
import io.swagger.v3.jaxrs2.ReaderListener;
import io.swagger.v3.jaxrs2.ResolvedParameter;
import io.swagger.v3.jaxrs2.ext.OpenAPIExtension;
import io.swagger.v3.jaxrs2.ext.OpenAPIExtensions;
import io.swagger.v3.jaxrs2.util.ReaderUtils;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.integration.ContextUtils;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiReader;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Encoding;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.tags.Tag;
import net.codestory.http.annotations.AnnotationHelper;
import net.codestory.http.annotations.Prefix;
import net.codestory.http.constants.Methods;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * from <a href="https://github.com/swagger-api/swagger-core/blob/master/modules/swagger-jaxrs2/src/main/java/io/swagger/v3/jaxrs2/Reader.java">Swagger Reader</a>
 */
public class FluentReader implements OpenApiReader {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private OpenAPI openAPI;
    private final Paths paths;
    private Components components;
    private final Set<Tag> openApiTags;
    protected OpenAPIConfiguration config;

    public FluentReader() {
        this(new OpenAPI());

    }

    public FluentReader(OpenAPI openAPI) {
        this.openAPI = openAPI;
        this.paths = new Paths();
        this.openApiTags = new LinkedHashSet<>();
        this.components = new Components();
    }

    public FluentReader(@NotNull SwaggerConfiguration openAPI31) {
        this();
        setConfiguration(openAPI31);
    }

    @Override
    public void setConfiguration(OpenAPIConfiguration openApiConfiguration) {
        if (openApiConfiguration != null) {
            this.config = ContextUtils.deepCopy(openApiConfiguration);
            if (openApiConfiguration.getOpenAPI() != null) {
                this.openAPI = this.config.getOpenAPI();
                if (this.openAPI.getComponents() != null) {
                    this.components = this.openAPI.getComponents();
                }
            }
        }
    }

    @Override
    public OpenAPI read(Set<Class<?>> set, Map<String, Object> map) {
        return openAPI;
    }

    public OpenAPI read(Set<Class<?>> classes) {
        Set<Class<?>> sortedClasses = new TreeSet<>((class1, class2) -> {
            if (class1.equals(class2)) {
                return 0;
            } else if (class1.isAssignableFrom(class2)) {
                return -1;
            } else if (class2.isAssignableFrom(class1)) {
                return 1;
            }
            return class1.getName().compareTo(class2.getName());
        });
        sortedClasses.addAll(classes);

        Map<Class<?>, ReaderListener> listeners = new HashMap<>();

        for (Class<?> cls : sortedClasses) {
            if (ReaderListener.class.isAssignableFrom(cls) && !listeners.containsKey(cls)) {
                try {
                    listeners.put(cls, (ReaderListener) cls.newInstance());
                } catch (Exception e) {
                    LOGGER.error("Failed to create ReaderListener", e);
                }
            }
        }

        for (ReaderListener listener : listeners.values()) {
            try {
                listener.beforeScan(this, openAPI);
            } catch (Exception e) {
                LOGGER.error("Unexpected error invoking beforeScan listener [" + listener.getClass().getName() + "]", e);
            }
        }

        for (Class<?> cls : sortedClasses) {
            read(cls, "datashare", null, false, null, null, new LinkedHashSet<String>(), new ArrayList<Parameter>(), new HashSet<Class<?>>());
        }

        for (ReaderListener listener : listeners.values()) {
            try {
                listener.afterScan(this, openAPI);
            } catch (Exception e) {
                LOGGER.error("Unexpected error invoking afterScan listener [" + listener.getClass().getName() + "]", e);
            }
        }
        return openAPI;
    }

    public OpenAPI read(Class<?> cls) {
        return openAPI;
    }

    protected void read(Class<?> cls,
                        String parentPath,
                        String parentMethod,
                        boolean isSubresource,
                        RequestBody parentRequestBody,
                        ApiResponses parentResponses,
                        Set<String> parentTags,
                        List<Parameter> parentParameters,
                        Set<Class<?>> scannedResources) {

        ApiResponse[] classResponses = ReflectionUtils.getRepeatableAnnotationsArray(cls, ApiResponse.class);
        io.swagger.v3.oas.annotations.tags.Tag[] apiTags = ReflectionUtils.getRepeatableAnnotationsArray(cls,
                io.swagger.v3.oas.annotations.tags.Tag.class);

        Prefix classPrefix = ReflectionUtils.getAnnotation(cls, Prefix.class);
        if (classPrefix != null) {
            final List<Method> methods = Arrays.stream(cls.getMethods())
                    .sorted(new MethodComparator())
                    .collect(Collectors.toList());

            List<Method> methodList = new ArrayList<>();
            RouteCollection routeCollection = new RouteCollection();
            AnnotationHelper.parseAnnotations("", cls, (httpMethod, uri, method) -> routeCollection.addResource(httpMethod, method, uri));
            for (RouteData route : routeCollection.routes) {
                io.swagger.v3.oas.annotations.Operation apiOperation = ReflectionUtils.getAnnotation(route.method, io.swagger.v3.oas.annotations.Operation.class);
                Operation operation = new Operation();
                setOperationObjectFromApiOperationAnnotation(operation, apiOperation);
                boolean methodDeprecated = ReflectionUtils.getAnnotation(route.method, Deprecated.class) != null;

                if (methodDeprecated) {
                    operation.setDeprecated(true);
                }
                JavaType classType = TypeFactory.defaultInstance().constructType(cls);
                BeanDescription bd = Json.mapper().getSerializationConfig().introspect(classType);
                AnnotatedMethod annotatedMethod = bd.findMethod(route.method.getName(), route.method.getParameterTypes());
                List<Parameter> operationParameters = new ArrayList<>();
                List<Parameter> formParameters = new ArrayList<>();
                Annotation[][] paramAnnotations = ReflectionUtils.getParameterAnnotations(route.method);

                for (int i = 0; i < annotatedMethod.getParameterCount(); i++) {
                    AnnotatedParameter param = annotatedMethod.getParameter(i);
                    final Type type = TypeFactory.defaultInstance().constructType(param.getParameterType(), cls);
                    io.swagger.v3.oas.annotations.Parameter paramAnnotation = AnnotationsUtils.getAnnotation(io.swagger.v3.oas.annotations.Parameter.class, paramAnnotations[i]);
                    Type paramType = ParameterProcessor.getParameterType(paramAnnotation, true);
                    if (paramType == null) {
                        paramType = type;
                    } else {
                        if (!(paramType instanceof Class)) {
                            paramType = type;
                        }
                    }
                    ResolvedParameter resolvedParameter = getParameters(paramType, Arrays.asList(paramAnnotations[i]), operation);
                    operationParameters.addAll(resolvedParameter.parameters);
                    // collect params to use together as request Body
                    formParameters.addAll(resolvedParameter.formParameters);
                }
                // if we have form parameters, need to merge them into single schema and use as request body.
                if (!formParameters.isEmpty()) {
                    Schema mergedSchema = new ObjectSchema();
                    Map<String, Encoding> encoding = new LinkedHashMap<>();
                    for (Parameter formParam : formParameters) {
                        if (formParam.getExplode() != null || (formParam.getStyle() != null) && Encoding.StyleEnum.fromString(formParam.getStyle().toString()) != null) {
                            Encoding e = new Encoding();
                            if (formParam.getExplode() != null) {
                                e.explode(formParam.getExplode());
                            }
                            if (formParam.getStyle() != null && Encoding.StyleEnum.fromString(formParam.getStyle().toString()) != null) {
                                e.style(Encoding.StyleEnum.fromString(formParam.getStyle().toString()));
                            }
                            encoding.put(formParam.getName(), e);
                        }
                        mergedSchema.addProperties(formParam.getName(), formParam.getSchema());
                        if (formParam.getSchema() != null &&
                                StringUtils.isNotBlank(formParam.getDescription()) &&
                                StringUtils.isBlank(formParam.getSchema().getDescription())) {
                            formParam.getSchema().description(formParam.getDescription());
                        }
                        if (null != formParam.getRequired() && formParam.getRequired()) {
                            mergedSchema.addRequiredItem(formParam.getName());
                        }
                    }
                    Parameter merged = new Parameter().schema(mergedSchema);
                }
                if (!operationParameters.isEmpty()) {
                    for (Parameter operationParameter : operationParameters) {
                        operation.addParametersItem(operationParameter);
                    }
                }

                // if subresource, merge parent parameters
                if (parentParameters != null) {
                    for (Parameter parentParameter : parentParameters) {
                        operation.addParametersItem(parentParameter);
                    }
                }

                final Iterator<OpenAPIExtension> chain = OpenAPIExtensions.chain();
                if (chain.hasNext()) {
                    final OpenAPIExtension extension = chain.next();
                    extension.decorateOperation(operation, route.method, chain);
                }

                PathItem pathItemObject;
                if (openAPI.getPaths() != null && openAPI.getPaths().get(route.uriPattern) != null) {
                    pathItemObject = openAPI.getPaths().get(route.uriPattern);
                } else {
                    pathItemObject = new PathItem();
                }

                if (StringUtils.isBlank(route.httpMethod)) {
                    continue;
                }
                setPathItemOperation(pathItemObject, route.httpMethod, operation);
                paths.addPathItem(route.uriPattern, pathItemObject);
                if (openAPI.getPaths() != null) {
                    this.paths.putAll(openAPI.getPaths());
                }
                openAPI.setPaths(this.paths);
            }

            if (!isEmptyComponents(components) && openAPI.getComponents() == null) {
                openAPI.setComponents(components);
            }

            AnnotationsUtils.getTags(apiTags, true).ifPresent(openApiTags::addAll);

            if (!openApiTags.isEmpty()) {
                Set<io.swagger.v3.oas.models.tags.Tag> tagsSet = new LinkedHashSet<>();
                if (openAPI.getTags() != null) {
                    for (io.swagger.v3.oas.models.tags.Tag tag : openAPI.getTags()) {
                        if (tagsSet.stream().noneMatch(t -> t.getName().equals(tag.getName()))) {
                            tagsSet.add(tag);
                        }
                    }
                }
                for (io.swagger.v3.oas.models.tags.Tag tag : openApiTags) {
                    if (tagsSet.stream().noneMatch(t -> t.getName().equals(tag.getName()))) {
                        tagsSet.add(tag);
                    }
                }
                openAPI.setTags(new ArrayList<>(tagsSet));
            }
        }
    }


    private boolean isEmptyComponents(Components components) {
        if (components == null) {
            return true;
        }
        if (components.getSchemas() != null && components.getSchemas().size() > 0) {
            return false;
        }
        if (components.getSecuritySchemes() != null && components.getSecuritySchemes().size() > 0) {
            return false;
        }
        if (components.getCallbacks() != null && components.getCallbacks().size() > 0) {
            return false;
        }
        if (components.getExamples() != null && components.getExamples().size() > 0) {
            return false;
        }
        if (components.getExtensions() != null && components.getExtensions().size() > 0) {
            return false;
        }
        if (components.getHeaders() != null && components.getHeaders().size() > 0) {
            return false;
        }
        if (components.getLinks() != null && components.getLinks().size() > 0) {
            return false;
        }
        if (components.getParameters() != null && components.getParameters().size() > 0) {
            return false;
        }
        if (components.getRequestBodies() != null && components.getRequestBodies().size() > 0) {
            return false;
        }
        if (components.getResponses() != null && components.getResponses().size() > 0) {
            return false;
        }

        return true;
    }

    private void setPathItemOperation(PathItem pathItemObject, String method, Operation operation) {
        switch (method) {
            case Methods.POST:
                pathItemObject.post(operation);
                break;
            case Methods.GET:
                pathItemObject.get(operation);
                break;
            case Methods.DELETE:
                pathItemObject.delete(operation);
                break;
            case Methods.PUT:
                pathItemObject.put(operation);
                break;
            case Methods.PATCH:
                pathItemObject.patch(operation);
                break;
            case Methods.TRACE:
                pathItemObject.trace(operation);
                break;
            case Methods.HEAD:
                pathItemObject.head(operation);
                break;
            case Methods.OPTIONS:
                pathItemObject.options(operation);
                break;
            default:
                break;
        }
    }

    private void setOperationObjectFromApiOperationAnnotation(
            Operation operation,
            io.swagger.v3.oas.annotations.Operation apiOperation) {
        if (StringUtils.isNotBlank(apiOperation.summary())) {
            operation.setSummary(apiOperation.summary());
        }
        if (StringUtils.isNotBlank(apiOperation.description())) {
            operation.setDescription(apiOperation.description());
        }

        ReaderUtils.getStringListFromStringArray(apiOperation.tags()).ifPresent(tags ->
                tags.stream()
                        .filter(t -> operation.getTags() == null || (operation.getTags() != null && !operation.getTags().contains(t)))
                        .forEach(operation::addTagsItem));

        OperationParser.getApiResponses(apiOperation.responses(), null, null, components, null).ifPresent(responses -> {
            if (operation.getResponses() == null) {
                operation.setResponses(responses);
            } else {
                responses.forEach(operation.getResponses()::addApiResponse);
            }
        });
    }

    protected ResolvedParameter getParameters(Type type, List<Annotation> annotations, Operation operation) {
        final Iterator<OpenAPIExtension> chain = OpenAPIExtensions.chain();
        if (!chain.hasNext()) {
            return new ResolvedParameter();
        }
        LOGGER.debug("getParameters for {}", type);
        Set<Type> typesToSkip = new HashSet<>();
        final OpenAPIExtension extension = chain.next();
        LOGGER.debug("trying extension {}", extension);

        return extension.extractParameters(annotations, type, typesToSkip, components, null, null, true, null, chain);
    }

    private static class MethodComparator implements Comparator<Method> {

        @Override
        public int compare(Method m1, Method m2) {
            // First compare the names of the method
            int val = m1.getName().compareTo(m2.getName());

            // If the names are equal, compare each argument type
            if (val == 0) {
                val = m1.getParameterTypes().length - m2.getParameterTypes().length;
                if (val == 0) {
                    Class<?>[] types1 = m1.getParameterTypes();
                    Class<?>[] types2 = m2.getParameterTypes();
                    for (int i = 0; i < types1.length; i++) {
                        val = types1[i].getName().compareTo(types2[i].getName());

                        if (val != 0) {
                            break;
                        }
                    }
                }
            }
            return val;
        }
    }

    private static class RouteCollection {
        List<RouteData> routes = new ArrayList<>();

        protected void addResource(String httpMethod, Method method, String uriPattern) {
            routes.add(new RouteData(httpMethod, method, uriPattern));
        }
    }

    private static class RouteData {
        final String httpMethod;
        final Method method;
        final String uriPattern;

        private RouteData(String httpMethod, Method method, String uriPattern) {
            this.httpMethod = httpMethod;
            this.method = method;
            this.uriPattern = uriPattern;
        }
    }
}
