# KUNDAL SECURITY OS V3 – DATABASE & BACKEND STANDARDIZATION MASTER REPORT
**Revision:** 3.0.0-PROD  
**Status:** APPROVED  
**Date:** June 22, 2026  
**Target Audience:** Lead Architect, SecOps Lead, Platform Engineering Group  

---

## EXECUTIVE SUMMARY & CRITICAL REVIEW
This report details the architectural standardization of the **Kundal Security OS V3** Firebase backend. The previous multi-tenant mapping exhibited architectural fragmentation—specifically, the persistence of the intermediate `areaOfficeId` (a legacy branch grouping construct) and naming inconsistencies (`companyID`, `CompanyId`, `company_id`). 

This Phase has successfully:
1. Standardized all Firestore schemas on a strict unified tenancy structure.
2. Formulated complete, zero-default security rules ensuring absolute direct client write blocking of billing, payroll, contracts, and audit logs.
3. Created standard composite index configurations for high-frequency queries to avoid runtime query evaluation bottlenecks.
4. Established strict middleware patterns for server-side Cloud Functions to prevent RBAC bypasses.

---

## DELIVERABLE 1: DATABASE REPORT
### Architectural Foundations
Firestore is structured as a schemaless document store. In a multi-tenant enterprise system, relying on standard schemaless flexibility is a critical vulnerability. We enforce a **Flat Tenancy Hierarchical Structure** where every single operational document must explicitly reference its tenancy bounds. 

```
                                [Super Admin] (Global Access)
                                       |
                              [Company Group] (companyGroupId)
                                       |
                                [Company]     (companyId)
                                       |
                                [Client Site] (siteId)
                                       |
                                [Physical Unit] (unitId)
```

### Tenancy Isolation Mechanics
*   **Logical Bounding**: Cross-tenant queries are blocked at the Firestore Security Rules level by comparing the tenant properties (`companyGroupId`, `companyId`, `siteId`) of the incoming `request.resource.data` with the active `request.auth.token` parameters (Custom Claims) or the stored User Record in Firestore.
*   **Normalization Strategy**: The middle-tier `area_office` collection is completely retired. Unifying the hierarchy reduces query depth, preserves limits on composite indexes, and minimizes security rules complexity.

---

## DELIVERABLE 2: COLLECTION INVENTORY

The following inventory governs the complete Firestore structure for Kundal Security OS V3:

| Collection Path | Read Access | Write Access | Storage Engine | Purpose / Notes |
| :--- | :--- | :--- | :--- | :--- |
| `/company_groups` | Authenticated (Scope Restricted) | Cloud Functions Only | Firestore | Highest logical container for conglomerate entities. |
| `/companies` | Authenticated (Scope Restricted) | Cloud Functions Only | Firestore | Individual security agencies / tenant companies. |
| `/sites` | Authenticated (Scope Restricted) | Cloud Functions Only | Firestore | Operations yards, clients, guard posts. |
| `/units` | Authenticated (Scope Restricted) | Cloud Functions Only | Firestore | Specific zones or checkpoints within a Site. |
| `/users` | Authenticated (Scope Restricted) | Cloud Functions / Self | Firestore | Global IAM store. Users bounded to site or company. |
| `/attendance` | Site-Restricted Roles | Cloud Functions Only | Firestore | Geo-validated guard rota check-ins. |
| `/visitors` | Site-Restricted Roles | Client (Rules Checked) | Firestore | Access control logs and resident pre-approval invitations. |
| `/incidents` | Site-Restricted Roles | Client (Rules Checked) | Firestore | Security exceptions and patrol logs. |
| `/payroll` | Corporate Roles Only | Cloud Functions Only | Firestore | **DIRECT WRITE BLOCKED.** Handled via Cloud Functions. |
| `/billing` | Corporate Roles Only | Cloud Functions Only | Firestore | **DIRECT WRITE BLOCKED.** Admin billing & invoicing. |
| `/contracts` | Corporate Roles Only | Cloud Functions Only | Firestore | **DIRECT WRITE BLOCKED.** SLAs, rate sheets. |
| `/leave_requests` | Self + Overseer | Client (Rules Checked) | Firestore | Active leave requests, shift replacements. |
| `/audit_logs` | Corporate Roles Only | Cloud Functions Only | Firestore | **IMMUTABLE APPEND-ONLY.** Write once, never edit. |
| `/assets` | Site-Restricted Roles | Cloud Functions / Client | Firestore | Security equipment, patrol tags, vehicle trackers. |
| `/notifications` | Recipient Bounded | Cloud Functions Only | Firestore | Real-time security dispatch broadcasts. |

