# KUNDAL SECURITY OS V3 – EXECUTIVE COMMAND CENTER & BUSINESS BI PLATFORM
**Revision:** 3.0.0-BI  
**Status:** APPROVED  
**Date:** June 22, 2026  
**Target Audience:** Chief Technology Officer, VP of Operational Security, Lead Systems Architects, Data Engineers  

---

## EXECUTIVE SUMMARY & INTENT
With Kundal Security OS V3 establishing clean **logical multi-tenant isolation** and high-fidelity **mobile field terminal boundaries**, the final critical tier is the **Business Intelligence and Command Center Level**. Security agency owners, operations directors, and company presidents must possess continuous, zero-latency visibility into operational metrics and financial realities across hundreds of service sites without degrading system performance.

This report specifies the architecture of the **Kundal V3 Executive Operations Command Center**. To prevent transaction-blocking performance degradation and high API read costs, **direct real-time query aggregation over active production collections is strictly forbidden.** 

All BI metrics are populated using an asynchronous, event-driven pattern:
1. **Aggressive Cache Layering**: Scheduled cron pipelines and change-triggers build cached, daily analytical aggregates inside dedicated `/analytics_cache/...` collections.
2. **Materialized Multi-Tenant Views**: Dashboard reads fetch single pre-computed records partitioned under explicit tenant boundaries (`companyGroupId`, `companyId`).
3. **External Warehouse Integration**: A stream-lined export pipeline migrates Firestore logs to **Google BigQuery** for high-dimensional Looker Studio querying.

---

## DELIVERABLE 1: SYSTEM TOPOLOGY & AGGREGATE CACHING ARCHITECTURE

To safeguard operational read limits, all analytics are computed server-side via scheduled Firebase Cloud Functions and triggered Firestore events.

```
+-----------------------------------------------------------------------------------------+
|                                    KUNDAL OS V3 ENGINE                                  |
|                                                                                         |
|  [Operational Collections]                                                              |
|  (/attendance, /incidents, /payroll, /billing, /visitors)                               |
|             │                                                                           |
|             ▼ (Real-time Cloud Function trigger / DB change stream)                     |
|  +─────────────────────────+                                                            |
|  |   BigQuery Export Flow  | ────► [Google BigQuery Serverless DW] ──► Looker Studio dashboards
|  +─────────────────────────+                                                            |
|             │                                                                           |
|             ▼ (Scheduled MapReduce / Chron Jobs at 00:00 UTC)                           |
|  +─────────────────────────+                                                            |
|  | Cloud Functions Engine  |                                                            |
|  +─────────────────────────+                                                            |
|             │                                                                           |
|             ▼ (Atomically writes optimized daily balances)                              |
|  [Analytics Cache Store]                                                                |
|  (/analytics_cache/companyGroupId/daily_metrics/)                                       |
|             │                                                                           |
|             ▼ (High-speed single Document Pull)                                         |
|  +─────────────────────────+                                                            |
|  | Executive Mobile Portal |                                                            |
|  +-------------------------+                                                            |
+-----------------------------------------------------------------------------------------+
```

---

## DELIVERABLE 2: COLLECTION INVENTORY - CACHED ANALYTICS SCHEMAS

The `/analytics_cache` structure guarantees that a single document read contains all company snapshots.

### 1. Document Path: `/analytics_cache/{companyId}/snapshots/daily_aggregates`
```json
{
  "id": "daily_agg_2026_06_22",
  "companyId": "cmp_centurion_02",
  "companyGroupId": "grp_alpha_01",
  "timestamp": 1782051200000,
  "dateString": "2026-06-22",
  
  "companySnapshot": {
    "totalSites": 24,
    "activeSites": 22,
    "totalGuards": 312,
    "activeGuardsOnDuty": 204,
    "reliefGuardsAvailable": 18,
    "totalClients": 14,
    "unitsManaged": 145
  },
  
  "financialSnapshot": {
    "monthlyRevenueYTD": 452000.00,
    "monthlyPayrollYTD": 310000.00,
    "grossMarginPercentage": 31.4,
    "outstandingReceivables": 89000.00,
    "collectedReceivables": 363000.00,
    "gstLiabilityYTD": 45200.00,
    "collectionRate": 80.3
  },
  
  "operationsSnapshot": {
    "attendanceComplianceRate": 96.2,
    "averageSlaResolutionSeconds": 482,
    "incidentRateYTD": 1.4,
    "patrolComplianceRate": 94.8,
    "totalVisitorVolume": 240,
    "activeSosEvents": 0
  },
  
  "slaScores": {
    "attendanceSlaRating": 98.4,
    "patrolSlaRating": 95.2,
    "incidentSlaRating": 92.1,
    "sosSlaRating": 99.9,
    "clientComplianceRating": 97.5
  },
  
  "forecastMetrics": {
    "predictedStaffingShortage": 3,
    "reliefDemandIndex": 1.2,
    "recruitmentNeedCount": 8,
    "predictedNextMonthCost": 312500.00
  }
}
```

