/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package RESTAPI;

import java.util.Set;

/**
 * JAX-RS Application Configuration Class
 * 
 * This class serves as the entry point for the REST application, configuring
 * which REST resource classes are available to clients. It extends the standard
 * JAX-RS Application class to customize REST service registration.
 * 
 * Key Responsibilities:
 * - Defines the base URL path for all REST endpoints ("/webresources")
 * - Registers all REST resource classes with the server
 * - Enables automatic service discovery by the application server
 * 
 * URL Structure:
 * Base: http://localhost:8080/RESTServices
 * + Application Path: /webresources
 * + Resource Path: /RESTAPI
 * = Full endpoint: http://localhost:8080/RESTServices/webresources/RESTAPI/...
 * 
 * The @ApplicationPath annotation is critical - it tells the server where
 * to mount all REST services defined in this application.
 * 
 * @author N1237155
 */
@javax.ws.rs.ApplicationPath("webresources")
public class ApplicationConfig extends javax.ws.rs.core.Application {

    // This method is called by the server to get a list of all REST service classes
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        // Add all REST service classes to the set
        addRestResourceClasses(resources);
        // Return the complete set of REST services
        return resources;
    }

   /*
     * Register all REST resource classes with the application
     * 
     * This method maintains the registry of all REST services that should
     * be exposed to clients. Each resource class added here becomes accessible
     * via HTTP requests.
     * 
     * NetBeans IDE Management:
     * - NetBeans automatically updates this method when you create new REST resources
     * - Manual modifications may be overwritten by the IDE
     * - Always use NetBeans wizards to create new REST services for automatic registration
     * 
     * Current Registered Services:
     * - RESTServices.class: Main orchestrator for CycleNest rental platform
     *   Handles item search, distance calculation, and request management
     * 
     * @param resources The set to populate with REST resource classes
   */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        // Register our RESTServices class so the server knows it exists
        // Without this line, clients wouldn't be able to access our REST service
        resources.add(RESTAPI.RESTServices.class);
    }
    
}