---

## DELIVERABLE 3: FIELD STANDARDIZATION REPORT

### Naming Inconsistency Audit
A systematic code and schema audit identified severe Technical Debt:
- **Redundant Naming**: Usage of `companyID`, `CompanyId`, `company_id`, and `comp_id` across legacy payloads.
- **V2 Intermediate Middle Tier Leakage**: Fields like `areaOfficeId` injected into `sites`, `attendance`, and `visitors`, resulting in "double-ownership mapping" which degraded Firestore read performance.

### Standardization Mappings (CamelCase Unified)
All collections have been refactored under strict `camelCase` parameters:

| Legacy Field | Standardized V3 Field | Target Data Type | Definition |
| :--- | :--- | :--- | :--- |
| `companyID` / `company_id` | `companyId` | `String` | Unique ID of the Security Company. |
| `group_id` / `CompanyGroupId` | `companyGroupId` | `String` | Unique ID of the Company Group conglomerate. |
| `site_id` / `SiteID` | `siteId` | `String` | Unique ID of the Client Site. |
| `user_id` / `workerID` | `userId` | `String` | Unified UUID for the user/worker resource. |
| `visitor_ID` / `guest_id` | `visitorId` | `String` | Unique ID of the visitor document. |
| `incident_id` / `exceptionId`| `incidentId` | `String` | Unique ID of the exception report. |
| `created_at` / `timestamp_c` | `createdAt` | `Long` (Epoch ms) | Immutable milliseconds from historical Epoch. |
| `updated_at` / `timestamp_u` | `updatedAt` | `Long` (Epoch ms) | Milliseconds of the latest document update. |

---

## DELIVERABLE 4: INDEX REPORT
Firestore query planning requires structured index coordination. If index templates do not align with runtime queries, Firestore throws runtime errors.

### Query Profiles & Composite Indexes
The following index mapping targets high-frequency operational pipelines:

```json
{
  "indexes": [
    {
      "collectionGroup": "attendance",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "siteId", "order": "ASCENDING" },
        { "fieldPath": "clockInTime", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "visitors",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "siteId", "order": "ASCENDING" },
        { "fieldPath": "status", "order": "ASCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "incidents",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "siteId", "order": "ASCENDING" },
        { "fieldPath": "severity", "order": "ASCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "payroll",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "companyId", "order": "ASCENDING" },
        { "fieldPath": "month", "order": "ASCENDING" }
      ]
    },
    {
      "collectionGroup": "billing",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "companyGroupId", "order": "ASCENDING" },
        { "fieldPath": "month", "order": "ASCENDING" }
      ]
    }
  ],
  "fieldOverrides": []
}
```

### Unused & Redundant Index Pruning
Index volume negatively impacts write latency in Firestore. In V3, single-field queries (e.g. `WHERE id == 'x'`) are handled natively by Firestore's automatic single-field indexing. We proactively exclude automatic indexing on heavy-payload properties (e.g. base64 photo streams or descriptive incident payloads) to minimize system costs and optimization bloating.

---

## DELIVERABLE 5: SECURITY REPORT & TENANT ISOLATION REVIEW

