# KUNDAL SECURITY OS V3 – MASTER ONBOARDING PROCESS BLUEPRINT
**Revision:** 3.0.0-ONB  
**Status:** APPROVED  
**Date:** June 22, 2026  
**Target Audience:** General Managers, SecOps Supervisors, Tenant Provisioning Teams, HR Administrators  

---

## 1. STRATEGIC GOAL: MASTER DATA DEPENDENCY SANITY
In legacy security ERP installations, operational tasks (such as attendance posting, incident reports processing, and invoicing) frequently suffered metadata errors. This occurred because transaction records were created without valid parent relations. 

We resolve this by enforcing a **One-Way Master Data Onboarding Flow**. Operation activation functions are hard-locked until the 10 preceding sequential master records are successfully provisioned:

```
[1. Company Onboarding] ➔ [2. Site Onboarding] ➔ [3. Client Onboarding]
                                                         │
[6. Resident Onboarding] ◄ [5. Guard Onboarding] ◄ [4. Staff Onboarding]
        │
[7. Unit Onboarding] ➔ [8. Asset Onboarding] ➔ [9. Contract Onboarding]
                                                         │
[11. OPERATIONS ACTIVATION] ◄ [10. Compliance Onboarding]
```

---

## 2. DETAILED ONBOARDING LIFECYCLE SCHEMAS

### Phase 1: Company Onboarding (Tenant Provisioning)
Establishes the primary container. A tenant company group coordinates sub-entities, taxes, billing limits, and system subscription models.

```
                      +-----------------------------+
                      |    Tenant Provisioning      |
                      +-----------------------------+
                                     │
                 [State: Draft ➔ Configured ➔ Active]
                                     │
      ├─ GST Register (Tax Number, Regional Fiscal Rules)
      ├─ Operational Bounds (Custom Shift Roster Limits)
      └─ Subscription Thresholds (Max Guards, Max Active Sites)
```

#### Document Schema Pattern
*   **Path**: `/companies/{companyId}`
*   **Target State Properties**:
    ```json
    {
      "id": "cmp_centurion_sec",
      "companyGroupId": "grp_alpha_conglomerate",
      "name": "Centurion Security Services",
      "status": "Configured", // Draft -> Configured -> Active
      "taxSettings": {
        "vatTaxNumber": "GST-SG-1938502A",
        "regionalTaxRate": 0.08
      },
      "payrollSettings": {
        "standardHourlyRate": 22.50,
        "overtimeMultiplier": 1.5
      },
      "subscriptionSettings": {
        "maxPermittedSites": 30,
        "maxPermittedGuards": 500
      }
    }
    ```

---

### Phase 2: Site Onboarding (Operations Yard Provisioning)
A client operations Site requires spatial boundary configuration, shift schedules creation, and local coordinate bindings to allow clock-ins.

```
                +-----------------------------------------+
                |     Site Provisioning Wizard            |
                +-----------------------------------------+
                                     │
               [Site Details ➔ Radius GPS Coordinates]
                                     │
        ├─ Shift Templates Creation (Day Shift, Night Shift)
        ├─ Attendance Geofencing Parameters (Radius in meters)
        └─ Readiness Tracker (Checklist state before active)
```

#### Document Schema Pattern
*   **Path**: `/sites/{siteId}`
*   **Target State Properties**:
    ```json
    {
      "id": "site_maritime_plaza",
      "companyId": "cmp_centurion_sec",
      "name": "Maritime Plaza Operations",
      "status": "APPROVED",
      "geoConfig": {
        "latitude": 1.294820,
        "longitude": 103.858390,
        "validationRadiusMeters": 50.0
      },
      "shifts": [
        { "shiftId": "sh_maritime_day", "startTime": "07:00", "endTime": "19:00" },
        { "shiftId": "sh_maritime_night", "startTime": "19:00", "endTime": "07:00" }
      ],
      "readinessChecklist": {
        "qrStandsInstalled": true,
        "cellularCoverageTested": true,
        "supervisorAssigned": true
      }
    }
    ```

---

### Phase 3: Client Onboarding (Sponsor Setup)
Establishes clear corporate accountability. Assigns client account managers, notification priorities, communication channels, and maps contracts to operations yards.

```json
{
  "id": "cl_maritime_holdings",
  "name": "Maritime Properties Ltd",
  "escalationContacts": [
    { "name": "Douglas Lim", "phone": "+6591827492", "role": "VP Operations" }
  ],
  "siteBindings": ["site_maritime_plaza"],
  "portalAccessUsers": ["usr_douglas_lim"]
}
```

