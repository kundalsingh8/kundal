package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MasterDataPane(
    viewModel: EnterpriseViewModel
) {
    // Collect all Master Data Flow states
    val companies = viewModel.companiesMaster.collectAsStateWithLifecycle().value
    val clients = viewModel.clientsMaster.collectAsStateWithLifecycle().value
    val sites = viewModel.sitesMaster.collectAsStateWithLifecycle().value
    val workforce = viewModel.workforceMaster.collectAsStateWithLifecycle().value
    val assets = viewModel.assetsMaster.collectAsStateWithLifecycle().value
    val contracts = viewModel.contractsMaster.collectAsStateWithLifecycle().value
    val rates = viewModel.ratesMaster.collectAsStateWithLifecycle().value
    val payroll = viewModel.payrollMaster.collectAsStateWithLifecycle().value
    val billing = viewModel.billingMaster.collectAsStateWithLifecycle().value
    val templates = viewModel.templatesMaster.collectAsStateWithLifecycle().value
    val compliance = viewModel.complianceMaster.collectAsStateWithLifecycle().value
    val approvals = viewModel.approvalRequests.collectAsStateWithLifecycle().value

    // Firestore Collections Checked Multi-Tenant states
    val fCompanies = viewModel.firestoreCompanies.collectAsStateWithLifecycle().value
    val fClients = viewModel.firestoreClients.collectAsStateWithLifecycle().value
    val fSites = viewModel.firestoreSites.collectAsStateWithLifecycle().value
    val fUsers = viewModel.firestoreUsers.collectAsStateWithLifecycle().value
    val fLogs = viewModel.firestoreAuditLogs.collectAsStateWithLifecycle().value

    var selectedTab by remember { mutableStateOf("Clients") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    // Client Onboarding Workspace custom state fields
    var selectedClientEntity by remember { mutableStateOf<FirestoreClient?>(null) }
    var showOnboardingWizard by remember { mutableStateOf(false) }
    var clientSearchQuery by remember { mutableStateOf("") }

    // Multi-tenant Firestore sandbox explorer states
    var activeVaultTenantFilter by remember { mutableStateOf("tenant_singh_sec") }
    var activeVaultCollection by remember { mutableStateOf("clients") }

    // Navigation sub-directories
    val tabs = listOf(
        "Companies" to Icons.Default.Business,
        "Clients" to Icons.Default.People,
        "Sites" to Icons.Default.Place,
        "Workforce" to Icons.Default.AccountCircle,
        "Assets" to Icons.Default.Build,
        "Contracts" to Icons.Default.Assignment,
        "Rates" to Icons.Default.AttachMoney,
        "Payroll" to Icons.Default.CreditCard,
        "Billing" to Icons.Default.Receipt,
        "Templates" to Icons.Default.Mail,
        "Compliance" to Icons.Default.CheckCircle,
        "Approvals" to Icons.Default.Check,
        "Firestore Vault" to Icons.Default.Lock
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF040813))
            .testTag("mdm_platform_root")
    ) {
        // LEFT ENTERPRISE SIDEBAR
        Column(
            modifier = Modifier
                .width(170.dp)
                .fillMaxHeight()
                .background(Color(0xFF0A0F1D))
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = "MASTER DATABASE",
                color = Color(0xFF64748B),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(tabs) { (tabName, icon) ->
                    val isSel = selectedTab == tabName
                    val pendingCount = if (tabName == "Approvals") approvals.count { it.status == "Pending" } else 0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) Color(0xFF006B54) else Color.Transparent)
                            .clickable {
                                selectedTab = tabName
                                searchQuery = ""
                            }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = tabName,
                            tint = if (isSel) Color.White else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = tabName,
                            color = if (isSel) Color.White else Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (pendingCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$pendingCount",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0E1629))
                    .padding(8.dp)
            ) {
                Column {
                    Text("ERP LAYER V12", color = Color(0xFF34D399), fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Text("Tenant Secure Hash", color = Color.Gray, fontSize = 7.sp)
                    Text("centurion_sec_sha256", color = Color(0xFF60A5FA), fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Thin Vertical Divider line separating sidebar from main content
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(Color(0xFF1E293B))
        )

        // RIGHT ENTITY WORKSPACE
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            if (selectedTab == "Clients") {
                if (showOnboardingWizard) {
                    ClientOnboardingWizard(
                        onDismiss = { showOnboardingWizard = false },
                        onSubmit = { newClient ->
                            viewModel.onboardFirestoreClient(newClient)
                        },
                        viewModel = viewModel
                    )
                } else if (selectedClientEntity != null) {
                    ClientDetailsScreen(
                        client = selectedClientEntity!!,
                        auditLogs = fLogs,
                        onBack = { selectedClientEntity = null },
                        onSaveClientUpdates = { updatedClient ->
                            viewModel.updateFirestoreClient(updatedClient)
                            selectedClientEntity = updatedClient
                        },
                        viewModel = viewModel
                    )
                } else {
                    ClientListScreen(
                        clients = fClients,
                        onSelectClient = { selectedClientEntity = it },
                        onLaunchWizard = { showOnboardingWizard = true },
                        searchQuery = clientSearchQuery,
                        onQueryChange = { clientSearchQuery = it }
                    )
                }
            } else {
                // HEADER BAR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                Column {
                    Text(
                        text = "🔒 MASTER DATA CENTER ($selectedTab)".uppercase(),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Unified single-source-of-truth record logs representing authorized system parameters.",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                if (selectedTab != "Approvals") {
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006B54)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Master", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // SEARCH AND METRICS BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter master records...", color = Color.Gray, fontSize = 11.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF0A0F1D),
                        unfocusedContainerColor = Color(0xFF0A0F1D),
                        focusedBorderColor = Color(0xFF006B54),
                        unfocusedBorderColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray, modifier = Modifier.size(16.dp)) }
                )

                // Quick statistics card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F1D)),
                    modifier = Modifier
                        .height(48.dp)
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp),
                ) {
                    Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "LOGGED ENTITIES: " + when (selectedTab) {
                                "Companies" -> companies.size
                                "Clients" -> clients.size
                                "Sites" -> sites.size
                                "Workforce" -> workforce.size
                                "Assets" -> assets.size
                                "Contracts" -> contracts.size
                                "Rates" -> rates.size
                                "Payroll" -> payroll.size
                                "Billing" -> billing.size
                                "Templates" -> templates.size
                                "Compliance" -> compliance.size
                                "Approvals" -> approvals.size
                                "Firestore Vault" -> {
                                    val filteredCompanies = fCompanies.filter { it.tenantId == activeVaultTenantFilter }
                                    val filteredClients = fClients.filter { it.tenantId == activeVaultTenantFilter }
                                    val filteredSites = fSites.filter { it.tenantId == activeVaultTenantFilter }
                                    val filteredUsers = fUsers.filter { it.tenantId == activeVaultTenantFilter }
                                    val filteredLogs = fLogs.filter { it.tenantId == activeVaultTenantFilter }
                                    filteredCompanies.size + filteredClients.size + filteredSites.size + filteredUsers.size + filteredLogs.size
                                }
                                else -> 0
                            },
                            color = Color(0xFF60A5FA),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // MAIN DATABASE SCROLLABLE VIEW
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF070B18)),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (selectedTab) {
                        "Companies" -> {
                            val filtered = companies.filter { it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery, ignoreCase = true) }
                            if (filtered.isEmpty()) {
                                item { Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text("No matching companies registered.", color = Color.Gray, fontSize = 12.sp) } }
                            } else {
                                items(filtered) { c ->
                                    CompanyMasterRow(c)
                                }
                            }
                        }
                        "Clients" -> {
                            val filtered = clients.filter { it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery, ignoreCase = true) || it.industry.contains(searchQuery, ignoreCase = true) }
                            if (filtered.isEmpty()) {
                                item { Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text("No matching clients registered.", color = Color.Gray, fontSize = 12.sp) } }
                            } else {
                                items(filtered) { cl ->
                                    ClientMasterRow(cl)
                                }
                            }
                        }
                        "Sites" -> {
                            val filtered = sites.filter { it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery, ignoreCase = true) || it.clientName.contains(searchQuery, ignoreCase = true) }
                            if (filtered.isEmpty()) {
                                item { Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text("No matching sites registered.", color = Color.Gray, fontSize = 12.sp) } }
                            } else {
                                items(filtered) { s ->
                                    SiteMasterRow(s)
                                }
                            }
                        }
                        "Workforce" -> {
                            val filtered = workforce.filter { it.name.contains(searchQuery, ignoreCase = true) || it.empId.contains(searchQuery, ignoreCase = true) || it.role.contains(searchQuery, ignoreCase = true) }
                            if (filtered.isEmpty()) {
                                item { Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text("No matching workforce files registered.", color = Color.Gray, fontSize = 12.sp) } }
                            } else {
                                items(filtered) { w ->
                                    WorkforceMasterRow(w)
                                }
                            }
                        }
                        "Assets" -> {
                            val filtered = assets.filter { it.serialNumber.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true) }
                            if (filtered.isEmpty()) {
                                item { Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text("No assets registered.", color = Color.Gray, fontSize = 12.sp) } }
                            } else {
                                items(filtered) { a ->
                                    AssetMasterRow(a)
                                }
                            }
                        }
                        "Contracts" -> {
                            val filtered = contracts.filter { it.contractCode.contains(searchQuery, ignoreCase = true) || it.clientCode.contains(searchQuery, ignoreCase = true) }
                            if (filtered.isEmpty()) {
                                item { Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text("No matching contracts registered.", color = Color.Gray, fontSize = 12.sp) } }
                            } else {
                                items(filtered) { con ->
                                    ContractMasterRow(con)
                                }
                            }
                        }
                        "Rates" -> {
                            val filtered = rates.filter { it.grade.contains(searchQuery, ignoreCase = true) }
                            if (filtered.isEmpty()) {
                                item { Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text("No standard rate cards compiled.", color = Color.Gray, fontSize = 12.sp) } }
                            } else {
                                items(filtered) { r ->
                                    RateCardMasterRow(r)
                                }
                            }
                        }
                        "Payroll" -> {
                            val filtered = payroll.filter { it.structureName.contains(searchQuery, ignoreCase = true) }
                            if (filtered.isEmpty()) {
                                item { Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text("No structured payroll rule groups.", color = Color.Gray, fontSize = 12.sp) } }
                            } else {
                                items(filtered) { p ->
                                    PayrollMasterRow(p)
                                }
                            }
                        }
                        "Billing" -> {
                            val filtered = billing.filter { it.templateName.contains(searchQuery, ignoreCase = true) }
                            if (filtered.isEmpty()) {
                                item { Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text("No core billing records defined.", color = Color.Gray, fontSize = 12.sp) } }
                            } else {
                                items(filtered) { b ->
                                    BillingMasterRow(b)
                                }
                            }
                        }
                        "Templates" -> {
                            val filtered = templates.filter { it.templateName.contains(searchQuery, ignoreCase = true) || it.channel.contains(searchQuery, ignoreCase = true) }
                            if (filtered.isEmpty()) {
                                item { Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text("No comm templates registered.", color = Color.Gray, fontSize = 12.sp) } }
                            } else {
                                items(filtered) { t ->
                                    CommunicationMasterRow(t)
                                }
                            }
                        }
                        "Compliance" -> {
                            val filtered = compliance.filter { it.name.contains(searchQuery, ignoreCase = true) || it.agreementCode.contains(searchQuery, ignoreCase = true) }
                            if (filtered.isEmpty()) {
                                item { Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text("No statutory compliance entries recorded.", color = Color.Gray, fontSize = 12.sp) } }
                            } else {
                                items(filtered) { r ->
                                    ComplianceMasterRow(r)
                                }
                            }
                        }
                        "Approvals" -> {
                            val filtered = approvals.filter { it.moduleName.contains(searchQuery, ignoreCase = true) || it.status.contains(searchQuery, ignoreCase = true) }
                            if (filtered.isEmpty()) {
                                item { Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text("No action pipeline queue requests.", color = Color.Gray, fontSize = 12.sp) } }
                            } else {
                                items(filtered) { req ->
                                    ApprovalRequestRow(
                                        request = req,
                                        onApprove = { viewModel.approveRequest(req.id) },
                                        onReject = { viewModel.rejectRequest(req.id) }
                                    )
                                }
                            }
                        }
                        "Firestore Vault" -> {
                            // RENDER EXQUISITE MULTI-TENANT FIRESTORE PORTAL DEMONSTRATOR
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF070B18), RoundedCornerShape(12.dp))
                                        .border(2.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // SECURITY DISCLAIMER
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF0F172A))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF059669)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Lock",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "KUNDAL SECURITY MULTI-TENANCY COMPLIANCE ENGINE",
                                                color = Color(0xFF10B981),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = "Every secure Firestore read/write query is enforced with mandatory filters preventing cross-tenant access. Test isolation below.",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    // SELECT TENANT FILTER TOGGLING
                                    Column {
                                        Text(
                                            text = "SIMULATE CURRENT USER AUTHENTICATED TENANT ID:",
                                            color = Color(0xFF64748B),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            val tenants = listOf(
                                                "tenant_singh_sec" to "Centurion Security (Authorized Tenant)",
                                                "tenant_malaysia_guards" to "Malaysia Sentry (Foreign Tenant Group)",
                                                "tenant_other_unauthorized" to "Unauthorized Probe Group (Empty)"
                                            )
                                            tenants.forEach { (tid, tlabel) ->
                                                val isTSelected = activeVaultTenantFilter == tid
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isTSelected) Color(0xFF047857) else Color(0xFF181F30))
                                                        .clickable { 
                                                            activeVaultTenantFilter = tid 
                                                            viewModel.showToast("Switched Simulator Tenant Context to: $tid")
                                                        }
                                                        .padding(10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(tlabel, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                                        Text(tid, color = if (isTSelected) Color(0xFFA7F3D0) else Color.Gray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // COLLECTION SELECTOR
                                    Column {
                                        Text(
                                            text = "CHOOSE FIRESTORE TARGET COLLECTION:",
                                            color = Color(0xFF64748B),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val collections = listOf("companies", "clients", "sites", "users", "audit_logs")
                                            collections.forEach { colName ->
                                                val isCSelected = activeVaultCollection == colName
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(20.dp))
                                                        .background(if (isCSelected) Color(0xFF1D4ED8) else Color(0xFF0F172A))
                                                        .clickable { activeVaultCollection = colName }
                                                        .padding(vertical = 8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(colName.uppercase(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(1.dp))

                                    // SECURE QUERIED OUTPUT RECORDS
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "SECURE QUERY RESULTS: .where(\"tenantId\", \"==\", \"$activeVaultTenantFilter\")",
                                                color = Color(0xFF38BDF8),
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF0C1FE3))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("COMPLIANCE SECURE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        // Select & Filter based on active tenantId
                                        when (activeVaultCollection) {
                                            "companies" -> {
                                                val col = fCompanies.filter { it.tenantId == activeVaultTenantFilter }
                                                if (col.isEmpty()) {
                                                    Text("🚫 QUARANTINED: Empty database results under this secure boundary context.", color = Color.Red, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                                } else {
                                                    col.forEach { c ->
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .background(Color(0xFF0B1320))
                                                                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                                                .padding(12.dp),
                                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Text("Document ID: ${c.id}", color = Color(0xFF34D399), fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                                            Text("• Code: ${c.companyCode} | Name: ${c.companyName}", color = Color.White, fontSize = 10.sp)
                                                            Text("• Legal Company: ${c.legalCompanyName} [Plan: " + c.subscriptionPlan.uppercase() + "]", color = Color.LightGray, fontSize = 9.sp)
                                                            Text("• PAN: ${c.panNumber} | GST No: " + (c.gstNumber ?: "N/A"), color = Color.Gray, fontSize = 9.sp)
                                                            Text("• Address: ${c.address.line1}, ${c.address.city}, ${c.address.country}", color = Color.Gray, fontSize = 9.sp)
                                                            Text("• Contact Officer: ${c.contact.name} (${c.contact.mobile})", color = Color.Gray, fontSize = 9.sp)
                                                        }
                                                    }
                                                }
                                            }
                                            "clients" -> {
                                                val col = fClients.filter { it.tenantId == activeVaultTenantFilter }
                                                if (col.isEmpty()) {
                                                    Text("🚫 QUARANTINED: Empty database results under this secure boundary context.", color = Color.Red, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                                } else {
                                                    col.forEach { c ->
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .background(Color(0xFF0B1320))
                                                                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                                                .padding(12.dp),
                                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                                Text("Document ID: ${c.id}", color = Color(0xFF34D399), fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                                                Box(modifier = Modifier.background(Color(0xFF065F46)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                                                                    Text(c.status.uppercase(), color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                                                }
                                                            }
                                                            Text("• Client Name: ${c.clientName} [Code: ${c.clientCode}] (Type: ${c.clientType})", color = Color.White, fontSize = 10.sp)
                                                            Text("• Primary Key User: ${c.contactPerson} (${c.designation}) | Ph: ${c.mobile}", color = Color.LightGray, fontSize = 9.sp)
                                                            Text("• Mail Mapping: ${c.email} | Portal mapped SSO: " + (c.portalEmail ?: "None"), color = Color.Gray, fontSize = 9.sp)
                                                            Text("• SLA Contract No: ${c.contractCode} | Expiry: ${c.contractEndDate} | Valuation: ${c.contractValuation}", color = Color.LightGray, fontSize = 9.sp)
                                                            Text("• Billing Policy: ${c.billingRules} | GST No: " + (c.gstNumber ?: "Pending"), color = Color.Gray, fontSize = 9.sp)
                                                            Text("• Level 1 Escalation: ${c.escalationMatrix.level1}", color = Color(0xFFFCA5A5), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                                            Text("• Emergency Response: ${c.escalationMatrix.emergency}", color = Color(0xFFEF4444), fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                            "sites" -> {
                                                val col = fSites.filter { it.tenantId == activeVaultTenantFilter }
                                                if (col.isEmpty()) {
                                                    Text("🚫 QUARANTINED: Empty database results under this secure boundary context.", color = Color.Red, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                                } else {
                                                    col.forEach { s ->
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .background(Color(0xFF0B1320))
                                                                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                                                .padding(12.dp),
                                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Text("Document ID: ${s.id}", color = Color(0xFF34D399), fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                                            Text("• Site Name: ${s.siteName} [${s.siteCode}] mapped to clientID ${s.clientId}", color = Color.White, fontSize = 10.sp)
                                                            Text("• Sentry Shift Mode: ${s.shiftModel}-Shift Model | Shift Start: ${s.shiftStartTime}", color = Color.LightGray, fontSize = 9.sp)
                                                            Text("• GPS coordinates: Lat ${s.gps.latitude} Lng ${s.gps.longitude} Fence: ${s.gps.geofenceRadius}m", color = Color.Gray, fontSize = 9.sp)
                                                            Text("• Location: ${s.address.line1}, ${s.address.area}, ${s.address.city}", color = Color.Gray, fontSize = 9.sp)
                                                            Text("• Sentry Readiness Score: ${s.readinessScore}% SLA Grade", color = Color(0xFF34D399), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                            "users" -> {
                                                val col = fUsers.filter { it.tenantId == activeVaultTenantFilter }
                                                if (col.isEmpty()) {
                                                    Text("🚫 QUARANTINED: Empty database results under this secure boundary context.", color = Color.Red, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                                } else {
                                                    col.forEach { u ->
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .background(Color(0xFF0B1320))
                                                                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                                                .padding(12.dp),
                                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Text("Document ID: ${u.id}", color = Color(0xFF34D399), fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                                            Text("• Full Username: ${u.fullName} [Role: ${u.role.uppercase()}]", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                            Text("• Contact Phone: ${u.mobile} | Email: ${u.email}", color = Color.LightGray, fontSize = 9.sp)
                                                            Text("• Status: ${u.status.uppercase()}", color = Color.Gray, fontSize = 9.sp)
                                                        }
                                                    }
                                                }
                                            }
                                            "audit_logs" -> {
                                                val col = fLogs.filter { it.tenantId == activeVaultTenantFilter }
                                                if (col.isEmpty()) {
                                                    Text("🚫 QUARANTINED: Empty database results under this secure boundary context.", color = Color.Red, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                                } else {
                                                    col.forEach { l ->
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .background(Color(0xFF0D0F1E))
                                                                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                                                .padding(12.dp),
                                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                                Text("Audit Log ID: ${l.id}", color = Color(0xFF38BDF8), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                                                val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(java.util.Date(l.createdAt))
                                                                Text(dateStr, color = Color.Gray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                                            }
                                                            Text("• Action Trigger: ${l.action}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                            Text("• Verified Details: ${l.details}", color = Color.LightGray, fontSize = 10.sp)
                                                            Text("• Initiator: ${l.performedBy} | Delta Diff: [${l.oldValue}] -> [${l.newValue}]", color = Color.Gray, fontSize = 9.sp)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }

    // Master Addition Dialog (With validation of unique IDs to prevent duplicate entries)
    if (showAddDialog) {
        AddMasterRecordDialog(
            tabName = selectedTab,
            onDismiss = { showAddDialog = false },
            onSubmitDirectly = { entityJson, desc ->
                // Direct Insertion (Bypass)
                when (selectedTab) {
                    "Companies" -> viewModel.addCompanyMasterDirect(entityJson as CompanyMaster)
                    "Clients" -> viewModel.addClientMasterDirect(entityJson as ClientMaster)
                    "Sites" -> viewModel.addSiteMasterDirect(entityJson as SiteMaster)
                    "Workforce" -> viewModel.addWorkforceMasterDirect(entityJson as WorkforceMaster)
                    "Assets" -> viewModel.addAssetMasterDirect(entityJson as AssetMaster)
                    "Contracts" -> viewModel.addContractMasterDirect(entityJson as ContractMaster)
                    "Rates" -> viewModel.addRateCardMasterDirect(entityJson as RateCardMaster)
                    "Payroll" -> viewModel.addPayrollMasterDirect(entityJson as PayrollMaster)
                    "Billing" -> viewModel.addBillingMasterDirect(entityJson as BillingMaster)
                    "Templates" -> viewModel.addCommunicationMasterDirect(entityJson as CommunicationMaster)
                    "Compliance" -> viewModel.addComplianceMasterDirect(entityJson as ComplianceMaster)
                }
                viewModel.showToast("Bypassed approval: Modified master dataset directly.")
                showAddDialog = false
            },
            onProposeWorkflow = { proposedJson, desc ->
                // Workflow Trigger proposal
                viewModel.submitMasterApprovalProposed(selectedTab, "Create", desc, proposedJson)
                showAddDialog = false
            }
        )
    }
}

// ==========================================
// LIST DATA ITEM ROWS WITH M3 ENTERPRISE STYLING
// ==========================================

@Composable
fun CompanyMasterRow(c: CompanyMaster) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1527)),
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(c.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFF10B981).copy(alpha = 0.2f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(c.status, color = Color(0xFF34D399), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Code: ${c.code} | Group: ${c.groupName}", color = Color(0xFF94A3B8), fontSize = 10.sp)
            Text("Headquarters: ${c.headOffice}", color = Color(0xFF64748B), fontSize = 10.sp)
            Text("Reg Tax Registry GST: ${c.taxId}", color = Color(0xFF60A5FA), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun ClientMasterRow(cl: ClientMaster) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1527)),
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(cl.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                val color = when (cl.status) {
                    "Active" -> Color(0xFF34D399)
                    "Prospect" -> Color(0xFFFBBF24)
                    else -> Color.Gray
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(color.copy(alpha = 0.2f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(cl.status, color = color, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Client Code: ${cl.code} | Sector Segment: ${cl.type} [${cl.industry}]", color = Color(0xFF60A5FA), fontSize = 10.sp, fontWeight = FontWeight.Medium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Phone: ${cl.contactPhone}", color = Color(0xFF94A3B8), fontSize = 10.sp)
                Text("GST Registry: ${cl.gst}", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
            Text("Contract Email: ${cl.contactEmail}", color = Color(0xFF94A3B8), fontSize = 10.sp)
        }
    }
}

@Composable
fun SiteMasterRow(s: SiteMaster) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1527)),
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(s.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFF34D399).copy(alpha = 0.2f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(s.status, color = Color(0xFF34D399), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Site Record Code: ${s.code} | Bound to Client: ${s.clientName}", color = Color(0xFF94A3B8), fontSize = 10.sp)
            Text("Physical Location: ${s.address}", color = Color(0xFF64748B), fontSize = 10.sp)
            Text("Coordinates: ${s.coordinates} | Shift Pattern: ${s.shiftModel}", color = Color(0xFF60A5FA), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun WorkforceMasterRow(w: WorkforceMaster) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1527)),
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(w.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFF34D399).copy(alpha = 0.2f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(w.status, color = Color(0xFF34D399), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Employee ID: ${w.empId} | System Role: ${w.role}", color = Color(0xFF60A5FA), fontSize = 10.sp)
            Text("KYC Dossier: ${w.kyc}", color = Color(0xFF94A3B8), fontSize = 10.sp)
            Text("Training Compliance: ${w.training}", color = Color(0xFF34D399), fontSize = 10.sp)
            Text("Bank Deposit Target: ${w.bankDetails} | Files: ${w.documents}", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun AssetMasterRow(a: AssetMaster) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1527)),
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Category: ${a.category}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                val color = if (a.status == "Available" || a.status == "Active") Color(0xFF34D399) else Color(0xFFF87171)
                Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(color.copy(alpha = 0.2f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(a.status, color = color, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Serial Code: ${a.serialNumber}", color = Color(0xFF60A5FA), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text("Purchase Date: ${a.purchaseDate} | Warranty Period: ${a.warranty}", color = Color(0xFF94A3B8), fontSize = 10.sp)
        }
    }
}

@Composable
fun ContractMasterRow(con: ContractMaster) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1527)),
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Contract Code: ${con.contractCode}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                val color = if (con.status == "Active") Color(0xFF34D399) else Color(0xFFFB923C)
                Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(color.copy(alpha = 0.2f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(con.status, color = color, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Affiliated Customer ID: ${con.clientCode} | Value: ${con.value}", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("Agreement Window: [${con.startDate} To ${con.endDate}]", color = Color(0xFF94A3B8), fontSize = 10.sp)
            Text("Billing Invoicing Rules: ${con.billingRules}", color = Color.Gray, fontSize = 10.sp)
        }
    }
}

@Composable
fun RateCardMasterRow(r: RateCardMaster) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1527)),
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Operational Grade: ${r.grade}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Day Pay: ${r.dayRate}", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    Text("Night Pay: ${r.nightRate}", color = Color(0xFFF1F5F9), fontSize = 10.sp)
                    Text("Overtime Pay: ${r.otRate}", color = Color(0xFFFF8A00), fontSize = 10.sp)
                }
                Column(modifier = Modifier.weight(1.2f)) {
                    Text("Guard Billing Invoice: ${r.guardBilling}", color = Color(0xFF60A5FA), fontSize = 10.sp)
                    Text("Supervisor Billing: ${r.supervisorBilling}", color = Color(0xFF60A5FA), fontSize = 10.sp)
                    Text("Relief Cover Billing: ${r.reliefBilling}", color = Color(0xFF60A5FA), fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun PayrollMasterRow(p: PayrollMaster) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1527)),
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Structure Group: ${p.structureName}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Base Salary Bracket: ${p.basicSalary}", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("PF Rule Config: ${p.pfRules}", color = Color(0xFF94A3B8), fontSize = 10.sp)
            Text("Statutory ESIC parameters: ${p.esicRules}", color = Color(0xFF94A3B8), fontSize = 10.sp)
            Text("TDS Auto Withholding Scheme: ${p.tdsRules}", color = Color(0xFF60A5FA), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun BillingMasterRow(b: BillingMaster) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1527)),
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Invoice Setup Template: ${b.templateName}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Regional GST Rules: ${b.gstRules}", color = Color(0xFF60A5FA), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text("Payment Terms Period: ${b.paymentTerms}", color = Color(0xFF94A3B8), fontSize = 10.sp)
            Text("Finance Credit Rules: ${b.creditTerms}", color = Color(0xFF34D399), fontSize = 10.sp)
        }
    }
}

@Composable
fun CommunicationMasterRow(t: CommunicationMaster) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1527)),
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Trigger event: ${t.triggerEvent}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFF60A5FA).copy(alpha = 0.2f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(t.channel.uppercase(), color = Color(0xFF60A5FA), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Template: ${t.templateName}", color = Color(0xFFFF8A00), fontSize = 10.sp, fontWeight = FontWeight.Medium)
            Text("System Outbox payload body:", color = Color.Gray, fontSize = 8.sp)
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(Color(0xFF030712)).padding(8.dp)) {
                Text(t.body, color = Color(0xFFB4F0D1), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun ComplianceMasterRow(r: ComplianceMaster) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1527)),
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(r.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFFF8A00).copy(alpha = 0.2f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(r.renewalCycle.uppercase() + " AUDIT", color = Color(0xFFFF8A00), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Statutory Reference: ${r.agreementCode} | Version check: ${r.version}", color = Color(0xFF60A5FA), fontSize = 10.sp)
            Text("Legal Guidelines index: ${r.complianceIndex}", color = Color(0xFF94A3B8), fontSize = 10.sp)
            Text("Operational SOP tied: ${r.sopRef} | Renewal expiry: ${r.expiry}", color = Color.Gray, fontSize = 10.sp)
        }
    }
}

@Composable
fun ApprovalRequestRow(
    request: ApprovalRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm UTC", Locale.US)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2F)),
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFF3B82F6).copy(alpha = 0.2f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(request.moduleName.uppercase(), color = Color(0xFF60A5FA), fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFFF8A00).copy(alpha = 0.2f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(request.actionType.uppercase(), color = Color(0xFFFF8A00), fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
                Text(
                    text = request.id,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = request.description, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(text = "Requested by ${request.requestedBy} | ${sdf.format(Date(request.requestedAt))}", color = Color(0xFF94A3B8), fontSize = 9.sp)

            Spacer(modifier = Modifier.height(10.dp))

            if (request.status == "Pending") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onReject,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.2f), contentColor = Color(0xFFF87171)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("REJECT", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("AUTHORIZE APPROVAL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val badgeColor = if (request.status == "Approved") Color(0xFF34D399) else Color(0xFFF87171)
                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(badgeColor.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(
                            text = "STATUS: ${request.status.uppercase()}",
                            color = badgeColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// CENTRAL FORM DIALOG TO PROPOSE OR INLINE ADD
// ==========================================

@Composable
fun AddMasterRecordDialog(
    tabName: String,
    onDismiss: () -> Unit,
    onSubmitDirectly: (Any, String) -> Unit,
    onProposeWorkflow: (String, String) -> Unit
) {
    // Collect standard form fields based on tab
    var text1 by remember { mutableStateOf("") }
    var text2 by remember { mutableStateOf("") }
    var text3 by remember { mutableStateOf("") }
    var text4 by remember { mutableStateOf("") }
    var text5 by remember { mutableStateOf("") }
    var text6 by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        title = {
            Text(
                "➕ CREATE NEW RECORD [$tabName]".uppercase(),
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select submission method to test either the automatic direct creation bypass or the multi-party approval state logs.", color = Color(0xFF94A3B8), fontSize = 10.sp)

                when (tabName) {
                    "Companies" -> {
                        OutlinedTextField(value = text1, onValueChange = { text1 = it }, label = { Text("Code (e.g. cmp_premium)", color = Color.Gray, fontSize = 10.sp) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = text2, onValueChange = { text2 = it }, label = { Text("Company Name", color = Color.Gray, fontSize = 10.sp) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = text3, onValueChange = { text3 = it }, label = { Text("Holdings Group Name", color = Color.Gray, fontSize = 10.sp) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = text4, onValueChange = { text4 = it }, label = { Text("Global HQ Location", color = Color.Gray, fontSize = 10.sp) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = text5, onValueChange = { text5 = it }, label = { Text("Tax GST Registration ID", color = Color.Gray, fontSize = 10.sp) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth())
                    }
                    "Clients" -> {
                        OutlinedTextField(value = text1, onValueChange = { text1 = it }, label = { Text("Client Code (e.g. CL-TEM)", color = Color.Gray, fontSize = 10.sp) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = text2, onValueChange = { text2 = it }, label = { Text("Sponsor Client Name", color = Color.Gray, fontSize = 10.sp) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = text3, onValueChange = { text3 = it }, label = { Text("Type (Corporate / Government)", color = Color.Gray, fontSize = 10.sp) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = text4, onValueChange = { text4 = it }, label = { Text("Industry sector", color = Color.Gray, fontSize = 10.sp) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = text5, onValueChange = { text5 = it }, label = { Text("GST registry | Corporate PAN", color = Color.Gray, fontSize = 10.sp) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = text6, onValueChange = { text6 = it }, label = { Text("Primary Email / Phone", color = Color.Gray, fontSize = 10.sp) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth())
                    }
                    "Sites" -> {
                        OutlinedTextField(value = text1, onValueChange = { text1 = it }, label = { Text("Site Code (ST-MOP01)", color = Color.Gray, fontSize = 10.sp) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = text2, onValueChange = { text2 = it }, label = { Text("Site Name", color = Color.Gray, fontSize = 10.sp) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = text3, onValueChange = { text3 = it }, label = { Text("Sponsor Client Name ID", color = Color.Gray, fontSize = 10.sp) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = text4, onValueChange = { text4 = it }, label = { Text("Full Location Address", color = Color.Gray, fontSize = 10.sp) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = text5, onValueChange = { text5 = it }, label = { Text("Geo Coordinates (Lat,Lng)", color = Color.Gray, fontSize = 10.sp) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth())
                    }
                    "Workforce" -> {
                        OutlinedTextField(value = text1, onValueChange = { text1 = it }, label = { Text("Employee ID (EMP-001)", color = Color.Gray, fontSize = 10.sp) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = text2, onValueChange = { text2 = it }, label = { Text("Full Staff Name", color = Color.Gray, fontSize = 10.sp) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = text3, onValueChange = { text3 = it }, label = { Text("Role (Guard / Supervisor / Mgr)", color = Color.Gray, fontSize = 10.sp) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = text4, onValueChange = { text4 = it }, label = { Text("NID Verification Registry", color = Color.Gray, fontSize = 10.sp) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = text5, onValueChange = { text5 = it }, label = { Text("Cert training modules completed", color = Color.Gray, fontSize = 10.sp) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth())
                    }
                    else -> {
                        // General fallback
                        OutlinedTextField(value = text1, onValueChange = { text1 = it }, label = { Text("Identifier Code", color = Color.Gray, fontSize = 10.sp) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = text2, onValueChange = { text2 = it }, label = { Text("Description / Primary Rule Value", color = Color.Gray, fontSize = 10.sp) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = text3, onValueChange = { text3 = it }, label = { Text("Remarks", color = Color.Gray, fontSize = 10.sp) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        // Package into workflow proposal
                        val desc = "Propose new entry in $tabName: $text2 ($text1)"
                        val pJson = "{\"code\":\"$text1\",\"name\":\"$text2\",\"type\":\"$text3\"}"
                        onProposeWorkflow(pJson, desc)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A00)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Propose", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        // Direct Admin Insertion Bypass
                        val obj = when (tabName) {
                            "Companies" -> CompanyMaster(text1.ifEmpty{"cmp_"+UUID.randomUUID().toString().take(4)}, text2.ifEmpty{"Unnamed Holding"}, text3.ifEmpty{"Holding"}, text4.ifEmpty{"Global"}, text5.ifEmpty{"GST-01"}, "2026-06", "Active")
                            "Clients" -> ClientMaster(text1.ifEmpty{"CL-"+UUID.randomUUID().toString().take(4)}, text2.ifEmpty{"Corporate Account"}, text3.ifEmpty{"Corporate"}, text4.ifEmpty{"N/A"}, text5.ifEmpty{"GST-01"}, text5.ifEmpty{"PAN-01"}, "+65 67", text6.ifEmpty{"billing@client.com"}, "Active")
                            "Sites" -> SiteMaster(text1.ifEmpty{"ST-"+UUID.randomUUID().toString().take(4)}, text2.ifEmpty{"Site Terminal"}, text3.ifEmpty{"Commercial"}, text3.ifEmpty{"Customer Parent"}, text4.ifEmpty{"SG Hub"}, "1.3, 103.8", "24/7 Security", "Corporate", "Active")
                            "Workforce" -> WorkforceMaster(text1.ifEmpty{"EMP-"+UUID.randomUUID().toString().take(4)}, text2.ifEmpty{"New Recruit"}, text3.ifEmpty{"Guard"}, text4.ifEmpty{"Verified"}, "Basic Drill", "Bank Target", "Doc.pdf", "Active")
                            "Assets" -> AssetMaster(text1.ifEmpty{"SN-"+UUID.randomUUID().toString().take(4)}, text2.ifEmpty{"Walkie Talkie"}, "2026-06", "1 Year", "Available")
                            "Contracts" -> ContractMaster(text1.ifEmpty{"CL-MOP"}, text2.ifEmpty{"CON-2026"}, "2026-01-01", "2027-12-31", "$100,000", "Net 30", "Active")
                            "Rates" -> RateCardMaster(text1.ifEmpty{"Grade X"}, "$15.00/Hr", "$18.00/Hr", "$22.00/Hr", "$24.00/Hr", "$30.00/Hr", "$28.00/Hr")
                            "Payroll" -> PayrollMaster(text1.ifEmpty{"Standard Spec"}, text2.ifEmpty{"$2,500"}, "12%", "3.25%", "Scale")
                            "Billing" -> BillingMaster(text1.ifEmpty{"Global Invoice"}, "GST 8%", "Net 30", "Limit $10k")
                            "Templates" -> CommunicationMaster("SMS", text1.ifEmpty{"Custom Alert"}, text2.ifEmpty{"Text payload"}, "Trigger Event")
                            "Compliance" -> ComplianceMaster(text1.ifEmpty{"CMP-01"}, text2.ifEmpty{"PSARA Audit Guide"}, "SOP-01", "Clause A", "V1", "2029", "Annual")
                            else -> Any()
                        }
                        onSubmitDirectly(obj, "Inserted direct master record bypass")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006B54)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Insert Direct", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray, fontSize = 11.sp)
            }
        }
    )
}
