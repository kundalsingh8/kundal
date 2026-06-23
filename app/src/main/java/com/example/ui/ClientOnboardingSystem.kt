package com.example.ui

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import java.text.SimpleDateFormat
import java.util.*

// ==========================================
// PHASE 14 COMPREHENSIVE CLIENT ONBOARDING & LIFE CYCLE SYSTEM
// ==========================================

// Client Types mandated
val CLIENT_TYPES_LATEST = listOf(
    "Residential Society",
    "Corporate",
    "Factory",
    "Warehouse",
    "Hospital",
    "School",
    "Hotel",
    "Mall",
    "Industrial",
    "Construction"
)

// Client Statuses mandated
val CLIENT_STATUSES_LATEST = listOf(
    "Lead",
    "Prospect",
    "Negotiation",
    "Active",
    "Suspended",
    "Closed"
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ClientListScreen(
    clients: List<FirestoreClient>,
    onSelectClient: (FirestoreClient) -> Unit,
    onLaunchWizard: () -> Unit,
    searchQuery: String,
    onQueryChange: (String) -> Unit
) {
    val filtered = clients.filter {
        it.clientName.contains(searchQuery, ignoreCase = true) ||
                it.clientCode.contains(searchQuery, ignoreCase = true) ||
                it.clientType.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B18))
    ) {
        // TOP CONTROLS BAR WITH ATMOSPHERIC GRADIENT
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF0F172A), Color(0xFF1E1E38))
                    )
                )
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PARTNER CLIENTS ENGINE",
                        color = Color(0xFF60A5FA),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Manage SLA Contracts, Contacts, Escalations & Life Cycle Systems",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                // + ONBOARD CLIENT TRIGGER
                Button(
                    onClick = onLaunchWizard,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006B54)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("onboard_client_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Wizard Onboard", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SEARCH INPUT FILTER BAR
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            placeholder = { Text("Search by name, unique code, or facility type...", color = Color.Gray, fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray, modifier = Modifier.size(16.dp)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("client_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF0A0F1D),
                unfocusedContainerColor = Color(0xFF0A0F1D),
                focusedBorderColor = Color(0xFF006B54),
                unfocusedBorderColor = Color(0xFF1E293B)
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        // LAZY LIST OF CLIENTS
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (filtered.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = "Empty",
                                tint = Color(0xFF334155),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Client master records found matches filter", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(filtered) { client ->
                    ClientMasterListRow(
                        client = client,
                        onClick = { onSelectClient(client) }
                    )
                }
            }
        }
    }
}

