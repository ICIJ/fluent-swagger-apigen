package org.icij.swagger.petstore;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.codestory.http.Context;
import net.codestory.http.annotations.Delete;
import net.codestory.http.annotations.Get;
import net.codestory.http.annotations.Post;
import net.codestory.http.annotations.Prefix;
import net.codestory.http.annotations.Put;
import net.codestory.http.payload.Payload;

import java.util.List;

import static net.codestory.http.errors.NotFoundException.notFoundIfNull;
import static net.codestory.http.payload.Payload.notFound;
import static net.codestory.http.payload.Payload.ok;


@Tag(name = "/api/pet", description = "Operations about pets")
@Prefix("/api/pet")
public class PetResource {
    static PetData petData = new PetData();

    @Get("/:petId")
    @Operation(description = "Find pet by ID",
            summary = "Returns a pet when ID < 10.  ID > 10 or nonintegers will simulate API error conditions"
    )
    @ApiResponses(value = { @ApiResponse(description = "returns pet", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description="when id is not a number"),
            @ApiResponse(responseCode = "404", description = "when no pet is found") })

    public Pet getPetById(
            @Parameter(name = "petId", description = "ID of pet that needs to be fetched", in = ParameterIn.PATH, schema = @Schema(minimum = "1", maximum = "5"), required = true)
            Long petId) {
        return notFoundIfNull(petData.getPetById(petId));
    }


    @Operation(description = "Deletes a pet")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "returns true if deleted", useReturnTypeSchema = true),
                            @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
                            @ApiResponse(responseCode = "404", description = "Pet not found")})
    @Delete("/:petId")
    public boolean deletePet(@Parameter(description = "Pet id to delete", in = ParameterIn.PATH, required = true) Long petId) {
        return petData.deletePet(petId);
    }


    @Operation( description = "Add a new pet to the store",
                requestBody = @RequestBody(
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = Pet.class)
                        )
                )
    )
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "success", useReturnTypeSchema = true),
                            @ApiResponse(responseCode = "405", description = "Invalid input"),
                            @ApiResponse(responseCode = "200", useReturnTypeSchema = true)})
    @Post
    public Pet addPet(Pet pet) {
        return petData.addPet(pet);
    }

    @Operation(description = "Update an existing pet")
    @ApiResponses(value = { @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
            @ApiResponse(responseCode = "404", description = "Pet not found"),
            @ApiResponse(responseCode = "405", description = "Validation exception"),
            @ApiResponse(responseCode = "200", useReturnTypeSchema = true)
    })
    @Put
    public Pet updatePet(@Parameter(description = "Pet object that needs to be added to the store", in=ParameterIn.DEFAULT, required = true) Pet pet) {
        return petData.addPet(pet);
    }

    @Get("/findByStatus")
    @Operation(summary = "Finds Pets by status",
            description = "Multiple status values can be provided with comma separated strings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Invalid status value"),
            @ApiResponse(responseCode = "200", description = "list of pet with input status", useReturnTypeSchema = true)
    })
    public List<Pet> findPetsByStatus(
            @Parameter(description = "Status values that need to be considered for filter", in = ParameterIn.QUERY, required = true) String status) {
        return petData.findPetByStatus(status);
    }

    @Operation(summary = "Finds Pets by tags",
            description = "Multiple tags can be provided with comma separated strings. Use tag1, tag2, tag3 for testing.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "list of bet with input tags", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Invalid tag value") })
    @Get("/findByTags?tags=:tags")
    public List<Pet> findPetsByTags(@Parameter(description = "Tags to filter by", in=ParameterIn.QUERY, required = true) String tags) {
        return petData.findPetByTags(tags);
    }

    @Operation(description = "Updates a pet in the store with form data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "updated pet", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "405", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Pet not found")
    })
    @Post("/:petId")
    public Payload updatePetWithForm (
            @Parameter(name = "petId", description = "ID of pet that needs to be updated", in = ParameterIn.PATH, required = true)Long petId,
            Context context, boolean justToTestWith3Parameters) {
        String name = context.query().get("name");
        String status = context.query().get("status");
        Pet pet = petData.getPetById(petId);
        if(pet != null) {
            if(name != null && !"".equals(name))
                pet.setName(name);
            if(status != null && !"".equals(status))
                pet.setStatus(status);
            petData.addPet(pet);
            return ok();
        }
        else {
            return notFound();
        }
    }
}