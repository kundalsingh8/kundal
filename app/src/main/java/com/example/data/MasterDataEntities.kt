package com.example.data

import kotlinx.coroutines.flow.MutableStateFlow

// ==========================================
// PHASE 12: Master Data Management (MDM) Platform Types
// ==========================================

data class CompanyMaster(
    val code: String,
    val name: String,
    val groupName: String,
    val headOffice: String,
    val taxId: String,
    val registrationDate: String,
    val status: String // Active, Suspended, Archived
)

data class ClientMaster(
    val code: String,
    val name: String,
    val type: String, // Corporate, Retail, Government, Residential
    val industry: String, // Financial, Tech, Manufacturing, Logistics
    val gst: String,
    val pan: String,
    val contactPhone: String,
    val contactEmail: String,
    val status: String // Lead, Prospect, Active, Inactive, Archived
)

data class SiteMaster(
    val code: String,
    val name: String,
    val type: String, // Commercial, Industrial, Residential
    val clientName: String,
    val address: String,
    val coordinates: String,
    val shiftModel: String, // 24/7 Security, Day Only, 3-Shift Rotational
    val classification: String, // Residential, Commercial, Industrial, Corporate, Hospital, School, Hotel, Mall
    val status: String // Active, Suspended, Archived
)

data class WorkforceMaster(
    val empId: String,
    val name: String,
    val role: String, // Guard, Supervisor, Operations Manager, Auditor
    val kyc: String, // Verified - NID Approved
    val training: String, // Fire Safety Level 2, Crowd Control, SOP Drill A
    val bankDetails: String, // DBS SG 120-4920-11
    val documents: String, // Contract, Work Permit, Medical Certificate
    val status: String // Candidate, Onboarding, Active, Suspended, Exited
)

data class AssetMaster(
    val serialNumber: String,
    val category: String, // Patrol Device, Mobile Device, Walkie Talkie, QR Stand, Biometric Device, CCTV Device
    val purchaseDate: String,
    val warranty: String, // 1 Year, 2 Years, None
    val status: String // Available, Active, Under Repair, Retired
)

data class ContractMaster(
    val clientCode: String,
    val contractCode: String,
    val startDate: String,
    val endDate: String,
    val value: String, // Annual SLA Value
    val billingRules: String, // Pay on 5th, Net-30, Bi-Weekly Invoice schedule
    val status: String // Active, Expiring, Renewing
)

data class RateCardMaster(
    val grade: String, // Grade A, Grade B, Grade C
    val dayRate: String,
    val nightRate: String,
    val otRate: String,
    val guardBilling: String,
    val supervisorBilling: String,
    val reliefBilling: String
)

data class PayrollMaster(
    val structureName: String,
    val basicSalary: String,
    val pfRules: String, // 12% standard, Employer matched
    val esicRules: String, // 3.25% Contribution, Direct
    val tdsRules: String // Tax bracket auto-withholding
)

data class BillingMaster(
    val templateName: String,
    val gstRules: String, // SG Trade Standard GST 8%
    val paymentTerms: String, // Net 30, COD
    val creditTerms: String // Credit Limit $25,000 max
)

data class CommunicationMaster(
    val channel: String, // SMS, Email, WhatsApp, Push Notifications
    val templateName: String,
    val body: String,
    val triggerEvent: String // Triggering incident alerts
)

data class ComplianceMaster(
    val agreementCode: String,
    val name: String,
    val sopRef: String,
    val complianceIndex: String, // regional rules compliance index
    val version: String,
    val expiry: String,
    val renewalCycle: String // Annual, Semi-Annual
)

data class ApprovalRequest(
    val id: String,
    val moduleName: String, // e.g. "Clients", "Workforce"
    val actionType: String, // e.g. "Create", "Edit", "Delete"
    val description: String,
    val proposedChange: String, // JSON serialization of the data
    val requestedBy: String,
    val requestedAt: Long,
    val status: String // Pending, Approved, Rejected
)
