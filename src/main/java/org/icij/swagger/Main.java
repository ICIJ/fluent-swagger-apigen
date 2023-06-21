package org.icij.swagger;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.icij.swagger.ClassUtils.getSetOfClassesFromPackage;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            LOGGER.error("usage: Main <package>");
            System.exit(1);
        }
        LOGGER.info("generating OpenAPI file for package {}", args[0]);
        final OpenAPI openAPI = new FluentReader().read(getSetOfClassesFromPackage("org.icij.swagger.petstore"));
        ObjectNode objectNode = Yaml.mapper().convertValue(openAPI, ObjectNode.class);
        byte[] serialized = objectNode.binaryValue();
        if (serialized != null) {
            Files.write(Paths.get("openapi.yml"), serialized);
        } else {
            LOGGER.info("cannot find classes in package {}", args[0]);
        }
    }
}
