# CycleNest REST API

A RESTful web service enabling sustainable item rental through a sharing economy platform. Users can browse items, calculate distances, apply filters, and manage rental requests.

**Student ID**: N1237155  
**Module**: COMP30231 Service-Centric and Cloud Computing  
**University**: Nottingham Trent University

<img width="906" height="476" alt="Screenshot 2026-02-02 095508" src="https://github.com/user-attachments/assets/a33b85f0-1bea-40f0-874d-c44a1a908116" />


---

## Quick Access

### 🌐 Local Development
```
http://localhost:8080/RESTServices/
```

### ☁️ Cloud Deployment
```
http://{Your VM IP}:8080/RESTServices/
```

Replace `{Your VM IP}` with your Azure VM's public IP address.

Both URLs provide interactive API documentation with click-to-copy endpoints.

---

## Coursework Parts Completed

### Part A: REST Orchestrator Implementation
Built a fully functional RESTful API with JAX-RS that handles:
- Item retrieval with filtering (category, city, condition)
- Pagination (5 items per page)
- Rental request creation and cancellation
- JSON serialization/deserialization using Jackson
- Connection pooling for Azure Cosmos DB

### Part B: External API Integration
Integrated OSRM routing API for real-time distance and duration calculations:
- Direct coordinate-to-coordinate distance
- User-to-item distance calculation
- Comprehensive error handling for external service failures
- Singleton HttpClient for connection pooling

### Part C: QoS Analysis & Performance Optimization
Conducted systematic JMeter load testing and implemented two critical performance improvements.

**Git Tag**: `pre-qos baseline` marks the codebase before performance improvements were applied.

**Bottleneck 1: Cosmos DB Connection Exhaustion**
- Issue: Creating new database connections per request caused system failure at 200 concurrent users
- Solution: Singleton pattern with connection pooling
- Result: 99.7% latency reduction, 0% error rate at 1000+ users

**Bottleneck 2: HTTP Client Resource Exhaustion**
- Issue: New HttpClient per OSRM call caused 50% failure rate under load
- Solution: Shared HttpClient with automatic connection reuse
- Result: 99.89% faster responses (111s → 0.1s), 0% errors at 1000 users

Testing evidence and detailed analysis available in `/Testing Evidence/` directory.

### Part D: Cloud Deployment
Successfully deployed to Azure with:
- Ubuntu 22.04 VM (Standard B2s)
- Apache Tomcat 9 with Java 17
- Azure Cosmos DB for NoSQL storage
- Public accessibility via HTTP port 8080

See [DEPLOYMENT_README.md](DEPLOYMENT_README.md) for complete deployment documentation.

---

## Architecture

**Frontend**: HTML5 API documentation page (dark theme)
**Backend**: JAX-RS REST services
**Database**: Azure Cosmos DB (NoSQL)
**External APIs**: OSRM (routing)
**Server**: Apache Tomcat 9
**Runtime**: Java 17

---

## Azure Cosmos DB Setup

Before running the application, you must create an Azure Cosmos DB account and configure the connection.

### 1. Create Azure Cosmos DB Account

1. Go to [Azure Portal](https://portal.azure.com)
2. Search for "Azure Cosmos DB" and click "Create"
3. Select **Azure Cosmos DB for NoSQL**
4. Configure:
   - **Subscription**: Your Azure subscription
   - **Resource Group**: Create new or use existing
   - **Account Name**: Choose a unique name
   - **Location**: Choose a region close to your VM
   - **Capacity mode**: Serverless (for development) or Provisioned throughput

### 2. Create Database and Containers

After the account is created:

1. Go to your Cosmos DB account → **Data Explorer**
2. Click **New Database** and create a database named: `Coursework`
3. Create two containers:
   - **Container 1**: `items` with partition key `/item_id`
   - **Container 2**: `Requests` with partition key `/item_id`

### 3. Get Your Endpoint and Key

1. Go to your Cosmos DB account → **Keys** (under Settings)
2. Copy the following values:
   - **URI** (Endpoint): `https://your-account-name.documents.azure.com:443/`
   - **PRIMARY KEY**: Your secret key

### 4. Configure the Application

Open `src/java/RESTAPI/CosmosDBConnection.java` and update:

```java
private static final String ENDPOINT = "https://your-account-name.documents.azure.com:443/";
private static final String KEY = "your-primary-key-here";
```

**Security Note**: For production, store credentials in environment variables instead of hardcoding them.

---

## Testing Links

### Localhost Endpoints

**Distance Calculation**
```
http://localhost:8080/RESTServices/webresources/RESTAPI/direct?startLon=-0.1276&startLat=51.5074&endLon=-2.2426&endLat=53.4808
```

**Get All Items**
```
http://localhost:8080/RESTServices/webresources/RESTAPI/items
```

**Get Items with Distance**
```
http://localhost:8080/RESTServices/webresources/RESTAPI/items?userLat=51.5074&userLon=-0.1276
```

**Pagination**
```
http://localhost:8080/RESTServices/webresources/RESTAPI/items?page=1
```

**Filtering (multiple filters supported)**
```
http://localhost:8080/RESTServices/webresources/RESTAPI/items?category=Tools&city=London&condition=Excellent
```

**Request Item**
```bash
curl -X POST "http://localhost:8080/RESTServices/webresources/RESTAPI/items/i001/request?user_id=Alice"
```

**Cancel Request**
```bash
curl -X PUT "http://localhost:8080/RESTServices/webresources/RESTAPI/requests/REQ-{request_id}/cancel"
```

### Cloud Deployment Endpoints

Replace `localhost:8080` with `{Your VM IP}:8080` in any of the above URLs.

**Example**:
```
http://{Your VM IP}:8080/RESTServices/webresources/RESTAPI/items
```

---

## Repository Structure

```
├── src/RESTAPI/                    # Java source files
│   ├── RESTServices.java           # Main REST endpoints
│   ├── CosmosDBConnection.java     # Database connection (singleton)
│   ├── items.java                  # Item data model
│   ├── Request.java                # Request data model
│   └── [other models]              # Response models
├── web/
│   ├── index.html                  # API documentation page
│   └── WEB-INF/lib/                # JAR dependencies
├── Testing Evidence/               # JMeter tests & QoS analysis
│   ├── Bottle Necks/              # Performance issue documentation
│   ├── Part C - QoS Testing/      # Load testing results
│   └── Part D - Cloud Deployment/ # Deployment screenshots
├── DEPLOYMENT_README.md            # Detailed deployment guide
└── README.md                       # This file
```

---

## Technologies

- **Java 17** - Runtime environment
- **JAX-RS** - RESTful web services
- **Jackson** - JSON processing
- **Azure Cosmos DB SDK** - NoSQL database client
- **Apache Tomcat 9** - Application server
- **OSRM API** - Distance/routing calculations
- **JMeter** - Load testing and QoS analysis

---

## Key Features

✅ RESTful API following REST principles  
✅ Cloud-deployed with public accessibility  
✅ Real-time distance calculations via OSRM  
✅ Advanced filtering and pagination  
✅ Performance-optimized with connection pooling  
✅ Comprehensive error handling  
✅ Load-tested up to 1000+ concurrent users  
✅ Interactive API documentation

---

## Performance Metrics

**After Optimization:**
- Average response time: **~100ms**
- Throughput: **99 requests/sec**
- Error rate: **0%** (at 1000 concurrent users)
- Database connection overhead: **Eliminated**
- External API call efficiency: **1000× improvement**

---

**© 2026 | Developed for COMP30231 Service-Centric and Cloud Computing | Nottingham Trent University**
