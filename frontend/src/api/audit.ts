import { get } from './client'
import type { AuditEventResponse } from '../types/api'

export function getRunAudit(
  agentRunId: number,
): Promise<AuditEventResponse[]> {
  return get<AuditEventResponse[]>(
    `/agent-runs/${agentRunId}/audit`,
  )
}

export function getRoundAudit(
  agentRoundId: number,
): Promise<AuditEventResponse[]> {
  return get<AuditEventResponse[]>(
    `/agent-rounds/${agentRoundId}/audit`,
  )
}

export function getDecisions(
  agentRunId: number,
): Promise<AuditEventResponse[]> {
  return get<AuditEventResponse[]>(
    `/agent-runs/${agentRunId}/decisions`,
  )
}