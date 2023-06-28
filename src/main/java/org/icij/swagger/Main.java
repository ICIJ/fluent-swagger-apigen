package org.icij.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.core.util.Json31;
import io.swagger.v3.core.util.Yaml31;
import io.swagger.v3.oas.models.OpenAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.Objects;
import java.util.concurrent.Callable;

import static org.icij.swagger.ClassUtils.findAllClassesUsingClassLoader;

@CommandLine.Command(name = "fluentopenapi", mixinStandardHelpOptions = true, version = "fluentopenapi 0.1",
                    description = "fluentopenapi to generate open API specification file")
public class Main implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    @CommandLine.Option(names = {"-o",  "--output"}, type = Output.class, description = "Type of output. values: ${COMPLETION-CANDIDATES}", defaultValue = "YAML")
    private Output outputType;

    @CommandLine.Parameters(index = "0", description = "Package to scan for Fluent Resource classes")
    private String packageName;

    @Override
    public Integer call() throws Exception {
        LOGGER.info("generating OpenAPI file for package {}", packageName);
        final OpenAPI openAPI = new FluentReader().read(findAllClassesUsingClassLoader(packageName));
        if (openAPI.getPaths().isEmpty()) {
            LOGGER.warn("cannot find classes in package {}", packageName);
            return 1;
        } else {
            ObjectNode objectNode = createObjectMapper(outputType).convertValue(openAPI, ObjectNode.class);
            System.out.println(outputType == Output.JSON ? Json31.pretty(objectNode):Yaml31.pretty(objectNode));
            return 0;
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    static ObjectMapper createObjectMapper(Output type) {
        if (Objects.requireNonNull(type) == Output.JSON) {
            return Json31.mapper();
        }
        return Yaml31.mapper();
    }

    enum Output {
        YAML, JSON
    }
}
