export interface MetricsResponse {
  outstandingAmount: number
  recoveredAmount: number
  recoveryRate: number
  interventionCount: number
  blockedCount: number
  escalations: number
  baselineRecoveredAmount: number
  agentRecoveredAmount: number
  incrementalRecovery: number
}

export interface InvoiceResponse {
  id: number
  externalRef: string
  customerId: number | null
  customerReference: string | null
  customerName: string | null
  customerEmail: string | null
  customerPhone: string | null
  totalAmount: number
  outstandingAmount: number
  currency: string
  issueDate: string
  dueDate: string
  status: string
  description: string | null
}

export interface AgentRunResponse {
  id: number
  invoiceId: number | null
  invoiceReference: string | null
  status: string | null
  maxRounds: number
  startedAt: string | null
  completedAt: string | null
  createdAt: string | null
}

export interface AgentRoundResponse {
  id: number
  agentRunId: number | null
  roundNumber: number
  status: string | null
  diagnosisCategory: string | null
  propensityScore: number | null
  selectedStrategy: string | null
  authorized: boolean
  startedAt: string | null
  completedAt: string | null
}

export interface AuditEventResponse {
  id: number
  agentRunId: number | null
  agentRoundId: number | null
  sequenceNumber: number
  eventType: string | null
  actor: string | null
  eventData: string | null
  createdAt: string | null
}

export interface EscalationResponse {
  auditEventId: number
  agentRunId: number | null
  agentRoundId: number | null
  sequenceNumber: number
  decision: string
  reason: string | null
  createdAt: string | null
}

export type AgentEventType =
  | 'RUN_STARTED'
  | 'INVOICE_STARTED'
  | 'OBSERVATION_CREATED'
  | 'DIAGNOSIS_COMPLETED'
  | 'DECISION_CALCULATED'
  | 'POLICY_BLOCKED'
  | 'DECISION_AUTHORIZED'
  | 'EXECUTION_COMPLETED'
  | 'OUTCOME_RECEIVED'
  | 'INVOICE_COMPLETED'
  | 'RUN_COMPLETED'

export interface AgentEvent {
  eventId: string
  agentRunId: number
  roundId: number | null
  eventType: AgentEventType
  timestamp: string
  payload: Record<string, unknown>
}

export interface PaymentResponse {
  id: number
  amount: number
  currency: string
  receivedAt: string
  status: string
  reference: string | null
}

export interface InterventionOutcomeResponse {
  id: number
  agentRoundId: number | null
  outcomeType: string
  recoveredAmount: number
  notes: string | null
  occurredAt: string
}

export interface PromiseToPayResponse {
  id: number
  promisedAmount: number
  promisedDate: string
  status: string
  brokenAt: string | null
  createdAt: string
}