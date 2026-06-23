package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Database
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// ==========================================
// Phase 9 & Phase 2: Database Standardization
// Unified Base Operational Structure
// ==========================================

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val companyGroupId: String,
    val companyId: String,
    val siteId: String,  // Bind to site (or Empty for global admin roles)
    val name: String,
    val email: String,
    val role: String,    // standard roles strictly from Phase 3: super_admin, group_admin, company_admin, site_admin, guard, staff, resident, client_manager, client_user
    val createdAt: Long,
    val updatedAt: Long,
    val createdBy: String,
    val updatedBy: String,
    val status: String   // Active, Suspended, Archived
)

@Entity(tableName = "sites")
data class Site(
    @PrimaryKey val id: String,
    val companyGroupId: String,
    val companyId: String,
    val siteId: String,  // Equal to id for single source of truth routing
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: Long,
    val updatedAt: Long,
    val createdBy: String,
    val updatedBy: String,
    val status: String   // Phase 5 Lifecycle: Draft, Pending Approval, Setup, Active, Suspended, Archived
)

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey val id: String,
    val companyGroupId: String,
    val companyId: String,
    val siteId: String,
    val userId: String,
    val userName: String,
    val role: String,
    val clockInTime: Long,
    val clockOutTime: Long?,
    val clockInGeo: String,
    val clockOutGeo: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val createdBy: String,
    val updatedBy: String,
    val status: String   // Open, Closed
)

@Entity(tableName = "incidents")
data class Incident(
    @PrimaryKey val id: String,
    val companyGroupId: String,
    val companyId: String,
    val siteId: String,
    val title: String,
    val severity: String, // Low, Medium, High, Critical
    val description: String,
    val reportedBy: String,
    val reporterName: String,
    val createdAt: Long,
    val updatedAt: Long,
    val createdBy: String,
    val updatedBy: String,
    val status: String   // Draft, Pending, Under Investigation, Resolved, Suspended
)

@Entity(tableName = "sos_alerts")
data class SOSAlert(
    @PrimaryKey val id: String,
    val companyGroupId: String,
    val companyId: String,
    val siteId: String,
    val triggeredBy: String,
    val triggererName: String,
    val triggererRole: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: Long,
    val updatedAt: Long,
    val createdBy: String,
    val updatedBy: String,
    val status: String   // Active, Dispatched, Resolved
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey val id: String,
    val companyGroupId: String,
    val companyId: String,
    val siteId: String,
    val action: String,       // ACTION_NAME
    val details: String,
    val targetId: String,
    val performedBy: String,
    val oldValue: String,
    val newValue: String,
    val timestamp: Long
)
