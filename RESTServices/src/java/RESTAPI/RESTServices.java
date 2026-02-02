/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/WebServices/GenericResource.java to edit this template
 */
package RESTAPI;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.net.http.HttpTimeoutException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javax.ws.rs.POST;

/**
 * CycleNest REST Orchestrator Service
 * 
 * This service acts as the central orchestrator for the rental platform, handling:
 * - Item search with filtering and pagination
 * - Distance calculations via OSRM API integration
 * - Rental request creation and cancellation
 * - Database interactions with Azure Cosmos DB
 * 
 * Base URL: http://localhost:8080/RESTServices/webresources/RESTAPI
 * 
 * @author N1237155
 * 
 */
@Path("RESTAPI")
public class RESTServices {

    @Context
    private UriInfo context;
    
    // Timeout duration for OSRM API requests (30 seconds)
    private static final int TIMEOUT_SECONDS = 30;
    
    // Shared HttpClient instance for all OSRM API Calls
    // Creating a new client for every request causes resources exhaustion under load
    // This singilton client enables connection pooling and efficient resourse usage
    private static final HttpClient SHARED_HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .build();

    // Empty constructor required for REST services
    public RESTServices() {
    }

    /**
     * Endpoint: GET /direct
     * 
     * Calculate direct distance between two coordinates using OSRM API
     * Useful for checking distances between any two points
     * 
     * Example: http://localhost:8080/RESTServices/webresources/RESTAPI/direct?startLon=-1.15&startLat=52.95&endLon=-0.13&endLat=51.51
     * 
     * @param startLon Starting longitude
     * @param startLat Starting latitude
     * @param endLon Ending longitude
     * @param endLat Ending latitude
     * @return JSON with distance in km and duration in minutes
     * 
     * The @GET annotation means this method responds to HTTP GET requests
     * The @Produces annotation tells the client we're sending back JSON data
     */
    @GET
    @Path("/direct")
    @Produces(MediaType.APPLICATION_JSON)
    // The @QueryParam annotations extract parameters from the URL
    public String getDirectDistence(@QueryParam("startLon") String startLon,
                                    @QueryParam("startLat") String startLat,
                                    @QueryParam("endLon") String endLon,
                                    @QueryParam("endLat") String endLat) {
        
        // Validate that all required parameters are provided
        if (startLon == null || startLat == null || endLon == null || endLat == null) {
            return createErrorResponse("MISSING_PARAMETERS", 
                "All parameters are required: startLon, startLat, endLon, endLat");
        }
        
        // Validate coordinate format (basic validation)
        try {
            Double.parseDouble(startLon);
            Double.parseDouble(startLat);
            Double.parseDouble(endLon);
            Double.parseDouble(endLat);
        } catch (NumberFormatException e) {
            return createErrorResponse("INVALID_COORDINATES","Coordinates must be valid numbers");
        }
        
        //Calculate distance
        return calculateOSRMDistance(startLon, startLat, endLon, endLat);
    }
        
