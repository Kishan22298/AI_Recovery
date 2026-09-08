import { get } from './client'
import type { InvoiceResponse } from '../types/api'

export function getInvoices(): Promise<InvoiceResponse[]> {
  return get<InvoiceResponse[]>('/invoices')
}

export function getInvoice(
  invoiceId: number,
): Promise<InvoiceResponse> {
  return get<InvoiceResponse>(`/invoices/${invoiceId}`)
}