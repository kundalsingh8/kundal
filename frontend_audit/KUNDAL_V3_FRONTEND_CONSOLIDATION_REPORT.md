# KUNDAL SECURITY OS V3 – FRONTEND ARCHITECTURE & UX CONSOLIDATION REPORT
**Revision:** 3.0.0-UX  
**Status:** APPROVED  
**Date:** June 22, 2026  
**Target Audience:** Frontend Architects, Jetpack Compose Engineers, UX/UI Research Leads  

---

## EXECUTIVE SUMMARY
With the successful standardization of the Firestore schemas, security indices, and server-side callable middleware, Phase 3 addresses the **Frontend Architecture & UX Fragmentation**. A comprehensive audit of the client application revealed structural duplication: multiple nested dashboards, divergent page headers, non-uniform table containers, and varying transactional modal patterns across operational modules. 

This document defines the unified, single-activity architectural standard for **Kundal OS V3**. It completely eliminates legacy UI layers, enforcing a single robust **AppShell Navigation System** controlled by contextual role-based permissions, backed by Material Design 3 (M3) Jetpack Compose components.

---

## DELIVERABLE 1: UI ARCHITECTURE MAP (JETPACK COMPOSE)

### 1. Component Map
The design system operates on hierarchical, stateless UI elements wrapped under the centralized `MyApplicationTheme` layer.

```
+--------------------------------------------------------------------------------+
|                         MyApplicationTheme (LightColorScheme)                  |
|  +--------------------------------------------------------------------------+  |
|  |                             AppShell (Scaffold)                          |  |
|  |  +--------------------------------------------------------------------+  |  |
|  |  |                      PageHeader (Adaptive UX)                      |  |  |
|  |  +--------------------------------------------------------------------+  |  |
|  |  +-------------------------------+ +----------------------------------+  |  |
|  |  |  Left-Sidebar Navigation Rail | |          Active Content          |  |  |
|  |  |  (Allowed Roles Menu)         | |  +----------------------------+  |  |  |
|  |  |                               | |  |     PageHeader + KPIs      |  |  |  |
|  |  |                               | |  +----------------------------+  |  |  |
|  |  |                               | |  |     FiltersBar / Search    |  |  |  |
|  |  |                               | |  +----------------------------+  |  |  |
|  |  |                               | |  |         Data Grid          |  |  |  |
|  |  |                               | |  +----------------------------+  |  |  |
|  |  |                               | |  |    Interactive Drawer      |  |  |  |
|  |  +-------------------------------+ +----------------------------------+  |  |
|  +--------------------------------------------------------------------------+  |
+--------------------------------------------------------------------------------+
```

### 2. Layout Structure (Adaptive Canonical Design)
*   **Mobile Mode (Compact)**: Collapsed bottom navigation rail, floating actions overlay, full-screen sheets.
*   **Tablet/Desktop Mode (Expanded)**: Fixed side-aligned Navigation Rail, side-by-side split screens (Left: Data Cards, Right: Supporting Details Pane).

---

## DELIVERABLE 2: UX REVIEW & CLEANUP PLAN

### 1. Duplicate UI Audit & Cleanup Strategy

| Legacy Fragment / View | Current State | Refactored V3 Strategy |
| :--- | :--- | :--- |
| `LegacyGuardDashboard` | **RETIRED** | Merged into standard `OverviewPane` using conditional layouts. |
| `SiteDetailModal` | **RETIRED** | Migrated to dedicated, standard bottom drawer panels or full screen routes. |
| `AreaOfficeSelector` | **PURGED** | Completely removed from UI. Scope bounded directly by `Site` and `Company` parameters. |
| `IncidentCreateModal` | **STANDARDIZED**| Migrated to single unified `CreateDialog`/`EditDialog` template pattern. |

### 2. Navigation Consolidation Map
Visible screens are explicitly indexed according to security roles. The global navigation drawer is calculated programmatically at composition runtime:

```kotlin
val navTree = listOf(
    NavGroup(
        title = "Overview",
        items = listOf(NavItem("Overview", Icons.Default.Analytics, roles = listOf("super_admin", "group_admin", "company_admin", "site_admin", "guard")))
    ),
    NavGroup(
        title = "Operations",
        items = listOf(
            NavItem("Sites", Icons.Default.Business, roles = listOf("super_admin", "group_admin", "company_admin")),
            NavItem("Workforce", Icons.Default.People, roles = listOf("super_admin", "group_admin", "company_admin", "site_admin")),
            NavItem("Attendance", Icons.Default.CheckCircle, roles = listOf("super_admin", "company_admin", "site_admin", "guard")),
            NavItem("Visitors", Icons.Default.Person, roles = listOf("super_admin", "site_admin", "resident")),
            NavItem("Incidents", Icons.Default.Assignment, roles = listOf("super_admin", "company_admin", "site_admin", "guard")),
            NavItem("SOS", Icons.Default.Warning, roles = listOf("super_admin", "company_admin", "site_admin", "guard"))
        )
    ),
    NavGroup(
        title = "Finance",
        items = listOf(
            NavItem("Payroll", Icons.Default.AttachMoney, roles = listOf("super_admin", "group_admin", "company_admin")),
            NavItem("Billing", Icons.Default.Receipt, roles = listOf("super_admin", "group_admin", "company_admin"))
        )
    ),
    NavGroup(
        title = "Administration",
        items = listOf(
            NavItem("Audits", Icons.Default.Lock, roles = listOf("super_admin", "group_admin"))
        )
    )
)
```

---

## DELIVERABLE 3: DESIGN SYSTEM COMPONENTS SPECIFICATION

To ensure visual consistency and pixel-perfect layouts, developers must use the following pre-configured Jetpack Compose interfaces. These styles maintain the beautiful **Professional Polish** theme:

### 1. Modern Page Header Component
```kotlin
@Composable
fun PageHeader(
    title: String,
    subtitle: String,
    actionButton: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF191C1B),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF3F4945)
            )
        }
        actionButton?.invoke()
    }
}
```

### 2. High-Fidelity MetricCard (KPI Panels)
```kotlin
@Composable
fun MetricCard(
    title: String,
    value: String,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
            .border(1.dp, Color(0xFFD0DED9), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.toUpperCase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3F4945),
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
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
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                color = Color(0xFF191C1B)
            )
        }
    }
}
```

---

## DELIVERABLE 4: STANDARD PAGE STRUCTURE LAYOUT

To enforce identical user flow configurations, every standard content screen in the Kundal OS V3 UI must follow this strict vertical layout layout:

```
[ PageHeader / Title Block ]
             ↓
[ Horizontally Scrolled MetricCard KPIs Panel ]
             ↓
[ FilterBar: Text Search, Status Badges, Scope Toggles ]
             ↓
[ LazyColumn Data Grid Items Area ]
             ↓
[ Supporting Actions Area & Swipe-Up Detail Sheets ]
```

### Performance Optimization Directives
1.  **State Placement**: Hoist all network or database events into the corresponding `ViewModel`. Use Kotlin Flows with `.collectAsStateWithLifecycle()` to prevent memory leaks.
2.  **Explicit Memory Bounding**: Wrap heavily iterated list cards in `remember(item.id)` wrappers to prevent redundant recompositions when auxiliary data parameters update.
3.  **Code Splitting**: Avoid combining disparate screen rendering components into a single monolithic source file. Modularize each pane into separate view package containers.

---
*Verified and Certified by Lead UI/UX Architect.*
