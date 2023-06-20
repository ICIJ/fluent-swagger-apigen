package org.icij.swagger.petstore;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import net.codestory.http.Context;
import net.codestory.http.annotations.*;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.codestory.http.annotations.Prefix;
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
    @ApiResponses(value = { @ApiResponse(responseCode = "400", ref = "Invalid ID supplied"),
            @ApiResponse(responseCode = "404", ref = "Pet not found") })

    public Pet getPetById(
            @Parameter(name = "petId", description = "ID of pet that needs to be fetched", in = ParameterIn.PATH, schema = @Schema(minimum = "1", maximum = "5"), required = true)
            Long petId) {
        return notFoundIfNull(petData.getPetById(petId));
    }


    @Operation(description = "Deletes a pet")
    @ApiResponses(value = { @ApiResponse(responseCode = "400", ref = "Invalid ID supplied"),
                            @ApiResponse(responseCode = "404", ref = "Pet not found")})
    @Delete("/:petId")
    public boolean deletePet(@Parameter(description = "Pet id to delete", in = ParameterIn.PATH, required = true) Long petId) {
        return petData.deletePet(petId);
    }


    @Operation(description = "Add a new pet to the store")
    @ApiResponses(value = { @ApiResponse(responseCode = "405", ref = "Invalid input", useReturnTypeSchema = true) })
    @Post
    public Pet addPet(Pet pet) {
        return petData.addPet(pet);
    }

    @Operation(description = "Update an existing pet")
    @ApiResponses(value = { @ApiResponse(responseCode = "400", ref = "Invalid ID supplied"),
            @ApiResponse(responseCode = "404", ref = "Pet not found"),
            @ApiResponse(responseCode = "405", ref = "Validation exception"),
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
            @ApiResponse(responseCode = "400", ref = "Invalid status value"),
            @ApiResponse(responseCode = "200", useReturnTypeSchema = true)
    })
    public List<Pet> findPetsByStatus(
            @Parameter(description = "Status values that need to be considered for filter", in = ParameterIn.QUERY, required = true) String status) {
        return petData.findPetByStatus(status);
    }

    @Operation(summary = "Finds Pets by tags",
            description = "Multiple tags can be provided with comma separated strings. Use tag1, tag2, tag3 for testing.")
    @ApiResponses(value = { @ApiResponse(responseCode = "400", ref = "Invalid tag value") })
    @Get("/findByTags?tags=:tags")
    public List<Pet> findPetsByTags(@Parameter(description = "Tags to filter by", in=ParameterIn.QUERY, required = true) String tags) {
        return petData.findPetByTags(tags);
    }

    @Operation(description = "Updates a pet in the store with form data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "405", ref = "Invalid input"),
            @ApiResponse(responseCode = "404", ref = "Pet not found")
    })
    @Post("/:petId")
    public Payload updatePetWithForm (
            @Parameter(name = "petId", description = "ID of pet that needs to be updated", in = ParameterIn.PATH, required = true)Long petId,
            Context context) {
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