/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author n1237155
 */
package RESTAPI;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/*
 * OSRM API Response Model - Deserializes JSON from OSRM routing service
 * 
 * This class maps to the JSON response structure returned by the OSRM API
 * Used exclusively for deserialization - converts external API JSON into Java objects
 * 
 * OSRM Response Structure:
 * {
 *   "code": "Ok",
 *   "routes": [
 *     {
 *       "distance": 5420.3,
 *       "duration": 678.2
 *     }
 *   ]
 * }
 * 
 * Purpose:
 * - Captures the complete OSRM API response format
 * - code field indicates success/failure ("Ok" = successful routing)
 * - routes array contains possible route options (we use the first one)
 * 
 * Why Nested Class:
 * - RouteInfo is nested because it only exists within an OSRM response
 * - Keeps related structures together for better code organization
 * - Mirrors the nested JSON structure from OSRM
 * 
 * Jackson Strategy:
 * - @JsonIgnoreProperties allows OSRM to add new fields without breaking our code
 * - Only deserializes the fields we need (code, routes with distance/duration)
 * - Ignores additional OSRM fields like waypoints, geometry, etc.
 * 
 * @author N1237155
*/

@JsonIgnoreProperties(ignoreUnknown = true)
public class OSRMResponse {
    
    // The "code" field from OSRM's response (e.g., "Ok" if successful)
    // The "routes" field contains an array of possible routes
    // We use a List to store multiple RouteInfo objects
    private String code;
    private List<RouteInfo> routes;

    // Empty constructor required by Jackson for deserialization
    public OSRMResponse() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<RouteInfo> getRoutes() {
        return routes;
    }

    public void setRoutes(List<RouteInfo> routes) {
        this.routes = routes;
    }

    /**
     * RouteInfo - Nested class representing individual route details
     * 
     * Maps to each object in the "routes" array from OSRM response
     * Contains the core routing information we need: distance and duration
     * 
     * Units from OSRM:
     * - distance: meters (we convert to kilometers)
     * - duration: seconds (we convert to minutes)
     * 
     * Static nested class because:
     * - Doesn't need reference to outer OSRMResponse instance
     * - Can be instantiated independently if needed
     * - More memory efficient than inner class
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RouteInfo {
        private double distance;
        private double duration;

        // Empty constructor required for Jackson deserialization
        public RouteInfo() {
        }

        public double getDistance() {
            return distance;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }

        public double getDuration() {
            return duration;
        }

        public void setDuration(double duration) {
            this.duration = duration;
        }
    }
}