### Role Security Context Boundaries (Client Gatekeeping)
1.  **super_admin**: Bypasses tenancy parameters. Accesses `/company_groups`, `/companies`, `/sites`, `/users`, etc. without query boundary restrictions.
2.  **group_admin**: Bypassed restricted access only if `resource.data.companyGroupId == request.auth.token.companyGroupId`.
3.  **company_admin**: Bounded to single `companyId`. Allowed visibility into sites, payroll, and billing sheets belonging strictly to that `companyId`.
4.  **site_admin**: Restricted to assigned `siteId`. Cannot query entries outside their designated client site bounds.
5.  **client_user** & **resident**: Narrow reading parameters. Client users read attendance aggregated at their assigned `siteId`. Residents are strictly bounded relative to their specific `siteId` and custom `unitId`.
6.  **guard**: Allowed to READ information regarding themselves and active `sites` schema items for their designated `siteId`. Can ONLY write `incidents` and `visitors` payloads that strictly bind to their active assigned `siteId`.

### Architectural Verification Against Leakage
*   **Security Principle**: "Do not trust user input." In all client-initiated writes, the engine MUST NOT rely on client-provided company properties without active authorization parsing.
*   **Rules Guarding**: FireStore rules enforce tenancy alignment by evaluating existing records:
    `get(/databases/$(database)/documents/users/$(request.auth.uid)).data.companyId == request.resource.data.companyId`

---

## DELIVERABLE 6: RULES REVIEW (FIRESTORE & STORAGE)

### Client WRITE Block Invariants (Hardened Bounding)
To prevent compromised devices from editing enterprise financials or covering trail signatures, **we completely block direct client-side WRITE, UPDATE, and DELETE operations** (no legacy wildcards allowed) on:
- `/company_groups`
- `/companies`
- `/sites`
- `/payroll`
- `/billing`
- `/contracts`
- `/audit_logs`

These records are strictly generated, updated, or purged via **Firebase Cloud Functions** operating in a highly protected, isolated backend Node.js environment utilizing the `firebase-admin` SDK.

### Active Collections Rules Strategy
For client-writeable items (`attendance`, `visitors`, `incidents`, `leave_requests`), the rules engine enforces strict data schema schemas and tenancy alignment. For example, a Guard cannot upload an incident report representing Site A if they are currently assigned to Site B.

---

## DELIVERABLE 7: RISK ANALYSIS & MITIGATION REPORT

### 1. Firestore Security Rule Execution Budgets
*   **Skeptical Discovery**: Firestore Security Rules support a maximum of 10 `get()` or `exists()` document validations for a single request. 
*   **Concrete Risk**: If rules contain recursive path assertions (e.g. checking company group, then company validation, then site parameters), a single query checking multiple documents will exhaust the rules transaction limit, throwing instant `403 Permission Denied` runtime exceptions.
*   **Mitigation Strategy**: Inject tenancy scopes directly into the User's Custom Claims (e.g., `companyId`, `companyGroupId`, `role`) on Token generation / Authentication. Using custom token claims allows rules to access tenancy assertions instantly via `request.auth.token.companyId` with **zero** `get()` document reads, freeing rule execution budgets.

### 2. Clock-Sync Skew Vulnerability
*   **Skeptical Discovery**: Clients can easily spoof timestamps (`createdAt`, `updatedAt`) in direct client writes to manipulate billing metrics or cover attendance lapses.
*   **Concrete Risk**: A guard clocking in late can falsify client-side device time to force an on-time log.
*   **Mitigation Strategy**: Firestore Rules must validate that timestamps match server time:
    `request.resource.data.createdAt == request.time`

---

## DELIVERABLE 8: BACKEND REPORT & FUNCTION INVENTORY

The following backend services compose the core Cloud Functions execution pool:

| Function Name | Invocation Style | Roles Allowed | Complexity Profile | Expected Writes |
| :--- | :--- | :--- | :--- | :--- |
| `createSite` | HTTPS Callable | `company_admin`, `group_admin` | Medium | `/sites`, `/audit_logs` |
| `updateSite` | HTTPS Callable | `company_admin`, `group_admin` | Medium | `/sites`, `/audit_logs` |
| `createVisitor` | HTTPS Callable / Trigger | `resident`, `guard`, `site_admin` | Low | `/visitors`, `/audit_logs` |
| `createIncident` | HTTPS Callable / Trigger | `guard`, `site_admin`, `company_admin` | Low | `/incidents`, `/audit_logs` |
| `approveLeave` | HTTPS Callable | `company_admin`, `site_admin` | Medium | `/leave_requests`, `/audit_logs` |
| `generateInvoice` | HTTPS Callable / Cron | System / `company_admin` | High | `/billing`, `/audit_logs` |
| `calculatePayroll` | HTTPS Callable / Cron | System / `company_admin` | High | `/payroll`, `/audit_logs` |

