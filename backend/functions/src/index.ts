import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

// Initialize firebase admin wrapper
if (admin.apps.length === 0) {
  admin.initializeApp();
}

const db = admin.firestore();

// ==========================================
// 6-PHASE CLOUD FUNCTION PIPELINE MIDDLEWARE
// ==========================================

export interface PipelineContext {
  uid: string;
  tokenId: string;
  role: string;
  companyGroupId: string;
  companyId: string;
  siteId: string;
}

/**
 * PHASE 1: Authenticates request and produces user claims.
 */
function assertAuthenticated(context: functions.https.CallableContext): PipelineContext {
  if (!context.auth) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'The requested action requires valid user credentials.'
    );
  }

  // Safely extract Custom Claims from Token payload
  const token = context.auth.token;
  return {
    uid: context.auth.uid,
    tokenId: context.auth.uid,
    role: (token.role as string) || 'guard',
    companyGroupId: (token.companyGroupId as string) || '',
    companyId: (token.companyId as string) || '',
    siteId: (token.siteId as string) || ''
  };
}

/**
 * PHASE 2: Asserts user is authorized based on standard RBAC matrix.
 */
function assertRole(allowedRoles: string[], userContext: PipelineContext) {
  if (userContext.role === 'super_admin') {
    return; // Bypass validation
  }

  if (!allowedRoles.includes(userContext.role)) {
    throw new functions.https.HttpsError(
      'permission-denied',
      `Access Denied: Current role [${userContext.role}] lacks clearance for this action.`
    );
  }
}

/**
 * PHASE 3: Checks incoming inputs for structural type compliance.
 */
function validateInput<T>(schemaValidator: (data: any) => T, inputData: any): T {
  try {
    return schemaValidator(inputData);
  } catch (error: any) {
    throw new functions.https.HttpsError(
      'invalid-argument',
      `Input Schema Error: ${error.message || 'Payload validation failed.'}`
    );
  }
}

/**
 * PHASE 4: Enforces strict data scope boundaries for cross-tenant operations.
 */
function verifyTenantScope(user: PipelineContext, entityTenantGroupId: string, entityTenantCompanyId: string, entityTenantSiteId?: string) {
  if (user.role === 'super_admin') {
    return; // System overrides allowed
  }

  // Block any operations addressing separate company groups
  if (user.companyGroupId !== entityTenantGroupId) {
    throw new functions.https.HttpsError(
      'permission-denied',
      'Scope Violation: Target Company Group boundary is inaccessible.'
    );
  }

  // Bounded Company Admin isolation check
  if (user.role === 'company_admin' && user.companyId !== entityTenantCompanyId) {
    throw new functions.https.HttpsError(
      'permission-denied',
      'Scope Violation: Target Company boundary is inaccessible.'
    );
  }

  // Bounded Site Admin/Guard isolation check
  if (
    ['site_admin', 'guard'].includes(user.role) &&
    entityTenantSiteId &&
    user.siteId !== entityTenantSiteId &&
    user.siteId !== ''
  ) {
    throw new functions.https.HttpsError(
      'permission-denied',
      'Scope Violation: Target Client Site boundary is inaccessible.'
    );
  }
}

/**
 * PHASE 6: Centralized, Immutable Audit logging emitter.
 */
async function writeAuditLog(params: {
  eventType: string;
  userContext: PipelineContext;
  entityType: string;
  entityId: string;
  before: any;
  after: any;
}) {
  const auditRef = db.collection('audit_logs').doc();
  const logDocument = {
    id: auditRef.id,
    eventType: params.eventType,
    companyGroupId: params.userContext.companyGroupId,
    companyId: params.userContext.companyId,
    siteId: params.userContext.siteId,
    entityType: params.entityType,
    entityId: params.entityId,
    performedBy: params.userContext.uid,
    timestamp: Date.now(),
    before: params.before || null,
    after: params.after || null
  };

  // Safe fire-and-forget append pipeline
  await auditRef.set(logDocument);
}

// ==========================================
// HTTPS CALLABLE API ENDPOINT SKELETON EXAMPLES
// ==========================================

interface UpdateSitePayload {
  siteId: string;
  name: string;
  address: string;
}

/**
 * Standard Operational Refactored Function: 'updateSite'
 * Fully complies with Phase 5 standardization rules.
 */
export const updateSite = functions.https.onCall(async (data, context) => {
  // 1. assertAuthenticated()
  const caller = assertAuthenticated(context);

  // 2. assertRole()
  assertRole(['super_admin', 'group_admin', 'company_admin'], caller);

  // 3. validateInput()
  const payload = validateInput<UpdateSitePayload>((input) => {
    if (!input.siteId || typeof input.siteId !== 'string') {
      throw new Error('siteId is required and must be a string.');
    }
    if (!input.name || typeof input.name !== 'string') {
      throw new Error('name is required and must be a string.');
    }
    if (!input.address || typeof input.address !== 'string') {
      throw new Error('address is required and must be a string.');
    }
    return input;
  }, data);

  // Retrieve current state of Site from DB
  const siteRef = db.collection('sites').doc(payload.siteId);
  const siteSnap = await siteRef.get();
  if (!siteSnap.exists) {
    throw new functions.https.HttpsError('not-found', 'Site does not exist.');
  }
  const currentSite = siteSnap.data()!;

  // 4. verifyTenantScope()
  verifyTenantScope(
    caller,
    currentSite.companyGroupId,
    currentSite.companyId,
    payload.siteId
  );

  const beforeData = { ...currentSite };
  const afterData = {
    ...currentSite,
    name: payload.name,
    address: payload.address,
    updatedAt: Date.now(),
    updatedBy: caller.uid
  };

  // 5. execute() - Database update transaction
  await siteRef.update(afterData);

  // 6. writeAuditLog()
  await writeAuditLog({
    eventType: 'SITE_UPDATED',
    userContext: caller,
    entityType: 'Site',
    entityId: payload.siteId,
    before: beforeData,
    after: afterData
  });

  return { success: true, updatedSiteId: payload.siteId };
});
