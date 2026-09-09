import { get } from './client'
import type {
  InvoiceResponse,
  PaymentResponse,
  InterventionOutcomeResponse,
  PromiseToPayResponse,
} from '../types/api'

export function getInvoices(): Promise<InvoiceResponse[]> {
  return get<InvoiceResponse[]>('/invoices')
}

export function getInvoice(invoiceId: number): Promise<InvoiceResponse> {
  return get<InvoiceResponse>(`/invoices/${invoiceId}`)
}

export function getInvoiceAgentRuns(
  invoiceId: number,
): Promise<import('../types/api').AgentRunResponse[]> {
  return get<import('../types/api').AgentRunResponse[]>(
    `/invoices/${invoiceId}/agent-runs`,
  )
}

export function getInvoicePayments(
  invoiceId: number,
): Promise<PaymentResponse[]> {
  return get<PaymentResponse[]>(
    `/invoices/${invoiceId}/payments`,
  )
}

export function getInvoiceInterventions(
  invoiceId: number,
): Promise<InterventionOutcomeResponse[]> {
  return get<InterventionOutcomeResponse[]>(
    `/invoices/${invoiceId}/interventions`,
  )
}

export function getInvoicePromises(
  invoiceId: number,
): Promise<PromiseToPayResponse[]> {
  return get<PromiseToPayResponse[]>(
    `/invoices/${invoiceId}/promises`,
  )
}