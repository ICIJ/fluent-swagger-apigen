package org.icij.swagger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ClassUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassUtils.class);
    static Class[] getClasses(final String packageName)
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
            classes.addAll(ClassUtils.findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    static Set<Class<?>> getSetOfClassesFromPackage(final String packageName) throws IOException, ClassNotFoundException {
        final Set<Class<?>> classSet = new HashSet<>();
        final Class[] classes = getClasses(packageName);
        LOGGER.info("found classes {}", classes);

        for (final Class aClass : classes) {
            classSet.add(aClass);
        }
        return classSet;
    }
    static List<Class> findClasses(final File directory, final String packageName) throws ClassNotFoundException {
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
}
