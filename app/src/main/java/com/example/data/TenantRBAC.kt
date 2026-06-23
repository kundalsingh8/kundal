package com.example.data

import android.util.Log

/**
 * ======================================================================================
 * KUNDAL OS V3 FOUNDATION - MULTI-TENANT ROLE-BASED ACCESS CONTROL (RBAC) UTILITY
 * ======================================================================================
 * Robust, production-grade security hierarchy and permission validation system.
 * This class coordinates user role ranks, granular action permissions, multi-tenant boundaries,
 * and data isolation rules to ensure compliance for the security platform.
 */

// 1. STANDARD ROLES DEFINITION WITH RANK HIERARCHY
enum class TenantRole(
    val idString: String,
    val displayName: String,
    val rank: Int,
    val defaultScope: AccessScope,
    val description: String
) {
    SUPER_ADMIN(
        idString = "super_admin",
        displayName = "Super Administrator",
        rank = 9,
        defaultScope = AccessScope.GLOBAL,
        description = "Full global visibility, master administration override capabilities across any tenant group or company."
    ),
    GROUP_ADMIN(
        idString = "group_admin",
        displayName = "Company Group Admin",
        rank = 8,
        defaultScope = AccessScope.GROUP,
        description = "Multi-tenant control and administrative functions bound strictly to their specific holding Company Group."
    ),
    COMPANY_ADMIN(
        idString = "company_admin",
        displayName = "Company Administrator",
        rank = 7,
        defaultScope = AccessScope.COMPANY,
        description = "Single-company operations controller. Owns compliance, payroll setups, and physical facility billing logs."
    ),
    SITE_ADMIN(
        idString = "site_admin",
        displayName = "Site Administrator",
        rank = 6,
        defaultScope = AccessScope.SITE,
        description = "Controls localized site operations, rosters, guard shifts, visitor permits, and site intelligence reports."
    ),
    CLIENT_MANAGER(
        idString = "client_manager",
        displayName = "Client Facility Manager",
        rank = 5,
        defaultScope = AccessScope.COMPANY,
        description = "External manager oversight role. Views attendance, billing summaries, and security statistics of their assets."
    ),
    CLIENT_USER(
        idString = "client_user",
        displayName = "Client Representative",
        rank = 4,
        defaultScope = AccessScope.SITE,
        description = "Localized external client liaison. Views attendance logs and visitor registries for their designated block."
    ),
    STAFF(
        idString = "staff",
        displayName = "HQ Operations Staff",
        rank = 3,
        defaultScope = AccessScope.COMPANY,
        description = "HQ administrative employee managing workforce assignments, roster planning, and operations databases."
    ),
    GUARD(
        idString = "guard",
        displayName = "Security Officer",
        rank = 2,
        defaultScope = AccessScope.SITE,
        description = "Field operations compliance officer executing patrols, registering visitor passes, logging incidents, and SOS signals."
    ),
    RESIDENT(
        idString = "resident",
        displayName = "Unit Resident",
        rank = 1,
        defaultScope = AccessScope.SITE,
        description = "End-user / block resident generating digital guest pre-passes, viewing active visitor queues, and trigger SOS."
    );

    companion object {
        fun fromIdString(id: String): TenantRole {
            return values().firstOrNull { it.idString.equals(id, ignoreCase = true) } ?: RESIDENT
        }
    }
}

// 2. TENANCY CLASSIFICATION SCOPES
enum class AccessScope {
    GLOBAL,   // No tenant boundaries applied (Super Admin)
    GROUP,    // Restricted to CompanyGroup level
    COMPANY,  // Restricted to specific Company
    SITE      // Restructed strictly to individual siteId
}

// 3. FINE-GRAINED SECURITY ACTION CONSTANTS
object TenantAction {
    // Site Operations
    const val CREATE_SITE = "CREATE_SITE"
    const val TRANSITION_SITE_STATUS = "TRANSITION_SITE_STATUS"
    const val VIEW_SITES = "VIEW_SITES"

    // Workforce & Employees
    const val MANAGE_WORKFORCE = "MANAGE_WORKFORCE"
    const val EDIT_SALARY_PAYROLL = "EDIT_SALARY_PAYROLL"
    const val VIEW_FINANCIALS = "VIEW_FINANCIALS"

