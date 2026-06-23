package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VisitorSystemPane(
    viewModel: EnterpriseViewModel,
    currentUser: User?
) {
    val visitors by viewModel.visitors.collectAsState()
    val passes by viewModel.visitorPasses.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()
    val deliveries by viewModel.deliveries.collectAsState()
    val contractors by viewModel.contractors.collectAsState()
    val contractorWorkers by viewModel.contractorWorkers.collectAsState()
    val blacklist by viewModel.blacklist.collectAsState()
    val evacuationRegister by viewModel.evacuationRegister.collectAsState()
    val visitorReports by viewModel.visitorReports.collectAsState()
    val sites by viewModel.firestoreSites.collectAsState()

    var activeTab by remember { mutableStateOf("COMMAND_CENTER") } // COMMAND_CENTER, REGISTER_LOG, ENTITIES, REGISTRATIONS, BLACKLIST_EVAC

    // Styling constants
    val darkBg = Color(0xFF0F172A)
    val cardBg = Color(0xFF1E293B)
    val accentBlue = Color(0xFF3B82F6)
    val glowGreen = Color(0xFF10B981)
    val alertRed = Color(0xFFEF4444)
    val warningYellow = Color(0xFFF59E0B)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // --- Header Banner ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Color(0xFF1E1B4B), Color(0xFF311042))))
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Security Core Icon",
                        tint = glowGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "KUNDAL SECURITY OS • V3",
                        color = glowGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🎟️ Gate Command & Visitor Master",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Active Gate Guard: ${currentUser?.name ?: "Guard Singh"} | Role: ${currentUser?.role?.uppercase() ?: "GUARD_SIMULATION"}",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }
        }

        // --- Tab Navigation Row ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0B0F19))
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
            val tabs = listOf(
                "COMMAND_CENTER" to "📊 Control Center",
                "REGISTER_LOG" to "📋 Gate Register & PASS",
                "ENTITIES" to "🚚 Deliveries & Contracting",
                "REGISTRATIONS" to "✍️ Register Guest",
                "BLACKLIST_EVAC" to "🚨 Blacklist & Evac Control"
            )

            tabs.forEach { (tabId, tabName) ->
                val selected = activeTab == tabId
                Button(
                    onClick = { activeTab = tabId },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) accentBlue else Color(0xFF1E293B),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 4.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = tabName,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // --- Main Dashboard Frame ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
        ) {
            when (activeTab) {
                "COMMAND_CENTER" -> {
                    // STEP 9: VISITOR COMMAND CENTER
                    VisitorCommandCenterView(
                        visitors = visitors,
                        blacklist = blacklist,
                        contractorWorkers = contractorWorkers,
                        deliveries = deliveries,
                        visitorReports = visitorReports,
                        onTriggerEvac = { viewModel.triggerEmergencyEvacuationRegistry() },
                        onTriggerReport = { type -> viewModel.generateVisitorCustomReport(type) }
                    )
                }
                "REGISTER_LOG" -> {
                    // STEP 10: DIGITAL GATE REGISTER & STEP 3: QR PASS DISPLAY
                    GateRegisterAndPassView(
                        visitors = visitors,
                        passes = passes,
                        onCheckIn = { id, remarks -> viewModel.checkInVisitor(id, remarks) },
                        onCheckOut = { id -> viewModel.checkOutVisitor(id) },
                        onApprove = { id, mode -> viewModel.approveVisitor(id, mode) },
                        onReject = { id -> viewModel.rejectVisitor(id) }
                    )
                }
                "ENTITIES" -> {
                    // STEP 5: DELIVERY & STEP 6: CONTRACTOR MANAGEMENT
                    DeliveriesAndContractorsView(
                        deliveries = deliveries,
                        contractors = contractors,
                        contractorWorkers = contractorWorkers,
                        vehicles = vehicles,
                        onAddDelivery = { del -> viewModel.registerDelivery(del) },
                        onCheckoutDelivery = { id -> viewModel.checkOutDelivery(id) },
                        onAddContractor = { cont -> viewModel.registerContractor(cont) },
                        onAddWorker = { w -> viewModel.registerWorker(w) },
                        onCheckInWorker = { id -> viewModel.checkInContractorWorker(id) },
                        onCheckOutWorker = { id -> viewModel.checkOutContractorWorker(id) }
                    )
                }
                "REGISTRATIONS" -> {
                    // STEP 1: VISITOR REGISTRATION & STEP 4: VEHICLE & STEP 11: PRE-APPROVED REG
                    RegistrationsAndPreApprovedView(
                        sites = sites,
                        onAddVisitor = { vis -> viewModel.registerVisitor(vis) },
                        onAddVehicle = { veh -> viewModel.registerVehicle(veh) },
                        onAddPrePass = { name, mobile, siteId, host, type, days ->
                            viewModel.createPreApprovedPass(name, mobile, siteId, host, type, days)
                        }
                    )
                }
                "BLACKLIST_EVAC" -> {
                    // STEP 7: BLACKLIST CONTROL CENTER & STEP 8: EMERGENCY EVACUATION REGISTER
                    BlacklistAndEvacView(
                        blacklist = blacklist,
                        evacRegister = evacuationRegister,
                        onAddBlacklist = { entry -> viewModel.addBlacklistEntry(entry) },
                        onRemoveBlacklist = { id -> viewModel.removeBlacklistEntry(id) },
                        onTriggerEvac = { viewModel.triggerEmergencyEvacuationRegistry() }
                    )
                }
            }
        }
    }
}

