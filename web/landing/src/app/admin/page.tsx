'use client'

import { useState, useEffect, useCallback } from 'react'

const API_BASE = 'https://pass-alarm-api.nogeass-inc.workers.dev'

type Tab = 'backers' | 'tokens' | 'entitlements' | 'config' | 'audit'

interface Backer {
  id: number; email: string; name: string; tier: string; source: string; created_at: string; notes: string | null
}
interface Token {
  id: number; token: string; backer_id: number; status: string; claimed_by: string | null; claimed_at: string | null; expires_at: string | null; created_at: string; backer_email?: string
}
interface Entitlement {
  id: number; firebase_uid: string; tier: string; source: string; granted_at: string; expires_at: string | null; revoked_at: string | null; token_id: number | null
}
interface AuditLog {
  id: number; action: string; actor: string; target: string | null; detail: string | null; created_at: string
}

export default function AdminPage() {
  const [apiKey, setApiKey] = useState('')
  const [authed, setAuthed] = useState(false)
  const [tab, setTab] = useState<Tab>('backers')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  // Data
  const [backers, setBackers] = useState<Backer[]>([])
  const [tokens, setTokens] = useState<Token[]>([])
  const [entitlements, setEntitlements] = useState<Entitlement[]>([])
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([])
  const [configs, setConfigs] = useState<Record<string, string>>({})

  // CSV import
  const [csvText, setCsvText] = useState('')

  // Token generation
  const [generatedTokens, setGeneratedTokens] = useState<{ backer_id: number; token: string }[]>([])

  // Manual entitlement
  const [grantUid, setGrantUid] = useState('')

  const headers = useCallback(() => ({
    'Content-Type': 'application/json',
    'X-Admin-Key': apiKey,
  }), [apiKey])

  const fetchData = useCallback(async (endpoint: string) => {
    const res = await fetch(`${API_BASE}${endpoint}`, { headers: headers() })
    if (!res.ok) throw new Error(`${res.status}: ${await res.text()}`)
    return res.json()
  }, [headers])

  const postData = useCallback(async (endpoint: string, body: unknown) => {
    const res = await fetch(`${API_BASE}${endpoint}`, {
      method: 'POST',
      headers: headers(),
      body: JSON.stringify(body),
    })
    if (!res.ok) throw new Error(`${res.status}: ${await res.text()}`)
    return res.json()
  }, [headers])

  const putData = useCallback(async (endpoint: string, body: unknown) => {
    const res = await fetch(`${API_BASE}${endpoint}`, {
      method: 'PUT',
      headers: headers(),
      body: JSON.stringify(body),
    })
    if (!res.ok) throw new Error(`${res.status}: ${await res.text()}`)
    return res.json()
  }, [headers])

  const loadTab = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      switch (tab) {
        case 'backers': {
          const data = await fetchData('/api/admin/backers')
          setBackers(data.backers)
          break
        }
        case 'tokens': {
          const data = await fetchData('/api/admin/tokens')
          setTokens(data.tokens)
          break
        }
        case 'entitlements': {
          const data = await fetchData('/api/admin/entitlements')
          setEntitlements(data.entitlements)
          break
        }
        case 'config': {
          const data = await fetchData('/api/admin/audit')
          setAuditLogs([])
          // Also load config
          const cfgRes = await fetch(`${API_BASE}/api/config`)
          const cfgData = await cfgRes.json()
          setConfigs(cfgData)
          break
        }
        case 'audit': {
          const data = await fetchData('/api/admin/audit')
          setAuditLogs(data.logs)
          break
        }
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Unknown error')
    }
    setLoading(false)
  }, [tab, fetchData])

  useEffect(() => {
    if (authed) loadTab()
  }, [authed, tab, loadTab])

  const handleLogin = () => {
    if (apiKey.trim()) setAuthed(true)
  }

  const handleCsvImport = async () => {
    setError(null)
    try {
      const lines = csvText.trim().split('\n').filter(l => l.trim())
      const backersList = lines.map(line => {
        const [email, name, tier, notes] = line.split(',').map(s => s.trim())
        return { email, name: name || '', tier: tier || 'pro', notes: notes || '' }
      })
      await postData('/api/admin/backers/import', { backers: backersList })
      setCsvText('')
      loadTab()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Import failed')
    }
  }

  const handleGenerateTokens = async () => {
    setError(null)
    try {
      const data = await postData('/api/admin/tokens/generate', { all_backers: true })
      setGeneratedTokens(data.tokens)
      loadTab()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Token generation failed')
    }
  }

  const handleRevokeToken = async (id: number) => {
    if (!confirm(`Token #${id} を失効させますか？`)) return
    setError(null)
    try {
      await postData(`/api/admin/tokens/${id}/revoke`, {})
      loadTab()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Revoke failed')
    }
  }

  const handleGrantEntitlement = async () => {
    if (!grantUid.trim()) return
    setError(null)
    try {
      await postData('/api/admin/entitlements/grant', { firebase_uid: grantUid.trim() })
      setGrantUid('')
      loadTab()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Grant failed')
    }
  }

  const handleToggleKillSwitch = async () => {
    const current = configs['REDEEM_DISABLED'] === 'true'
    const newVal = current ? 'false' : 'true'
    setError(null)
    try {
      await putData('/api/admin/config/REDEEM_DISABLED', { value: newVal })
      setConfigs({ ...configs, REDEEM_DISABLED: newVal })
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Config update failed')
    }
  }

  if (!authed) {
    return (
      <div style={styles.center}>
        <div style={styles.card}>
          <h1 style={styles.h1}>PassAlarm Admin</h1>
          <input
            type="password"
            placeholder="Admin API Key"
            value={apiKey}
            onChange={e => setApiKey(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleLogin()}
            style={styles.input}
          />
          <button onClick={handleLogin} style={styles.btn}>ログイン</button>
        </div>
      </div>
    )
  }

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <h1 style={styles.h1}>PassAlarm Admin</h1>
        <div style={styles.tabs}>
          {(['backers', 'tokens', 'entitlements', 'config', 'audit'] as Tab[]).map(t => (
            <button
              key={t}
              onClick={() => setTab(t)}
              style={tab === t ? { ...styles.tab, ...styles.tabActive } : styles.tab}
            >
              {t === 'backers' && '支援者'}
              {t === 'tokens' && 'トークン'}
              {t === 'entitlements' && '権利'}
              {t === 'config' && '設定'}
              {t === 'audit' && '監査ログ'}
            </button>
          ))}
        </div>
      </div>

      {error && <div style={styles.error}>{error}</div>}
      {loading && <div style={styles.loading}>読み込み中...</div>}

      <div style={styles.content}>
        {tab === 'backers' && (
          <>
            <div style={styles.section}>
              <h2 style={styles.h2}>CSV取込</h2>
              <p style={styles.hint}>形式: email, name, tier, notes (1行1件)</p>
              <textarea
                value={csvText}
                onChange={e => setCsvText(e.target.value)}
                rows={5}
                style={styles.textarea}
                placeholder="user@example.com, 山田太郎, pro, Gold tier&#10;user2@example.com, 田中, pro, Silver"
              />
              <button onClick={handleCsvImport} style={styles.btn} disabled={!csvText.trim()}>
                インポート
              </button>
            </div>
            <table style={styles.table}>
              <thead>
                <tr>
                  <th style={styles.th}>ID</th>
                  <th style={styles.th}>Email</th>
                  <th style={styles.th}>Name</th>
                  <th style={styles.th}>Tier</th>
                  <th style={styles.th}>Source</th>
                  <th style={styles.th}>Created</th>
                </tr>
              </thead>
              <tbody>
                {backers.map(b => (
                  <tr key={b.id}>
                    <td style={styles.td}>{b.id}</td>
                    <td style={styles.td}>{b.email}</td>
                    <td style={styles.td}>{b.name}</td>
                    <td style={styles.td}>{b.tier}</td>
                    <td style={styles.td}>{b.source}</td>
                    <td style={styles.td}>{b.created_at}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            <p style={styles.hint}>{backers.length} 件</p>
          </>
        )}

        {tab === 'tokens' && (
          <>
            <div style={styles.section}>
              <h2 style={styles.h2}>トークン一括発行</h2>
              <button onClick={handleGenerateTokens} style={styles.btn}>
                全支援者にトークン発行
              </button>
              {generatedTokens.length > 0 && (
                <div style={styles.generated}>
                  <h3 style={styles.h3}>発行済みトークン ({generatedTokens.length}件)</h3>
                  <textarea
                    readOnly
                    value={generatedTokens.map(t => `${t.backer_id},${t.token}`).join('\n')}
                    rows={Math.min(generatedTokens.length + 1, 10)}
                    style={styles.textarea}
                  />
                </div>
              )}
            </div>
            <table style={styles.table}>
              <thead>
                <tr>
                  <th style={styles.th}>ID</th>
                  <th style={styles.th}>Token</th>
                  <th style={styles.th}>Backer</th>
                  <th style={styles.th}>Status</th>
                  <th style={styles.th}>Claimed By</th>
                  <th style={styles.th}>Created</th>
                  <th style={styles.th}>Action</th>
                </tr>
              </thead>
              <tbody>
                {tokens.map(t => (
                  <tr key={t.id}>
                    <td style={styles.td}>{t.id}</td>
                    <td style={{ ...styles.td, fontFamily: 'monospace' }}>{t.token}</td>
                    <td style={styles.td}>{t.backer_email || t.backer_id}</td>
                    <td style={styles.td}>
                      <span style={{
                        padding: '2px 8px',
                        borderRadius: '4px',
                        fontSize: '12px',
                        background: t.status === 'pending' ? '#1e40af' : t.status === 'claimed' ? '#15803d' : '#b91c1c',
                        color: '#fff',
                      }}>{t.status}</span>
                    </td>
                    <td style={styles.td}>{t.claimed_by || '-'}</td>
                    <td style={styles.td}>{t.created_at}</td>
                    <td style={styles.td}>
                      {t.status === 'pending' && (
                        <button onClick={() => handleRevokeToken(t.id)} style={styles.btnSmall}>
                          失効
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            <p style={styles.hint}>{tokens.length} 件</p>
          </>
        )}

        {tab === 'entitlements' && (
          <>
            <div style={styles.section}>
              <h2 style={styles.h2}>手動付与</h2>
              <div style={{ display: 'flex', gap: '8px' }}>
                <input
                  placeholder="Firebase UID"
                  value={grantUid}
                  onChange={e => setGrantUid(e.target.value)}
                  style={styles.input}
                />
                <button onClick={handleGrantEntitlement} style={styles.btn} disabled={!grantUid.trim()}>
                  付与
                </button>
              </div>
            </div>
            <table style={styles.table}>
              <thead>
                <tr>
                  <th style={styles.th}>ID</th>
                  <th style={styles.th}>Firebase UID</th>
                  <th style={styles.th}>Tier</th>
                  <th style={styles.th}>Source</th>
                  <th style={styles.th}>Granted</th>
                  <th style={styles.th}>Expires</th>
                </tr>
              </thead>
              <tbody>
                {entitlements.map(e => (
                  <tr key={e.id}>
                    <td style={styles.td}>{e.id}</td>
                    <td style={{ ...styles.td, fontFamily: 'monospace', fontSize: '12px' }}>{e.firebase_uid}</td>
                    <td style={styles.td}>{e.tier}</td>
                    <td style={styles.td}>{e.source}</td>
                    <td style={styles.td}>{e.granted_at}</td>
                    <td style={styles.td}>{e.expires_at || 'lifetime'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            <p style={styles.hint}>{entitlements.length} 件</p>
          </>
        )}

        {tab === 'config' && (
          <div style={styles.section}>
            <h2 style={styles.h2}>Kill Switch</h2>
            <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
              <span style={{ color: '#94a3b8' }}>REDEEM_DISABLED:</span>
              <span style={{
                color: configs['REDEEM_DISABLED'] === 'true' ? '#ef4444' : '#22c55e',
                fontWeight: 'bold',
              }}>
                {configs['REDEEM_DISABLED'] === 'true' ? 'ON (停止中)' : 'OFF (有効)'}
              </span>
              <button
                onClick={handleToggleKillSwitch}
                style={{
                  ...styles.btn,
                  background: configs['REDEEM_DISABLED'] === 'true' ? '#22c55e' : '#ef4444',
                }}
              >
                {configs['REDEEM_DISABLED'] === 'true' ? '再開する' : '停止する'}
              </button>
            </div>
          </div>
        )}

        {tab === 'audit' && (
          <table style={styles.table}>
            <thead>
              <tr>
                <th style={styles.th}>ID</th>
                <th style={styles.th}>Action</th>
                <th style={styles.th}>Actor</th>
                <th style={styles.th}>Target</th>
                <th style={styles.th}>Detail</th>
                <th style={styles.th}>Time</th>
              </tr>
            </thead>
            <tbody>
              {auditLogs.map(l => (
                <tr key={l.id}>
                  <td style={styles.td}>{l.id}</td>
                  <td style={styles.td}>{l.action}</td>
                  <td style={styles.td}>{l.actor}</td>
                  <td style={styles.td}>{l.target || '-'}</td>
                  <td style={{ ...styles.td, maxWidth: '300px', overflow: 'hidden', textOverflow: 'ellipsis' }}>{l.detail || '-'}</td>
                  <td style={styles.td}>{l.created_at}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  center: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  container: {
    minHeight: '100vh',
    color: '#f8fafc',
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, monospace',
  },
  header: {
    padding: '16px 24px',
    borderBottom: '1px solid #1e293b',
  },
  h1: {
    fontSize: '20px',
    fontWeight: 700,
    color: '#f8fafc',
    marginBottom: '12px',
  },
  h2: {
    fontSize: '16px',
    fontWeight: 600,
    color: '#e2e8f0',
    marginBottom: '8px',
  },
  h3: {
    fontSize: '14px',
    fontWeight: 600,
    color: '#94a3b8',
    marginBottom: '4px',
  },
  tabs: {
    display: 'flex',
    gap: '4px',
  },
  tab: {
    padding: '8px 16px',
    background: 'transparent',
    border: '1px solid #334155',
    borderRadius: '6px',
    color: '#94a3b8',
    cursor: 'pointer',
    fontSize: '14px',
  },
  tabActive: {
    background: '#1e40af',
    borderColor: '#1e40af',
    color: '#fff',
  },
  content: {
    padding: '24px',
  },
  section: {
    marginBottom: '24px',
    padding: '16px',
    background: '#1e293b',
    borderRadius: '8px',
  },
  card: {
    background: '#1e293b',
    padding: '32px',
    borderRadius: '12px',
    width: '360px',
    display: 'flex',
    flexDirection: 'column' as const,
    gap: '12px',
  },
  input: {
    padding: '10px 14px',
    background: '#0f172a',
    border: '1px solid #334155',
    borderRadius: '6px',
    color: '#f8fafc',
    fontSize: '14px',
    flex: 1,
  },
  textarea: {
    padding: '10px 14px',
    background: '#0f172a',
    border: '1px solid #334155',
    borderRadius: '6px',
    color: '#f8fafc',
    fontSize: '13px',
    fontFamily: 'monospace',
    width: '100%',
    resize: 'vertical' as const,
  },
  btn: {
    padding: '10px 20px',
    background: '#1e40af',
    border: 'none',
    borderRadius: '6px',
    color: '#fff',
    cursor: 'pointer',
    fontSize: '14px',
    fontWeight: 600,
  },
  btnSmall: {
    padding: '4px 10px',
    background: '#b91c1c',
    border: 'none',
    borderRadius: '4px',
    color: '#fff',
    cursor: 'pointer',
    fontSize: '12px',
  },
  error: {
    margin: '16px 24px',
    padding: '12px 16px',
    background: '#450a0a',
    border: '1px solid #b91c1c',
    borderRadius: '6px',
    color: '#fca5a5',
    fontSize: '14px',
  },
  loading: {
    padding: '16px 24px',
    color: '#64748b',
    fontSize: '14px',
  },
  hint: {
    color: '#64748b',
    fontSize: '13px',
    marginTop: '8px',
  },
  table: {
    width: '100%',
    borderCollapse: 'collapse' as const,
    fontSize: '14px',
  },
  th: {
    textAlign: 'left' as const,
    padding: '10px 12px',
    borderBottom: '1px solid #334155',
    color: '#94a3b8',
    fontSize: '12px',
    fontWeight: 600,
    textTransform: 'uppercase' as const,
  },
  td: {
    padding: '10px 12px',
    borderBottom: '1px solid #1e293b',
    color: '#e2e8f0',
    whiteSpace: 'nowrap' as const,
  },
  generated: {
    marginTop: '12px',
  },
}
