/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package RESTAPI;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Item Model - Represents a rental item in the CycleNest platform
 * 
 * This POJO maps to documents in the Cosmos DB "items" container
 * Each item represents a physical object available for rental (bikes, camping gear, tools, etc.)
 * 
 * Key Features:
 * - Stores item details: name, category, condition, daily rate, description
 * - Contains nested Location object with city and GPS coordinates
 * - Includes transient fields (distance, duration) calculated at runtime via OSRM
 * 
 * Database Integration:
 * - item_id serves as both unique identifier and partition key in Cosmos DB
 * - Location is stored as nested JSON object in database
 * - Available field tracks rental availability status
 * 
 * OSRM Integration:
 * - distance and duration fields are NOT stored in database
 * - Calculated dynamically when user provides their coordinates
 * - Used to show "how far away" items are from user's location
 * 
 * Jackson Annotations:
 * - @JsonProperty: Maps database field names to Java variables
 * - @JsonIgnoreProperties: Allows database schema evolution without breaking code
 * 
 * @author N1237155
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class items {
    
    // Item details from database
    @JsonProperty("item_id")
    private String item_id;
    
    private String owner_id;
    private String name;
    private String category;
    private Location location; // Nested object containing city and coordinates
    
    @JsonProperty("daily_rate")
    private double daily_rate;
    
    private boolean available;
    private String condition;
    private String description;
    
    // Transient fields calculated at runtime via OSRM API
    // Not stored in database - only included in API responses    
    private Double distance;
    private Double duration;
    
    // Empty constructor required for Jackson
    public items() {
    }
    
    // Constructor with all fields
    public items(String item_id, String owner_id, String name, String category,Location location, double daily_rate, 
                boolean availability, String condition, String description) {
        this.item_id = item_id;
        this.owner_id = owner_id;
        this.name = name;
        this.category = category;
        this.location = location;
        this.daily_rate = daily_rate;
        this.available = availability;
        this.condition = condition;
        this.description = description;
    }

    // Getters and Setters
    @JsonProperty("item_id")
    public String getId() {
        return item_id;
    }

    public void setId(String id) {
        this.item_id = id;
    }

    public String getOwnerId() {
        return owner_id;
    }

    public void setOwnerId(String ownerId) {
        this.owner_id = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public double getDailyRate() {
        return daily_rate;
    }

    public void setDailyRate(double dailyRate) {
        this.daily_rate = dailyRate;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean availability) {
        this.available = available;
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

    // Convenience method to get city from nested location object
    // Returns null if location not set (null-safe)
    public String getCity() {
    return location != null ? location.getCity() : null;
    }

    // Convenience method to get latitude from nested location object
    // Returns 0.0 if location not set (prevents NullPointerException)
    public double getLatitude() {
    return location != null ? location.getLatitude() : 0.0;
    }

    // Convenience method to get longitude from nested location object
    // Returns 0.0 if location not set (prevents NullPointerException)
    public double getLongitude() {
    return location != null ? location.getLongitude() : 0.0;    
    }
    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }
}