// --------------------------------------------------------------------------------------
// SUB-VIEWS FOR VISITOR MANAGEMENT SYSTEM PORT
// --------------------------------------------------------------------------------------

@Composable
fun VisitorCommandCenterView(
    visitors: List<FirestoreVisitor>,
    blacklist: List<FirestoreBlacklistEntry>,
    contractorWorkers: List<FirestoreContractorWorker>,
    deliveries: List<FirestoreDelivery>,
    visitorReports: List<FirestoreVisitorReport>,
    onTriggerEvac: () -> Unit,
    onTriggerReport: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    
    // Compute KPIs (Step 9)
    val todayStart = remember {
        val c = java.util.Calendar.getInstance()
        c.set(java.util.Calendar.HOUR_OF_DAY, 0)
        c.set(java.util.Calendar.MINUTE, 0)
        c.set(java.util.Calendar.SECOND, 0)
        c.timeInMillis
    }
    val visitorsToday = visitors.count { it.createdAt >= todayStart }
    val insideCount = visitors.count { it.status == "INSIDE" }
    val pendingCount = visitors.count { it.status == "PENDING" }
    val rejectedCount = visitors.count { it.status == "REJECTED" }
    val activeContractors = contractorWorkers.count { it.status == "INSIDE" }
    val deliveriesToday = deliveries.count { it.checkInTime >= todayStart }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // --- STEP 9 Live KPIs Grid ---
        Text(
            text = "KPI METRICS (LIVE CAPTURES)",
            color = Color(0xFF64748B),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            KpiCard(title = "Visitors Today", count = "$visitorsToday", color = Color(0xFF3B82F6), icon = Icons.Default.Group, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            KpiCard(title = "Visitors Inside", count = "$insideCount", color = Color(0xFF10B981), icon = Icons.Default.MeetingRoom, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            KpiCard(title = "Pending Auth", count = "$pendingCount", color = Color(0xFFF59E0B), icon = Icons.Default.RingVolume, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            KpiCard(title = "Rejected Entries", count = "$rejectedCount", color = Color(0xFFEF4444), icon = Icons.Default.Block, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            KpiCard(title = "Contractors active", count = "$activeContractors", color = Color(0xFF8B5CF6), icon = Icons.Default.Engineering, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            KpiCard(title = "Deliveries Today", count = "$deliveriesToday", color = Color(0xFFEC4899), icon = Icons.Default.LocalShipping, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Live Feed Board & Actions ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡ GATE LIVE TELEMETRY STREAM",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF111827))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF10B981)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "LIVE SYNC", color = Color(0xFF10B981), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Live Feed entries
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                val feedItems = visitors.take(3)
                if (feedItems.isEmpty()) {
                    Text("No gate events recorded today.", color = Color(0xFF94A3B8), fontSize = 11.sp, fontStyle = FontStyle.Italic)
                } else {
                    feedItems.forEach { vis ->
                        val actionText = when (vis.status) {
                            "PENDING" -> "demands mobile pre-authorization"
                            "APPROVED" -> "was authorized. Awaiting checkin"
                            "REJECTED" -> "was rejected entry by tenant host"
                            "INSIDE" -> "punched in to ${vis.unitFlatOffice}"
                            "EXITED" -> "checked out. Clearance successful"
                            else -> "logged"
                        }
                        val statusColor = when (vis.status) {
                            "PENDING" -> Color(0xFFF59E0B)
                            "APPROVED" -> Color(0xFF3B82F6)
                            "REJECTED" -> Color(0xFFEF4444)
                            "INSIDE" -> Color(0xFF10B981)
                            else -> Color(0xFF94A3B8)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (vis.status == "INSIDE") Icons.Default.Login else Icons.Default.History,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row {
                                    Text(text = vis.fullName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(text = " (" + vis.visitorType + ") ", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                    Text(text = actionText, color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                }
                                Text(
                                    text = "Host: ${vis.hostName} • ${formatVisitorTime(vis.createdAt)}",
                                    color = Color(0xFF64748B),
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Divider(color = Color(0xFF334155), thickness = 0.5.dp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- STEP 12: VISITOR ANALYTICS & REPORTS GENERATION ---
        Text(
            text = "📊 VISITOR ANALYTICS & COMPLIANCE GENERATOR",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Generate and download custom PDF/Excel telemetry structures matching V3 regulations.",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { onTriggerReport("DAILY") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                modifier = Modifier.weight(1f).padding(end = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Daily Report", fontSize = 10.sp)
                }
            }

            Button(
                onClick = { onTriggerReport("MONTHLY") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Monthly Logs", fontSize = 10.sp)
                }
            }

            Button(
                onClick = { onTriggerReport("BLACKLIST") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B)),
                modifier = Modifier.weight(1f).padding(start = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Blacklist PDF", fontSize = 10.sp, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "COMPILED TELEMETRY ARTIFACTS",
            color = Color(0xFF64748B),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (visitorReports.isEmpty()) {
                    Text("No reports generated yet.", color = Color(0xFF94A3B8), fontSize = 11.sp, fontStyle = FontStyle.Italic)
                } else {
                    visitorReports.forEach { rep ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "📄 [${rep.type}] Visitor Pass Summary Report", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(text = rep.contentSummary, color = Color(0xFF94A3B8), fontSize = 10.sp)
                                Text(text = "Compiled by: ${rep.generatedBy} • ${formatVisitorTime(rep.timestamp)}", color = Color(0xFF64748B), fontSize = 9.sp)
                            }
                            Button(
                                onClick = { }, // Simulation download action
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("DOWNLOAD", fontSize = 9.sp, color = Color(0xFF3B82F6))
                            }
                        }
                        Divider(color = Color(0xFF334155))
                    }
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    count: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title.uppercase(), color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = count, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
    }
}

// --------------------------------------------------------------------------------------
// STEP 10: DIGITAL GATE REGISTER & VISITOR PASS CONTROL
// --------------------------------------------------------------------------------------
@Composable
fun GateRegisterAndPassView(
    visitors: List<FirestoreVisitor>,
    passes: List<FirestoreVisitorPass>,
    onCheckIn: (String, String) -> Unit,
    onCheckOut: (String) -> Unit,
    onApprove: (String, String) -> Unit,
    onReject: (String) -> Unit
) {
    var filterType by remember { mutableStateOf("ALL") } // ALL, PENDING, INSIDE, APPROVED, EXITED
    var selectedVisitorWithPass by remember { mutableStateOf<FirestoreVisitor?>(null) }
    var guardRemarksInput by remember { mutableStateOf("") }

    Row(modifier = Modifier.fillMaxSize()) {
        // --- Left part: Master Visitor List (Digital Register) ---
        Column(modifier = Modifier.weight(1.3f).fillMaxHeight()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📄 REGISTER OF OCCURRENCE",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Dropdown or Row of filter pills
                Row {
                    val filters = listOf("ALL" to "All", "PENDING" to "Pending", "INSIDE" to "Inside", "APPROVED" to "Approved")
                    filters.forEach { (fid, label) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (filterType == fid) Color(0xFF3B82F6) else Color(0xFF1E293B))
                                .clickable { filterType = fid }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .padding(horizontal = 2.dp)
                        ) {
                            Text(text = label, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val filteredList = remember(visitors, filterType) {
                if (filterType == "ALL") visitors 
                else visitors.filter { it.status == filterType }
            }

            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(filteredList) { vis ->
                    val isSelected = selectedVisitorWithPass?.id == vis.id
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF1E3A8A) else Color(0xFF1E293B)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { 
                                selectedVisitorWithPass = vis 
                                guardRemarksInput = vis.remarks
                            }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val initials = if (vis.fullName.isNotEmpty()) {
                                        vis.fullName.split(" ").map { it.firstOrNull()?.uppercaseChar() ?: "" }.joinToString("").take(2)
                                    } else "V"
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))))
                                            .border(1.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = initials,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = vis.fullName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "${vis.visitorType} • ${vis.mobile}", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                    }
                                }

                                Badge(
                                    containerColor = when (vis.status) {
                                        "PENDING" -> Color(0xFFF59E0B)
                                        "APPROVED" -> Color(0xFF3B82F6)
                                        "INSIDE" -> Color(0xFF10B981)
                                        "EXITED" -> Color(0xFF64748B)
                                        else -> Color.DarkGray
                                    }
                                ) {
                                    Text(text = vis.status, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Host Details", color = Color(0xFF64748B), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "${vis.hostName} (${vis.unitFlatOffice})", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "Purpose / Proof", color = Color(0xFF64748B), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "${vis.purposeOfVisit} | ${vis.idProofType ?: "None Specified"}", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                }
                            }

                            if (vis.remarks.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "✍️ Guard Remarks: ${vis.remarks}",
                                    color = Color(0xFFF59E0B),
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // --- Right part: Active Action Hub & QR PASS Render (Step 3 & Step 2) ---
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Text(
                text = "⚡ CONTROL CONSOLE",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Approve requests or clear checks in/out",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val currentVis = selectedVisitorWithPass
            if (currentVis == null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Tap on any visitor profile on the left to show live action console and generate high-contrast digital QR pass.",
                            color = Color(0xFF475569),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "ACTIVE OCCURRENCE DETAIL", color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(text = currentVis.fullName, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                        Text(text = "Identity Verification Type: ${currentVis.idProofType ?: "None"} (${currentVis.idProofNumber ?: "Not verified"})", color = Color(0xFF94A3B8), fontSize = 10.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Render Step 3: QR Visitor Pass Visual
                        val linkedPass = passes.firstOrNull { it.id == currentVis.qrPassId || it.visitorId == currentVis.id }
                        if (linkedPass != null) {
                            Text(text = "🎟️ ACTIVE QR PASS: ${linkedPass.passType}", color = Color(0xFF3B82F6), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            // Beautiful drawn custom QR graphic (Canvas based)
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(90.dp)) {
                                    val size = this.size.width
                                    // Make simulated QR dots
                                    drawRect(color = Color.Black, size = androidx.compose.ui.geometry.Size(30f, 30f))
                                    drawRect(color = Color.Black, topLeft = androidx.compose.ui.geometry.Offset(size - 30f, 0f), size = androidx.compose.ui.geometry.Size(30f, 30f))
                                    drawRect(color = Color.Black, topLeft = androidx.compose.ui.geometry.Offset(0f, size - 30f), size = androidx.compose.ui.geometry.Size(30f, 30f))
                                    
                                    // Inner squares
                                    drawRect(color = Color.Black, topLeft = androidx.compose.ui.geometry.Offset(45f, 40f), size = androidx.compose.ui.geometry.Size(20f, 25f))
                                    drawRect(color = Color.Black, topLeft = androidx.compose.ui.geometry.Offset(10f, 45f), size = androidx.compose.ui.geometry.Size(20f, 10f))
                                    drawRect(color = Color.Black, topLeft = androidx.compose.ui.geometry.Offset(50f, 15f), size = androidx.compose.ui.geometry.Size(15f, 15f))
                                    
                                    // Outline Stroke for alignment scanning
                                    drawRect(color = Color(0xFF3B82F6), style = Stroke(width = 2f))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Code: ${linkedPass.qrContent}",
                                color = Color.Gray,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Pass Validity Status: " + linkedPass.status,
                                color = if (linkedPass.status == "ACTIVE") Color(0xFF10B981) else Color(0xFFEF4444),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "No visual QR Pass issued. Pre-verification clearance required.",
                                color = Color(0xFFEF4444),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Guard remarks input box
                        OutlinedTextField(
                            value = guardRemarksInput,
                            onValueChange = { guardRemarksInput = it },
                            label = { Text("Guard Entry Remarks", fontSize = 11.sp) },
                            textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color(0xFF475569)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // --- Step 2 Host Approval flow, and Check In / Out ---
                        if (currentVis.status == "PENDING") {
                            Text(
                                text = "STEP 2: HOST PRE-APPROVAL INTRUSION ACTION",
                                color = Color(0xFFF59E0B),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { onApprove(currentVis.id, "Mobile App Authorization") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    modifier = Modifier.weight(1f).padding(end = 2.dp)
                                ) {
                                    Text("App Override", fontSize = 9.sp)
                                }
                                Button(
                                    onClick = { onApprove(currentVis.id, "Security Desk Voice Call Verification") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B21B6)),
                                    modifier = Modifier.weight(1f).padding(start = 2.dp)
                                ) {
                                    Text("Call Host", fontSize = 9.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = { onReject(currentVis.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Reject Pass / Expire Request", fontSize = 10.sp)
                            }
                        } else if (currentVis.status == "APPROVED") {
                            // Check in action
                            Button(
                                onClick = { 
                                    onCheckIn(currentVis.id, guardRemarksInput)
                                    selectedVisitorWithPass = visitors.firstOrNull { it.id == currentVis.id }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Check-In / Open Barrier Gate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else if (currentVis.status == "INSIDE") {
                            // Check out action
                            Button(
                                onClick = { 
                                    onCheckOut(currentVis.id)
                                    selectedVisitorWithPass = visitors.firstOrNull { it.id == currentVis.id }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Check-Out / Exited Premises", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else if (currentVis.status == "EXITED" || currentVis.status == "REJECTED") {
                            Text(
                                text = "Occurence logged. History entries lock verified.",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------------------
// STEP 5: DELIVERY & STEP 6: CONTRACTOR MANAGEMENT MODULES
// --------------------------------------------------------------------------------------
@Composable
fun DeliveriesAndContractorsView(
    deliveries: List<FirestoreDelivery>,
    contractors: List<FirestoreContractor>,
    contractorWorkers: List<FirestoreContractorWorker>,
    vehicles: List<FirestoreVehicle>,
    onAddDelivery: (FirestoreDelivery) -> String?,
    onCheckoutDelivery: (String) -> Unit,
    onAddContractor: (FirestoreContractor) -> Unit,
    onAddWorker: (FirestoreContractorWorker) -> Unit,
    onCheckInWorker: (String) -> Unit,
    onCheckOutWorker: (String) -> Unit
) {
    var deliveryCompany by remember { mutableStateOf("Amazon") }
    var orderNumber by remember { mutableStateOf("") }
    var recipientName by remember { mutableStateOf("") }
    var recipientUnit by remember { mutableStateOf("") }

    var selectedContractorId by remember { mutableStateOf("") }
    var contractorCompanyInput by remember { mutableStateOf("") }
    var supervisorInput by remember { mutableStateOf("") }
    var contactInput by remember { mutableStateOf("") }
    var permitTypeInput by remember { mutableStateOf("Civil") }

    var workerNameInput by remember { mutableStateOf("") }
    var workerMobileInput by remember { mutableStateOf("") }
    var workerIdProofType by remember { mutableStateOf("Work Permit") }
    var workerIdProofNo by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Column: Delivery Management Center
        Column(modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(scrollState)) {
            Text(
                text = "🚚 STEP 5: DELIVERY CARGO HUBS",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Registrations Box
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Register Parcel Delivery Entry", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    val options = listOf("Amazon", "Flipkart", "Swiggy", "Zomato", "Blinkit", "Zepto", "BigBasket", "Courier", "Custom")
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                        options.forEach { opt ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (deliveryCompany == opt) Color(0xFF3B82F6) else Color(0xFF0F172A))
                                    .clickable { deliveryCompany = opt }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .padding(end = 4.dp)
                            ) {
                                Text(text = opt, color = Color.White, fontSize = 9.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = orderNumber,
                        onValueChange = { orderNumber = it },
                        label = { Text("Order/Receipt ID", fontSize = 10.sp) },
                        textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF3B82F6))
                    )

                    OutlinedTextField(
                        value = recipientName,
                        onValueChange = { recipientName = it },
                        label = { Text("Recipient Native Name", fontSize = 10.sp) },
                        textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF3B82F6))
                    )

                    OutlinedTextField(
                        value = recipientUnit,
                        onValueChange = { recipientUnit = it },
                        label = { Text("Recipient Unit / Apartment Flat", fontSize = 10.sp) },
                        textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF3B82F6))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (orderNumber.isNotEmpty() && recipientUnit.isNotEmpty()) {
                                onAddDelivery(
                                    FirestoreDelivery(
                                        orderNumber = orderNumber,
                                        deliveryCompany = deliveryCompany,
                                        recipientName = recipientName,
                                        recipientUnit = recipientUnit
                                    )
                                )
                                orderNumber = ""
                                recipientName = ""
                                recipientUnit = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Check-In Delivery Inbound", fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Deliveries List
            Text("Active Inside Deliveries", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            deliveries.filter { it.status == "INSIDE" }.forEach { del ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "📦 [${del.deliveryCompany}] Order: ${del.orderNumber}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Dest: ${del.recipientUnit} (${del.recipientName})", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                            Text(text = "Checked In: ${formatVisitorTime(del.checkInTime)}", color = Color.Gray, fontSize = 8.sp)
                        }
                        Button(
                            onClick = { onCheckoutDelivery(del.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text("Clear Out", fontSize = 9.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Right Column: Contractors & Working Permits
        Column(modifier = Modifier.weight(1.3f).fillMaxHeight().verticalScroll(rememberScrollState())) {
            Text(
                text = "👷 STEP 6: CONTRACTORS & PERMITS CONTROL",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Register Maintenance Contractor Block", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = contractorCompanyInput,
                        onValueChange = { contractorCompanyInput = it },
                        label = { Text("Company Name", fontSize = 10.sp) },
                        textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF10B981))
                    )

                    Row {
                        OutlinedTextField(
                            value = supervisorInput,
                            onValueChange = { supervisorInput = it },
                            label = { Text("Supervisor Name", fontSize = 10.sp) },
                            textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                            modifier = Modifier.weight(1f).padding(end = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF10B981))
                        )
                        OutlinedTextField(
                            value = contactInput,
                            onValueChange = { contactInput = it },
                            label = { Text("Supervisor Contact", fontSize = 10.sp) },
                            textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                            modifier = Modifier.weight(1f).padding(start = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF10B981))
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Work Permit Category (Step 6)", color = Color.Gray, fontSize = 9.sp)
                    val permits = listOf("Electrical", "Civil", "Painting", "Plumbing", "Housekeeping", "Maintenance")
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                        permits.forEach { p ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (permitTypeInput == p) Color(0xFF10B981) else Color(0xFF0F172A))
                                    .clickable { permitTypeInput = p }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .padding(end = 4.dp)
                            ) {
                                Text(text = p, color = Color.White, fontSize = 8.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (contractorCompanyInput.isNotEmpty() && supervisorInput.isNotEmpty()) {
                                onAddContractor(
                                    FirestoreContractor(
                                        companyName = contractorCompanyInput,
                                        supervisorName = supervisorInput,
                                        contactNumber = contactInput,
                                        workPermitType = permitTypeInput
                                    )
                                )
                                contractorCompanyInput = ""
                                supervisorInput = ""
                                contactInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Issue Permits & Register Contractor", fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Workers Hub
            Text(text = "👷 CONTRACTOR WORKER ROSTER & GATES", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Verify & Check-In Contractor Workers", color = Color.Gray, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(4.dp))

                    val selectedCont = contractors.firstOrNull()
                    if (selectedCont == null) {
                        Text("Register contractor first before managing worker identities.", color = Color.DarkGray, fontSize = 10.sp, fontStyle = FontStyle.Italic)
                    } else {
                        Text("Affiliation: ${selectedCont.companyName} [Permit: ${selectedCont.workPermitType}]", color = Color(0xFFCBD5E1), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        
                        OutlinedTextField(
                            value = workerNameInput,
                            onValueChange = { workerNameInput = it },
                            label = { Text("Worker Full Name", fontSize = 9.sp) },
                            textStyle = TextStyle(color = Color.White, fontSize = 10.sp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF8B5CF6))
                        )

                        Row {
                            OutlinedTextField(
                                value = workerMobileInput,
                                onValueChange = { workerMobileInput = it },
                                label = { Text("Worker Contact Phone", fontSize = 9.sp) },
                                textStyle = TextStyle(color = Color.White, fontSize = 10.sp),
                                modifier = Modifier.weight(1f).padding(end = 2.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF8B5CF6))
                            )
                            OutlinedTextField(
                                value = workerIdProofNo,
                                onValueChange = { workerIdProofNo = it },
                                label = { Text("ID doc / Work Permit #", fontSize = 9.sp) },
                                textStyle = TextStyle(color = Color.White, fontSize = 10.sp),
                                modifier = Modifier.weight(1f).padding(start = 2.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF8B5CF6))
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                if (workerNameInput.isNotEmpty()) {
                                    onAddWorker(
                                        FirestoreContractorWorker(
                                            contractorId = selectedCont.id,
                                            companyName = selectedCont.companyName,
                                            workerName = workerNameInput,
                                            mobile = workerMobileInput,
                                            idProofType = workerIdProofType,
                                            idProofNumber = workerIdProofNo
                                        )
                                    )
                                    workerNameInput = ""
                                    workerMobileInput = ""
                                    workerIdProofNo = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Provision Worker Identity Card", fontSize = 9.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Workers List
            Text("Provisioned Workers Gate Registry", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            contractorWorkers.forEach { w ->
                val inside = w.status == "INSIDE"
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (inside) Color(0xFF1E293B) else Color(0xFF0F172A)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "👷 " + w.workerName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${w.companyName} • ID: ${w.idProofNumber}", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                            if (w.checkInTime != null) {
                                Text(text = "In: " + formatVisitorTime(w.checkInTime), color = Color.Gray, fontSize = 8.sp)
                            }
                        }
                        Button(
                            onClick = { if (inside) onCheckOutWorker(w.id) else onCheckInWorker(w.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (inside) Color(0xFFEF4444) else Color(0xFF10B981)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(text = if (inside) "Gate Clear Out" else "Clear Check-In", fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------------------
// STEP 1: VISITOR REGISTRATION & STEP 4: VEHICLE & STEP 11: PRE-APPROVED REG
// --------------------------------------------------------------------------------------
@Composable
fun RegistrationsAndPreApprovedView(
    sites: List<FirestoreSite>,
    onAddVisitor: (FirestoreVisitor) -> String?,
    onAddVehicle: (FirestoreVehicle) -> String?,
    onAddPrePass: (String, String, String, String, String, Int) -> Unit
) {
    val scrollState = rememberScrollState()

    // Visitor States (Step 1)
    var fullName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var visitorType by remember { mutableStateOf("Guest") }
    var idProofType by remember { mutableStateOf("Aadhaar") }
    var idProofNumber by remember { mutableStateOf("") }
    var unitFlatOffice by remember { mutableStateOf("") }
    var hostName by remember { mutableStateOf("") }
    var hostMobile by remember { mutableStateOf("") }
    var purposeOfVisit by remember { mutableStateOf("") }
    var expectedDuration by remember { mutableStateOf("1 hour") }
    var selectedSiteId by remember { mutableStateOf("ST-MOP01") }

    // Vehicle States (Step 4)
    var vehicleNumber by remember { mutableStateOf("") }
    var vehicleType by remember { mutableStateOf("Car") }
    var color by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var driverName by remember { mutableStateOf("") }
    var driverMobile by remember { mutableStateOf("") }

    // Pre Approved States (Step 11)
    var preGuestName by remember { mutableStateOf("") }
    var preGuestPhone by remember { mutableStateOf("") }
    var preGuestHost by remember { mutableStateOf("") }
    var prePassType by remember { mutableStateOf("Guest Pass") }
    var preDurationDays by remember { mutableStateOf(1) }

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Column: Visitor & Vehicle Registration
        Column(modifier = Modifier.weight(1.2f).fillMaxHeight().verticalScroll(scrollState)) {
            Text(
                text = "📝 STEP 1 & 4: COMPLIANT GATE REGISTRATION",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "STEP 1: Basic Visitor Profile", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name (Official)", fontSize = 10.sp) },
                        textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF3B82F6))
                    )

                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        label = { Text("Mobile Number", fontSize = 10.sp) },
                        textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF3B82F6))
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Visitor Category", color = Color.Gray, fontSize = 9.sp)
                    
                    val categories = listOf("Guest", "Family", "Delivery Executive", "Contractor", "Vendor", "Candidate", "Emergency")
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                        categories.forEach { cat ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (visitorType == cat) Color(0xFF3B82F6) else Color(0xFF0F172A))
                                    .clickable { visitorType = cat }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .padding(end = 4.dp)
                            ) {
                                Text(text = cat, color = Color.White, fontSize = 9.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row {
                        OutlinedTextField(
                            value = idProofType,
                            onValueChange = { idProofType = it },
                            label = { Text("ID Proof: Aadhaar, PAN...", fontSize = 9.sp) },
                            textStyle = TextStyle(color = Color.White, fontSize = 10.sp),
                            modifier = Modifier.weight(1f).padding(end = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF3B82F6))
                        )
                        OutlinedTextField(
                            value = idProofNumber,
                            onValueChange = { idProofNumber = it },
                            label = { Text("ID Doc Serial Code", fontSize = 9.sp) },
                            textStyle = TextStyle(color = Color.White, fontSize = 10.sp),
                            modifier = Modifier.weight(1.2f).padding(start = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF3B82F6))
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = "Visit Information & Dest Host", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row {
                        OutlinedTextField(
                            value = unitFlatOffice,
                            onValueChange = { unitFlatOffice = it },
                            label = { Text("Flat / Unit / Office #", fontSize = 9.sp) },
                            textStyle = TextStyle(color = Color.White, fontSize = 10.sp),
                            modifier = Modifier.weight(1f).padding(end = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF3B82F6))
                        )
                        OutlinedTextField(
                            value = hostName,
                            onValueChange = { hostName = it },
                            label = { Text("Host Full Name", fontSize = 9.sp) },
                            textStyle = TextStyle(color = Color.White, fontSize = 10.sp),
                            modifier = Modifier.weight(1f).padding(start = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF3B82F6))
                        )
                    }

                    OutlinedTextField(
                        value = purposeOfVisit,
                        onValueChange = { purposeOfVisit = it },
                        label = { Text("Purpose of Entry", fontSize = 10.sp) },
                        textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF3B82F6))
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    // Step 4 Vehicle capture within the entry registration
                    Text(text = "STEP 4: Vehicle Details (Optional)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = vehicleNumber,
                        onValueChange = { vehicleNumber = it },
                        label = { Text("Vehicle number plate ie: SGA1234D", fontSize = 9.sp) },
                        textStyle = TextStyle(color = Color.White, fontSize = 10.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF3B82F6))
                    )

                    val vTypes = listOf("Bike", "Car", "Truck", "Tempo", "Van", "Taxi", "Auto")
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                        vTypes.forEach { vt ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (vehicleType == vt) Color(0xFF10B981) else Color(0xFF0F172A))
                                    .clickable { vehicleType = vt }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .padding(end = 4.dp)
                            ) {
                                Text(text = vt, color = Color.White, fontSize = 8.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (fullName.isNotEmpty() && mobile.isNotEmpty() && hostName.isNotEmpty()) {
                                // If vehicle specified, register the vehicle bounds first!
                                if (vehicleNumber.isNotEmpty()) {
                                    onAddVehicle(
                                        FirestoreVehicle(
                                            vehicleNumber = vehicleNumber,
                                            vehicleType = vehicleType,
                                            driverName = fullName,
                                            driverMobile = mobile
                                        )
                                    )
                                }

                                val statusToPut = if (visitorType == "Emergency") "INSIDE" else "PENDING"
                                val outcome = onAddVisitor(
                                    FirestoreVisitor(
                                        fullName = fullName,
                                        mobile = mobile,
                                        visitorType = visitorType,
                                        gender = gender,
                                        idProofType = idProofType,
                                        idProofNumber = idProofNumber,
                                        unitFlatOffice = unitFlatOffice,
                                        hostName = hostName,
                                        purposeOfVisit = purposeOfVisit,
                                        status = statusToPut,
                                        expectedDuration = expectedDuration,
                                        siteId = selectedSiteId
                                    )
                                )

                                if (outcome == null) {
                                    fullName = ""
                                    mobile = ""
                                    idProofNumber = ""
                                    unitFlatOffice = ""
                                    hostName = ""
                                    purposeOfVisit = ""
                                    vehicleNumber = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Register Entry & Sync To Host Mobile", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Right Column: Resident Pre-Approval Pass Creator (Step 11)
        Column(modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())) {
            Text(
                text = "⚡ STEP 11: RESIDENT PRE-AUTH SUITE",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Simulates residents generating digital passes for dynamic events, family or recurring entries.",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Generate Guest / Event Pass", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = preGuestName,
                        onValueChange = { preGuestName = it },
                        label = { Text("Pre-Approved Guest Name", fontSize = 10.sp) },
                        textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF59E0B))
                    )

                    OutlinedTextField(
                        value = preGuestPhone,
                        onValueChange = { preGuestPhone = it },
                        label = { Text("Guest Phone Number", fontSize = 10.sp) },
                        textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF59E0B))
                    )

                    OutlinedTextField(
                        value = preGuestHost,
                        onValueChange = { preGuestHost = it },
                        label = { Text("Resident Host Name (Your Identity)", fontSize = 10.sp) },
                        textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF59E0B))
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Authorized Pass Type (Step 11)", color = Color.Gray, fontSize = 9.sp)
                    
                    val passTypes = listOf("Guest Pass", "Event Pass", "Temporary Pass", "Recurring Pass")
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                        passTypes.forEach { pt ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (prePassType == pt) Color(0xFFF59E0B) else Color(0xFF0F172A))
                                    .clickable { prePassType = pt }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .padding(end = 4.dp)
                            ) {
                                Text(text = pt, color = Color.White, fontSize = 8.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (preGuestName.isNotEmpty() && preGuestPhone.isNotEmpty()) {
                                onAddPrePass(
                                    preGuestName,
                                    preGuestPhone,
                                    "ST-MOP01",
                                    preGuestHost,
                                    prePassType,
                                    preDurationDays
                                )
                                preGuestName = ""
                                preGuestPhone = ""
                                preGuestHost = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Publish Auth Token Code & QR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------------------
// STEP 7: BLACKLIST CONTROL CENTER & STEP 8: EMERGENCY EVACUATION REGISTER
// --------------------------------------------------------------------------------------
@Composable
fun BlacklistAndEvacView(
    blacklist: List<FirestoreBlacklistEntry>,
    evacRegister: List<FirestoreEvacuationEntry>,
    onAddBlacklist: (FirestoreBlacklistEntry) -> Unit,
    onRemoveBlacklist: (String) -> Unit,
    onTriggerEvac: () -> Unit
) {
    var blacklistType by remember { mutableStateOf("VISITOR") } // VISITOR, CONTRACTOR, VEHICLE
    var blacklistValue by remember { mutableStateOf("") }
    var blacklistName by remember { mutableStateOf("") }
    var blacklistReason by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Column: Blacklist Control Center (Step 7 - Hard Blocks)
        Column(modifier = Modifier.weight(1.1f).fillMaxHeight().verticalScroll(scrollState)) {
            Text(
                text = "🛡️ STEP 7: BLACKLIST CONTROL CENTER",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Add Entity to Sentinel Blacklist", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Blacklisted targets will be HARD BLOCKED from any check-in.", color = Color.Gray, fontSize = 9.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    val types = listOf("VISITOR" to "Visitor Mobile", "CONTRACTOR" to "Contractor Name", "VEHICLE" to "Vehicle Plate")
                    Row(modifier = Modifier.fillMaxWidth()) {
                        types.forEach { (tid, label) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (blacklistType == tid) Color(0xFFEF4444) else Color(0xFF0F172A))
                                    .clickable { blacklistType = tid }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                    .padding(end = 4.dp)
                            ) {
                                Text(text = label, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = blacklistName,
                        onValueChange = { blacklistName = it },
                        label = { Text("Display / Entity Name", fontSize = 10.sp) },
                        textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFEF4444))
                    )

                    OutlinedTextField(
                        value = blacklistValue,
                        onValueChange = { blacklistValue = it },
                        label = { Text("Identifier Code (Phone, Plate Number)", fontSize = 10.sp) },
                        textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFEF4444))
                    )

                    OutlinedTextField(
                        value = blacklistReason,
                        onValueChange = { blacklistReason = it },
                        label = { Text("Reason for hard block", fontSize = 10.sp) },
                        textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFEF4444))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (blacklistValue.isNotEmpty() && blacklistReason.isNotEmpty()) {
                                onAddBlacklist(
                                    FirestoreBlacklistEntry(
                                        type = blacklistType,
                                        targetValue = blacklistValue.trim(),
                                        targetName = blacklistName,
                                        reason = blacklistReason
                                    )
                                )
                                blacklistValue = ""
                                blacklistName = ""
                                blacklistReason = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Add to Blacklist & Prohibit Entry", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Blacklist occurrences
            Text("Active Prohibited list", color = Color(0xFFEF4444), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            blacklist.forEach { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).border(0.5.dp, Color(0xFF991B1B), RoundedCornerShape(10.dp))
                ) {
                    val labelType = when (item.type) {
                        "VISITOR" -> "🚷 Phone Block"
                        "VEHICLE" -> "🚗 Plate Block"
                        "CONTRACTOR" -> "🏢 Contractor Block"
                        else -> "Blocked"
                    }
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "$labelType: ${item.targetName}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Target ID: ${item.targetValue}", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                            Text(text = "Reason: ${item.reason}", color = Color(0xFFEF4444), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { onRemoveBlacklist(item.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Pardon", fontSize = 9.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Right Column: Assembly Muster / Emergency Evacuation Register (Step 8)
        Column(modifier = Modifier.weight(1.1f).fillMaxHeight().verticalScroll(rememberScrollState())) {
            Text(
                text = "🚨 STEP 8: EMERGENCY PERIMETER MUSTER",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onTriggerEvac() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Podcasts, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("TRIGGER EVACUATION REPORT snapshot", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "WHO IS INSIDE SITE PREMISES RIGHT NOW:",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Real-time assembly lists for guard roll-calls at muster points (Replace physical sheet lists).",
                color = Color.Gray,
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    if (evacRegister.isEmpty()) {
                        Text("No active entities declared inside secure blocks.", color = Color.DarkGray, fontSize = 11.sp, fontStyle = FontStyle.Italic)
                    } else {
                        evacRegister.forEach { item ->
                            val iconType = when (item.type) {
                                "VISITOR" -> "🎟️"
                                "CONTRACTOR_WORKER" -> "👷"
                                "DELIVERY" -> "📦"
                                else -> "👤"
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "$iconType ${item.name}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(text = item.detail, color = Color(0xFF94A3B8), fontSize = 10.sp)
                                    Text(text = "Punched: " + formatVisitorTime(item.checkInTime), color = Color.Gray, fontSize = 8.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF047857))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "INSIDE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Divider(color = Color(0xFF334155))
                        }
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------------------
// HELPER METHODS
// --------------------------------------------------------------------------------------
fun formatVisitorTime(time: Long): String {
    val df = SimpleDateFormat("HH:mm", Locale.getDefault())
    return df.format(Date(time))
}
