/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package RESTAPI;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Location Model - Represents geographic location data for rental items
 * 
 * This nested POJO is embedded within the items class to store location information
 * Maps to the "location" nested object in Cosmos DB item documents
 * 
 * Structure in Database:
 * {
 *   "item_id": "i001",
 *   "location": {
 *      "city": "London",
 *      "longitude": -0.1276,
 *      "latitude": 51.5074
 *   }
 * }
 * 
 * Purpose:
 * - Stores human-readable city name for filtering and display
 * - Stores GPS coordinates (latitude/longitude) for OSRM distance calculations
 * - Enables location-based search and proximity filtering
 * 
 * OSRM Integration:
 * - longitude and latitude are passed to OSRM API for distance calculations
 * - Format: longitude, latitude (note the order - lon first, then lat)
 * 
 * @author N1237155
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Location {
    
    private String city;
    private double longitude;
    private double latitude;
    
    // Empty constructor
    public Location() {
    }
    
    // Constructor with parameters
    public Location(String city, double longitude, double latitude) {
        this.city = city;
        this.longitude = longitude;
        this.latitude = latitude;
    }
    
    // Getters and Setters
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}