import { useEffect, useMemo, useState } from 'react'
import { DecisionDetail } from './DecisionDetail'
import {
  cancelAgentRun,
  getAgentRun,
  startAgentRun,
} from '../api/agentRuns'
import { getInvoices } from '../api/invoices'
import type {
  AgentEvent,
  AgentRunResponse,
  InvoiceResponse,
} from '../types/api'
import { useAgentRunEvents } from '../hooks/useAgentRunEvents'

function formatCurrency(amount: number, currency: string) {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency,
    maximumFractionDigits: 2,
  }).format(amount)
}

function formatTimestamp(value: string | null) {
  if (!value) {
    return '—'
  }

  return new Date(value).toLocaleString()
}

function eventLabel(eventType: string) {
  return eventType
    .replaceAll('_', ' ')
    .toLowerCase()
    .replace(/\b\w/g, (character) => character.toUpperCase())
}

function eventTone(eventType: string) {
  if (eventType === 'POLICY_BLOCKED') {
    return 'blocked'
  }

  if (eventType === 'RUN_COMPLETED' || eventType === 'INVOICE_COMPLETED') {
    return 'success'
  }

  if (
    eventType === 'DIAGNOSIS_COMPLETED' ||
    eventType === 'DECISION_CALCULATED' ||
    eventType === 'DECISION_AUTHORIZED'
  ) {
    return 'decision'
  }

  return 'neutral'
}

