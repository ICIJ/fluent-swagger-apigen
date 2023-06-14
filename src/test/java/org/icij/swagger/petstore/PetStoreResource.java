package org.icij.swagger.petstore;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import net.codestory.http.WebServer;
import net.codestory.http.annotations.*;

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
            responses = { @ApiResponse(responseCode = "200") }
    )
    @Get("/inventory")
    public Map<String, Integer> getInventory() {
        return petData.getInventoryByStatus();
    }

    @Operation(summary = "Find purchase order by ID",
            description = "For valid response try integer IDs with value >= 1 and <= 10. Other values will generated exceptions",
            responses = { @ApiResponse(responseCode = "400", ref = "Invalid ID supplied"),
                    @ApiResponse(responseCode = "404", ref = "Order not found") }
    )
    @Get("/order/:orderId")
    public Order getOrderById(
            @Parameter(name = "orderId", description = "ID of pet that needs to be fetched", required = true ) Long orderId) {
        return notFoundIfNull(storeData.findOrderById(orderId));
    }

    @Post("/order")
    @Operation(description = "Place an order for a pet")
    @ApiResponses({ @ApiResponse(responseCode = "400", ref = "Invalid Order") })
    public Order placeOrder(
            @Parameter(description = "order placed for purchasing the pet", required = true) Order order) {
        return storeData.placeOrder(order);
    }

    @Operation(description = "Delete purchase order by ID",
            summary = "For valid response try integer IDs with positive integer value. Negative or non-integer values will generate API errors",
            responses = { @ApiResponse(responseCode = "400", ref = "Invalid ID supplied"),
                    @ApiResponse(responseCode = "404", ref = "Order not found") })
    @Delete("/order/:orderId")
    public boolean deleteOrder(
            @Parameter(name="orderId", description = "ID of the order that needs to be deleted", required = true) Long orderId) {
        return storeData.deleteOrder(orderId);
    }

    public static void main(String[] args) {
        new WebServer().withSelectThreads(2).withThreadCount(10).configure(
                routes -> routes.add(PetStoreResource.class).add(PetResource.class)
        ).start(12345);
    }
}