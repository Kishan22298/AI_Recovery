import { get } from './client'
import type { MetricsResponse } from '../types/api'

export function getMetrics(): Promise<MetricsResponse> {
  return get<MetricsResponse>('/metrics')
}