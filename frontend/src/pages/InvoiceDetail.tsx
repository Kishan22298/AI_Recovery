import { useEffect, useState } from 'react'
import { getInvoice } from '../api/invoices'
import { getInvoiceAgentRuns } from '../api/agentRuns'
import type {
  AgentRunResponse,
  InvoiceResponse,
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

export function InvoiceDetail({
  invoiceId,
  onBack,
}: InvoiceDetailProps) {
  const [invoice, setInvoice] =
    useState<InvoiceResponse | null>(null)

  const [loading, setLoading] =
    useState(true)

  const [error, setError] =
    useState<string | null>(null)

  const [agentRuns, setAgentRuns] =
  useState<AgentRunResponse[]>([])

  useEffect(() => {
    let cancelled = false

    async function loadInvoice() {
      try {
        setLoading(true)
        setError(null)

        const [invoiceData, agentRunsData] =
  await Promise.all([
    getInvoice(invoiceId),
    getInvoiceAgentRuns(invoiceId),
  ])

if (!cancelled) {
  setInvoice(invoiceData)
  setAgentRuns(agentRunsData)
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
          <p className="eyebrow">
            INVOICE DETAIL
          </p>

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
                Core invoice and customer information.
              </p>
            </div>
          </div>

          <div className="detail-grid">
            <div>
              <span>Invoice Reference</span>

              <strong>
                {invoice.externalRef}
              </strong>
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

              <strong>
                {invoice.currency}
              </strong>
            </div>

            <div>
              <span>Issue Date</span>

              <strong>
                {invoice.issueDate}
              </strong>
            </div>

            <div>
              <span>Due Date</span>

              <strong>
                {invoice.dueDate}
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

      {latestAgentRun && (
  <AuditTimeline agentRunId={latestAgentRun.id} />
)}
    </section>
  )
}