import { useEffect, useState } from 'react'
import type {
  AgentEvent,
  AgentEventType,
} from '../types/api'

const EVENT_TYPES: AgentEventType[] = [
  'RUN_STARTED',
  'INVOICE_STARTED',
  'OBSERVATION_CREATED',
  'DIAGNOSIS_COMPLETED',
  'DECISION_CALCULATED',
  'POLICY_BLOCKED',
  'DECISION_AUTHORIZED',
  'EXECUTION_COMPLETED',
  'OUTCOME_RECEIVED',
  'INVOICE_COMPLETED',
  'RUN_COMPLETED',
]

interface AgentRunEventsState {
  events: AgentEvent[]
  connected: boolean
  error: string | null
}

export function useAgentRunEvents(
  agentRunId: number | null,
): AgentRunEventsState {
  const [events, setEvents] = useState<AgentEvent[]>([])
  const [connected, setConnected] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (agentRunId === null) {
      setEvents([])
      setConnected(false)
      setError(null)
      return
    }

    const source = new EventSource(
      `/api/agent-runs/${agentRunId}/events`,
    )

    setEvents([])
    setError(null)

    const handleEvent = (message: MessageEvent) => {
      try {
        const event = JSON.parse(message.data) as AgentEvent

        setEvents((current) => [...current, event])
      } catch {
        setError('Received an invalid event from the agent stream.')
      }
    }

    for (const eventType of EVENT_TYPES) {
      source.addEventListener(eventType, handleEvent)
    }

    source.onopen = () => {
      setConnected(true)
      setError(null)
    }

    source.onerror = () => {
      setConnected(false)
      setError('The live agent event stream disconnected.')
    }

    return () => {
      for (const eventType of EVENT_TYPES) {
        source.removeEventListener(eventType, handleEvent)
      }

      source.close()
    }
  }, [agentRunId])

  return {
    events,
    connected,
    error,
  }
}