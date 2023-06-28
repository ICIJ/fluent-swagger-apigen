# fluent-swagger-apigen

[![Java CI](https://github.com/ICIJ/fluent-swagger-apigen/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/ICIJ/fluent-swagger-apigen/actions/workflows/ci.yml)

This repository provides a utility jar to scan your [fluent-http](https://github.com/CodeStory/fluent-http) resources and create an [openAPI specification file](https://swagger.io/specification/).

## How to use it?

You need to build or download the `fluent-swagger-apigen` jar. Then you have to call the [Main](src/main/java/org/icij/swagger/Main.java) class that will scan classpath resources with the `@Prefix` annotation. 

It will create an `openapi.yml` file with the specifications.

example : 

```
java -cp path/to/fluent-swagger-apigen-0.1-jar-with-dependencies.jar:path/to/your/fluent/http/jar org.icij.swagger.Main your.resources.package.name
```

It is a work in progress.