package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import java.util.UUID

// Predefined Options
val EMPLOYEE_TYPES = listOf("Guard", "Supervisor", "Lady Guard", "Bouncer", "Operator", "Site Admin", "Staff", "Area Manager")
val BLOOD_GROUPS = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
val MARITAL_STATUSES = listOf("Single", "Married", "Divorced", "Widowed")
val GENDERS = listOf("Male", "Female", "Other")
val VERIFICATION_STATUSES = listOf("Pending", "Verified", "Rejected")
val TRAINING_STATUSES = listOf("Not Started", "In Progress", "Completed", "Expired")
val ATTENDANCE_PREFERENCES = listOf("QR", "GPS", "Selfie")

// Simplified Onboarding State Holder
data class EmployeeOnboardingDraft(
    // Step 1: Basic
    val empId: String = "EMP-" + (100 + (1..99).random()),
    val fullName: String = "",
    val fatherName: String = "",
    val dob: String = "1995-10-10",
    val gender: String = "Male",
    val mobile: String = "",
    val altMobile: String = "",
    val email: String = "",
    val bloodGroup: String = "O+",
    val maritalStatus: String = "Single",

    // Step 2: Address
    val permAddress: String = "",
    val permCity: String = "Singapore",
    val permState: String = "Singapore",
    val permPincode: String = "",
    val currAddress: String = "",
    val currCity: String = "Singapore",
    val currState: String = "Singapore",
    val currPincode: String = "",

    // Step 3: Employment
    val employeeType: String = "Guard",
    val joiningDate: String = "2026-06-22",
    val reportingManager: String = "Lim Ah San (Supervisor)",
    val company: String = "Centurion Security Services Private Ltd",
    val department: String = "Operations",

    // Step 4: KYC
    val aadhaar: String = "",
    val pan: String = "",
    val voterId: String = "",
    val drivingLicense: String = "",
    val passport: String = "",
    val policeVerification: String = "Pending",
    val backgroundVerification: String = "Verified",

    // Step 5: Bank
    val accountHolder: String = "",
    val bankName: String = "",
    val accountNumber: String = "",
    val ifscCode: String = "",
    val upiId: String = "",

    // Step 6: Training
    val psaraTraining: String = "Not Started",
    val fireSafety: String = "Not Started",
    val firstAid: String = "Not Started",
    val industrialSafety: String = "Not Started",
    val siteSopTraining: String = "Not Started",

    // Step 7: Uniform
    val isUniformIssued: Boolean = false,
    val isIdCardIssued: Boolean = false,
    val isShoesIssued: Boolean = false,
    val isBeltIssued: Boolean = false,
    val isCapIssued: Boolean = false,
    val isWalkieIssued: Boolean = false,
    val isMobileDeviceIssued: Boolean = false,

    // Step 8: Readiness scores
    val documentsVerified: Boolean = false,
    val medicalFitnessVerified: Boolean = false,

    // Step 9: Deployment
    val selectedSiteId: String = "",
    val selectedSiteName: String = "",
    val shiftName: String = "Morning Shift (08:00 - 20:00)",
    val position: String = "Static Post Sentry",

    // Step 10: Leaves & Preferences
    val attendancePreference: String = "QR",
    val casualLeaveLimit: Int = 12,
    val sickLeaveLimit: Int = 10,
    val earnedLeaveLimit: Int = 15,

    // Step 11: Emergency Contact
    val emergencyContactName: String = "",
    val emergencyContactRelation: String = "",
    val emergencyContactPhone: String = "",

    // Step 12: Docs url
    val aadhaarDocUrl: String = "",
    val panDocUrl: String = "",
    val photoDocUrl: String = "",
    val passbookDocUrl: String = "",
    val policeDocUrl: String = "",
    val trainingDocUrl: String = "",
    val medicalDocUrl: String = ""
)

