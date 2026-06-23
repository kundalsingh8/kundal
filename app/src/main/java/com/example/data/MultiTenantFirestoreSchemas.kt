package com.example.data

// ==========================================
// CHECKPOINT 1: MULTI-TENANT FIRESTORE FOUNDATION
// Locked schema definitions for Kundal Security OS V3
// ==========================================

data class FirestoreAddress(
    val line1: String = "",
    val area: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val country: String = "Singapore"
)

data class FirestoreSiteAddress(
    val line1: String = "",
    val area: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = ""
)

data class FirestoreContact(
    val name: String = "",
    val mobile: String = "",
    val email: String = ""
)

data class FirestoreEscalationMatrix(
    val level1: String? = null,
    val level2: String? = null,
    val level3: String? = null,
    val emergency: String? = null
)

data class FirestoreGps(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val geofenceRadius: Double? = null
)

data class FirestoreModules(
    val attendance: Boolean = true,
    val patrol: Boolean = true,
    val visitor: Boolean = true,
    val incident: Boolean = true,
    val sos: Boolean = true,
    val resident: Boolean = false
)

data class FirestoreKyc(
    val aadhaar: String? = null,
    val pan: String? = null
)

// 1. COMPANIES COLLECTION
// companies/{companyId}
data class FirestoreCompany(
    val id: String,
    val companyCode: String,
    val companyName: String,
    val legalCompanyName: String,
    val companyType: String, // proprietorship | partnership | llp | private_limited | public_limited
    val businessCategory: String, // security | facility_management | security_facility
    val companyGroupId: String? = null,
    val tenantId: String,
    val panNumber: String,
    val gstEnabled: Boolean,
    val gstNumber: String? = null,
    val cinNumber: String? = null,
    val address: FirestoreAddress = FirestoreAddress(),
    val contact: FirestoreContact = FirestoreContact(),
    val payrollSettingsId: String? = null,
    val billingSettingsId: String? = null,
    val subscriptionPlan: String, // starter | professional | enterprise
    val status: String, // draft | active | suspended | archived
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// 2. CLIENTS COLLECTION
// clients/{clientId}
data class FirestoreClient(
    val id: String,
    val tenantId: String,
    val companyId: String,
    val clientCode: String,
    val clientName: String,
    val clientType: String, // residential | corporate | industrial | hospital | school | hotel | mall
    val contactPerson: String,
    val mobile: String,
    val email: String,
    val designation: String,
    val escalationMatrix: FirestoreEscalationMatrix = FirestoreEscalationMatrix(),
    val billingAddress: String? = null,
    val gstNumber: String? = null,
    val status: String, // lead | prospect | active | inactive | archived
    
    // Contract & Portal mapping enhancements for Phase 13 Client Onboarding System
    val contractCode: String? = null,
    val contractStartDate: String? = null,
    val contractEndDate: String? = null,
    val contractValuation: String? = null, // SLA valuation (e.g. $120,000 / Yr)
    val billingRules: String? = null, // Pay on 5th, Net-30, etc.
    val portalEmail: String? = null, // Client Portal Account Mapping
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// 3. SITES COLLECTION
// sites/{siteId}
data class FirestoreSite(
    val id: String,
    val tenantId: String,
    val companyId: String,
    val clientId: String,
    val siteCode: String,
    val siteName: String,
    val siteType: String, // residential | commercial | corporate | factory | warehouse | hospital | school | hotel | mall | construction
    val address: FirestoreSiteAddress = FirestoreSiteAddress(),
    val gps: FirestoreGps = FirestoreGps(),
    val modules: FirestoreModules = FirestoreModules(),
    val shiftModel: Int, // 1 | 2 | 3 | 4
    val shiftStartTime: String = "08:00",
    val readinessScore: Double = 100.0,
    val status: String, // draft | setup | active | suspended | archived
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// 4. USERS COLLECTION
// users/{userId}
data class FirestoreUser(
    val id: String,
    val tenantId: String,
    val companyId: String,
    val siteId: String? = null,
    val employeeId: String? = null,
    val fullName: String,
    val mobile: String,
    val email: String? = null,
    val role: String, // super_admin | company_admin | client_manager | site_admin | supervisor | guard | resident
    val status: String, // active | inactive | suspended
    val kyc: FirestoreKyc = FirestoreKyc(),
    val deviceBindingEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// 5. ROLES COLLECTION
// roles/{roleId}
data class FirestoreRole(
    val roleId: String,
    val roleName: String,
    val description: String,
    val privileges: List<String> = emptyList()
)

// 6. AUDIT LOGS COLLECTION
// audit_logs/{logId}
data class FirestoreAuditLog(
    val id: String,
    val tenantId: String,
    val companyId: String,
    val action: String,
    val details: String,
    val performedBy: String,
    val oldValue: String = "",
    val newValue: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

// ==========================================
// PHASE 15: WORKFORCE MANAGEMENT & RECORDS
// ==========================================

data class FirestoreWfAddress(
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = ""
)

data class FirestoreEmployee(
    val id: String, // Employee ID (e.g. EMP-001)
    val tenantId: String,
    val companyId: String,
    
    // Step 1: Basic Information
    val fullName: String,
    val fatherName: String = "",
    val dob: String = "",
    val gender: String = "Male",
    val mobile: String,
    val altMobile: String = "",
    val email: String = "",
    val bloodGroup: String = "O+",
    val maritalStatus: String = "Single",
    
    // Step 2: Address Details
    val permanentAddress: FirestoreWfAddress = FirestoreWfAddress(),
    val currentAddress: FirestoreWfAddress = FirestoreWfAddress(),
    
    // Step 3: Employment Details
    val employeeType: String, // Guard, Supervisor, Lady Guard, Bouncer, Operator, Site Admin, Staff, Area Manager
    val joiningDate: String = "2026-06-22",
    val reportingManager: String = "Lim Ah San (Supervisor)",
    val company: String = "Centurion Security",
    val department: String = "Operations",
    val status: String = "Active", // Candidate, Document Verification, Training, Joining, Deployment Ready, Active, Transferred, Suspended, Exited
    
    // Step 4: KYC & Compliance
    val aadhaar: String = "",
    val pan: String = "",
    val voterId: String = "",
    val drivingLicense: String = "",
    val passport: String = "",
    val policeVerificationStatus: String = "Pending", // Pending, Verified, Rejected
    val backgroundVerificationStatus: String = "Pending", // Pending, Verified, Rejected
    
    // Step 5: Bank Details
    val accountHolder: String = "",
    val bankName: String = "",
    val accountNumber: String = "",
    val ifscCode: String = "",
    val upiId: String = "",
    
    // Step 8: Readiness Score
    val readinessScore: Double = 0.0, // 0 - 100%
    
    // Step 10: Leave & Attendance preferences
    val attendancePreference: String = "QR", // QR, GPS, Selfie
    
    // Step 11: Emergency Contacts
    val emergencyContactName: String = "",
    val emergencyContactRelation: String = "",
    val emergencyContactPhone: String = "",
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "system",
    val updatedBy: String = "system"
)

data class FirestoreEmployeeDocument(
    val id: String,
    val tenantId: String,
    val companyId: String,
    val employeeId: String,
    
    // Document Vault Vault-URLs/Metadata representing Step 12
    val aadhaarDocUrl: String = "",
    val panDocUrl: String = "",
    val photoUrl: String = "",
    val bankPassbookUrl: String = "",
    val policeVerificationUrl: String = "",
    val trainingCertificatesUrl: String = "",
    val medicalCertificateUrl: String = "",
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "system",
    val updatedBy: String = "system"
)

data class FirestoreEmployeeTraining(
    val id: String,
    val tenantId: String,
    val companyId: String,
    val employeeId: String,
    
    // Step 6: Training status trackers
    val psaraTraining: String = "Not Started", // Not Started, In Progress, Completed, Expired
    val fireSafety: String = "Not Started",
    val firstAid: String = "Not Started",
    val industrialSafety: String = "Not Started",
    val siteSopTraining: String = "Not Started",
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "system",
    val updatedBy: String = "system"
)

data class FirestoreEmployeeDeployment(
    val id: String,
    val tenantId: String,
    val companyId: String,
    val employeeId: String,
    
    // Step 9: Site Assignments
    val siteId: String = "",
    val siteName: String = "",
    val shiftName: String = "Morning Shift (08:00 - 20:00)",
    val position: String = "Security Guard",
    val reportingSupervisor: String = "Lim Ah San",
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "system",
    val updatedBy: String = "system"
)

data class FirestoreEmployeeTransfer(
    val id: String,
    val tenantId: String,
    val companyId: String,
    val employeeId: String,
    
    val fromSiteId: String = "",
    val fromSiteName: String = "",
    val toSiteId: String = "",
    val toSiteName: String = "",
    val fromShift: String = "",
    val toShift: String = "",
    val fromPosition: String = "",
    val toPosition: String = "",
    val transferDate: String = "2026-06-22",
    val reason: String = "Routine operational reassignment",
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "system",
    val updatedBy: String = "system"
)

data class FirestoreEmployeeEquipment(
    val id: String,
    val tenantId: String,
    val companyId: String,
    val employeeId: String,
    
    // Step 7: Uniform & Equipment assignment (True/False or date issued)
    val uniformSetIssuedDate: String = "",
    val uniformSetReturnedDate: String = "",
    val idCardIssuedDate: String = "",
    val idCardReturnedDate: String = "",
    val shoesIssuedDate: String = "",
    val shoesReturnedDate: String = "",
    val beltIssuedDate: String = "",
    val beltReturnedDate: String = "",
    val capIssuedDate: String = "",
    val capReturnedDate: String = "",
    val walkieTalkieIssuedDate: String = "",
    val walkieTalkieReturnedDate: String = "",
    val mobileDeviceIssuedDate: String = "",
    val mobileDeviceReturnedDate: String = "",
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "system",
    val updatedBy: String = "system"
)

data class FirestoreEmployeeLeaveProfile(
    val id: String,
    val tenantId: String,
    val companyId: String,
    val employeeId: String,
    
    // Step 10: Leave balances initialized
    val casualLeaveLimit: Int = 12,
    val casualLeaveTaken: Int = 0,
    val sickLeaveLimit: Int = 10,
    val sickLeaveTaken: Int = 0,
    val earnedLeaveLimit: Int = 15,
    val earnedLeaveTaken: Int = 0,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "system",
    val updatedBy: String = "system"
)

data class FirestoreEmployeePayrollProfile(
    val id: String,
    val tenantId: String,
    val companyId: String,
    val employeeId: String,
    
    // Payroll properties
    val baseSalaryOption: String = "Monthly Fixed", // Monthly Fixed or Hourly
    val baseSalaryAmount: Double = 2200.0,
    val bankAccountHolder: String = "",
    val bankAccountNumber: String = "",
    val pfEnabled: Boolean = true,
    val esicEnabled: Boolean = true,
    val tdsWithholdingPercentage: Double = 10.0,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "system",
    val updatedBy: String = "system"
)

// ==========================================
// PHASE 16: Attendance & Shift Management
// ==========================================

data class FirestoreGpsCoords(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracy: Double = 5.0
)

data class FirestoreAttendanceRecord(
    val id: String = "",
    val tenantId: String = "",
    val companyId: String = "",
    val employeeId: String = "",
    val employeeName: String = "",
    val siteId: String = "",
    val siteName: String = "",
    val shiftId: String = "",
    val shiftName: String = "",
    val attendanceMethod: String = "GPS", // 'QR' | 'GPS' | 'SELFIE' | 'HYBRID'
    val checkInTime: Long = 0L,
    val checkOutTime: Long? = null,
    val status: String = "PRESENT", // PRESENT, ABSENT, LATE, HALF_DAY, ON_LEAVE, EARLY_EXIT, MISSED_CHECK_OUT
    val workedHours: Double = 0.0,
    val overtimeHours: Double = 0.0,
    val lateMinutes: Int = 0,
    val gps: FirestoreGpsCoords? = null,
    val checkOutGps: FirestoreGpsCoords? = null,
    val selfieUrl: String? = null,
    val checkOutSelfieUrl: String? = null,
    val remarks: String = "",
    val checkOutRemarks: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class FirestoreAttendanceSession(
    val id: String = "",
    val recordId: String = "",
    val employeeId: String = "",
    val siteId: String = "",
    val shiftId: String = "",
    val sessionType: String = "CHECK_IN", // CHECK_IN, CHECK_OUT, PATROLLING, SHIFT_LOG
    val deviceId: String = "",
    val gpsVerified: Boolean = true,
    val selfieVerified: Boolean = true,
    val qrVerified: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

data class FirestoreAttendanceException(
    val id: String = "",
    val recordId: String = "",
    val employeeId: String = "",
    val employeeName: String = "",
    val siteId: String = "",
    val siteName: String = "",
    val exceptionType: String = "", // LATE_ARRIVAL, MISSED_CHECK_IN, MISSED_CHECK_OUT, GPS_FAILURE, DEVICE_FAILURE, SELFIE_FAILURE, DUPLICATE_ATTENDANCE
    val description: String = "",
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED, OVERRIDDEN, ESCALATED
    val timestamp: Long = System.currentTimeMillis(),
    val remarks: String = ""
)

data class FirestoreAttendanceCorrection(
    val id: String = "",
    val recordId: String = "",
    val employeeId: String = "",
    val employeeName: String = "",
    val siteId: String = "",
    val requestDate: Long = System.currentTimeMillis(),
    val requestedCheckIn: Long? = null,
    val requestedCheckOut: Long? = null,
    val reason: String = "",
    val status: String = "PENDING", // PENDING, SUPERVISOR_REVIEW, SITE_ADMIN_APPROVED, REJECTED
    val auditLog: List<String> = emptyList()
)

data class FirestoreAttendanceConfig(
    val siteId: String = "",
    val siteName: String = "",
    val shiftId: String = "",
    val shiftName: String = "",
    val attendanceMethod: String = "GPS", // QR, GPS, SELFIE, QR+GPS, QR+SELFIE, GPS+SELFIE, QR+GPS+SELFIE
    val gpsRequired: Boolean = true,
    val selfieRequired: Boolean = false,
    val qrRequired: Boolean = false,
    val deviceBindingRequired: Boolean = true,
    val geofenceRadius: Double = 200.0, // in meters
    val allowedDeviceIds: List<String> = emptyList()
)

data class FirestoreAttendanceQrCode(
    val id: String = "",
    val siteId: String = "",
    val siteName: String = "",
    val shiftId: String = "",
    val shiftName: String = "",
    val qrContent: String = "",
    val isActive: Boolean = true,
    val generatedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 86400000L // 24 hours
)

data class FirestoreAttendanceReport(
    val id: String = "",
    val type: String = "DAILY", // DAILY, WEEKLY, MONTHLY, LATE, OVERTIME, ABSENT
    val generatedBy: String = "",
    val format: String = "PDF", // PDF, EXCEL, CSV
    val timestamp: Long = System.currentTimeMillis(),
    val downloadUrl: String = "",
    val filterSiteId: String = "ALL",
    val filterMonth: String = "June 2026"
)

data class FirestoreVisitor(
    val id: String = "",
    val tenantId: String = "tenant_singh_sec",
    val companyId: String = "cmp_centurion_sec",
    val siteId: String = "",
    
    val visitorType: String = "Guest", // Guest, Family, Delivery, Courier, Vendor, Contractor, Candidate, Emergency, etc.
    val fullName: String = "",
    val mobile: String = "",
    val gender: String = "Male",
    val photoUrl: String? = null,
    val idProofType: String? = null, // Aadhaar, PAN, Driving License
    val idProofNumber: String? = null,
    
    val unitFlatOffice: String = "",
    val hostName: String = "",
    val hostMobile: String = "",
    val purposeOfVisit: String = "",
    val expectedDuration: String = "", 
    
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED, INSIDE, EXITED
    val hostApprovalMethod: String = "Mobile App", 
    
    val qrPassId: String? = null,
    val checkInTime: Long? = null,
    val checkOutTime: Long? = null,
    val guardName: String = "Guard Singh",
    val remarks: String = "",
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class FirestoreVisitorPass(
    val id: String = "",
    val visitorId: String = "",
    val visitorName: String = "",
    val siteId: String = "",
    val hostName: String = "",
    val qrContent: String = "",
    val passType: String = "GUEST_PASS", // Guest Pass, Event Pass, Temporary Pass, Recurring Pass
    val status: String = "ACTIVE", // ACTIVE, EXPIRED, REVOKED, USED
    val validFrom: Long = System.currentTimeMillis(),
    val validUntil: Long = System.currentTimeMillis() + 86400000L
)

data class FirestoreVehicle(
    val id: String = "",
    val vehicleNumber: String = "",
    val vehicleType: String = "Car", // Bike, Car, Truck, Tempo, Van, Taxi, Auto
    val color: String = "",
    val company: String = "",
    val driverName: String = "",
    val driverMobile: String = "",
    val associatedVisitorId: String? = null,
    val checkInTime: Long = System.currentTimeMillis(),
    val checkOutTime: Long? = null
)

data class FirestoreDelivery(
    val id: String = "",
    val orderNumber: String = "",
    val deliveryCompany: String = "Amazon", // Amazon, Flipkart, Swiggy, Zomato, Blinkit, Zepto, BigBasket, Courier, Custom
    val recipientName: String = "",
    val recipientUnit: String = "",
    val checkInTime: Long = System.currentTimeMillis(),
    val checkOutTime: Long? = null,
    val status: String = "INSIDE" // INSIDE, EXITED
)

data class FirestoreContractor(
    val id: String = "",
    val companyName: String = "",
    val supervisorName: String = "",
    val contactNumber: String = "",
    val workPermitType: String = "General", // Electrical, Civil, Painting, Plumbing, Housekeeping, Maintenance
    val permitStart: Long = System.currentTimeMillis(),
    val permitEnd: Long = System.currentTimeMillis() + 86400000L * 7,
    val status: String = "ACTIVE" // ACTIVE, EXPIRED, REVOKED
)

data class FirestoreContractorWorker(
    val id: String = "",
    val contractorId: String = "",
    val companyName: String = "",
    val workerName: String = "",
    val mobile: String = "",
    val idProofType: String = "",
    val idProofNumber: String = "",
    val checkInTime: Long? = null,
    val checkOutTime: Long? = null,
    val status: String = "OUT" // INSIDE, OUT
)

data class FirestoreBlacklistEntry(
    val id: String = "",
    val type: String = "VISITOR", // VISITOR, CONTRACTOR, VEHICLE, VENDOR, DELIVERY
    val targetValue: String = "", // Mobile, vehicle plate or company to match
    val targetName: String = "",
    val reason: String = "",
    val createdBy: String = "Supervisor Admin",
    val createdAt: Long = System.currentTimeMillis(),
    val expiryAt: Long? = null
)

data class FirestoreEvacuationEntry(
    val id: String = "",
    val name: String = "",
    val type: String = "", // VISITOR, CONTRACTOR_WORKER, DELIVERY
    val detail: String = "", 
    val inboundVehicle: String? = null,
    val checkInTime: Long = System.currentTimeMillis()
)

data class FirestoreVisitorReport(
    val id: String = "",
    val type: String = "DAILY", // DAILY, MONTHLY, ATTENDEES, EVACUATION, BLACKLIST, VEHICLE
    val generatedBy: String = "Guard Singh",
    val timestamp: Long = System.currentTimeMillis(),
    val format: String = "PDF", // PDF, EXCEL, CSV
    val contentSummary: String = "",
    val downloadUrl: String = ""
)

