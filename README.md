# fluent-swagger-apigen

[![Java CI](https://github.com/ICIJ/fluent-swagger-apigen/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/ICIJ/fluent-swagger-apigen/actions/workflows/ci.yml)

This repository provides a utility jar to scan your [fluent-http](https://github.com/CodeStory/fluent-http) resources and create an [openAPI specification file](https://swagger.io/specification/).

It is a work in progress.

## How to use it?

### Good old way

You need to build or download the `fluent-swagger-apigen` jar. Then you have to call the [Main](src/main/java/org/icij/swagger/Main.java) class that will scan classpath resources with the `@Prefix` annotation. 

It will create an `openapi.yml` file with the specifications.

Example: 

```shell
java -cp path/to/fluent-swagger-apigen-0.1-jar-with-dependencies.jar:path/to/your/fluent/http/jar org.icij.swagger.Main your.resources.package.name
```

### With jbang

First install [jbang](https://www.jbang.dev/)

Then use it:

```shell
jbang run --cp path/to/your/fluent/webapp.jar fluentopenapi.java
```

## How to document resources

You will find a small example of a fluent petstore [in the tests](src/test/java/org/icij/swagger/petstore).

It is based on the swagger annotations v3.

Example:

```java
@Operation(description = "Deletes a pet")
@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "returns true if deleted", useReturnTypeSchema = true),
                        @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
                        @ApiResponse(responseCode = "404", description = "Pet not found")})
@Delete("/:petId")
public boolean deletePet(@Parameter(description = "Pet id to delete", in = ParameterIn.PATH, required = true) Long petId) {
    return petData.deletePet(petId);
}
```
