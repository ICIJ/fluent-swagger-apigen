package org.icij.swagger.petstore;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import net.codestory.http.WebServer;
import net.codestory.http.annotations.Delete;
import net.codestory.http.annotations.Get;
import net.codestory.http.annotations.Post;
import net.codestory.http.annotations.Prefix;

import java.util.Map;

import static net.codestory.http.errors.NotFoundException.notFoundIfNull;

@Prefix("/api/store")
public class PetStoreResource {
    static StoreData storeData = new StoreData();
    static PetData petData = new PetData();


    @Operation(description = "Returns petstore home page")
    @Get
    public String root() {
        return "Fluent HTTP petstore";
    }

    @Operation(description = "Returns pet inventories by status",
            summary = "Returns a map of status codes to quantities",
            responses = { @ApiResponse(responseCode = "200", useReturnTypeSchema = true) }
    )
    @Get("/inventory")
    public Map<String, Integer> getInventory() {
        return petData.getInventoryByStatus();
    }

    @Operation(summary = "Find purchase order by ID",
            description = "For valid response try integer IDs with value >= 1 and <= 10. Other values will generated exceptions",
            responses = { @ApiResponse(responseCode = "200", description = "returns order with given id", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
                    @ApiResponse(responseCode = "404", description = "Order not found") }
    )
    @Get("/order/:orderId")
    public Order getOrderById(
            @Parameter(name = "orderId", in = ParameterIn.PATH, description = "ID of pet that needs to be fetched", required = true ) Long orderId) {
        return notFoundIfNull(storeData.findOrderById(orderId));
    }

    @Operation(description = "Place an order for a pet")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "returns the placed order", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Invalid Order") })
    @Post("/order")
    public Order placeOrder(Order order) {
        return storeData.placeOrder(order);
    }

    @Operation(description = "Delete purchase order by ID",
            summary = "For valid response try integer IDs with positive integer value. Negative or non-integer values will generate API errors",
            responses = { @ApiResponse(responseCode = "200", description = "returns true if deleted", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
                    @ApiResponse(responseCode = "404", description = "Order not found") })
    @Delete("/order/:orderId")
    public boolean deleteOrder(
            @Parameter(name="orderId", in = ParameterIn.PATH, description = "ID of the order that needs to be deleted", required = true) Long orderId) {
        return storeData.deleteOrder(orderId);
    }

    public static void main(String[] args) {
        new WebServer().withSelectThreads(2).withThreadCount(10).configure(
                routes -> routes.add(PetStoreResource.class).add(PetResource.class)
        ).start(12345);
    }
}