# KUNDAL SECURITY OS V3 – MOBILE PLATFORM & FIELD OPERATIONS ECOSYSTEM REPORT
**Revision:** 3.0.0-MOB  
**Status:** APPROVED  
**Date:** June 22, 2026  
**Target Audience:** Mobile Engineering leads, SecOps Directors, Systems Architects  

---

## EXECUTIVE SUMMARY & STRUCTURAL POLARIZATION

To scale operations across global client installations, **Kundal Security OS V3** strictly separates corporate/enterprise management from real-time field operations. 

*   **Administration & Analytics** (Super Admins, Company Admins, Group Admins) reside strictly in secure **Web Portals (Next.js)**. 
*   **Field Execution & Verification** (Guards, Patrols, Site Supervisors) are deployed exclusively via high-performance, containerized **Mobile Clients (Flutter Native/Kotlin Engine)**.

This structural separation isolates high-risk transaction engines (payroll calculations, invoices, user permissions administration) from field-accessible terminals, shrinking the overall security attack surface and ensuring low-latency native execution under intermittent network coverage.

---

## DELIVERABLE 1: MOBILE PLATFORM ARCHITECTURE MAP

```
+-----------------------------------------------------------------------------------+
|                                 KUNDAL CLOUD BACKEND                              |
|   +-------------------+  +-------------------+  +-----------------------------+   |
|   |   Admin Web API   |  |  Client Web API   |  | Firebase Cloud Functions    |   |
|   +---------+---------+  +---------+---------+  +--------------+--------------+   |
+-------------|----------------------|---------------------------|------------------+
              |                      |                           | (gRPC Secure Gateway)
              | (HTTPS Restful)      | (HTTPS Restful)           |
+-------------v---------+  +---------v---------+  +--------------v------------------+
|      ADMIN PORTAL     |  |   CLIENT PORTAL   |  |          MOBILE ECOSYSTEM       |
|      (Next.js Web)    |  |   (Next.js Web)   |  |                                 |
|                       |  |                   |  |  +------------+  +-----------+  |
|  - Corporates Mgmt    |  |  - Service SLAs   |  |  |  GUARD APP |  | SUPERVISOR|  |
|  - Group Consolidation|  |  - Incident Feeds |  |  |  (Flutter) |  |   (Fltr)  |  |
|  - Payroll Approval   |  |  - Bill Sheets   |  |  +------------+  +-----------+  |
|  - System Audit Logs  |  |  - Dispatch Map   |  |  (Offline-FS)   (Command Ctr) |
|                       |  |                   |  |                                 |
+-----------------------+  +-------------------+  +---------------------------------+
```

### Modular Specifications
1.  **Admin Portal (Next.js Web)**: Target vehicle for heavy corporate processing. Restricted to desktop viewport rendering, optimizing tabular visualization of financial parameters.
2.  **Guard App (Flutter - Android Target Focus)**: Single-purpose executor optimized for intensive outdoor operations. Characterized by high-contrast UI patterns, big tap-targets (minimum 52dp padding), automatic GPS background collection, and aggressive power conservation modes.
3.  **Supervisor App (Flutter - Android Target Focus)**: Operational command hub. Contains tactical dashboards, relief dispatch workflows, spatial layouts, and push exception processing interfaces.
4.  **Client Portal (Next.js Web)**: External-facing read-only security view. Clients inspect historical incident logs, current guard roster presence rates, and monthly compliance audits.

---

## DELIVERABLE 2: OFFLINE-FIRST STRATEGY & SYNC ENGINE SPECIFICATION

Field personnel operate in deep underground parks, structural steel complexes, and remote yards where cellular signals drop. A fragile client-server model invites clock-in failures and lost incident logs. We specify a resilient **Local-First Synchronization Architecture**.

### 1. Database Cache Design
Every mobile client runs an embedded instance of **SQLite** (accessed via Room in Android native / Hive & drift in Flutter). 

