/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author n1237155
 */
package RESTAPI;


/**
 * Route Response Model - Simplified routing response for client consumption
 * 
 * This class represents the cleaned-up response we send to clients
 * Converts complex OSRM data into user-friendly format with converted units
 * 
 * Purpose:
 * - Simplifies OSRM's complex response into just the essential information
 * - Converts units from OSRM format (meters, seconds) to user-friendly format (km, minutes)
 * - Adds status field to indicate success/failure of routing calculation
 * 
 * Data Flow:
 * 1. OSRM returns: {"distance": 5420.3, "duration": 678.2} (meters, seconds)
 * 2. We convert to: {"distanceKm": 5.42, "durationMinutes": 11.3, "status": "success"}
 * 3. Client receives user-friendly data without needing unit conversion
 * 
 * Why Separate from OSRMResponse:
 * - Decouples external API format from our API contract
 * - Allows OSRM response format to change without affecting clients
 * - Provides cleaner, more intuitive response structure
 * - Hides implementation details from API consumers
 * 
 * Used By:
 * - GET /direct - Direct distance calculation
 * - GET /items/{id}/distance - Item-specific distance calculation
 * - GET /items (with distance calculation) - Bulk distance calculations
 * 
 * @author N1237155
 */
public class RouteResponse {
    
    /*
    *Distance in meters (simplified from OSRM's response)
    *Duration in seconds (simplified from OSRM's response)
    *Status message to let the client know if the request was successful */
    private double distanceKm;
    private double durationMinutes;
    private String status;

    // Empty constructor - required for Jackson serialization
    public RouteResponse() {
    }

    // Constructor with parameters - makes it easy to create a RouteResponse object
    // We use this in RESTServices.java to quickly create our response
    public RouteResponse(double distance, double duration, String status) {
        this.distanceKm = distance;
        this.durationMinutes = duration;
        this.status = status;
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