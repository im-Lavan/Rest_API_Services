# CycleNest - Cloud Deployment Guide

**Student ID**: N1237155  
**Module**: COMP30231 Service-Centric and Cloud Computing

---

## Deployment Configuration

### Infrastructure
- **Platform**: Microsoft Azure
- **VM**: AssesmentVM (Standard B2s - 2 vcpus, 4 GiB memory)
- **OS**: Ubuntu 22.04 LTS (Minimal)
- **Region**: France Central
- **Public IP**: 4.251.106.173

### Software Stack
- **Server**: Apache Tomcat 9.0.58
- **Runtime**: OpenJDK 17.0.17
- **Database**: Azure Cosmos DB (NoSQL)
- **External API**: OSRM (routing)

### Network
- **HTTP**: Port 8080 (public access)
- **SSH**: Port 22 (key-based authentication)
- **Firewall**: Azure Network Security Group

---

## Live API Access

**Base URL**: `http://4.251.106.173:8080/RESTServices/webresources/RESTAPI`

**API Documentation**: `http://4.251.106.173:8080/RESTServices/`

### Quick Test Endpoints

```bash
# Get all items
curl http://4.251.106.173:8080/RESTServices/webresources/RESTAPI/items

# Get items with distance
curl "http://4.251.106.173:8080/RESTServices/webresources/RESTAPI/items?userLat=51.5074&userLon=-0.1276"

# Request an item
curl -X POST "http://4.251.106.173:8080/RESTServices/webresources/RESTAPI/items/i001/request?user_id=Alice"

# Cancel request
curl -X PUT "http://4.251.106.173:8080/RESTServices/webresources/RESTAPI/requests/REQ-{id}/cancel"
```

---

## Deployment Process

### 1. Create Azure VM

**Azure Portal → Virtual Machines → Create**

```
Subscription: Azure for Students
Resource Group: [Your choice]
VM Name: AssesmentVM
Region: France Central
Image: Ubuntu 22.04 LTS - Gen2 (Minimal)
Size: Standard B2s (2 vcpus, 4 GiB memory)
Authentication: SSH public key
Username: azureuser
```

Download and save the SSH private key file.

---

### 2. Install Software

**Connect to VM:**
```bash
ssh -i <path-to-key> azureuser@4.251.106.173
```

**Install Java 17 & Tomcat 9:**
```bash
sudo apt update
sudo apt install openjdk-17-jdk
sudo apt install tomcat9 tomcat9-admin
sudo apt-get install nano
```

---

### 3. Configure Tomcat

**Set Java Home:**
```bash
sudo nano /etc/default/tomcat9
```
Add: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64`

**Configure Admin User:**
```bash
sudo nano /etc/tomcat9/tomcat-users.xml
```
Add: `<user username="tomcat" password="pass" roles="manager-gui" />`

**Restart Tomcat:**
```bash
sudo systemctl restart tomcat9
sudo systemctl status tomcat9
```

---

### 4. Configure Azure Firewall

**Azure Portal → VM → Networking → Add inbound port rule**

```
Source: Any
Destination port: 8080
Protocol: TCP
Action: Allow
Priority: 310
Name: HTTP
```

**Enable UFW (if needed):**
```bash
sudo ufw allow from any to any port 8080 proto tcp
```

---

### 5. Deploy Application

**Option 1: Tomcat Manager (Recommended)**

1. Build WAR file in NetBeans (Clean and Build)
2. Go to: `http://4.251.106.173:8080/manager/html`
3. Login with: tomcat / pass
4. Scroll to "WAR file to deploy"
5. Choose `RESTServices.war` and click Deploy

**Option 2: WinSCP File Transfer**

1. Connect via WinSCP to 4.251.106.173
2. Protocol: SFTP, Port: 22
3. Upload WAR to: `/var/lib/tomcat9/webapps/`
4. Restart Tomcat: `sudo systemctl restart tomcat9`

**Option 3: SCP Command**

```bash
scp -i <key> RESTServices.war azureuser@4.251.106.173:/var/lib/tomcat9/webapps/
```

---

### 6. Verify Deployment

**Check deployment:**
```
http://4.251.106.173:8080/RESTServices/
```

**Test API endpoint:**
```
http://4.251.106.173:8080/RESTServices/webresources/RESTAPI/items
```

**Check Tomcat logs:**
```bash
sudo tail -f /var/log/tomcat9/catalina.out
```

---

## Common Issues & Solutions