```
+-----------------------+
|   Local Sync Queue    |
|  - Operation Type     |
|  - Target Path        |
|  - Serialized JSON    |
|  - Queue Priority     |
+-----------------------+
           ↓ (Auto Synchronizer Task)
+-----------------------+      (Online Check)      +-------------------------+
| Cellular/Wifi Connection? | ➔➔➔➔ YES ➔➔➔➔➔ | Stream to Cloud Function|
+-----------------------+                          +-------------------------+
           ↓ NO
+-----------------------+
| Retain in Queue Cache |
| Schedule Retry (Expo) |
+-----------------------+
```

### 2. Conflict Resolution Invariants
Because mobile clients check in elements offline, synchronization conflicts are resolved deterministically using a **Last-Write-Wins (LWW) with Immutable Append Only Log** hybrid approach:
*   **Transactions (Attendance, SOS)**: Append-Only logs are written locally with server-trusted NTP synced timestamps. These logs are *never* editable or mergeable. If a guard clocks in offline, the exact transaction is stored with status `pending_sync`. When connection is restored, the server accepts the transaction chronologically without modification fields.
*   **Entity Updates (Visitors, Assets)**: Tracked using incrementing `version` sequences. If local version `v` overrides a cloud updated `v+1`, the Cloud Function automatically rejects the write and forces a schema pull before permitting client-side mutation.

---

## DELIVERABLE 3: PUSH NOTIFICATION ARCHITECTURE (FCM topic topology)

To minimize background wake cycles and coordinate instantaneous emergency dispatching, Firebase Cloud Messaging (FCM) is divided into clean, topic-based pipelines.

### Topic Subscription Matrix

```
/topics/global
├── /topics/companyGroupId
│   ├── /topics/companyId
│   │   ├── /topics/siteId_ops      (Guards & Supervisors assigned to Site)
│   │   ├── /topics/siteId_alerts   (Supervisors only)
│   │   └── /topics/companyId_admin (Corporate Admins only)
```

### Command Notification Payloads

#### SOS Alert Payload (Critical Priority)
FCM uses high-priority JSON payloads to bypass system standby optimization constraints, triggering immediate system overlays on target Supervisor devices:
```json
{
  "to": "/topics/site_apex_towers_alerts",
  "priority": "high",
  "android": {
    "priority": "high",
    "ttl": "0s"
  },
  "data": {
    "click_action": "FLUTTER_NOTIFICATION_CLICK",
    "category": "SOS_DISPATCH",
    "title": "EMERGENCY: Guard SOS Triggered",
    "body": "Guard Aaron Chen has triggered SOS at Post 3 - Sector Delta.",
    "incidentId": "inc_71948B",
    "siteId": "site_apex_towers",
    "latitude": "1.290270",
    "longitude": "103.851959",
    "timestamp": "1782051200000"
  }
}
```

---

## DELIVERABLE 4: DEVICE BOUNDING & TRUSTED SECURE ENCLOVE FLOWS

To prevent credential leaks, password sharing, and spoofed coordinates, we implement a **Cryptographic Hardware-Backed Device Bounding Engine**.

```
[New Guard LogIn Requested]
             ↓
[Query Android Keystore / iOS Secure Enclave for Keypair "kundal_os_auth_key"]
             ↓
[If Key Pair is Missing] ➔ [Generate 2048-bit Private Key inside hardware Secure Enclave]
             ↓
[Submit CSR (Certificate Signing Request) to Cloud Registration Function]
             ↓
[Validation Check: Is Device UUID Approved in Site Registry & Free of active bindings?]
             ↓
[Success: Store Fingerprint Binding in Firestore Users Collection]
```

### Security Check Mechanics
1.  **Strict Device Attestation**: On startup, the Mobile Client challenges the device's hardware integrity (Google Play Integrity Key / Apple DeviceCheck). Fake emulators or rooted devices are rejected at the REST API boundary.
2.  **Coordinates Spoof Protection**: The GPS coordinate tracking system cross-references GPS hardware reporting times with network-reported NTP standard times. Mock GPS providers (Developer Options) are programmatically queried via native plugins and automatically lock the client app, signaling a `TAMPER_EXCEPTION` to the Supervisor Command Desk.

