import { useEffect, useMemo, useState } from 'react'
import {
  createOrder,
  listAgents,
  listOrders,
  listSuggestions,
  suggestOrder,
  updateAgentStatus,
  updateSuggestionStatus,
} from './api'
import './App.css'

const AGENT_STATUSES = ['AVAILABLE', 'BUSY', 'OFFLINE']
const ORDER_STATUSES = [
  'ASSIGNED',
  'REASSIGNMENT_PENDING',
  'REASSIGNED',
  'DELIVERED',
]

const INITIAL_ORDER_FORM = {
  id: '',
  description: '',
  assignedAgentId: '',
}

function App() {
  const [agents, setAgents] = useState([])
  const [orders, setOrders] = useState([])
  const [suggestions, setSuggestions] = useState([])
  const [agentFilter, setAgentFilter] = useState('')
  const [orderFilter, setOrderFilter] = useState('')
  const [orderForm, setOrderForm] = useState(INITIAL_ORDER_FORM)
  const [draftStatuses, setDraftStatuses] = useState({})
  const [loading, setLoading] = useState(true)
  const [action, setAction] = useState('')
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  useEffect(() => {
    let active = true

    async function loadData() {
      setLoading(true)
      setError('')

      try {
        const [agentsData, ordersData, suggestionsData] = await Promise.all([
          listAgents(agentFilter || undefined),
          listOrders(orderFilter || undefined),
          listSuggestions(),
        ])

        if (!active) {
          return
        }

        setAgents(agentsData)
        setOrders(ordersData)
        setSuggestions(suggestionsData)
        setDraftStatuses((current) => {
          const next = { ...current }

          for (const agent of agentsData) {
            if (!(agent.id in next)) {
              next[agent.id] = agent.status
            }
          }

          return next
        })
      } catch (fetchError) {
        if (active) {
          setError(getErrorMessage(fetchError))
        }
      } finally {
        if (active) {
          setLoading(false)
        }
      }
    }

    void loadData()

    return () => {
      active = false
    }
  }, [agentFilter, orderFilter])

  const stats = useMemo(() => {
    const assignedOrders = orders.filter(
      (order) => order.status === 'ASSIGNED' || order.status === 'REASSIGNMENT_PENDING',
    ).length

    return {
      agents: agents.length,
      availableAgents: agents.filter((agent) => agent.status === 'AVAILABLE').length,
      activeOrders: assignedOrders,
      pendingSuggestions: suggestions.length,
    }
  }, [agents, orders, suggestions])

  async function refreshData(nextNotice) {
    const [agentsData, ordersData, suggestionsData] = await Promise.all([
      listAgents(agentFilter || undefined),
      listOrders(orderFilter || undefined),
      listSuggestions(),
    ])

    setAgents(agentsData)
    setOrders(ordersData)
    setSuggestions(suggestionsData)
    setDraftStatuses((current) => {
      const next = { ...current }

      for (const agent of agentsData) {
        next[agent.id] = next[agent.id] ?? agent.status
      }

      return next
    })
    setNotice(nextNotice ?? '')
  }

  async function handleCreateOrder(event) {
    event.preventDefault()
    setAction('create-order')
    setError('')
    setNotice('')

    try {
      await createOrder({
        id: orderForm.id.trim(),
        description: orderForm.description.trim(),
        assignedAgentId: orderForm.assignedAgentId,
      })
      setOrderForm(INITIAL_ORDER_FORM)
      await refreshData(`Order ${orderForm.id.trim()} created.`)
    } catch (createError) {
      setError(getErrorMessage(createError))
    } finally {
      setAction('')
    }
  }

  async function handleSuggest(orderId) {
    setAction(`suggest-${orderId}`)
    setError('')
    setNotice('')

    try {
      await suggestOrder(orderId)
      await refreshData(`Suggestion created for ${orderId}.`)
    } catch (suggestError) {
      setError(getErrorMessage(suggestError))
    } finally {
      setAction('')
    }
  }

  async function handleAgentStatusSave(agentId) {
    const nextStatus = draftStatuses[agentId]

    setAction(`agent-${agentId}`)
    setError('')
    setNotice('')

    try {
      await updateAgentStatus(agentId, nextStatus)
      await refreshData(`Agent ${agentId} updated to ${nextStatus}.`)
    } catch (updateError) {
      setError(getErrorMessage(updateError))
    } finally {
      setAction('')
    }
  }

  async function handleSuggestionStatusSave(suggestionId, status) {
    setAction(`suggestion-${suggestionId}`)
    setError('')
    setNotice('')

    try {
      await updateSuggestionStatus(suggestionId, status)
      await refreshData(`Suggestion ${suggestionId} marked ${status.toLowerCase()}.`)
    } catch (updateError) {
      setError(getErrorMessage(updateError))
    } finally {
      setAction('')
    }
  }

  const agentOptions = agents.length > 0 ? agents : []

  return (
    <main className="app-shell">
      <section className="hero">
        <div>
          <p className="eyebrow">Dispatch dashboard</p>
          <h1>Simple order routing control panel</h1>
          <p className="lede">
            Lightweight UI for creating orders, managing agents, and handling reassignment
            suggestions.
          </p>
        </div>

        <div className="stats-grid">
          <StatCard label="Agents" value={stats.agents} />
          <StatCard label="Available" value={stats.availableAgents} />
          <StatCard label="Active orders" value={stats.activeOrders} />
          <StatCard label="Pending suggestions" value={stats.pendingSuggestions} />
        </div>
      </section>

      {error ? <Banner tone="error" text={error} /> : null}
      {notice ? <Banner tone="success" text={notice} /> : null}

      <section className="panel-grid">
        <Panel title="Create order" subtitle="Add an order and assign it to an agent">
          <form className="stack" onSubmit={handleCreateOrder}>
            <Field label="Order ID">
              <input
                value={orderForm.id}
                onChange={(event) =>
                  setOrderForm((current) => ({ ...current, id: event.target.value }))
                }
                placeholder="ORD-1001"
                required
              />
            </Field>
            <Field label="Description">
              <textarea
                value={orderForm.description}
                onChange={(event) =>
                  setOrderForm((current) => ({ ...current, description: event.target.value }))
                }
                placeholder="Customer order details"
                rows={4}
                required
              />
            </Field>
            <Field label="Assigned agent">
              <select
                value={orderForm.assignedAgentId}
                onChange={(event) =>
                  setOrderForm((current) => ({
                    ...current,
                    assignedAgentId: event.target.value,
                  }))
                }
                required
              >
                <option value="" disabled>
                  Select agent
                </option>
                {agentOptions.map((agent) => (
                  <option key={agent.id} value={agent.id}>
                    {agent.name} ({agent.id})
                  </option>
                ))}
              </select>
            </Field>
            <button type="submit" className="primary" disabled={action === 'create-order'}>
              {action === 'create-order' ? 'Creating...' : 'Create order'}
            </button>
          </form>
        </Panel>

        <Panel
          title="Agent status"
          subtitle="Change availability to match live capacity"
          controls={
            <>
              <select value={agentFilter} onChange={(event) => setAgentFilter(event.target.value)}>
                <option value="">All agents</option>
                {AGENT_STATUSES.map((status) => (
                  <option key={status} value={status}>
                    {status}
                  </option>
                ))}
              </select>
              <button type="button" className="secondary" onClick={() => void refreshData()}>
                Refresh
              </button>
            </>
          }
        >
          <div className="table-card">
            <table>
              <thead>
                <tr>
                  <th>Agent</th>
                  <th>Orders</th>
                  <th>Status</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {agents.map((agent) => (
                  <tr key={agent.id}>
                    <td>
                      <strong>{agent.name}</strong>
                      <div className="muted">{agent.id}</div>
                    </td>
                    <td>{agent.activeOrderCount ?? 0}</td>
                    <td>
                      <select
                        value={draftStatuses[agent.id] ?? agent.status}
                        onChange={(event) =>
                          setDraftStatuses((current) => ({
                            ...current,
                            [agent.id]: event.target.value,
                          }))
                        }
                      >
                        {AGENT_STATUSES.map((status) => (
                          <option key={status} value={status}>
                            {status}
                          </option>
                        ))}
                      </select>
                    </td>
                    <td>
                      <button
                        type="button"
                        className="secondary"
                        disabled={
                          action === `agent-${agent.id}` ||
                          (draftStatuses[agent.id] ?? agent.status) === agent.status
                        }
                        onClick={() => void handleAgentStatusSave(agent.id)}
                      >
                        {action === `agent-${agent.id}` ? 'Saving...' : 'Save'}
                      </button>
                    </td>
                  </tr>
                ))}
                {agents.length === 0 ? (
                  <tr>
                    <td colSpan={4} className="empty">
                      No agents found.
                    </td>
                  </tr>
                ) : null}
              </tbody>
            </table>
          </div>
        </Panel>
      </section>

      <section className="panel-grid">
        <Panel
          title="Orders"
          subtitle="Track order lifecycle and create reassignment suggestions"
          controls={
            <>
              <select value={orderFilter} onChange={(event) => setOrderFilter(event.target.value)}>
                <option value="">All orders</option>
                {ORDER_STATUSES.map((status) => (
                  <option key={status} value={status}>
                    {status}
                  </option>
                ))}
              </select>
            </>
          }
        >
          <div className="list-stack">
            {orders.map((order) => (
              <article key={order.id} className="record-card">
                <div className="record-header">
                  <div>
                    <strong>{order.id}</strong>
                    <div className="muted">{formatDateTime(order.createdAt)}</div>
                  </div>
                  <StatusPill value={order.status} />
                </div>
                <p>{order.description}</p>
                <div className="record-footer">
                  <span className="muted">Assigned agent: {order.assignedAgentId ?? '—'}</span>
                  <button
                    type="button"
                    className="secondary"
                    disabled={action === `suggest-${order.id}` || order.status === 'REASSIGNMENT_PENDING'}
                    onClick={() => void handleSuggest(order.id)}
                  >
                    {action === `suggest-${order.id}` ? 'Suggesting...' : 'Suggest reassignment'}
                  </button>
                </div>
              </article>
            ))}
            {orders.length === 0 ? <EmptyState text="No orders found." /> : null}
          </div>
        </Panel>

        <Panel title="Pending suggestions" subtitle="Accept or reject routing recommendations">
          <div className="list-stack">
            {suggestions.map((suggestion) => (
              <article key={suggestion.id} className="record-card">
                <div className="record-header">
                  <div>
                    <strong>{suggestion.id}</strong>
                    <div className="muted">
                      Order {suggestion.orderId} → Agent {suggestion.recommendedAgentId}
                    </div>
                  </div>
                  <StatusPill value={suggestion.status} />
                </div>
                <p>{suggestion.reasoning}</p>
                <div className="record-footer">
                  <span className="muted">
                    Confidence {formatConfidence(suggestion.confidenceScore)}
                  </span>
                  <div className="button-row">
                    <button
                      type="button"
                      className="secondary"
                      disabled={action === `suggestion-${suggestion.id}`}
                      onClick={() => void handleSuggestionStatusSave(suggestion.id, 'REJECTED')}
                    >
                      Reject
                    </button>
                    <button
                      type="button"
                      className="primary"
                      disabled={action === `suggestion-${suggestion.id}`}
                      onClick={() => void handleSuggestionStatusSave(suggestion.id, 'ACCEPTED')}
                    >
                      Accept
                    </button>
                  </div>
                </div>
              </article>
            ))}
            {suggestions.length === 0 ? (
              <EmptyState text="No pending suggestions." />
            ) : null}
          </div>
        </Panel>
      </section>

      {loading ? <div className="loading-bar">Loading data…</div> : null}
    </main>
  )
}