    // Safety & Emergency
    const val TRIGGER_SOS = "TRIGGER_SOS"
    const val RESOLVE_SOS = "RESOLVE_SOS"
    const val TRIGGER_EVACUATION = "TRIGGER_EVACUATION"

    // Guarding operations
    const val CLOCK_IN_OUT = "CLOCK_IN_OUT"
    const val APPROVE_TIMESHEETS = "APPROVE_TIMESHEETS"

    // Logs & Incident management
    const val LOG_INCIDENT = "LOG_INCIDENT"
    const val EDIT_INCIDENT = "EDIT_INCIDENT"
    const val RESOLVE_INCIDENT = "RESOLVE_INCIDENT"

    // Visitor Operations
    const val MANAGE_BLACK_LIST = "MANAGE_BLACK_LIST"
    const val GENERATE_VISITOR_PASS = "GENERATE_VISITOR_PASS"
    const val CHECK_IN_OUT_VISITOR = "CHECK_IN_OUT_VISITOR"

    // Audits & Reporting
    const val VIEW_AUDITS = "VIEW_AUDITS"
    const val EXPORT_REPORTS = "EXPORT_REPORTS"
    const val CONFIGURE_SYSTEM = "CONFIGURE_SYSTEM"
}

// 4. CENTRALIZED RBAC RULE MATRIX
object TenantRBAC {
    private const val TAG = "TenantRBAC"

    // Defines which actions each abstract role has permission to execute (capability check)
    private val rolePermissions: Map<TenantRole, Set<String>> = mapOf(
        TenantRole.SUPER_ADMIN to setOf(
            TenantAction.CREATE_SITE, TenantAction.TRANSITION_SITE_STATUS, TenantAction.VIEW_SITES,
            TenantAction.MANAGE_WORKFORCE, TenantAction.EDIT_SALARY_PAYROLL, TenantAction.VIEW_FINANCIALS,
            TenantAction.TRIGGER_SOS, TenantAction.RESOLVE_SOS, TenantAction.TRIGGER_EVACUATION,
            TenantAction.CLOCK_IN_OUT, TenantAction.APPROVE_TIMESHEETS,
            TenantAction.LOG_INCIDENT, TenantAction.EDIT_INCIDENT, TenantAction.RESOLVE_INCIDENT,
            TenantAction.MANAGE_BLACK_LIST, TenantAction.GENERATE_VISITOR_PASS, TenantAction.CHECK_IN_OUT_VISITOR,
            TenantAction.VIEW_AUDITS, TenantAction.EXPORT_REPORTS, TenantAction.CONFIGURE_SYSTEM
        ),
        TenantRole.GROUP_ADMIN to setOf(
            TenantAction.CREATE_SITE, TenantAction.TRANSITION_SITE_STATUS, TenantAction.VIEW_SITES,
            TenantAction.MANAGE_WORKFORCE, TenantAction.EDIT_SALARY_PAYROLL, TenantAction.VIEW_FINANCIALS,
            TenantAction.TRIGGER_SOS, TenantAction.RESOLVE_SOS, TenantAction.TRIGGER_EVACUATION,
            TenantAction.APPROVE_TIMESHEETS,
            TenantAction.LOG_INCIDENT, TenantAction.EDIT_INCIDENT, TenantAction.RESOLVE_INCIDENT,
            TenantAction.MANAGE_BLACK_LIST, TenantAction.GENERATE_VISITOR_PASS, TenantAction.CHECK_IN_OUT_VISITOR,
            TenantAction.VIEW_AUDITS, TenantAction.EXPORT_REPORTS
        ),
        TenantRole.COMPANY_ADMIN to setOf(
            TenantAction.CREATE_SITE, TenantAction.TRANSITION_SITE_STATUS, TenantAction.VIEW_SITES,
            TenantAction.MANAGE_WORKFORCE, TenantAction.EDIT_SALARY_PAYROLL, TenantAction.VIEW_FINANCIALS,
            TenantAction.TRIGGER_SOS, TenantAction.RESOLVE_SOS, TenantAction.TRIGGER_EVACUATION,
            TenantAction.APPROVE_TIMESHEETS,
            TenantAction.LOG_INCIDENT, TenantAction.EDIT_INCIDENT, TenantAction.RESOLVE_INCIDENT,
            TenantAction.MANAGE_BLACK_LIST, TenantAction.GENERATE_VISITOR_PASS, TenantAction.CHECK_IN_OUT_VISITOR,
            TenantAction.EXPORT_REPORTS
        ),
        TenantRole.SITE_ADMIN to setOf(
            TenantAction.VIEW_SITES,
            TenantAction.MANAGE_WORKFORCE, TenantAction.TRIGGER_SOS, TenantAction.RESOLVE_SOS, TenantAction.TRIGGER_EVACUATION,
            TenantAction.CLOCK_IN_OUT, TenantAction.APPROVE_TIMESHEETS,
            TenantAction.LOG_INCIDENT, TenantAction.EDIT_INCIDENT, TenantAction.RESOLVE_INCIDENT,
            TenantAction.MANAGE_BLACK_LIST, TenantAction.GENERATE_VISITOR_PASS, TenantAction.CHECK_IN_OUT_VISITOR,
            TenantAction.EXPORT_REPORTS
        ),
        TenantRole.CLIENT_MANAGER to setOf(
            TenantAction.VIEW_SITES,
            TenantAction.TRIGGER_SOS, TenantAction.VIEW_FINANCIALS,
            TenantAction.LOG_INCIDENT, TenantAction.EXPORT_REPORTS
        ),
        TenantRole.CLIENT_USER to setOf(
            TenantAction.VIEW_SITES,
            TenantAction.LOG_INCIDENT,
            TenantAction.GENERATE_VISITOR_PASS
        ),
        TenantRole.STAFF to setOf(
            TenantAction.VIEW_SITES,
            TenantAction.MANAGE_WORKFORCE,
            TenantAction.LOG_INCIDENT, TenantAction.EDIT_INCIDENT,
            TenantAction.GENERATE_VISITOR_PASS, TenantAction.CHECK_IN_OUT_VISITOR
        ),
        TenantRole.GUARD to setOf(
            TenantAction.TRIGGER_SOS,
            TenantAction.CLOCK_IN_OUT,
            TenantAction.LOG_INCIDENT,
            TenantAction.GENERATE_VISITOR_PASS, TenantAction.CHECK_IN_OUT_VISITOR
        ),
        TenantRole.RESIDENT to setOf(
            TenantAction.TRIGGER_SOS,
            TenantAction.LOG_INCIDENT,
            TenantAction.GENERATE_VISITOR_PASS
        )
    )

