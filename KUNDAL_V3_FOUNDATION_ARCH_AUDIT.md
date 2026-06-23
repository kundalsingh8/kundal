# KUNDAL SECURITY OS V3 – ARCHITECTURE AUDIT & FOUNDATION BLUEPRINT

This document serves as the master blueprint, system audit, and single source of truth for the **Kundal Security OS V3** platform transition. It addresses the architectural requirements of a multi-tenant Security Agency Enterprise Resource Planning (ERP) platform designed to scale from 10 to 10,000+ guards.

---

## 1. System & Architecture Diagrams

### System Architecture Diagram (High-Level)

```
+-------------------------------------------------+
|               KUNDAL SECURITY V3                |
|               Multi-Tenant Gateway              |
+-------------------------------------------------+
                        |
       +----------------+----------------+
       |                                 |
+--------------+                 +---------------+
|  Web Admin   |                 |   Guard       |
|  Portal UI   |                 |   Mobile App  |
|  (Next.js)   |                 |   (Kotlin /   |
|              |                 |   Compose)    |
+--------------+                 +---------------+
       |                                 |
       +----------------+----------------+
                        |
                        v
+-------------------------------------------------+
|             Enterprise Service Layer            |
|       (Centralized RBAC Engine & Services)      |
+-------------------------------------------------+
                        |
       +----------------+----------------+
       v                                 v
+--------------+                 +---------------+
|  Cloud-Edge  |                 | Local Secure  |
|  Real-time   |                 | SQLite / Room |
|  Firestore   |                 | Edge DB       |
+--------------+                 +---------------+
```

### Frontend Architecture Diagram (Mobile & Web)

```
+-------------------------------------------------------------+
|                     PRESENTATION LAYER                      |
|                                                             |
|   +-------------------+             +-------------------+   |
|   |   Admin & Client  |             | Guard Operational |   |
|   |   Views / Modules |             | Views / Modules   |   |
|   +-------------------+             +-------------------+   |
|             \                                 /             |
|              \                               /              |
|               v                             v               |
|            +-----------------------------------+            |
|            |      Universal Dashboard Shell    |            |
|            |      (Adaptive Screen Layout)     |            |
|            +-----------------------------------+            |
+------------------------------+------------------------------+
                               | (Updates UI State)
                               v
+-------------------------------------------------------------+
|                      VIEWMODEL / STATE                      |
|                                                             |
|   +-----------------------------------------------------+   |
|   |                 EnterpriseViewModel                 |   |
|   |         Exposes StateFlows of Core Entities         |   |
|   +-----------------------------------------------------+   |
+------------------------------+------------------------------+
                               | (Dispatches Repo Operations)
                               v
+-------------------------------------------------------------+
|                      REPOSITORY LAYER                       |
|                                                             |
|   +-----------------------------------------------------+   |
|   |                 SecurityOSRepository                |   |
|   |         (Single Source of Truth abstraction)        |   |
|   +-----------------------------------------------------+   |
+------------------------------+------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|                         DATA LAYER                          |
|                                                             |
|     +-------------------+         +-------------------+     |
|     |  Local Database   |         |   Cloud Database  |     |
|     |  (SQLite / Room)  |         |     (Firestore)   |     |
|     +-------------------+         +-------------------+     |
+-------------------------------------------------------------+
```

---

## 2. Technical Debt & Audit Report (Phase 1)

**Audit Scope Assessment:**
*   **Legacy Systems Identified:** V1/V2 systems suffered from decoupled dashboard views, redundant navigation bars, inconsistent user roles, and scattered business logic.
*   **The "Area Office" Dependency:** A major structural flaw in V2 was the presence of a middle-tier `areaOfficeId` (belonging to an intermediate branch collection) that intercepted assignments between a *Company* and a *Site*. This caused:
    1.  Redundant data structures and joins.
    2.  Brittle security policies that had to account for double-ownership.
    3.  Fragmented workflows for onboarding, scheduling, and billing reporting.
*   **Duplicate UIs:** Dashboard-in-dashboard nested navigations, separate UI components for supervisors versus guards with copied code, and multiple non-synchronized database write handlers.

### Action Plan
1.  **Refactor Hierarchy:** Fully deprecate `area_office` records and `areaOfficeId` references. Flatten operations so that **Sites** belong directly to a **Company**, and Companies belong to a **Company Group**.
2.  **Consolidate Shell:** Implement one single dynamic dashboard shell. Navigation items must be derived dynamically from high-fidelity RBAC permissions, preventing view-switching boilerplate.
3.  **Clean Code Guidelines:** Implement Strict Type safety, shared entities, immutable timestamps, and transaction-driven record modifications with immediate synchronization or fail-safe local holding.

---

## 3. Database & Firestore Collection Map (Phase 2 & 9)

Every document contains the Core Auditing Fields:
*   `id` (String / Primary Key)
*   `companyGroupId` (String)
*   `companyId` (String)
*   `siteId` (String)
*   `unitId` (String - Optional)
*   `createdAt` (Long UTC Timestamp)
*   `updatedAt` (Long UTC Timestamp)
*   `createdBy` (String)
*   `updatedBy` (String)
*   `status` (String - Active, Inactive, Completed, Suspended, etc.)

### Firestore schemas (V3 Standard)