---

### Phase 4: Staff Onboarding (Administrative Hierarchy)
Provisions roles that execute the system from the backend offices (excluding field guards):

| Administrative Role | Key Operational Clearance |
| :--- | :--- |
| **company_admin** | Full financial operations, payroll approvals, and invoice generations. |
| **site_admin** | Daily scheduling, site perimeter validation, and equipment allocation. |
| **office_staff** | Basic document entry, verification processes, and resident mapping. |
| **accounts_staff**| Client receivable logs, billing generation, and GST calculation. |

---

### Phase 5: Guard Onboarding (Field Lifecycle Engine)
The complete guard onboarding pipeline follows a strict chronological state progression:

$$\text{REGISTERED} \longrightarrow \text{DOCUMENTS\_VERIFIED} \longrightarrow \text{TRAINING\_COMPLETED} \longrightarrow \text{SITE\_ASSIGNED} \longrightarrow \text{DEVICE\_REGISTERED} \longrightarrow \text{ATTENDANCE\_READY} \longrightarrow \text{ACTIVE}$$

#### Document Schema Pattern
*   **Path**: `/users/{userId}`
```json
{
  "id": "usr_guard_chen",
  "name": "Aaron Chen",
  "role": "guard",
  "onboardingState": "ATTENDANCE_READY",
  "kycApproved": true,
  "bankAccount": {
    "bankCode": "DBS_SG",
    "accountNumber": "194-82948-2"
  },
  "trainingStatus": {
    "basicSecurityPassed": true,
    "firstAidExpiry": 1827453600000
  },
  "deviceBinding": {
    "deviceId": "dev_pixel8_94819d",
    "fingerprint": "eb6274a123bc8481ae"
  }
}
```

---

### Phase 6 & 7: Residential Onboarding & Unit Generation
For private residential complexes, guards coordinate visitor entries against unit assignments.

#### Unit Bulk Generation Specs
*   **Manual Entry**: High-control single creation.
*   **Bulk Generation Engine**: Developers run programmatic expansion routines to map full building layouts instantly:
    $$\text{Tower} \times \text{Floors} \times \text{Flats} \implies \text{Unit Collection Documents}$$
*   **Schema**:
    ```json
    {
      "id": "unit_t1_f12_a",
      "siteId": "site_apex_condo",
      "tower": "Tower 1",
      "floor": "Floor 12",
      "flat": "12-A",
      "checkpointQRToken": "chk_qr_t1_12a"
    }
    ```

---

### Phase 8: Asset Onboarding & Lifecycle Track
Security hardware (such as QR patrol checkpoints, high-freq NFC tags, mobile walkie talkies, and body-worn cameras) is registered and bounded inside site locations.

```
       [Asset Status Workflow]
       Available ──► Assigned ──► Maintenance ──► Retired
```

---

### Phase 9: Contract & SLA Rate-sheet Binding
Establishes the mathematical baseline for billing. Connects billing schedules to worked hours.

*   **Fixed Rate Rota**: Set fee per guard per calendar shift.
*   **Hourly Rates SLA**: Rate bounded by hours logged by geofenced QR scans on-site.
*   **Compliance Deductions**: Inward penalizations for unstaffed shifts.

---

### Phase 10: Compliance & PSARA Document Vault
Saves regional regulatory certifications: P.S.A.R.A laws (Private Security Agencies Regulation Act), third-party damage insurances, and local worker safety registrations.

```
                   +------------------------+
                   |  Compliance Document   |
                   +------------------------+
                                |
             Check Validity ➔ Trigger Alerts if:
             (Current Time >= Expiry Time - 30 Days)
```

---

## 3. TRANSITION TO OPERATIONS ACTIVATION

Only when Phases 1 through 10 report a complete, non-null status, does the **Service Activation Trigger** invoke. This locks the master onboarding data structures and unleashes the operational execution layer:

```
+───────────────────────────────────────────────────────+
|               SERVICE ACTIVATION TRIGGER              |
+───────────────────────────────────────────────────────+
  │
  ├── 🟢 Attendance Clock-Ins Block Unlocked
  ├── 🟢 Visitor Verification QR Screens Active
  ├── 🟢 Incident Submission Panels Connected
  ├── 🟢 SOS Handshake Broadcast Transceiver Activated
  └── 🟢 Live Command BI Stream Initiated
```

This structural discipline guarantees a highly reliable environment for the Kundal ERP software suite.

---
*Authorized for Platform Distribution by the ERP Operations Council on June 22, 2026.*
