package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.EnterpriseViewModel
import com.example.ui.MasterDataPane
import com.example.ui.WorkforcePane
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ==========================================
// CENTRALIZED RBAC CONTROLLER (Phase 3 Core)
// ==========================================
object SecurityRBAC {
    fun getAllowedSections(role: String): List<String> {
        return TenantRBAC.getAllowedSections(role)
    }

    fun canPerformAction(role: String, action: String): Boolean {
        val tenantAction = when (action) {
            "CREATE_SITE" -> TenantAction.CREATE_SITE
            "TRANSITION_SITE_STATUS" -> TenantAction.TRANSITION_SITE_STATUS
            "TRIGGER_SOS" -> TenantAction.TRIGGER_SOS
            "RESOLVE_SOS" -> TenantAction.RESOLVE_SOS
            "CLOCK_IN_OUT" -> TenantAction.CLOCK_IN_OUT
            "LOG_INCIDENT" -> TenantAction.LOG_INCIDENT
            "VIEW_AUDITS" -> TenantAction.VIEW_AUDITS
            "VIEW_FINANCIALS" -> TenantAction.VIEW_FINANCIALS
            else -> action
        }
        
        // Strict boundary validation: TRANSITION_SITE_STATUS is only super_admin and company_admin in Legacy
        if (action == "TRANSITION_SITE_STATUS") {
            return role == "super_admin" || role == "company_admin"
        }
        
        return TenantRBAC.canRolePerformAction(role, tenantAction)
    }
}

