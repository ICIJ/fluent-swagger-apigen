package org.icij.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.swagger.v3.core.util.ObjectMapperFactory;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.icij.swagger.ClassUtils.findAllClassesUsingClassLoader;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            LOGGER.error("usage: Main <package>");
            System.exit(1);
        }
        LOGGER.info("generating OpenAPI file for package {}", args[0]);
        final OpenAPI openAPI = new FluentReader().read(findAllClassesUsingClassLoader(args[0]));
        if (openAPI.getPaths().isEmpty()) {
            LOGGER.warn("cannot find classes in package {}", args[0]);
        } else {
            ObjectNode objectNode = createFactory().convertValue(openAPI, ObjectNode.class);
            String serialized = Yaml.pretty(objectNode);
            Files.write(Paths.get("openapi.yml"), serialized.getBytes());
        }
    }

    static ObjectMapper createFactory() {
        YAMLFactory factory = new YAMLFactory();
        factory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
        factory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
        factory.enable(YAMLGenerator.Feature.SPLIT_LINES);
        factory.enable(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS);
        ObjectMapper objectMapper = ObjectMapperFactory.create(factory, true);
        return objectMapper;
    }
}
