/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package RESTAPI;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;
import java.util.ArrayList;
import java.util.List;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.CosmosItemRequestOptions;

/**
 * Azure Cosmos DB Connection Handler
 * 
 * Manages all database operations for the CycleNest rental platform including:
 * - Connection initialization to Azure Cosmos DB
 * - Item retrieval (single and bulk operations)
 * - Rental request creation with proper partition key handling
 * - Request cancellation with status updates
 * 
 * Database Structure:
 * - Database: "Coursework"
 * - Containers: "items" (partition key: /item_id), "Requests" (partition key: /item_id)
 * 
 * @author N1237155
 */
public class CosmosDBConnection {
    
    // Replace with your Azure Cosmos DB connection credentials
    // These should be stored in environment variables in production for security
    private static final String ENDPOINT = ""; 
    private static final String KEY = " "; 
    private static final String DATABASE_NAME = "Coursework"; 
    private static final String CONTAINER_NAME = "items"; 
    private static final String REQUESTS_CONTAINER_NAME = "Requests";
    
    // Singleton instance - shared across all threads
    private static volatile CosmosDBConnection instance;
    
    // Azure Cosmos DB client objects - shared across all requests
    private final CosmosClient client;
    private final CosmosDatabase database;
    public final CosmosContainer container;
    public final CosmosContainer requestsContainer;
    
    /**
     * Constructor - Establishes connection to Azure Cosmos DB
     * 
     * Initializes the Cosmos client using the builder pattern and connects to:
     * 1. The Coursework database
     * 2. The items container (for rental items)
     * 3. The Requests container (for rental requests)
     * 
     * This connection is established once per CosmosDBConnection instance
     * and should be closed after use via the close() method
     */
    private CosmosDBConnection() {
        this.client = new CosmosClientBuilder()
                .endpoint(ENDPOINT)
                .key(KEY)
                .buildClient();
        
        this.database = client.getDatabase(DATABASE_NAME);
        this.container = database.getContainer(CONTAINER_NAME);
        this.requestsContainer = database.getContainer(REQUESTS_CONTAINER_NAME);
    }
    
    /**
 * Get the singleton instance of CosmosDBConnection
 * 
 * Thread-safe lazy initialization using double-checked locking.
 * First request creates the instance, all subsequent requests reuse it.
 * 
 * @return The single shared CosmosDBConnection instance
 */
public static CosmosDBConnection getInstance() {
    if (instance == null) {
        synchronized (CosmosDBConnection.class) {
            if (instance == null) {
                instance = new CosmosDBConnection();
                System.out.println("CosmosDB Connection initialized (singleton)");
            }
        }
    }
    return instance;
}
    
    /**
      * Retrieve a single rental item by its unique ID
     * 
     * Uses Cosmos DB SQL query to find items matching the provided ID
     * Returns the first matching item (IDs should be unique)
     * 
     * @param item_id The unique identifier for the item (e.g., "i001")
     * @return Item object containing all item details including location coordinates,
     * or null if item not found or database error occurs
     */
    public items getItemById(String item_id) {
        try {
            // SQL query to find item by ID
            String query = "SELECT * FROM c WHERE c.item_id = '" + item_id + "'";
            
            // Execute query against items container
            // CosmosPagedIterable allows efficient iteration over results
            CosmosPagedIterable<items> itemsList = container.queryItems(
                query, 
                new CosmosQueryRequestOptions(), 
                items.class
            );
            
            // Return first item found (should only be one with this ID)
            for (items item : itemsList) {
                return item;
            }
            
            // Item not found
            return null;
            
        } catch (Exception e) {
            System.err.println("Database error: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Retrieve all rental items from the database
     * 
     * Fetches the complete catalog of rental items without filtering
     * Used by the GET /items endpoint for browsing and searching
     * 
     * @return List of all items in the database, or empty list if error occurs
     */
    public List<items> getAllItems(){
        try{
            // Query to select all documents in the container
            String query="SELECT * FROM c";
            
            // Execute query and get paginated results
            CosmosPagedIterable<items>itemsList = container.queryItems(
                    query, 
                    new CosmosQueryRequestOptions(), 
                    items.class
            );
            
            // Convert paginated results into standard Java List
            List<items> allItems = new ArrayList<>();
            for (items item : itemsList){
                allItems.add(item);
            }
            
            return allItems;
            
        }catch (Exception e){
            System.err.println("Database error: " + e.getMessage());
            return new ArrayList<>();// Return empty list instead of null for safer handling
        }
    }
    
    /**
     * Create a new rental request in the Requests container
     * 
     * Saves a rental request with 'pending' status to Cosmos DB
     * Uses explicit partition key (item_id) to ensure proper data distribution
     * 
     * Partition Key Strategy:
     * - Requests are partitioned by item_id (not request id)
     * - This groups all requests for the same item together
     * - Enables efficient queries like "show all requests for item X"
     * 
     * @param request The Request object containing id, item_id, user_id, status, created_at
     * @return The saved Request object, or null if save operation failed
     */
    
    public Request createRequest(Request request){
        try{
            // Create item in Requests container with explicit partition key
            // Partition key must match container configuration (/item_id)
            requestsContainer.createItem(
                    request,
                    new PartitionKey(request.getItem_id()), // Partition by item_id
                    new CosmosItemRequestOptions()
            );
            System.out.println("Successfully created request: " + request.getId());
            return request;
            
        }catch(Exception e ){
        System.err.println("Error creating request: "+ e.getMessage());
        System.err.println("Request ID was: " + request.getId());
        e.printStackTrace();
        return null;
        }
    }
    
    /**
     * Cancel a rental request by updating its status to "cancelled"
     * 
     * Process:
     * 1. Query Requests container to find request by unique ID
     * 2. Update the status field from "pending" to "cancelled"
     * 3. Use replaceItem() to save changes back to database
     * 
     * Partition Key Handling:
     * - replaceItem() requires the partition key value (item_id)
     * - This is critical: using wrong partition key causes operation to fail
     * - Must match the partition key used during createRequest()
     * 
     * @param requestId The unique request ID to cancel (e.g., "REQ-1736694123456-7845")
     * @return Updated Request object with status="cancelled", or null if not found
     */
    
    public Request cancelRequest(String requestId){
        try{
            //query to find the request by request id
            String query = "SELECT * FROM c WHERE c.id = '" +  requestId + "'";
            
            // Execute query against Requests container
            CosmosPagedIterable<Request> requests = requestsContainer.queryItems(
                    query,
                    new CosmosQueryRequestOptions(),
                    Request.class
            );
            
            //find the request
            for(Request request : requests){
                request.setStatus("cancelled"); // Update status field
                
                // Save changes back to database using replaceItem()
                // Must provide correct partition key
                requestsContainer.replaceItem(
                        request,                           
                        request.getId(),                  
                        new PartitionKey(request.getItem_id()),// Partition by item_id
                        new CosmosItemRequestOptions()
                );
                
                System.out.println("Successfully cancelled request: " + requestId);
                return request;
            }
            
            System.err.println("Request not found: " + requestId);
            return null;
            
        }catch(Exception e){
            System.err.println("Error cancelling request: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
    * Close the database connection and release resources
    * 
    * Should be called in a finally block after database operations complete
    * Ensures proper cleanup of Azure Cosmos DB client connections
    * Prevents resource leaks and connection pool exhaustion
    */
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
