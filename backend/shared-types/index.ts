// ==========================================
// KUNDAL SECURITY OS V3 – SHARED SCHEMAS & INTERFACES
// Single Source of Truth for Tenancy Bounds
// ==========================================

export interface BaseAuditFields {
  id: string;
  companyGroupId: string;
  companyId: string;
  siteId: string;
  status: string;    // E.g., 'Active', 'Pending', 'Suspended', 'Archived'
  createdAt: number; // UTC Milliseconds Epoch
  updatedAt: number; // UTC Milliseconds Epoch
  createdBy: string; // User UUID
  updatedBy: string; // User UUID
  version: number;   // Document mutation sequencing version
}

// 1. USER REPRESENTATION (RBAC Bound)
export interface User extends BaseAuditFields {
  name: string;
  email: string;
  role: 'super_admin' | 'group_admin' | 'company_admin' | 'site_admin' | 'guard' | 'resident' | 'client_manager' | 'client_user';
  phoneNumber?: string;
  avatarUrl?: string;
}

// 2. COMPANY GROUP (Ultimate Tenancy Bound)
export interface CompanyGroup extends Omit<BaseAuditFields, 'companyId' | 'siteId'> {
  name: string;
  corporateRegistrationNumber: string;
}

// 3. UNDERLYING ENTERPRISE SECURITY COMPANY
export interface Company extends Omit<BaseAuditFields, 'siteId'> {
  name: string;
  vatTaxNumber: string;
  address: string;
}

// 4. CLIENT OPERATIONS SITE (Deployable Guards Bound)
export interface Site extends BaseAuditFields {
  name: string;
  address: string;
  latitude: number;
  longitude: number;
  geoRadiusMeters: number; // Fence range for mobile Clock In matching
  slaContractId?: string;
}

// 5. PHYSICAL SENSING ZONE OR UNIT
export interface Unit extends BaseAuditFields {
  name: string;
  checkpointQRToken: string; // Token parsed to verify physically tagged patrols
  floorLevel?: string;
}

// 6. REAL-TIME ROSTER ATTENDANCE
export interface Attendance extends BaseAuditFields {
  userId: string;
  userName: string;
  role: string;
  clockInTime: number;
  clockOutTime?: number;
  clockInGeo: {
    latitude: number;
    longitude: number;
  };
  clockOutGeo?: {
    latitude: number;
    longitude: number;
  };
}

// 7. SECURE VISITOR INCOMING REGISTRATION 
export interface Visitor extends BaseAuditFields {
  name: string;
  idNumber: string; // Cryptographically hashed or masked input
  purpose: string;
  hostUnitId: string; // Binds visiting flow directly to a residential/office Unit
  hostUserId: string;
  checkInTime: number;
  checkOutTime?: number;
}

// 8. SECURITY EXCEPTION LOG (INCIDENT)
export interface Incident extends BaseAuditFields {
  title: string;
  severity: 'low' | 'medium' | 'high' | 'critical';
  description: string;
  reportedBy: string;
  reporterName: string;
  resolutionNotes?: string;
}

// 9. FINANCIAL PAYROLL RECORD
export interface Payroll extends BaseAuditFields {
  userId: string;
  userName: string;
  month: string;           // Formatted as YYYY-MM
  hourlyRate: number;      // Bounded by Company SLA Rates
  hoursWorked: number;
  grossAmount: number;
  deductions: number;
  netPayAmount: number;
  directDepositConfirmedAt?: number;
}

// 10. CLIENT BILLING INVOICE
export interface Invoice extends BaseAuditFields {
  clientId: string;
  month: string;           // Formatted as YYYY-MM
  totalAmountDue: number;
  dueDate: number;
  lineItems: Array<{
    description: string;
    quantity: number;
    unitPrice: number;
    amount: number;
  }>;
}

// 11. CUSTOMER RATE CONTRACT
export interface Contract extends BaseAuditFields {
  contractTitle: string;
  termsDetails: string;
  contractStartDate: number;
  contractEndDate: number;
  serviceRatePerHour: number;
}

// 12. ROSTER LEAVE REQUEST
export interface LeaveRequest extends BaseAuditFields {
  userId: string;
  leaveType: 'sick' | 'vacation' | 'unpaid' | 'compassionate';
  startDate: number;
  endDate: number;
  approvalNotes?: string;
}

// 13. HARDWARE CAPABILITY ASSET
export interface Asset extends BaseAuditFields {
  assetName: string;
  assetSerial: string;
  assetType: 'vehicle' | 'radio' | 'bodycam' | 'tracker';
  assignedUserId?: string;
}

// 14. EMERGENCY NOTIFICATION SYSTEM
export interface Notification extends BaseAuditFields {
  recipientId: string;
  notificationTitle: string;
  notificationBody: string;
  priority: 'info' | 'alert' | 'critical';
  readAt?: number;
}
