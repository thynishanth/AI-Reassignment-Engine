const API_BASE_URL = (
  import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
).replace(/\/$/, '')

async function request(path, options = {}) {
  const hasBody = options.body !== undefined
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      ...(hasBody ? { 'Content-Type': 'application/json' } : {}),
      ...(options.headers ?? {}),
    },
    ...options,
  })

  const contentType = response.headers.get('content-type') ?? ''
  const payload = contentType.includes('application/json')
    ? await response.json()
    : await response.text()

  if (!response.ok) {
    throw new Error(extractErrorMessage(payload, response.status))
  }

  return payload
}

function extractErrorMessage(payload, status) {
  if (typeof payload === 'string' && payload.trim()) {
    return payload
  }

  if (payload && typeof payload === 'object') {
    return payload.message || payload.error || `Request failed (${status})`
  }

  return `Request failed (${status})`
}

function queryString(status) {
  if (!status) {
    return ''
  }

  return `?${new URLSearchParams({ status }).toString()}`
}

export function listAgents(status) {
  return request(`/agents${queryString(status)}`)
}

export function listOrders(status) {
  return request(`/orders${queryString(status)}`)
}

export function listSuggestions() {
  return request('/suggestions')
}

export function createOrder(order) {
  return request('/orders', {
    method: 'POST',
    body: JSON.stringify(order),
  })
}

export function suggestOrder(orderId) {
  return request(`/orders/${orderId}/suggest`, {
    method: 'POST',
  })
}

export function updateAgentStatus(agentId, status) {
  return request(`/agents/${agentId}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status }),
  })
}

export function updateSuggestionStatus(suggestionId, status) {
  return request(`/suggestions/${suggestionId}`, {
    method: 'PATCH',
    body: JSON.stringify({ status }),
  })
}
