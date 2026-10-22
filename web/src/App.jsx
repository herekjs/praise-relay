import { useEffect, useState } from 'react'

const NAME_MAX = 40
const MESSAGE_MAX = 500

async function readJson(res) {
  try {
    return await res.json()
  } catch {
    return null
  }
}

function errorText(body, fallback) {
  return body && typeof body.error === 'string' ? body.error : fallback
}

function formatTime(iso) {
  const d = new Date(iso)
  return Number.isNaN(d.getTime()) ? iso : d.toLocaleString('ko-KR')
}

export default function App() {
  const [entries, setEntries] = useState([])
  const [loading, setLoading] = useState(true)
  const [name, setName] = useState('')
  const [message, setMessage] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  async function load() {
    try {
      const res = await fetch('/api/entries')
      const body = await readJson(res)
      if (!res.ok) throw new Error(errorText(body, '목록을 불러오지 못했어요.'))
      setEntries(body.entries)
      setError('')
    } catch (e) {
      setError(e instanceof TypeError ? '서버에 연결할 수 없어요. 잠시 뒤 다시 해 주세요.' : e.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  async function submit(e) {
    e.preventDefault()
    if (saving) return
    setSaving(true)
    setError('')
    try {
      const res = await fetch('/api/entries', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, message }),
      })
      const body = await readJson(res)
      if (!res.ok) throw new Error(errorText(body, '저장하지 못했어요.'))
      setEntries((prev) => [body.entry, ...prev].slice(0, 200))
      setMessage('')
    } catch (e) {
      setError(e instanceof TypeError ? '서버에 연결할 수 없어요. 잠시 뒤 다시 해 주세요.' : e.message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <main className="page">
      <h1>🎈 칭찬 릴레이</h1>
      <p className="lead">동료에게 칭찬 한마디를 남겨 보세요.</p>

      <form className="card" onSubmit={submit}>
        <label>
          보내는 사람
          <input
            value={name}
            maxLength={NAME_MAX}
            onChange={(e) => setName(e.target.value)}
            placeholder="이름을 적어 주세요"
            disabled={saving}
          />
        </label>
        <label>
          칭찬 한마디
          <textarea
            value={message}
            maxLength={MESSAGE_MAX}
            rows={3}
            onChange={(e) => setMessage(e.target.value)}
            placeholder="고마웠던 일을 적어 주세요"
            disabled={saving}
          />
        </label>
        <div className="row">
          <span className="count">{message.length}/{MESSAGE_MAX}</span>
          <button type="submit" disabled={saving}>
            {saving ? '저장하는 중…' : '칭찬 남기기'}
          </button>
        </div>
        {error && <p className="error" role="alert">{error}</p>}
      </form>

      <section className="list" aria-label="칭찬 목록">
        {loading && <p className="empty">불러오는 중…</p>}
        {!loading && entries.length === 0 && <p className="empty">아직 칭찬이 없어요. 첫 칭찬을 남겨 보세요!</p>}
        {entries.map((entry) => (
          <article className="bubble" key={entry.id}>
            <p className="msg">{entry.message}</p>
            <p className="meta">
              <strong>{entry.name}</strong> · {formatTime(entry.created_at)}
            </p>
          </article>
        ))}
      </section>
    </main>
  )
}