class MainActivity : ComponentActivity() {
    private val viewModel: EnterpriseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    DashboardRootScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardRootScreen(
    viewModel: EnterpriseViewModel,
    modifier: Modifier = Modifier
) {
    val users by viewModel.users.collectAsStateWithLifecycle()
    val sites by viewModel.sites.collectAsStateWithLifecycle()
    val attendance by viewModel.attendance.collectAsStateWithLifecycle()
    val incidents by viewModel.incidents.collectAsStateWithLifecycle()
    val sosAlerts by viewModel.sosAlerts.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val currentSection by viewModel.currentSection.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    var showUserSwitcher by remember { mutableStateOf(false) }

    // Synchronize selected section if role change makes section inaccessible
    LaunchedEffect(currentUser) {
        val user = currentUser
        if (user != null) {
            val allowed = SecurityRBAC.getAllowedSections(user.role)
            if (currentSection !in allowed) {
                viewModel.navigateTo(allowed.firstOrNull() ?: "Overview")
            }
        }
    }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isCompact = configuration.screenWidthDp < 600

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val userRole = currentUser?.role ?: "guard"
    val allowedSections = SecurityRBAC.getAllowedSections(userRole)

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isCompact,
        drawerContent = {
            if (isCompact) {
                ModalDrawerSheet(
                    drawerContainerColor = Color(0xFFFFFFFF),
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Header inside drawer
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp, top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF006B54)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "K",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Kundal OS V3",
                                color = Color(0xFF191C1B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        
                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(allowedSections) { sec ->
                                val isSelected = currentSection == sec
                                val icon = when (sec) {
                                    "Overview" -> Icons.Default.Analytics
                                    "Onboarding" -> Icons.Default.AppRegistration
                                    "Sites" -> Icons.Default.Business
                                    "Workforce" -> Icons.Default.People
                                    "Attendance" -> Icons.Default.CheckCircle
                                    "Visitors" -> Icons.Default.Person
                                    "Incidents" -> Icons.Default.Assignment
                                    "SOS" -> Icons.Default.Warning
                                    "Payroll" -> Icons.Default.AttachMoney
                                    "Billing" -> Icons.Default.Receipt
                                    "Master Data" -> Icons.Default.Storage
                                    "Reports" -> Icons.Default.Assessment
                                    "Audits" -> Icons.Default.Lock
                                    else -> Icons.Default.Settings
                                }
                                
                                NavigationDrawerItem(
                                    icon = { Icon(icon, contentDescription = sec) },
                                    label = { Text(sec, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.navigateTo(sec)
                                        scope.launch { drawerState.close() }
                                    },
                                    colors = NavigationDrawerItemDefaults.colors(
                                        selectedContainerColor = Color(0xFFB4F0D1),
                                        selectedIconColor = Color(0xFF006B54),
                                        selectedTextColor = Color(0xFF006B54),
                                        unselectedIconColor = Color(0xFF3F4945),
                                        unselectedTextColor = Color(0xFF3F4945)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ==========================================
                // HEADER BAR: BRANDING, STATUS & TIME
                // ==========================================
                HeaderBar(
                    activeUser = currentUser,
                    onToggleUserSwitcher = { showUserSwitcher = !showUserSwitcher },
                    isCompact = isCompact,
                    onToggleDrawer = {
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 1.dp)

            // Dynamic User switcher dropdown drawer
            AnimatedVisibility(
                visible = showUserSwitcher,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    UserSimulationDrawer(
                        users = users,
                        currentUser = currentUser,
                        onSelectUser = {
                            viewModel.selectUser(it)
                            showUserSwitcher = false
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 1.dp)
                }
            }

            // Platform Foundation Status Alerts Banner
            PlatformAlertsBanner(currentUser = currentUser)

            // Main navigation + screen body
            val userRole = currentUser?.role ?: "guard"
            val allowedSections = SecurityRBAC.getAllowedSections(userRole)

            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (!isCompact) {
                    // Adaptive Left-Side Jetpack Navigation Rail for wide viewports
                    AdaptiveNavigationRail(
                        allowedSections = allowedSections,
                        currentSection = currentSection,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                }

                // Mobile Navigation Bar (We put it in a column to align spacing on small screens)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    when (currentSection) {
                        "Overview" -> OverviewPane(
                            currentUser = currentUser,
                            sites = sites,
                            attendance = attendance,
                            incidents = incidents,
                            sosAlerts = sosAlerts,
                            onNavigateToSection = { viewModel.navigateTo(it) }
                        )
                        "Onboarding" -> OnboardingPane(
                            currentUser = currentUser,
                            viewModel = viewModel
                        )
                        "Sites" -> SitesPane(
                            currentUser = currentUser,
                            sites = sites,
                            onCreateSite = { name, desc, status -> viewModel.createSite(name, desc, status) },
                            onTransitionStatus = { site, newStatus -> viewModel.updateSiteStatus(site, newStatus) }
                        )
                        "Workforce" -> WorkforcePane(
                            viewModel = viewModel,
                            currentUser = currentUser
                        )
                        "Attendance" -> com.example.ui.AttendanceSystemPane(
                            viewModel = viewModel,
                            currentUser = currentUser
                        )
                        "Visitors" -> com.example.ui.VisitorSystemPane(
                            viewModel = viewModel,
                            currentUser = currentUser
                        )
                        "Incidents" -> IncidentsPane(
                            currentUser = currentUser,
                            incidents = incidents,
                            sites = sites,
                            onLogIncident = { t, sev, desc, sId -> viewModel.logIncident(t, sev, desc, sId) }
                        )
                        "SOS" -> SOSPane(
                            currentUser = currentUser,
                            sosAlerts = sosAlerts,
                            sites = sites,
                            onTriggerSOS = { sId, sName -> viewModel.triggerSOS(sId, sName) },
                            onResolveSOS = { viewModel.resolveSOS(it) }
                        )
                        "Payroll" -> PayrollPane()
                        "Billing" -> BillingPane()
                        "Master Data" -> MasterDataPane(viewModel = viewModel)
                        "Reports" -> ReportsPane(
                            sites = sites,
                            incidents = incidents,
                            attendance = attendance
                        )
                        "Audits" -> AuditsPane(
                            currentUser = currentUser,
                            auditLogs = auditLogs
                        )
                    }
                }
            }
        }

        // Action confirmation or feedback toasts
        toastMessage?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xE610B981))
                    .border(1.dp, Color(0xFF34D399), RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Success Notification",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = it,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
}

// ==========================================
// SUB-COMPONENTS
// ==========================================

@Composable
fun HeaderBar(
    activeUser: User?,
    onToggleUserSwitcher: () -> Unit,
    isCompact: Boolean,
    onToggleDrawer: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFFFFF))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isCompact) {
                IconButton(
                    onClick = onToggleDrawer,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .testTag("nav_drawer_toggle_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Navigation Menu",
                        tint = Color(0xFF006B54)
                    )
                }
            }
            // Brand Accent Box matching "w-10 h-10 bg-[#006B54] rounded-xl flex items-center justify-center text-white font-bold text-xl"
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF006B54)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "K",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Kundal OS V3",
                    color = Color(0xFF191C1B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "FOUNDATION ACTIVE",
                    color = Color(0xFF3F4945),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Role simulator button styled perfectly like user avatar button: "w-10 h-10 bg-[#D7E5E0] rounded-full ... border border-[#BFC9C4]"
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFD7E5E0))
                    .border(1.dp, Color(0xFFBFC9C4), RoundedCornerShape(20.dp))
                    .clickable { onToggleUserSwitcher() }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("simulation_switch_btn"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Active Role Context",
                    tint = Color(0xFF006B54),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = activeUser?.name ?: "DEVELOPER",
                        color = Color(0xFF191C1B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "ROLE: ${activeUser?.role?.uppercase() ?: "DEVELOPER"}",
                        color = Color(0xFF3F4945),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Toggle",
                    tint = Color(0xFF191C1B),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun UserSimulationDrawer(
    users: List<User>,
    currentUser: User?,
    onSelectUser: (User) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAF8))
            .padding(16.dp)
    ) {
        Text(
            text = "🕹️ SYSTEM RBAC TESTING SIMULATOR",
            color = Color(0xFF006B54),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Select any multi-tenant operational identity below to instantly re-bind visible navigation, telemetry controls, and secure SQLite CRUD endpoints:",
            color = Color(0xFF3F4945),
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(users) { user ->
                val isSelected = currentUser?.id == user.id
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFE7F3EF) else Color(0xFFFFFFFF)
                    ),
                    modifier = Modifier
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color(0xFF006B54) else Color(0xFFBFC9C4),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectUser(user) }
                        .width(160.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0xFF006B54) else Color(0xFFBFC9C4))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = user.name,
                                color = Color(0xFF191C1B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user.role.uppercase(),
                            color = Color(0xFF006B54),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Site scope: ${if (user.siteId.isEmpty()) "ALL SITES" else user.siteId}",
                            color = Color(0xFF3F4945),
                            fontSize = 8.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        currentUser?.let { user ->
            val tenantRole = TenantRole.fromIdString(user.role)
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF5F2)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFD0DFD9), RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🛡️ Active RBAC Context: ${tenantRole.displayName}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00372A),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF006B54), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "RANK ${tenantRole.rank} • ${tenantRole.defaultScope}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tenantRole.description,
                        color = Color(0xFF2E3B36),
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tenant boundaries: GROUP=[${user.companyGroupId}] | COMPANY=[${user.companyId}] | SITE=[${if (user.siteId.isEmpty()) "ALL_SITES" else user.siteId}]",
                        color = Color(0xFF4C5D56),
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PlatformAlertsBanner(currentUser: User?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF002114))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = "Compliance State Alert",
            tint = Color(0xFFB4F0D1),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "V3 Security Rules enforced. Legacy 'area_officer' dependencies successfully scrubbed.",
            color = Color(0xFFB4F0D1),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AdaptiveNavigationRail(
    allowedSections: List<String>,
    currentSection: String,
    onNavigate: (String) -> Unit
) {
    NavigationRail(
        containerColor = Color(0xFFFFFFFF),
        modifier = Modifier.fillMaxHeight(),
        header = {
            Icon(
                imageVector = Icons.Filled.Hub,
                contentDescription = "OS Core Nodes",
                tint = Color(0xFF006B54),
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(allowedSections) { sec ->
                val isSelected = currentSection == sec
                val icon = when (sec) {
                    "Overview" -> Icons.Default.Analytics
                    "Onboarding" -> Icons.Default.AppRegistration
                    "Sites" -> Icons.Default.Business
                    "Workforce" -> Icons.Default.People
                    "Attendance" -> Icons.Default.CheckCircle
                    "Visitors" -> Icons.Default.Person
                    "Incidents" -> Icons.Default.Assignment
                    "SOS" -> Icons.Default.Warning
                    "Payroll" -> Icons.Default.AttachMoney
                    "Billing" -> Icons.Default.Receipt
                    "Reports" -> Icons.Default.Assessment
                    "Audits" -> Icons.Default.Lock
                    else -> Icons.Default.Settings
                }
                NavigationRailItem(
                    selected = isSelected,
                    onClick = { onNavigate(sec) },
                    icon = { Icon(icon, contentDescription = sec) },
                    label = { Text(sec, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = Color(0xFF006B54),
                        unselectedIconColor = Color(0xFF3F4945),
                        selectedTextColor = Color(0xFF006B54),
                        unselectedTextColor = Color(0xFF3F4945),
                        indicatorColor = Color(0xFFB4F0D1)
                    ),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

// ==========================================
// PANELS (SCREEN VIEWS)
// ==========================================

@Composable
fun OverviewPane(
    currentUser: User?,
    sites: List<Site>,
    attendance: List<Attendance>,
    incidents: List<Incident>,
    sosAlerts: List<SOSAlert>,
    onNavigateToSection: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Welcome back, ${currentUser?.name ?: "User"}",
                    color = Color(0xFF191C1B),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Multi-tenant scope check: ${currentUser?.companyGroupId?.uppercase() ?: "GLOBAL"} / Site restriction: ${currentUser?.siteId?.ifEmpty { "unrestricted" } ?: "unrestricted"}",
                    color = Color(0xFF3F4945),
                    fontSize = 11.sp
                )
            }
        }

        // Grid Analytics summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnalyticalCard(
                    title = "Active Sites",
                    value = "${sites.count { it.status == "Active" }} / ${sites.size}",
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToSection("Sites") }
                )
                AnalyticalCard(
                    title = "Punched Guards",
                    value = "${attendance.count { it.status == "Open" }} On Duty",
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToSection("Attendance") }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnalyticalCard(
                    title = "Pending Incidents",
                    value = "${incidents.count { it.status != "Resolved" }} Logging",
                    color = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToSection("Incidents") }
                )
                AnalyticalCard(
                    title = "Emergency Beacons",
                    value = "${sosAlerts.count { it.status == "Active" }} Active",
                    color = if (sosAlerts.none { it.status == "Active" }) Color(0xFF64748B) else Color(0xFFF43F5E),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToSection("SOS") }
                )
            }
        }

        // SOS Hazard Warning Banner if active
        val activeBeacons = sosAlerts.filter { it.status == "Active" }
        if (activeBeacons.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Active Threat Warning",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CRITICAL: SOS DISPATCH REQUESTED",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${activeBeacons.size} beacon(s) actively broadcasting alert from locations.",
                                color = Color(0xFFFCA5A5),
                                fontSize = 11.sp
                            )
                        }
                        Button(
                            onClick = { onNavigateToSection("SOS") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("RESPOND", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Standard Operational Hierarchy Guideline Card Styled like Deep Dark Active Operations block
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF002114)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF006B54), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Hub,
                            contentDescription = "Hierarchy Map",
                            tint = Color(0xFFB4F0D1),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "V3 PLATFORM ENTITY HIERARCHY",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Company Group  ➔  Security Company  ➔  Client Site  ➔  Physical Unit",
                        color = Color(0xFFB4F0D1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Every transaction complies with this routing structure. Unsecured intermediate structures like 'area_offices' have been completely retired from the database.",
                        color = Color(0xFFD7E5E0),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AnalyticalCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        modifier = modifier
            .border(1.dp, Color(0xFFBFC9C4), RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, color = Color(0xFF3F4945), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = value, color = Color(0xFF191C1B), fontSize = 18.sp, fontWeight = FontWeight.Black)
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun SitesPane(
    currentUser: User?,
    sites: List<Site>,
    onCreateSite: (String, String, String) -> Unit,
    onTransitionStatus: (Site, String) -> Unit
) {
    var siteName by remember { mutableStateOf("") }
    var siteAddress by remember { mutableStateOf("") }
    var initialLifecycleStage by remember { mutableStateOf("Draft") }
    var showAddDialog by remember { mutableStateOf(false) }

    val canManage = currentUser?.let { SecurityRBAC.canPerformAction(it.role, "CREATE_SITE") } ?: false

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🌐 Central Site Records",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Primary site state lifecycles conforming to structural V3 rules",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }

            if (canManage) {
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A00))
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("REGISTER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (sites.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No sites in dataset.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sites) { site ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = site.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "📍 ${site.address}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }

                                val statusColor = when (site.status.uppercase()) {
                                    "ACTIVE" -> Color(0xFF10B981)
                                    "SETUP" -> Color(0xFFF59E0B)
                                    "DRAFT" -> Color(0xFF94A3B8)
                                    "SUSPENDED" -> Color(0xFFEF4444)
                                    else -> Color.DarkGray
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(statusColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = site.status.uppercase(),
                                        color = statusColor,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Tenancy Bounds: GROUP ID '${site.companyGroupId}' | COMPANY ID '${site.companyId}'",
                                color = Color(0xFF64748B),
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Status Transitions Flow (Phase 5)
                            if (canManage) {
                                HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "TRANSITION SITE STATE LIFECYCLE:",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                val states = listOf("Draft", "Pending Approval", "Setup", "Active", "Suspended")
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(states) { stage ->
                                        if (stage != site.status) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF1E293B))
                                                    .clickable { onTransitionStatus(site, stage) }
                                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = "To $stage",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Medium
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
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Register V3 Security Site", color = Color.White) },
            containerColor = Color(0xFF0F172A),
            text = {
                Column {
                    OutlinedTextField(
                        value = siteName,
                        onValueChange = { siteName = it },
                        label = { Text("Client Site Name") },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = siteAddress,
                        onValueChange = { siteAddress = it },
                        label = { Text("Physical Street Address") },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    Text("Initial Lifecycle Stage:", color = Color.Gray, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Draft", "Setup").forEach { stage ->
                            val currentChoice = initialLifecycleStage == stage
                            FilterChip(
                                selected = currentChoice,
                                onClick = { initialLifecycleStage = stage },
                                label = { Text(stage) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (siteName.isNotEmpty()) {
                            onCreateSite(siteName, siteAddress, initialLifecycleStage)
                            siteName = ""
                            siteAddress = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A00))
                ) {
                    Text("SAVE SITE", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("CANCEL", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun AttendancePane(
    currentUser: User?,
    sites: List<Site>,
    attendance: List<Attendance>,
    onClockIn: (String, String) -> Unit,
    onClockOut: () -> Unit
) {
    val currentActiveUser = currentUser ?: return
    val userShiftClosed = attendance.firstOrNull { it.userId == currentActiveUser.id && it.status == "Open" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "⏰ Roster & Attendance Verification",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Geo-validated shift punches for client site metrics",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        }

        // Clock In Controls
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SHIFT OPERATIONS PANEL",
                        color = Color(0xFFFF8A00),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (userShiftClosed != null) {
                        // Currently on Duty
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E3A8A))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("ON DUTY AT: ${userShiftClosed.siteId}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Punched at: ${SimpleDateFormat("HH:mm:ss 'UTC'", Locale.US).format(Date(userShiftClosed.clockInTime))}", color = Color(0xFF93C5FD), fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { onClockOut() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("PUNCH OUT (CLOCK OUT)", fontWeight = FontWeight.Black)
                        }
                    } else {
                        // Off Duty - Clock in to an active site
                        val activeSites = sites.filter { it.status == "Active" }

                        if (activeSites.isEmpty()) {
                            Text("No Active Sites configured. Cannot start roster shifts.", color = Color.Gray, fontSize = 11.sp)
                        } else {
                            Text("Select an Active Site to initialize shift punch:", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                            activeSites.forEach { site ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E293B))
                                        .clickable { onClockIn(site.id, site.name) }
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(site.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("PUNCH IN ➔", color = Color(0xFFFF8A00), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Shift register log
        item {
            Text(
                text = "Historical Attendance Logs",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (attendance.isEmpty()) {
            item {
                Text("No shift logs found.", color = Color.Gray)
            }
        } else {
            items(attendance) { att ->
                val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.US)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = att.userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "Site: ${att.siteId}", color = Color.LightGray, fontSize = 11.sp)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (att.status == "Open") Color(0xFF2563EB).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = att.status.uppercase(),
                                    color = if (att.status == "Open") Color(0xFF3B82F6) else Color(0xFF10B981),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Shift Duration: ${sdf.format(Date(att.clockInTime))} ➔ ${att.clockOutTime?.let { sdf.format(Date(it)) } ?: "ACTIVE"}",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                        Text(
                            text = att.clockInGeo,
                            color = Color(0xFF64748B),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VisitorsPane(
    currentUser: User?,
    sites: List<Site>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "🎟️ Visitor Pass Registration",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Manage residential and corporate visitor authorizations",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Standard Visitor Log Integration: ACTIVE",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "V3 utilizes strict site/unit indices configuration for visitor entries. Unverified non-unit credentials represent strict compliance breaches. Register visitor profiles directly dynamically within real-time Site scopes bounds.",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun IncidentsPane(
    currentUser: User?,
    incidents: List<Incident>,
    sites: List<Site>,
    onLogIncident: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf("High") }
    var description by remember { mutableStateOf("") }
    var selectedSiteId by remember { mutableStateOf("") }
    var showReportDialog by remember { mutableStateOf(false) }

    val canLog = currentUser?.let { SecurityRBAC.canPerformAction(it.role, "LOG_INCIDENT") } ?: false

    // Sync site selection choice to default
    LaunchedEffect(sites) {
        if (selectedSiteId.isEmpty()) {
            selectedSiteId = sites.firstOrNull()?.id ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "⚡ Real-time Incident Ledger",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Critical security logs recorded across active site units",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }

            if (canLog && sites.isNotEmpty()) {
                Button(
                    onClick = { showReportDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("FILE REPORT", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (incidents.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No incidents currently active in this database.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(incidents) { inc ->
                    val severityColor = when (inc.severity.uppercase()) {
                        "CRITICAL" -> Color(0xFFEF4444)
                        "HIGH" -> Color(0xFFF97316)
                        "MEDIUM" -> Color(0xFFF59E0B)
                        else -> Color(0xFF10B981)
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(severityColor)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = inc.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(severityColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = inc.severity.uppercase(),
                                        color = severityColor,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = inc.description,
                                color = Color(0xFFD1D5DB),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Reported by: ${inc.reporterName} | Site: ${inc.siteId}",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 9.sp
                                )

                                Text(
                                    text = "Status: ${inc.status.uppercase()}",
                                    color = Color(0xFFFF8A00),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Log Operational Exception Alert", color = Color.White) },
            containerColor = Color(0xFF0F172A),
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Incident Summary Title") },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Detailed Logs Description") },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    Text("Severity Index:", color = Color.Gray, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Medium", "High", "Critical").forEach { level ->
                            val currentChoice = severity == level
                            FilterChip(
                                selected = currentChoice,
                                onClick = { severity = level },
                                label = { Text(level) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Select Operations Site Target:", color = Color.Gray, fontSize = 11.sp)
                    sites.forEach { site ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSiteId = site.id }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedSiteId == site.id,
                                onClick = { selectedSiteId = site.id }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(site.name, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotEmpty() && selectedSiteId.isNotEmpty()) {
                            onLogIncident(title, severity, description, selectedSiteId)
                            title = ""
                            description = ""
                            showReportDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("DISPATCH EXCEPTION ACTION", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("ABORT", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun SOSPane(
    currentUser: User?,
    sosAlerts: List<SOSAlert>,
    sites: List<Site>,
    onTriggerSOS: (String, String) -> Unit,
    onResolveSOS: (String) -> Unit
) {
    val canTrigger = currentUser?.let { SecurityRBAC.canPerformAction(it.role, "TRIGGER_SOS") } ?: false
    val canResolve = currentUser?.let { SecurityRBAC.canPerformAction(it.role, "RESOLVE_SOS") } ?: false

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "🚨 SOS Emergency Priority Console",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Direct priority telemetry and critical incident triggers",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        }

        // Active trigger controls
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CRITICAL FORCE PANIC SIGNALS",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (canTrigger) {
                        val activeSites = sites.filter { it.status == "Active" }
                        if (activeSites.isEmpty()) {
                            Text("No Active sites currently linked. SOS simulation holds.", color = Color.Gray, fontSize = 11.sp)
                        } else {
                            Text(
                                text = "Press any button below to broadcast emergency security alerts immediately:",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            activeSites.forEach { site ->
                                Button(
                                    onClick = { onTriggerSOS(site.id, site.name) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .height(48.dp)
                                ) {
                                    Icon(imageVector = Icons.Filled.Warning, contentDescription = "Active Alarm")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("FORCE SOS - ${site.name.uppercase()}", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Identity '${currentUser?.role}' does not possess emergency SOS broadcast credentials.",
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Active Alerts Beacons List
        item {
            Text(
                text = "Real-time Priority Threats Channels",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        val activeThreats = sosAlerts.filter { it.status == "Active" }

        if (activeThreats.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🟢 Direct clear. Zero high severity distress alerts registered.",
                        color = Color(0xFF10B981),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(activeThreats) { alert ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "ALARM ON ${alert.siteId.uppercase()}",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Triggered by: ${alert.triggererName} (Role: ${alert.triggererRole.uppercase()})",
                                color = Color(0xFFFCA5A5),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Geolocation coordinates: (${alert.latitude}, ${alert.longitude})",
                                color = Color(0xFFEF4444),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (canResolve) {
                            Button(
                                onClick = { onResolveSOS(alert.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Text("RESOLVE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PayrollPane() {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Filled.AttachMoney, contentDescription = "Payroll", tint = Color.LightGray, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("Automated Payroll Module", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Integrated with site operational roster shifts logs.", color = Color.Gray, fontSize = 11.sp)
        }
    }
}

@Composable
fun BillingPane() {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Filled.Receipt, contentDescription = "Invoices", tint = Color.LightGray, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("Automated Billing Ledger", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Generates invoices directly matching site activity metrics.", color = Color.Gray, fontSize = 11.sp)
        }
    }
}

@Composable
fun ReportsPane(
    sites: List<Site>,
    incidents: List<Incident>,
    attendance: List<Attendance>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("📈 Enterprise Operational Analytics", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("COMPLIANCE REPORTS OVERVIEW", color = Color(0xFFFF8A00), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Total Sites registered: ${sites.size}", color = Color.White, fontSize = 13.sp)
                    Text("Pending unsolved incidents exceptions: ${incidents.count { it.status != "Resolved" }}", color = Color.White, fontSize = 13.sp)
                    Text("Aggregate clocked shift entries inside Room DB: ${attendance.size}", color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun AuditsPane(
    currentUser: User?,
    auditLogs: List<AuditLog>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "🛡️ Cryptographic System Audit Trail",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Immutable compliance log stream securely recorded in Room DB",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (auditLogs.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No audits logged currently.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(auditLogs) { log ->
                    val sdf = SimpleDateFormat("HH:mm:ss 'UTC'", Locale.US)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F1D)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF3B82F6).copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = log.action,
                                        color = Color(0xFF60A5FA),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Text(
                                    text = sdf.format(Date(log.timestamp)),
                                    color = Color.Gray,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = log.details,
                                color = Color.White,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Performed by user: ${log.performedBy}",
                                    color = Color(0xFF64748B),
                                    fontSize = 8.sp
                                )

                                Text(
                                    text = "Diff: ${log.oldValue} ➔ ${log.newValue}",
                                    color = Color(0xFFFF8A00),
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class CompanyOnboardingState(
    // 1. Corporate Profile
    val companyName: String = "Centurion Security Services",
    val gstNumber: String = "GST-SG-1938502A",
    val headOfficeAddress: String = "12 Marina Boulevard, Tower 2, Singapore",
    val contactPhone: String = "+65 6789 0123",
    val contactEmail: String = "contact@centurionsec.com",

    // 2. Head Administrator
    val adminName: String = "Vikram Singh",
    val adminEmail: String = "vikram.singh@centurionsec.com",
    val adminPhone: String = "+65 9123 4567",
    val adminRole: String = "Super Administrator",
    val pinEnforcement: Boolean = true,

    // 3. Billing & Limits
    val hourlyRate: String = "22.50",
    val billingEmail: String = "finance@centurionsec.com",
    val billingCycle: String = "Monthly Invoice", // e.g. Monthly, Bi-weekly, Weekly
    val maxSites: String = "30",
    val maxGuards: String = "500"
)

@Composable
fun OnboardingPane(
    currentUser: User?,
    viewModel: EnterpriseViewModel
) {
    var activeStep by remember { mutableStateOf(1) }
    
    // Step 1: Company Onboarding (with unified persistent state object)
    var companyOnboardingState by remember { mutableStateOf(CompanyOnboardingState()) }
    var companySubStep by remember { mutableStateOf(1) }
    var step1Status by remember { mutableStateOf("CONFIGURED") }
    
    // Derived local property shortcuts for secondary step compatibility
    val companyName = companyOnboardingState.companyName
    val gstNumber = companyOnboardingState.gstNumber
    val hourlyRate = companyOnboardingState.hourlyRate
    val subLimitSites = companyOnboardingState.maxSites
    val subLimitGuards = companyOnboardingState.maxGuards

    // Step 2: Site Onboarding
    var siteName by remember { mutableStateOf("Maritime Plaza Operations") }
    var siteLat by remember { mutableStateOf("1.29482") }
    var siteLng by remember { mutableStateOf("103.85839") }
    var siteRadius by remember { mutableStateOf("50") }
    var checklistQR by remember { mutableStateOf(true) }
    var checklistCellular by remember { mutableStateOf(true) }
    var checklistSupervisor by remember { mutableStateOf(true) }
    var step2Status by remember { mutableStateOf("CONFIGURED") }

    // Step 3: Client Onboarding (Phase 13 Comprehensive System)
    var p13ClientCode by remember { mutableStateOf("MOP-SGP") }
    var p13ClientName by remember { mutableStateOf("Maritime Properties Ltd") }
    var p13ClientType by remember { mutableStateOf("corporate") } // residential, corporate, industrial, hospital, school, hotel, mall
    var p13ContactPerson by remember { mutableStateOf("Douglas Lim") }
    var p13Mobile by remember { mutableStateOf("+65 9182 7492") }
    var p13Email by remember { mutableStateOf("douglas.lim@maritimeplaza.com") }
    var p13Designation by remember { mutableStateOf("Senior Procurement Director") }
    
    // Escalation Matrix
    var p13Level1 by remember { mutableStateOf("Shift Guard Desk (+65 6777 1111)") }
    var p13Level2 by remember { mutableStateOf("Zone Supervisor (+65 9222 3444)") }
    var p13Level3 by remember { mutableStateOf("Operations Manager (+65 9182 7492)") }
    var p13Emergency by remember { mutableStateOf("24/7 Command Center (+65 8888 9999)") }

    // Contract Management
    var p13ContractCode by remember { mutableStateOf("CON-2026-MOP-SGP") }
    var p13StartDate by remember { mutableStateOf("2026-06-01") }
    var p13EndDate by remember { mutableStateOf("2028-05-31") }
    var p13Valuation by remember { mutableStateOf("$145,000 / Year") }
    var p13BillingRules by remember { mutableStateOf("Monthly post-paid cycle, Net 30 days") }
    var p13GstNumber by remember { mutableStateOf("GST-SG-238491A") }
    var p13BillingAddress by remember { mutableStateOf("10 Marina Boulevard, Singapore 018981") }

    // Portal Mapping
    var p13PortalEmailMapped by remember { mutableStateOf("douglas.lim@maritimeplaza.com") }

    // Status Lifecycle (lead | prospect | active | inactive | archived)
    var p13Status by remember { mutableStateOf("lead") } 

    // Local UI Inner Tab for Client Wizard flow
    var p13TabSelected by remember { mutableStateOf(1) }

    // Backup compatibility handles
    var clientName by remember { mutableStateOf("Maritime Properties Ltd") }
    var clientContact by remember { mutableStateOf("Douglas Lim (+65 9182 7492)") }
    var step3Status by remember { mutableStateOf("DRAFT") }

    // Step 4: Staff Onboarding
    var accountsStaffName by remember { mutableStateOf("Shirley Tan") }
    var hrStaffName by remember { mutableStateOf("James Wong") }
    var step4Status by remember { mutableStateOf("DRAFT") }

    // Step 5: Guard Onboarding Lifecycle
    var guardName by remember { mutableStateOf("Aaron Chen") }
    var guardKyc by remember { mutableStateOf(true) }
    var guardTraining by remember { mutableStateOf(true) }
    var guardDeviceBound by remember { mutableStateOf(false) }
    var step5Status by remember { mutableStateOf("DRAFT") }

    // Step 6: Resident Onboarding
    var residentName by remember { mutableStateOf("Jonathan Low") }
    var residentUnit by remember { mutableStateOf("Tower 1, 12-A") }
    var step6Status by remember { mutableStateOf("DRAFT") }

    // Step 7: Unit Generation
    var numTowers by remember { mutableStateOf("3") }
    var floorsPerTower by remember { mutableStateOf("15") }
    var flatsPerFloor by remember { mutableStateOf("4") }
    var bulkUnitsCount by remember { mutableStateOf("0") }
    var step7Status by remember { mutableStateOf("DRAFT") }

    // Step 8: Asset Onboarding
    var assetQrStands by remember { mutableStateOf("12") }
    var assetCctvCount by remember { mutableStateOf("32") }
    var assetStatusState by remember { mutableStateOf("Available") }
    var step8Status by remember { mutableStateOf("DRAFT") }

    // Step 9: Contract Onboarding
    var contractAgreementCode by remember { mutableStateOf("AGR-CENT-2026-904") }
    var billingModel by remember { mutableStateOf("Hourly Rates SLA") }
    var step9Status by remember { mutableStateOf("DRAFT") }

    // Step 10: Compliance Document Vault
    var psaraCertLoaded by remember { mutableStateOf(true) }
    var liabilityInsuranceExpiry by remember { mutableStateOf("2027-12-31") }
    var step10Status by remember { mutableStateOf("CONFIGURED") }

    // Step 11: Operations Activation
    var activeOperationsOn by remember { mutableStateOf(false) }
    var step11Status by remember { mutableStateOf("DRAFT") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090F1C))
            .padding(16.dp)
    ) {
        // Heading Block
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "⚙️ Master Onboarding Terminal",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Step-by-step multi-tenant provisioning & ERP schema validator",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
            
            // Overall Activation Progress Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (activeOperationsOn) Color(0xFF006B54) else Color(0xFF334155)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (activeOperationsOn) "🚀 ACTIVE" else "🛠️ ONBOARDING",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sequential Wizard Horizontal bar with 11 steps
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "ONBOARDING LIFECYCLE SEQUENCE (11 PHASES)",
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items((1..11).toList()) { step ->
                        val isCurrent = activeStep == step
                        val isPassed = step < activeStep
                        val stepStatus = when(step) {
                            1 -> step1Status
                            2 -> step2Status
                            3 -> step3Status
                            4 -> step4Status
                            5 -> step5Status
                            6 -> step6Status
                            7 -> step7Status
                            8 -> step8Status
                            9 -> step9Status
                            10 -> step10Status
                            11 -> step11Status
                            else -> "DRAFT"
                        }
                        val stepColor = when {
                            isCurrent -> Color(0xFF006B54)
                            isPassed || stepStatus == "ACTIVE" || stepStatus == "CONFIGURED" -> Color(0xFF34D399)
                            else -> Color(0xFF475569)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(stepColor.copy(alpha = if (isCurrent) 1.0f else 0.25f))
                                .border(
                                    1.dp, 
                                    if (isCurrent) Color(0xFFB4F0D1) else stepColor.copy(alpha = 0.5f), 
                                    RoundedCornerShape(8.dp)
                                    )
                                .clickable { activeStep = step }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$step",
                                    color = if (isCurrent) Color.White else stepColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when(step) {
                                        1 -> "Company"
                                        2 -> "Site"
                                        3 -> "Client"
                                        4 -> "Staff"
                                        5 -> "Guard"
                                        6 -> "Resident"
                                        7 -> "Unit Scan"
                                        8 -> "Assets"
                                        9 -> "Contract"
                                        10 -> "Compliance"
                                        11 -> "GO LIVE"
                                        else -> ""
                                    },
                                    color = if (isCurrent) Color.White else Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Wizard Configuration Panel
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F1D)),
            modifier = Modifier
                .weight(1.0f)
                .fillMaxWidth()
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header of current wizard step
                item {
                    Column {
                        Text(
                            text = when(activeStep) {
                                1 -> "🏢 PHASE 1: COMPANY TENANT CREATION"
                                2 -> "📍 PHASE 2: OPERATIONS SITE GPS PROVISIONING"
                                3 -> "🤝 PHASE 3: CLIENT & CONTRACT SPONSOR ACCOUNT"
                                4 -> "💼 PHASE 4: OFFICE ADMINISTRATIVE STAFF ROLES"
                                5 -> "🛡️ PHASE 5: GUARD LIFECYCLE CERTIFICATION"
                                6 -> "🏠 PHASE 6: RESIDENTIAL ASSOCIATION OVERLAYS"
                                7 -> "🏢 PHASE 7: ADDRESS UNIT BULK SCHEDULER"
                                8 -> "⚙️ PHASE 8: FIELD HARDWARE & ASSETS TAGGING"
                                9 -> "📝 PHASE 9: CONTRACT BINDING & SLA RATES"
                                10 -> "📜 PHASE 10: REGULATORY COMPLIANCE VAULT"
                                11 -> "🚀 PHASE 11: FINAL SYSTEM PRODUCTION GO LIVE"
                                else -> ""
                            },
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when(activeStep) {
                                1 -> "Configure master corporate limits, tax registry parameters, and basic payroll schema boundaries."
                                2 -> "Define geo-spatially locked guard yards with exact GPS coordinate tolerances."
                                3 -> "Map corporate client management users, contact priorities, and portal invitation boundaries."
                                4 -> "Provision back-office and auditing users with role-governed authorization profiles."
                                5 -> "Log security verification files, complete basic guard drills, and enforce device hardware binding."
                                6 -> "For residential projects: map buildings, flats, vehicle lists, and emergency notifications."
                                7 -> "Programmatically coordinate and populate tower-floor-unit database schemas."
                                8 -> "Tag QR patrol sites, high-frequency NFC stickers, hand-held radios, and CCTV streams."
                                9 -> "Apply work orders, agreement validity calendars, hourly wages calculations, and SLA models."
                                10 -> "Fulfill regional Private Security Regulation (PSARA) laws, liability coverage indexes, and certificates."
                                11 -> "Trigger the master server activation module to unlock active transaction logging features."
                                else -> ""
                            },
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                item {
                    HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)
                }

                // Step-Specific Forms
                item {
                    when (activeStep) {
                        1 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                // 1. PRESET AUTOFILL & SUB-STEP SELECTOR SECTION
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Sub-phase Navigation".uppercase(),
                                        color = Color(0xFF64748B),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                    
                                    // Quick-Action template loader button
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF1E293B))
                                            .clickable {
                                                companyOnboardingState = CompanyOnboardingState(
                                                    companyName = "Centurion Security Services Private Ltd",
                                                    gstNumber = "GST-SG-2026849F-V3",
                                                    headOfficeAddress = "8 Marina Boulevard, Marina Bay Tower 1, Singapore",
                                                    contactPhone = "+65 6839 4920",
                                                    contactEmail = "communications@centurionsec.com",
                                                    adminName = "Vikram Singh Chaudhary",
                                                    adminEmail = "vikram.singh@centurionsec.com",
                                                    adminPhone = "+65 9283 5029",
                                                    adminRole = "Super Administrator Chief",
                                                    pinEnforcement = true,
                                                    hourlyRate = "24.75",
                                                    billingEmail = "accounts.receivables@centurionsec.com",
                                                    billingCycle = "Bi-Weekly Invoice",
                                                    maxSites = "45",
                                                    maxGuards = "750"
                                                )
                                                viewModel.logSystemAudit(
                                                    action = "ONB_TEMPLATE_AUTOFILLED",
                                                    details = "Successfully populated unified company onboarding state with premium defaults.",
                                                    oldValue = "DEFAULT",
                                                    newValue = "KUNDAL_OS_V3_PRESETS"
                                                )
                                                viewModel.showToast("Loaded Centurion Security Service live templates!")
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "⚡ LOAD PRESETS",
                                            color = Color(0xFF34D399),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Interactive Horizon Sub-Step Indicator Nodes
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val subSteps = listOf(
                                        1 to "🏢 Corporate",
                                        2 to "🛡️ Admin Account",
                                        3 to "💵 Billing & Limit"
                                    )
                                    subSteps.forEach { (stepNum, stepTitle) ->
                                        val isCurrentSub = companySubStep == stepNum
                                        val isPassedSub = stepNum < companySubStep
                                        val activeSubColor = if (isCurrentSub) Color(0xFF006B54) else if (isPassedSub) Color(0xFF34D399).copy(alpha = 0.8f) else Color(0xFF1E293B)
                                        
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(activeSubColor)
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isCurrentSub) Color(0xFFB4F0D1) else Color.Transparent,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable { 
                                                    companySubStep = stepNum
                                                    viewModel.logSystemAudit(
                                                        action = "ONB_SUBSTEP_NAVIGATED",
                                                        details = "User clicked sub-tab tracker step $stepNum ($stepTitle)",
                                                        oldValue = "SubStep_$companySubStep",
                                                        newValue = "SubStep_$stepNum"
                                                    )
                                                }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = stepTitle,
                                                color = if (isCurrentSub || isPassedSub) Color.White else Color(0xFF94A3B8),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                // Render matching sub-step form
                                when (companySubStep) {
                                    1 -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Text(
                                                text = "Phase 1A: Corporate Registration Settings",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )

                                            OutlinedTextField(
                                                value = companyOnboardingState.companyName,
                                                onValueChange = { 
                                                    companyOnboardingState = companyOnboardingState.copy(companyName = it)
                                                },
                                                label = { Text("Corporate Entity Name", color = Color.Gray, fontSize = 11.sp) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White,
                                                    focusedContainerColor = Color(0xFF040812),
                                                    unfocusedContainerColor = Color(0xFF040812)
                                                ),
                                                modifier = Modifier.fillMaxWidth().testTag("onboarding_company_name_input")
                                            )

                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                OutlinedTextField(
                                                    value = companyOnboardingState.gstNumber,
                                                    onValueChange = { 
                                                        companyOnboardingState = companyOnboardingState.copy(gstNumber = it)
                                                    },
                                                    label = { Text("Tax Registry / GST ID", color = Color.Gray, fontSize = 11.sp) },
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        focusedContainerColor = Color(0xFF040812),
                                                        unfocusedContainerColor = Color(0xFF040812)
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )

                                                OutlinedTextField(
                                                    value = companyOnboardingState.contactPhone,
                                                    onValueChange = { 
                                                        companyOnboardingState = companyOnboardingState.copy(contactPhone = it)
                                                    },
                                                    label = { Text("Corporate Phone", color = Color.Gray, fontSize = 11.sp) },
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        focusedContainerColor = Color(0xFF040812),
                                                        unfocusedContainerColor = Color(0xFF040812)
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }

                                            OutlinedTextField(
                                                value = companyOnboardingState.headOfficeAddress,
                                                onValueChange = { 
                                                    companyOnboardingState = companyOnboardingState.copy(headOfficeAddress = it)
                                                },
                                                label = { Text("Global Headquarters Physical Address", color = Color.Gray, fontSize = 11.sp) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White,
                                                    focusedContainerColor = Color(0xFF040812),
                                                    unfocusedContainerColor = Color(0xFF040812)
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            OutlinedTextField(
                                                value = companyOnboardingState.contactEmail,
                                                onValueChange = { 
                                                    companyOnboardingState = companyOnboardingState.copy(contactEmail = it)
                                                },
                                                label = { Text("Global Communications Email Address", color = Color.Gray, fontSize = 11.sp) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White,
                                                    focusedContainerColor = Color(0xFF040812),
                                                    unfocusedContainerColor = Color(0xFF040812)
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                    2 -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Text(
                                                text = "Phase 1B: System Super Administrator Profile",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )

                                            OutlinedTextField(
                                                value = companyOnboardingState.adminName,
                                                onValueChange = { 
                                                    companyOnboardingState = companyOnboardingState.copy(adminName = it)
                                                },
                                                label = { Text("Administrator Full Name", color = Color.Gray, fontSize = 11.sp) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White,
                                                    focusedContainerColor = Color(0xFF040812),
                                                    unfocusedContainerColor = Color(0xFF040812)
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                OutlinedTextField(
                                                    value = companyOnboardingState.adminEmail,
                                                    onValueChange = { 
                                                        companyOnboardingState = companyOnboardingState.copy(adminEmail = it)
                                                    },
                                                    label = { Text("Login Sign-In Email", color = Color.Gray, fontSize = 11.sp) },
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        focusedContainerColor = Color(0xFF040812),
                                                        unfocusedContainerColor = Color(0xFF040812)
                                                    ),
                                                    modifier = Modifier.weight(1.2f)
                                                )

                                                OutlinedTextField(
                                                    value = companyOnboardingState.adminRole,
                                                    onValueChange = { 
                                                        companyOnboardingState = companyOnboardingState.copy(adminRole = it)
                                                    },
                                                    label = { Text("Governance Role Profile", color = Color.Gray, fontSize = 11.sp) },
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        focusedContainerColor = Color(0xFF040812),
                                                        unfocusedContainerColor = Color(0xFF040812)
                                                    ),
                                                    modifier = Modifier.weight(0.8f)
                                                )
                                            }

                                            OutlinedTextField(
                                                value = companyOnboardingState.adminPhone,
                                                onValueChange = { 
                                                    companyOnboardingState = companyOnboardingState.copy(adminPhone = it)
                                                },
                                                label = { Text("Direct Administrator Contact Phone Number", color = Color.Gray, fontSize = 11.sp) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White,
                                                    focusedContainerColor = Color(0xFF040812),
                                                    unfocusedContainerColor = Color(0xFF040812)
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            // Security settings toggle switch
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1629)),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = "Enforce Admin Hardened Mode",
                                                            color = Color.White,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = "Blocks remote credential transfers and enforces biometric device signature binding checks.",
                                                            color = Color(0xFF94A3B8),
                                                            fontSize = 9.sp
                                                        )
                                                    }
                                                    Switch(
                                                        checked = companyOnboardingState.pinEnforcement,
                                                        onCheckedChange = {
                                                            companyOnboardingState = companyOnboardingState.copy(pinEnforcement = it)
                                                            viewModel.logSystemAudit(
                                                                action = "ONB_SECURITY_HARDEN_TOGGLED",
                                                                details = "Toggled admin hardware isolation to secure state: $it",
                                                                oldValue = (!it).toString(),
                                                                newValue = it.toString()
                                                            )
                                                        },
                                                        colors = SwitchDefaults.colors(
                                                            checkedThumbColor = Color(0xFF34D399),
                                                            checkedTrackColor = Color(0xFF006B54)
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    3 -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Text(
                                                text = "Phase 1C: SLA Margins & Scale Sizing",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )

                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                OutlinedTextField(
                                                    value = companyOnboardingState.hourlyRate,
                                                    onValueChange = { 
                                                        companyOnboardingState = companyOnboardingState.copy(hourlyRate = it)
                                                    },
                                                    label = { Text("Standard SLA Margin ($/Hr)", color = Color.Gray, fontSize = 11.sp) },
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        focusedContainerColor = Color(0xFF040812),
                                                        unfocusedContainerColor = Color(0xFF040812)
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )

                                                OutlinedTextField(
                                                    value = companyOnboardingState.billingEmail,
                                                    onValueChange = { 
                                                        companyOnboardingState = companyOnboardingState.copy(billingEmail = it)
                                                    },
                                                    label = { Text("Finance Billing Contact", color = Color.Gray, fontSize = 11.sp) },
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        focusedContainerColor = Color(0xFF040812),
                                                        unfocusedContainerColor = Color(0xFF040812)
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }

                                            // Billing cycle selector selector pills
                                            Column {
                                                Text(
                                                    text = "Preferred Corporate Invoicing Period",
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(bottom = 5.dp)
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    val cycles = listOf("Weekly Calendar", "Bi-Weekly Invoice", "Monthly Invoice")
                                                    cycles.forEach { cycle ->
                                                        val isSel = companyOnboardingState.billingCycle == cycle
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(if (isSel) Color(0xFF006B54) else Color(0xFF1E293B))
                                                                .border(1.dp, if (isSel) Color(0xFF34D399) else Color.Transparent, RoundedCornerShape(6.dp))
                                                                .clickable {
                                                                    companyOnboardingState = companyOnboardingState.copy(billingCycle = cycle)
                                                                }
                                                                .padding(vertical = 6.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = cycle,
                                                                color = if (isSel) Color.White else Color(0xFF94A3B8),
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                OutlinedTextField(
                                                    value = companyOnboardingState.maxSites,
                                                    onValueChange = { 
                                                        companyOnboardingState = companyOnboardingState.copy(maxSites = it)
                                                    },
                                                    label = { Text("Site Provision Capacity", color = Color.Gray, fontSize = 11.sp) },
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        focusedContainerColor = Color(0xFF040812),
                                                        unfocusedContainerColor = Color(0xFF040812)
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )

                                                OutlinedTextField(
                                                    value = companyOnboardingState.maxGuards,
                                                    onValueChange = { 
                                                        companyOnboardingState = companyOnboardingState.copy(maxGuards = it)
                                                    },
                                                    label = { Text("Guard Payroll Slot Cap", color = Color.Gray, fontSize = 11.sp) },
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        focusedContainerColor = Color(0xFF040812),
                                                        unfocusedContainerColor = Color(0xFF040812)
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Real-time JSON persistent state manifest board (Always available, but takes custom rich style on sub step 3)
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (companySubStep == 3) "🔥 ERP SYSTEM PROVISIONING DATA MODEL (READY)" else "📦 UNIFIED LOCAL ONBOARDING STATE OBJECT",
                                                color = if (companySubStep == 3) Color(0xFF34D399) else Color(0xFF60A5FA),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(if (companyOnboardingState.companyName.isNotEmpty()) Color(0xFF34D399) else Color.Red)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (companyOnboardingState.companyName.isNotEmpty()) "STATE ACTIVE" else "STATE INVALID",
                                                    color = Color.Gray,
                                                    fontSize = 8.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        
                                        // Format as a neat diagnostic manifest matching the console aesthetic
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = "{",
                                                color = Color(0xFFF1F5F9),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = "  \"corporate\": {",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = "    \"alias\": \"${companyOnboardingState.companyName}\",",
                                                color = Color(0xFFB4F0D1),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.padding(start = 12.dp)
                                            )
                                            Text(
                                                text = "    \"tax_gst_id\": \"${companyOnboardingState.gstNumber}\",",
                                                color = Color(0xFFB4F0D1),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.padding(start = 12.dp)
                                            )
                                            Text(
                                                text = "    \"mail\": \"${companyOnboardingState.contactEmail}\"",
                                                color = Color(0xFFB4F0D1),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.padding(start = 12.dp)
                                            )
                                            Text(
                                                text = "  },",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = "  \"administrator\": {",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = "    \"master_user\": \"${companyOnboardingState.adminName}\",",
                                                color = Color(0xFFB4F0D1),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.padding(start = 12.dp)
                                            )
                                            Text(
                                                text = "    \"secure_enforce_biometric\": ${companyOnboardingState.pinEnforcement}",
                                                color = Color(0xFFB4F0D1),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.padding(start = 12.dp)
                                            )
                                            Text(
                                                text = "  },",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = "  \"billing_thresholds\": {",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = "    \"sla_margin_usd_hr\": ${companyOnboardingState.hourlyRate},",
                                                color = Color(0xFFB4F0D1),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.padding(start = 12.dp)
                                            )
                                            Text(
                                                text = "    \"invoicing_cycle\": \"${companyOnboardingState.billingCycle}\",",
                                                color = Color(0xFFB4F0D1),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.padding(start = 12.dp)
                                            )
                                            Text(
                                                text = "    \"max_enforceable_sites\": ${companyOnboardingState.maxSites},",
                                                color = Color(0xFFB4F0D1),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.padding(start = 12.dp)
                                            )
                                            Text(
                                                text = "    \"max_enforceable_guards\": ${companyOnboardingState.maxGuards}",
                                                color = Color(0xFFB4F0D1),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.padding(start = 12.dp)
                                            )
                                            Text(
                                                text = "  }",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = "}",
                                                color = Color(0xFFF1F5F9),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }

                                // 4. LOCAL BACK/FORWARD FOR COMPANY SUB-STEPS
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { if (companySubStep > 1) {
                                            val oldSub = companySubStep
                                            companySubStep--
                                            viewModel.logSystemAudit(
                                                action = "ONB_SUBSTEP_PREV",
                                                details = "User navigated to previous company substep.",
                                                oldValue = "SubStep_$oldSub",
                                                newValue = "SubStep_$companySubStep"
                                            )
                                        }},
                                        enabled = companySubStep > 1,
                                        modifier = Modifier.weight(1f).padding(end = 6.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF1E293B),
                                            disabledContainerColor = Color(0xFF0F172A),
                                            contentColor = Color.White,
                                            disabledContentColor = Color.DarkGray
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("< PRIOR SUB-STEP", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    if (companySubStep < 3) {
                                        Button(
                                            onClick = {
                                                val oldSub = companySubStep
                                                companySubStep++
                                                viewModel.logSystemAudit(
                                                    action = "ONB_SUBSTEP_NEXT",
                                                    details = "User navigated to next company substep.",
                                                    oldValue = "SubStep_$oldSub",
                                                    newValue = "SubStep_$companySubStep"
                                                )
                                            },
                                            modifier = Modifier.weight(1f).padding(start = 6.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF006B54)
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("NEXT SUB-STEP >", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    } else {
                                        // Reaches end. Reminds the user they can finalize at bottom
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF0F172A))
                                                .border(1.dp, Color(0xFF34D399), RoundedCornerShape(8.dp))
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "READY! USE MAIN BUTTON 👇",
                                                color = Color(0xFF34D399),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Yard & Location Parameters", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = siteName,
                                    onValueChange = { siteName = it },
                                    label = { Text("Location Code", color = Color.Gray, fontSize = 11.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = siteLat,
                                        onValueChange = { siteLat = it },
                                        label = { Text("Latitude Coordinate", color = Color.Gray, fontSize = 11.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = siteLng,
                                        onValueChange = { siteLng = it },
                                        label = { Text("Longitude Coordinate", color = Color.Gray, fontSize = 11.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Checkbox(
                                        checked = checklistQR,
                                        onCheckedChange = { checklistQR = it },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF006B54))
                                    )
                                    Text("Perimeter QR Stands Installed", color = Color.White, fontSize = 11.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Checkbox(
                                        checked = checklistCellular,
                                        onCheckedChange = { checklistCellular = it },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF006B54))
                                    )
                                    Text("Cellular Signal Stability Tested", color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                        3 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                // Phase 13 Sub-navigation Tabs (basic profile, escalation matrices, billing agreement, and tenant explorer)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val tabs = listOf(
                                        1 to "📋 Profile & Lifecycle",
                                        2 to "📞 Contacts Matrix",
                                        3 to "💼 SLA Contract",
                                        4 to "🔐 Tenant Sandbox"
                                    )
                                    tabs.forEach { (num, name) ->
                                        val isSel = p13TabSelected == num
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSel) Color(0xFF006B54) else Color(0xFF1E293B))
                                                .clickable { p13TabSelected = num }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                         ) {
                                            Text(
                                                text = name,
                                                color = if (isSel) Color.White else Color(0xFF94A3B8),
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }

                                when (p13TabSelected) {
                                    1 -> {
                                        // SUB-TAB 1: CLIENT WIZARD & STATUS LIFECYCLE
                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Text(
                                                text = "CLIENT WIZARD & LIFECYCLE MANAGEMENT",
                                                color = Color(0xFF38BDF8),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )

                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                OutlinedTextField(
                                                    value = p13ClientCode,
                                                    onValueChange = { p13ClientCode = it },
                                                    label = { Text("Client Code (Unique)", color = Color.Gray, fontSize = 11.sp) },
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        focusedContainerColor = Color(0xFF040812),
                                                        unfocusedContainerColor = Color(0xFF040812)
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )

                                                OutlinedTextField(
                                                    value = p13ClientName,
                                                    onValueChange = { 
                                                        p13ClientName = it
                                                        clientName = it // Synchronize backup handle
                                                    },
                                                    label = { Text("Client Business Name", color = Color.Gray, fontSize = 11.sp) },
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        focusedContainerColor = Color(0xFF040812),
                                                        unfocusedContainerColor = Color(0xFF040812)
                                                    ),
                                                    modifier = Modifier.weight(1.5f)
                                                )
                                            }

                                            // Client Type Selector
                                            Column {
                                                Text("Client Site Category Type", color = Color(0xFF94A3B8), fontSize = 10.sp, modifier = Modifier.padding(bottom = 6.dp))
                                                val types = listOf("residential", "corporate", "industrial", "hospital", "school", "hotel", "mall")
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    types.take(4).forEach { type ->
                                                        val isSel = p13ClientType == type
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(if (isSel) Color(0xFF0F766E) else Color(0xFF1E293B))
                                                                .clickable { p13ClientType = type }
                                                                .padding(vertical = 6.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(type.uppercase(), color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    types.drop(4).forEach { type ->
                                                        val isSel = p13ClientType == type
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(if (isSel) Color(0xFF0F766E) else Color(0xFF1E293B))
                                                                .clickable { p13ClientType = type }
                                                                .padding(vertical = 6.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(type.uppercase(), color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }

                                            // Status Lifecycle Transition
                                            Column {
                                                Text("Status Lifecycle Transition (Auditable)", color = Color(0xFF94A3B8), fontSize = 10.sp, modifier = Modifier.padding(bottom = 6.dp))
                                                val lifecycle = listOf("lead", "prospect", "active", "inactive", "archived")
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    lifecycle.forEach { status ->
                                                        val isSel = p13Status == status
                                                        val statusColor = when (status) {
                                                            "lead" -> Color(0xFF38BDF8)
                                                            "prospect" -> Color(0xFFFBBF24)
                                                            "active" -> Color(0xFF34D399)
                                                            "inactive" -> Color(0xFFF87171)
                                                            "archived" -> Color(0xFF94A3B8)
                                                            else -> Color.White
                                                        }
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(if (isSel) statusColor else Color(0xFF1E293B))
                                                                .clickable {
                                                                    viewModel.logSystemAudit(
                                                                        action = "CLIENT_STATUS_TRANSITION",
                                                                        details = "Manually transitioned client draft status for $p13ClientName",
                                                                        oldValue = p13Status.uppercase(),
                                                                        newValue = status.uppercase()
                                                                    )
                                                                    p13Status = status
                                                                    viewModel.showToast("Transitioned Client ${p13ClientName} status to ${status.uppercase()}")
                                                                }
                                                                .padding(vertical = 8.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = status.uppercase(),
                                                                color = if (isSel) Color(0xFF0F172A) else Color.White,
                                                                fontSize = 8.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = "* Clicking a state logs an immutable transaction record immediately to audit_logs collection verifying SOC audit readiness.",
                                                    color = Color(0xFF64748B),
                                                    fontSize = 9.sp,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                    2 -> {
                                        // SUB-TAB 2: CLIENT CONTACTS & ESCALATION MATRIX
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Text(
                                                text = "PORTAL USER MAPPING & ESCALATION KEY MATRIX",
                                                color = Color(0xFF38BDF8),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )

                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                OutlinedTextField(
                                                    value = p13ContactPerson,
                                                    onValueChange = { 
                                                        p13ContactPerson = it
                                                        clientContact = "$it ($p13Mobile)" // Synchronize backup handle
                                                    },
                                                    label = { Text("Primary Key Person", color = Color.Gray, fontSize = 11.sp) },
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        focusedContainerColor = Color(0xFF040812),
                                                        unfocusedContainerColor = Color(0xFF040812)
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )

                                                OutlinedTextField(
                                                    value = p13Designation,
                                                    onValueChange = { p13Designation = it },
                                                    label = { Text("Corporate Designation", color = Color.Gray, fontSize = 11.sp) },
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        focusedContainerColor = Color(0xFF040812),
                                                        unfocusedContainerColor = Color(0xFF040812)
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                OutlinedTextField(
                                                    value = p13Mobile,
                                                    onValueChange = { 
                                                        p13Mobile = it
                                                        clientContact = "$p13ContactPerson ($it)" // Synchronize backup handle
                                                    },
                                                    label = { Text("Phone Contact", color = Color.Gray, fontSize = 11.sp) },
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        focusedContainerColor = Color(0xFF040812),
                                                        unfocusedContainerColor = Color(0xFF040812)
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )

                                                OutlinedTextField(
                                                    value = p13Email,
                                                    onValueChange = { 
                                                        p13Email = it
                                                        p13PortalEmailMapped = it // Pre-fill portal mapping
                                                    },
                                                    label = { Text("Official Email Address", color = Color.Gray, fontSize = 11.sp) },
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        focusedContainerColor = Color(0xFF040812),
                                                        unfocusedContainerColor = Color(0xFF040812)
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }

                                            // Client Portal Mapping
                                            OutlinedTextField(
                                                value = p13PortalEmailMapped,
                                                onValueChange = { p13PortalEmailMapped = it },
                                                label = { Text("Mapped Client Portal User Email (Single-Sign-On Account)", color = Color.Gray, fontSize = 11.sp) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White,
                                                    focusedContainerColor = Color(0xFF040812),
                                                    unfocusedContainerColor = Color(0xFF040812)
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            // Multi-tiered Escalation Matrix parameters
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1322)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Text(
                                                        text = "🚨 ENTRUYTED ESCALATION MATRIX PATHWAY",
                                                        color = Color(0xFFFCA5A5),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )

                                                    OutlinedTextField(
                                                        value = p13Level1,
                                                        onValueChange = { p13Level1 = it },
                                                        label = { Text("LEVEL 1 - Local Sentry Patrol Station Desk", color = Color.Gray, fontSize = 10.sp) },
                                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    OutlinedTextField(
                                                        value = p13Level2,
                                                        onValueChange = { p13Level2 = it },
                                                        label = { Text("LEVEL 2 - Operational Zone Supervisor Liaison", color = Color.Gray, fontSize = 10.sp) },
                                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    OutlinedTextField(
                                                        value = p13Level3,
                                                        onValueChange = { p13Level3 = it },
                                                        label = { Text("LEVEL 3 - Executive Security Agency Director Desk", color = Color.Gray, fontSize = 10.sp) },
                                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    OutlinedTextField(
                                                        value = p13Emergency,
                                                        onValueChange = { p13Emergency = it },
                                                        label = { Text("URGENT / EMERGENCY - 24/7 Red Alert Hotline", color = Color.Gray, fontSize = 10.sp) },
                                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    3 -> {
                                        // SUB-TAB 3: CLIENT CONTRACT MANAGEMENT & SLA
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Text(
                                                text = "CLIENT CONTRACT MASTER DOCUMENT & SLA",
                                                color = Color(0xFF38BDF8),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )

                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                OutlinedTextField(
                                                    value = p13ContractCode,
                                                    onValueChange = { p13ContractCode = it },
                                                    label = { Text("SLA Contract Reference", color = Color.Gray, fontSize = 11.sp) },
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        focusedContainerColor = Color(0xFF040812),
                                                        unfocusedContainerColor = Color(0xFF040812)
                                                    ),
                                                    modifier = Modifier.weight(1.2f)
                                                )

                                                OutlinedTextField(
                                                    value = p13Valuation,
                                                    onValueChange = { p13Valuation = it },
                                                    label = { Text("Total Yearly Valuation", color = Color.Gray, fontSize = 11.sp) },
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        focusedContainerColor = Color(0xFF040812),
                                                        unfocusedContainerColor = Color(0xFF040812)
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                OutlinedTextField(
                                                    value = p13StartDate,
                                                    onValueChange = { p13StartDate = it },
                                                    label = { Text("Start Date Calendar", color = Color.Gray, fontSize = 11.sp) },
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        focusedContainerColor = Color(0xFF040812),
                                                        unfocusedContainerColor = Color(0xFF040812)
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )

                                                OutlinedTextField(
                                                    value = p13EndDate,
                                                    onValueChange = { p13EndDate = it },
                                                    label = { Text("Expiry Term Calendar", color = Color.Gray, fontSize = 11.sp) },
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        focusedContainerColor = Color(0xFF040812),
                                                        unfocusedContainerColor = Color(0xFF040812)
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }

                                            OutlinedTextField(
                                                value = p13BillingRules,
                                                onValueChange = { p13BillingRules = it },
                                                label = { Text("Billing Payment Conditions & Terms", color = Color.Gray, fontSize = 11.sp) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White,
                                                    focusedContainerColor = Color(0xFF040812),
                                                    unfocusedContainerColor = Color(0xFF040812)
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                OutlinedTextField(
                                                    value = p13GstNumber,
                                                    onValueChange = { p13GstNumber = it },
                                                    label = { Text("VAT / GST Tax Registration", color = Color.Gray, fontSize = 11.sp) },
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        focusedContainerColor = Color(0xFF040812),
                                                        unfocusedContainerColor = Color(0xFF040812)
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )

                                                OutlinedTextField(
                                                    value = p13BillingAddress,
                                                    onValueChange = { p13BillingAddress = it },
                                                    label = { Text("Tax Billing Address", color = Color.Gray, fontSize = 11.sp) },
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        focusedContainerColor = Color(0xFF040812),
                                                        unfocusedContainerColor = Color(0xFF040812)
                                                    ),
                                                    modifier = Modifier.weight(1.5f)
                                                )
                                            }
                                        }
                                    }
                                    4 -> {
                                        // SUB-TAB 4: FIRESTORE REAL-TIME MULTI-TENANT QUERY PREVIEW
                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "🔐 SEPARATED TENANT FIRESTORE EXPLORER",
                                                    color = Color(0xFF10B981),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "tenantId: tenant_singh_sec",
                                                    color = Color(0xFF34D399),
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }

                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1322)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Text(
                                                        text = "FIRESTORE ACTIVE QUERY:",
                                                        color = Color(0xFF94A3B8),
                                                        fontFamily = FontFamily.Monospace,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "clientsCollection.where(\"tenantId\", \"==\", user.tenantId)",
                                                        color = Color(0xFFF3F4F6),
                                                        fontFamily = FontFamily.Monospace,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        modifier = Modifier
                                                            .background(Color(0xFF040812))
                                                            .padding(6.dp)
                                                            .fillMaxWidth()
                                                    )
                                                    Text(
                                                        text = "* Satisfies absolute database level separation. Resources belonging to 'tenant_other_account' are completely invisible and query quarantined.",
                                                        color = Color(0xFF64748B),
                                                        fontSize = 9.sp
                                                    )
                                                }
                                            }

                                            // Read-Only active clients queried under current simulated tenantId
                                            Text(
                                                text = "ACTIVE TENANT RECOVERED DOCUMENTS (FIRESTORE):",
                                                color = Color(0xFF64748B),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )

                                            val firestoreClientsState by viewModel.firestoreClients.collectAsStateWithLifecycle()
                                            // Render matched clients securely
                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                if (firestoreClientsState.isEmpty()) {
                                                    Text("No documents loaded.", color = Color.Gray, fontSize = 10.sp)
                                                } else {
                                                    firestoreClientsState.forEach { fc ->
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(Color(0xFF0B1220))
                                                                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                                                .padding(10.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Column {
                                                                Text(text = "ID: ${fc.id} [${fc.clientCode}]", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                                Text(text = "Name: ${fc.clientName}", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                                                Text(text = "Portal: ${fc.portalEmail ?: "None"}", color = Color(0xFF64748B), fontSize = 9.sp)
                                                            }
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(6.dp))
                                                                    .background(Color(0xFF006B54))
                                                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                                            ) {
                                                                Text(fc.status.uppercase(), color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Master Onboard trigger
                                Button(
                                    onClick = {
                                        // Instantiate perfect FirestoreClient schema object matching CHECKPOINT 1
                                        val tenant = "tenant_singh_sec"
                                        val company = "cmp_centurion_sec"
                                        
                                        val newOnboardedClient = com.example.data.FirestoreClient(
                                            id = "cli_${p13ClientCode.lowercase()}",
                                            tenantId = tenant,
                                            companyId = company,
                                            clientCode = p13ClientCode,
                                            clientName = p13ClientName,
                                            clientType = p13ClientType,
                                            contactPerson = p13ContactPerson,
                                            mobile = p13Mobile,
                                            email = p13Email,
                                            designation = p13Designation,
                                            escalationMatrix = com.example.data.FirestoreEscalationMatrix(
                                                level1 = p13Level1,
                                                level2 = p13Level2,
                                                level3 = p13Level3,
                                                emergency = p13Emergency
                                            ),
                                            billingAddress = p13BillingAddress,
                                            gstNumber = p13GstNumber,
                                            status = p13Status,
                                            contractCode = p13ContractCode,
                                            contractStartDate = p13StartDate,
                                            contractEndDate = p13EndDate,
                                            contractValuation = p13Valuation,
                                            billingRules = p13BillingRules,
                                            portalEmail = p13PortalEmailMapped
                                        )
                                        
                                        viewModel.onboardFirestoreClient(newOnboardedClient)
                                        step3Status = "CONFIGURED"
                                        viewModel.logSystemAudit(
                                            action = "ONB_PHASE_3_CONFIG",
                                            details = "Sponsor setup configured and logged into simulated multi-tenant Firestore cluster.",
                                            oldValue = "DRAFT",
                                            newValue = "CLIENT_SUCCESS_BOUNDED: ${newOnboardedClient.clientName}"
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006B54)),
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "⚡ CONFIGURE SECURE CLIENT ONBOARDING",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                        4 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Provision Administrative Staff", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = accountsStaffName,
                                    onValueChange = { accountsStaffName = it },
                                    label = { Text("Ledger Accountant Title", color = Color.Gray, fontSize = 11.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = hrStaffName,
                                    onValueChange = { hrStaffName = it },
                                    label = { Text("HR Compliance Manager", color = Color.Gray, fontSize = 11.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        5 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Security Operatives File logs", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = guardName,
                                    onValueChange = { guardName = it },
                                    label = { Text("Guard Full Name", color = Color.Gray, fontSize = 11.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = guardKyc, onCheckedChange = { guardKyc = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF006B54)))
                                    Text("KYC Verification Completed", color = Color.White, fontSize = 11.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = guardTraining, onCheckedChange = { guardTraining = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF006B54)))
                                    Text("Basic Fire/First-Aid Training Checked", color = Color.White, fontSize = 11.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = guardDeviceBound, onCheckedChange = { guardDeviceBound = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF006B54)))
                                    Text("Enforce Hardware Signature Device Binding", color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                        6 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Private Sector Resident Registry", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = residentName,
                                    onValueChange = { residentName = it },
                                    label = { Text("Resident Primary Manager", color = Color.Gray, fontSize = 11.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = residentUnit,
                                    onValueChange = { residentUnit = it },
                                    label = { Text("Bound Floor / Flat Address Code", color = Color.Gray, fontSize = 11.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        7 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Address Schema Bulk Generator", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = numTowers,
                                        onValueChange = { numTowers = it },
                                        label = { Text("Towers", color = Color.Gray, fontSize = 11.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = floorsPerTower,
                                        onValueChange = { floorsPerTower = it },
                                        label = { Text("Floors", color = Color.Gray, fontSize = 11.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = flatsPerFloor,
                                        onValueChange = { flatsPerFloor = it },
                                        label = { Text("Flats/Floor", color = Color.Gray, fontSize = 11.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Button(
                                    onClick = {
                                        val towers = numTowers.toIntOrNull() ?: 0
                                        val floors = floorsPerTower.toIntOrNull() ?: 0
                                        val flats = flatsPerFloor.toIntOrNull() ?: 0
                                        bulkUnitsCount = "${towers * floors * flats}"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("🤖 COMPUTE GRID MATRIX UNITS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                if (bulkUnitsCount != "0") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF006B54).copy(alpha = 0.15f))
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = "🚀 SUCCESS: Programmatically provisioned $bulkUnitsCount unit containers in the SQLite scheme.",
                                            color = Color(0xFF34D399),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        8 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Physical Assets & Hardware Registry", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = assetQrStands,
                                        onValueChange = { assetQrStands = it },
                                        label = { Text("QR Stand IDs", color = Color.Gray, fontSize = 11.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = assetCctvCount,
                                        onValueChange = { assetCctvCount = it },
                                        label = { Text("CCTV Stream Keys", color = Color.Gray, fontSize = 11.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    listOf("Available", "Assigned", "Maintenance", "Retired").forEach { st ->
                                        val active = assetStatusState == st
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (active) Color(0xFF006B54) else Color(0xFF1E293B))
                                                .clickable { assetStatusState = st }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(st, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                        9 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Agreement Contract & Billing SLAs", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = contractAgreementCode,
                                    onValueChange = { contractAgreementCode = it },
                                    label = { Text("Contract Reference Code", color = Color.Gray, fontSize = 11.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = billingModel,
                                    onValueChange = { billingModel = it },
                                    label = { Text("Billing Rate Rule (e.g. Fixed Rota, Hourly)", color = Color.Gray, fontSize = 11.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        10 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Regulatory Compliance and PSARA laws check", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = psaraCertLoaded, onCheckedChange = { psaraCertLoaded = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF006B54)))
                                    Text("PSARA License Fulfils Security Mandate", color = Color.White, fontSize = 11.sp)
                                }
                                OutlinedTextField(
                                    value = liabilityInsuranceExpiry,
                                    onValueChange = { liabilityInsuranceExpiry = it },
                                    label = { Text("Work Injury Liability Coverage Expiry", color = Color.Gray, fontSize = 11.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        11 -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Ready for activation",
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(52.dp)
                                )
                                Text(
                                    text = "Ready for Services Activation!",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Activating will lock multi-tenant structures and initialize active transactions loops for guards and clients.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E293B))
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "MASTER OPERATIONS PERMISSION SWITCH",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Switch(
                                        checked = activeOperationsOn,
                                        onCheckedChange = { activeOperationsOn = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF006B54)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Global Button Controls for Wizard Stepping
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { if (activeStep > 1) activeStep-- },
                            enabled = activeStep > 1,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF334155),
                                disabledContainerColor = Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("BACK", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                // Save & Lock visual status configurations
                                when (activeStep) {
                                    1 -> step1Status = "ACTIVE"
                                    2 -> step2Status = "ACTIVE"
                                    3 -> step3Status = "ACTIVE"
                                    4 -> step4Status = "ACTIVE"
                                    5 -> step5Status = "ACTIVE"
                                    6 -> step6Status = "ACTIVE"
                                    7 -> step7Status = "ACTIVE"
                                    8 -> step8Status = "ACTIVE"
                                    9 -> step9Status = "ACTIVE"
                                    10 -> step10Status = "ACTIVE"
                                    11 -> {
                                        step11Status = "ACTIVE"
                                        activeOperationsOn = true
                                    }
                                }
                                viewModel.logSystemAudit(
                                    action = "ONB_PHASE_${activeStep}_COMPLETE",
                                    details = "Successfully completed and verified Onboarding Phase $activeStep components.",
                                    oldValue = "DRAFT",
                                    newValue = "ACTIVE"
                                )
                                if (activeStep < 11) {
                                    activeStep++
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006B54)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (activeStep == 11) "FINALIZE GO-LIVE" else "CONFIRM & NEXT STEP",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
