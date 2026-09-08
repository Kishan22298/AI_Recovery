import { get, post } from './client'
import type {
  AgentRoundResponse,
  AgentRunResponse,
} from '../types/api'

export function startAgentRun(
  invoiceReference: string,
  maxRounds = 1,
): Promise<AgentRunResponse> {
  const params = new URLSearchParams({
    invoiceReference,
    maxRounds: String(maxRounds),
  })

  return post<AgentRunResponse>(
    `/agent-runs?${params.toString()}`,
  )
}

export function getAgentRun(
  agentRunId: number,
): Promise<AgentRunResponse> {
  return get<AgentRunResponse>(
    `/agent-runs/${agentRunId}`,
  )
}

export function getAgentRunRounds(
  agentRunId: number,
): Promise<AgentRoundResponse[]> {
  return get<AgentRoundResponse[]>(
    `/agent-runs/${agentRunId}/rounds`,
  )
}

export function getAgentRound(
  roundId: number,
): Promise<AgentRoundResponse> {
  return get<AgentRoundResponse>(
    `/agent-rounds/${roundId}`,
  )
}

export function cancelAgentRun(
  agentRunId: number,
): Promise<string> {
  return post<string>(
    `/agent-runs/${agentRunId}/cancel`,
  )
}

export function getInvoiceAgentRuns(
  invoiceId: number,
): Promise<AgentRunResponse[]> {
  return get<AgentRunResponse[]>(
    `/invoices/${invoiceId}/agent-runs`,
  )
}