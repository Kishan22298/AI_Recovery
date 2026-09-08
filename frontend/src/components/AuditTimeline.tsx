import { useEffect, useState } from 'react'
import { getRunAudit } from '../api/audit'
import type { AuditEventResponse } from '../types/api'

interface AuditTimelineProps {
  agentRunId: number
}

function formatTimestamp(value: string | null) {
  if (!value) {
    return '—'
  }

  return new Date(value).toLocaleString()
}

function eventLabel(eventType: string | null) {
  if (!eventType) {
    return 'Unknown Event'
  }

  return eventType
    .replaceAll('_', ' ')
    .toLowerCase()
    .replace(/\b\w/g, (character) => character.toUpperCase())
}

function parseEventData(eventData: string | null) {
  if (!eventData) {
    return null
  }

  try {
    return JSON.parse(eventData) as Record<string, unknown>
  } catch {
    return null
  }
}

function formatValue(value: unknown): string {
  if (value === null || value === undefined) {
    return '—'
  }

  if (typeof value === 'object') {
    return JSON.stringify(value, null, 2)
  }

  return String(value)
}

export function AuditTimeline({
  agentRunId,
}: AuditTimelineProps) {
  const [events, setEvents] = useState<AuditEventResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false

    async function loadAudit() {
      try {
        setLoading(true)
        setError(null)

        const data = await getRunAudit(agentRunId)

        if (!cancelled) {
          setEvents(data)
        }
      } catch (requestError) {
        if (!cancelled) {
          setError(
            requestError instanceof Error
              ? requestError.message
              : 'Unable to load audit timeline.',
          )
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    void loadAudit()

    return () => {
      cancelled = true
    }
  }, [agentRunId])

  if (loading) {
    return (
      <section className="panel audit-timeline-panel">
        <div className="state-message">
          Loading audit timeline...
        </div>
      </section>
    )
  }

  if (error) {
    return (
      <section className="panel audit-timeline-panel">
        <div className="error-message">{error}</div>
      </section>
    )
  }

  return (
    <section className="panel audit-timeline-panel">
      <div className="panel-header">
        <div>
          <p className="eyebrow">AUDIT TIMELINE</p>
          <h2>Decision Audit History</h2>
          <p>
            Recorded events for Agent Run #{agentRunId}
          </p>
        </div>

        <span className="count-badge">
          {events.length}
        </span>
      </div>

      {events.length === 0 ? (
        <div className="state-message">
          No audit events are available for this run.
        </div>
      ) : (
        <div className="audit-timeline">
          {events.map((event) => {
            const eventData = parseEventData(event.eventData)

            return (
              <article
                className="audit-event"
                key={event.id}
              >
                <div className="audit-sequence">
                  {event.sequenceNumber}
                </div>

                <div className="audit-event-body">
                  <div className="audit-event-header">
                    <div>
                      <h3>
                        {eventLabel(event.eventType)}
                      </h3>

                      <span className="audit-event-type">
                        {event.eventType ?? 'UNKNOWN'}
                      </span>
                    </div>

                    <time>
                      {formatTimestamp(event.createdAt)}
                    </time>
                  </div>

                  <div className="audit-event-meta">
                    <span>
                      Actor: {event.actor ?? '—'}
                    </span>

                    {event.agentRoundId !== null && (
                      <span>
                        Round: {event.agentRoundId}
                      </span>
                    )}
                  </div>

                  {eventData && (
                    <div className="audit-event-data">
                      {Object.entries(eventData).map(
                        ([key, value]) => (
                          <div
                            className="audit-data-row"
                            key={key}
                          >
                            <span>{key}</span>

                            <strong>
                              {formatValue(value)}
                            </strong>
                          </div>
                        ),
                      )}
                    </div>
                  )}

                  {!eventData && event.eventData && (
                    <pre className="audit-raw-data">
                      {event.eventData}
                    </pre>
                  )}
                </div>
              </article>
            )
          })}
        </div>
      )}
    </section>
  )
}