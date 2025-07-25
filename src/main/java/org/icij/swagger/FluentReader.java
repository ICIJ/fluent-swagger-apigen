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
import io.swagger.v3.jaxrs2.Reader;
import io.swagger.v3.jaxrs2.ResolvedParameter;
import io.swagger.v3.jaxrs2.SecurityParser;
import io.swagger.v3.jaxrs2.ext.OpenAPIExtension;
import io.swagger.v3.jaxrs2.ext.OpenAPIExtensions;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Encoding;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponses;
import net.codestory.http.Query;
import net.codestory.http.annotations.AnnotationHelper;
import net.codestory.http.annotations.Prefix;
import net.codestory.http.constants.Methods;
import net.codestory.http.routes.UriParser;
import org.apache.commons.lang3.StringUtils;
import org.simpleframework.http.parse.QueryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * from <a href="https://github.com/swagger-api/swagger-core/blob/master/modules/swagger-jaxrs2/src/main/java/io/swagger/v3/jaxrs2/Reader.java">Swagger Reader</a>
 */
public class FluentReader extends Reader {
    private static final Logger LOGGER = LoggerFactory.getLogger(FluentReader.class);

    public FluentReader() {
        super();
    }

    public FluentReader(OpenAPI openAPI) {
        super(openAPI);
    }

    public FluentReader(OpenAPIConfiguration openApiConfiguration) {
        super(openApiConfiguration);
    }

