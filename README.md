# Scan2Dine 🍽️

> **Tagline:** Smart Barcode-Based Hostel Dining Management Platform  
> **Type:** Multi-Tenant SaaS (Shared Database + Shared Schema Isolation)

Scan2Dine is a multi-tenant cloud application that enables colleges to manage hostel dining attendance using students' existing College ID Card barcodes. It eliminates paperwork, audits queue times, configurations dining windows, blocks duplicate entries, and generates PDF/Excel consumption reports.

---

## Technical Stack
- **Backend Core**: Java 21, Spring Boot 3.3.2, Spring Security (JWT Stateless Authentication)
- **Persistence**: Spring Data JPA, Hibernate (with Tenant Filtering Aspect), MySQL
- **Reports**: OpenPDF (PDF creation), Apache POI (Excel spreadsheet creation)
- **Documentation**: Springdoc OpenAPI (Swagger Web UI)
- **Frontend client**: Single Page HTML5 App with light theme styling (Vanilla CSS, dynamic theme variables, native fetch APIs)

---

## Package Architecture Structure
```text
com.scan2dine.api
 ├── config            # TenantContext, TenantAspect, DataSeeder, SpringDocConfig
 ├── security          # JwtTokenProvider, JwtAuthenticationFilter, TenantFilter, CustomUserDetails, CustomUserDetailsService, WebSecurityConfig
 ├── entity            # BaseEntity, College, User, Student, Hostel, Room, Meal, Attendance, BarcodeRegistration, Notification, Report
 ├── repository        # JpaRepositories
 ├── dto
 │    ├── request      # Request payload models (Login, Register, StudentRequest, etc.)
 │    └── response     # Response payloads (AuthResponse, ApiResponse, DashboardResponse, etc.)
 ├── mapper            # Domain entity to DTO mapping layer
 ├── service           # Service interfaces
 ├── service.impl      # Service implementation logic
 ├── controller        # REST Controllers (Auth, College, Student, ERP, Barcode, Reports, etc.)
 ├── exception         # ResourceNotFound, BadRequestException, GlobalExceptionHandler
 └── integration.erp   # ERP simulated integration module (Campus7, Fedena, CAMU, Academia)
```

---

## Seeder Login Accounts (Out-of-the-Box Demo)
The application has a built-in startup `DataSeeder` that will populate sample data if the tables are empty.

| Role | Username | Password | Context | Description |
|---|---|---|---|---|
| **Super Admin** | `superadmin` | `superadmin123` | Global Platform | Manages plans, registers/approves colleges, sees platform analytics. |
| **College Admin** | `collegeadmin` | `collegeadmin123` | `ECE101` College | Configures ERP, maps barcodes, registers hostels, rooms, wardens. |
| **Warden** | `warden` | `warden123` | `ECE101` College | Operates scanner simulation console, views scan logs history. |

---

## Setup & Running Guide

### 1. Database Setup
Make sure you have a MySQL server running. Create the database named `scan2dine` (or it will be automatically created on startup if permissions allow):
```sql
CREATE DATABASE IF NOT EXISTS `scan2dine` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Update your database username and password in [application.properties](file:///c:/Users/Ariharan/Downloads/Saas/src/main/resources/application.properties) if necessary.

### 2. Launch the Application
Run the Maven spring-boot plugin from the project root:
```bash
mvn spring-boot:run
```

### 3. Open the Frontend Interface
Open your web browser and navigate to:
👉 **[http://localhost:8080/](http://localhost:8080/)**

The single-page dashboard serves from the static classpath and is immediately interactive.

---

## API Documentation (Swagger)
View the complete interactive OpenAPI schema specifications at:
👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

---

## Testing Scenarios walkthrough

### Scenario A: Warden Meal Scanning Simulator
1. Log in with warden credentials (`warden` / `warden123`).
2. Go to the **Scan Console** tab.
3. Try typing the barcode of a seeded student: `9876543210` (mapped to student *Rahul Sharma*).
4. Click **VERIFY & RECORD SCAN**:
   - If a meal timing slot is active right now (e.g. current system time matches Breakfast 7:00-9:30, Lunch 12:00-14:30, or Dinner 19:30-22:00), you will see a large green **ACCESS GRANTED** banner.
   - If no meal is active, it will return **ACCESS DENIED: No active meal session**.
   - If you scan the same barcode **twice** during the same active meal, it will block the request and return **ACCESS DENIED: Duplicate scan attempt**, and create an alert log.

### Scenario B: ERP Synchronizer & Barcode Mapping
1. Log in with college administrator credentials (`collegeadmin` / `collegeadmin123`).
2. Go to the **ERP Synchronizer** tab. The mock connection will show "Connected to Campus7".
3. In the "Sync Student Profile" box, type roll number `20ECE03` and click **Fetch & Sync Student**.
4. The system simulated search pulls the student from Campus7 and registers them into the local database.
5. Go to **Manage Students** tab. You'll see the synced student in the table, but with a **Map Barcode** button instead of a barcode value.
6. Click **Map Barcode**, type a new barcode (e.g. `9876543212`), and save. The card is now active and ready for the Warden scan simulation!

### Scenario C: Branding Customizations & Exports
1. College Admins can configure theme colors (e.g. register a new college with `#e11d48` Rose or `#16a34a` Green).
2. When users belonging to that college log in, the dashboard dynamically adapts to their tenant branding!
3. Go to the **Exports & Reports** tab on the College Admin dashboard, select a date range, and click **Export Excel** or **Download PDF** to download complete check-in reports built in POI/OpenPDF.
