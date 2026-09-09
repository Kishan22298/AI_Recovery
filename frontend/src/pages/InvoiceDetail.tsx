import { useEffect, useState } from 'react'
import {
  getInvoice,
  getInvoiceAgentRuns,
  getInvoicePayments,
  getInvoiceInterventions,
  getInvoicePromises,
} from '../api/invoices'
import type {
  AgentRunResponse,
  InvoiceResponse,
  PaymentResponse,
  InterventionOutcomeResponse,
  PromiseToPayResponse,
} from '../types/api'
import { AuditTimeline } from '../components/AuditTimeline'

interface InvoiceDetailProps {
  invoiceId: number
  onBack: () => void
}

function formatCurrency(amount: number, currency: string) {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency,
    maximumFractionDigits: 2,
  }).format(amount)
}

function formatDateTime(value: string) {
  return new Date(value).toLocaleString('en-IN')
}

export function InvoiceDetail({
  invoiceId,
  onBack,
}: InvoiceDetailProps) {
  const [invoice, setInvoice] =
    useState<InvoiceResponse | null>(null)

  const [agentRuns, setAgentRuns] =
    useState<AgentRunResponse[]>([])

  const [payments, setPayments] =
    useState<PaymentResponse[]>([])

  const [interventions, setInterventions] =
    useState<InterventionOutcomeResponse[]>([])

  const [promises, setPromises] =
    useState<PromiseToPayResponse[]>([])

  const [loading, setLoading] = useState(true)

  const [error, setError] =
    useState<string | null>(null)

  useEffect(() => {
    let cancelled = false

    async function loadInvoice() {
      try {
        setLoading(true)
        setError(null)

        const [
          invoiceData,
          agentRunsData,
          paymentsData,
          interventionsData,
          promisesData,
        ] = await Promise.all([
          getInvoice(invoiceId),
          getInvoiceAgentRuns(invoiceId),
          getInvoicePayments(invoiceId),
          getInvoiceInterventions(invoiceId),
          getInvoicePromises(invoiceId),
        ])

        if (!cancelled) {
          setInvoice(invoiceData)
          setAgentRuns(agentRunsData)
          setPayments(paymentsData)
          setInterventions(interventionsData)
          setPromises(promisesData)
        }
      } catch (requestError) {
        if (!cancelled) {
          setError(
            requestError instanceof Error
              ? requestError.message
              : 'Unable to load invoice.',
          )
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    void loadInvoice()

    return () => {
      cancelled = true
    }
  }, [invoiceId])

  if (loading) {
    return (
      <section className="page-section">
        <div className="state-message">
          Loading invoice...
        </div>
      </section>
    )
  }

  if (error) {
    return (
      <section className="page-section">
        <button
          className="secondary-button"
          type="button"
          onClick={onBack}
        >
          ← Back to Dashboard
        </button>

        <div className="error-message">
          {error}
        </div>
      </section>
    )
  }

  if (!invoice) {
    return (
      <section className="page-section">
        <button
          className="secondary-button"
          type="button"
          onClick={onBack}
        >
          ← Back to Dashboard
        </button>

        <div className="state-message">
          Invoice not found.
        </div>
      </section>
    )
  }

  const latestAgentRun =
    agentRuns.length > 0
      ? agentRuns[agentRuns.length - 1]
      : null

  return (
    <section className="invoice-detail-page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">INVOICE DETAIL</p>

          <h1>{invoice.externalRef}</h1>

          <p className="page-subtitle">
            Detailed invoice information and recovery
            state.
          </p>
        </div>

        <button
          className="secondary-button"
          type="button"
          onClick={onBack}
        >
          ← Back to Dashboard
        </button>
      </div>

      <div className="invoice-detail-grid">
        <section className="panel">
          <div className="panel-header">
            <div>
              <h2>Invoice Information</h2>

              <p>
                Core invoice and recovery information.
              </p>
            </div>
          </div>

          <div className="detail-grid">
            <div>
              <span>Invoice Reference</span>
              <strong>{invoice.externalRef}</strong>
            </div>

            <div>
              <span>Customer Reference</span>
              <strong>
                {invoice.customerReference ?? '—'}
              </strong>
            </div>

            <div>
              <span>Status</span>
              <strong>
                {invoice.status ?? '—'}
              </strong>
            </div>

            <div>
              <span>Currency</span>
              <strong>{invoice.currency}</strong>
            </div>

            <div>
              <span>Issue Date</span>
              <strong>{invoice.issueDate}</strong>
            </div>

            <div>
              <span>Due Date</span>
              <strong>{invoice.dueDate}</strong>
            </div>
          </div>
        </section>

        <section className="panel">
          <div className="panel-header">
            <div>
              <h2>Customer Information</h2>

              <p>
                Customer contact and account details.
              </p>
            </div>
          </div>

          <div className="detail-grid">
            <div>
              <span>Customer</span>
              <strong>
                {invoice.customerName ?? '—'}
              </strong>
            </div>

            <div>
              <span>Customer ID</span>
              <strong>
                {invoice.customerId ?? '—'}
              </strong>
            </div>

            <div>
              <span>Email</span>
              <strong>
                {invoice.customerEmail ?? '—'}
              </strong>
            </div>

            <div>
              <span>Phone</span>
              <strong>
                {invoice.customerPhone ?? '—'}
              </strong>
            </div>
          </div>
        </section>

        <section className="panel">
          <div className="panel-header">
            <div>
              <h2>Amounts</h2>

              <p>
                Current financial state of the invoice.
              </p>
            </div>
          </div>

          <div className="amount-summary">
            <div>
              <span>Total Amount</span>

              <strong>
                {formatCurrency(
                  invoice.totalAmount,
                  invoice.currency,
                )}
              </strong>
            </div>

            <div>
              <span>Outstanding Amount</span>

              <strong>
                {formatCurrency(
                  invoice.outstandingAmount,
                  invoice.currency,
                )}
              </strong>
            </div>
          </div>
        </section>

        {invoice.description && (
          <section className="panel">
            <div className="panel-header">
              <div>
                <h2>Description</h2>
              </div>
            </div>

            <p className="invoice-description">
              {invoice.description}
            </p>
          </section>
        )}
      </div>

      <section className="panel">
        <div className="panel-header">
          <div>
            <h2>Payment History</h2>

            <p>
              Payments recorded against this invoice.
            </p>
          </div>
        </div>

        {payments.length === 0 ? (
          <div className="state-message">
            No payments recorded for this invoice.
          </div>
        ) : (
          <div className="data-table-wrapper">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Reference</th>
                  <th>Amount</th>
                  <th>Status</th>
                </tr>
              </thead>

              <tbody>
                {payments.map((payment) => (
                  <tr key={payment.id}>
                    <td>
                      {formatDateTime(
                        payment.receivedAt,
                      )}
                    </td>

                    <td>
                      {payment.reference ?? '—'}
                    </td>

                    <td>
                      {formatCurrency(
                        payment.amount,
                        payment.currency,
                      )}
                    </td>

                    <td>{payment.status}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className="panel">
        <div className="panel-header">
          <div>
            <h2>Intervention History</h2>

            <p>
              Recovery interventions and their outcomes.
            </p>
          </div>
        </div>

        {interventions.length === 0 ? (
          <div className="state-message">
            No intervention outcomes recorded.
          </div>
        ) : (
          <div className="data-table-wrapper">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Occurred</th>
                  <th>Round</th>
                  <th>Outcome</th>
                  <th>Recovered</th>
                  <th>Notes</th>
                </tr>
              </thead>

              <tbody>
                {interventions.map((intervention) => (
                  <tr key={intervention.id}>
                    <td>
                      {formatDateTime(
                        intervention.occurredAt,
                      )}
                    </td>

                    <td>
                      {intervention.agentRoundId ?? '—'}
                    </td>

                    <td>
                      {intervention.outcomeType}
                    </td>

                    <td>
                      {formatCurrency(
                        intervention.recoveredAmount,
                        invoice.currency,
                      )}
                    </td>

                    <td>
                      {intervention.notes ?? '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className="panel">
        <div className="panel-header">
          <div>
            <h2>Promise to Pay</h2>

            <p>
              Customer commitments associated with the
              invoice.
            </p>
          </div>
        </div>

        {promises.length === 0 ? (
          <div className="state-message">
            No promises to pay recorded.
          </div>
        ) : (
          <div className="data-table-wrapper">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Promised Date</th>
                  <th>Amount</th>
                  <th>Status</th>
                  <th>Broken At</th>
                  <th>Created</th>
                </tr>
              </thead>

              <tbody>
                {promises.map((promise) => (
                  <tr key={promise.id}>
                    <td>
                      {promise.promisedDate}
                    </td>

                    <td>
                      {formatCurrency(
                        promise.promisedAmount,
                        invoice.currency,
                      )}
                    </td>

                    <td>{promise.status}</td>

                    <td>
                      {promise.brokenAt
                        ? formatDateTime(
                            promise.brokenAt,
                          )
                        : '—'}
                    </td>

                    <td>
                      {formatDateTime(
                        promise.createdAt,
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {latestAgentRun && (
        <AuditTimeline
          agentRunId={latestAgentRun.id}
        />
      )}
    </section>
  )
}