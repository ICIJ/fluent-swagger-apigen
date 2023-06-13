package org.icij.swagger.petstore;

import io.swagger.v3.oas.models.OpenAPI;
import org.icij.swagger.FluentReader;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNotNull;


public class AcceptanceTest {
    @Test
    public void test_populate_openapi() throws Exception {
        final FluentReader reader = new FluentReader(new OpenAPI());
        final OpenAPI openAPI = reader.read(getSetOfClassesFromPackage("org.icij.swagger.petstore"));
        assertNotNull(openAPI);
        SerializationMatchers.assertEqualsToYaml(openAPI, Files.readString(Paths.get(getClass().getClassLoader().getResource("petstore/FullPetResource.yaml").toURI())));
    }

    private static List<Class> findClasses(final File directory, final String packageName) throws ClassNotFoundException {
        final List<Class> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        final File[] files = directory.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.isDirectory()) {
                    assert !file.getName().contains(".");
                    classes.addAll(findClasses(file, packageName + "." + file.getName()));
                } else if (file.getName().endsWith(".class")) {
                    classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
                }
            }
        }
        return classes;
    }

    private static Class[] getClasses(final String packageName)
            throws ClassNotFoundException, IOException {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        final String path = packageName.replace(".", "/");
        final Enumeration<URL> resources = classLoader.getResources(path);
        final List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            final URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        final ArrayList<Class> classes = new ArrayList<>();
        for (final File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }


    private Set<Class<?>> getSetOfClassesFromPackage(final String packageName) throws IOException, ClassNotFoundException {
        final Set<Class<?>> classSet = new HashSet<>();
            final Class[] classes = getClasses(packageName);
            for (final Class aClass : classes) {
                classSet.add(aClass);
            }
        return classSet;
    }
}
