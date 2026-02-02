/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package RESTAPI;

/**
 * Item Distance Response Model - Combines item details with calculated distance
 * 
 * This composite response class merges item information from database
 * with distance/duration data calculated via OSRM API
 * 
 * Purpose:
 * - Provides complete item information plus proximity data in single response
 * - Used when clients need both "what is the item" and "how far away is it"
 * - Eliminates need for clients to make multiple API calls
 * 
 * Data Sources:
 * - Item fields (id, name, category, etc.): From Cosmos DB items container
 * - Distance fields (distanceKm, durationMinutes): Calculated via OSRM API
 * - Status field: Indicates routing calculation success/failure
 * 
 * Used By:
 * - GET /items/{id}/distance - Single item with distance
 * - GET /items?userLat=X&userLon=Y - Multiple items with distances (paginated)
 * 
 * Design Pattern:
 * - Follows Data Transfer Object (DTO) pattern
 * - Aggregates data from multiple sources into single response
 * - Optimizes client-server communication by reducing round trips
 * 
 * @author N1237155
 */
public class ItemDistanceResponse {
    
    private String itemId;
    private String itemName;
    private String category;
    private double dailyRate;
    private String city;
    private String condition;
    private String description;
    private double distanceKm;  // in kilometers
    private double durationMinutes;  // in minutes
    private String status;

    // Empty constructor for Jackson
    public ItemDistanceResponse() {
    }

    // Constructor with all fields
    public ItemDistanceResponse(String itemId, String itemName, String category,
                                double dailyRate,String city, String condition, String description, double distance, double duration,
                                String status) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.category = category;
        this.dailyRate = dailyRate;
        this.city = city;
        this.condition = condition;
        this.description = description;
        this.distanceKm = distance;
        this.durationMinutes = duration;
        this.status = status;
    }

    // Getters and Setters
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(double dailyRate) {
        this.dailyRate = dailyRate;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distance) {
        this.distanceKm = distance;
    }

    public double getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(double duration) {
        this.durationMinutes = duration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}