        /**
          * Endpoint: GET /items/{item_id}/distance
          * 
          * Calculate distance from user location to a specific rental item
          * Combines item details from database with OSRM distance calculation
          * 
          * Example: http://localhost:8080/RESTServices/webresources/RESTAPI/items/i001/distance?userLat=52.95&userLon=-1.15
          * 
          * @param itemId The item ID from database (e.g., i001)
          * @param userLat User's latitude coordinate
          * @param userLon User's longitude coordinate
          * @return JSON with item details, distance in km, and duration in minutes
         */
        @GET
        @Path("/items/{item_id}/distance")
        public String getItemDistance(@PathParam("item_id")String itemId,
                                        @QueryParam("userLat") String userLat,
                                        @QueryParam("userLon") String userLon){
                                    
            //Validate user coordinates
            if (userLat == null || userLon == null){
                return createErrorResponse("MISSING_PARAMETERS", "User coordinates required:userLat, userLon");
            }
            
            try{
                Double.parseDouble(userLat);
                Double.parseDouble(userLon);
            }catch (NumberFormatException e){
                return createErrorResponse("INVALID_COORDINATES", "User coordinates must be valid numbers");
            }
            
        //Fetch item from database
        CosmosDBConnection db = null;
        items item = null;
        
        try {
            db = CosmosDBConnection.getInstance();
            item = db.getItemById(itemId);
            
            if (item == null) {
                return createErrorResponse("ITEM_NOT_FOUND", 
                    "Item with ID '" + itemId + "' not found");
            }
            
        } catch (Exception e) {
            return createErrorResponse("DATABASE_ERROR", 
                "Database connection failed: " + e.getMessage());
        } finally {
            if (db != null) {
            }
        }
        
        //Get item coordinates from database
        String itemLon = String.valueOf(item.getLongitude());
        String itemLat = String.valueOf(item.getLatitude());
        
        //Calculate distance using OSRM
        String osrmResult = calculateOSRMDistance(userLon, userLat, itemLon, itemLat);
        
        //Combine item info with distance
        try {
            ObjectMapper mapper = new ObjectMapper();
            RouteResponse routeResponse = mapper.readValue(osrmResult, RouteResponse.class);
            
            // Create enhanced response
            ItemDistanceResponse response = new ItemDistanceResponse(
            item.getId(),
           item.getName(),
           item.getCategory(),
          item.getDailyRate(),
              item.getCity(),
          item.getCondition(),
         item.getDescription(),
           routeResponse.getDistanceKm(),
           routeResponse.getDurationMinutes(),
            "success"
            );
            
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
            
        } catch (JsonProcessingException e) {
            // If OSRM returned an error, just return it
            return osrmResult;
        }
    }
        
       /**
        * Endpoint: GET /items
        * 
        * Retrieve all items with optional filtering, pagination, and distance calculation
        * 
        * Filtering options:
        * - category: Filter by item category (e.g., Sports, Camping)
        * - city: Filter by city location (case-insensitive)
        * - condition: Filter by item condition (e.g., Excellent, Good)
        * 
        * Pagination:
        * - page: Page number (default: 1)
        * - pageSize: Fixed at 5 items per page
        * 
        * Distance calculation (optional):
        * - userLat, userLon: Calculate distance from user to each item
        * 
        * Example: http://localhost:8080/RESTServices/webresources/RESTAPI/items?category=Sports&city=London&page=2&userLat=51.5&userLon=-0.1
        * 
        * @param userLat Optional user latitude for distance calculation
        * @param userLon Optional user longitude for distance calculation
        * @param pageParam Page number (default: 1)
        * @param category Filter by category
        * @param city Filter by city
        * @param condition Filter by condition
        * @return Paginated JSON response with items and metadata
        */

