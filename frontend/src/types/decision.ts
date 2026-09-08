export interface CandidateStrategiesData {
  strategies: string[]
}

export interface PropensityData {
  selectedStrategy: string
  probabilityOfSuccess: number
}

export interface ExpectedValueStrategy {
  strategy: string
  expectedValue: number
  interventionCost: number
  outstandingAmount: number
  probabilityOfSuccess: number
}

export interface ExpectedValueData {
  strategies: ExpectedValueStrategy[]
}

export interface RankingItem {
  rank: number
  strategy: string
  expectedValue: number
}

export interface RankingData {
  ranking: RankingItem[]
}

export interface PolicyData {
  reason: string
  decision: string
  selectedStrategy: string | null
}

export interface FinalDecisionData {
  reason: string
  decision: string
  selectedStrategy: string | null
}

export interface DecisionDetails {
  candidates: CandidateStrategiesData | null
  propensity: PropensityData | null
  expectedValue: ExpectedValueData | null
  ranking: RankingData | null
  policy: PolicyData | null
  finalDecision: FinalDecisionData | null
}