package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class SecurityRepository(private val dao: SecurityDao) {

    val usersFlow: Flow<List<User>> = dao.getAllUsersFlow()
    val sitesFlow: Flow<List<Site>> = dao.getAllSitesFlow()
    val attendanceFlow: Flow<List<Attendance>> = dao.getAllAttendanceFlow()
    val incidentsFlow: Flow<List<Incident>> = dao.getAllIncidentsFlow()
    val sosAlertsFlow: Flow<List<SOSAlert>> = dao.getAllSOSAlertsFlow()
    val auditLogsFlow: Flow<List<AuditLog>> = dao.getAllAuditLogsFlow()

    suspend fun insertUser(user: User) {
        dao.insertUser(user)
    }

    suspend fun insertSite(site: Site) {
        dao.insertSite(site)
        logAudit(
            action = "SITE_UPSERT",
            details = "Upserted site: ${site.name} (Status: ${site.status})",
            targetId = site.id,
            performedBy = site.createdBy,
            oldValue = "",
            newValue = site.status,
            siteId = site.id,
            companyId = site.companyId,
            companyGroupId = site.companyGroupId
        )
    }

    suspend fun insertAttendance(attendance: Attendance) {
        dao.insertAttendance(attendance)
    }

    suspend fun getActiveAttendanceForUser(userId: String): Attendance? {
        return dao.getActiveAttendanceForUser(userId)
    }

    suspend fun insertIncident(incident: Incident) {
        dao.insertIncident(incident)
        logAudit(
            action = "INCIDENT_LOG",
            details = "Logged incident '${incident.title}' with severity ${incident.severity}",
            targetId = incident.id,
            performedBy = incident.createdBy,
            oldValue = "None",
            newValue = incident.status,
            siteId = incident.siteId,
            companyId = incident.companyId,
            companyGroupId = incident.companyGroupId
        )
    }

    suspend fun insertSOSAlert(alert: SOSAlert) {
        dao.insertSOSAlert(alert)
        logAudit(
            action = "SOS_TRIGGERED",
            details = "SOS threat signal broadcasted by ${alert.triggererName} (${alert.triggererRole})",
            targetId = alert.id,
            performedBy = alert.triggeredBy,
            oldValue = "Inactive",
            newValue = alert.status,
            siteId = alert.siteId,
            companyId = alert.companyId,
            companyGroupId = alert.companyGroupId
        )
    }

    suspend fun resolveSOSAlert(alertId: String, userId: String, userName: String) {
        val now = System.currentTimeMillis()
        dao.resolveSOSAlert(alertId, now, userId)
        logAudit(
            action = "SOS_RESOLVED",
            details = "SOS alert resolved by $userName",
            targetId = alertId,
            performedBy = userId,
            oldValue = "Active",
            newValue = "Resolved",
            siteId = "global_sys",
            companyId = "global_sys_cmp",
            companyGroupId = "global_sys_grp"
        )
    }

    suspend fun logAudit(
        action: String,
        details: String,
        targetId: String,
        performedBy: String,
        oldValue: String,
        newValue: String,
        siteId: String,
        companyId: String,
        companyGroupId: String
    ) {
        val audit = AuditLog(
            id = "aud_${UUID.randomUUID().toString().take(6)}",
            companyGroupId = companyGroupId,
            companyId = companyId,
            siteId = siteId,
            action = action,
            details = details,
            targetId = targetId,
            performedBy = performedBy,
            oldValue = oldValue,
            newValue = newValue,
            timestamp = System.currentTimeMillis()
        )
        dao.insertAuditLog(audit)
    }

    suspend fun seedMockDataIfNeeded() {
        if (dao.getUserCount() > 0) return

        val now = System.currentTimeMillis()
        val grp = "grp_kundal_global"
        val cmp = "cmp_centurion_sec"
        val siteActive = "site_cyber_hub"
        val siteSetup = "site_apex_towers"
        val siteDraft = "site_green_meadows"

        // 1. STANDARD ROLES (Phase 3)
        val seedUsers = listOf(
            User("usr_vikram", grp, cmp, "", "Vikram Kundal", "vikram@kundal.com", "super_admin", now, now, "system", "system", "Active"),
            User("usr_karan", grp, cmp, "", "Karan Johar", "karan@kundal.com", "group_admin", now, now, "system", "system", "Active"),
            User("usr_siddharth", grp, cmp, "", "Siddharth Malhotra", "siddharth@kundal.com", "company_admin", now, now, "system", "system", "Active"),
            User("usr_amitabh", grp, cmp, siteActive, "Amitabh Bachchan", "amitabh@kundal.com", "site_admin", now, now, "system", "system", "Active"),
            User("usr_rahul", grp, cmp, siteActive, "Rahul Kumar", "rahul@kundal.com", "guard", now, now, "system", "system", "Active"),
            User("usr_rajesh", grp, cmp, siteActive, "Rajesh Koothrappali", "rajesh@kundal.com", "resident", now, now, "system", "system", "Active"),
            User("usr_tony", grp, cmp, siteActive, "Tony Stark", "tony@kundal.com", "client_manager", now, now, "system", "system", "Active"),
            User("usr_pepper", grp, cmp, siteActive, "Pepper Potts", "pepper@kundal.com", "client_user", now, now, "system", "system", "Active")
        )
        for (u in seedUsers) {
            dao.insertUser(u)
        }

        // 2. STANDARD SITES with Standard Phase 5 lifecycles
        val seedSites = listOf(
            Site(siteActive, grp, cmp, siteActive, "Cyber Hub Campus V3", "Building 10, Sector 24, Cyber City", 28.4951, 77.0894, now - 600000, now, "usr_siddharth", "usr_siddharth", "Active"),
            Site(siteSetup, grp, cmp, siteSetup, "Apex Towers Premium Complex", "Plot 42, Gachibowli High Street", 17.4483, 78.3741, now - 500000, now, "usr_amitabh", "usr_amitabh", "Setup"),
            Site(siteDraft, grp, cmp, siteDraft, "Green Meadow Residences", "Street 19, Whitefield Boulevard", 12.9698, 77.7500, now, now, "usr_amitabh", "usr_amitabh", "Draft"),
            Site("site_metro", grp, cmp, "site_metro", "Metro Depot Delta (Suspended OS)", "Sector-8 Depot yards", 28.6139, 77.2090, now - 900000, now, "usr_vikram", "system", "Suspended"),
            Site("site_yards", grp, cmp, "site_yards", "Legacy Steel Yards (Archived)", "Industrial Belt Gate B", 19.0760, 72.8777, now - 1000000, now, "usr_vikram", "system", "Archived")
        )
        for (s in seedSites) {
            dao.insertSite(s)
        }

        // 3. Historical Attendance Logging
        val attendance = listOf(
            Attendance("att_1", grp, cmp, siteActive, "usr_rahul", "Rahul Kumar", "guard", now - 3600000, now, "GeoIn: (28.4951,77.0894)", "GeoOut: (28.4951,77.0894)", now - 3600000, now, "system", "system", "Closed"),
            Attendance("att_2", grp, cmp, siteActive, "usr_amitabh", "Amitabh Bachchan", "site_admin", now - 2800000, null, "GeoIn: (28.4952,77.0895)", null, now - 2800000, now - 2800000, "system", "system", "Open")
        )
        for (a in attendance) {
            dao.insertAttendance(a)
        }

        // 4. Initial Incident Incident Records
        val seedIncidents = listOf(
            Incident("inc_1", grp, cmp, siteActive, "Unattended Package Gate 2", "High", "Unidentified cardboard box spotted near heavy transit lanes. Scanning completed, cleared as shipping garbage.", "usr_rahul", "Rahul Kumar", now - 500000, now - 200000, "usr_rahul", "usr_amitabh", "Resolved"),
            Incident("inc_2", grp, cmp, siteActive, "Faulty Security Sensor B4", "Medium", "Motion sensor repeatedly triggers false exceptions under windy bursts. Assigned vendor dispatch.", "usr_amitabh", "Amitabh Bachchan", now - 300000, now - 300000, "usr_amitabh", "usr_amitabh", "Under Investigation")
        )
        for (i in seedIncidents) {
            dao.insertIncident(i)
        }

        // 5. Initial SOS
        val seedSOS = SOSAlert(
            id = "sos_pioneer",
            companyGroupId = grp,
            companyId = cmp,
            siteId = siteActive,
            triggeredBy = "usr_rahul",
            triggererName = "Rahul Kumar",
            triggererRole = "guard",
            latitude = 28.4951,
            longitude = 77.0894,
            createdAt = now - 50000,
            updatedAt = now - 50000,
            createdBy = "usr_rahul",
            updatedBy = "usr_rahul",
            status = "Active"
        )
        dao.insertSOSAlert(seedSOS)

        // 6. Formulate Audit Trails
        val audits = listOf(
            AuditLog("aud_init_sys", grp, cmp, "global_sys", "SYSTEM_INITIALIZATION", "Kundal Security OS V3 Database Engine bootstrapped.", "sys_master", "system", "None", "Active", now - 2000000),
            AuditLog("aud_rbac_setup", grp, cmp, "global_sys", "RBAC_PROVISIONING", "Centralized authorization tokens created for super_admin, company_admin, group_admin, site_admin, and guard.", "sys_master", "system", "None", "Active", now - 1800000),
            AuditLog("aud_area_officer_dep", grp, cmp, "global_sys", "POLICY_CLEANUP", "Successfully DEPRECATED legacy area_officer role and areaOfficeId middle-tier bindings.", "sys_master", "system", "area_officer", "None", now - 1700000)
        )
        for (au in audits) {
            dao.insertAuditLog(au)
        }
    }
}