@Composable
fun ClientMasterListRow(
    client: FirestoreClient,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1527)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Title & Status Layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = client.clientName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "System ID: ${client.id} | Code: ${client.clientCode}",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Status Badge
                val statusColor = when (client.status.lowercase()) {
                    "active" -> Color(0xFF34D399)
                    "lead" -> Color(0xFF38BDF8)
                    "prospect" -> Color(0xFFFBBF24)
                    "negotiation" -> Color(0xFFC084FC)
                    "suspended" -> Color(0xFFF87171)
                    "closed" -> Color(0xFF94A3B8)
                    else -> Color.Gray
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(1.dp, statusColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = client.status.uppercase(),
                        color = statusColor,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Facility Type Info block
                Column(modifier = Modifier.weight(1f)) {
                    Text("FACILITY CATEGORY", color = Color(0xFF475569), fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
                    Text(client.clientType.uppercase(), color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Contract valuation block
                Column(modifier = Modifier.weight(1f)) {
                    Text("SLA ANNUAL VALUE", color = Color(0xFF475569), fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
                    Text(client.contractValuation ?: "Negotiation Phase", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Main Point of Contact block
                Column(modifier = Modifier.weight(1.5f)) {
                    Text("PRIMARY CONTACT", color = Color(0xFF475569), fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
                    Text(client.contactPerson, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Footer info mapping details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Email, contentDescription = "Email", tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(client.email, color = Color.Gray, fontSize = 10.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Portal", tint = if (client.portalEmail != null) Color(0xFF006B54) else Color.Gray, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (client.portalEmail != null) "SSO Map: ${client.portalEmail}" else "No Portal SSO Assigned",
                        color = if (client.portalEmail != null) Color(0xFF34D399) else Color.Gray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun ClientDetailsScreen(
    client: FirestoreClient,
    auditLogs: List<FirestoreAuditLog>,
    onBack: () -> Unit,
    onSaveClientUpdates: (FirestoreClient) -> Unit,
    viewModel: EnterpriseViewModel
) {
    var selectedSubTab by remember { mutableStateOf(1) }
    
    // Manage local screen edits safely
    var editedClient by remember(client) { mutableStateOf(client) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B18))
    ) {
        // DETAILED ACTION SCREEN TOOLBAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("back_to_list_btn")
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = editedClient.clientName,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "LIFECYCLE WORKSPACE FOR ${editedClient.clientCode}",
                        color = Color(0xFF64748B),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Save updates button!
            Button(
                onClick = {
                    onSaveClientUpdates(editedClient)
                    viewModel.showToast("Changes saved successfully to Customer Registry!")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("save_client_details_btn")
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save", tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Workspace", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // DOUBLE-ROW NAVIGATION TAB CONTROLLER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val sections = listOf(
                1 to "📋 Profile & Status",
                2 to "📞 Contacts",
                3 to "⚡ Escalation Matrix",
                4 to "📜 Contract & SLA",
                5 to "🪐 Multi-Tenant SSO",
                6 to "📝 Audit Trails"
            )
            
            // Render them split in horizontal flow or rows
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    sections.take(3).forEach { (idx, name) ->
                        val isSel = selectedSubTab == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) Color(0xFF006B54) else Color(0xFF0F172A))
                                .border(1.dp, if (isSel) Color(0xFF34D399) else Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                .clickable { selectedSubTab = idx }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(name, color = if (isSel) Color.White else Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    sections.drop(3).forEach { (idx, name) ->
                        val isSel = selectedSubTab == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) Color(0xFF006B54) else Color(0xFF0F172A))
                                .border(1.dp, if (isSel) Color(0xFF34D399) else Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                .clickable { selectedSubTab = idx }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(name, color = if (isSel) Color.White else Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SELECTED SUB-SECTION CONTAINER
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0B0F19))
                .border(2.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            when (selectedSubTab) {
                1 -> {
                    // TAB 1: PROFILE & AUDITABLE LIFECYCLE
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Text(
                            text = "LIFECYCLE STATUS TRANSITIONS",
                            color = Color(0xFF60A5FA),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        
                        // Current status display row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F172A))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ACTIVE CURRENT STATE:", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF0F766E))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(editedClient.status.uppercase(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // AUDITABLE LIFECYCLE SELECTION MATRIX
                        Column {
                            Text(
                                "TRIGGER TRANSITION MANUALLY (AUDIT LOGGED):",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val firstHalf = CLIENT_STATUSES_LATEST.take(3)
                                firstHalf.forEach { state ->
                                    val isCurrent = editedClient.status.lowercase() == state.lowercase()
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isCurrent) Color(0xFF006B54) else Color(0xFF0F172A))
                                            .clickable {
                                                if (editedClient.status != state) {
                                                    val oldVal = editedClient.status
                                                    editedClient = editedClient.copy(
                                                        status = state,
                                                        updatedAt = System.currentTimeMillis()
                                                    )
                                                    viewModel.logFirestoreAudit(
                                                        tenantId = editedClient.tenantId,
                                                        companyId = editedClient.companyId,
                                                        action = "CLIENT_STATUS_TRANSITION",
                                                        details = "State altered for partner client account: ${editedClient.clientName}",
                                                        oldValue = oldVal.uppercase(),
                                                        newValue = state.uppercase()
                                                    )
                                                    viewModel.showToast("Logged state audit: $oldVal -> $state")
                                                }
                                            }
                                            .padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(state.uppercase(), color = if (isCurrent) Color.White else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val secondHalf = CLIENT_STATUSES_LATEST.drop(3)
                                secondHalf.forEach { state ->
                                    val isCurrent = editedClient.status.lowercase() == state.lowercase()
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isCurrent) Color(0xFF006B54) else Color(0xFF0F172A))
                                            .clickable {
                                                if (editedClient.status != state) {
                                                    val oldVal = editedClient.status
                                                    editedClient = editedClient.copy(
                                                        status = state,
                                                        updatedAt = System.currentTimeMillis()
                                                    )
                                                    viewModel.logFirestoreAudit(
                                                        tenantId = editedClient.tenantId,
                                                        companyId = editedClient.companyId,
                                                        action = "CLIENT_STATUS_TRANSITION",
                                                        details = "State altered for partner client account: ${editedClient.clientName}",
                                                        oldValue = oldVal.uppercase(),
                                                        newValue = state.uppercase()
                                                    )
                                                    viewModel.showToast("Logged state audit: $oldVal -> $state")
                                                }
                                            }
                                            .padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(state.uppercase(), color = if (isCurrent) Color.White else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Basic Particular Fields
                        Text("BASIC PROFILE SETTINGS:", color = Color(0xFF60A5FA), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = editedClient.clientCode,
                                onValueChange = { editedClient = editedClient.copy(clientCode = it) },
                                label = { Text("Partner Agency Code", color = Color.Gray, fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                                ),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = editedClient.clientName,
                                onValueChange = { editedClient = editedClient.copy(clientName = it) },
                                label = { Text("Corporate Client Name", color = Color.Gray, fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                                ),
                                modifier = Modifier.weight(2.0f),
                                singleLine = true
                            )
                        }

                        // Client Type dropdown-sim
                        Column {
                            Text("Security Site Classification Sector Model:", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(Color(0xFF0F172A))
                                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                    .padding(6.dp)
                            ) {
                                items(CLIENT_TYPES_LATEST) { type ->
                                    val isMatch = editedClient.clientType.lowercase() == type.lowercase()
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isMatch) Color(0xFF006B54) else Color.Transparent)
                                            .clickable { editedClient = editedClient.copy(clientType = type) }
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(type, color = Color.White, fontSize = 11.sp)
                                        if (isMatch) Icon(Icons.Default.Check, contentDescription = "Active", tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: CONTACT PARTICULARS DIRECT EDIT
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Text(
                            text = "CLIENT POINT OF CONTACT DIRECTORY",
                            color = Color(0xFF60A5FA),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        OutlinedTextField(
                            value = editedClient.contactPerson,
                            onValueChange = { editedClient = editedClient.copy(contactPerson = it) },
                            label = { Text("Chief Contact Person Name", color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editedClient.designation,
                            onValueChange = { editedClient = editedClient.copy(designation = it) },
                            label = { Text("Designation (e.g. Procurement General, Operations VP)", color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Build, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = editedClient.mobile,
                                onValueChange = { editedClient = editedClient.copy(mobile = it) },
                                label = { Text("Direct Telephone Mobile", color = Color.Gray) },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                                ),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = editedClient.email,
                                onValueChange = { editedClient = editedClient.copy(email = it) },
                                label = { Text("Corporate Security Email", color = Color.Gray) },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                                ),
                                modifier = Modifier.weight(1.2f),
                                singleLine = true
                            )
                        }
                    }
                }

                3 -> {
                    // TAB 3: ESCALATION MATRIX DIRECT EDIT SCREEN
                    ClientEscalationScreen(
                        client = editedClient,
                        onUpdateEscalation = { updatedEscalation ->
                            editedClient = editedClient.copy(escalationMatrix = updatedEscalation)
                        }
                    )
                }

                4 -> {
                    // TAB 4: CONTRACTS AND SLA SETTINGS DIRECT SCREEN
                    ClientContractScreen(
                        client = editedClient,
                        onUpdateContract = { updatedClient ->
                            editedClient = updatedClient
                        }
                    )
                }

                5 -> {
                    // TAB 5: CLIENT PORTAL ACCOUNT SINGLE-SIGN-ON MAPPING
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Text(
                            text = "CLIENT PORTAL SSO ACCOUNT INTEGRATION",
                            color = Color(0xFF60A5FA),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = "Enable secure dashboard SSO mapping so this partner manager can access real-time patrol verification rosters, attendance logs, and exceptions directly from the Client Portal terminal.",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val checkPortalActive = editedClient.portalEmail != null
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (checkPortalActive) Color(0xFF044E3E) else Color(0xFF1E293B))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Map Portal Identity ID", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text(
                                    text = if (checkPortalActive) "Mapped to: ${editedClient.portalEmail}" else "No active SSO link established",
                                    color = if (checkPortalActive) Color(0xFFA7F3D0) else Color.LightGray,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            
                            Switch(
                                checked = checkPortalActive,
                                onCheckedChange = { activate ->
                                    val logMessage: String
                                    val targetEmail = if (activate) editedClient.email else null
                                    editedClient = editedClient.copy(portalEmail = targetEmail)
                                    logMessage = if (activate) "Activated customer single sign on for identity: ${editedClient.email}" else "Deactivated SSO portal account for client: ${editedClient.clientName}"
                                    viewModel.logFirestoreAudit(
                                        tenantId = editedClient.tenantId,
                                        companyId = editedClient.companyId,
                                        action = "CLIENT_PORTAL_SSO_MAPPED",
                                        details = logMessage,
                                        oldValue = if (activate) "Inactive" else "Active",
                                        newValue = if (activate) "SSO Map Active (${editedClient.email})" else "No Link"
                                    )
                                    viewModel.showToast(logMessage)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF34D399),
                                    checkedTrackColor = Color(0xFF006B54)
                                )
                            )
                        }

                        if (checkPortalActive) {
                            OutlinedTextField(
                                value = editedClient.portalEmail ?: "",
                                onValueChange = { editedClient = editedClient.copy(portalEmail = it) },
                                label = { Text("Custom Portal Authorization Identity Principal (SSO Email)", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }

                6 -> {
                    // TAB 6: DEDICATED AUDIT LOG HISTORY FOR THIS CLIENT
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Text(
                            text = "SECURED EVENT AUDIT LOG TRAIL FOR: " + editedClient.clientCode,
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        
                        // Check audits belonging to this client (by matching code, name, id or details)
                        val clientAudits = auditLogs.filter {
                            it.details.contains(editedClient.clientCode, ignoreCase = true) ||
                                    it.details.contains(editedClient.clientName, ignoreCase = true) ||
                                    it.action.contains("CLIENT", ignoreCase = true)
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (clientAudits.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(30.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No security audit logs found for this partner.", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            } else {
                                items(clientAudits) { audit ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F1D)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(audit.action, color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                                Text(df.format(Date(audit.createdAt)), color = Color.Gray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(audit.details, color = Color.White, fontSize = 10.sp)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row {
                                                Text("Initiated by: ${audit.performedBy}", color = Color.Gray, fontSize = 8.sp, modifier = Modifier.weight(1f))
                                                Text("Change: [${audit.oldValue}] -> [${audit.newValue}]", color = Color(0xFF34D399), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
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

// Multi-step Client Onboarding Draft Object to hold state temporarily before final Firestore submission
data class ClientOnboardingDraft(
    val clientCode: String = "",
    val clientName: String = "",
    val clientType: String = "",
    val status: String = "",
    val contactPerson: String = "",
    val mobile: String = "",
    val email: String = "",
    val designation: String = "Procurement Lead",
    val escalationLevel1: String = "Shift Duty Officer (+65 677 1234)",
    val escalationLevel2: String = "Zone Operations Commander (+65 918 5555)",
    val escalationLevel3: String = "SLA General Manager (+65 829 8888)",
    val escalationEmergency: String = "24/7 Red Alert Hotline (+65 999 1111)",
    val contractCode: String = "",
    val contractStartDate: String = "2026-06-01",
    val contractEndDate: String = "2027-05-31",
    val contractValuation: String = "$120,000 / Yr",
    val billingAddress: String = "Flat 25, Corporate Square, Singapore",
    val billingRules: String = "Standard Net 30, Due on invoice day 10",
    val gstNumber: String = "",
    val portalEmail: String = "",
    val enableSSO: Boolean = true
)

@Composable
fun ClientOnboardingWizard(
    onDismiss: () -> Unit,
    onSubmit: (FirestoreClient) -> Unit,
    viewModel: EnterpriseViewModel
) {
    var activeWizardStep by remember { mutableStateOf(1) }

    // Initialize unified temporary state draft object with random identifiers and default schemas
    val initialDraft = remember {
        ClientOnboardingDraft(
            clientCode = "CL-" + UUID.randomUUID().toString().take(4).uppercase(),
            clientType = CLIENT_TYPES_LATEST.first(),
            status = CLIENT_STATUSES_LATEST.first(),
            contractCode = "CON-" + Calendar.getInstance().get(Calendar.YEAR) + "-" + UUID.randomUUID().toString().take(3).uppercase(),
            gstNumber = "SG-GST-AUTO-" + UUID.randomUUID().toString().take(4).uppercase()
        )
    }
    
    var onboardingDraft by remember { mutableStateOf(initialDraft) }
    val totalWizardSteps = 5

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090F1C))
            .padding(16.dp)
    ) {
        // Step Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "⚙️ PARTNER MULTI-TENANT ONBOARDING ENGINE",
                    color = Color(0xFF10B981),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Provisoning Wizard • Step $activeWizardStep of $totalWizardSteps",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("onboard_wizard_close")
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress bar indicators & saved status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (step in 1..totalWizardSteps) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (step <= activeWizardStep) Color(0xFF006B54) else Color(0xFF1E293B))
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Saved", tint = Color(0xFF34D399), modifier = Modifier.size(10.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("DRAFT AUTO-SAVED", color = Color(0xFF34D399), fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Multi-Step Layout Panel
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1629)),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    when (activeWizardStep) {
                        1 -> {
                            // STEP 1: BASIC PROFILE & INITIAL LIFECYCLE
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("STEP 1: CORPORATE BOUNDARY & LIFECYCLE", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                
                                OutlinedTextField(
                                    value = onboardingDraft.clientCode,
                                    onValueChange = { onboardingDraft = onboardingDraft.copy(clientCode = it) },
                                    label = { Text("Unique SLA Account Code (Required)", color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("w_code_tf"),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = onboardingDraft.clientName,
                                    onValueChange = { onboardingDraft = onboardingDraft.copy(clientName = it) },
                                    label = { Text("Partner Agency Business Entity Name (Required)", color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("w_name_tf"),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Site Service Segment Classification Type:", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    CLIENT_TYPES_LATEST.take(5).forEach { type ->
                                        val isSel = onboardingDraft.clientType.lowercase() == type.lowercase()
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSel) Color(0xFF006B54) else Color(0xFF1E293B))
                                                .clickable { onboardingDraft = onboardingDraft.copy(clientType = type) }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(type, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                        }
                                    }
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    CLIENT_TYPES_LATEST.drop(5).forEach { type ->
                                        val isSel = onboardingDraft.clientType.lowercase() == type.lowercase()
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSel) Color(0xFF006B54) else Color(0xFF1E293B))
                                                .clickable { onboardingDraft = onboardingDraft.copy(clientType = type) }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(type, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Assign Current Auditable Onboarding Status:", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    CLIENT_STATUSES_LATEST.forEach { myst ->
                                        val isSel = onboardingDraft.status.lowercase() == myst.lowercase()
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSel) Color(0xFF006B54) else Color(0xFF1E293B))
                                                .clickable { onboardingDraft = onboardingDraft.copy(status = myst) }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(myst, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            // STEP 2: CONTACT INFORMATION
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("STEP 2: PRIMARY KEY CONTACT DIRECTORY", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                                OutlinedTextField(
                                    value = onboardingDraft.contactPerson,
                                    onValueChange = { onboardingDraft = onboardingDraft.copy(contactPerson = it) },
                                    label = { Text("Chief Contact Person (Required)", color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("w_contact_person_tf"),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = onboardingDraft.designation,
                                    onValueChange = { onboardingDraft = onboardingDraft.copy(designation = it) },
                                    label = { Text("Role Designation (e.g. VPS Procurement)", color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("w_designation_tf"),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = onboardingDraft.mobile,
                                    onValueChange = { onboardingDraft = onboardingDraft.copy(mobile = it) },
                                    label = { Text("Direct Handheld Telephone Mobile", color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("w_mobile_tf"),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = onboardingDraft.email,
                                    onValueChange = { onboardingDraft = onboardingDraft.copy(email = it) },
                                    label = { Text("Corporate Security Account Email Address", color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("w_email_tf"),
                                    singleLine = true
                                )
                            }
                        }

                        3 -> {
                            // STEP 3: ESCALATION PATHS
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("STEP 3: OUTLINE EXQUISITE ESCALATION MATRIX DIRECTORY", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                                OutlinedTextField(
                                    value = onboardingDraft.escalationLevel1,
                                    onValueChange = { onboardingDraft = onboardingDraft.copy(escalationLevel1 = it) },
                                    label = { Text("Tier Level 1 Contact (Sentry Desk)", color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("w_l1_tf"),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = onboardingDraft.escalationLevel2,
                                    onValueChange = { onboardingDraft = onboardingDraft.copy(escalationLevel2 = it) },
                                    label = { Text("Tier Level 2 Contact (Zone Supervisor)", color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("w_l2_tf"),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = onboardingDraft.escalationLevel3,
                                    onValueChange = { onboardingDraft = onboardingDraft.copy(escalationLevel3 = it) },
                                    label = { Text("Tier Level 3 Contact (Operations General)", color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("w_l3_tf"),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = onboardingDraft.escalationEmergency,
                                    onValueChange = { onboardingDraft = onboardingDraft.copy(escalationEmergency = it) },
                                    label = { Text("24/7 Red Alarm Command Hotline Dispatch (SLA Essential)", color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("w_emergency_tf"),
                                    singleLine = true
                                )
                            }
                        }

                        4 -> {
                            // STEP 4: SLA CONTRACT DETAILS
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("STEP 4: CONTRACT & SLA BOUNDARY PARTICULARS", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = onboardingDraft.contractCode,
                                        onValueChange = { onboardingDraft = onboardingDraft.copy(contractCode = it) },
                                        label = { Text("Contract Registry ID", color = Color.Gray, fontSize = 11.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                            focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                                        ),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = onboardingDraft.contractValuation,
                                        onValueChange = { onboardingDraft = onboardingDraft.copy(contractValuation = it) },
                                        label = { Text("Annual Contract SLA Valuation", color = Color.Gray, fontSize = 11.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                            focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                                        ),
                                        modifier = Modifier.weight(1.2f),
                                        singleLine = true
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = onboardingDraft.contractStartDate,
                                        onValueChange = { onboardingDraft = onboardingDraft.copy(contractStartDate = it) },
                                        label = { Text("SLA Start Date", color = Color.Gray, fontSize = 11.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                            focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                                        ),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = onboardingDraft.contractEndDate,
                                        onValueChange = { onboardingDraft = onboardingDraft.copy(contractEndDate = it) },
                                        label = { Text("SLA End Date", color = Color.Gray, fontSize = 11.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                            focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                                        ),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }

                                OutlinedTextField(
                                    value = onboardingDraft.billingAddress,
                                    onValueChange = { onboardingDraft = onboardingDraft.copy(billingAddress = it) },
                                    label = { Text("Headquarters Corporate Invoice Address", color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("w_billing_address_tf")
                                )

                                OutlinedTextField(
                                    value = onboardingDraft.billingRules,
                                    onValueChange = { onboardingDraft = onboardingDraft.copy(billingRules = it) },
                                    label = { Text("Invoicing Schedules & Penalty Rules SLA", color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("w_billing_rules_tf")
                                )

                                OutlinedTextField(
                                    value = onboardingDraft.gstNumber,
                                    onValueChange = { onboardingDraft = onboardingDraft.copy(gstNumber = it) },
                                    label = { Text("Regional Tax/GST Business ID (Auto-Generated)", color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("w_gst_number_tf"),
                                    singleLine = true
                                )
                            }
                        }

                        5 -> {
                            // STEP 5: CLIENT PORTAL ACCESS SETUP
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("STEP 5: CLIENT PORTAL Single Sign-On IDENTITY", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                                Text(
                                    text = "Configure direct secure portal accounts linked by corporate email mappings to unlock independent dashboard analytics.",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0F172A))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Authorize Immediate SSO Sign-On Link", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text("If enabled, portal accounts are mapped using contact email", color = Color.Gray, fontSize = 10.sp)
                                    }
                                    Switch(
                                        checked = onboardingDraft.enableSSO,
                                        onCheckedChange = { onboardingDraft = onboardingDraft.copy(enableSSO = it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color(0xFF34D399),
                                            checkedTrackColor = Color(0xFF006B54)
                                        )
                                    )
                                }

                                if (onboardingDraft.enableSSO) {
                                    val mappedPortalEmail = if (onboardingDraft.portalEmail.isEmpty()) onboardingDraft.email else onboardingDraft.portalEmail
                                    OutlinedTextField(
                                        value = mappedPortalEmail,
                                        onValueChange = { onboardingDraft = onboardingDraft.copy(portalEmail = it) },
                                        label = { Text("Principal Portal Account Username Key Email (Required)", color = Color.Gray) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                            focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                                        ),
                                        modifier = Modifier.fillMaxWidth().testTag("w_portal_email_tf"),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Navigation actions buttons (Back / Next / Save)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    if (activeWizardStep > 1) activeWizardStep-- else onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("wizard_back_btn")
            ) {
                Text(if (activeWizardStep > 1) "Previous" else "Cancel", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    if (activeWizardStep < totalWizardSteps) {
                        // Validate fields to proceed
                        if (activeWizardStep == 1 && onboardingDraft.clientName.trim().isEmpty()) {
                            viewModel.showToast("Please input corporate business name to continue.")
                        } else if (activeWizardStep == 2 && onboardingDraft.contactPerson.trim().isEmpty()) {
                            viewModel.showToast("Please designate primary contact officer.")
                        } else {
                            activeWizardStep++
                        }
                    } else {
                        // COMPLETE PROVISIONING SYSTEM NOW!
                        if (onboardingDraft.clientName.trim().isEmpty()) {
                            viewModel.showToast("Partner name is missing.")
                            return@Button
                        }
                        
                        val newClientRecord = FirestoreClient(
                            id = "cl_prov_" + UUID.randomUUID().toString().take(6),
                            tenantId = "tenant_singh_sec", // simulated logged tenant identity securely
                            companyId = "cmp_centurion_sec",
                            clientCode = onboardingDraft.clientCode.ifEmpty { "CODE-AUTO" },
                            clientName = onboardingDraft.clientName,
                            clientType = onboardingDraft.clientType.lowercase(),
                            contactPerson = onboardingDraft.contactPerson,
                            mobile = onboardingDraft.mobile,
                            email = onboardingDraft.email,
                            designation = onboardingDraft.designation,
                            escalationMatrix = FirestoreEscalationMatrix(
                                level1 = onboardingDraft.escalationLevel1,
                                level2 = onboardingDraft.escalationLevel2,
                                level3 = onboardingDraft.escalationLevel3,
                                emergency = onboardingDraft.escalationEmergency
                            ),
                            billingAddress = onboardingDraft.billingAddress,
                            gstNumber = onboardingDraft.gstNumber,
                            status = onboardingDraft.status.lowercase(),
                            contractCode = onboardingDraft.contractCode,
                            contractStartDate = onboardingDraft.contractStartDate,
                            contractEndDate = onboardingDraft.contractEndDate,
                            contractValuation = onboardingDraft.contractValuation,
                            billingRules = onboardingDraft.billingRules,
                            portalEmail = if (onboardingDraft.enableSSO) {
                                if (onboardingDraft.portalEmail.isEmpty()) onboardingDraft.email else onboardingDraft.portalEmail
                            } else null,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        onSubmit(newClientRecord)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006B54)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("wizard_next_btn")
            ) {
                Text(if (activeWizardStep < totalWizardSteps) "Continue Next" else "Complete Profile Provisioning", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ClientContractScreen(
    client: FirestoreClient,
    onUpdateContract: (FirestoreClient) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "CONTRACT SLA BOUNDARIES & BILLING RULES",
            color = Color(0xFF60A5FA),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = client.contractCode ?: "",
                onValueChange = { onUpdateContract(client.copy(contractCode = it)) },
                label = { Text("Active Contract Code ID", color = Color.Gray, fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                ),
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            OutlinedTextField(
                value = client.contractValuation ?: "",
                onValueChange = { onUpdateContract(client.copy(contractValuation = it)) },
                label = { Text("Contract SLA Valuation (e.g. $12M / Yr)", color = Color.Gray, fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                ),
                modifier = Modifier.weight(1.2f),
                singleLine = true
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = client.contractStartDate ?: "",
                onValueChange = { onUpdateContract(client.copy(contractStartDate = it)) },
                label = { Text("Contract Active Start Date", color = Color.Gray, fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                ),
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            OutlinedTextField(
                value = client.contractEndDate ?: "",
                onValueChange = { onUpdateContract(client.copy(contractEndDate = it)) },
                label = { Text("Contract Expiration Date", color = Color.Gray, fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
                ),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        OutlinedTextField(
            value = client.billingAddress ?: "",
            onValueChange = { onUpdateContract(client.copy(billingAddress = it)) },
            label = { Text("Corporate Legal Invoice Billing Address", color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = client.billingRules ?: "",
            onValueChange = { onUpdateContract(client.copy(billingRules = it)) },
            label = { Text("Invoice Billing Schedules & Credit Limit Constraints", color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = client.gstNumber ?: "",
            onValueChange = { onUpdateContract(client.copy(gstNumber = it)) },
            label = { Text("Regional Tax / GST Verification ID", color = Color.Gray, fontSize = 11.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
fun ClientEscalationScreen(
    client: FirestoreClient,
    onUpdateEscalation: (FirestoreEscalationMatrix) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "CLIENT RESPONSIVE EMERGENCY ESCALATION PATHS",
            color = Color(0xFFEF4444),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        OutlinedTextField(
            value = client.escalationMatrix.level1 ?: "",
            onValueChange = { onUpdateEscalation(client.escalationMatrix.copy(level1 = it)) },
            label = { Text("Escalation Tier 1: Sentry Shift Duty Desk (Minutes Response)", color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = client.escalationMatrix.level2 ?: "",
            onValueChange = { onUpdateEscalation(client.escalationMatrix.copy(level2 = it)) },
            label = { Text("Escalation Tier 2: Field Supervisor Zone Commander", color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = client.escalationMatrix.level3 ?: "",
            onValueChange = { onUpdateEscalation(client.escalationMatrix.copy(level3 = it)) },
            label = { Text("Escalation Tier 3: Chief Operations Director Desk", color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = client.escalationMatrix.emergency ?: "",
            onValueChange = { onUpdateEscalation(client.escalationMatrix.copy(emergency = it)) },
            label = { Text("24/7 Red Alarm Command Hotline Dispatch (SLA Essential)", color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF040812), unfocusedContainerColor = Color(0xFF040812)
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}
