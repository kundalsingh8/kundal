package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SecurityDao {

    // ==========================================
    // USERS
    // ==========================================
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsersFlow(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?

    // ==========================================
    // SITES
    // ==========================================
    @Query("SELECT * FROM sites ORDER BY name ASC")
    fun getAllSitesFlow(): Flow<List<Site>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSite(site: Site)

    @Query("SELECT * FROM sites WHERE id = :siteId")
    suspend fun getSiteById(siteId: String): Site?

    // ==========================================
    // ATTENDANCE
    // ==========================================
    @Query("SELECT * FROM attendance ORDER BY clockInTime DESC")
    fun getAllAttendanceFlow(): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)

    @Query("SELECT * FROM attendance WHERE userId = :userId AND status = 'Open' LIMIT 1")
    suspend fun getActiveAttendanceForUser(userId: String): Attendance?

    // ==========================================
    // INCIDENTS
    // ==========================================
    @Query("SELECT * FROM incidents ORDER BY createdAt DESC")
    fun getAllIncidentsFlow(): Flow<List<Incident>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: Incident)

    // ==========================================
    // SOS ALERTS
    // ==========================================
    @Query("SELECT * FROM sos_alerts ORDER BY createdAt DESC")
    fun getAllSOSAlertsFlow(): Flow<List<SOSAlert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSOSAlert(alert: SOSAlert)

    @Query("UPDATE sos_alerts SET status = 'Resolved', updatedAt = :timestamp, updatedBy = :userId WHERE id = :alertId")
    suspend fun resolveSOSAlert(alertId: String, timestamp: Long, userId: String)

    // ==========================================
    // AUDIT LOGS
    // ==========================================
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogsFlow(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog)

    // ==========================================
    // DEVELOPMENT INITIALIZATION SEED CHECKERS
    // ==========================================
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}