@Composable
fun WorkforcePane(
    viewModel: EnterpriseViewModel,
    currentUser: User?,
    modifier: Modifier = Modifier
) {
    val employees by viewModel.firestoreEmployees.collectAsStateWithLifecycle()
    val deployments by viewModel.firestoreEmployeeDeployments.collectAsStateWithLifecycle()
    val trainingRecords by viewModel.firestoreEmployeeTraining.collectAsStateWithLifecycle()
    val equipmentRecords by viewModel.firestoreEmployeeEquipment.collectAsStateWithLifecycle()
    val leaveProfiles by viewModel.firestoreEmployeeLeaveProfiles.collectAsStateWithLifecycle()
    val payrollProfiles by viewModel.firestoreEmployeePayrollProfiles.collectAsStateWithLifecycle()
    val transfers by viewModel.firestoreEmployeeTransfers.collectAsStateWithLifecycle()
    val sites by viewModel.sites.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf("Employees") }
    var showOnboardWizard by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Details overlay state
    var selectedEmployeeDetails by remember { mutableStateOf<FirestoreEmployee?>(null) }
    
    // Transfer Dialog state
    var employeeToTransfer by remember { mutableStateOf<FirestoreEmployee?>(null) }

    val tabs = listOf(
        "Employees", "Guards", "Supervisors", "Staff", 
        "Relief Pool", "Training", "Documents", 
        "Deployments", "Transfers", "Exits"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .padding(16.dp)
    ) {
        // High fidelity header matching CRM theme
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "💼 KUNDAL SECURITY OS WORKFORCE PLATFORM",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Audit-ready Employee Master Registry & Unified Operational Deployment Controls",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = { showOnboardWizard = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006B54)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("onboard_employee_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Onboard", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("ONBOARD EMP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            prefix = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray, modifier = Modifier.size(16.dp)) },
            placeholder = { Text("Filter database records by name or Employee ID...", color = Color.Gray, fontSize = 13.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF006B54), unfocusedBorderColor = Color(0xFF1E293B),
                focusedContainerColor = Color(0xFF0F172A), unfocusedContainerColor = Color(0xFF090D16)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tab selection lazy row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(tabs) { tab ->
                val isSel = activeSubTab == tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSel) Color(0xFF006B54) else Color(0xFF151D30))
                        .clickable { activeSubTab = tab }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = tab.uppercase(),
                        color = if (isSel) Color.White else Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main List Content
        val filteredList = remember(employees, activeSubTab, searchQuery, deployments) {
            employees.filter { emp ->
                val matchSearch = emp.fullName.contains(searchQuery, ignoreCase = true) || emp.id.contains(searchQuery, ignoreCase = true)
                
                val matchTab = when (activeSubTab) {
                    "Employees" -> emp.status != "Exited"
                    "Guards" -> (emp.employeeType.lowercase().contains("guard") || emp.employeeType.lowercase() == "bouncer") && emp.status != "Exited"
                    "Supervisors" -> emp.employeeType.lowercase().contains("supervisor") && emp.status != "Exited"
                    "Staff" -> (emp.employeeType.lowercase().contains("admin") || emp.employeeType.lowercase() == "staff" || emp.employeeType.lowercase().contains("manager")) && emp.status != "Exited"
                    "Relief Pool" -> {
                        val isAssigned = deployments.any { it.employeeId == emp.id }
                        !isAssigned && emp.readinessScore >= 80.0 && emp.status != "Exited" && emp.status != "Suspended"
                    }
                    "Exits" -> emp.status == "Exited"
                    else -> true // documents, deployments, training, transfers show specialized list views below
                }
                matchSearch && matchTab
            }
        }

        if (activeSubTab == "Transfers") {
            // Render historical transfers ledger
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (transfers.isEmpty()) {
                    item { EmptyStateMessage("No historic transfers logged in ledger.") }
                }
                items(transfers) { trf ->
                    val empName = employees.find { it.id == trf.employeeId }?.fullName ?: "Unknown Personnel"
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                        border = borderLine(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.SwapHoriz, contentDescription = "Transfer", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "TRANSFER RULING ID: ${trf.id}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                                Text(text = trf.transferDate, color = Color.Gray, fontSize = 10.sp)
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Employee: $empName (ID: ${trf.employeeId})", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("PAST WORK SITE", color = Color.Gray, fontSize = 9.sp)
                                    Text(trf.fromSiteName, color = Color(0xFFF87171), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Post: ${trf.fromPosition}", color = Color.Gray, fontSize = 10.sp)
                                }
                                Icon(Icons.Default.ArrowForward, contentDescription = "to", tint = Color.Gray, modifier = Modifier.size(18.dp).align(Alignment.CenterVertically))
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                    Text("DESTINATION SITE", color = Color.Gray, fontSize = 9.sp)
                                    Text(trf.toSiteName, color = Color(0xFF34D399), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Post: ${trf.toPosition}", color = Color.Gray, fontSize = 10.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0F172A))
                                    .padding(6.dp)
                            ) {
                                Text("REASON FOR MOVEMENT: ${trf.reason}", color = Color(0xFF94A3B8), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        } else if (activeSubTab == "Training") {
            // Display comprehensive safety & PSARA Training metrics
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredList) { emp ->
                    val trn = trainingRecords.find { it.employeeId == emp.id }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                        border = borderLine(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(emp.fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                TypeTag(emp.employeeType)
                            }
                            Text("ID: ${emp.id} | Training Stream Matrix Status", color = Color.Gray, fontSize = 11.sp)
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // 5 Pillars of Security Training mandated
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TrainingIndicator("PSARA", trn?.psaraTraining ?: "Not Started", modifier = Modifier.weight(1f))
                                TrainingIndicator("Fire Safety", trn?.fireSafety ?: "Not Started", modifier = Modifier.weight(1f))
                                TrainingIndicator("First Aid", trn?.firstAid ?: "Not Started", modifier = Modifier.weight(1f))
                                TrainingIndicator("Industrial", trn?.industrialSafety ?: "Not Started", modifier = Modifier.weight(1f))
                                TrainingIndicator("Site SOP", trn?.siteSopTraining ?: "Not Started", modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        } else if (activeSubTab == "Documents") {
            // Display Document Vault and KYC status
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredList) { emp ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                        border = borderLine(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(emp.fullName + " (ID: ${emp.id})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                DocStatusBadge("Aadhaar", emp.aadhaar.isNotEmpty())
                                DocStatusBadge("PAN Card", emp.pan.isNotEmpty())
                                DocStatusBadge("Voter ID", emp.voterId.isNotEmpty())
                                DocStatusBadge("Driving Lic", emp.drivingLicense.isNotEmpty())
                                DocStatusBadge("Police Verification", emp.policeVerificationStatus == "Verified")
                                DocStatusBadge("Med Health", emp.readinessScore >= 80.0)
                            }
                        }
                    }
                }
            }
        } else {
            // Employees List (Guards, Supervisors, Staff, Relief Pool, Exits)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (filteredList.isEmpty()) {
                    item { EmptyStateMessage("No matching employee records categorized here.") }
                }
                
                items(filteredList) { emp ->
                    val dpl = deployments.find { it.employeeId == emp.id }
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedEmployeeDetails = emp },
                        border = borderLine()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1E293B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (emp.status == "Suspended") Icons.Default.Warning else Icons.Default.Person,
                                        contentDescription = "Avatar",
                                        tint = if (emp.status == "Suspended") Color(0xFFF87171) else Color(0xFFFF8A00)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = emp.fullName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(if (emp.readinessScore >= 80.0) Color(0xFF059669) else Color(0xFFDC2626))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("${emp.readinessScore.toInt()}% READY", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(
                                        text = "ID: ${emp.id} • ${emp.employeeType} • Join: ${emp.joiningDate}",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = if (dpl != null) "📍 Site: ${dpl.siteName} (${dpl.position})" else "⚡ Pool: Unassigned Ready Relief",
                                        color = if (dpl != null) Color(0xFF38BDF8) else Color(0xFFFFB74D),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            
                            // Action buttons per employee row
                            Column(horizontalAlignment = Alignment.End) {
                                StatusBadge(emp.status)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (emp.status != "Exited") {
                                        Icon(
                                            imageVector = Icons.Default.SwapHoriz,
                                            contentDescription = "Transfer",
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF1E293B))
                                                .clickable { employeeToTransfer = emp }
                                                .padding(4.dp)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Block,
                                            contentDescription = "Suspend",
                                            tint = Color(0xFFF87171),
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF1E293B))
                                                .clickable { viewModel.suspendEmployee(emp.id) }
                                                .padding(4.dp)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ExitToApp,
                                            contentDescription = "Exit",
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF1E293B))
                                                .clickable { viewModel.exitEmployee(emp.id, "Voluntary discharge") }
                                                .padding(4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Onboarding Multi-step Wizard Dialog (12 Steps)
    if (showOnboardWizard) {
        EmployeeOnboardWizard(
            sites = sites,
            onDismiss = { showOnboardWizard = false },
            onCommit = { emp, docs, trn, dpl, eq, lv, pay ->
                viewModel.onboardFirestoreEmployee(emp, docs, trn, dpl, eq, lv, pay)
                showOnboardWizard = false
            }
        )
    }

    // Detailed stats dialog
    selectedEmployeeDetails?.let { emp ->
        val trn = trainingRecords.find { it.employeeId == emp.id }
        val dpl = deployments.find { it.employeeId == emp.id }
        val lvs = leaveProfiles.find { it.employeeId == emp.id }
        val pay = payrollProfiles.find { it.employeeId == emp.id }
        val eq = equipmentRecords.find { it.employeeId == emp.id }

        AlertDialog(
            onDismissRequest = { selectedEmployeeDetails = null },
            title = { Text("Profile Dossier: ${emp.fullName}", color = Color.White) },
            containerColor = Color(0xFF0F172A),
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GroupHeader("1. Basic Information")
                    LabelValue("Father Name", emp.fatherName)
                    LabelValue("Date of Birth", emp.dob)
                    LabelValue("Gender", emp.gender)
                    LabelValue("Mobile", emp.mobile)
                    LabelValue("Email", emp.email)
                    LabelValue("Blood Group", emp.bloodGroup)
                    LabelValue("Marital Status", emp.maritalStatus)
                    
                    GroupHeader("2. Corporate Assignment")
                    LabelValue("Role Type", emp.employeeType)
                    LabelValue("Status Code", emp.status)
                    LabelValue("Deployment Site", dpl?.siteName ?: "Ready relief pool")
                    LabelValue("Work Shift", dpl?.shiftName ?: "None")
                    LabelValue("Manager", emp.reportingManager)

                    GroupHeader("3. Compliance & KYC Indicators")
                    LabelValue("National Aadhaar ID", emp.aadhaar.ifEmpty { "Pending documentation" })
                    LabelValue("Tax PAN Number", emp.pan.ifEmpty { "Pending documentation" })
                    LabelValue("Police Record Verification", emp.policeVerificationStatus)
                    LabelValue("Backcheck Validation", emp.backgroundVerificationStatus)
                    
                    GroupHeader("4. Vault & Files Verified")
                    LabelValue("Aadhaar Registry File", if (emp.aadhaar.isNotEmpty()) "aadhaar_vault_ok.pdf" else "Missing")
                    LabelValue("Financial Tax Copy", if (emp.pan.isNotEmpty()) "pan_vault_ok.pdf" else "Missing")
                    LabelValue("Training Certification", "psara_training_completion.pdf")

                    GroupHeader("5. Statutory Leave Limits")
                    LabelValue("Casual Leaves", "Taken: ${lvs?.casualLeaveTaken ?: 0} / Limit: ${lvs?.casualLeaveLimit ?: 12}")
                    LabelValue("Sick Leaves", "Taken: ${lvs?.sickLeaveTaken ?: 0} / Limit: ${lvs?.sickLeaveLimit ?: 10}")
                    LabelValue("Earned Leaves", "Taken: ${lvs?.earnedLeaveTaken ?: 0} / Limit: ${lvs?.earnedLeaveLimit ?: 15}")

                    GroupHeader("6. Base Compensation Profile")
                    LabelValue("Salary Schema", pay?.baseSalaryOption ?: "Monthly Fixed")
                    LabelValue("Base Value", "${pay?.baseSalaryAmount ?: 2200.0} / Period")
                    LabelValue("CPF Employer PF Match", if (pay?.pfEnabled == true) "Yes (12%)" else "Disabled")
                    LabelValue("ESIC Statutory Share", if (pay?.esicEnabled == true) "Yes" else "Disabled")

                    GroupHeader("7. Issued Equipment & Asset Check")
                    LabelValue("Uniform Issued", if (eq?.uniformSetIssuedDate?.isNotEmpty() == true) "Yes (${eq.uniformSetIssuedDate})" else "No")
                    LabelValue("Walkie Talkie Asset", if (eq?.walkieTalkieIssuedDate?.isNotEmpty() == true) "Yes (${eq.walkieTalkieIssuedDate})" else "No")
                    LabelValue("ID Card Badge", if (eq?.idCardIssuedDate?.isNotEmpty() == true) "Yes (${eq.idCardIssuedDate})" else "No")
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedEmployeeDetails = null }) {
                    Text("DISMISS DOSSIER", color = Color(0xFF38BDF8))
                }
            }
        )
    }

    // Dynamic site-transfer dispatcher dialog
    employeeToTransfer?.let { emp ->
        var selectedSite by remember { mutableStateOf(sites.firstOrNull() ?: Site(id = "ST-NULL", companyGroupId = "grp_singh", companyId = "cmp_centurion_sec", siteId = "ST-NULL", name = "Relief Guard Pool", address = "HQ", latitude = 1.0, longitude = 1.0, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis(), createdBy = "system", updatedBy = "system", status = "active")) }
        var targetShift by remember { mutableStateOf("Morning Shift (08:00 - 20:00)") }
        var targetPosition by remember { mutableStateOf("Asset Protection Guard") }
        var justificationReason by remember { mutableStateOf("Operational optimization request.") }

        AlertDialog(
            onDismissRequest = { employeeToTransfer = null },
            title = { Text("Deploy / Transfer Guard: ${emp.fullName}", color = Color.White) },
            containerColor = Color(0xFF0F172A),
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Reassign the corporate deployment or route this guard into active sites. Calculated readiness scores will check compliance boundaries.", color = Color.Gray, fontSize = 11.sp)
                    
                    Text("Select Destination Site:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        sites.forEach { s ->
                            val isSel = selectedSite.id == s.id
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) Color(0xFF006B54) else Color(0xFF1E293B))
                                    .clickable { selectedSite = s }
                                    .padding(8.dp)
                            ) {
                                Text(s.name, color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = targetShift,
                        onValueChange = { targetShift = it },
                        label = { Text("Assigned Working Shift Hour", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = targetPosition,
                        onValueChange = { targetPosition = it },
                        label = { Text("Deployment Designation Position", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = justificationReason,
                        onValueChange = { justificationReason = it },
                        label = { Text("Audit Action Justification Log", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.transferEmployee(
                            employeeId = emp.id,
                            toSiteId = selectedSite.id,
                            toSiteName = selectedSite.name,
                            toShift = targetShift,
                            toPosition = targetPosition,
                            reason = justificationReason
                        )
                        employeeToTransfer = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006B54))
                ) {
                    Text("EXECUTE DEPLOYMENT")
                }
            },
            dismissButton = {
                TextButton(onClick = { employeeToTransfer = null }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
fun TrainingIndicator(name: String, status: String, modifier: Modifier = Modifier) {
    val color = when (status) {
        "Completed" -> Color(0xFF10B981)
        "In Progress" -> Color(0xFFF59E0B)
        "Expired" -> Color(0xFFEF4444)
        else -> Color(0xFF6B7280)
    }
    Column(
        modifier = modifier
            .background(Color(0xFF1E293B))
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(name, color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(color.copy(alpha = 0.2f))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(status.uppercase(), color = color, fontSize = 7.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
fun DocStatusBadge(name: String, ok: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (ok) Color(0xFF065F46) else Color(0xFF374151))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(
            text = (if (ok) "✓ " else "✗ ") + name.uppercase(),
            color = if (ok) Color(0xFF34D399) else Color(0xFF94A3B8),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun GroupHeader(text: String) {
    Text(
        text = text.uppercase(),
        color = Color(0xFF38BDF8),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun LabelValue(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun TypeTag(type: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFFF8A00).copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(type.uppercase(), color = Color(0xFFFF8A00), fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status.lowercase()) {
        "active", "deployment ready", "joining" -> Color(0xFF34D399)
        "candidate", "document verification", "training" -> Color(0xFFF59E0B)
        "suspended" -> Color(0xFFF87171)
        else -> Color(0xFF94A3B8)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(status.uppercase(), color = color, fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EmptyStateMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.Gray, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
    }
}

fun borderLine(): androidx.compose.foundation.BorderStroke = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))


// ==========================================
// 12-STEP EMPLOYEE ONBOARDING WIZARD SYSTEM
// ==========================================
@Composable
fun EmployeeOnboardWizard(
    sites: List<Site>,
    onDismiss: () -> Unit,
    onCommit: (
        FirestoreEmployee,
        FirestoreEmployeeDocument,
        FirestoreEmployeeTraining,
        FirestoreEmployeeDeployment?,
        FirestoreEmployeeEquipment,
        FirestoreEmployeeLeaveProfile,
        FirestoreEmployeePayrollProfile
    ) -> Unit
) {
    var step by remember { mutableStateOf(1) }
    val totalSteps = 12
    var draft by remember { mutableStateOf(EmployeeOnboardingDraft()) }

    // Dynamic readiness score calculations based on key components
    val readinessScore = remember(draft) {
        var score = 0.0
        // checklist criteria:
        // 1. Documents (Aadhaar & PAN entered) -> 20%
        if (draft.aadhaar.isNotEmpty() && draft.pan.isNotEmpty()) score += 20.0
        // 2. Police verification verified -> 20%
        if (draft.policeVerification == "Verified") score += 20.0
        // 3. Bank details filled out -> 15%
        if (draft.accountNumber.isNotEmpty() && draft.ifscCode.isNotEmpty()) score += 15.0
        // 4. Training PSARA Completed -> 20%
        if (draft.psaraTraining == "Completed") score += 20.0
        // 5. Uniform Issued -> 15%
        if (draft.isUniformIssued) score += 15.0
        // 6. Medical Fitness -> 10%
        if (draft.medicalFitnessVerified) score += 10.0
        score.coerceAtMost(100.0)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .border(2.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header step index
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🛡️ STEP $step OF $totalSteps: WORKFORCE SETUP",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "New Enterprise Employee Provisioning",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "dismiss", tint = Color.Gray)
                    }
                }

                // Progress Indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    for (i in 1..totalSteps) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (i <= step) Color(0xFF006B54) else Color(0xFF1E293B))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable content area per steps
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (step) {
                        1 -> {
                            Text("Step 1: Employee Basic Information", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            
                            OutlinedTextField(
                                value = draft.fullName,
                                onValueChange = { draft = draft.copy(fullName = it) },
                                label = { Text("Full Name (Mandatory)", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = draft.fatherName,
                                onValueChange = { draft = draft.copy(fatherName = it) },
                                label = { Text("Father/Guardian Surname Name", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = draft.mobile,
                                onValueChange = { draft = draft.copy(mobile = it) },
                                label = { Text("Mobile Phone Number (Mandatory)", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = draft.email,
                                onValueChange = { draft = draft.copy(email = it) },
                                label = { Text("Corporate Security Portal Email", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text("Gender Assignment Selection:", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                GENDERS.forEach { opt ->
                                    val isSel = draft.gender == opt
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSel) Color(0xFF006B54) else Color(0xFF1E293B))
                                            .clickable { draft = draft.copy(gender = opt) }
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(opt, color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }

                            Text("Blood Group Array:", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                BLOOD_GROUPS.take(4).forEach { bg ->
                                    val isSel = draft.bloodGroup == bg
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSel) Color(0xFF006B54) else Color(0xFF1E293B))
                                            .clickable { draft = draft.copy(bloodGroup = bg) }
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(bg, color = Color.White, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        2 -> {
                            Text("Step 2: Permanent & Current Address Records", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Permanent Home Address Details:", color = Color(0xFF38BDF8), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            
                            OutlinedTextField(
                                value = draft.permAddress,
                                onValueChange = { draft = draft.copy(permAddress = it) },
                                label = { Text("House/Flat No, Residential Block, Street", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = draft.permCity,
                                    onValueChange = { draft = draft.copy(permCity = it) },
                                    label = { Text("City", color = Color.Gray, fontSize = 11.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = draft.permPincode,
                                    onValueChange = { draft = draft.copy(permPincode = it) },
                                    label = { Text("Zip / Pincode", color = Color.Gray, fontSize = 11.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Text("Current Address Details (If different):", color = Color(0xFF38BDF8), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            OutlinedTextField(
                                value = draft.currAddress,
                                onValueChange = { draft = draft.copy(currAddress = it) },
                                label = { Text("Current Living House/Flat details", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        3 -> {
                            Text("Step 3: Employment Profile Settings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            
                            Text("Role Class Assignment (Operational):", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                EMPLOYEE_TYPES.chunked(4).forEach { row ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        row.forEach { type ->
                                            val isSel = draft.employeeType == type
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isSel) Color(0xFF006B54) else Color(0xFF1E293B))
                                                    .clickable { draft = draft.copy(employeeType = type) }
                                                    .padding(8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(type, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = draft.joiningDate,
                                onValueChange = { draft = draft.copy(joiningDate = it) },
                                label = { Text("Joining Assignment Date (YYYY-MM-DD)", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = draft.reportingManager,
                                onValueChange = { draft = draft.copy(reportingManager = it) },
                                label = { Text("Assigned Reporting Supervisor/Manager", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        4 -> {
                            Text("Step 4: Statutory KYC Verification & Compliance Checks", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            
                            OutlinedTextField(
                                value = draft.aadhaar,
                                onValueChange = { draft = draft.copy(aadhaar = it) },
                                label = { Text("National Aadhaar ID/Social Card Number (Required)", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = draft.pan,
                                onValueChange = { draft = draft.copy(pan = it) },
                                label = { Text("Tax Registration PAN ID (Required)", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = draft.drivingLicense,
                                onValueChange = { draft = draft.copy(drivingLicense = it) },
                                label = { Text("Driving License (Optional)", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text("Police Clearance Certification Status:", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                VERIFICATION_STATUSES.forEach { opt ->
                                    val isSel = draft.policeVerification == opt
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSel) Color(0xFF006B54) else Color(0xFF1E293B))
                                            .clickable { draft = draft.copy(policeVerification = opt) }
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(opt, color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        5 -> {
                            Text("Step 5: Salary Bank Disbursement Registry", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            
                            OutlinedTextField(
                                value = draft.accountHolder,
                                onValueChange = { draft = draft.copy(accountHolder = it) },
                                label = { Text("Account Holder Display Name", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = draft.bankName,
                                onValueChange = { draft = draft.copy(bankName = it) },
                                label = { Text("Bank Name e.g. DBS Bank, OCBC", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = draft.accountNumber,
                                onValueChange = { draft = draft.copy(accountNumber = it) },
                                label = { Text("Bank Account System Destination Number", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = draft.ifscCode,
                                onValueChange = { draft = draft.copy(ifscCode = it) },
                                label = { Text("Statutory IFSC / SWIFT Route Code", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        6 -> {
                            Text("Step 6: Training and Tactical Certification Matrices", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            
                            Text("1. PSARA Training Stream:", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TRAINING_STATUSES.forEach { opt ->
                                    val isSel = draft.psaraTraining == opt
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSel) Color(0xFF006B54) else Color(0xFF1E293B))
                                            .clickable { draft = draft.copy(psaraTraining = opt) }
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(opt, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Text("2. Fire Safety Drills:", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TRAINING_STATUSES.forEach { opt ->
                                    val isSel = draft.fireSafety == opt
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSel) Color(0xFF006B54) else Color(0xFF1E293B))
                                            .clickable { draft = draft.copy(fireSafety = opt) }
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(opt, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Text("3. First Aid Medical Certification:", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TRAINING_STATUSES.forEach { opt ->
                                    val isSel = draft.firstAid == opt
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSel) Color(0xFF006B54) else Color(0xFF1E293B))
                                            .clickable { draft = draft.copy(firstAid = opt) }
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(opt, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        7 -> {
                            Text("Step 7: Uniform Asset & Personal Equipment Issuance", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Uniform Set (Standard Duty Outerwear)", color = Color.White, fontSize = 13.sp)
                                Switch(checked = draft.isUniformIssued, onCheckedChange = { draft = draft.copy(isUniformIssued = it) })
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("SLA Enclosed ID Badge Card", color = Color.White, fontSize = 13.sp)
                                Switch(checked = draft.isIdCardIssued, onCheckedChange = { draft = draft.copy(isIdCardIssued = it) })
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Safety Boots / Combat Shoes", color = Color.White, fontSize = 13.sp)
                                Switch(checked = draft.isShoesIssued, onCheckedChange = { draft = draft.copy(isShoesIssued = it) })
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Tactical Guard Webbing Belt", color = Color.White, fontSize = 13.sp)
                                Switch(checked = draft.isBeltIssued, onCheckedChange = { draft = draft.copy(isBeltIssued = it) })
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Motorola VHF Walkie Talkie Radioset", color = Color.White, fontSize = 13.sp)
                                Switch(checked = draft.isWalkieIssued, onCheckedChange = { draft = draft.copy(isWalkieIssued = it) })
                            }
                        }

                        8 -> {
                            Text("Step 8: Auto deployment readiness scores", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Document Vault Verified Check", color = Color.White, fontSize = 13.sp)
                                Switch(checked = draft.documentsVerified, onCheckedChange = { draft = draft.copy(documentsVerified = it) })
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Medical Health & Fitness Clearance Certification", color = Color.White, fontSize = 13.sp)
                                Switch(checked = draft.medicalFitnessVerified, onCheckedChange = { draft = draft.copy(medicalFitnessVerified = it) })
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (readinessScore >= 80) Color(0xFF065F46) else Color(0xFF7F1D1D))
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "CALCULATED READINESS SCORE: ${readinessScore.toInt()}%",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (readinessScore >= 80.0) "✓ PASSED COMPLIANCE THRESHOLD (>=80%). This employee is cleared for deployment on client commercial properties."
                                               else "⚠️ LOCKED: Score under 80%. System will prohibit assignment of this employee to active roster shifts until gaps are resolved.",
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        9 -> {
                            Text("Step 9: Standard Client Site Deployment", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            
                            if (readinessScore < 80.0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF7F1D1D))
                                        .padding(12.dp)
                                ) {
                                    Text("⚠️ DEPLOYMENT PREVENTED: This guard has a deployment readiness score under 80% and cannot be assigned to any custom site. Proceeding as generic unassigned relief guard pool.", color = Color.White, fontSize = 12.sp)
                                }
                            } else {
                                Text("Select Destination Site Assignment:", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    sites.forEach { s ->
                                        val isSel = draft.selectedSiteId == s.id
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSel) Color(0xFF006B54) else Color(0xFF1E293B))
                                                .clickable { draft = draft.copy(selectedSiteId = s.id, selectedSiteName = s.name) }
                                                .padding(10.dp)
                                        ) {
                                            Text(s.name, color = Color.White, fontSize = 12.sp)
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = draft.shiftName,
                                    onValueChange = { draft = draft.copy(shiftName = it) },
                                    label = { Text("Assigned shift hour code description", color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        10 -> {
                            Text("Step 10: Leave Allowances and Attendance Profile Initialization", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            
                            Text("Attendance Verification Channel Mode Preference:", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                ATTENDANCE_PREFERENCES.forEach { opt ->
                                    val isSel = draft.attendancePreference == opt
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSel) Color(0xFF006B54) else Color(0xFF1E293B))
                                            .clickable { draft = draft.copy(attendancePreference = opt) }
                                            .padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(opt, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Initial Leave Balances configured automatically:", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            LabelValue("Casual Leave Allowance", "${draft.casualLeaveLimit} Days / Yr")
                            LabelValue("Sick Medical Leave Allowance", "${draft.sickLeaveLimit} Days / Yr")
                            LabelValue("Earned Privilege Leave Balance", "${draft.earnedLeaveLimit} Days / Yr")
                        }

                        11 -> {
                            Text("Step 11: Emergency Contact & Support Directives", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            
                            OutlinedTextField(
                                value = draft.emergencyContactName,
                                onValueChange = { draft = draft.copy(emergencyContactName = it) },
                                label = { Text("Primary Next-of-Kin Contact Name (Required)", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = draft.emergencyContactRelation,
                                onValueChange = { draft = draft.copy(emergencyContactRelation = it) },
                                label = { Text("Relationship to Employee (e.g. Spouse / Brother)", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = draft.emergencyContactPhone,
                                onValueChange = { draft = draft.copy(emergencyContactPhone = it) },
                                label = { Text("Emergency Dispatch Cellphone Mobile", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        12 -> {
                            Text("Step 12: Statutory Digital Document Vault", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Provide filenames simulating upload in Cloud Firestore/Security Storage:", color = Color.Gray, fontSize = 11.sp)
                            
                            OutlinedTextField(
                                value = draft.aadhaarDocUrl.ifEmpty { "aadhaar_${draft.empId.lowercase()}.pdf" },
                                onValueChange = { draft = draft.copy(aadhaarDocUrl = it) },
                                label = { Text("Aadhaar Identity Document Vault Copy", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = draft.panDocUrl.ifEmpty { "pan_financial_${draft.empId.lowercase()}.pdf" },
                                onValueChange = { draft = draft.copy(panDocUrl = it) },
                                label = { Text("Income Tax Account PAN Scanned Copy", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = draft.photoDocUrl.ifEmpty { "display_portrait_${draft.empId.lowercase()}.jpg" },
                                onValueChange = { draft = draft.copy(photoDocUrl = it) },
                                label = { Text("Profile Photograph ID Portrait URL", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation controls footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            if (step > 1) step--
                        },
                        enabled = step > 1
                    ) {
                        Text("PREVIOUS", color = if (step > 1) Color.White else Color.DarkGray)
                    }

                    Button(
                        onClick = {
                            if (step < totalSteps) {
                                // Add strict validations for Step 1
                                if (step == 1 && (draft.fullName.trim().isEmpty() || draft.mobile.trim().isEmpty())) {
                                    // Custom visual alert simulation
                                    return@Button
                                }
                                step++
                            } else {
                                // Final submission and compilation! Add automatic state generation
                                val employeeRecord = FirestoreEmployee(
                                    id = draft.empId,
                                    tenantId = "tenant_singh_sec",
                                    companyId = "cmp_centurion_sec",
                                    fullName = draft.fullName,
                                    fatherName = draft.fatherName,
                                    dob = draft.dob,
                                    gender = draft.gender,
                                    mobile = draft.mobile,
                                    altMobile = draft.altMobile,
                                    email = draft.email,
                                    bloodGroup = draft.bloodGroup,
                                    maritalStatus = draft.maritalStatus,
                                    permanentAddress = FirestoreWfAddress(draft.permAddress, draft.permCity, draft.permState, draft.permPincode),
                                    currentAddress = FirestoreWfAddress(draft.currAddress, draft.currCity, draft.currState, draft.currPincode),
                                    employeeType = draft.employeeType,
                                    joiningDate = draft.joiningDate,
                                    reportingManager = draft.reportingManager,
                                    status = if (readinessScore >= 80.0 && draft.selectedSiteId.isNotEmpty()) "Active" else "Candidate",
                                    aadhaar = draft.aadhaar,
                                    pan = draft.pan,
                                    voterId = draft.voterId,
                                    drivingLicense = draft.drivingLicense,
                                    passport = draft.passport,
                                    policeVerificationStatus = draft.policeVerification,
                                    backgroundVerificationStatus = draft.backgroundVerification,
                                    accountHolder = draft.accountHolder,
                                    bankName = draft.bankName,
                                    accountNumber = draft.accountNumber,
                                    ifscCode = draft.ifscCode,
                                    upiId = draft.upiId,
                                    readinessScore = readinessScore,
                                    attendancePreference = draft.attendancePreference,
                                    emergencyContactName = draft.emergencyContactName,
                                    emergencyContactRelation = draft.emergencyContactRelation,
                                    emergencyContactPhone = draft.emergencyContactPhone
                                )

                                val documentVaultRecord = FirestoreEmployeeDocument(
                                    id = "doc_" + draft.empId.lowercase(),
                                    tenantId = "tenant_singh_sec",
                                    companyId = "cmp_centurion_sec",
                                    employeeId = draft.empId,
                                    aadhaarDocUrl = draft.aadhaarDocUrl.ifEmpty { "aadhaar_${draft.empId.lowercase()}.pdf" },
                                    panDocUrl = draft.panDocUrl.ifEmpty { "pan_financial_${draft.empId.lowercase()}.pdf" },
                                    photoUrl = draft.photoDocUrl.ifEmpty { "portrait_${draft.empId.lowercase()}.jpg" },
                                    policeVerificationUrl = "police_verified_${draft.empId.lowercase()}.pdf"
                                )

                                val trainingRecordInstance = FirestoreEmployeeTraining(
                                    id = "trn_" + draft.empId.lowercase(),
                                    tenantId = "tenant_singh_sec",
                                    companyId = "cmp_centurion_sec",
                                    employeeId = draft.empId,
                                    psaraTraining = draft.psaraTraining,
                                    fireSafety = draft.fireSafety,
                                    firstAid = draft.firstAid,
                                    industrialSafety = draft.industrialSafety,
                                    siteSopTraining = draft.siteSopTraining
                                )

                                val activeDeploymentRecord = if (readinessScore >= 80.0 && draft.selectedSiteId.isNotEmpty()) {
                                    FirestoreEmployeeDeployment(
                                        id = "dpl_" + UUID.randomUUID().toString().take(6),
                                        tenantId = "tenant_singh_sec",
                                        companyId = "cmp_centurion_sec",
                                        employeeId = draft.empId,
                                        siteId = draft.selectedSiteId,
                                        siteName = draft.selectedSiteName,
                                        shiftName = draft.shiftName,
                                        position = draft.position,
                                        reportingSupervisor = draft.reportingManager
                                    )
                                } else null

                                val equipmentRecord = FirestoreEmployeeEquipment(
                                    id = "eq_" + draft.empId.lowercase(),
                                    tenantId = "tenant_singh_sec",
                                    companyId = "cmp_centurion_sec",
                                    employeeId = draft.empId,
                                    uniformSetIssuedDate = if (draft.isUniformIssued) "2026-06-22" else "",
                                    idCardIssuedDate = if (draft.isIdCardIssued) "2026-06-22" else ""
                                )

                                val leaveProfile = FirestoreEmployeeLeaveProfile(
                                    id = "lv_" + draft.empId.lowercase(),
                                    tenantId = "tenant_singh_sec",
                                    companyId = "cmp_centurion_sec",
                                    employeeId = draft.empId,
                                    casualLeaveLimit = draft.casualLeaveLimit,
                                    sickLeaveLimit = draft.sickLeaveLimit,
                                    earnedLeaveLimit = draft.earnedLeaveLimit
                                )

                                val payrollProfile = FirestoreEmployeePayrollProfile(
                                    id = "pay_" + draft.empId.lowercase(),
                                    tenantId = "tenant_singh_sec",
                                    companyId = "cmp_centurion_sec",
                                    employeeId = draft.empId,
                                    baseSalaryAmount = if (draft.employeeType.contains("Supervisor")) 2800.0 else 2200.0,
                                    bankAccountHolder = draft.accountHolder,
                                    bankAccountNumber = draft.accountNumber
                                )

                                onCommit(
                                    employeeRecord,
                                    documentVaultRecord,
                                    trainingRecordInstance,
                                    activeDeploymentRecord,
                                    equipmentRecord,
                                    leaveProfile,
                                    payrollProfile
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006B54))
                    ) {
                        Text(if (step == totalSteps) "COMPLETE PROVISIONING" else "NEXT")
                    }
                }
            }
        }
    }
}