export function LiveAgentRun() {
  const [invoices, setInvoices] = useState<InvoiceResponse[]>([])
  const [selectedInvoiceId, setSelectedInvoiceId] = useState<number | null>(
    null,
  )
  const [run, setRun] = useState<AgentRunResponse | null>(null)
  const [maxRounds, setMaxRounds] = useState(1)
  const [loadingInvoices, setLoadingInvoices] = useState(true)
  const [starting, setStarting] = useState(false)
  const [cancelling, setCancelling] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const { events, connected, error: streamError } =
    useAgentRunEvents(run?.id ?? null)

  useEffect(() => {
    let cancelled = false

    async function loadInvoices() {
      try {
        setLoadingInvoices(true)
        setError(null)

        const data = await getInvoices()

        if (cancelled) {
          return
        }

        setInvoices(data)

        if (data.length > 0) {
          setSelectedInvoiceId((current) => current ?? data[0].id)
        }
      } catch (requestError) {
        if (!cancelled) {
          setError(
            requestError instanceof Error
              ? requestError.message
              : 'Unable to load invoices.',
          )
        }
      } finally {
        if (!cancelled) {
          setLoadingInvoices(false)
        }
      }
    }

    void loadInvoices()

    return () => {
      cancelled = true
    }
  }, [])

  const selectedInvoice = useMemo(
    () =>
      invoices.find((invoice) => invoice.id === selectedInvoiceId) ?? null,
    [invoices, selectedInvoiceId],
  )

  useEffect(() => {
    if (!run || !['CREATED', 'RUNNING'].includes(run.status ?? '')) {
      return
    }

    let cancelled = false

    const poll = async () => {
      try {
        const updated = await getAgentRun(run.id)

        if (!cancelled) {
          setRun(updated)
        }
      } catch (requestError) {
        if (!cancelled) {
          setError(
            requestError instanceof Error
              ? requestError.message
              : 'Unable to refresh the agent run.',
          )
        }
      }
    }

    void poll()

    const interval = window.setInterval(() => {
      void poll()
    }, 1500)

    return () => {
      cancelled = true
      window.clearInterval(interval)
    }
  }, [run])

  async function handleStart() {
    if (!selectedInvoice) {
      setError('Select an invoice before starting a run.')
      return
    }

    try {
      setStarting(true)
      setError(null)
      setRun(null)

      const createdRun = await startAgentRun(
        selectedInvoice.externalRef,
        maxRounds,
      )

      setRun(createdRun)
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : 'Unable to start the agent run.',
      )
    } finally {
      setStarting(false)
    }
  }

  async function handleCancel() {
    if (!run) {
      return
    }

    try {
      setCancelling(true)
      setError(null)

      await cancelAgentRun(run.id)

      const updatedRun = await getAgentRun(run.id)
      setRun(updatedRun)
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : 'Unable to cancel the agent run.',
      )
    } finally {
      setCancelling(false)
    }
  }

  const terminal =
    run !== null &&
    !['CREATED', 'RUNNING'].includes(run.status ?? '')

  return (
    <section className="live-agent-page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">LIVE AGENT</p>
          <h1>Live Agent Run</h1>
          <p className="page-subtitle">
            Start a simulated recovery run and watch its deterministic
            lifecycle in real time.
          </p>
        </div>
      </div>

      <div className="live-agent-grid">
        <section className="panel">
          <div className="panel-header">
            <div>
              <h2>Start a simulated recovery run</h2>
              <p>Select an invoice and choose the maximum number of rounds.</p>
            </div>
          </div>

          {loadingInvoices ? (
            <div className="state-message">Loading invoices...</div>
          ) : invoices.length === 0 ? (
            <div className="state-message">No invoices available.</div>
          ) : (
            <div className="run-form">
              <label>
                <span>Invoice</span>
                <select
                  value={selectedInvoiceId ?? ''}
                  onChange={(event) =>
                    setSelectedInvoiceId(Number(event.target.value))
                  }
                  disabled={starting}
                >
                  {invoices.map((invoice) => (
                    <option key={invoice.id} value={invoice.id}>
                      {invoice.externalRef} —{' '}
                      {formatCurrency(
                        invoice.outstandingAmount,
                        invoice.currency,
                      )}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                <span>Maximum rounds</span>
                <input
                  type="number"
                  min={1}
                  max={10}
                  value={maxRounds}
                  onChange={(event) =>
                    setMaxRounds(
                      Math.max(1, Math.min(10, Number(event.target.value))),
                    )
                  }
                  disabled={starting}
                />
              </label>

              <button
                className="primary-button"
                type="button"
                onClick={() => void handleStart()}
                disabled={starting || loadingInvoices || !selectedInvoice}
              >
                {starting ? 'Starting...' : 'Start Agent Run'}
              </button>
            </div>
          )}
        </section>

        {selectedInvoice && (
          <section className="panel">
            <div className="panel-header">
              <div>
                <h2>Current Invoice</h2>
                <p>{selectedInvoice.externalRef}</p>
              </div>
            </div>

            <div className="invoice-summary">
              <div>
                <span>Customer</span>
                <strong>
                  {selectedInvoice.customerReference ?? '—'}
                </strong>
              </div>

              <div>
                <span>Outstanding</span>
                <strong>
                  {formatCurrency(
                    selectedInvoice.outstandingAmount,
                    selectedInvoice.currency,
                  )}
                </strong>
              </div>

              <div>
                <span>Status</span>
                <strong>{selectedInvoice.status ?? '—'}</strong>
              </div>

              <div>
                <span>Due Date</span>
                <strong>{selectedInvoice.dueDate}</strong>
              </div>
            </div>
          </section>
        )}

        {run && (
          <section className="panel run-panel">
            <div className="panel-header">
              <div>
                <p className="eyebrow">RUN STATUS</p>
                <h2>Agent Run #{run.id}</h2>
              </div>

              <span
                className={`status-badge status-${(
                  run.status ?? 'UNKNOWN'
                ).toLowerCase()}`}
              >
                {run.status ?? 'UNKNOWN'}
              </span>
            </div>

            <div className="run-summary">
              <div>
                <span>Invoice</span>
                <strong>{run.invoiceReference ?? '—'}</strong>
              </div>

              <div>
                <span>Max rounds</span>
                <strong>{run.maxRounds}</strong>
              </div>

              <div>
                <span>Stream</span>
                <strong>{connected ? 'Connected' : 'Disconnected'}</strong>
              </div>

              <div>
                <span>Events</span>
                <strong>{events.length}</strong>
              </div>
            </div>

            {!terminal && (
              <button
                className="secondary-button danger-button"
                type="button"
                onClick={() => void handleCancel()}
                disabled={cancelling}
              >
                {cancelling ? 'Cancelling...' : 'Cancel Run'}
              </button>
            )}
          </section>
        )}

        {(error || streamError) && (
          <section className="error-message">
            {error ?? streamError}
          </section>
        )}

        {run && (
          <section className="panel event-panel">
            <div className="panel-header">
              <div>
                <p className="eyebrow">EVENT STREAM</p>
                <h2>Agent Timeline</h2>
              </div>

              <span
                className={`stream-indicator ${
                  connected ? 'connected' : 'disconnected'
                }`}
              >
                <span />
                {connected ? 'Connected' : 'Disconnected'}
              </span>
            </div>

            {events.length === 0 ? (
              <div className="state-message">
                Waiting for agent events...
              </div>
            ) : (
              <div className="event-timeline">
                {events.map((event: AgentEvent, index) => (
                  <article
                    className={`event-card event-${eventTone(
                      event.eventType,
                    )}`}
                    key={event.eventId}
                  >
                    <div className="event-number">{index + 1}</div>

                    <div className="event-content">
                      <div className="event-title-row">
                        <div>
                          <h3>{eventLabel(event.eventType)}</h3>
                          <span className="event-type">
                            {event.eventType}
                          </span>
                        </div>

                        <time>{formatTimestamp(event.timestamp)}</time>
                      </div>

                      <pre>
                        {JSON.stringify(event.payload, null, 2)}
                      </pre>
                    </div>
                  </article>
                ))}
              </div>
            )}
          </section>
        )}
	{run && terminal && (
          <DecisionDetail agentRunId={run.id} />
        )}
      </div>
    </section>
  )
}