---

## DELIVERABLE 5: DIGITAL STANDARD FORMS & SHIFT CHECKLISTS

Paper logbooks are unreliable, prone to loss, and slow to aggregate. V3 enforces strict digital schemas for field data collection:

```
                  +-----------------------------------+
                  |      SHIFT INITIALIZATION FLOW    |
                  +-----------------------------------+
                                    |
          1. Uniform Check (Selfie Upload with OCR Detection)
                                    |
          2. Equipment Verification (Barcode Scan of Radio & Cam)
                                    |
          3. Physical Site Perimeter Inspection Walk
```

### Digital Handover Specification (Guard Shift Closure)
Guards cannot check out of their active shift until they complete the digital handover protocol inside the Guard App:
*   **Active Handover Match**: Offcoming guard and incoming replacement guard must perform a local QR swap. Offcoming guard generates a Handover Session QR. Incoming guard scans it with their biometric authentication, registering the immediate mutual presence of both personnel.
*   **Inventory Reconciliation**: Verifies that all site assets (body cameras, vehicles, patrol tags) have their barcode states matched and cleared. Discrepancies generate a priority notification directly to the Supervisor.

---

## DELIVERABLE 6: SCREEN DIRECTORY & CLINICAL USER JOURNEYS (CUJS)

The following matrix inventories all structural mobile screens, detailing target view purposes:

### 1. Guard App Screen Checklist
*   **G_AUTH_PIN**: Native Biometric verification (FaceID/Fingerprint) / Secure 6-digit backup passcode gate.
*   **G_DASHBOARD**: Clean main feed displaying active site name, assigned shifts, current geofenced tracking status, and primary heavy-action buttons (SOS & Check-in).
*   **G_ATTENDANCE_PUNCH**: QR reader interface overlaid on standard camera stream with active GPS tracking metric displays.
*   **G_INCIDENT_REPORT**: Form layout containing input widgets, rich multimedia capture pipelines (image/video/audio recorder), and category selector wheels.
*   **G_VISITATION_FLOW**: Multi-step stepper allowing visitor pre-registration or rapid visitor check-in based on invite QR scanning.
*   **G_NOTICES_FEED**: Broad emergency announcement card deck with reading confirmation swipe controls.
*   **G_PROFILE_IAM**: Visual ID card overlay, digital training certification badge vault, and monthly historic shift timesheets.

### 2. Supervisor App Screen Checklist
*   **S_DASHBOARD**: Tactical overview map showing live geographic pins of active guards, site checkpoint statuses, and aggregate presence KPIs.
*   **S_SOS_COMMAND**: Flashing full-screen prioritization cockpit featuring high-contrast route maps, incident metadata, and immediate relief team dispatch buttons.
*   **S_WORKFORCE_ROTA**: Shift coordination dashboard allowing direct worker redeployments during emergency absences.
*   **S_ATTENDANCE_EXCEPTIONS**: Verification queue containing flagged geofenced breaches, missed check-ins, or selfie failures requiring supervisor manual sign-offs.

---

## DELIVERABLE 7: OPERATIONAL PERFORMANCE SLAs

To ensure optimal operations in high-stress field conditions, mobile clients must adhere to strict performance constraints:

| Mobile Operational Transaction | Maximum Target Latency | Resiliency Failover Method |
| :--- | :--- | :--- |
| **Attendance Check-In** | < 2.5 seconds | Cached locally to SQLite sync queue if server handshake exceeds 2s timeout. |
| **SOS Incident Generation & Broadcast**| < 4.0 seconds | High-priority FCM data pipeline bypass with parallel local logging. |
| **Visitor Barcode Check-In** | < 1.5 seconds | Local matching loop against pre-downloaded guest token cache bucket. |
| **Incident Video Upload Progressing** | < 30 seconds (10MB) | Async upload queue executing in dedicated system background worker threads. |

---
*Certified for Deployment across Mobile Platform Infrastructure on June 22, 2026.*