---

## DELIVERABLE 3: REAL-TIME OPERATIONS COMMAND CENTER (WALL DISPLAY)

The Operations Command interface acts as an active operations wall display with localized state-handling, showing live incident levels, real-time alerts, and presence heatmaps.

```
========================================================================================
                      KUNDAL SECURITY V3 LIVE COMMAND CENTER
========================================================================================
[SOS ACTIVE BEACONS] 🔴 0 ACTIVE (AVG RESPONSE: 4.8 SEC)
----------------------------------------------------------------------------------------
[SITE HEALTH MATRIX]
 🟢 Site: Apex Towers      | Guards: 12/12  | Incidents: 0  | Status: HEALTHY
 🟢 Site: Sector Delta     | Guards: 8/8    | Incidents: 0  | Status: HEALTHY
 🟡 Site: Maritime Plaza   | Guards: 4/5    | Incidents: 1  | Status: WARNING (Staff Shortage)
 🔴 Site: Terminal 3 Cargo | Guards: 1/4    | Incidents: 3  | Status: CRITICAL (SOS/Late Breach)
----------------------------------------------------------------------------------------
[ATTENDANCE FEED]
 Present: 184 (94%) | Absent: 4 (2%) | Late: 8 (4%) | Geofence Violations: 1
========================================================================================
```

### SLA Violation Severity Rules
Any failure to meet security parameters immediately escalates site status:
*   **Green (Healthy)**: No outstanding incident reports, attendance matching >95% SLA constraints.
*   **Yellow (Warning)**: Single unresolved Medium-level incident or guard-punch omission exceeding 15 minutes past shift activation.
*   **Red (Critical)**: Active unresolved SOS, staffing volume <75% of scheduled shift count, or high-severity incident reported within last 30 minutes.

---

## DELIVERABLE 4: COMPLIANCE MATRIX & CLIENT RISK EQUATION

Bi-dimensional mapping identifies vulnerable business margins through mathematical indicators.

### 1. Unified Compliance Score ($C$)
The compliance score of an entity (Company, Site, or Employee) is computed chronologically over a trailing 30-day window:

$$C = w_a A_c + w_p P_c + w_d D_c$$

Where:
*   $A_c$: Trailing Attendance Compliance rate (0.0 to 1.0)
*   $P_c$: Patrol Schedule Compliance rate (0.0 to 1.0)
*   $D_c$: Document and Certification Validity score (1.0 if zero expired documents, 0.0 if any expired)
*   $w_a, w_p, w_d$: Custom weights ($\Sigma w_i = 1.0$), with standard coefficients: $w_a = 0.5$, $w_p = 0.3$, $w_d = 0.2$.

### 2. Client Risk Index ($R$)
To alert executives of impending contract vulnerabilities, we rank sites on a decimal scale of `0.0` (Impeccable) to `1.0` (Extreme Financial/Operational Flight Risk) using a weighted risk engine:

$$R = 0.4 \times (\text{Unstaffed Hours Percentage}) + 0.3 \times (\text{Unresolved SLA Breaches}) + 0.3 \times (\text{Days Payment Overdue} / 90.0)$$

---

## DELIVERABLE 5: REVENUE, PAYROLL, & SITE PROFITABILITY COCKPIT

Executives track site profitability down to municipal bounds. The dashboard maps exact site profitability by joining and deducting localized costs from gross SLA payouts.

### Site Profitability Equation

$$\text{Net Site Profit} = \text{SLA Payout Billing} - \left( \sum (\text{Worked Hours} \times \text{Guard Hourly Rate}) + \text{Compliance Overtime Penalties} + \text{Regional Dispatch Costs} \right)$$

### 1. Invoices & Receivables Trailing Ledger
*   **Current Days (0-30)**: Standard operating collections pool. 
*   **Warning Days (31-60)**: Automated email payment reminders initiated. User alert labeled yellow.
*   **High Risk Days (61-90)**: Local site supervisor notified. In-app risk meter transitions to Amber.
*   **Critical Bad Debt (91+)**: Direct contract warning generated. System suspends credit facilities. Client is locked from making further visitor invitations.

---

## DELIVERABLE 6: CLOUD FUNCTION SCHEDULER & AGGREGATION PIPELINE

This TypeScript snippet illustrates how the daily analytics dashboard values are constructed server-side inside `Firebase Cloud Functions` without causing high real-time Firestore database lockups.

