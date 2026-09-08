import { useEffect, useState } from 'react'
import { getDecisions } from '../api/audit'
import { parseDecisionEvents } from '../api/decisionParser'
import type { AuditEventResponse } from '../types/api'
import type { DecisionDetails } from '../types/decision'

interface DecisionDetailProps {
  agentRunId: number
}

function formatCurrency(amount: number) {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
  }).format(amount)
}

export function DecisionDetail({
  agentRunId,
}: DecisionDetailProps) {
  const [details, setDetails] = useState<DecisionDetails | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false

    async function loadDecisionDetails() {
      try {
        setLoading(true)
        setError(null)

        const events: AuditEventResponse[] =
          await getDecisions(agentRunId)

        if (!cancelled) {
          setDetails(parseDecisionEvents(events))
        }
      } catch (requestError) {
        if (!cancelled) {
          setError(
            requestError instanceof Error
              ? requestError.message
              : 'Unable to load decision details.',
          )
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    void loadDecisionDetails()

    return () => {
      cancelled = true
    }
  }, [agentRunId])

  if (loading) {
    return (
      <section className="panel decision-detail-panel">
        <div className="state-message">
          Loading decision details...
        </div>
      </section>
    )
  }

  if (error) {
    return (
      <section className="panel decision-detail-panel">
        <div className="error-message">{error}</div>
      </section>
    )
  }

  if (!details) {
    return null
  }

  const hasDecisionData =
    details.candidates ||
    details.propensity ||
    details.expectedValue ||
    details.ranking ||
    details.policy ||
    details.finalDecision

  if (!hasDecisionData) {
    return (
      <section className="panel decision-detail-panel">
        <div className="panel-header">
          <div>
            <p className="eyebrow">DECISION DETAIL</p>
            <h2>Decision Details</h2>
          </div>
        </div>

        <div className="state-message">
          No decision details are available for this run.
        </div>
      </section>
    )
  }

  return (
    <section className="panel decision-detail-panel">
      <div className="panel-header">
        <div>
          <p className="eyebrow">DECISION DETAIL</p>
          <h2>Agent Decision</h2>
          <p>
            Deterministic decision data recorded for Agent Run #{agentRunId}
          </p>
        </div>
      </div>

      {details.candidates && (
        <div className="decision-section">
          <div className="decision-section-heading">
            <h3>Candidate Strategies</h3>
            <span>{details.candidates.strategies.length} candidates</span>
          </div>

          <div className="strategy-chips">
            {details.candidates.strategies.map((strategy) => (
              <span className="strategy-chip" key={strategy}>
                {strategy}
              </span>
            ))}
          </div>
        </div>
      )}

      {details.propensity && (
        <div className="decision-section">
          <div className="decision-section-heading">
            <h3>Propensity</h3>
          </div>

          <div className="decision-highlight">
            <div>
              <span>Selected Strategy</span>
              <strong>{details.propensity.selectedStrategy}</strong>
            </div>

            <div>
              <span>Probability of Success</span>
              <strong>
                {(details.propensity.probabilityOfSuccess * 100).toFixed(1)}%
              </strong>
            </div>
          </div>
        </div>
      )}

      {details.expectedValue && (
        <div className="decision-section">
          <div className="decision-section-heading">
            <h3>Expected Value</h3>
            <span>Deterministic calculation</span>
          </div>

          <div className="table-wrapper">
            <table className="decision-table">
              <thead>
                <tr>
                  <th>Strategy</th>
                  <th>Probability</th>
                  <th>Expected Value</th>
                  <th>Intervention Cost</th>
                </tr>
              </thead>

              <tbody>
                {details.expectedValue.strategies.map((strategy) => (
                  <tr key={strategy.strategy}>
                    <td>{strategy.strategy}</td>
                    <td>
                      {(strategy.probabilityOfSuccess * 100).toFixed(1)}%
                    </td>
                    <td>{formatCurrency(strategy.expectedValue)}</td>
                    <td>{formatCurrency(strategy.interventionCost)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {details.ranking && (
        <div className="decision-section">
          <div className="decision-section-heading">
            <h3>Ranking</h3>
            <span>Highest expected value first</span>
          </div>

          <div className="ranking-list">
            {details.ranking.ranking.map((item) => (
              <div className="ranking-row" key={item.strategy}>
                <span className="ranking-number">
                  #{item.rank}
                </span>

                <strong>{item.strategy}</strong>

                <span>
                  {formatCurrency(item.expectedValue)}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      {details.policy && (
        <div className="decision-section">
          <div className="decision-section-heading">
            <h3>Policy Result</h3>
          </div>

          <div className="policy-result">
            <div>
              <span>Decision</span>
              <strong>{details.policy.decision}</strong>
            </div>

            <div>
              <span>Selected Strategy</span>
              <strong>
                {details.policy.selectedStrategy ?? 'None'}
              </strong>
            </div>

            <div>
              <span>Reason</span>
              <p>{details.policy.reason}</p>
            </div>
          </div>
        </div>
      )}

      {details.finalDecision && (
        <div className="decision-section final-decision-section">
          <div className="decision-section-heading">
            <h3>Final Decision</h3>
          </div>

          <div className="final-decision">
            <div>
              <span>Decision</span>
              <strong>{details.finalDecision.decision}</strong>
            </div>

            <div>
              <span>Action</span>
              <strong>
                {details.finalDecision.selectedStrategy ?? 'No action'}
              </strong>
            </div>

            <div>
              <span>Reason</span>
              <p>{details.finalDecision.reason}</p>
            </div>
          </div>
        </div>
      )}
    </section>
  )
}