const API_BASE_URL = '/api'

export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

async function request<T>(
  path: string,
  options?: RequestInit,
): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      Accept: 'application/json',
      ...(options?.headers ?? {}),
    },
    ...options,
  })

  if (!response.ok) {
    const message = await response.text()

    throw new ApiError(
      response.status,
      message || `Request failed with status ${response.status}`,
    )
  }

  return response.json() as Promise<T>
}

export async function get<T>(path: string): Promise<T> {
  return request<T>(path)
}

export async function post<T>(
  path: string,
): Promise<T> {
  return request<T>(path, {
    method: 'POST',
  })
}