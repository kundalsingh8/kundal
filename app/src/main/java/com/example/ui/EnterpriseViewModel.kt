package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class EnterpriseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SecurityRepository
    val users: StateFlow<List<User>>
    val sites: StateFlow<List<Site>>
    val attendance: StateFlow<List<Attendance>>
    val incidents: StateFlow<List<Incident>>
    val sosAlerts: StateFlow<List<SOSAlert>>
    val auditLogs: StateFlow<List<AuditLog>>

    // Simulated Active User representing standard roles
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Screen navigation tracking inside the main dashboard shell
    private val _currentSection = MutableStateFlow("Overview")
    val currentSection: StateFlow<String> = _currentSection.asStateFlow()

    // Temporary validation messages
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // ==========================================
    // PHASE 12: CRM & ERP Master Data Registries
    // ==========================================
    private val _companiesMaster = MutableStateFlow<List<CompanyMaster>>(listOf(
        CompanyMaster("cmp_centurion_sec", "Centurion Security Services Private Ltd", "Kundal Global Holding Group", "8 Marina Boulevard, Marina Bay Tower 1, Singapore", "GST-SG-2026849F-V3", "2024-03-12", "Active"),
        CompanyMaster("cmp_shield_guard", "ShieldGuard Protections", "Kundal Global Group", "150 Orchard Road, Orchard Plaza, Singapore", "GST-SG-2022881X", "2025-01-08", "Active")
    ))
    val companiesMaster: StateFlow<List<CompanyMaster>> = _companiesMaster.asStateFlow()

    private val _clientsMaster = MutableStateFlow<List<ClientMaster>>(listOf(
        ClientMaster("CL-MOP", "Maritime Operations Plaza Ltd", "Corporate", "Logistics", "GST-SG-238491A", "PAN-SG8019X", "+65 6839 2200", "procurement@maritimeplaza.com", "Active"),
        ClientMaster("CL-RAP", "Raffles Apex Retail Consortium", "Retail", "Services", "GST-SG-228192B", "PAN-SG9922P", "+65 6555 4910", "billing@rafflesapex.com", "Active"),
        ClientMaster("CL-COV", "The Cove Condominium HOA", "Residential", "HOA Service", "GST-SG-218392C", "PAN-SG0918L", "+65 6444 8820", "committee@thecovehoa.org", "Active"),
        ClientMaster("CL-TEM", "Temasek Aviation Hub", "Government", "Logistics", "GST-SG-202543Z", "PAN-SG5432X", "+65 6111 2222", "contracts@temasekaviation.gov", "Prospect")
    ))
    val clientsMaster: StateFlow<List<ClientMaster>> = _clientsMaster.asStateFlow()

    private val _sitesMaster = MutableStateFlow<List<SiteMaster>>(listOf(
        SiteMaster("ST-MOP01", "Maritime Plaza HQ", "Commercial", "Maritime Operations Plaza Ltd", "10 Marina Boulevard, Singapore", "1.2789, 103.8543", "24/7 Security", "Commercial", "Active"),
        SiteMaster("ST-RAP02", "Raffles Apex Mall", "Commercial", "Raffles Apex Retail Consortium", "250 Orchard Road, Singapore", "1.3005, 103.8324", "12-Hr Shift Rotational", "Mall", "Active"),
        SiteMaster("ST-COV01", "The Cove Tower A & B", "Residential", "The Cove Condominium HOA", "100 Tanjong Rhu Road, Singapore", "1.2982, 103.8821", "24-Hr Gate Check", "Residential", "Active"),
        SiteMaster("ST-IND04", "Tuas Logistics Yard 4", "Industrial", "Maritime Operations Plaza Ltd", "88 Tuas South Avenue 2, Singapore", "1.3190, 103.6212", "24/7 Security Plus Dog", "Industrial", "Active")
    ))
    val sitesMaster: StateFlow<List<SiteMaster>> = _sitesMaster.asStateFlow()

    private val _workforceMaster = MutableStateFlow<List<WorkforceMaster>>(listOf(
        WorkforceMaster("EMP-001", "Vikram Singh Chaudhary", "Operations Manager", "Verified - NID Approved", "VIP Tactics, Crowd Level 3", "DBS SG 120-4920-11", "Employment Contract V3.pdf", "Active"),
        WorkforceMaster("EMP-002", "Lim Ah San", "Supervisor", "Verified - NID Approved", "Fire Safety Level 2, First Aid", "OCBC SG 082-9901-22", "First Aid Cert 2026.pdf", "Active"),
        WorkforceMaster("EMP-003", "Karan Johar", "Guard", "Verified - NID Approved", "Basic Onboarding Drill", "POSB SG 190-2810-99", "Security License A.pdf", "Active"),
        WorkforceMaster("EMP-004", "Sarah Tan", "Auditor", "Verified - NID Approved", "PDPA Compliance, ISO9001", "UOB SG 120-2210-09", "Auditing Framework V2.pdf", "Active"),
        WorkforceMaster("EMP-005", "Rajesh Kumar", "Guard", "Police Check Verified", "Basic Onboarding Drill", "POSB SG 111-2334-55", "Police Check Receipt.pdf", "Onboarding")
    ))
    val workforceMaster: StateFlow<List<WorkforceMaster>> = _workforceMaster.asStateFlow()

    private val _assetsMaster = MutableStateFlow<List<AssetMaster>>(listOf(
        AssetMaster("SN-PT-901", "Patrol Device", "2025-05-10", "2 Years", "Active"),
        AssetMaster("SN-PT-902", "Patrol Device", "2025-05-15", "2 Years", "Available"),
        AssetMaster("SN-WT-011", "Walkie Talkie", "2026-01-20", "1 Year", "Active"),
        AssetMaster("SN-QR-2101", "QR Stand", "2025-11-30", "None", "Active"),
        AssetMaster("SN-BIO-441", "Biometric Device", "2024-12-01", "1 Year", "Under Repair"),
        AssetMaster("SN-CTV-880", "CCTV Device", "2025-02-14", "3 Years", "Active")
    ))
    val assetsMaster: StateFlow<List<AssetMaster>> = _assetsMaster.asStateFlow()

    private val _contractsMaster = MutableStateFlow<List<ContractMaster>>(listOf(
        ContractMaster("CL-MOP", "CON-2026-MOP", "2026-01-01", "2027-12-31", "$180,000 / Yr", "Standard SLA, Net 30, Auto-billed", "Active"),
        ContractMaster("CL-RAP", "CON-2026-RAP", "2026-03-15", "2027-03-14", "$95,000 / Yr", "Invoices due on 5th Business Day", "Active"),
        ContractMaster("CL-COV", "CON-2025-COV", "2025-07-01", "2026-06-30", "$120,000 / Yr", "Bi-Weekly billing cycles", "Expiring")
    ))
    val contractsMaster: StateFlow<List<ContractMaster>> = _contractsMaster.asStateFlow()

    private val _ratesMaster = MutableStateFlow<List<RateCardMaster>>(listOf(
        RateCardMaster("Grade A (Elite)", "$28.00 / Hr", "$34.00 / Hr", "$42.00 / Hr", "$38.00 / Hr", "$45.00 / Hr", "$40.00 / Hr"),
        RateCardMaster("Grade B (Standard)", "$22.50 / Hr", "$27.00 / Hr", "$33.75 / Hr", "$30.00 / Hr", "$36.00 / Hr", "$32.00 / Hr"),
        RateCardMaster("Grade C (Relief)", "$18.00 / Hr", "$21.50 / Hr", "$27.00 / Hr", "$24.00 / Hr", "$29.00 / Hr", "$26.00 / Hr")
    ))
    val ratesMaster: StateFlow<List<RateCardMaster>> = _ratesMaster.asStateFlow()

    private val _payrollMaster = MutableStateFlow<List<PayrollMaster>>(listOf(
        PayrollMaster("Standard Executive", "$5,500.00 / Mo", "12% standard, Employer matched", "ESIC Not Applicable", "TDS Auto Scale Tax Bracket"),
        PayrollMaster("Standard Hourly Guard", "$22.50 / Hr Base", "12% matched CPF", "3.25% Contribution applicable", "TDS Flat 10% Withholding")
    ))
    val payrollMaster: StateFlow<List<PayrollMaster>> = _payrollMaster.asStateFlow()

    private val _billingMaster = MutableStateFlow<List<BillingMaster>>(listOf(
        BillingMaster("SLA Dynamic Invoice", "SG Trade Standard GST 8%", "Net 30 Invoicing Calendar", "Credit Limit $50,000 max"),
        BillingMaster("HOA Fixed Recurring", "SG Trade Standard GST 8%", "Prepaid Monthly HOA terms", "Credit Limit $10,000 max")
    ))
    val billingMaster: StateFlow<List<BillingMaster>> = _billingMaster.asStateFlow()

    private val _templatesMaster = MutableStateFlow<List<CommunicationMaster>>(listOf(
        CommunicationMaster("SMS", "Emergency SOS Notification", "⚠️ ALERT: SOS triggered at [SiteName] by [TriggererName]. Dispatching units.", "SOS Broadcaster Triggered"),
        CommunicationMaster("WhatsApp", "Shift Checklist Automated", "Welcome to your shift [GuardName] at [SiteName]. Please scan QR checkpoint 1 to begin.", "Attendance Check-In"),
        CommunicationMaster("Email", "Critical Incident Management Escalation", "Dear Client, a [Severity] incident '[Title]' has been reported. Details: [Details].", "Incident Recorded")
    ))
    val templatesMaster: StateFlow<List<CommunicationMaster>> = _templatesMaster.asStateFlow()

    private val _complianceMaster = MutableStateFlow<List<ComplianceMaster>>(listOf(
        ComplianceMaster("CMP-PSARA-08", "PSARA SG Security License", "OP-SOP-011", "Section 18.2 Registration Requirements", "V4.2", "2028-12-31", "Annual"),
        ComplianceMaster("CMP-ISO9001", "Quality Management Standard Certificate", "QA-SOP-50", "Clause 8.1 Performance Review", "V2.0", "2027-06-15", "Annual")
    ))
    val complianceMaster: StateFlow<List<ComplianceMaster>> = _complianceMaster.asStateFlow()

    private val _approvalRequests = MutableStateFlow<List<ApprovalRequest>>(listOf(
        ApprovalRequest("APP-094", "Rates", "Edit", "Propose otRate hike on Standard Grade B to $35.00", "{\"grade\":\"Grade B (Standard)\",\"otRate\":\"$35.00 / Hr\"}", "Lim Ah San", System.currentTimeMillis() - 7200000, "Pending"),
        ApprovalRequest("APP-095", "Clients", "Create", "Sponsor onboard on Temasek Aviation Hub (Prospect Status)", "{\"name\":\"Temasek Aviation Hub\",\"type\":\"Government\"}", "Vikram Singh Chaudhary", System.currentTimeMillis() - 3600000, "Pending")
    ))
    val approvalRequests: StateFlow<List<ApprovalRequest>> = _approvalRequests.asStateFlow()

    // ==========================================
    // PHASE 12: MDM Operations Actions
    // ==========================================
    fun submitMasterApprovalProposed(moduleName: String, actionType: String, description: String, proposedChangeJson: String) {
        val user = _currentUser.value ?: return
        val reqId = "APP-" + UUID.randomUUID().toString().take(4).uppercase()
        val newReq = ApprovalRequest(
            id = reqId,
            moduleName = moduleName,
            actionType = actionType,
            description = description,
            proposedChange = proposedChangeJson,
            requestedBy = user.name.ifEmpty { "super_admin_system" },
            requestedAt = System.currentTimeMillis(),
            status = "Pending"
        )
        _approvalRequests.update { listOf(newReq) + it }
        logSystemAudit(
            action = "MD_PROPOSAL_SUBMITTED",
            details = "Proposed $actionType on module $moduleName: $description",
            oldValue = "None",
            newValue = "Pending authorization in pipeline $reqId"
        )
        showToast("Proposed master modification: submitted request $reqId")
    }

    fun approveRequest(id: String) {
        val user = _currentUser.value ?: return
        val current = _approvalRequests.value
        val request = current.find { it.id == id } ?: return
        
        _approvalRequests.value = current.map {
            if (it.id == id) it.copy(status = "Approved") else it
        }

        // Apply state modification
        when (request.moduleName) {
            "Clients" -> {
                val list = _clientsMaster.value.toMutableList()
                list.add(ClientMaster("CL-TEM", "Temasek Aviation Hub", "Government", "Logistics", "GST-SG-202543Z", "PAN-SG5432X", "+65 6111 2222", "contracts@temasekaviation.gov", "Active"))
                _clientsMaster.value = list
            }
            "Rates" -> {
                _ratesMaster.value = _ratesMaster.value.map {
                    if (it.grade.contains("Grade B")) it.copy(otRate = "$35.00 / Hr") else it
                }
            }
        }

        logSystemAudit(
            action = "MD_PROPOSAL_APPROVED",
            details = "Approved request $id: ${request.description}",
            oldValue = "Pending",
            newValue = "Active Master Record Applied (Who: ${user.name})"
        )
        showToast("Workflow Approved: Authorized record state change for ${request.moduleName}")
    }

    fun rejectRequest(id: String) {
        val user = _currentUser.value ?: return
        val current = _approvalRequests.value
        
        _approvalRequests.value = current.map {
            if (it.id == id) it.copy(status = "Rejected") else it
        }

        logSystemAudit(
            action = "MD_PROPOSAL_REJECTED",
            details = "Rejected request $id: proposed changes canceled.",
            oldValue = "Pending",
            newValue = "Rejected by ${user.name}"
        )
        showToast("Workflow Cancelled: Rejected proposal $id.")
    }

    fun addCompanyMasterDirect(company: CompanyMaster) {
        _companiesMaster.update { it + company }
        logSystemAudit("MD_COMPANY_CREATED", "Central registry added company ${company.name} in state ${company.status}", "None", company.code)
    }

    fun addClientMasterDirect(client: ClientMaster) {
        _clientsMaster.update { it + client }
        logSystemAudit("MD_CLIENT_CREATED", "Add client registry record ${client.name} under ${client.code}", "None", client.status)
    }

    fun addSiteMasterDirect(site: SiteMaster) {
        _sitesMaster.update { it + site }
        logSystemAudit("MD_SITE_CREATED", "Add central site registry ${site.name} mapped to ${site.clientName}", "None", site.status)
    }

    fun addWorkforceMasterDirect(worker: WorkforceMaster) {
        _workforceMaster.update { it + worker }
        logSystemAudit("MD_WORKFORCE_CREATED", "Add workforce staff record ${worker.name} ID: ${worker.empId}", "None", worker.status)
    }

    fun addAssetMasterDirect(asset: AssetMaster) {
        _assetsMaster.update { it + asset }
        logSystemAudit("MD_ASSET_CREATED", "Add hardware asset ${asset.category} serial: ${asset.serialNumber}", "None", asset.status)
    }

    fun addContractMasterDirect(contract: ContractMaster) {
        _contractsMaster.update { it + contract }
        logSystemAudit("MD_CONTRACT_CREATED", "Bind customer contract ${contract.contractCode} to Client ${contract.clientCode}", "None", contract.status)
    }

    fun addRateCardMasterDirect(rate: RateCardMaster) {
        _ratesMaster.update { it + rate }
        logSystemAudit("MD_RATECARD_CREATED", "Create standard payroll grade rates card for ${rate.grade}", "None", rate.otRate)
    }

    fun addPayrollMasterDirect(payroll: PayrollMaster) {
        _payrollMaster.update { it + payroll }
        logSystemAudit("MD_PAYROLL_CREATED", "Configure standard payroll salary structure: ${payroll.structureName}", "None", payroll.basicSalary)
    }

    fun addBillingMasterDirect(billing: BillingMaster) {
        _billingMaster.update { it + billing }
        logSystemAudit("MD_BILLING_CREATED", "Configure billing terms template: ${billing.templateName}", "None", billing.paymentTerms)
    }

    fun addCommunicationMasterDirect(comm: CommunicationMaster) {
        _templatesMaster.update { it + comm }
        logSystemAudit("MD_TEMPLATE_CREATED", "Add auto dispatch communication template: ${comm.templateName}", "None", comm.channel)
    }

    fun addComplianceMasterDirect(comp: ComplianceMaster) {
        _complianceMaster.update { it + comp }
        logSystemAudit("MD_COMPLIANCE_CREATED", "Log statutory compliance rules docket for ${comp.name}", "None", comp.expiry)
    }

    // ==========================================
    // CHECKPOINT 1: MULTI-TENANT FIRESTORE SIMULATED CLIENT
    // Security Boundaries by tenantId & companyId
    // ==========================================
    private val _firestoreCompanies = MutableStateFlow<List<FirestoreCompany>>(emptyList())
    val firestoreCompanies: StateFlow<List<FirestoreCompany>> = _firestoreCompanies.asStateFlow()

    private val _firestoreClients = MutableStateFlow<List<FirestoreClient>>(emptyList())
    val firestoreClients: StateFlow<List<FirestoreClient>> = _firestoreClients.asStateFlow()

    private val _firestoreSites = MutableStateFlow<List<FirestoreSite>>(emptyList())
    val firestoreSites: StateFlow<List<FirestoreSite>> = _firestoreSites.asStateFlow()

    private val _firestoreUsers = MutableStateFlow<List<FirestoreUser>>(emptyList())
    val firestoreUsers: StateFlow<List<FirestoreUser>> = _firestoreUsers.asStateFlow()

    private val _firestoreAuditLogs = MutableStateFlow<List<FirestoreAuditLog>>(emptyList())
    val firestoreAuditLogs: StateFlow<List<FirestoreAuditLog>> = _firestoreAuditLogs.asStateFlow()

    // Phase 15 state flows
    private val _firestoreEmployees = MutableStateFlow<List<FirestoreEmployee>>(emptyList())
    val firestoreEmployees: StateFlow<List<FirestoreEmployee>> = _firestoreEmployees.asStateFlow()

    private val _firestoreEmployeeDocuments = MutableStateFlow<List<FirestoreEmployeeDocument>>(emptyList())
    val firestoreEmployeeDocuments: StateFlow<List<FirestoreEmployeeDocument>> = _firestoreEmployeeDocuments.asStateFlow()

    private val _firestoreEmployeeTraining = MutableStateFlow<List<FirestoreEmployeeTraining>>(emptyList())
    val firestoreEmployeeTraining: StateFlow<List<FirestoreEmployeeTraining>> = _firestoreEmployeeTraining.asStateFlow()

    private val _firestoreEmployeeDeployments = MutableStateFlow<List<FirestoreEmployeeDeployment>>(emptyList())
    val firestoreEmployeeDeployments: StateFlow<List<FirestoreEmployeeDeployment>> = _firestoreEmployeeDeployments.asStateFlow()

    private val _firestoreEmployeeTransfers = MutableStateFlow<List<FirestoreEmployeeTransfer>>(emptyList())
    val firestoreEmployeeTransfers: StateFlow<List<FirestoreEmployeeTransfer>> = _firestoreEmployeeTransfers.asStateFlow()

    private val _firestoreEmployeeEquipment = MutableStateFlow<List<FirestoreEmployeeEquipment>>(emptyList())
    val firestoreEmployeeEquipment: StateFlow<List<FirestoreEmployeeEquipment>> = _firestoreEmployeeEquipment.asStateFlow()

    private val _firestoreEmployeeLeaveProfiles = MutableStateFlow<List<FirestoreEmployeeLeaveProfile>>(emptyList())
    val firestoreEmployeeLeaveProfiles: StateFlow<List<FirestoreEmployeeLeaveProfile>> = _firestoreEmployeeLeaveProfiles.asStateFlow()

    private val _firestoreEmployeePayrollProfiles = MutableStateFlow<List<FirestoreEmployeePayrollProfile>>(emptyList())
    val firestoreEmployeePayrollProfiles: StateFlow<List<FirestoreEmployeePayrollProfile>> = _firestoreEmployeePayrollProfiles.asStateFlow()

    // Phase 16: Attendance & Shift Management state flows
    private val _firestoreAttendanceRecords = MutableStateFlow<List<FirestoreAttendanceRecord>>(emptyList())
    val firestoreAttendanceRecords: StateFlow<List<FirestoreAttendanceRecord>> = _firestoreAttendanceRecords.asStateFlow()

    private val _firestoreAttendanceSessions = MutableStateFlow<List<FirestoreAttendanceSession>>(emptyList())
    val firestoreAttendanceSessions: StateFlow<List<FirestoreAttendanceSession>> = _firestoreAttendanceSessions.asStateFlow()

    private val _firestoreAttendanceExceptions = MutableStateFlow<List<FirestoreAttendanceException>>(emptyList())
    val firestoreAttendanceExceptions: StateFlow<List<FirestoreAttendanceException>> = _firestoreAttendanceExceptions.asStateFlow()

    private val _firestoreAttendanceCorrections = MutableStateFlow<List<FirestoreAttendanceCorrection>>(emptyList())
    val firestoreAttendanceCorrections: StateFlow<List<FirestoreAttendanceCorrection>> = _firestoreAttendanceCorrections.asStateFlow()

    private val _firestoreAttendanceConfigs = MutableStateFlow<List<FirestoreAttendanceConfig>>(emptyList())
    val firestoreAttendanceConfigs: StateFlow<List<FirestoreAttendanceConfig>> = _firestoreAttendanceConfigs.asStateFlow()

    private val _firestoreAttendanceQrCodes = MutableStateFlow<List<FirestoreAttendanceQrCode>>(emptyList())
    val firestoreAttendanceQrCodes: StateFlow<List<FirestoreAttendanceQrCode>> = _firestoreAttendanceQrCodes.asStateFlow()

    private val _firestoreAttendanceReports = MutableStateFlow<List<FirestoreAttendanceReport>>(emptyList())
    val firestoreAttendanceReports: StateFlow<List<FirestoreAttendanceReport>> = _firestoreAttendanceReports.asStateFlow()

    // Phase 17: Visitor, Blacklist, Contractor and Delivery flows
    private val _visitors = MutableStateFlow<List<FirestoreVisitor>>(emptyList())
    val visitors: StateFlow<List<FirestoreVisitor>> = _visitors.asStateFlow()

    private val _visitorPasses = MutableStateFlow<List<FirestoreVisitorPass>>(emptyList())
    val visitorPasses: StateFlow<List<FirestoreVisitorPass>> = _visitorPasses.asStateFlow()

    private val _vehicles = MutableStateFlow<List<FirestoreVehicle>>(emptyList())
    val vehicles: StateFlow<List<FirestoreVehicle>> = _vehicles.asStateFlow()

    private val _deliveries = MutableStateFlow<List<FirestoreDelivery>>(emptyList())
    val deliveries: StateFlow<List<FirestoreDelivery>> = _deliveries.asStateFlow()

    private val _contractors = MutableStateFlow<List<FirestoreContractor>>(emptyList())
    val contractors: StateFlow<List<FirestoreContractor>> = _contractors.asStateFlow()

    private val _contractorWorkers = MutableStateFlow<List<FirestoreContractorWorker>>(emptyList())
    val contractorWorkers: StateFlow<List<FirestoreContractorWorker>> = _contractorWorkers.asStateFlow()

    private val _blacklist = MutableStateFlow<List<FirestoreBlacklistEntry>>(emptyList())
    val blacklist: StateFlow<List<FirestoreBlacklistEntry>> = _blacklist.asStateFlow()

    private val _evacuationRegister = MutableStateFlow<List<FirestoreEvacuationEntry>>(emptyList())
    val evacuationRegister: StateFlow<List<FirestoreEvacuationEntry>> = _evacuationRegister.asStateFlow()

    private val _visitorReports = MutableStateFlow<List<FirestoreVisitorReport>>(emptyList())
    val visitorReports: StateFlow<List<FirestoreVisitorReport>> = _visitorReports.asStateFlow()


    fun onboardFirestoreEmployee(
        employee: FirestoreEmployee,
        documents: FirestoreEmployeeDocument,
        training: FirestoreEmployeeTraining,
        deployment: FirestoreEmployeeDeployment?,
        equipment: FirestoreEmployeeEquipment,
        leave: FirestoreEmployeeLeaveProfile,
        payroll: FirestoreEmployeePayrollProfile
    ) {
        _firestoreEmployees.update { it + employee }
        _firestoreEmployeeDocuments.update { it + documents }
        _firestoreEmployeeTraining.update { it + training }
        if (deployment != null) {
            _firestoreEmployeeDeployments.update { it + deployment }
        }
        _firestoreEmployeeEquipment.update { it + equipment }
        _firestoreEmployeeLeaveProfiles.update { it + leave }
        _firestoreEmployeePayrollProfiles.update { it + payroll }

        // Sync with standard master workforce registry for full functional backward parity!
        val standardWorker = WorkforceMaster(
            empId = employee.id,
            name = employee.fullName,
            role = employee.employeeType,
            kyc = "Aadhaar: ${employee.aadhaar.ifEmpty { "Pending" }} | Police: ${employee.policeVerificationStatus}",
            training = "PSARA: ${training.psaraTraining} | Fire Safety: ${training.fireSafety}",
            bankDetails = "${employee.bankName} ${employee.accountNumber}",
            documents = "Verification Checklist Readiness: ${employee.readinessScore}%",
            status = employee.status
        )
        addWorkforceMasterDirect(standardWorker)

        // Write Audit Log
        val logId = "log_" + UUID.randomUUID().toString().take(6)
        val logEntry = FirestoreAuditLog(
            id = logId,
            tenantId = employee.tenantId,
            companyId = employee.companyId,
            action = "EMPLOYEE_ONBOARDED",
            details = "Completed Phase 15 Onboarding for ${employee.fullName} (${employee.id}). Readiness Score: ${employee.readinessScore}%. Status: ${employee.status}",
            performedBy = _currentUser.value?.name ?: "System Onboarder",
            oldValue = "None",
            newValue = "Active (Deployed to: ${deployment?.siteName ?: "Relief Pool"})"
        )
        _firestoreAuditLogs.update { it + logEntry }
        showToast("Onboarded ${employee.fullName} under ID ${employee.id}")
    }

    fun transferEmployee(
        employeeId: String,
        toSiteId: String,
        toSiteName: String,
        toShift: String,
        toPosition: String,
        reason: String
    ) {
        val employee = _firestoreEmployees.value.find { it.id == employeeId } ?: return
        val currentDeployment = _firestoreEmployeeDeployments.value.find { it.employeeId == employeeId }
        
        val fromSiteId = currentDeployment?.siteId ?: ""
        val fromSiteName = currentDeployment?.siteName ?: "Relief Pool"
        val fromShift = currentDeployment?.shiftName ?: "None"
        val fromPosition = currentDeployment?.position ?: "None"

        // Update Deployment Record
        val updatedDeployment = FirestoreEmployeeDeployment(
            id = currentDeployment?.id ?: ("dpl_" + UUID.randomUUID().toString().take(6)),
            tenantId = employee.tenantId,
            companyId = employee.companyId,
            employeeId = employeeId,
            siteId = toSiteId,
            siteName = toSiteName,
            shiftName = toShift,
            position = toPosition,
            reportingSupervisor = "System Team Coordinator"
        )

        _firestoreEmployeeDeployments.update { old ->
            if (old.any { it.employeeId == employeeId }) {
                old.map { if (it.employeeId == employeeId) updatedDeployment else it }
            } else {
                old + updatedDeployment
            }
        }

        // Update employee record status
        _firestoreEmployees.update { old ->
            old.map { 
                if (it.id == employeeId) {
                    it.copy(
                        status = "Transferred", 
                        reportingManager = "Coordinator (Site: $toSiteName)",
                        updatedAt = System.currentTimeMillis()
                    ) 
                } else it 
            }
        }

        // Add to Transfer Ledger Collection
        val transferId = "trf_" + UUID.randomUUID().toString().take(6)
        val transferRecord = FirestoreEmployeeTransfer(
            id = transferId,
            tenantId = employee.tenantId,
            companyId = employee.companyId,
            employeeId = employeeId,
            fromSiteId = fromSiteId,
            fromSiteName = fromSiteName,
            toSiteId = toSiteId,
            toSiteName = toSiteName,
            fromShift = fromShift,
            toShift = toShift,
            fromPosition = fromPosition,
            toPosition = toPosition,
            transferDate = "2026-06-22",
            reason = reason
        )
        _firestoreEmployeeTransfers.update { it + transferRecord }

        // Add to standard master audits
        logSystemAudit(
            action = "EMPLOYEE_TRANSFERRED",
            details = "Transferred ${employee.fullName} from $fromSiteName to $toSiteName. Reason: $reason",
            oldValue = fromSiteName,
            newValue = toSiteName
        )

        // Sync WorkforceMaster state
        _workforceMaster.update { old ->
            old.map {
                if (it.empId == employeeId) {
                    it.copy(status = "Transferred")
                } else it
            }
        }

        showToast("Successfully transferred employee ${employee.fullName} to $toSiteName")
    }

    fun exitEmployee(employeeId: String, reason: String) {
        val employee = _firestoreEmployees.value.find { it.id == employeeId } ?: return
        
        _firestoreEmployees.update { old ->
            old.map { if (it.id == employeeId) it.copy(status = "Exited", updatedAt = System.currentTimeMillis()) else it }
        }

        // De-assign Deployment Record
        _firestoreEmployeeDeployments.update { old ->
            old.filter { it.employeeId != employeeId }
        }

        // Add Transfer/Exit Ledger Log
        logSystemAudit(
            action = "EMPLOYEE_EXITED",
            details = "Service termination of ${employee.fullName} ($employeeId). Reason: $reason",
            oldValue = "Active",
            newValue = "Exited"
        )

        // Sync WorkforceMaster state
        _workforceMaster.update { old ->
            old.map {
                if (it.empId == employeeId) {
                    it.copy(status = "Exited")
                } else it
            }
        }

        showToast("Employee with ID $employeeId is discharged and deactivated.")
    }

    fun suspendEmployee(employeeId: String) {
        val employee = _firestoreEmployees.value.find { it.id == employeeId } ?: return
        
        _firestoreEmployees.update { old ->
            old.map { if (it.id == employeeId) it.copy(status = "Suspended", updatedAt = System.currentTimeMillis()) else it }
        }

        logSystemAudit(
            action = "EMPLOYEE_SUSPENDED",
            details = "Temporary administrative suspension order on ${employee.fullName} ($employeeId)",
            oldValue = "Active",
            newValue = "Suspended"
        )

        _workforceMaster.update { old ->
            old.map {
                if (it.empId == employeeId) {
                    it.copy(status = "Suspended")
                } else it
            }
        }

        showToast("Employee with ID $employeeId has been suspended.")
    }

    fun onboardFirestoreClient(client: FirestoreClient) {
        _firestoreClients.update { it + client }
        
        // Also log to firestore audit_logs collection
        val logId = "log_" + UUID.randomUUID().toString().take(6)
        val logEntry = FirestoreAuditLog(
            id = logId,
            tenantId = client.tenantId,
            companyId = client.companyId,
            action = "CLIENT_ONBOARD_WIZARD",
            details = "Completed Phase 14 Client Onboarding System for ${client.clientName} (Code: ${client.clientCode}) with Escalation and Contract Mapping.",
            performedBy = _currentUser.value?.name ?: "System Onboarder",
            oldValue = "None",
            newValue = "Client Onboarded (Status: ${client.status.uppercase()})"
        )
        _firestoreAuditLogs.update { it + logEntry }
        
        // Also update standard master clients as well for full system parity!
        val standardClient = ClientMaster(
            code = client.clientCode,
            name = client.clientName,
            type = client.clientType.replaceFirstChar { it.uppercase() },
            industry = "Security Contracted",
            gst = client.gstNumber ?: "Pending",
            pan = "PAN-AUTO-PL",
            contactPhone = client.mobile,
            contactEmail = client.email,
            status = client.status.replaceFirstChar { it.uppercase() }
        )
        addClientMasterDirect(standardClient)
        
        showToast("Successfully onboarded secure multi-tenant Client: ${client.clientName}")
    }

    fun updateFirestoreClient(client: FirestoreClient) {
        _firestoreClients.update { oldList ->
            oldList.map { if (it.id == client.id) client else it }
        }
    }

    fun logFirestoreAudit(tenantId: String, companyId: String, action: String, details: String, oldValue: String, newValue: String) {
        val logId = "log_" + UUID.randomUUID().toString().take(6)
        val logEntry = FirestoreAuditLog(
            id = logId,
            tenantId = tenantId,
            companyId = companyId,
            action = action,
            details = details,
            performedBy = _currentUser.value?.name ?: "System Operator",
            oldValue = oldValue,
            newValue = newValue,
            createdAt = System.currentTimeMillis()
        )
        _firestoreAuditLogs.update { it + logEntry }
    }

    // Helper functions representing secure multi-tenant queries: `.where("tenantId", "==", tenantId)`
    fun getCompaniesForTenant(tenantId: String): List<FirestoreCompany> {
        return _firestoreCompanies.value.filter { it.tenantId == tenantId }
    }

    fun getClientsForTenant(tenantId: String): List<FirestoreClient> {
        return _firestoreClients.value.filter { it.tenantId == tenantId }
    }

    fun getSitesForTenant(tenantId: String): List<FirestoreSite> {
        return _firestoreSites.value.filter { it.tenantId == tenantId }
    }

    fun getUsersForTenant(tenantId: String): List<FirestoreUser> {
        return _firestoreUsers.value.filter { it.tenantId == tenantId }
    }

    fun getAuditLogsForTenant(tenantId: String): List<FirestoreAuditLog> {
        return _firestoreAuditLogs.value.filter { it.tenantId == tenantId }
    }

    private fun seedFirestoreMockData() {
        val defaultTenant = "tenant_singh_sec"
        val defaultCompany = "cmp_centurion_sec"

        _firestoreCompanies.value = listOf(
            FirestoreCompany(
                id = defaultCompany,
                companyCode = "CENT",
                companyName = "Centurion Security Services Private Ltd",
                legalCompanyName = "Centurion Security Services Private Ltd",
                companyType = "private_limited",
                businessCategory = "security_facility",
                companyGroupId = "grp_kundal",
                tenantId = defaultTenant,
                panNumber = "PAN849201L",
                gstEnabled = true,
                gstNumber = "GST-SG-2026849F-V3",
                cinNumber = "CIN749102X",
                address = FirestoreAddress(
                    line1 = "8 Marina Boulevard",
                    area = "Marina Bay Ring",
                    city = "Singapore",
                    state = "Singapore",
                    pincode = "018981",
                    country = "Singapore"
                ),
                contact = FirestoreContact(
                    name = "Vikram Singh Chaudhary",
                    mobile = "+65 9283 5029",
                    email = "vikram.singh@centurionsec.com"
                ),
                subscriptionPlan = "enterprise",
                status = "active"
            )
        )

        _firestoreClients.value = listOf(
            FirestoreClient(
                id = "CL-MOP",
                tenantId = defaultTenant,
                companyId = defaultCompany,
                clientCode = "MOP",
                clientName = "Maritime Operations Plaza Ltd",
                clientType = "corporate",
                contactPerson = "Douglas Lim",
                mobile = "+65 9182 7492",
                email = "procurement@maritimeplaza.com",
                designation = "Procurement Lead Director",
                escalationMatrix = FirestoreEscalationMatrix(
                    level1 = "Douglas Lim (+65 9182 7492)",
                    level2 = "Operational Support (+65 6839 2200)",
                    level3 = "Management Desk (+65 6839 1100)",
                    emergency = "Command Center (+65 9999 1111)"
                ),
                billingAddress = "10 Marina Boulevard, Singapore",
                gstNumber = "GST-SG-238491A",
                status = "active",
                contractCode = "CON-2026-MOP",
                contractStartDate = "2026-01-01",
                contractEndDate = "2027-12-31",
                contractValuation = "$180,000 / Yr",
                billingRules = "SLA Hourly, Net-30 invoicing terms",
                portalEmail = "douglas.lim@maritimeplaza.com"
            ),
            FirestoreClient(
                id = "CL-RAP",
                tenantId = defaultTenant,
                companyId = defaultCompany,
                clientCode = "RAP",
                clientName = "Raffles Apex Retail Consortium",
                clientType = "mall",
                contactPerson = "Shirley Tan",
                mobile = "+65 6555 4910",
                email = "billing@rafflesapex.com",
                designation = "Retail Finance Head",
                escalationMatrix = FirestoreEscalationMatrix(
                    level1 = "Shirley Tan (+65 6555 4910)",
                    level2 = "Store Emergency (+65 6555 1120)",
                    emergency = "HQ Alert Line (+65 6555 1200)"
                ),
                billingAddress = "250 Orchard Road, Orchard Plaza, Singapore",
                gstNumber = "GST-SG-228192B",
                status = "active",
                contractCode = "CON-2026-RAP",
                contractStartDate = "2026-03-15",
                contractEndDate = "2027-03-14",
                contractValuation = "$95,000 / Yr",
                billingRules = "Invoices due on 5th Business Day",
                portalEmail = "shirley.tan@rafflesapex.com"
            )
        )

        _firestoreSites.value = listOf(
            FirestoreSite(
                id = "ST-MOP01",
                tenantId = defaultTenant,
                companyId = defaultCompany,
                clientId = "CL-MOP",
                siteCode = "MOP01",
                siteName = "Maritime Plaza HQ",
                siteType = "corporate",
                address = FirestoreSiteAddress(
                    line1 = "10 Marina Boulevard",
                    area = "Marina Bay",
                    city = "Singapore",
                    state = "Singapore",
                    pincode = "018981"
                ),
                gps = FirestoreGps(latitude = 1.2789, longitude = 103.8543, geofenceRadius = 150.0),
                modules = FirestoreModules(),
                shiftModel = 3,
                shiftStartTime = "07:00",
                readinessScore = 98.4,
                status = "active"
            )
        )

        _firestoreUsers.value = listOf(
            FirestoreUser(
                id = "usr_super_admin",
                tenantId = defaultTenant,
                companyId = defaultCompany,
                fullName = "Karan Singh",
                mobile = "+65 8888 7777",
                email = "superadmin@securityos.com",
                role = "super_admin",
                status = "active"
            ),
            FirestoreUser(
                id = "usr_client_mgr",
                tenantId = defaultTenant,
                companyId = defaultCompany,
                fullName = "Douglas Lim",
                mobile = "+65 9182 7492",
                email = "douglas.lim@maritimeplaza.com",
                role = "client_manager",
                status = "active"
            )
        )

        _firestoreAuditLogs.value = listOf(
            FirestoreAuditLog(
                id = "log_init_001",
                tenantId = defaultTenant,
                companyId = defaultCompany,
                action = "FIRESTORE_SEC_BOUND_ESTABLISHED",
                details = "Multi-tenant authorization sandbox locked with tenantId: $defaultTenant",
                performedBy = "System Provisioner"
            )
        )

        // Seed Phase 15 Workforce Collections for the default tenant
        _firestoreEmployees.value = listOf(
            FirestoreEmployee(
                id = "EMP-001",
                tenantId = defaultTenant,
                companyId = defaultCompany,
                fullName = "Vikram Singh Chaudhary",
                fatherName = "R. S. Chaudhary",
                dob = "1988-04-12",
                gender = "Male",
                mobile = "+65 9283 5029",
                email = "vikram.singh@centurionsec.com",
                employeeType = "Area Manager",
                joiningDate = "2024-03-12",
                reportingManager = "Karan Singh (Super Admin)",
                status = "Active",
                aadhaar = "AD-5491-0329-8811",
                pan = "PAN-CH8491M",
                policeVerificationStatus = "Verified",
                backgroundVerificationStatus = "Verified",
                accountHolder = "Vikram Singh Chaudhary",
                bankName = "DBS Bank Singapore",
                accountNumber = "120-4920-11",
                ifscCode = "DBSGSGSGXXX",
                readinessScore = 100.0,
                attendancePreference = "GPS",
                emergencyContactName = "Preeti Chaudhary",
                emergencyContactRelation = "Spouse",
                emergencyContactPhone = "+65 9283 5028"
            ),
            FirestoreEmployee(
                id = "EMP-002",
                tenantId = defaultTenant,
                companyId = defaultCompany,
                fullName = "Lim Ah San",
                fatherName = "Lim Keng Boey",
                dob = "1984-09-25",
                gender = "Male",
                mobile = "+65 9181 2234",
                email = "ahsan.lim@centurionsec.com",
                employeeType = "Supervisor",
                joiningDate = "2025-01-08",
                reportingManager = "Vikram Singh Chaudhary",
                status = "Active",
                aadhaar = "AD-8210-9912-4411",
                pan = "PAN-LM2210Y",
                policeVerificationStatus = "Verified",
                backgroundVerificationStatus = "Verified",
                accountHolder = "Lim Ah San",
                bankName = "OCBC Bank Singapore",
                accountNumber = "082-9901-22",
                ifscCode = "OCBCSGSGXXX",
                readinessScore = 95.0,
                attendancePreference = "QR",
                emergencyContactName = "Mandy Lim",
                emergencyContactRelation = "Daughter",
                emergencyContactPhone = "+65 9181 2235"
            ),
            FirestoreEmployee(
                id = "EMP-003",
                tenantId = defaultTenant,
                companyId = defaultCompany,
                fullName = "Karan Johar",
                fatherName = "Yash Johar",
                dob = "1994-05-15",
                gender = "Male",
                mobile = "+65 8291 9912",
                email = "karan@centurionsec.com",
                employeeType = "Guard",
                joiningDate = "2025-11-20",
                reportingManager = "Lim Ah San (Supervisor)",
                status = "Active",
                aadhaar = "AD-1102-3921-9988",
                pan = "PAN-JH1102K",
                policeVerificationStatus = "Verified",
                backgroundVerificationStatus = "Verified",
                accountHolder = "Karan Johar",
                bankName = "POSB Bank Singapore",
                accountNumber = "190-2810-99",
                ifscCode = "POSBSGSGXXX",
                readinessScore = 90.0,
                attendancePreference = "Selfie",
                emergencyContactName = "Hiroo Johar",
                emergencyContactRelation = "Mother",
                emergencyContactPhone = "+65 8291 9913"
            ),
            FirestoreEmployee(
                id = "EMP-004",
                tenantId = defaultTenant,
                companyId = defaultCompany,
                fullName = "Sarah Tan",
                fatherName = "Tan Kok Wah",
                dob = "1996-11-02",
                gender = "Female",
                mobile = "+65 9012 3456",
                email = "sarah.tan@centurionsec.com",
                employeeType = "Site Admin",
                joiningDate = "2026-02-01",
                reportingManager = "Vikram Singh Chaudhary",
                status = "Active",
                aadhaar = "AD-9912-3490-1122",
                pan = "PAN-TN9912S",
                policeVerificationStatus = "Verified",
                backgroundVerificationStatus = "Verified",
                accountHolder = "Sarah Tan",
                bankName = "UOB Bank Singapore",
                accountNumber = "120-2210-09",
                ifscCode = "UOBHSGSGXXX",
                readinessScore = 100.0,
                attendancePreference = "QR",
                emergencyContactName = "Tan Kok Wah",
                emergencyContactRelation = "Father",
                emergencyContactPhone = "+65 9012 3457"
            ),
            FirestoreEmployee(
                id = "EMP-005",
                tenantId = defaultTenant,
                companyId = defaultCompany,
                fullName = "Rajesh Kumar",
                fatherName = "Suresh Kumar",
                dob = "1991-07-20",
                gender = "Male",
                mobile = "+65 8123 4567",
                email = "rajesh@centurionsec.com",
                employeeType = "Bouncer",
                joiningDate = "2026-06-15",
                reportingManager = "Lim Ah San (Supervisor)",
                status = "Candidate",
                aadhaar = "AD-5511-9922-8821",
                pan = "PAN-KM5512R",
                policeVerificationStatus = "Pending",
                backgroundVerificationStatus = "Verified",
                accountHolder = "Rajesh Kumar",
                bankName = "POSB Bank Singapore",
                accountNumber = "111-2334-55",
                ifscCode = "POSBSGSGXXX",
                readinessScore = 50.0,
                attendancePreference = "QR",
                emergencyContactName = "Suresh Kumar",
                emergencyContactRelation = "Father",
                emergencyContactPhone = "+65 8123 4568"
            )
        )

        _firestoreEmployeeDocuments.value = listOf(
            FirestoreEmployeeDocument(
                id = "doc_emp001",
                tenantId = defaultTenant,
                companyId = defaultCompany,
                employeeId = "EMP-001",
                aadhaarDocUrl = "aadhaar_chk_v3.pdf",
                panDocUrl = "pan_ch8491m.pdf",
                photoUrl = "photo_user_001.png"
            ),
            FirestoreEmployeeDocument(
                id = "doc_emp002",
                tenantId = defaultTenant,
                companyId = defaultCompany,
                employeeId = "EMP-002",
                aadhaarDocUrl = "aadhaar_chk_v3_lim.pdf",
                panDocUrl = "pan_lm2210y.pdf",
                photoUrl = "photo_user_002.png"
            ),
            FirestoreEmployeeDocument(
                id = "doc_emp003",
                tenantId = defaultTenant,
                companyId = defaultCompany,
                employeeId = "EMP-003",
                aadhaarDocUrl = "aadhaar_karan.pdf",
                panDocUrl = "pan_karan.pdf"
            )
        )

        _firestoreEmployeeTraining.value = listOf(
            FirestoreEmployeeTraining(
                id = "trn_emp001",
                tenantId = defaultTenant,
                companyId = defaultCompany,
                employeeId = "EMP-001",
                psaraTraining = "Completed",
                fireSafety = "Completed",
                firstAid = "Completed",
                industrialSafety = "Completed",
                siteSopTraining = "Completed"
            ),
            FirestoreEmployeeTraining(
                id = "trn_emp002",
                tenantId = defaultTenant,
                companyId = defaultCompany,
                employeeId = "EMP-002",
                psaraTraining = "Completed",
                fireSafety = "Completed",
                firstAid = "Completed",
                siteSopTraining = "Completed"
            ),
            FirestoreEmployeeTraining(
                id = "trn_emp003",
                tenantId = defaultTenant,
                companyId = defaultCompany,
                employeeId = "EMP-003",
                psaraTraining = "Completed",
                fireSafety = "Not Started",
                firstAid = "Completed",
                siteSopTraining = "In Progress"
            )
        )

        _firestoreEmployeeDeployments.value = listOf(
            FirestoreEmployeeDeployment(
                id = "dpl_emp003",
                tenantId = defaultTenant,
                companyId = defaultCompany,
                employeeId = "EMP-003",
                siteId = "ST-MOP01",
                siteName = "Maritime Plaza HQ",
                shiftName = "Morning Shift (08:00 - 20:00)",
                position = "Sentry Guard",
                reportingSupervisor = "Lim Ah San"
            ),
            FirestoreEmployeeDeployment(
                id = "dpl_emp002",
                tenantId = defaultTenant,
                companyId = defaultCompany,
                employeeId = "EMP-002",
                siteId = "ST-MOP01",
                siteName = "Maritime Plaza HQ",
                shiftName = "General Supervising Shift",
                position = "Shift Commander",
                reportingSupervisor = "Vikram Singh Chaudhary"
            )
        )

        _firestoreEmployeeEquipment.value = listOf(
            FirestoreEmployeeEquipment(
                id = "eq_emp003",
                tenantId = defaultTenant,
                companyId = defaultCompany,
                employeeId = "EMP-003",
                uniformSetIssuedDate = "2025-11-21",
                idCardIssuedDate = "2025-11-21",
                shoesIssuedDate = "2025-11-21",
                beltIssuedDate = "2025-11-21",
                capIssuedDate = "2025-11-21",
                walkieTalkieIssuedDate = "2025-11-21"
            )
        )

        _firestoreEmployeeLeaveProfiles.value = listOf(
            FirestoreEmployeeLeaveProfile(
                id = "lv_emp003",
                tenantId = defaultTenant,
                companyId = defaultCompany,
                employeeId = "EMP-003"
            )
        )

        _firestoreEmployeePayrollProfiles.value = listOf(
            FirestoreEmployeePayrollProfile(
                id = "pay_emp003",
                tenantId = defaultTenant,
                companyId = defaultCompany,
                employeeId = "EMP-003",
                baseSalaryOption = "Hourly",
                baseSalaryAmount = 22.50
            )
        )

        _firestoreEmployeeTransfers.value = listOf(
            FirestoreEmployeeTransfer(
                id = "trf_seeded_001",
                tenantId = defaultTenant,
                companyId = defaultCompany,
                employeeId = "EMP-003",
                fromSiteId = "Relief Pool",
                fromSiteName = "Relief Guard Pool",
                toSiteId = "ST-MOP01",
                toSiteName = "Maritime Plaza HQ",
                fromShift = "None",
                toShift = "Morning Shift (08:00 - 20:00)",
                fromPosition = "Unassigned Relief",
                toPosition = "Sentry Guard",
                transferDate = "2026-06-01",
                reason = "Initial Deployment verification passed."
            )
        )

        // Seed Phase 16 Attendance configs and data
        _firestoreAttendanceConfigs.value = listOf(
            FirestoreAttendanceConfig(
                siteId = "ST-MOP01",
                siteName = "Maritime Plaza HQ",
                shiftId = "shift_morn_001",
                shiftName = "Morning Shift (08:00 - 20:00)",
                attendanceMethod = "QR+GPS+SELFIE",
                gpsRequired = true,
                selfieRequired = true,
                qrRequired = true,
                deviceBindingRequired = true,
                geofenceRadius = 200.0,
                allowedDeviceIds = listOf("DEV-BND-001", "DEV-BND-002", "DEV-BND-DEFAULT", "system-emulator")
            ),
            FirestoreAttendanceConfig(
                siteId = "ST-RAP02",
                siteName = "Raffles Apex Mall",
                shiftId = "shift_morn_002",
                shiftName = "Morning Shift (09:00 - 21:00)",
                attendanceMethod = "QR+GPS",
                gpsRequired = true,
                selfieRequired = false,
                qrRequired = true,
                deviceBindingRequired = false,
                geofenceRadius = 150.0,
                allowedDeviceIds = emptyList()
            ),
            FirestoreAttendanceConfig(
                siteId = "ST-COV01",
                siteName = "The Cove Tower A & B",
                shiftId = "shift_night_001",
                shiftName = "Night Shift (20:00 - 08:00)",
                attendanceMethod = "GPS+SELFIE",
                gpsRequired = true,
                selfieRequired = true,
                qrRequired = false,
                deviceBindingRequired = true,
                geofenceRadius = 250.0,
                allowedDeviceIds = listOf("DEV-BND-002", "DEV-BND-DEFAULT", "system-emulator")
            )
        )

        _firestoreAttendanceQrCodes.value = listOf(
            FirestoreAttendanceQrCode(
                id = "qr_mop_001",
                siteId = "ST-MOP01",
                siteName = "Maritime Plaza HQ",
                shiftId = "shift_morn_001",
                shiftName = "Morning Shift (08:00 - 20:00)",
                qrContent = "SEC-QR-ST-MOP01-MORN",
                isActive = true,
                generatedAt = System.currentTimeMillis() - 3600000,
                expiresAt = System.currentTimeMillis() + 86400000
            ),
            FirestoreAttendanceQrCode(
                id = "qr_rap_001",
                siteId = "ST-RAP02",
                siteName = "Raffles Apex Mall",
                shiftId = "shift_morn_002",
                shiftName = "Morning Shift (09:00 - 21:00)",
                qrContent = "SEC-QR-ST-RAP02-MORN",
                isActive = true,
                generatedAt = System.currentTimeMillis(),
                expiresAt = System.currentTimeMillis() + 86400000
            )
        )

        val yesterdayStart = System.currentTimeMillis() - 86400000
        _firestoreAttendanceRecords.value = listOf(
            FirestoreAttendanceRecord(
                id = "att_record_001",
                tenantId = defaultTenant,
                companyId = defaultCompany,
                employeeId = "EMP-003",
                employeeName = "Karan Johar",
                siteId = "ST-MOP01",
                siteName = "Maritime Plaza HQ",
                shiftId = "shift_morn_001",
                shiftName = "Morning Shift (08:00 - 20:00)",
                attendanceMethod = "QR+GPS+SELFIE",
                checkInTime = yesterdayStart + 28800000, // 08:00 AM yesterday
                checkOutTime = yesterdayStart + 72000000, // 20:00 PM yesterday
                status = "PRESENT",
                workedHours = 12.0,
                overtimeHours = 0.0,
                lateMinutes = 0,
                gps = FirestoreGpsCoords(1.2789, 103.8543),
                checkOutGps = FirestoreGpsCoords(1.2789, 103.8543),
                selfieUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                remarks = "Reporting for scheduled entry sentinel shift",
                createdAt = yesterdayStart,
                updatedAt = yesterdayStart + 72000000
            ),
            FirestoreAttendanceRecord(
                id = "att_record_002",
                tenantId = defaultTenant,
                companyId = defaultCompany,
                employeeId = "EMP-002",
                employeeName = "Lim Ah San",
                siteId = "ST-MOP01",
                siteName = "Maritime Plaza HQ",
                shiftId = "shift_morn_001",
                shiftName = "Morning Shift (08:00 - 20:00)",
                attendanceMethod = "QR+GPS+SELFIE",
                checkInTime = System.currentTimeMillis() - 7200000, // Checked in 2 hrs ago
                checkOutTime = null,
                status = "LATE",
                workedHours = 2.0,
                overtimeHours = 0.0,
                lateMinutes = 15,
                remarks = "Heavy traffic jam near PIE expressway.",
                gps = FirestoreGpsCoords(1.2787, 103.8541),
                createdAt = System.currentTimeMillis() - 7200000,
                updatedAt = System.currentTimeMillis()
            ),
            FirestoreAttendanceRecord(
                id = "att_record_003",
                tenantId = defaultTenant,
                companyId = defaultCompany,
                employeeId = "EMP-001",
                employeeName = "Vikram Singh Chaudhary",
                siteId = "ST-MOP01",
                siteName = "Maritime Plaza HQ",
                shiftId = "shift_morn_001",
                shiftName = "Morning Shift (08:00 - 20:00)",
                attendanceMethod = "GPS",
                status = "ON_LEAVE",
                createdAt = System.currentTimeMillis()
            )
        )

        _firestoreAttendanceSessions.value = listOf(
            FirestoreAttendanceSession(
                id = "sess_001",
                recordId = "att_record_001",
                employeeId = "EMP-003",
                siteId = "ST-MOP01",
                shiftId = "shift_morn_001",
                sessionType = "CHECK_IN",
                deviceId = "DEV-BND-001",
                timestamp = yesterdayStart + 28800000
            ),
            FirestoreAttendanceSession(
                id = "sess_002",
                recordId = "att_record_001",
                employeeId = "EMP-003",
                siteId = "ST-MOP01",
                shiftId = "shift_morn_001",
                sessionType = "CHECK_OUT",
                deviceId = "DEV-BND-001",
                timestamp = yesterdayStart + 72000000
            ),
            FirestoreAttendanceSession(
                id = "sess_003",
                recordId = "att_record_002",
                employeeId = "EMP-002",
                siteId = "ST-MOP01",
                shiftId = "shift_morn_001",
                sessionType = "CHECK_IN",
                deviceId = "DEV-BND-002",
                timestamp = System.currentTimeMillis() - 7200000
            )
        )

        _firestoreAttendanceExceptions.value = listOf(
            FirestoreAttendanceException(
                id = "exc_001",
                recordId = "att_record_002",
                employeeId = "EMP-002",
                employeeName = "Lim Ah San",
                siteId = "ST-MOP01",
                siteName = "Maritime Plaza HQ",
                exceptionType = "LATE_ARRIVAL",
                description = "Lim Ah San checked in late by 15 minutes for Scheduled Morning Shift.",
                status = "PENDING",
                timestamp = System.currentTimeMillis() - 7100000
            ),
            FirestoreAttendanceException(
                id = "exc_002",
                recordId = "att_record_dummy_ex",
                employeeId = "EMP-004",
                employeeName = "Sarah Tan",
                siteId = "ST-MOP01",
                siteName = "Maritime Plaza HQ",
                exceptionType = "GPS_FAILURE",
                description = "GPS lock attempted outside the geofence perimeter (Measured distance: 1,420 meters from HQ guard house).",
                status = "PENDING",
                timestamp = System.currentTimeMillis() - 14400000
            )
        )

        _firestoreAttendanceCorrections.value = listOf(
            FirestoreAttendanceCorrection(
                id = "corr_001",
                recordId = "att_record_001",
                employeeId = "EMP-003",
                employeeName = "Karan Johar",
                siteId = "ST-MOP01",
                requestDate = System.currentTimeMillis() - 3600000,
                requestedCheckIn = yesterdayStart + 28550000, // Requesting slightly earlier adjustments due to transit lag
                reason = "Correction requested for biometric sync issue at south deployment lobby gates.",
                status = "PENDING",
                auditLog = listOf("Guard requested correction.")
            )
        )

        _firestoreAttendanceReports.value = listOf(
            FirestoreAttendanceReport(
                id = "rep_001",
                type = "DAILY",
                generatedBy = "Vikram Singh Chaudhary",
                format = "PDF",
                timestamp = System.currentTimeMillis() - 18000000,
                downloadUrl = "https://example.com/downloads/daily_rep_june22.pdf"
            ),
            FirestoreAttendanceReport(
                id = "rep_002",
                type = "OVERTIME",
                generatedBy = "Vikram Singh Chaudhary",
                format = "EXCEL",
                timestamp = System.currentTimeMillis() - 15000000,
                downloadUrl = "https://example.com/downloads/overtime_rep_june22.xlsx"
            )
        )

        // --- PHASE 17 SEED DATA ---
        val now = System.currentTimeMillis()
        
        // Seed Blacklist
        _blacklist.value = listOf(
            FirestoreBlacklistEntry(
                id = "bl_001",
                type = "VISITOR",
                targetValue = "+65 8888 8888",
                targetName = "John Menard",
                reason = "Repeated tailgating and arguing with on-duty guards regarding gate pass procedures",
                createdBy = "Supervisor Lim",
                createdAt = now - 86400000L * 5
            ),
            FirestoreBlacklistEntry(
                id = "bl_002",
                type = "VEHICLE",
                targetValue = "SGA1111A",
                targetName = "Mercedes Benz S350",
                reason = "Illegal parking in disabled lanes and blocking emergency entrance twice",
                createdBy = "Guard Singh",
                createdAt = now - 86400000L * 2
            ),
            FirestoreBlacklistEntry(
                id = "bl_003",
                type = "CONTRACTOR",
                targetValue = "Starlight Painters",
                targetName = "Starlight Painters Staff",
                reason = "Failed to provide safety harnesses during exterior painting work block, compliance breach",
                createdBy = "Site Admin Karan",
                createdAt = now - 86400000L * 10
            )
        )

        // Seed Contractors
        _contractors.value = listOf(
            FirestoreContractor(
                id = "ctr_001",
                companyName = "Centurion Elevators Ltd",
                supervisorName = "Lim Ah San",
                contactNumber = "+65 9111 2222",
                workPermitType = "Civil",
                permitStart = now - 86400000L * 3,
                permitEnd = now + 86400000L * 30,
                status = "ACTIVE"
            ),
            FirestoreContractor(
                id = "ctr_002",
                companyName = "Super Spark Electricals",
                supervisorName = "Tan Kok Hua",
                contactNumber = "+65 9333 4444",
                workPermitType = "Electrical",
                permitStart = now - 86400000L,
                permitEnd = now + 86400000L * 5,
                status = "ACTIVE"
            )
        )

        // Seed Contractor Workers
        _contractorWorkers.value = listOf(
            FirestoreContractorWorker(
                id = "cw_001",
                contractorId = "ctr_001",
                companyName = "Centurion Elevators Ltd",
                workerName = "Ravi Kumar",
                mobile = "+65 8123 4567",
                idProofType = "S-Pass",
                idProofNumber = "S1234567A",
                checkInTime = now - 3600000 * 2,
                status = "INSIDE"
            ),
            FirestoreContractorWorker(
                id = "cw_002",
                contractorId = "ctr_001",
                companyName = "Centurion Elevators Ltd",
                workerName = "Wang Wei",
                mobile = "+65 8234 5678",
                idProofType = "Work Permit",
                idProofNumber = "W7654321B",
                checkInTime = now - 3600000 * 2,
                status = "INSIDE"
            ),
            FirestoreContractorWorker(
                id = "cw_003",
                contractorId = "ctr_002",
                companyName = "Super Spark Electricals",
                workerName = "Ahmad Yusuf",
                mobile = "+65 8345 6789",
                idProofType = "Nric",
                idProofNumber = "S8888888D",
                status = "OUT"
            )
        )

        // Seed Visitors
        val vis1Id = "vis_001"
        val vis2Id = "vis_002"
        val vis3Id = "vis_003"
        val vis4Id = "vis_004"
        _visitors.value = listOf(
            FirestoreVisitor(
                id = vis1Id,
                siteId = "ST-MOP01",
                visitorType = "Guest",
                fullName = "David Beckham",
                mobile = "+65 9123 9876",
                gender = "Male",
                photoUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=120",
                idProofType = "Passport",
                idProofNumber = "GBR9981245",
                unitFlatOffice = "Office Panel 6-B",
                hostName = "Vikram Singh",
                hostMobile = "+65 9283 5029",
                purposeOfVisit = "Quarterly Security Audit Discussion",
                expectedDuration = "2 hours",
                status = "INSIDE",
                hostApprovalMethod = "Mobile App",
                qrPassId = "pass_vis_001",
                checkInTime = now - 3600000,
                guardName = "Guard Singh",
                remarks = "Brought physical presentation files."
            ),
            FirestoreVisitor(
                id = vis2Id,
                siteId = "ST-MOP01",
                visitorType = "Delivery Executive",
                fullName = "Zack Tan",
                mobile = "+65 9555 4321",
                gender = "Male",
                photoUrl = null,
                idProofType = "Aadhaar",
                idProofNumber = "N71284",
                unitFlatOffice = "Flat 802-A",
                hostName = "Johnathan Yeo",
                hostMobile = "+65 9312 9481",
                purposeOfVisit = "Parcel Dropoff",
                expectedDuration = "15 mins",
                status = "INSIDE",
                hostApprovalMethod = "SMS Override",
                qrPassId = "pass_vis_002",
                checkInTime = now - 1800000,
                guardName = "Guard Singh"
            ),
            FirestoreVisitor(
                id = vis3Id,
                siteId = "ST-MOP01",
                visitorType = "Candidate",
                fullName = "Emma Watson",
                mobile = "+65 9444 5555",
                gender = "Female",
                photoUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=120",
                idProofType = "Driving License",
                idProofNumber = "DL-SG-912A",
                unitFlatOffice = "Conference Room Alpha",
                hostName = "HR Recruiter",
                hostMobile = "+65 9182 3456",
                purposeOfVisit = "Technical Coding Interview",
                expectedDuration = "1 hour",
                status = "APPROVED",
                hostApprovalMethod = "Voice Call Verification",
                qrPassId = "pass_vis_003"
            ),
            FirestoreVisitor(
                id = vis4Id,
                siteId = "ST-MOP01",
                visitorType = "Family Visitor",
                fullName = "Maria Singh",
                mobile = "+65 9777 7777",
                gender = "Female",
                unitFlatOffice = "Quarter 5",
                hostName = "Guard Singh",
                hostMobile = "+65 9812 7311",
                purposeOfVisit = "Delivering lunchbox",
                expectedDuration = "30 mins",
                status = "EXITED",
                hostApprovalMethod = "Security Desk Override",
                checkInTime = now - 7200000,
                checkOutTime = now - 6000000,
                guardName = "Supervisor Karan",
                remarks = "Leaving through West lobby exit."
            )
        )

        // Seed Passes
        _visitorPasses.value = listOf(
            FirestoreVisitorPass(
                id = "pass_vis_001",
                visitorId = vis1Id,
                visitorName = "David Beckham",
                siteId = "ST-MOP01",
                hostName = "Vikram Singh",
                qrContent = "SEC-VIS-PASS-001-DB",
                passType = "Guest Pass",
                status = "USED",
                validFrom = now - 7200000,
                validUntil = now + 86400000L
            ),
            FirestoreVisitorPass(
                id = "pass_vis_002",
                visitorId = vis2Id,
                visitorName = "Zack Tan",
                siteId = "ST-MOP01",
                hostName = "Johnathan Yeo",
                qrContent = "SEC-VIS-PASS-002-ZT",
                passType = "Temporary Pass",
                status = "USED",
                validFrom = now - 3600000,
                validUntil = now + 86400000L
            ),
            FirestoreVisitorPass(
                id = "pass_vis_003",
                visitorId = vis3Id,
                visitorName = "Emma Watson",
                siteId = "ST-MOP01",
                hostName = "HR Recruiter",
                qrContent = "SEC-VIS-PASS-003-EW",
                passType = "Guest Pass",
                status = "ACTIVE",
                validFrom = now,
                validUntil = now + 86400000L * 2
            )
        )

        // Seed vehicles
        _vehicles.value = listOf(
            FirestoreVehicle(
                id = "v_001",
                vehicleNumber = "SGC9911X",
                vehicleType = "Car",
                color = "Silver",
                company = "Grab Premium",
                driverName = "Zack Tan",
                driverMobile = "+65 9555 4321",
                associatedVisitorId = vis2Id,
                checkInTime = now - 1800000
            ),
            FirestoreVehicle(
                id = "v_002",
                vehicleNumber = "SFH8234D",
                vehicleType = "Truck",
                color = "White",
                company = "Centurion Sourcing Ltd",
                driverName = "Lorry Lao",
                driverMobile = "+65 9222 1111",
                checkInTime = now - 3600000 * 4,
                checkOutTime = now - 3600000 * 3
            )
        )

        // Seed deliveries
        _deliveries.value = listOf(
            FirestoreDelivery(
                id = "del_001",
                orderNumber = "AMZ-99104-SG",
                deliveryCompany = "Amazon",
                recipientName = "Jane Ho",
                recipientUnit = "Flat 802-A",
                checkInTime = now - 1800000,
                status = "INSIDE"
            ),
            FirestoreDelivery(
                id = "del_002",
                orderNumber = "FD-92815",
                deliveryCompany = "Food Delivery",
                recipientName = "Karan Johar",
                recipientUnit = "Office Room B",
                checkInTime = now - 7200000,
                checkOutTime = now - 6600000,
                status = "EXITED"
            )
        )

        // Seed Evacuation Entries
        _evacuationRegister.value = listOf(
            FirestoreEvacuationEntry(
                id = "ev_001",
                name = "David Beckham",
                type = "VISITOR",
                detail = "Guest • Office Panel 6-B",
                checkInTime = now - 3600000
            ),
            FirestoreEvacuationEntry(
                id = "ev_002",
                name = "Zack Tan",
                type = "VISITOR",
                detail = "Delivery Executive • Flat 802-A",
                inboundVehicle = "SGC9911X",
                checkInTime = now - 1800000
            ),
            FirestoreEvacuationEntry(
                id = "ev_003",
                name = "Ravi Kumar",
                type = "CONTRACTOR_WORKER",
                detail = "Centurion Elevators Ltd (Civil)",
                checkInTime = now - 3600000 * 2
            ),
            FirestoreEvacuationEntry(
                id = "ev_004",
                name = "Wang Wei",
                type = "CONTRACTOR_WORKER",
                detail = "Centurion Elevators Ltd (Civil)",
                checkInTime = now - 3600000 * 2
            ),
            FirestoreEvacuationEntry(
                id = "ev_005",
                name = "Amazon Prime Logistics",
                type = "DELIVERY",
                detail = "Order AMZ-99104-SG • Recipient Jane Ho",
                checkInTime = now - 1800000
            )
        )

        // Seed Reports
        _visitorReports.value = listOf(
            FirestoreVisitorReport(
                id = "vrep_001",
                type = "DAILY",
                generatedBy = "Guard Singh",
                timestamp = now - 3600000,
                format = "PDF",
                contentSummary = "Processed 28 entries, 0 blacklist warnings popped.",
                downloadUrl = "https://example.com/downloads/visitor_daily_june22.pdf"
            ),
            FirestoreVisitorReport(
                id = "vrep_002",
                type = "EVACUATION",
                generatedBy = "System Override",
                timestamp = now - 1800000,
                format = "EXCEL",
                contentSummary = "Live-evac registry snap: 5 active entities inside perimeter.",
                downloadUrl = "https://example.com/downloads/evacuation_snap_june22.xlsx"
            )
        )
    }

    init {
        val database = SecurityDatabase.getDatabase(application)
        val dao = database.securityDao()
        repository = SecurityRepository(dao)

        users = repository.usersFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        sites = repository.sitesFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        attendance = repository.attendanceFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        incidents = repository.incidentsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        sosAlerts = repository.sosAlertsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        auditLogs = repository.auditLogsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed data and set default simulated active user
        viewModelScope.launch {
            repository.seedMockDataIfNeeded()
            seedFirestoreMockData()
            // Set default active user to Super Admin to unlock full navigation first
            val allUsers = users.first { it.isNotEmpty() }
            _currentUser.value = allUsers.firstOrNull { it.role == "super_admin" }
        }
    }

    fun selectUser(user: User) {
        _currentUser.value = user
        showToast("Switched active user to ${user.name} [Role: ${user.role.uppercase()}]")
        viewModelScope.launch {
            repository.logAudit(
                action = "ROLE_SIMULATION_SWITCH",
                details = "Simulated security sessions set to active identity: ${user.name} (${user.role})",
                targetId = user.id,
                performedBy = "developer",
                oldValue = "None",
                newValue = user.role,
                siteId = "global_sys",
                companyId = user.companyId,
                companyGroupId = user.companyGroupId
            )
        }
    }

    fun navigateTo(section: String) {
        _currentSection.value = section
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            if (_toastMessage.value == msg) {
                _toastMessage.value = null
            }
        }
    }

    // ==========================================
    // Phase 5: Centralized Site Management
    // ==========================================
    fun createSite(name: String, address: String, status: String) {
        val user = _currentUser.value ?: return
        val siteId = "site_" + UUID.randomUUID().toString().take(6)
        val newSite = Site(
            id = siteId,
            companyGroupId = user.companyGroupId.ifEmpty { "grp_kundal_global" },
            companyId = user.companyId.ifEmpty { "cmp_centurion_sec" },
            siteId = siteId,
            name = name,
            address = address,
            latitude = 28.5 + (Math.random() * 0.1),
            longitude = 77.1 + (Math.random() * 0.1),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            createdBy = user.id,
            updatedBy = user.id,
            status = status // Draft, Setup, etc.
        )
        viewModelScope.launch {
            repository.insertSite(newSite)
            showToast("Successfully created site: $name in stage ${status.uppercase()}")
        }
    }

    fun updateSiteStatus(site: Site, newStatus: String) {
        val user = _currentUser.value ?: return
        val updatedSite = site.copy(
            status = newStatus,
            updatedAt = System.currentTimeMillis(),
            updatedBy = user.id
        )
        viewModelScope.launch {
            repository.insertSite(updatedSite)
            showToast("Moved Site '${site.name}' from ${site.status.uppercase()} -> ${newStatus.uppercase()}")
            repository.logAudit(
                action = "SITE_LIFECYCLE_MIGRATION",
                details = "Site state migrated safely: ${site.name}",
                targetId = site.id,
                performedBy = user.id,
                oldValue = site.status,
                newValue = newStatus,
                siteId = site.id,
                companyId = site.companyId,
                companyGroupId = site.companyGroupId
            )
        }
    }

    // ==========================================
    // Unified Operational Handlers
    // ==========================================
    fun checkInUser(siteId: String, siteName: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val active = repository.getActiveAttendanceForUser(user.id)
            if (active != null) {
                showToast("Already punched in to other location: ${active.siteId}")
                return@launch
            }
            val now = System.currentTimeMillis()
            val attend = Attendance(
                id = "att_" + UUID.randomUUID().toString().take(6),
                companyGroupId = user.companyGroupId,
                companyId = user.companyId,
                siteId = siteId,
                userId = user.id,
                userName = user.name,
                role = user.role,
                clockInTime = now,
                clockOutTime = null,
                clockInGeo = "GeoIn: Site ($siteName)",
                clockOutGeo = null,
                createdAt = now,
                updatedAt = now,
                createdBy = user.id,
                updatedBy = user.id,
                status = "Open"
            )
            repository.insertAttendance(attend)
            showToast("Attendance Saved! Clocked-In successfully.")

            repository.logAudit(
                action = "ATTENDANCE_CLOCK_IN",
                details = "Roster punch validated for ${user.name} - Site: $siteName",
                targetId = attend.id,
                performedBy = user.id,
                oldValue = "Out",
                newValue = "In",
                siteId = siteId,
                companyId = user.companyId,
                companyGroupId = user.companyGroupId
            )
        }
    }

    fun checkOutUser() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val active = repository.getActiveAttendanceForUser(user.id)
            if (active == null) {
                showToast("No active clock-ins found for ${user.name}")
                return@launch
            }
            val now = System.currentTimeMillis()
            val completed = active.copy(
                clockOutTime = now,
                clockOutGeo = "GeoOut: Handheld punch",
                updatedAt = now,
                updatedBy = user.id,
                status = "Closed"
            )
            repository.insertAttendance(completed)
            showToast("Attendance Closed! Clocked-Out successfully.")

            repository.logAudit(
                action = "ATTENDANCE_CLOCK_OUT",
                details = "Roster exit logged for ${user.name}",
                targetId = completed.id,
                performedBy = user.id,
                oldValue = "In",
                newValue = "Out",
                siteId = active.siteId,
                companyId = user.companyId,
                companyGroupId = user.companyGroupId
            )
        }
    }

    fun logIncident(title: String, severity: String, description: String, siteId: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val incId = "inc_" + UUID.randomUUID().toString().take(6)
            val incident = Incident(
                id = incId,
                companyGroupId = user.companyGroupId.ifEmpty { "grp_kundal_global" },
                companyId = user.companyId.ifEmpty { "cmp_centurion_sec" },
                siteId = siteId,
                title = title,
                severity = severity,
                description = description,
                reportedBy = user.id,
                reporterName = user.name,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                createdBy = user.id,
                updatedBy = user.id,
                status = "Pending"
            )
            repository.insertIncident(incident)
            showToast("Exception report dispatch completed successfully.")
        }
    }

    fun triggerSOS(siteId: String, siteName: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val alertId = "sos_" + UUID.randomUUID().toString().take(6)
            val baseLat = 28.4951
            val baseLng = 77.0894
            val sos = SOSAlert(
                id = alertId,
                companyGroupId = user.companyGroupId.ifEmpty { "grp_kundal_global" },
                companyId = user.companyId.ifEmpty { "cmp_centurion_sec" },
                siteId = siteId,
                triggeredBy = user.id,
                triggererName = user.name,
                triggererRole = user.role,
                latitude = baseLat,
                longitude = baseLng,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                createdBy = user.id,
                updatedBy = user.id,
                status = "Active"
            )
            repository.insertSOSAlert(sos)
            showToast("⚠️ SOS EMERGENCY BROADCAST INITIATED FOR $siteName!")
        }
    }

    fun resolveSOS(alertId: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.resolveSOSAlert(alertId, user.id, user.name)
            showToast("SOS Alert resolved and logged out of active priority channel.")
        }
    }

    // ==========================================
    // PHASE 16: ATTENDANCE & SHIFT ENGINE
    // ==========================================

    fun clockInEmployee(
        employeeId: String,
        siteId: String,
        shiftId: String,
        latitude: Double,
        longitude: Double,
        selfieUrl: String?,
        scannedQr: String?,
        deviceId: String
    ): String? {
        val user = _currentUser.value ?: return "User context not set."
        val emp = _firestoreEmployees.value.firstOrNull { it.id == employeeId }
            ?: return "Employee ID details not found."

        // HARD BLOCKER 1: Employee Suspended
        if (emp.status.uppercase() == "SUSPENDED" || emp.status.uppercase() != "ACTIVE") {
            createAttendanceException(
                employeeId = employeeId,
                employeeName = emp.fullName,
                siteId = siteId,
                siteName = "Selected Site",
                type = "DEVICE_FAILURE",
                desc = "Check-in blocked: Employee status is ${emp.status}."
            )
            return "Blocker: Employee is Suspended or not Active in registry."
        }

        // HARD BLOCKER 2: Deployment Missing
        val dpl = _firestoreEmployeeDeployments.value.firstOrNull { it.employeeId == employeeId }
        if (dpl == null) {
            createAttendanceException(
                employeeId = employeeId,
                employeeName = emp.fullName,
                siteId = siteId,
                siteName = "Selected Site",
                type = "MISSED_CHECK_IN",
                desc = "Check-in blocked: Active deployment registry entry is missing."
            )
            return "Blocker: Active site deployment mapping is missing for this guard."
        }

        // HARD BLOCKER 3: Leave Approved
        val leave = _firestoreEmployeeLeaveProfiles.value.firstOrNull { it.employeeId == employeeId }
        if (emp.status == "On Leave" || (leave != null && leave.casualLeaveTaken + leave.sickLeaveTaken + leave.earnedLeaveTaken > 30)) {
            createAttendanceException(
                employeeId = employeeId,
                employeeName = emp.fullName,
                siteId = siteId,
                siteName = "Selected Site",
                type = "DUPLICATE_ATTENDANCE",
                desc = "Check-in blocked: Employee has an approved leave context for today's roster."
            )
            return "Blocker: Employee has approved Leave and cannot register clock-in."
        }

        // HARD BLOCKER 4: Duplicate Check-In
        val activeRecords = _firestoreAttendanceRecords.value.filter { it.employeeId == employeeId && it.checkOutTime == null }
        if (activeRecords.isNotEmpty()) {
            createAttendanceException(
                employeeId = employeeId,
                employeeName = emp.fullName,
                siteId = siteId,
                siteName = "Selected Site",
                type = "DUPLICATE_ATTENDANCE",
                desc = "Duplicate enrollment check-in: An open attendance session is already active for this guard."
            )
            return "Blocker: Duplicate clock-in. An open shift session is already active on-site."
        }

        // Find configurations or fallback
        val config = _firestoreAttendanceConfigs.value.firstOrNull { it.siteId == siteId && it.shiftId == shiftId }
            ?: FirestoreAttendanceConfig(
                siteId = siteId,
                siteName = "Maritime Plaza HQ",
                shiftId = shiftId,
                shiftName = "Morning Shift (08:00 - 20:00)",
                attendanceMethod = emp.attendancePreference,
                gpsRequired = true,
                selfieRequired = !selfieUrl.isNullOrEmpty(),
                qrRequired = !scannedQr.isNullOrEmpty(),
                deviceBindingRequired = false
            )

        // HARD BLOCKER 5: GPS Geofencing Validation
        if (config.gpsRequired) {
            val targetSite = _firestoreSites.value.firstOrNull { it.id == siteId }
            val baseLat = targetSite?.gps?.latitude ?: 1.2789
            val baseLng = targetSite?.gps?.longitude ?: 103.8543
            val distance = calculateDistanceInMeters(latitude, longitude, baseLat, baseLng)
            if (distance > config.geofenceRadius) {
                createAttendanceException(
                    employeeId = employeeId,
                    employeeName = emp.fullName,
                    siteId = siteId,
                    siteName = targetSite?.siteName ?: "Selected Site",
                    type = "GPS_FAILURE",
                    desc = "Geofence Violation: Clock-in attempted at distance of ${distance.toInt()} meters (Allowed limit: ${config.geofenceRadius.toInt()}m)."
                )
                return "Blocker: GPS validation failed. You are outside the designated site geofence perimeter by ${distance.toInt()}m."
            }
        }

        // HARD BLOCKER 6: Device Binding Verification
        if (config.deviceBindingRequired && config.allowedDeviceIds.isNotEmpty()) {
            if (!config.allowedDeviceIds.contains(deviceId)) {
                createAttendanceException(
                    employeeId = employeeId,
                    employeeName = emp.fullName,
                    siteId = siteId,
                    siteName = "Selected Site",
                    type = "DEVICE_FAILURE",
                    desc = "Unauthorized Terminal Device ID: Attempted check-in via device ID '$deviceId' which is not bound to this employee."
                )
                return "Blocker: Device binding verification failed. This mobile phone terminal is not authorized for your registry profile."
            }
        }

        // HARD BLOCKER 7: QR Code Authentication
        if (config.qrRequired && scannedQr.isNullOrBlank()) {
            createAttendanceException(
                employeeId = employeeId,
                employeeName = emp.fullName,
                siteId = siteId,
                siteName = "Selected Site",
                type = "MISSED_CHECK_IN",
                desc = "QR Code Scanner Failure: No QR scanned or validation code mismatched."
            )
            return "Blocker: QR Code scan is mandatory. Please align cameras with the site QR stand."
        }

        // Determine shift status
        val currentTime = System.currentTimeMillis()
        val c = java.util.Calendar.getInstance()
        c.timeInMillis = currentTime
        val hour = c.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = c.get(java.util.Calendar.MINUTE)
        val isLateCheck = hour > 8 || (hour == 8 && minute > 5)
        val status = if (isLateCheck) "LATE" else "PRESENT"
        val lateMins = if (isLateCheck) ((hour - 8) * 60 + minute) else 0

        val recordId = "att_record_" + UUID.randomUUID().toString().take(6)
        val newRecord = FirestoreAttendanceRecord(
            id = recordId,
            tenantId = "tenant_singh_sec",
            companyId = user.companyId.ifEmpty { "cmp_centurion_sec" },
            employeeId = employeeId,
            employeeName = emp.fullName,
            siteId = siteId,
            siteName = _firestoreSites.value.firstOrNull { it.id == siteId }?.siteName ?: "Client Site",
            shiftId = shiftId,
            shiftName = "Standard Shift (08:00 - 20:00)",
            attendanceMethod = config.attendanceMethod,
            checkInTime = currentTime,
            checkOutTime = null,
            status = status,
            workedHours = 0.0,
            overtimeHours = 0.0,
            lateMinutes = lateMins,
            gps = FirestoreGpsCoords(latitude, longitude),
            selfieUrl = selfieUrl,
            remarks = "Punched via modern adaptive client app on device $deviceId.",
            createdAt = currentTime,
            updatedAt = currentTime
        )

        _firestoreAttendanceRecords.update { listOf(newRecord) + it }

        // Create Session LOG
        val newSession = FirestoreAttendanceSession(
            id = "sess_" + UUID.randomUUID().toString().take(6),
            recordId = recordId,
            employeeId = employeeId,
            siteId = siteId,
            shiftId = shiftId,
            sessionType = "CHECK_IN",
            deviceId = deviceId,
            gpsVerified = true,
            selfieVerified = !selfieUrl.isNullOrEmpty(),
            qrVerified = !scannedQr.isNullOrEmpty(),
            timestamp = currentTime
        )
        _firestoreAttendanceSessions.update { listOf(newSession) + it }

        if (isLateCheck) {
            createAttendanceException(
                employeeId = employeeId,
                employeeName = emp.fullName,
                siteId = siteId,
                siteName = newRecord.siteName,
                type = "LATE_ARRIVAL",
                desc = "Late Arrival verified: clocked in late by ${lateMins} minutes overdue.",
                recId = recordId
            )
        }

        logSystemAudit("ATTENDANCE_CHECK_IN", "Employee ${emp.fullName} clocked in successfully at ${newRecord.siteName}.", "None", status)
        showToast("Shift Started: Active Clock-In verified for ${emp.fullName}!")
        return null
    }

    fun clockOutEmployeeRecord(
        recordId: String,
        latitude: Double,
        longitude: Double,
        selfieUrl: String?,
        remarks: String
    ): String? {
        val user = _currentUser.value ?: return "User context missing."
        val records = _firestoreAttendanceRecords.value
        val index = records.indexOfFirst { it.id == recordId }
        if (index == -1) return "Record not found."

        val record = records[index]
        val checkIn = record.checkInTime
        val checkOut = System.currentTimeMillis()
        val deltaHrs = (checkOut - checkIn).toDouble() / 3600000.0
        val workedHrs = Math.max(0.1, Math.round(deltaHrs * 100.0) / 100.0)

        // Strict Overtime Engine Logic
        val baseShiftHrs = 12.0
        val isOvertime = workedHrs > baseShiftHrs
        val otHrs = if (isOvertime) (workedHrs - baseShiftHrs) else 0.0

        var resultStatus = record.status
        if (resultStatus != "LATE") {
            resultStatus = "PRESENT"
        }

        val updatedRecord = record.copy(
            checkOutTime = checkOut,
            workedHours = workedHrs,
            overtimeHours = otHrs,
            checkOutGps = FirestoreGpsCoords(latitude, longitude),
            checkOutSelfieUrl = selfieUrl,
            checkOutRemarks = remarks,
            status = resultStatus,
            updatedAt = checkOut
        )

        _firestoreAttendanceRecords.update { list ->
            list.map { if (it.id == recordId) updatedRecord else it }
        }

        // Create Session LOG for checkout
        val newSession = FirestoreAttendanceSession(
            id = "sess_" + UUID.randomUUID().toString().take(6),
            recordId = recordId,
            employeeId = record.employeeId,
            siteId = record.siteId,
            shiftId = record.shiftId,
            sessionType = "CHECK_OUT",
            deviceId = "current-device",
            gpsVerified = true,
            selfieVerified = !selfieUrl.isNullOrEmpty(),
            timestamp = checkOut
        )
        _firestoreAttendanceSessions.update { listOf(newSession) + it }

        logSystemAudit("ATTENDANCE_CHECK_OUT", "Employee ${record.employeeName} checked out. Worked: ${workedHrs}h, OT: ${otHrs}h", "Open Session", "Completed")
        showToast("Shift completed! Worked ${workedHrs} hours (Overtime: ${otHrs} hrs).")
        return null
    }

    fun submitCorrectionProposed(
        recordId: String,
        employeeId: String,
        reason: String,
        newClockIn: Long?,
        newClockOut: Long?
    ) {
        val emp = _firestoreEmployees.value.firstOrNull { it.id == employeeId } ?: return
        val rec = _firestoreAttendanceRecords.value.firstOrNull { it.id == recordId }
        val id = "corr_" + UUID.randomUUID().toString().take(6)
        val correction = FirestoreAttendanceCorrection(
            id = id,
            recordId = recordId,
            employeeId = employeeId,
            employeeName = emp.fullName,
            siteId = rec?.siteId ?: "ST-MOP01",
            requestDate = System.currentTimeMillis(),
            requestedCheckIn = newClockIn,
            requestedCheckOut = newClockOut,
            reason = reason,
            status = "PENDING",
            auditLog = listOf("Correction request initialized by Supervisor. [Pending Site Admin review]")
        )
        _firestoreAttendanceCorrections.update { listOf(correction) + it }
        showToast("Correction proposed for review. ID: $id")
    }

    fun approveOrRejectCorrection(correctionId: String, approve: Boolean, remarks: String) {
        val user = _currentUser.value ?: return
        val list = _firestoreAttendanceCorrections.value
        val index = list.indexOfFirst { it.id == correctionId }
        if (index == -1) return

        val item = list[index]
        val nextStatus = if (approve) "SITE_ADMIN_APPROVED" else "REJECTED"
        val logLine = "Reviewed by ${user.name} as ${nextStatus}: $remarks"

        val updatedCorrection = item.copy(
            status = nextStatus,
            auditLog = item.auditLog + listOf(logLine)
        )
        _firestoreAttendanceCorrections.update { l ->
            l.map { if (it.id == correctionId) updatedCorrection else it }
        }

        if (approve && item.recordId.isNotEmpty()) {
            _firestoreAttendanceRecords.update { recs ->
                recs.map { rec ->
                    if (rec.id == item.recordId) {
                        var updatedRec = rec
                        if (item.requestedCheckIn != null) {
                            updatedRec = updatedRec.copy(checkInTime = item.requestedCheckIn)
                        }
                        if (item.requestedCheckOut != null) {
                            val inTime = item.requestedCheckIn ?: rec.checkInTime
                            val deltaHrs = (item.requestedCheckOut - inTime).toDouble() / 3600000.0
                            val workedHrs = Math.max(0.1, Math.round(deltaHrs * 100.0) / 100.0)
                            val otHrs = if (workedHrs > 12.0) (workedHrs - 12.0) else 0.0
                            updatedRec = updatedRec.copy(
                                checkOutTime = item.requestedCheckOut,
                                workedHours = workedHrs,
                                overtimeHours = otHrs,
                                status = "PRESENT"
                            )
                        }
                        updatedRec
                    } else rec
                }
            }
        }

        logSystemAudit("ATT_CORRECTION_RESOLVED", "Correction ID $correctionId updated to $nextStatus", item.status, nextStatus)
        showToast("Correction request resolved: Status $nextStatus")
    }

    fun processExceptionAction(exceptionId: String, action: String, adminRemarks: String) {
        val list = _firestoreAttendanceExceptions.value
        val index = list.indexOfFirst { it.id == exceptionId }
        if (index == -1) return

        val item = list[index]
        val updatedException = item.copy(
            status = action.uppercase() + "D",
            remarks = adminRemarks
        )

        _firestoreAttendanceExceptions.update { l ->
            l.map { if (it.id == exceptionId) updatedException else it }
        }

        logSystemAudit("ATT_EXCEPTION_RESOLVED", "Exception ID $exceptionId resolved as $action.", item.status, action)
        showToast("Exception updated: $action successfully recorded.")
    }

    fun bulkAttendanceImport(simulatedInputs: List<FirestoreAttendanceRecord>) {
        _firestoreAttendanceRecords.update { current -> simulatedInputs + current }
        logSystemAudit("ATT_BULK_IMPORT", "Imported ${simulatedInputs.size} roster shift punches.", "None", "Imported Successfully")
        showToast("Roster Sync Complete: Successfully imported ${simulatedInputs.size} logs.")
    }

    fun bulkCorrectionsApproval() {
        val pendingCount = _firestoreAttendanceCorrections.value.count { it.status == "PENDING" }
        if (pendingCount == 0) {
            showToast("No pending correction workflows detected.")
            return
        }

        _firestoreAttendanceCorrections.update { list ->
            list.map {
                if (it.status == "PENDING") {
                    val item = it
                    if (item.recordId.isNotEmpty()) {
                        _firestoreAttendanceRecords.update { recs ->
                            recs.map { rec ->
                                if (rec.id == item.recordId) {
                                    var updatedRec = rec
                                    if (item.requestedCheckIn != null) {
                                        updatedRec = updatedRec.copy(checkInTime = item.requestedCheckIn)
                                    }
                                    if (item.requestedCheckOut != null) {
                                        val inTime = item.requestedCheckIn ?: rec.checkInTime
                                        val deltaHrs = (item.requestedCheckOut - inTime).toDouble() / 3600000.0
                                        val workedHrs = Math.max(0.1, Math.round(deltaHrs * 100.0) / 100.0)
                                        val otHrs = if (workedHrs > 12.0) (workedHrs - 12.0) else 0.0
                                        updatedRec = updatedRec.copy(
                                            checkOutTime = item.requestedCheckOut,
                                            workedHours = workedHrs,
                                            overtimeHours = otHrs,
                                            status = "PRESENT"
                                        )
                                    }
                                    updatedRec
                                } else rec
                            }
                        }
                    }
                    it.copy(status = "SITE_ADMIN_APPROVED", auditLog = it.auditLog + listOf("Bulk approved by supervisor."))
                } else it
            }
        }
        showToast("Bulk Pipeline: Approved $pendingCount correction requests.")
    }

    fun generateAttendanceReportRequest(type: String, format: String, siteId: String, month: String) {
        val user = _currentUser.value ?: return
        val id = "rep_" + UUID.randomUUID().toString().take(6)
        val downloadPrefix = when (format.uppercase()) {
            "PDF" -> "pdf"
            "EXCEL" -> "xlsx"
            else -> "csv"
        }
        val report = FirestoreAttendanceReport(
            id = id,
            type = type,
            generatedBy = user.name.ifEmpty { "Administrator" },
            format = format,
            timestamp = System.currentTimeMillis(),
            downloadUrl = "https://example.com/downloads/${type.lowercase()}_rep_${month.lowercase().replace(" ", "_")}.$downloadPrefix",
            filterSiteId = siteId,
            filterMonth = month
        )
        _firestoreAttendanceReports.update { listOf(report) + it }
        showToast("System compiling $type report as $format...")
    }

    fun saveConfig(config: FirestoreAttendanceConfig) {
        _firestoreAttendanceConfigs.update { list ->
            val index = list.indexOfFirst { it.siteId == config.siteId && it.shiftId == config.shiftId }
            if (index == -1) {
                list + config
            } else {
                list.map { if (it.siteId == config.siteId && it.shiftId == config.shiftId) config else it }
            }
        }
        showToast("Attendance Policy set for ${config.siteName}")
    }

    fun generateQrCode(siteId: String, shiftId: String) {
        val current = System.currentTimeMillis()
        val codeId = "qr_" + UUID.randomUUID().toString().take(6)
        val newQr = FirestoreAttendanceQrCode(
            id = codeId,
            siteId = siteId,
            siteName = _firestoreSites.value.firstOrNull { it.id == siteId }?.siteName ?: "Client Site",
            shiftId = shiftId,
            shiftName = "Assigned Shift",
            qrContent = "SEC-QR-$siteId-" + UUID.randomUUID().toString().take(4).uppercase(),
            isActive = true,
            generatedAt = current,
            expiresAt = current + 86400000
        )
        _firestoreAttendanceQrCodes.update { listOf(newQr) + it }
        showToast("Regenerated secure dynamic QR code.")
    }

    private fun createAttendanceException(
        employeeId: String,
        employeeName: String,
        siteId: String,
        siteName: String,
        type: String,
        desc: String,
        recId: String = "att_record_err"
    ) {
        val newExc = FirestoreAttendanceException(
            id = "exc_" + UUID.randomUUID().toString().take(6),
            recordId = recId,
            employeeId = employeeId,
            employeeName = employeeName,
            siteId = siteId,
            siteName = siteName,
            exceptionType = type,
            description = desc,
            status = "PENDING",
            timestamp = System.currentTimeMillis(),
            remarks = ""
        )
        _firestoreAttendanceExceptions.update { listOf(newExc) + it }
    }

    private fun calculateDistanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371e3 // Earth radius in meters
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaPhi = Math.toRadians(lat2 - lat1)
        val deltaLambda = Math.toRadians(lon2 - lon1)
        val a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
                Math.cos(phi1) * Math.cos(phi2) *
                Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    // ==========================================
    // PHASE 17: VISITOR OPERATIONS
    // ==========================================

    fun registerVisitor(visitor: FirestoreVisitor): String? {
        // Step 7: Check blacklist (Hard Block)
        val isMobileBlacklisted = _blacklist.value.any { it.type == "VISITOR" && it.targetValue == visitor.mobile.trim() }
        val isNameBlacklisted = _blacklist.value.any { it.type == "VISITOR" && it.targetName.equals(visitor.fullName.trim(), ignoreCase = true) }
        
        if (isMobileBlacklisted || isNameBlacklisted) {
            val matchedReason = _blacklist.value.firstOrNull { 
                (it.type == "VISITOR" && it.targetValue == visitor.mobile.trim()) || 
                (it.type == "VISITOR" && it.targetName.equals(visitor.fullName.trim(), ignoreCase = true))
            }?.reason ?: "Security Protocol Hard Block"
            
            showToast("❌ HARD BLOCK: Visitor ${visitor.fullName} is BLACKLISTED! Reason: $matchedReason")
            logSystemAudit("BLACKLIST_BLOCKED_ATTEMPT", "Visitor ${visitor.fullName} (Mobile: ${visitor.mobile}) was blocked at gate", "", matchedReason)
            return "Visitor of identity and mobile is blacklisted! Reason: $matchedReason"
        }

        val uniqueId = "vis_${System.currentTimeMillis()}"
        val passId = "pass_$uniqueId"
        
        val newVisitor = visitor.copy(
            id = uniqueId,
            qrPassId = passId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        _visitors.update { listOf(newVisitor) + it }

        // If registered straight into INSIDE (e.g. support delivery or immediate bypass), setup details
        if (newVisitor.status == "INSIDE") {
            _visitorPasses.update { passes ->
                passes + FirestoreVisitorPass(
                    id = passId,
                    visitorId = uniqueId,
                    visitorName = newVisitor.fullName,
                    siteId = newVisitor.siteId,
                    hostName = newVisitor.hostName,
                    qrContent = "SEC-PASS-$uniqueId",
                    passType = if (newVisitor.visitorType == "Delivery Executive") "Event Pass" else "Guest Pass",
                    status = "USED"
                )
            }
            // Auto create gate register and evacuation entry
            _evacuationRegister.update { list ->
                list + FirestoreEvacuationEntry(
                    id = "ev_$uniqueId",
                    name = newVisitor.fullName,
                    type = "VISITOR",
                    detail = "${newVisitor.visitorType} • ${newVisitor.unitFlatOffice}",
                    checkInTime = System.currentTimeMillis()
                )
            }
            logSystemAudit("VISITOR_CHECK_IN_AUTO", "Visitor ${newVisitor.fullName} checked-in. Created QR Pass, Gate Register, Audit entry automatically.", "", "OK")
        } else if (newVisitor.status == "APPROVED") {
            _visitorPasses.update { passes ->
                passes + FirestoreVisitorPass(
                    id = passId,
                    visitorId = uniqueId,
                    visitorName = newVisitor.fullName,
                    siteId = newVisitor.siteId,
                    hostName = newVisitor.hostName,
                    qrContent = "SEC-PASS-$uniqueId",
                    passType = "Guest Pass",
                    status = "ACTIVE"
                )
            }
        }

        showToast("✓ Visitor ${newVisitor.fullName} registered successfully [Status: ${newVisitor.status}]")
        return null
    }

    fun approveVisitor(visitorId: String, method: String) {
        _visitors.update { currentList ->
            currentList.map { vis ->
                if (vis.id == visitorId) {
                    val passId = vis.qrPassId ?: "pass_${vis.id}"
                    // Auto create passes if missing
                    val hasPass = _visitorPasses.value.any { it.id == passId }
                    if (!hasPass) {
                        _visitorPasses.update { passes ->
                            passes + FirestoreVisitorPass(
                                id = passId,
                                visitorId = vis.id,
                                visitorName = vis.fullName,
                                siteId = vis.siteId,
                                hostName = vis.hostName,
                                qrContent = "SEC-PASS-${vis.id}",
                                passType = "Guest Pass",
                                status = "ACTIVE"
                            )
                        }
                    }
                    vis.copy(status = "APPROVED", hostApprovalMethod = method, qrPassId = passId, updatedAt = System.currentTimeMillis())
                } else vis
            }
        }
        showToast("✓ Visitor approved via $method")
        logSystemAudit("VISITOR_HOST_APPROVAL", "Visitor $visitorId approved via $method", "PENDING", "APPROVED")
    }

    fun rejectVisitor(visitorId: String) {
        _visitors.update { currentList ->
            currentList.map { vis ->
                if (vis.id == visitorId) {
                    vis.copy(status = "REJECTED", updatedAt = System.currentTimeMillis())
                } else vis
            }
        }
        showToast("✗ Visitor pass rejected.")
    }

    fun checkInVisitor(visitorId: String, remarks: String = "") {
        val vis = _visitors.value.firstOrNull { it.id == visitorId } ?: return
        
        // Blacklist hard block re-check
        val isMobileBlacklisted = _blacklist.value.any { it.type == "VISITOR" && it.targetValue == vis.mobile.trim() }
        if (isMobileBlacklisted) {
            showToast("❌ hard block: Blacklisted mobile check triggered during checkin!")
            return
        }

        _visitors.update { currentList ->
            currentList.map { item ->
                if (item.id == visitorId) {
                    item.copy(
                        status = "INSIDE", 
                        checkInTime = System.currentTimeMillis(),
                        remarks = remarks,
                        updatedAt = System.currentTimeMillis()
                    )
                } else item
            }
        }

        // Generate or Update Pass Status
        val passId = vis.qrPassId ?: "pass_${vis.id}"
        _visitorPasses.update { passes ->
            val hasPass = passes.any { it.id == passId }
            if (hasPass) {
                passes.map { p -> if (p.id == passId) p.copy(status = "USED") else p }
            } else {
                passes + FirestoreVisitorPass(
                    id = passId,
                    visitorId = vis.id,
                    visitorName = vis.fullName,
                    siteId = vis.siteId,
                    hostName = vis.hostName,
                    qrContent = "SEC-PASS-${vis.id}",
                    passType = "Guest Pass",
                    status = "USED"
                )
            }
        }

        // Auto-create Gate Register / Evacuation entry
        _evacuationRegister.update { list ->
            list.filter { it.id != "ev_$visitorId" } + FirestoreEvacuationEntry(
                id = "ev_$visitorId",
                name = vis.fullName,
                type = "VISITOR",
                detail = "${vis.visitorType} • ${vis.unitFlatOffice}",
                checkInTime = System.currentTimeMillis()
            )
        }

        showToast("✓ Visitor ${vis.fullName} has Checked-In to premises.")
        logSystemAudit("VISITOR_CHECK_IN", "Visitor ${vis.fullName} officially checked-in at lobby gates.", "APPROVED", "INSIDE")
    }

    fun checkOutVisitor(visitorId: String) {
        val vis = _visitors.value.firstOrNull { it.id == visitorId } ?: return
        
        _visitors.update { currentList ->
            currentList.map { item ->
                if (item.id == visitorId) {
                    item.copy(
                        status = "EXITED", 
                        checkOutTime = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                } else item
            }
        }

        // Update Pass
        val passId = vis.qrPassId ?: "pass_${vis.id}"
        _visitorPasses.update { passes ->
            passes.map { p -> if (p.id == passId) p.copy(status = "EXPIRED") else p }
        }

        // Remove from Evacuation Register
        _evacuationRegister.update { list ->
            list.filter { it.id != "ev_$visitorId" }
        }

        showToast("✓ Visitor ${vis.fullName} has Checked-Out from premises.")
        logSystemAudit("VISITOR_CHECK_OUT", "Visitor ${vis.fullName} exited secure perimeter.", "INSIDE", "EXITED")
    }

    // --- Blacklist Operations ---
    fun addBlacklistEntry(entry: FirestoreBlacklistEntry) {
        val uid = "bl_${System.currentTimeMillis()}"
        val newEntry = entry.copy(id = uid, createdAt = System.currentTimeMillis())
        _blacklist.update { listOf(newEntry) + it }
        showToast("✓ Added to security Blacklist: ${entry.targetName}")
        logSystemAudit("BLACKLIST_ADD", "Blacklist created for ${entry.targetName} (${entry.type})", "", entry.reason)
    }

    fun removeBlacklistEntry(id: String) {
        val entry = _blacklist.value.firstOrNull { it.id == id }
        _blacklist.update { list -> list.filter { it.id != id } }
        if (entry != null) {
            showToast("✓ Removed ${entry.targetName} from Blacklist")
            logSystemAudit("BLACKLIST_REMOVE", "Lifted blacklist constraints for ${entry.targetName}", entry.reason, "REMOVED")
        }
    }

    // --- Delivery Operations ---
    fun registerDelivery(delivery: FirestoreDelivery): String? {
        val isBlocked = _blacklist.value.any { it.type == "DELIVERY" && it.targetValue.equals(delivery.deliveryCompany, ignoreCase = true) }
        if (isBlocked) {
            val reason = _blacklist.value.firstOrNull { it.type == "DELIVERY" && it.targetValue.equals(delivery.deliveryCompany, ignoreCase = true) }?.reason ?: "Company is restricted"
            showToast("❌ delivery company blocked: $reason")
            return "This delivery company is blacklisted! Reason: $reason"
        }

        val uid = "del_${System.currentTimeMillis()}"
        val newDel = delivery.copy(id = uid, checkInTime = System.currentTimeMillis(), status = "INSIDE")
        _deliveries.update { listOf(newDel) + it }

        // Evacuation Register
        _evacuationRegister.update { list ->
            list + FirestoreEvacuationEntry(
                id = "ev_$uid",
                name = "${newDel.deliveryCompany} Cargo",
                type = "DELIVERY",
                detail = "Order ${newDel.orderNumber} • ${newDel.recipientUnit}",
                checkInTime = System.currentTimeMillis()
            )
        }

        showToast("✓ Registered ${newDel.deliveryCompany} Delivery inside premises.")
        logSystemAudit("DELIVERY_IN", "Delivery parcel checked-in. Company: ${newDel.deliveryCompany}", "", "INSIDE")
        return null
    }

    fun checkOutDelivery(deliveryId: String) {
        _deliveries.update { current ->
            current.map { d ->
                if (d.id == deliveryId) d.copy(checkOutTime = System.currentTimeMillis(), status = "EXITED") else d
            }
        }
        _evacuationRegister.update { list ->
            list.filter { it.id != "ev_$deliveryId" }
        }
        showToast("✓ Checked out Delivery executive.")
    }

    // --- Contractor & Worker Operations ---
    fun registerContractor(contractor: FirestoreContractor) {
        val isBlocked = _blacklist.value.any { it.type == "CONTRACTOR" && it.targetValue.equals(contractor.companyName, ignoreCase = true) }
        if (isBlocked) {
            val reason = _blacklist.value.firstOrNull { it.type == "CONTRACTOR" && it.targetValue.equals(contractor.companyName, ignoreCase = true) }?.reason ?: "Restricted"
            showToast("❌ contractor block: Company ${contractor.companyName} is blacklisted: $reason")
            return
        }

        val uid = "ctr_${System.currentTimeMillis()}"
        val newCont = contractor.copy(id = uid)
        _contractors.value = listOf(newCont) + _contractors.value
        showToast("✓ Contractor company ${contractor.companyName} registered.")
    }

    fun registerWorker(worker: FirestoreContractorWorker) {
        val uid = "cw_${System.currentTimeMillis()}"
        val newWorker = worker.copy(id = uid, status = "OUT")
        _contractorWorkers.value = listOf(newWorker) + _contractorWorkers.value
        showToast("✓ Contractor worker ${worker.workerName} profile registered.")
    }

    fun checkInContractorWorker(workerId: String) {
        val w = _contractorWorkers.value.firstOrNull { it.id == workerId } ?: return
        val isBlocked = _blacklist.value.any { it.type == "CONTRACTOR" && it.targetValue.equals(w.companyName, ignoreCase = true) }
        if (isBlocked) {
            showToast("❌ Hard block: Worker is affiliated with blacklisted contractor!")
            return
        }

        _contractorWorkers.update { current ->
            current.map { item ->
                if (item.id == workerId) item.copy(status = "INSIDE", checkInTime = System.currentTimeMillis()) else item
            }
        }

        // Evacuation Register update
        _evacuationRegister.update { list ->
            list.filter { it.id != "ev_$workerId" } + FirestoreEvacuationEntry(
                id = "ev_$workerId",
                name = w.workerName,
                type = "CONTRACTOR_WORKER",
                detail = "${w.companyName} worker",
                checkInTime = System.currentTimeMillis()
            )
        }

        showToast("✓ Contractor worker ${w.workerName} checked-in.")
        logSystemAudit("CONTRACTOR_WORKER_IN", "Contractor worker: ${w.workerName} entered building.", "", "INSIDE")
    }

    fun checkOutContractorWorker(workerId: String) {
        val w = _contractorWorkers.value.firstOrNull { it.id == workerId } ?: return
        _contractorWorkers.update { current ->
            current.map { item ->
                if (item.id == workerId) item.copy(status = "OUT", checkOutTime = System.currentTimeMillis()) else item
            }
        }
        _evacuationRegister.update { list ->
            list.filter { it.id != "ev_$workerId" }
        }
        showToast("✓ Contractor worker ${w.workerName} checked-out.")
        logSystemAudit("CONTRACTOR_WORKER_OUT", "Contractor worker: ${w.workerName} has exited.", "", "OUT")
    }

    // --- Vehicle Management ---
    fun registerVehicle(vehicle: FirestoreVehicle): String? {
        val isBlocked = _blacklist.value.any { it.type == "VEHICLE" && it.targetValue.equals(vehicle.vehicleNumber.trim(), ignoreCase = true) }
        if (isBlocked) {
            val reason = _blacklist.value.firstOrNull { it.type == "VEHICLE" && it.targetValue.equals(vehicle.vehicleNumber.trim(), ignoreCase = true) }?.reason ?: "Speeding/parking violation"
            showToast("❌ Vehicle is restricted from entry! Reason: $reason")
            return "This vehicle license plate is blacklisted! Reason: $reason"
        }

        val uid = "v_${System.currentTimeMillis()}"
        val newV = vehicle.copy(id = uid, checkInTime = System.currentTimeMillis())
        _vehicles.update { listOf(newV) + it }
        showToast("✓ Vehicle ${vehicle.vehicleNumber} checked-in to secure lot.")
        logSystemAudit("VEHICLE_CHECK_IN", "Inbound vehicle ${vehicle.vehicleNumber} checked-in.", "", "INSIDE")
        return null
    }

    fun checkOutVehicle(vehicleId: String) {
        _vehicles.update { current ->
            current.map { v ->
                if (v.id == vehicleId) v.copy(checkOutTime = System.currentTimeMillis()) else v
            }
        }
        showToast("✓ Vehicle checked-out successfully.")
    }

    // --- Pre-Approved Visitor Passes ---
    fun createPreApprovedPass(name: String, phone: String, siteId: String, host: String, passType: String, durationDays: Int) {
        val uniqueVisId = "vis_pre_${System.currentTimeMillis()}"
        val passId = "pass_pre_${System.currentTimeMillis()}"
        val now = System.currentTimeMillis()

        val visitorObj = FirestoreVisitor(
            id = uniqueVisId,
            siteId = siteId,
            fullName = name,
            mobile = phone,
            visitorType = if (passType.contains("Event")) "Group Guest" else "Guest",
            hostName = host,
            unitFlatOffice = "Resident Confirmed",
            status = "APPROVED",
            qrPassId = passId,
            hostApprovalMethod = "Resident Mobile Pre-Auth",
            createdAt = now,
            updatedAt = now
        )

        val passObj = FirestoreVisitorPass(
            id = passId,
            visitorId = uniqueVisId,
            visitorName = name,
            siteId = siteId,
            hostName = host,
            qrContent = "PRE-SEC-PASS-${System.currentTimeMillis()}",
            passType = passType,
            status = "ACTIVE",
            validFrom = now,
            validUntil = now + 86400000L * durationDays
        )

        _visitors.update { listOf(visitorObj) + it }
        _visitorPasses.update { listOf(passObj) + it }
        showToast("✓ Issued pre-approved $passType for $name.")
        logSystemAudit("PRE_PASS_CREATE", "Resident created pre-approved $passType. Valid for $durationDays days.", "", "ACTIVE")
    }

    // --- Evacuation Command & Visitor Reports ---
    fun triggerEmergencyEvacuationRegistry() {
        val now = System.currentTimeMillis()
        val reportId = "vrep_${System.currentTimeMillis()}"
        val activeParticipantsCount = _evacuationRegister.value.size
        
        val newReport = FirestoreVisitorReport(
            id = reportId,
            type = "EVACUATION",
            generatedBy = "System Emergency Trigger",
            timestamp = now,
            format = "PDF",
            contentSummary = "CRITICAL: Captured exact roll-call snapshot of $activeParticipantsCount entities inside perimeter during emergency assembly call.",
            downloadUrl = "https://example.com/downloads/emergency_evacuation_snap_${System.currentTimeMillis()}.pdf"
        )

        _visitorReports.update { listOf(newReport) + it }
        showToast("🚨 EMERGENCY ASSEMBLY: Snapshot and PDF generated for $activeParticipantsCount inside entities!")
        logSystemAudit("EMERGENCY_EVAC_CAPTURE", "Assembly roll-call PDF snapshot completed for $activeParticipantsCount entities inside.", "", "PDF")
    }

    fun generateVisitorCustomReport(type: String) {
        val now = System.currentTimeMillis()
        val reportId = "vrep_${System.currentTimeMillis()}"
        
        val summary = when (type) {
            "DAILY" -> "Processed ${_visitors.value.size} total visitor logs today, ${_blacklist.value.size} active blacklisted vectors watched."
            "MONTHLY" -> "Monthly report aggregate - ${_visitors.value.size + 140} visits inside tenant boundary scopes."
            "BLACKLIST" -> "Blacklist compliance register: ${_blacklist.value.size} hard blocks activated."
            "VEHICLE" -> "Vehicle gate throughput log: ${_vehicles.value.size} active inside."
            else -> "Standard perimeter activity logs consolidated."
        }

        val report = FirestoreVisitorReport(
            id = reportId,
            type = type,
            generatedBy = "Supervisor Admin",
            timestamp = now,
            format = "PDF",
            contentSummary = summary,
            downloadUrl = "https://example.com/downloads/v3_visitor_${type.lowercase()}_$now.pdf"
        )

        _visitorReports.update { listOf(report) + it }
        showToast("✓ $type Report compiled & PDF download simulated.")
        logSystemAudit("REPORTS_VISITOR", "Visitor telemetry $type PDF created successfully.", "", "OK")
    }

    fun logSystemAudit(action: String, details: String, oldValue: String, newValue: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.logAudit(
                action = action,
                details = details,
                targetId = "system_onb",
                performedBy = user.name.ifEmpty { "system" },
                oldValue = oldValue,
                newValue = newValue,
                siteId = "global_sys",
                companyId = user.companyId.ifEmpty { "cmp_centurion_sec" },
                companyGroupId = user.companyGroupId.ifEmpty { "grp_kundal_global" }
            )
        }
    }
}
