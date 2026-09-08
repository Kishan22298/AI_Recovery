import type {
  AuditEventResponse,
} from '../types/api'

import type {
  CandidateStrategiesData,
  DecisionDetails,
  ExpectedValueData,
  FinalDecisionData,
  PolicyData,
  PropensityData,
  RankingData,
} from '../types/decision'

function parseEventData<T>(
  event: AuditEventResponse,
): T | null {
  if (!event.eventData) {
    return null
  }

  try {
    return JSON.parse(event.eventData) as T
  } catch {
    return null
  }
}

export function parseDecisionEvents(
  events: AuditEventResponse[],
): DecisionDetails {
  const details: DecisionDetails = {
    candidates: null,
    propensity: null,
    expectedValue: null,
    ranking: null,
    policy: null,
    finalDecision: null,
  }

  for (const event of events) {
    switch (event.eventType) {
      case 'CANDIDATE_STRATEGIES':
        details.candidates =
          parseEventData<CandidateStrategiesData>(
            event,
          )
        break

      case 'PROPENSITY':
        details.propensity =
          parseEventData<PropensityData>(event)
        break

      case 'EXPECTED_VALUE':
        details.expectedValue =
          parseEventData<ExpectedValueData>(event)
        break

      case 'RANKING':
        details.ranking =
          parseEventData<RankingData>(event)
        break

      case 'POLICY':
        details.policy =
          parseEventData<PolicyData>(event)
        break

      case 'FINAL_DECISION':
        details.finalDecision =
          parseEventData<FinalDecisionData>(event)
        break

      default:
        break
    }
  }

  return details
}