    /**
     * Determines which segments of the system layout are authorized for rendering
     */
    fun getAllowedSections(roleId: String): List<String> {
        val role = TenantRole.fromIdString(roleId)
        return when (role) {
            TenantRole.SUPER_ADMIN, TenantRole.GROUP_ADMIN -> listOf(
                "Overview", "Onboarding", "Master Data", "Sites", "Workforce", "Attendance", "Visitors", "Incidents", "SOS", "Payroll", "Billing", "Reports", "Audits"
            )
            TenantRole.COMPANY_ADMIN -> listOf(
                "Overview", "Onboarding", "Master Data", "Sites", "Workforce", "Attendance", "Visitors", "Incidents", "SOS", "Payroll", "Billing", "Reports", "Audits"
            )
            TenantRole.SITE_ADMIN -> listOf(
                "Overview", "Workforce", "Attendance", "Visitors", "Incidents", "SOS", "Reports"
            )
            TenantRole.CLIENT_MANAGER -> listOf(
                "Overview", "Attendance", "Incidents", "SOS", "Billing", "Reports"
            )
            TenantRole.CLIENT_USER -> listOf(
                "Overview", "Attendance", "Visitors", "Incidents"
            )
            TenantRole.STAFF -> listOf(
                "Overview", "Workforce", "Attendance", "Visitors", "Incidents"
            )
            TenantRole.GUARD -> listOf(
                "Overview", "Attendance", "Visitors", "Incidents", "SOS"
            )
            TenantRole.RESIDENT -> listOf(
                "Overview", "Visitors", "Incidents", "SOS"
            )
        }
    }

    /**
     * Performs an abstract capability permission check on the role alone.
     */
    fun canRolePerformAction(roleId: String, action: String): Boolean {
        val role = TenantRole.fromIdString(roleId)
        val permissions = rolePermissions[role] ?: emptySet()
        val hasCap = permissions.contains(action)
        Log.d(TAG, "Role permission check: role=$roleId, action=$action -> allowed=$hasCap")
        return hasCap
    }

