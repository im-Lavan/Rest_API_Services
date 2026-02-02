/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package RESTAPI;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
/**
 * Request Model - Represents a rental request in the CycleNest platform
 * 
 * This POJO (Plain Old Java Object) maps to documents in the Cosmos DB "Requests" container
 * Used for JSON serialization/deserialization when creating and managing rental requests
 * 
 * Key Fields:
 * - id: Unique request identifier (format: REQ-{timestamp}-{random})
 * - item_id: Links request to specific rental item (also partition key in Cosmos DB)
 * - user_id: Identifies the user making the request
 * - status: Current request state ("pending", "cancelled", etc.)
 * - created_at: Timestamp when request was created
 * 
 * Jackson Annotations:
 * - @JsonIgnoreProperties: Ignores unknown JSON fields during deserialization
 * - @JsonProperty: Maps JSON field names to Java variables (e.g., "item_id" → item_id)
 * 
 * @author N1237155
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Request{
    
    private String id;
    
    @JsonProperty("item_id")
    private String item_id;
    
    @JsonProperty("user_id")
    private String user_id;
    
    private String status;
    
    @JsonProperty("created_at")
    private String created_at;
    
    // Empty constructor required for Jackson deserialization
    public Request(){
    }

    // Parameterized constructor for creating new request objects
    public Request(String id, String item_id, String user_id,String status, String created_at){
        this.id = id;
        this.item_id = item_id;
        this.user_id =  user_id;
        this.status = status;
        this.created_at = created_at;
    }

    // Getters and Setters for all fields
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }


}