---

## DELIVERABLE 9: FUNCTION REFACTOR PLAN & AUDIT LOGGING PLAN

### Production Middleware Pipeline Specification
Every Cloud Function MUST conform to a strict 6-phase pipeline pattern to guarantee tenant boundaries are maintained:

```
[Trigger] ➔ [assertAuthenticated] ➔ [assertRole] ➔ [validateInput] ➔ [verifyTenantScope] ➔ [execute] ➔ [writeAuditLog]
```

1.  **assertAuthenticated()**: Decodes incoming JWT, checks revocation tokens, rejects anonymous tasks.
2.  **assertRole()**: Matches RBAC claims table against metadata parameters.
3.  **validateInput()**: Validates payload schema properties using standard validation packages (e.g., `zod` schema parsers).
4.  **verifyTenantScope()**: Verifies that the caller's authorized tenant scope matches the scope of the target document they are creating, editing, or deleting.
5.  **execute()**: Runs database mutations in highly transactional environments.
6.  **writeAuditLog()**: Atomically writes an immutable log of changes to `/audit_logs`.

### Immutable Audit Log Schema
Audit logs are strictly write-once records. Rules prohibit any updates or removals.
```json
{
  "id": "aud_72948a",
  "eventType": "SITE_UPDATED",
  "companyGroupId": "grp_alpha",
  "companyId": "cmp_centurion",
  "siteId": "site_apex_towers",
  "entityType": "Site",
  "entityId": "site_apex_towers",
  "performedBy": "usr_alpha_admin",
  "timestamp": 1782051200000,
  "before": { "status": "Draft", "name": "Apex Towers" },
  "after": { "status": "Active", "name": "Apex Towers Complex" }
}
```

---

## DELIVERABLE 10: MIGRATION REPORT & ROLLBACK STRATEGY

### Data Migration & Phase Shift Requirements
To migrate the legacy V2 backend data structure to the clean V3 standard without downtime:

1.  **Dual-Write Window**: Deploy Cloud Functions that support both legacy reading structures (`areaOfficeId`) and standardized unified variables (`siteId`, `companyId`) to maintain client-side compatibility during migration.
2.  **Conversion Command Script**: Execute a server-side Node script to populate empty `companyGroupId` properties by querying site bounds, flattening the intermediate branch layers entirely.

### Breaking Changes Impact
- **Immediate Change**: Client devices running V2 models (e.g., trying to write to `/sites` directly or referencing `areaOfficeId`) will fail instantly upon rules enforcement due to restricted client write barriers.
- **Dependency Purge**: The property `areaOfficeId` is thoroughly deprecated. Database queries relying on cross-joins over area office collections are retired.

### Fail-Safe Rollback Strategy
```
                     [DESTRUCTIVE FAILURE ENCOUNTERED]
                                    |
                Rollback Security Rules to Pre-V3 State
                                    |
              Restore Firestore Snapshot from Backup Bins
                                    |
                 Re-enable Legacy Client Direct Writes
```

*   **Database Backup Bin**: Perform a complete FireStore Metadata and Collection Export to Google Cloud Storage (`gs://kundal-v3-backup-bins`) directly prior to phase launch.
*   **Backup Command**:
    `gcloud firestore export gs://kundal-v3-backup-bins`
*   **Rules Rolling**: Maintain the legacy `firestore.v2.rules` repository branch. If production metrics exhibit schema validation crashes, immediately execute:
    `firebase deploy --only firestore:rules --config firebase.v2.json`

---
*Authorized by Lead Architect on behalf of Kundal Operations Command.*
