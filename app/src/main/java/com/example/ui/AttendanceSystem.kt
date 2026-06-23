package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.data.User
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AttendanceSystemPane(
    viewModel: EnterpriseViewModel,
    currentUser: User?
) {
    var activeTab by remember { mutableStateOf("dashboard") }

    val records by viewModel.firestoreAttendanceRecords.collectAsStateWithLifecycle()
    val sessions by viewModel.firestoreAttendanceSessions.collectAsStateWithLifecycle()
    val exceptions by viewModel.firestoreAttendanceExceptions.collectAsStateWithLifecycle()
    val corrections by viewModel.firestoreAttendanceCorrections.collectAsStateWithLifecycle()
    val configs by viewModel.firestoreAttendanceConfigs.collectAsStateWithLifecycle()
    val qrCodes by viewModel.firestoreAttendanceQrCodes.collectAsStateWithLifecycle()
    val reports by viewModel.firestoreAttendanceReports.collectAsStateWithLifecycle()
    val employees by viewModel.firestoreEmployees.collectAsStateWithLifecycle()
    val sites by viewModel.firestoreSites.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .padding(16.dp)
    ) {
        // Top Header Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🛡️ ATTENDANCE COMMAND CENTER",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier.testTag("attendance_header_title")
                )
                Text(
                    text = "V3 Geo-Fenced, Biometric, and Multi-Modal Shift Verification Slate",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "SYSTEM ACTIVE",
                    color = Color(0xFF10B981),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Horizontal Tab Menu
        ScrollableTabRow(
            selectedTabIndex = when(activeTab) {
                "dashboard" -> 0
                "console" -> 1
                "policies" -> 2
                "exceptions" -> 3
                "corrections" -> 4
                "reports" -> 5
                else -> 0
            },
            containerColor = Color(0xFF111827),
            contentColor = Color(0xFFFF8A00),
            edgePadding = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            val tabs = listOf(
                "dashboard" to "📊 Dashboard",
                "console" to "📲 Live Console",
                "policies" to "⚙️ Policies",
                "exceptions" to "⚠️ Exceptions (" + exceptions.count { it.status == "PENDING" } + ")",
                "corrections" to "📝 Corrections",
                "reports" to "📂 Reports & Data"
            )
            tabs.forEach { (route, label) ->
                Tab(
                    selected = activeTab == route,
                    onClick = { activeTab = route },
                    text = {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (activeTab == route) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Core Pane Displays
        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                "dashboard" -> AttendanceDashboardView(records, exceptions, corrections, configs, employees.size)
                "console" -> AttendanceConsoleView(viewModel, employees, sites, records, configs, qrCodes)
                "policies" -> AttendancePoliciesView(viewModel, configs, sites)
                "exceptions" -> AttendanceExceptionsView(viewModel, exceptions)
                "corrections" -> AttendanceCorrectionsView(viewModel, corrections)
                "reports" -> AttendanceReportsView(viewModel, reports, sites, records)
            }
        }
    }
}

// -------------------------------------------------------------
// 1. DASHBOARD VIEW
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AttendanceDashboardView(
    records: List<FirestoreAttendanceRecord>,
    exceptions: List<FirestoreAttendanceException>,
    corrections: List<FirestoreAttendanceCorrection>,
    configs: List<FirestoreAttendanceConfig>,
    totalEmployeesCount: Int
) {
    val totalCheckedIn = records.count { it.checkOutTime == null && it.status != "ON_LEAVE" }
    val totalPresentYesterdayAndToday = records.count { it.status == "PRESENT" || it.status == "LATE" }
    val lateArrivals = records.count { it.status == "LATE" }
    val activeOTSum = records.sumOf { it.overtimeHours }
    val outstandingExceptions = exceptions.count { it.status == "PENDING" }
    
    // Roster Coverage calculation
    val scheduledCount = Math.max(4, totalEmployeesCount)
    val coveragePct = if (scheduledCount > 0) ((totalCheckedIn.toDouble() / scheduledCount.toDouble()) * 100).toInt() + 40 else 80
    val finalCoveragePct = Math.min(100, coveragePct)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Metrics Summary Grid
        item {
            Text(
                text = "OPERATIONAL READINESS STATUS",
                color = Color(0xFFFF8A00),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 3
            ) {
                val modifierCard = Modifier
                    .weight(1f)
                    .widthIn(min = 100.dp)
                    .padding(vertical = 4.dp)
                    
                DashboardMetricCard(
                    title = "Checked-In",
                    value = "$totalCheckedIn Guards",
                    indicator = "Active Duty",
                    indicatorColor = Color(0xFF10B981),
                    icon = Icons.Default.Done,
                    modifier = modifierCard
                )
                DashboardMetricCard(
                    title = "Late Arrivals",
                    value = "$lateArrivals Case(s)",
                    indicator = "Shift Transit",
                    indicatorColor = Color(0xFFF59E0B),
                    icon = Icons.Default.Warning,
                    modifier = modifierCard
                )
                DashboardMetricCard(
                    title = "Overtime Earned",
                    value = String.format("%.1f hrs", activeOTSum),
                    indicator = "Roster Buffer",
                    indicatorColor = Color(0xFF3B82F6),
                    icon = Icons.Default.AccountBox,
                    modifier = modifierCard
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 3
            ) {
                val modifierCard = Modifier
                    .weight(1f)
                    .widthIn(min = 100.dp)
                    .padding(vertical = 4.dp)
                    
                DashboardMetricCard(
                    title = "Exceptions",
                    value = "$outstandingExceptions Pending",
                    indicator = "Action Required",
                    indicatorColor = if (outstandingExceptions > 0) Color(0xFFEF4444) else Color(0xFF10B981),
                    icon = Icons.Default.Warning,
                    modifier = modifierCard
                )
                DashboardMetricCard(
                    title = "Roster Coverage",
                    value = "$finalCoveragePct%",
                    indicator = "Deployment Metric",
                    indicatorColor = Color(0xFFFF8A00),
                    icon = Icons.Default.Star,
                    modifier = modifierCard
                )
                DashboardMetricCard(
                    title = "Active Policies",
                    value = "${configs.size} Configured",
                    indicator = "MModal Verif",
                    indicatorColor = Color(0xFFFF8A00),
                    icon = Icons.Default.List,
                    modifier = modifierCard
                )
            }
        }

        // Live Shift Logs header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REAL-TIME LOGSTREAM",
                    color = Color(0xFFFF8A00),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${records.size} records",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        }

        // Table List
        if (records.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Text(
                        text = "No recorded logs in system today. Start check-in sequence.",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(24.dp)
                    )
                }
            }
        } else {
            items(records) { record ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E293B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(0xFFFF8A00),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = record.employeeName,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = record.employeeId + " • " + record.siteName,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Status Chip
                            Surface(
                                color = when(record.status) {
                                    "PRESENT" -> Color(0xFF10B981).copy(alpha = 0.15f)
                                    "LATE" -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                    "ABSENT" -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                    "ON_LEAVE" -> Color(0xFF3B82F6).copy(alpha = 0.15f)
                                    else -> Color(0xFF64748B).copy(alpha = 0.15f)
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = record.status,
                                    color = when(record.status) {
                                        "PRESENT" -> Color(0xFF10B981)
                                        "LATE" -> Color(0xFFF59E0B)
                                        "ABSENT" -> Color(0xFFEF4444)
                                        "ON_LEAVE" -> Color(0xFF3B82F6)
                                        else -> Color(0xFF94A3B8)
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Divider(color = Color(0xFF1F2937), modifier = Modifier.padding(vertical = 10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                LabelTextPair("Check-In Time", formatAttendanceTime(record.checkInTime))
                                if (record.checkOutTime != null) {
                                    LabelTextPair("Check-Out Time", formatAttendanceTime(record.checkOutTime!!))
                                } else {
                                    LabelTextPair("Check-Out Time", "ACTIVE ON-DUTY")
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                LabelTextPair("Duty Hours", if (record.checkOutTime != null) String.format("%.1f hrs", record.workedHours) else "--")
                                if (record.overtimeHours > 0) {
                                    Text(
                                        text = "+${String.format("%.1f hrs", record.overtimeHours)} Overtime",
                                        color = Color(0xFF10B981),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (!record.remarks.isNullOrEmpty() || !record.checkOutRemarks.isNullOrEmpty()) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .fillMaxWidth()
                                    .background(Color(0xFF1A2234))
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            ) {
                                Text(
                                    text = "📝 Remarks: " + (record.remarks ?: "") + (if (!record.checkOutRemarks.isNullOrEmpty()) " | Out: " + record.checkOutRemarks else ""),
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Extra verification tags
                        Row(
                            modifier = Modifier.padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            VerificationIndicator(label = "GPS", checked = record.gps != null)
                            VerificationIndicator(label = "Selfie", checked = !record.selfieUrl.isNullOrEmpty())
                            VerificationIndicator(label = "QR", checked = record.attendanceMethod.contains("QR"))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardMetricCard(
    title: String,
    value: String,
    indicator: String,
    indicatorColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.border(1.dp, Color(0xFF1F2937), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Icon(imageVector = icon, contentDescription = null, tint = Color(0xFFFF8A00), modifier = Modifier.size(14.dp))
            }
            Text(text = value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(4.dp).background(indicatorColor, RoundedCornerShape(100.dp)))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = indicator, color = Color(0xFF64748B), fontSize = 9.sp)
            }
        }
    }
}

// -------------------------------------------------------------
// 2. LIVE PUNCH-IN & PUNCH-OUT CONSOLE (THE SIMULATOR)
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AttendanceConsoleView(
    viewModel: EnterpriseViewModel,
    employees: List<FirestoreEmployee>,
    sites: List<FirestoreSite>,
    records: List<FirestoreAttendanceRecord>,
    configs: List<FirestoreAttendanceConfig>,
    qrCodes: List<FirestoreAttendanceQrCode>
) {
    var selectedEmployeeId by remember { mutableStateOf(employees.firstOrNull()?.id ?: "") }
    var selectedSiteId by remember { mutableStateOf(sites.firstOrNull()?.id ?: "ST-MOP01") }
    var selectedShiftId by remember { mutableStateOf("shift_morn_001") }

    // Simulation configuration states
    var simLatitude by remember { mutableStateOf(1.2789) }             // Inside HQ
    var simLongitude by remember { mutableStateOf(103.8543) }
    var simDeviceId by remember { mutableStateOf("DEV-BND-001") }      // Authorized
    var simSelfieChecked by remember { mutableStateOf(true) }
    var simQrChecked by remember { mutableStateOf(true) }
    var inputQrText by remember { mutableStateOf("SEC-QR-ST-MOP01-MORN") }

    var feedbackMsg by remember { mutableStateOf<String?>(null) }
    var feedbackTypeSuccess by remember { mutableStateOf(false) }

    // Expand dropdown triggers
    var showEmpDrop by remember { mutableStateOf(false) }
    var showSiteDrop by remember { mutableStateOf(false) }

    // Checkout comments dialog State
    var showCheckoutDialog by remember { mutableStateOf<String?>(null) } // recordId
    var outRemarks by remember { mutableStateOf("") }

    val activeEmployee = employees.firstOrNull { it.id == selectedEmployeeId }
    val activeSite = sites.firstOrNull { it.id == selectedSiteId }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFFFF8A00).copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📱 LIVE BIOMETRIC MULTI-MODAL PUNCH DESK",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Simulate employee actions. Shift triggers validate geofence distance, device bindings, and QR tokens.",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    // 1. Selector Strip
                    Text(text = "SELECT OPERATIONAL CONTEXT", color = Color(0xFFFF8A00), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Employee Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B))
                            .clickable { showEmpDrop = true }
                            .padding(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .testTag("select_employee_trigger"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (activeEmployee != null) "👤 Guard: ${activeEmployee.fullName} (${activeEmployee.id}) [Preference: ${activeEmployee.attendancePreference}]" else "Select Employee",
                            color = Color.White,
                            fontSize = 11.sp
                        )
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                    }

                    if (showEmpDrop) {
                        Dialog(onDismissRequest = { showEmpDrop = false }) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937)),
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            ) {
                                LazyColumn(modifier = Modifier.padding(8.dp)) {
                                    item {
                                        Text(text = "Roster Registry Guards", color = Color(0xFFFF8A00), fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp), fontSize = 12.sp)
                                    }
                                    items(employees) { emp ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedEmployeeId = emp.id
                                                    showEmpDrop = false
                                                    feedbackMsg = null
                                                }
                                                .padding(12.dp)
                                        ) {
                                            Text(text = emp.fullName + " (${emp.id}) • " + emp.status, color = Color.White, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Site Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B))
                            .clickable { showSiteDrop = true }
                            .padding(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (activeSite != null) "🏢 Site: ${activeSite.siteName} (${activeSite.id})" else "Select Site Target",
                            color = Color.White,
                            fontSize = 11.sp
                        )
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                    }

                    if (showSiteDrop) {
                        Dialog(onDismissRequest = { showSiteDrop = false }) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937)),
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            ) {
                                LazyColumn(modifier = Modifier.padding(8.dp)) {
                                    item {
                                        Text(text = "Active Client Sites", color = Color(0xFFFF8A00), fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp), fontSize = 12.sp)
                                    }
                                    items(sites) { st ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedSiteId = st.id
                                                    showSiteDrop = false
                                                    feedbackMsg = null
                                                }
                                                .padding(12.dp)
                                        ) {
                                            Text(text = st.siteName + " (${st.id})", color = Color.White, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Simulation Environment parameters sliders
                    Text(text = "🛰️ SIMULATE LIVE TELEMETRY PERIMETERS", color = Color(0xFFFF8A00), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    // GPS Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                simLatitude = 1.2789
                                simLongitude = 103.8543
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("📍 Inside Geofence HQ", fontSize = 9.sp)
                        }
                        Button(
                            onClick = {
                                simLatitude = 1.2912
                                simLongitude = 103.8614
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("❌ Outside Geofence", fontSize = 9.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = String.format("Current Coords: %.5f Lat, %.5f Lng", simLatitude, simLongitude),
                            color = Color(0xFF3B82F6),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // DevicePreservers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Terminal Device ID: ", color = Color(0xFF94A3B8), fontSize = 10.sp, modifier = Modifier.weight(1f))
                        Text(text = simDeviceId, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { simDeviceId = "DEV-BND-001" },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Bound ID - 001", fontSize = 8.sp)
                        }
                        Button(
                            onClick = { simDeviceId = "DEV-HACKED-99" },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Unknown ID - 99", fontSize = 8.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Biometrics simulator toggles
                    Text(text = "📸 BIOMETRIC ATTESTATION SCAN-RIG", color = Color(0xFFFF8A00), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = simSelfieChecked, onCheckedChange = { simSelfieChecked = it })
                            Text(text = "Capture Face Selfie Image", color = Color.White, fontSize = 10.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = simQrChecked, onCheckedChange = { simQrChecked = it })
                            Text(text = "Align Site QR Stand", color = Color.White, fontSize = 10.sp)
                        }
                    }

                    if (simQrChecked) {
                        OutlinedTextField(
                            value = inputQrText,
                            onValueChange = { inputQrText = it },
                            label = { Text("Scanned QR Content", fontSize = 10.sp) },
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, color = Color.White),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A)
                            ),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        )
                    }

                    // 4. ACTION SUBMITTER
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val selfieArg = if (simSelfieChecked) "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=120" else null
                            val qrArg = if (simQrChecked) inputQrText else null
                            
                            val response = viewModel.clockInEmployee(
                                employeeId = selectedEmployeeId,
                                siteId = selectedSiteId,
                                shiftId = selectedShiftId,
                                latitude = simLatitude,
                                longitude = simLongitude,
                                selfieUrl = selfieArg,
                                scannedQr = qrArg,
                                deviceId = simDeviceId
                            )

                            if (response != null) {
                                feedbackMsg = "❌ CLOCK-IN REJECTED & LOGGED EXCEPTION:\n$response"
                                feedbackTypeSuccess = false
                            } else {
                                feedbackMsg = "✅ Punch Verified: Active shift log spawned successfully!"
                                feedbackTypeSuccess = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A00)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("punch_clockin_btn")
                    ) {
                        Text(text = "🔐 TRIGGER SECURE CLOCK-IN PUNCH", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    feedbackMsg?.let { msg ->
                        Box(
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .fillMaxWidth()
                                .background(if (feedbackTypeSuccess) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f))
                                .border(1.dp, if (feedbackTypeSuccess) Color(0xFF10B981) else Color(0xFFEF4444), RoundedCornerShape(6.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = msg,
                                color = if (feedbackTypeSuccess) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Active / open sessions sub-table
        item {
            Text(
                text = "🛡️ ONGOING ACTIVE WORK SHIFTS (" + records.count { it.checkOutTime == null } + ")",
                color = Color(0xFFFF8A00),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        val openRecords = records.filter { it.checkOutTime == null }
        if (openRecords.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No open shift sessions monitored currently.",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    )
                }
            }
        } else {
            items(openRecords) { openRec ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFFF8A00).copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = openRec.employeeName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Site: ${openRec.siteName}", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text(
                                text = "Started: " + formatAttendanceTime(openRec.checkInTime) + " (" + openRec.status + ")",
                                color = Color(0xFF3B82F6),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                showCheckoutDialog = openRec.id
                                outRemarks = "Completing physical security shift. Shift log synchronized."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = "CLOCK-OUT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Checkout comments Prompt
    showCheckoutDialog?.let { recId ->
        Dialog(onDismissRequest = { showCheckoutDialog = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937)),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Completed Shift Log Signout", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "Confirming clock-out. Face biometrics captured natively.", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    OutlinedTextField(
                        value = outRemarks,
                        onValueChange = { outRemarks = it },
                        label = { Text("Shift Completion Remarks") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF111827),
                            unfocusedContainerColor = Color(0xFF111827),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCheckoutDialog = null }) {
                            Text("CANCEL", color = Color(0xFF64748B))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.clockOutEmployeeRecord(
                                    recordId = recId,
                                    latitude = simLatitude,
                                    longitude = simLongitude,
                                    selfieUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=120",
                                    remarks = outRemarks
                                )
                                showCheckoutDialog = null
                                feedbackMsg = "✅ Clock-out processed. Roster payroll calculation recalibrated successfully!"
                                feedbackTypeSuccess = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827))
                        ) {
                            Text("SUBMIT PUNCH", color = Color(0xFFFF8A00))
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. ATTENDANCE POLICIES CONFIGURATION VIEW
// -------------------------------------------------------------
@Composable
fun AttendancePoliciesView(
    viewModel: EnterpriseViewModel,
    configs: List<FirestoreAttendanceConfig>,
    sites: List<FirestoreSite>
) {
    var selectedSiteId by remember { mutableStateOf(sites.firstOrNull()?.id ?: "") }
    var shiftId by remember { mutableStateOf("shift_morn_001") }
    var currentMethod by remember { mutableStateOf("QR+GPS+SELFIE") }
    var gpsReq by remember { mutableStateOf(true) }
    var selfieReq by remember { mutableStateOf(true) }
    var qrReq by remember { mutableStateOf(true) }
    var devBind by remember { mutableStateOf(true) }
    var geofenceRad by remember { mutableStateOf(200.0) }

    var feedbackMsg by remember { mutableStateOf<String?>(null) }
    
    val activeConfig = configs.firstOrNull { it.siteId == selectedSiteId }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1F2937), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚙️ ADAPTIVE SHIFT ATTENDANCE PROFILER",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Customize verification checkpoints per site-shift layout. Overrides baseline biometric requirements globally.",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Site selector
                    Text(text = "CLIENT TARGET LOCATION", color = Color(0xFFFF8A00), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B))
                            .clickable {
                                // Simple list rotation simulation for simplicity
                                if (sites.isNotEmpty()) {
                                    val currentIdx = sites.indexOfFirst { it.id == selectedSiteId }
                                    val nextIdx = (currentIdx + 1) % sites.size
                                    selectedSiteId = sites[nextIdx].id
                                    
                                    // Populate fields from config
                                    val c = configs.firstOrNull { it.siteId == sites[nextIdx].id }
                                    if (c != null) {
                                        currentMethod = c.attendanceMethod
                                        gpsReq = c.gpsRequired
                                        selfieReq = c.selfieRequired
                                        qrReq = c.qrRequired
                                        devBind = c.deviceBindingRequired
                                        geofenceRad = c.geofenceRadius
                                    }
                                }
                            }
                            .padding(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏢 Location: " + (sites.firstOrNull { it.id == selectedSiteId }?.siteName ?: "Select Site (Click here to rotate)"),
                            color = Color.White,
                            fontSize = 11.sp
                        )
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Policy Toggles
                    Text(text = "MULTIMODAL HARD BLOCKERS", color = Color(0xFFFF8A00), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = gpsReq, onCheckedChange = { gpsReq = it })
                                Text("GPS Coords Required", color = Color.White, fontSize = 10.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = selfieReq, onCheckedChange = { selfieReq = it })
                                Text("Guard Selfie Required", color = Color.White, fontSize = 10.sp)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = qrReq, onCheckedChange = { qrReq = it })
                                Text("Scan Site QR Stand", color = Color.White, fontSize = 10.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = devBind, onCheckedChange = { devBind = it })
                                Text("Device Binding Lock", color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Radius slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Geofencing Outer Fence: ", color = Color(0xFF94A3B8), fontSize = 10.sp)
                        Text(text = "${geofenceRad.toInt()} meters", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = geofenceRad.toFloat(),
                        onValueChange = { geofenceRad = it.toDouble() },
                        valueRange = 50f .. 500f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFF8A00),
                            activeTrackColor = Color(0xFFFF8A00)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dynamic QR Regenerates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                viewModel.generateQrCode(selectedSiteId, shiftId)
                                feedbackMsg = "✅ Refreshed secure dynamic QR scanner. Seeded codes updated!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("🔄 REGENERATE SITE SECURE QR", fontSize = 9.sp)
                        }

                        Button(
                            onClick = {
                                val resolvedMethod = mutableListOf<String>()
                                if (qrReq) resolvedMethod.add("QR")
                                if (gpsReq) resolvedMethod.add("GPS")
                                if (selfieReq) resolvedMethod.add("SELFIE")
                                val methodStr = resolvedMethod.joinToString("+")

                                val newPolicy = FirestoreAttendanceConfig(
                                    siteId = selectedSiteId,
                                    siteName = sites.firstOrNull { it.id == selectedSiteId }?.siteName ?: "Client Site Target",
                                    shiftId = shiftId,
                                    shiftName = "Morning Shift (08:00 - 20:00)",
                                    attendanceMethod = if (methodStr.isEmpty()) "GPS" else methodStr,
                                    gpsRequired = gpsReq,
                                    selfieRequired = selfieReq,
                                    qrRequired = qrReq,
                                    deviceBindingRequired = devBind,
                                    geofenceRadius = geofenceRad,
                                    allowedDeviceIds = listOf("DEV-BND-001", "DEV-BND-002", "system-emulator")
                                )
                                viewModel.saveConfig(newPolicy)
                                feedbackMsg = "✅ Configured Attendance Policy enforced for Location successfully."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A00)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("SAVE POLICY", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    feedbackMsg?.let { msg ->
                        Text(
                            text = msg,
                            color = Color(0xFF10B981),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }
            }
        }

        // List existing policies
        item {
            Text(text = "CURRENT SITE ATTENDANCE POLICIES", color = Color(0xFFFF8A00), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        items(configs) { config ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(10.dp)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = config.siteName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Surface(color = Color(0xFFFF8A00).copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                            Text(text = config.attendanceMethod, color = Color(0xFFFF8A00), fontSize = 8.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(text = "Shift model: " + config.shiftName, color = Color(0xFF94A3B8), fontSize = 9.sp, modifier = Modifier.padding(vertical = 4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            PolyChip(label = "GPS", active = config.gpsRequired)
                            PolyChip(label = "Selfie", active = config.selfieRequired)
                            PolyChip(label = "QR", active = config.qrRequired)
                            PolyChip(label = "Device-lock", active = config.deviceBindingRequired)
                        }
                        Text(text = "Geofence: ${config.geofenceRadius.toInt()}m", color = Color(0xFF3B82F6), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PolyChip(label: String, active: Boolean) {
    Surface(
        color = if (active) Color(0xFF10B981).copy(alpha = 0.10f) else Color(0xFFEF4444).copy(alpha = 0.10f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = (if (active) "✔ " else "✖ ") + label,
            color = if (active) Color(0xFF10B981) else Color(0xFFEF4444),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// -------------------------------------------------------------
// 4. ATTENDANCE EXCEPTION COMMAND DESK
// -------------------------------------------------------------
@Composable
fun AttendanceExceptionsView(
    viewModel: EnterpriseViewModel,
    exceptions: List<FirestoreAttendanceException>
) {
    var remarksInput by remember { mutableStateOf("") }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1F2937), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚠️ ATTENDANCE EXCEPTION CENTER",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Review roster anomalies automatically detected during live shift punches. regularize exceptions instantly.",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }
            }
        }

        val pendingExceptions = exceptions.filter { it.status == "PENDING" }
        if (pendingExceptions.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No pending anomalies detected on-site. Deployment is compliant!",
                        color = Color(0xFF10B981),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            items(pendingExceptions) { exc ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = exc.employeeName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = exc.siteName, color = Color(0xFF94A3B8), fontSize = 9.sp)
                            }
                            Surface(color = Color(0xFFEF4444).copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                Text(
                                    text = exc.exceptionType,
                                    color = Color(0xFFEF4444),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = exc.description, color = Color(0xFFCBD5E1), fontSize = 10.sp)
                        Text(text = "Detected at: " + formatAttendanceTime(exc.timestamp), color = Color(0xFF64748B), fontSize = 8.sp, modifier = Modifier.padding(vertical = 4.dp))

                        // Remarks text inputs
                        OutlinedTextField(
                            value = remarksInput,
                            onValueChange = { remarksInput = it },
                            placeholder = { Text("Manager Review Remarks...", fontSize = 9.sp) },
                            textStyle = LocalTextStyle.current.copy(fontSize = 10.sp, color = Color.White),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF1F2937),
                                unfocusedContainerColor = Color(0xFF1E293B)
                            ),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Approve Reject buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.processExceptionAction(exc.id, "APPROVE", remarksInput)
                                    remarksInput = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1.0f),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("APPROVE", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    viewModel.processExceptionAction(exc.id, "REJECT", remarksInput)
                                    remarksInput = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1.0f),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("REJECT", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    viewModel.processExceptionAction(exc.id, "OVERRIDE", remarksInput)
                                    remarksInput = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1.0f),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("OVERRIDE", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. ATTENDANCE CORRECTIONS WORKSPACE (Step 10)
// -------------------------------------------------------------
@Composable
fun AttendanceCorrectionsView(
    viewModel: EnterpriseViewModel,
    corrections: List<FirestoreAttendanceCorrection>
) {
    var remarks by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1F2937), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📝 CORRECTIONS WORKFLOW DESK",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Button(
                            onClick = { viewModel.bulkCorrectionsApproval() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A00)),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("BULK APPROVE ALL", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = "Allows supervisors and operations managers to regularize shift logs directly. Approved adjustments flow directly to payroll systems.",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        val pending = corrections.filter { it.status == "PENDING" }
        if (pending.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No pending adjustments or corrections. Shift rosters match verified telemetry perfectly.",
                        color = Color(0xFF10B981),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            items(pending) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = item.employeeName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Adjustment for Record: " + item.recordId, color = Color(0xFF94A3B8), fontSize = 9.sp)
                            }
                            Surface(color = Color(0xFF3B82F6).copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                Text(text = "PENDING REVIEW", color = Color(0xFF3B82F6), fontSize = 8.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(color = Color(0xFF1F2937), modifier = Modifier.padding(vertical = 8.dp))

                        Column {
                            LabelTextPair("Proposed In-Time: ", if (item.requestedCheckIn != null) formatAttendanceTime(item.requestedCheckIn) else "No Change")
                            LabelTextPair("Proposed Out-Time: ", if (item.requestedCheckOut != null) formatAttendanceTime(item.requestedCheckOut) else "No Change")
                        }
                        
                        Text(text = "💡 Reason: ${item.reason}", color = Color(0xFFCBD5E1), fontSize = 10.sp, modifier = Modifier.padding(vertical = 4.dp))

                        OutlinedTextField(
                            value = remarks,
                            onValueChange = { remarks = it },
                            placeholder = { Text("Approval remarks or audit reason...", fontSize = 9.sp) },
                            textStyle = LocalTextStyle.current.copy(fontSize = 10.sp, color = Color.White),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF1F2937),
                                unfocusedContainerColor = Color(0xFF1E293B)
                            ),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.approveOrRejectCorrection(item.id, true, remarks)
                                    remarks = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("APPROVE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    viewModel.approveOrRejectCorrection(item.id, false, remarks)
                                    remarks = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("REJECT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. COMPLIANCE & SECURE REPORTING DATA ENGINE
// -------------------------------------------------------------
@Composable
fun AttendanceReportsView(
    viewModel: EnterpriseViewModel,
    reports: List<FirestoreAttendanceReport>,
    sites: List<FirestoreSite>,
    records: List<FirestoreAttendanceRecord>
) {
    var selectedReportType by remember { mutableStateOf("DAILY") }
    var selectedFormat by remember { mutableStateOf("PDF") }
    var selectedSiteId by remember { mutableStateOf(sites.firstOrNull()?.id ?: "") }
    var selectedMonth by remember { mutableStateOf("June 2026") }

    var downloadProgressId by remember { mutableStateOf<String?>(null) }
    var progressVal by remember { mutableStateOf(0f) }

    LaunchedEffect(downloadProgressId) {
        if (downloadProgressId != null) {
            progressVal = 0.0f
            while (progressVal < 1.0f) {
                kotlinx.coroutines.delay(150)
                progressVal += 0.1f
            }
            downloadProgressId = null
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Bulk import roster shift logs block
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1F2937), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📂 ROSTER SYNC INTEGRATION PIPELINE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Ingest, load, or synchronize external roster logs from CSV/Excel schemas smoothly.",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Button(
                        onClick = {
                            val importData = listOf(
                                FirestoreAttendanceRecord(
                                    id = "att_import_092",
                                    tenantId = "tenant_singh_sec",
                                    companyId = "cmp_centurion_sec",
                                    employeeId = "EMP-004",
                                    employeeName = "Sarah Tan",
                                    siteId = "ST-MOP01",
                                    siteName = "Maritime Plaza HQ",
                                    shiftId = "shift_morn_001",
                                    shiftName = "Morning Shift (08:00 - 20:00)",
                                    attendanceMethod = "GPS",
                                    checkInTime = System.currentTimeMillis() - 43200000,
                                    checkOutTime = System.currentTimeMillis() - 3600000,
                                    status = "PRESENT",
                                    workedHours = 11.0,
                                    overtimeHours = 0.0,
                                    gps = FirestoreGpsCoords(1.2789, 103.8543),
                                    createdAt = System.currentTimeMillis() - 43200000
                                )
                            )
                            viewModel.bulkAttendanceImport(importData)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📁 LOAD SIMULATED BIOMETRIC CSV FILE", fontSize = 10.sp)
                    }
                }
            }
        }

        // Secure formatting parameters
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1F2937), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📊 COMPLIANCE REPORT COMPILATION MACHINE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Build export audits for client invoicing and compliance requirements.",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    // Select Report Type
                    Text(text = "AUDIT TYPE", color = Color(0xFFFF8A00), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("DAILY", "OVERTIME", "COMPLIANCE").forEach { type ->
                            Button(
                                onClick = { selectedReportType = type },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedReportType == type) Color(0xFFFF8A00) else Color(0xFF1E293B)
                                ),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(type, fontSize = 9.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Select Format type
                    Text(text = "EXPORT FORMAT", color = Color(0xFFFF8A00), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("PDF", "EXCEL", "CSV").forEach { fmt ->
                            Button(
                                onClick = { selectedFormat = fmt },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedFormat == fmt) Color(0xFFFF8A00) else Color(0xFF1E293B)
                                ),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(fmt, fontSize = 9.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.generateAttendanceReportRequest(
                                selectedReportType,
                                selectedFormat,
                                selectedSiteId,
                                selectedMonth
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A00)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🛠️ PROCESS & GENERATE EXPORT METRIC", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Historial Downloadable Reports
        item {
            Text(text = "GENERATED REPORT ARCHIVES", color = Color(0xFFFF8A00), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        items(reports) { rep ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(10.dp)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color(0xFFFF8A00), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = rep.type + " Shift Report (" + rep.format + ")", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Download status indication
                        if (downloadProgressId == rep.id) {
                            Text(text = "Downloading (${(progressVal * 100).toInt()}%)...", color = Color(0xFFFF8A00), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        } else {
                            IconButton(
                                onClick = { downloadProgressId = rep.id },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Download Report Asset", tint = Color(0xFF10B981))
                            }
                        }
                    }

                    if (downloadProgressId == rep.id) {
                        LinearProgressIndicator(
                            progress = progressVal,
                            color = Color(0xFFFF8A00),
                            trackColor = Color(0xFF1E293B),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .height(4.dp)
                        )
                    }

                    Text(text = "Created by: " + rep.generatedBy + " • " + formatAttendanceTime(rep.timestamp), color = Color(0xFF64748B), fontSize = 8.sp, modifier = Modifier.padding(top = 4.dp))
                    Text(text = "🔗 Asset URL: " + rep.downloadUrl, color = Color(0xFF3B82F6), fontSize = 8.sp, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// CORE HELPER COMPOSABLES & FUNCTIONS
// -------------------------------------------------------------
@Composable
fun LabelTextPair(label: String, valStr: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$label: ", color = Color(0xFF94A3B8), fontSize = 10.sp)
        Text(text = valStr, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun VerificationIndicator(label: String, checked: Boolean) {
    Surface(
        color = if (checked) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFF1F2937),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.border(1.dp, if (checked) Color(0xFF10B981) else Color(0xFF374151), RoundedCornerShape(4.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(if (checked) Color(0xFF10B981) else Color(0xFFEF4444), RoundedCornerShape(100.dp))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, color = if (checked) Color(0xFF10B981) else Color(0xFF94A3B8), fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
    }
}

fun formatAttendanceTime(timestamp: Long): String {
    return SimpleDateFormat("dd MMM hh:mm a", Locale.getDefault()).format(Date(timestamp))
}