    /**
     * Core Enterprise Security Checker: Evaluates permission + Multi-tenant isolation boundaries.
     * Combines high-resolution functional auth with strict row-level relational isolation.
     *
     * @param user The authenticated user/employee executing the action
     * @param targetGroupId The group ID of the target resource
     * @param targetCompanyId The company ID of the target resource
     * @param targetSiteId The site ID of the target resource
     * @param action The operation token requested (from TenantAction)
     * @return Boolean true if fully authorized, false if denied (creating an audit hazard).
     */
    fun checkAccess(
        user: User,
        targetGroupId: String,
        targetCompanyId: String,
        targetSiteId: String,
        action: String
    ): Boolean {
        // Step 1: Perform the capability permission check
        if (!canRolePerformAction(user.role, action)) {
            Log.w(TAG, "ACCESS DENIED: Role '${user.role}' lacks functional capability for '$action'")
            return false
        }

        // Step 2: Super Admin bypasses all structural tenant checks
        val role = TenantRole.fromIdString(user.role)
        if (role == TenantRole.SUPER_ADMIN) {
            Log.d(TAG, "ACCESS GRANTED: Superadmin override capability applied")
            return true
        }

        // Step 3: Evaluate Tenant Isolation Boundaries
        return when (role.defaultScope) {
            AccessScope.GLOBAL -> {
                // Technically only Super Admin has Global, but added for safety
                true
            }
            AccessScope.GROUP -> {
                // Must belong to the exact same holding company group
                val match = user.companyGroupId == targetGroupId
                if (!match) {
                    Log.w(TAG, "TENANT BREACH AVOIDED: Group-Admin group '${user.companyGroupId}' != Resource group '$targetGroupId'")
                }
                match
            }
            AccessScope.COMPANY -> {
                // Must belong to the exact same company group AND company ID
                val sameGroup = user.companyGroupId == targetGroupId
                val sameCompany = user.companyId == targetCompanyId
                val allowed = sameGroup && sameCompany
                if (!allowed) {
                    Log.w(TAG, "TENANT BREACH AVOIDED: Company-level boundary mismatch for user company [${user.companyId}] and target [${targetCompanyId}]")
                }
                allowed
            }
            AccessScope.SITE -> {
                // Must belong to the exact same group, company, and physical site
                val sameGroup = user.companyGroupId == targetGroupId
                val sameCompany = user.companyId == targetCompanyId
                
                // If user's siteId is empty/unasigned, they manage the entire company (or site_admin with unassigned handles company)
                val sameSite = if (user.siteId.isEmpty()) {
                    true
                } else {
                    user.siteId == targetSiteId
                }

                val allowed = sameGroup && sameCompany && sameSite
                if (!allowed) {
                    Log.w(TAG, "TENANT BREACH AVOIDED: Site-level boundary mismatch. User site [${user.siteId}] vs Target site [${targetSiteId}]")
                }
                allowed
            }
        }
    }

    /**
     * Enforces User Provisioning & Administration Hierarchy Ranks.
     * Prevents administrative escalation (e.g. site_admin creating a company_admin).
     *
     * @param actor The administrative user issuing the command
     * @param targetRoleString The role string the actor is trying to grant or update
     * @return Boolean true if authorization succeeds
     */
    fun canManageUserRole(actor: User, targetRoleString: String): Boolean {
        val actorRole = TenantRole.fromIdString(actor.role)
        val targetRole = TenantRole.fromIdString(targetRoleString)

        // Super Admin can assign anything except they are the absolute ceiling
        if (actorRole == TenantRole.SUPER_ADMIN) return true

        // Strict: Administrative role rank must be strictly higher than the target role level
        val allowed = actorRole.rank > targetRole.rank
        if (!allowed) {
            Log.w(TAG, "HIERARCHY ESCALATION ATTEMPT: Actor '${actor.role}' (rank ${actorRole.rank}) tried to manage role '$targetRoleString' (rank ${targetRole.rank})")
        }
        return allowed
    }

    /**
     * Helper to retrieve security authorization metadata details for audit logging
     */
    fun prepareAuditDetails(
        user: User,
        action: String,
        success: Boolean,
        targetId: String
    ): String {
        val status = if (success) "AUTHORIZED" else "DENIED_RBAC_VIOLATION"
        return "RBAC ($status) | ActorID: ${user.id} | Role: ${user.role} | Action: $action | TargetID: $targetId | GeoBound: ${user.siteId.ifEmpty { "GLOBAL" }}"
    }
}