function Panel({ title, subtitle, controls, children }) {
  return (
    <section className="panel">
      <div className="panel-header">
        <div>
          <h2>{title}</h2>
          <p className="muted">{subtitle}</p>
        </div>
        {controls ? <div className="panel-controls">{controls}</div> : null}
      </div>
      {children}
    </section>
  )
}

function Field({ label, children }) {
  return (
    <label className="field">
      <span>{label}</span>
      {children}
    </label>
  )
}

function StatCard({ label, value }) {
  return (
    <article className="stat-card">
      <span>{label}</span>
      <strong>{value}</strong>
    </article>
  )
}

function StatusPill({ value }) {
  return <span className={`pill pill-${value.toLowerCase()}`}>{value}</span>
}

function Banner({ tone, text }) {
  return <div className={`banner banner-${tone}`}>{text}</div>
}

function EmptyState({ text }) {
  return <div className="empty">{text}</div>
}

function getErrorMessage(error) {
  return error instanceof Error ? error.message : 'Something went wrong.'
}

function formatDateTime(value) {
  if (!value) {
    return '—'
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date)
}

function formatConfidence(value) {
  const numericValue = Number(value)

  if (Number.isNaN(numericValue)) {
    return '—'
  }

  return `${(numericValue * 100).toFixed(1)}%`
}

export default App