### Issue: Tomcat Not Starting
```bash
# Check Java version
java -version

# Verify Java Home
echo $JAVA_HOME

# Check Tomcat service
sudo systemctl status tomcat9

# View error logs
sudo tail -100 /var/log/tomcat9/catalina.out
```

### Issue: Cannot Access from Browser
```bash
# Verify Tomcat is listening
ss -ltn | grep 8080

# Check firewall rules
sudo ufw status

# Verify Azure NSG rule for port 8080
```

### Issue: Application Not Deploying
```bash
# Check deployment status
ls -la /var/lib/tomcat9/webapps/

# View deployment logs
sudo tail -f /var/log/tomcat9/catalina.out

# Check file permissions
sudo chown tomcat:tomcat /var/lib/tomcat9/webapps/RESTServices.war
```

### Issue: Database Connection Failure
- Verify Cosmos DB endpoint and key in source code
- Check network connectivity from VM to Azure Cosmos DB
- Ensure Cosmos DB allows access from all networks or VM's IP

---

## Maintenance Commands

**Tomcat Management:**
```bash
sudo systemctl start tomcat9       # Start Tomcat
sudo systemctl stop tomcat9        # Stop Tomcat
sudo systemctl restart tomcat9     # Restart Tomcat
sudo systemctl status tomcat9      # Check status
```

**View Logs:**
```bash
sudo tail -f /var/log/tomcat9/catalina.out        # Live log view
sudo tail -100 /var/log/tomcat9/catalina.out      # Last 100 lines
sudo nano /var/log/tomcat9/catalina.out           # Open in editor
```

**Undeploy Application:**
```bash
sudo rm -rf /var/lib/tomcat9/webapps/RESTServices*
sudo systemctl restart tomcat9
```

**VM Management:**
```bash
# Via Azure Portal
Stop VM:     Azure Portal → VM → Stop
Start VM:    Azure Portal → VM → Start
Restart VM:  Azure Portal → VM → Restart
```

---

## Security Considerations

### Implemented
✅ SSH key-based authentication (no password login)  
✅ Azure Network Security Group rules  
✅ Tomcat admin credentials  
✅ Database credentials not hardcoded (environment variables recommended)

### Production Recommendations
- Use HTTPS with SSL certificate (Let's Encrypt)
- Implement API authentication 
- Enable Azure DDoS protection
- Regular security updates: `sudo apt update && sudo apt upgrade`
- Change default Tomcat admin password
- Restrict SSH access to specific IPs

---

## Testing Evidence Location

All testing evidence and screenshots are stored in:
```
/Testing Evidence/
├── Part C - QoS Testing/
│   ├── JMeter Testing Evidence/
│   └── All End Points With Evidence/
└── Part D - Cloud Deployment/
    ├── All Endpoints Tested/
    └── VM Configurations/
```

---

## Performance Metrics

**Load Testing Results** (After optimization):
- Concurrent users tested: 50, 100, 200, 500, 1000
- Average response time: ~100ms
- Throughput: 99 requests/sec
- Error rate: 0%
- Successfully handles 1000+ concurrent users

**Bottlenecks Resolved**:
1. Cosmos DB connection pooling (singleton pattern)
2. HTTP client connection reuse for OSRM API calls

See `/Testing Evidence/Bottle Necks/` for detailed analysis.

---

## SSH Connection

**Connect to VM:**
```bash
ssh -i azureuser.pem azureuser@4.251.106.173
```

**File Transfer (WinSCP):**
- Host: 4.251.106.173
- Port: 22
- Protocol: SFTP
- Username: azureuser
- Authentication: Use SSH key file

---

## Quick Reference

| Component | Value |
|-----------|-------|
| Public IP | 4.251.106.173 |
| HTTP Port | 8080 |
| SSH Port | 22 |
| SSH User | azureuser |
| Tomcat Admin | tomcat / pass |
| Webapps Dir | /var/lib/tomcat9/webapps/ |
| Log File | /var/log/tomcat9/catalina.out |
| Config | /etc/tomcat9/ |

---

## Acknowledgments

**Module**: COMP30231 Service-Centric and Cloud Computing  
**University**: Nottingham Trent University  
**Academic Year**: 2025-2026

**Module Team**:
- Ismahane Cheheb (Module Leader)
- Brad Patrick
- Daniyal Haider
- Taha Osman

**Technologies**:
- Microsoft Azure (Cloud infrastructure)
- Apache Tomcat (Application server)
- Azure Cosmos DB (NoSQL database)
- OSRM Project (Routing API)

---

**End of Deployment Guide**