    @Override
    public OpenAPI read(Class<?> cls,
                        String parentPath,
                        String parentMethod,
                        boolean isSubresource,
                        RequestBody parentRequestBody,
                        ApiResponses parentResponses,
                        Set<String> parentTags,
                        List<Parameter> parentParameters,
                        Set<Class<?>> scannedResources) {

        // OpenApiDefinition
        OpenAPIDefinition openAPIDefinition = ReflectionUtils.getAnnotation(cls, OpenAPIDefinition.class);
        if (openAPIDefinition != null) {

            // info
            AnnotationsUtils.getInfo(openAPIDefinition.info()).ifPresent(info -> getOpenAPI().setInfo(info));

            // OpenApiDefinition security requirements
            SecurityParser
                    .getSecurityRequirements(openAPIDefinition.security())
                    .ifPresent(s -> getOpenAPI().setSecurity(s));
            //
            // OpenApiDefinition external docs
            AnnotationsUtils
                    .getExternalDocumentation(openAPIDefinition.externalDocs())
                    .ifPresent(docs -> getOpenAPI().setExternalDocs(docs));

            // OpenApiDefinition tags
            AnnotationsUtils
                    .getTags(openAPIDefinition.tags(), false)
                    .ifPresent(tags -> getOpenApiTags().addAll(tags));

            // OpenApiDefinition servers
            AnnotationsUtils.getServers(openAPIDefinition.servers()).ifPresent(servers -> getOpenAPI().setServers(servers));

            // OpenApiDefinition extensions
            if (openAPIDefinition.extensions().length > 0) {
                getOpenAPI().setExtensions(AnnotationsUtils
                        .getExtensions(openAPIDefinition.extensions()));
            }
        }

        ApiResponse[] classResponses = ReflectionUtils.getRepeatableAnnotationsArray(cls, ApiResponse.class);
        io.swagger.v3.oas.annotations.tags.Tag[] apiTags = ReflectionUtils.getRepeatableAnnotationsArray(cls,
                io.swagger.v3.oas.annotations.tags.Tag.class);

        Prefix classPrefix = ReflectionUtils.getAnnotation(cls, Prefix.class);
        if (classPrefix != null) {
            JavaType classType = TypeFactory.defaultInstance().constructType(cls);
            BeanDescription bd = Json.mapper().getSerializationConfig().introspect(classType);

            RouteCollection routeCollection = new RouteCollection();
            AnnotationHelper.parseAnnotations("", cls, (httpMethod, uri, method) -> routeCollection.addResource(httpMethod, method, uri));
            for (RouteData route : routeCollection.routes) {
                io.swagger.v3.oas.annotations.Operation apiOperation = ReflectionUtils.getAnnotation(route.method, io.swagger.v3.oas.annotations.Operation.class);
                if (apiOperation != null) {
                    AnnotatedMethod annotatedMethod = bd.findMethod(route.method.getName(), route.method.getParameterTypes());
                    Operation operation = parseMethod(
                            route.method,
                            null,
                            null,
                            null,
                            null,
                            null,
                            new ArrayList<>(),
                            Optional.empty(),
                            null,
                            null,
                            isSubresource,
                            parentRequestBody,
                            parentResponses,
                            null,
                            classResponses,
                            annotatedMethod);
                    boolean methodDeprecated = ReflectionUtils.getAnnotation(route.method, Deprecated.class) != null;

                    if (methodDeprecated) {
                        operation.setDeprecated(true);
                    }
                    List<Parameter> operationParameters = new ArrayList<>();
                    List<Parameter> formParameters = new ArrayList<>();
                    Annotation[][] paramAnnotations = ReflectionUtils.getParameterAnnotations(route.method);

                    if (annotatedMethod == null) { // annotatedMethod not null only when method with 0-2 parameters
                        Type[] genericParameterTypes = route.method.getGenericParameterTypes();
                        for (int i = 0; i < genericParameterTypes.length; i++) {
                            final Type type = TypeFactory.defaultInstance().constructType(genericParameterTypes[i], cls);
                            io.swagger.v3.oas.annotations.Parameter paramAnnotation = AnnotationsUtils.getAnnotation(io.swagger.v3.oas.annotations.Parameter.class, paramAnnotations[i]);
                            Type paramType = ParameterProcessor.getParameterType(paramAnnotation, true);
                            if (paramType == null) {
                                paramType = type;
                            } else {
                                if (!(paramType instanceof Class)) {
                                    paramType = type;
                                }
                            }
                            ResolvedParameter resolvedParameter = getParameters(paramType, Arrays.asList(paramAnnotations[i]), operation, null, null, null);
                            operationParameters.addAll(resolvedParameter.parameters);
                            // collect params to use together as request Body
                            formParameters.addAll(resolvedParameter.formParameters);
                            if (resolvedParameter.requestBody != null) {
                                processRequestBody(
                                        resolvedParameter.requestBody,
                                        operation,
                                        null,
                                        null,
                                        operationParameters,
                                        paramAnnotations[i],
                                        type,
                                        null,
                                        null);
                            }
                        }
                    } else {
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
                            ResolvedParameter resolvedParameter = getParameters(paramType, Arrays.asList(paramAnnotations[i]), operation, null, null, null);
                            operationParameters.addAll(resolvedParameter.parameters);
                            // collect params to use together as request Body
                            formParameters.addAll(resolvedParameter.formParameters);
                            if (resolvedParameter.requestBody != null) {
                                processRequestBody(
                                        resolvedParameter.requestBody,
                                        operation,
                                        null,
                                        null,
                                        operationParameters,
                                        paramAnnotations[i],
                                        type,
                                        null,
                                        null);
                            }
                        }
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
                    if (getOpenAPI().getPaths() != null && getOpenAPI().getPaths().get(route.uriPattern) != null) {
                        pathItemObject = getOpenAPI().getPaths().get(route.uriPattern);
                    } else {
                        pathItemObject = new PathItem();
                    }

                    if (StringUtils.isBlank(route.httpMethod)) {
                        continue;
                    }
                    setPathItemOperation(pathItemObject, route.httpMethod, operation);
                    getPaths().addPathItem(route.uriPattern, pathItemObject);
                    if (getOpenAPI().getPaths() != null) {
                        getPaths().putAll(getOpenAPI().getPaths());
                    }
                    getOpenAPI().setPaths(getPaths());
                    LOGGER.info("added method {}.{} ({}) to openAPI", cls.getName(), route.method.getName(), route.httpMethod);
                }
            }

            if (!isEmptyComponents(getComponents()) && getOpenAPI().getComponents() == null) {
                getOpenAPI().setComponents(getComponents());
            }

            AnnotationsUtils.getTags(apiTags, true).ifPresent(getOpenApiTags()::addAll);

            if (!getOpenApiTags().isEmpty()) {
                Set<io.swagger.v3.oas.models.tags.Tag> tagsSet = new LinkedHashSet<>();
                if (getOpenAPI().getTags() != null) {
                    for (io.swagger.v3.oas.models.tags.Tag tag : getOpenAPI().getTags()) {
                        if (tagsSet.stream().noneMatch(t -> t.getName().equals(tag.getName()))) {
                            tagsSet.add(tag);
                        }
                    }
                }
                for (io.swagger.v3.oas.models.tags.Tag tag : getOpenApiTags()) {
                    if (tagsSet.stream().noneMatch(t -> t.getName().equals(tag.getName()))) {
                        tagsSet.add(tag);
                    }
                }
                getOpenAPI().setTags(new ArrayList<>(tagsSet));
            }
        }
        return getOpenAPI();
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

    static class RouteCollection {
        Set<RouteData> routes = new TreeSet<>();

        protected void addResource(String httpMethod, Method method, String uriPattern) {
            UriParser parser = new UriParser(uriPattern);
            String[] params = parser.params(bareUri(uriPattern), new SimpleQuery(new QueryParser(queryParams(uriPattern))));
            String uriPatterWithCurlyBraces = uriPattern;
            for (String param : params) {
                uriPatterWithCurlyBraces = uriPatterWithCurlyBraces.replace(param, String.format("{%s}", param.replace(":", "")));
            }
            routes.add(new RouteData(httpMethod, method, uriPatterWithCurlyBraces));
        }
    }

    record RouteData(String httpMethod, Method method, String uriPattern) implements Comparable<RouteData> {
        @Override
        public int compareTo(RouteData other) {
            int uriPatternResult = this.uriPattern.compareTo(other.uriPattern);
            return uriPatternResult == 0 ? this.httpMethod.compareTo(other.httpMethod) : uriPatternResult;
        }
    }

    static String queryParams(String uri) {
        int indexQuestionMark = uri.indexOf(63);
        return indexQuestionMark == -1 ? uri : uri.substring(indexQuestionMark+1);
    }

    static String bareUri(String uri) {
        int indexQuestionMark = uri.indexOf(63);
        return indexQuestionMark == -1 ? uri : uri.substring(0, indexQuestionMark);
    }

    static class SimpleQuery implements Query {
        private final org.simpleframework.http.Query query;

        SimpleQuery(org.simpleframework.http.Query query) {
            this.query = query;
        }

        @Override
        public Collection<String> keys() {
            return query.keySet();
        }

        @Override
        public String get(String name) {
            return query.get(name);
        }

        @Override
        public Iterable<String> all(String name) {
            return query.getAll(name);
        }

        @Override
        public Map<String, String> keyValues() {
            return query;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T unwrap(Class<T> type) {
            return type.isInstance(query) ? (T) query : null;
        }
    }
}
