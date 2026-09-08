import { get } from './client'
import type { EscalationResponse } from '../types/api'

export function getEscalations(): Promise<EscalationResponse[]> {
  return get<EscalationResponse[]>('/escalations')
}