#### 1. `users`
Tracks identity, tenancy bounds, assigned role, and contact particulars.
```json
{
  "id": "usr_94827",
  "companyGroupId": "grp_alpha",
  "companyId": "cmp_centurion",
  "siteId": "site_apex_towers", // For site-specific users like Guards/Site Admins
  "name": "Jane Doe",
  "email": "jane.doe@kundalsecurity.com",
  "role": "guard", // Allowed role token
  "status": "Active",
  "createdAt": 1782012000000,
  "updatedAt": 1782012000000
}
```

#### 2. `sites`
Primary operational entity tracking site boundaries, physical coordinates, and SLAs.
```json
{
  "id": "site_apex_towers",
  "companyGroupId": "grp_alpha",
  "companyId": "cmp_centurion",
  "name": "Apex Towers Complex",
  "latitude": 37.7749,
  "longitude": -122.4194,
  "status": "Active", // Draft -> Pending Approval -> Setup -> Active -> Suspended -> Archived
  "createdAt": 1782012000000,
  "updatedAt": 1782012000000
}
```

#### 3. `attendance`
Real-time, geo-validated roster verification records.
```json
{
  "id": "att_88274a",
  "companyGroupId": "grp_alpha",
  "companyId": "cmp_centurion",
  "siteId": "site_apex_towers",
  "userId": "usr_94827",
  "clockIn": 1782028800000,
  "clockOut": 1782057600000,
  "latitudeIn": 37.7750,
  "longitudeIn": -122.4193,
  "status": "Closed",
  "createdAt": 1782028800000,
  "updatedAt": 1782057600000
}
```

#### 4. `incidents`
Log records detailing exceptions, field reports, and security events.
```json
{
  "id": "inc_4429",
  "companyGroupId": "grp_alpha",
  "companyId": "cmp_centurion",
  "siteId": "site_apex_towers",
  "reportedBy": "usr_94827",
  "title": "Unsecured Service Gate Access",
  "severity": "High", // Critical, High, Medium, Low
  "description": "Found building B loading gate unlatched post-hours. Re-secured manually.",
  "status": "Resolved",
  "createdAt": 1782032400000,
  "updatedAt": 1782034200000
}
```

#### 5. `sos`
Zero-latency priority alerts broadcasted for critical emergencies.
```json
{
  "id": "sos_10928",
  "companyGroupId": "grp_alpha",
  "companyId": "cmp_centurion",
  "siteId": "site_apex_towers",
  "triggeredBy": "usr_94827",
  "latitude": 37.7749,
  "longitude": -122.4194,
  "status": "Active", // Active, Dispatched, Resolved
  "createdAt": 1782046800000,
  "updatedAt": 1782046800000
}
```

#### 6. `audit_logs`
Cryptographically chained or append-only transactional compliance streams.
```json
{
  "id": "aud_01029",
  "companyGroupId": "grp_alpha",
  "companyId": "cmp_centurion",
  "siteId": "site_apex_towers",
  "action": "ROLE_MODIFICATION",
  "targetUserId": "usr_94827",
  "performedBy": "usr_admin_1",
  "oldValue": "guard",
  "newValue": "site_admin",
  "timestamp": 1782051200000
}
```

---

## 4. Role-Based Access Control (RBAC) Matrix (Phase 3)

The platform enforces field-level security checks based on standard roles.

| Role | Permissions Description | Allowed Navigation Nodes |
| :--- | :--- | :--- |
| **super_admin** | Complete global visibility & database override capabilities. | All (Overview, Sites, Workforce, Attendance, Visitors, Incidents, SOS, Payroll, Billing, Reports, Settings) |
| **group_admin** | Multi-tenant control bound strictly to their Company Group. | All (Overview, Sites, Workforce, Attendance, Visitors, Incidents, SOS, Payroll, Billing, Reports, Settings) |
| **company_admin**| Single-company operations, compliance, payroll, billing logs. | Overview, Sites, Workforce, Attendance, Visitors, Incidents, SOS, Payroll, Billing, Reports, Settings |
| **site_admin** | Controls specific Site operations, guard shifts, localized reports. | Overview, Workforce, Attendance, Visitors, Incidents, SOS, Reports, Settings |
| **guard** | Site operation compliance, attendance logs, SOS signaling. | Overview (Guard Dashboard), Attendance, Visitors, Incidents, SOS, Settings |
| **resident** | Community bulletin, local unit check, visitor pass invites. | Overview, Visitors, Incidents, Settings |
| **client_manager**| Enterprise customer executive viewing compliance, invoices. | Overview, Attendance, Incidents, SOS, Reports, Billing |
| **client_user** | Customer floor rep reviewing specific visitor and site logs. | Overview, Attendance, Visitors, Incidents |

---

## 5. Site & Workforce Foundation Lifecycle

```
[Draft] -> [Pending Approval] -> [Setup] -> [Active] -> [Suspended] -> [Archived]
```

*   **Draft**: Site entered into database. No operational rosters of guards can be created.
*   **Pending Approval**: Reviewed by Company Admin or Audits for insurance/SLA clearance.
*   **Setup**: Boundaries drawn, checkpoint QR codes associated, guard templates configured.
*   **Active**: Roster schedules go live. Real-time geo-verified clocks allowed.
*   **Suspended**: Operations frozen due to default or emergency. Access keys restricted.
*   **Archived**: Logged for history records and billing compliance. Immutable.

---

This document represents the absolute architectural guide for Kundal Security OS V3. All changes downstream must align with these schemas, security guarantees, and structural constraints.
