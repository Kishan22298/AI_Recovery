import { useEffect, useState } from 'react'
import { getInvoices } from './api/invoices'
import { getMetrics } from './api/metrics'
import { InvoiceDetail } from './pages/InvoiceDetail'
import { LiveAgentRun } from './components/LiveAgentRun'
import { EscalationQueue } from './pages/EscalationQueue'
import type {
  InvoiceResponse,
  MetricsResponse,
} from './types/api'
import './App.css'

function formatCurrency(value: number, currency = 'INR') {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency,
    maximumFractionDigits: 2,
  }).format(value)
}

function formatPercent(value: number) {
  return `${(value * 100).toFixed(1)}%`
}

function MetricCard({
  label,
  value,
  detail,
}: {
  label: string
  value: string
  detail?: string
}) {
  return (
    <article className="metric-card">
      <span className="metric-label">{label}</span>
      <strong className="metric-value">{value}</strong>
      {detail && (
        <span className="metric-detail">{detail}</span>
      )}
    </article>
  )
}

function App() {
  const [metrics, setMetrics] =
    useState<MetricsResponse | null>(null)

  const [invoices, setInvoices] =
    useState<InvoiceResponse[]>([])

  const [loading, setLoading] = useState(true)

  const [error, setError] =
    useState<string | null>(null)

  const [page, setPage] = useState<
  'dashboard' | 'live-run' | 'invoice-detail' | 'escalations'
>('dashboard')

  const [selectedInvoiceId, setSelectedInvoiceId] =
    useState<number | null>(null)

  useEffect(() => {
    let active = true

    async function loadDashboard() {
      try {
        setLoading(true)
        setError(null)

        const [
          metricsResponse,
          invoicesResponse,
        ] = await Promise.all([
          getMetrics(),
          getInvoices(),
        ])

        if (!active) {
          return
        }

        setMetrics(metricsResponse)
        setInvoices(invoicesResponse)
      } catch (err) {
        if (!active) {
          return
        }

        setError(
          err instanceof Error
            ? err.message
            : 'Unable to load dashboard data.',
        )
      } finally {
        if (active) {
          setLoading(false)
        }
      }
    }

    void loadDashboard()

    return () => {
      active = false
    }
  }, [])

  return (
    <div className="app-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">
            RECEIVABLES AUTOMATION
          </p>

          <h1>ReceivablesGuard</h1>
        </div>

        <nav className="main-nav">
          <button
            type="button"
            className={
              page === 'dashboard'
                ? 'nav-active'
                : ''
            }
            onClick={() =>
              setPage('dashboard')
            }
          >
            Dashboard
          </button>

          <button
            type="button"
            className={
              page === 'live-run'
                ? 'nav-active'
                : ''
            }
            onClick={() =>
              setPage('live-run')
            }
          >
            Live Agent Run
          </button>

          <button
            type="button"
            className={
              page === 'escalations'
                ? 'nav-active'
                : ''
            }
            onClick={() =>
              setPage('escalations')
            }
          >
            Escalation
          </button>
        </nav>

        <div className="simulation-badge">
          <span className="status-dot" />
          Simulation Mode
        </div>
      </header>

      <main className="dashboard">
        {page === 'dashboard' && (
          <>
            <section className="page-heading">
              <div>
                <p className="section-kicker">
                  CONTROL CENTER
                </p>

                <h2>Recovery Dashboard</h2>

                <p className="section-description">
                  Monitor recovery performance, agent
                  activity and policy decisions.
                </p>
              </div>
            </section>

            {loading && (
              <section
                className="state-panel"
                aria-live="polite"
              >
                <div className="spinner" />

                <h3>Loading dashboard</h3>

                <p>
                  Fetching current recovery metrics
                  and invoices.
                </p>
              </section>
            )}

            {!loading && error && (
              <section
                className="state-panel error-panel"
                role="alert"
              >
                <span className="state-icon">!</span>

                <h3>Dashboard unavailable</h3>

                <p>{error}</p>
              </section>
            )}

            {!loading &&
              !error &&
              metrics && (
                <>
                  <section className="metric-grid">
                    <MetricCard
                      label="Recovered"
                      value={formatCurrency(
                        metrics.recoveredAmount,
                      )}
                      detail={`Recovery rate ${formatPercent(
                        metrics.recoveryRate,
                      )}`}
                    />

                    <MetricCard
                      label="Baseline Recovery"
                      value={formatCurrency(
                        metrics.baselineRecoveredAmount,
                      )}
                      detail="Baseline comparison"
                    />

                    <MetricCard
                      label="Incremental Recovery"
                      value={formatCurrency(
                        metrics.incrementalRecovery,
                      )}
                      detail="Agent vs baseline"
                    />

                    <MetricCard
                      label="Agent Recovery"
                      value={formatCurrency(
                        metrics.agentRecoveredAmount,
                      )}
                      detail="Agent-attributed recovery"
                    />
                  </section>

                  <section className="metric-grid secondary-grid">
                    <MetricCard
                      label="Outstanding"
                      value={formatCurrency(
                        metrics.outstandingAmount,
                      )}
                    />

                    <MetricCard
                      label="Interventions"
                      value={String(
                        metrics.interventionCount,
                      )}
                    />

                    <MetricCard
                      label="Blocked"
                      value={String(
                        metrics.blockedCount,
                      )}
                    />

                    <MetricCard
                      label="Escalations"
                      value={String(
                        metrics.escalations,
                      )}
                    />
                  </section>

                  <section className="content-panel">
                    <div className="panel-header">
                      <div>
                        <p className="section-kicker">
                          PORTFOLIO
                        </p>

                        <h3>Invoices</h3>
                      </div>

                      <span className="count-badge">
                        {invoices.length}
                      </span>
                    </div>

                    {invoices.length === 0 ? (
                      <div className="empty-state">
                        <h4>No invoices</h4>

                        <p>
                          There are currently no
                          invoices available to
                          display.
                        </p>
                      </div>
                    ) : (
                      <div className="invoice-list">
                        {invoices.map(
                          (invoice) => (
                            <article
                              className="invoice-row"
                              key={invoice.id}
                            >
                              <div className="invoice-main">
                                <strong>
                                  {invoice.externalRef}
                                </strong>

                                <span>
                                  {invoice.customerReference ??
                                    'Unknown customer'}
                                </span>
                              </div>

                              <div className="invoice-amount">
                                <strong>
                                  {formatCurrency(
                                    invoice.outstandingAmount,
                                    invoice.currency,
                                  )}
                                </strong>

                                <span>
                                  Outstanding
                                </span>
                              </div>

                              <span
                                className={`status-pill status-${(
                                  invoice.status ??
                                  'unknown'
                                ).toLowerCase()}`}
                              >
                                {invoice.status ??
                                  'UNKNOWN'}
                              </span>

                              <button
                                className="secondary-button"
                                type="button"
                                onClick={() => {
                                  setSelectedInvoiceId(
                                    invoice.id,
                                  )
                                  setPage(
                                    'invoice-detail',
                                  )
                                }}
                              >
                                View Invoice
                              </button>
                            </article>
                          ),
                        )}
                      </div>
                    )}
                  </section>
                </>
              )}
          </>
        )}

        {page === 'live-run' && (
          <LiveAgentRun />
        )}

        {page === 'invoice-detail' &&
          selectedInvoiceId !== null && (
            <InvoiceDetail
              invoiceId={selectedInvoiceId}
              onBack={() =>
                setPage('dashboard')
              }
            />
          )}

        {page === 'escalations' && (
          <EscalationQueue />
        )}
      </main>
    </div>
  )
}

export default App