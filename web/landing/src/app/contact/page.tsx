'use client'

import { useState, useRef, useEffect } from 'react'
import { ja } from '@/i18n/ja'

declare global {
  interface Window {
    turnstile?: {
      render: (
        container: string | HTMLElement,
        options: { sitekey: string; callback: (token: string) => void; 'expired-callback': () => void },
      ) => string
      reset: (widgetId: string) => void
    }
  }
}

const TURNSTILE_SITE_KEY = process.env.NEXT_PUBLIC_TURNSTILE_SITE_KEY || ''

export default function ContactPage() {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [message, setMessage] = useState('')
  const [turnstileToken, setTurnstileToken] = useState('')
  const [status, setStatus] = useState<'idle' | 'sending' | 'sent' | 'error'>('idle')
  const [errorMsg, setErrorMsg] = useState('')
  const turnstileRef = useRef<HTMLDivElement>(null)
  const widgetIdRef = useRef<string>('')

  useEffect(() => {
    if (!TURNSTILE_SITE_KEY || !turnstileRef.current) return
    const interval = setInterval(() => {
      if (window.turnstile && turnstileRef.current) {
        widgetIdRef.current = window.turnstile.render(turnstileRef.current, {
          sitekey: TURNSTILE_SITE_KEY,
          callback: (token: string) => setTurnstileToken(token),
          'expired-callback': () => setTurnstileToken(''),
        })
        clearInterval(interval)
      }
    }, 100)
    return () => clearInterval(interval)
  }, [])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (TURNSTILE_SITE_KEY && !turnstileToken) {
      setErrorMsg('認証チェックを完了してください。')
      return
    }

    setStatus('sending')
    setErrorMsg('')

    try {
      const res = await fetch('/api/contact', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, email, message, turnstileToken }),
      })
      const data = await res.json()
      if (res.ok) {
        setStatus('sent')
        setName('')
        setEmail('')
        setMessage('')
      } else {
        setStatus('error')
        setErrorMsg(data.error || '送信に失敗しました。')
      }
    } catch {
      setStatus('error')
      setErrorMsg('ネットワークエラーが発生しました。')
    }
  }

  return (
    <div className="max-w-xl mx-auto px-4 py-32">
      <h1 className="heading-lg mb-4">{ja.footer.contact}</h1>
      <p className="text-text-muted text-sm mb-8">
        ご質問・ご要望・不具合報告などお気軽にお問い合わせください。
      </p>

      {status === 'sent' ? (
        <div className="bg-pastel-mint/30 border border-pastel-mint rounded-xl p-6 text-center">
          <p className="text-lg font-bold mb-2">送信しました</p>
          <p className="text-text-muted text-sm">
            お問い合わせありがとうございます。内容を確認のうえ対応いたします。
          </p>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label htmlFor="name" className="block text-sm font-bold mb-1">
              お名前 <span className="text-red-500">*</span>
            </label>
            <input
              id="name"
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full px-4 py-3 rounded-xl border border-black/10 bg-white focus:outline-none focus:ring-2 focus:ring-brand-500/30 text-sm"
              placeholder="山田太郎"
            />
          </div>

          <div>
            <label htmlFor="email" className="block text-sm font-bold mb-1">
              メールアドレス <span className="text-red-500">*</span>
            </label>
            <input
              id="email"
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-4 py-3 rounded-xl border border-black/10 bg-white focus:outline-none focus:ring-2 focus:ring-brand-500/30 text-sm"
              placeholder="taro@example.com"
            />
          </div>

          <div>
            <label htmlFor="message" className="block text-sm font-bold mb-1">
              お問い合わせ内容 <span className="text-red-500">*</span>
            </label>
            <textarea
              id="message"
              required
              rows={6}
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              className="w-full px-4 py-3 rounded-xl border border-black/10 bg-white focus:outline-none focus:ring-2 focus:ring-brand-500/30 text-sm resize-none"
              placeholder="お問い合わせ内容をご記入ください"
            />
          </div>

          {TURNSTILE_SITE_KEY && <div ref={turnstileRef} className="mb-2" />}

          {errorMsg && (
            <p className="text-red-500 text-sm">{errorMsg}</p>
          )}

          <button
            type="submit"
            disabled={status === 'sending'}
            className="w-full py-3 px-6 bg-brand-500 text-white font-bold rounded-xl hover:bg-brand-600 transition disabled:opacity-50 disabled:cursor-not-allowed text-sm"
          >
            {status === 'sending' ? '送信中...' : '送信する'}
          </button>
        </form>
      )}
    </div>
  )
}