```typescript
import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

const db = admin.firestore();

/**
 * Nightly Cron Aggregator Engine (00:00 UTC)
 * Resolves mathematical parameters and caches daily dashboard results.
 */
export const runDailyBIAggregation = functions.pubsub
  .schedule('0 0 * * *')
  .timeZone('UTC')
  .onRun(async (context) => {
    const today = new Date().toISOString().split('T')[0];
    const companiesSnapshot = await db.collection('companies').get();

    for (const companyDoc of companiesSnapshot.docs) {
      const companyId = companyDoc.id;
      const comData = companyDoc.data();

      // Parallel reads over operational collections bounded by targeted companyId
      const [sitesSnap, usersSnap, incidentsSnap, attendanceSnap] = await Promise.all([
        db.collection('sites').where('companyId', '==', companyId).get(),
        db.collection('users').where('companyId', '==', companyId).get(),
        db.collection('incidents').where('companyId', '==', companyId).get(),
        db.collection('attendance').where('companyId', '==', companyId).get()
      ]);

      const totalSites = sitesSnap.size;
      const activeSites = sitesSnap.docs.filter((d) => d.data().status === 'Active').length;
      const totalGuards = usersSnap.docs.filter((d) => d.data().role === 'guard').length;
      
      // Calculate trailing attendance scoring
      const onDutyAttendance = attendanceSnap.docs.filter(
        (a) => a.data().status === 'Open'
      ).length;

      const dailyAggRef = db
        .collection('analytics_cache')
        .doc(companyId)
        .collection('daily_metrics')
        .doc(today);

      const payload = {
        id: `daily_agg_${today}`,
        companyId: companyId,
        companyGroupId: comData.companyGroupId || '',
        timestamp: Date.now(),
        dateString: today,
        companySnapshot: {
          totalSites,
          activeSites,
          totalGuards,
          activeGuardsOnDuty: onDutyAttendance,
          reliefGuardsAvailable: Math.max(0, totalGuards - onDutyAttendance),
          totalClients: Math.ceil(totalSites * 0.8),
          unitsManaged: totalSites * 6
        },
        financialSnapshot: {
          monthlyRevenueYTD: activeSites * 15000.0,
          monthlyPayrollYTD: onDutyAttendance * 12 * 25.0 * 30, // Sample analytical computation
          grossMarginPercentage: 30.5
        },
        operationsSnapshot: {
          attendanceComplianceRate: 94.5,
          incidentRateYTD: parseFloat((incidentsSnap.size / Math.max(1, totalSites)).toFixed(1))
        }
      };

      await dailyAggRef.set(payload, { merge: true });
    }

    functions.logger.info(`Daily BI Aggregation Engine executed successfully for all nodes.`);
    return null;
  });
```

---

## DELIVERABLE 7: REAL-TIME EXECUTIVE CHANNELS & PROACTIVE ALERT ENGINE

Super Administrators and Company Directors must receive push channels instantly for major Operational or Financial breaches.

### Alert Classification Hierarchy

| Alert Name | Severity Level | Trigger Condition | Delivery Channels |
| :--- | :--- | :--- | :--- |
| **SOS_BREACH_ALERT** | 🔴 CRITICAL | Active guard beacon raised | High Priority FCM, SMS, UI Flash overlay |
| **COMPLIANCE_LOW_ALERT** | 🟡 WARNING | Trailing site score falls below 90% | In-app notification, Daily Email |
| **PAYROLL_FAIL_ALERT**| 🔴 CRITICAL | Cloud payroll computation failure | System Telemetry Log, SMS to SecOps Lead |
| **INVOICE_OVERDUE_ALERT**| 🟡 WARNING | Invoice payment exceeds 30-day SLA window | In-app corporate feed notification |
| **CONTRACT_EXPIRY_ALERT**| 🟢 INFO | 60 days remaining before SLA expiry | Automated email to Client Account Exec |

---

## DELIVERABLE 8: BIGQUERY & ANALYTICS DATA WAREHOUSE PIPELINE

To support extensive cross-filtering, historical reports, and native Looker Studio / Power BI dashboards, Kundal OS V3 uses a serverless streaming export pipeline.

```
[Firestore Changes] ➔ [Change Data Capture Trigger] ➔ [PubSub Stream] ➔ [gcloud BigQuery BigLake Engine]
```

This streaming configuration removes processing loads from Firestore, preserving execution budgets while unlocking historical ad-hoc reporting.

---

## DELIVERABLE 9: MULTI-TENANT ANALYTICAL CARD (JETPACK COMPOSE MODEL)

The executive application renders dynamic metric screens using optimized Compose canvases, styled around the beautiful navy-and-green professional theme.

```kotlin
package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Modern High-Contrast Analytical Card for Executives.
 * Displays aggregate BI operational values with responsive feedback states.
 */
@Composable
fun ExecutiveAnalyticalCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = value,
                color = Color(0xFF0F172A),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
```

---

## DELIVERABLE 10: ARCHITECTURAL PERFORMANCE SLAS

To guarantee high scalability on all active terminal platforms:

1. **Dashboard Loading Bounds**: All standard analytic overview screens pull metric profiles from cache logs in **under 1.2 seconds**.
2. **Scheduled Execution Limits**: Aggregation Cron tasks finish processing 1,000 document events in **under 45 seconds**.
3. **Bandwidth Optimization**: Firestore dashboard responses are strictly checked on compression to consume **under 10KB** per document read, preventing high operational data costs on executive mobile networks.

---
*Authorized for Production Architecture Release by lead Analytics Group on June 22, 2026.*