        @GET
        @Path("/items")
        @Produces(MediaType.APPLICATION_JSON)
        public String getAllItems(@QueryParam("userLat")String userLat,
                                  @QueryParam("userLon")String userLon,
                                  @QueryParam("page")String pageParam,
                                  @QueryParam("category")String category,
                                  @QueryParam("city")String city,
                                  @QueryParam("condition")String condition){
            
            CosmosDBConnection db = null;
            List<items>allItems = null;
            
            int page = 1;
            int pageSize = 5;  // Fixed page size of 5 items per page to increase the efficiency
            
            try{
                if (pageParam != null && !pageParam.isEmpty()){
                    page = Integer.parseInt(pageParam);
                    if (page < 1){
                        return createErrorResponse("INVALID_PAGE", "Page number must be greater than 0");
                    }
                }
            }catch(NumberFormatException e){
                return createErrorResponse("INVALID_PAGEPARAM","Page must be a valid number");
            }
            
            try{
                db = CosmosDBConnection.getInstance();
                allItems = db.getAllItems();
                
                if (allItems == null || allItems.isEmpty()){
                    return createErrorResponse("NO_ITEMS_FOUND","No Items Available In The Database");
                }
                
                // Each filter checks both user input and item data for null values to prevent NullPointerException
                // Filters are applied sequentially, narrowing results with each criterion
                if (category != null && !category.isEmpty()){
                    allItems = allItems.stream()
                            .filter(item -> item.getCategory() != null && item.getCategory().equalsIgnoreCase(category))
                            .collect(java.util.stream.Collectors.toList());
                }
                
                if (city != null && !city.isEmpty()){
                    allItems = allItems.stream()
                            .filter(item -> item.getCity()!= null && item.getCity().equalsIgnoreCase(city))
                            .collect(java.util.stream.Collectors.toList());
                }
                
                if (condition != null && !condition.isEmpty()){
                    allItems = allItems.stream()
                            .filter(item -> item.getCondition()!= null && item.getCondition().equalsIgnoreCase(condition))
                            .collect(java.util.stream.Collectors.toList());
                }
                
                // Return error message if filters produced no results
                if(allItems.isEmpty() && (category != null || city != null || condition != null)){
                    String message = "No items found matching filters: ";
                    List<String> appliedFilters = new ArrayList<>();
                    
                    if (category != null && !category.isEmpty()){
                        appliedFilters.add("category="+ category);
                    }
                    
                    if (city != null && !city.isEmpty()){
                        appliedFilters.add("city="+ city);
                    }
                    
                    if (condition != null && !condition.isEmpty()){
                        appliedFilters.add("condition="+ condition);
                    }
                    
                    return createErrorResponse("NO_MATCHES ",message + String.join(", ", appliedFilters)+". Check spelling or try different filters.");
                    
                }
                
                // Calculate pagination metadata
                int totalItems = allItems.size();
                int totalPages = (int)Math.ceil((double)totalItems / pageSize);
                int startIndex = (page - 1) * pageSize;
                int endIndex = Math.min(startIndex + pageSize, totalItems);
                
                if (startIndex >= totalItems){
                    return createErrorResponse("PAGE_OUT_OF_RANGE ","page " + page +" does not exist. Total pages: "+ totalPages);
                }
                
                // Extract current page's items
                List<items> pageItems = allItems.subList(startIndex, endIndex);
                
                ObjectMapper mapper = new ObjectMapper();

                // Calculate distances if user coordinates provided
                // Only calculate for items on current page (optimization)
                if(userLat != null && userLon != null){
                    try{
                        Double.parseDouble(userLat);
                        Double.parseDouble(userLon);
                        
                        List<ItemDistanceResponse> enhancedItems = new ArrayList<>();
                        
                        // Calculate distance for each item on current page
                        for(items item : pageItems){
                            String itemLon = String.valueOf(item.getLongitude());
                            String itemLat = String.valueOf(item.getLatitude());
                            
                            String osrmResult = calculateOSRMDistance(userLon, userLat,itemLon,itemLat);
                            
                            try{
                                RouteResponse routeResponse = mapper.readValue(osrmResult, RouteResponse.class);
                                
                                ItemDistanceResponse response = new ItemDistanceResponse(
                                item.getId(),
                                item.getName(),
                                item.getCategory(),
                                item.getDailyRate(),
                                item.getCity(),
                                item.getCondition(),
                                item.getDescription(),
                                routeResponse.getDistanceKm(),
                                routeResponse.getDurationMinutes(),
                                "success"
                            );
                                
                                enhancedItems.add(response);
                                
                            }catch(JsonProcessingException e){
                                System.err.println("Failed to calculate distance for item "+item.getId());
                            }
                        }
                        
                        // Create paginated response with metadata(for the request with distance calculation)
                        PaginatedResponse<ItemDistanceResponse> response = new PaginatedResponse<>(
                           enhancedItems,
                       page,
                                pageSize,
                                totalItems,
                                totalPages
                        );
                        
                        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
                    
                    }catch(NumberFormatException e){
                        return createErrorResponse("INVALID_COORDINATES", "User coordinates must be valid");
                    }
                }
                
                // Create paginated response with metadata(for the request with-out distance calculation)
                 PaginatedResponse<items> response = new PaginatedResponse<>(
                           pageItems,
                       page,
                                pageSize,
                                totalItems,
                                totalPages
                        );
                
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
            
            }catch(JsonProcessingException e){
                return createErrorResponse("JSON_PROCESSING_ERROR","Failed to process items data: "+e.getMessage());
            }catch(Exception e){
                return createErrorResponse("DATABASE_ERROR","Failed to retrive items: "+e.getMessage());
            }finally{
                if(db != null){
                }
            }
        }
    
        
    /**
       * Core method: Calculate distance using OSRM API
       * 
       * This method handles the external API integration:
       * 1. Builds OSRM API URL with coordinates
       * 2. Sends HTTP request with timeout
       * 3. Parses JSON response
       * 4. Converts distance (m→km) and duration (s→min)
       * 5. Returns simplified JSON response
       * 
       * OSRM API documentation: https://project-osrm.org/docs/v5.24.0/api/
       * 
       * @param startLon Starting point longitude
       * @param startLat Starting point latitude
       * @param endLon Ending point longitude
       * @param endLat Ending point latitude
       * @return JSON string with distance and duration or error message
       */
        private String calculateOSRMDistance(String startLon, String startLat,String endLon, String endLat) {
        try {
            // Build the OSRM API URL using the coordinates provided by the client
            String osrmUrl = "http://router.project-osrm.org/route/v1/driving/" 
                           + startLon + "," + startLat + ";" 
                           + endLon + "," + endLat 
                           + "?overview=false";
            
            // Use the shared HTTP client instead of creating a new one 
            // The shared client handles connection pooling automatically
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(osrmUrl))
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .GET()
                    .build();
            
            //Send the request to OSRM and get the response as a String
            HttpResponse<String> response = SHARED_HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            
            // Check HTTP status code
            if (response.statusCode() != 200) {
                return createErrorResponse("OSRM_ERROR", 
                    "OSRM API returned status code: " + response.statusCode());
            }
            
            //Extract the JSON response body from OSRM
            String jsonResponse = response.body();
            
            // Validate that response is JSON
            if (!jsonResponse.trim().startsWith("{")) {
                return createErrorResponse("INVALID_RESPONSE", 
                    "OSRM API returned invalid response format");
            }
            
            //Deserialize OSRM's JSON response into our OSRMResponse Java object
            //This converts the JSON text into Java objects we can work with
            ObjectMapper mapper = new ObjectMapper();
            OSRMResponse osrmResponse = mapper.readValue(jsonResponse, OSRMResponse.class);
            
            //Check if OSRM returned any routes
            //Extract the first route from the list
            //OSRM can return multiple route options, but we just need the first one
            if (osrmResponse.getRoutes() != null && !osrmResponse.getRoutes().isEmpty()) {
                OSRMResponse.RouteInfo firstRoute = osrmResponse.getRoutes().get(0);
                
                //Convert units to be more understandable
                //Convert meters to kilometers(divide by 1000)
                //Convert seconds to minites(divide by 60)
                double distanceInKm = firstRoute.getDistance()/1000.0;
                double durationInMinites = firstRoute.getDuration()/60.0;
                
                //Create our simplified response object with Converted Values
                //This is much cleaner than sending back OSRM's entire complex response
                RouteResponse routeResponse = new RouteResponse(
                    distanceInKm,
                    durationInMinites,
                    "success"
                );
                
                // Serialize our RouteResponse object back into JSON
                // This converts our Java object into a JSON string to send to the client
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(routeResponse);
            } else {
                // If OSRM didn't return any routes, send an error message
               return createErrorResponse("NO_ROUTE", 
                    "No route found between the specified coordinates");
            }
            
          
        } catch (HttpTimeoutException e) {
            // Handle timeout specifically - could retry or suggest alternative
            return createErrorResponse("TIMEOUT", 
                "Request to OSRM API timed out after " + TIMEOUT_SECONDS + " seconds. Please try again.");
            
        } catch (JsonProcessingException e) {
            // Handle JSON parsing errors
            return createErrorResponse("JSON_PARSE_ERROR", 
                "Failed to parse response from OSRM API: " + e.getMessage());
            
        } catch (IOException e) {
            // Handle network/IO errors
            return createErrorResponse("NETWORK_ERROR", 
                "Network error while contacting OSRM API: " + e.getMessage());
            
        } catch (InterruptedException e) {
            // Handle thread interruption
            Thread.currentThread().interrupt();
            return createErrorResponse("INTERRUPTED", 
                "Request was interrupted. Please try again.");
            
        } catch (Exception e) {
            // Catch any other unexpected errors
            return createErrorResponse("UNKNOWN_ERROR", 
                "An unexpected error occurred: " + e.getMessage());
        }
    }
        
    /**
    * Helper method: Create consistent error responses
     * 
     * All errors follow the same JSON format:
     * {"status": "error", "errorCode": "CODE", "message": "description"}
     * 
     * @param errorCode Specific error code (e.g., ITEM_NOT_FOUND)
     * @param message Human-readable error description
     * @return JSON formatted error string
     */
        
    //Replace double quotes into single quotes in the message
    //Prevent breaking the JSON if message contain quotes - rare senorio but can happen
    private String createErrorResponse(String errorCode, String message) {
        return String.format("{\"status\": \"error\", \"errorCode\": \"%s\", \"message\": \"%s\"}", 
                             errorCode, message.replace("\"", "'"));
    }
    
    /**
     * Endpoint: POST /items/{item_id}/request
     * 
     * Create a rental request for a specific item
     * Validates item exists and saves request to Cosmos DB with 'pending' status
     * 
     * Request ID format: REQ-{timestamp}-{random}
     * Example: REQ-1736694123456-7845
     * 
     * Example: POST http://localhost:8080/RESTServices/webresources/RESTAPI/items/i001/request?user_id=Alice
     * 
     * @param itemId The item to request (path parameter)
     * @param userId The user making the request (query parameter, defaults to DEMO_USER)
     * @return JSON response with created request details including ID, status, timestamp
     */
        
        @POST
        @Path("/items/{item_id}/request")
        @Produces(MediaType.APPLICATION_JSON)
        public String createItemRequest(@PathParam("item_id") String itemId,
                                        @QueryParam("user_id") String userId){
        
        //use default user if not provided
        if (userId == null || userId.isEmpty()){
            userId = "DEMO_USER";
        }
        
        CosmosDBConnection db = null;
        
        try{
            db = CosmosDBConnection.getInstance();
        
        //validate item exists before creating request
        items item = db.getItemById(itemId);
        if (item == null){
            return createErrorResponse("ITEM_NOT_FOUND",
                                       "Item with ID '"+ itemId + "' not found");
        }
        
        //create a unique request ID using timestamps + random number to prevent duplicates
        String requestId = "REQ-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
        
        //get current timestamps
        String timestamp = java.time.LocalDateTime.now().toString();
        
        //create request object with 'pending' status
        Request request = new Request(
                requestId,
             itemId,
             userId,
            "pending",
          timestamp
        );
        
        //Saved to request container
        Request savedRequest = db.createRequest(request);
        
        if (savedRequest == null){
            return createErrorResponse("REQUEST_FAILED",
                 "Failed to create request in database");
        }
        
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writerWithDefaultPrettyPrinter()
                     .writeValueAsString(savedRequest);
        
        }catch(Exception e){
            return createErrorResponse("SERVER_ERROR","Failed to process request: " + e.getMessage());
        }finally{
         if(db != null){  
            }
        }  
    }
        
        /**
        * Endpoint: PUT /requests/{request_id}/cancel
        * 
        * Cancel an existing rental request by updating its status to 'cancelled'
        * Request must exist in database
        * 
        * Example: PUT http://localhost:8080/RESTServices/webresources/RESTAPI/requests/REQ-1736694123456-xxxx/cancel
        * 
        * @param requestId The unique request ID to cancel
        * @return JSON response with updated request showing 'cancelled' status
         */
        @PUT
        @Path("/requests/{request_id}/cancel")
        @Produces(MediaType.APPLICATION_JSON)
        public String cancelRequest(@PathParam("request_id") String requestId){
            
            CosmosDBConnection db = null;
            
            try{
                db = CosmosDBConnection.getInstance();
                
                Request cancelledRequest = db.cancelRequest(requestId);
                
                if (cancelledRequest == null){
                    return createErrorResponse("REQUEST_NOT_FOUND", "Request with ID '" + requestId +"' not found");
                }
                
                ObjectMapper mapper = new ObjectMapper();
                return mapper.writerWithDefaultPrettyPrinter()
                             .writeValueAsString(cancelledRequest);
                
            }catch(Exception e){
                return createErrorResponse("CANCEL_FAILED", "Failed to cancel the request: " + e.getMessage());
                
            }finally{
                if(db != null){
            }
        }
    }
}
