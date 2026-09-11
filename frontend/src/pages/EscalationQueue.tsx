import { useEffect, useState } from 'react'
import { getEscalations } from '../api/escalations'
import type { EscalationResponse } from '../types/api'

function formatDateTime(value: string | null) {
  if (!value) {
    return '—'
  }

  return new Intl.DateTimeFormat('en-IN', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

export function EscalationQueue() {
  const [escalations, setEscalations] =
    useState<EscalationResponse[]>([])

  const [loading, setLoading] = useState(true)

  const [error, setError] =
    useState<string | null>(null)

  useEffect(() => {
    let active = true

    async function loadEscalations() {
      try {
        setLoading(true) 
        setError(null)

        const response = await getEscalations()

        if (!active) {
          return
        }

        setEscalations(response)
      } catch (err) {
        if (!active) {
          return
        }

        setError(
          err instanceof Error
            ? err.message
            : 'Unable to load escalations.',
        )
      } finally {
        if (active) {
          setLoading(false)
        }
      }
    }

    void loadEscalations()

    return () => {
      active = false
    }
  }, [])

  return (
    <>
      <section className="page-heading">
        <div>
          <p className="section-kicker">
            HUMAN OVERSIGHT
          </p>

          <h2>Escalation Queue</h2>

          <p className="section-description">
            Review decisions that require human attention.
          </p>
        </div>

        <span className="count-badge">
          {loading ? '—' : escalations.length}
        </span>
      </section>

      {loading && (
        <section
          className="state-panel"
          aria-live="polite"
        >
          <div className="spinner" />

          <h3>Loading escalation queue</h3>

          <p>
            Fetching decisions that require human
            attention.
          </p>
        </section>
      )}

      {!loading && error && (
        <section
          className="state-panel error-panel"
          role="alert"
        >
          <span className="state-icon">!</span>

          <h3>Escalation queue unavailable</h3>

          <p>{error}</p>
        </section>
      )}

      {!loading && !error && escalations.length === 0 && (
        <section className="content-panel">
          <div className="empty-state">
            <h4>No escalations</h4>

            <p>
              There are currently no decisions waiting
              for human attention.
            </p>
          </div>
        </section>
      )}

      {!loading && !error && escalations.length > 0 && (
        <section className="content-panel">
          <div className="panel-header">
            <div>
              <p className="section-kicker">
                REVIEW REQUIRED
              </p>

              <h3>Pending Escalations</h3>
            </div>

            <span className="count-badge">
              {escalations.length}
            </span>
          </div>

          <div className="table-wrapper">
            <table className="decision-table">
              <thead>
                <tr>
                  <th>Agent Run</th>
                  <th>Round</th>
                  <th>Decision</th>
                  <th>Reason</th>
                  <th>Sequence</th>
                  <th>Created</th>
                </tr>
              </thead>

              <tbody>
                {escalations.map((escalation) => (
                  <tr
                    key={escalation.auditEventId}
                  >
                    <td>
                      #{escalation.agentRunId ?? '—'}
                    </td>

                    <td>
                      #{escalation.agentRoundId ?? '—'}
                    </td>

                    <td>
                      <strong>
                        {escalation.decision}
                      </strong>
                    </td>

                    <td>
                      {escalation.reason ?? '—'}
                    </td>

                    <td>
                      {escalation.sequenceNumber}
                    </td>

                    <td>
                      {formatDateTime(
                        escalation.createdAt,
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}
    </>
  )
}