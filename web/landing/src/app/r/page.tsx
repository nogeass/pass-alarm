'use client'

import { useEffect, useState } from 'react'

const APPSTORE_URL = 'https://apps.apple.com/us/app/off%E3%81%AB%E3%81%97%E3%81%AA%E3%81%84%E7%9B%AE%E8%A6%9A%E3%81%BE%E3%81%97-%E3%83%91%E3%82%B9%E3%82%A2%E3%83%A9%E3%83%BC%E3%83%A0/id6759545599'
const PLAYSTORE_URL = 'https://play.google.com/store/apps/details?id=com.nogeass.passalarm'

export default function RedeemPage() {
  const [token, setToken] = useState<string | null>(null)

  useEffect(() => {
    // Extract token from URL path: /r/XXXXXXXXXXXX or /r/XXXXXXXXXXXX/
    const match = window.location.pathname.match(/\/r\/([A-Za-z0-9]+)/)
    if (match) setToken(match[1])
  }, [])

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '2rem',
      fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
      background: 'linear-gradient(135deg, #0f172a 0%, #1e293b 100%)',
      color: '#f8fafc',
    }}>
      <div style={{
        maxWidth: '480px',
        width: '100%',
        textAlign: 'center',
      }}>
        <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>🎉</div>
        <h1 style={{
          fontSize: '1.5rem',
          fontWeight: 700,
          marginBottom: '0.5rem',
        }}>
          クラファン特典を受け取る
        </h1>
        <p style={{
          color: '#94a3b8',
          marginBottom: '2rem',
          lineHeight: 1.6,
        }}>
          パスアラーム Pro（ライフタイム）をアクティベートします。
          <br />
          アプリをインストールしてリンクを開いてください。
        </p>

        {token && (
          <div style={{
            background: 'rgba(255,255,255,0.05)',
            borderRadius: '12px',
            padding: '1.5rem',
            marginBottom: '2rem',
          }}>
            <p style={{
              color: '#94a3b8',
              fontSize: '0.875rem',
              marginBottom: '0.5rem',
            }}>
              あなたのトークン
            </p>
            <code style={{
              display: 'block',
              fontSize: '1.5rem',
              fontWeight: 700,
              letterSpacing: '0.1em',
              color: '#38bdf8',
              padding: '0.5rem',
              background: 'rgba(56,189,248,0.1)',
              borderRadius: '8px',
              userSelect: 'all' as const,
              cursor: 'text',
            }}>
              {token}
            </code>
            <p style={{
              color: '#64748b',
              fontSize: '0.75rem',
              marginTop: '0.75rem',
            }}>
              アプリが開かない場合は、このコードをアプリ内で手動入力してください
            </p>
          </div>
        )}

        <div style={{
          display: 'flex',
          flexDirection: 'column',
          gap: '0.75rem',
          marginBottom: '2rem',
        }}>
          <a
            href={APPSTORE_URL}
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              padding: '0.875rem 1.5rem',
              background: '#1d4ed8',
              color: '#fff',
              borderRadius: '10px',
              textDecoration: 'none',
              fontWeight: 600,
              fontSize: '1rem',
            }}
          >
            App Store からダウンロード
          </a>
          <a
            href={PLAYSTORE_URL}
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              padding: '0.875rem 1.5rem',
              background: '#15803d',
              color: '#fff',
              borderRadius: '10px',
              textDecoration: 'none',
              fontWeight: 600,
              fontSize: '1rem',
            }}
          >
            Google Play からダウンロード
          </a>
        </div>

        <div style={{
          background: 'rgba(255,255,255,0.03)',
          borderRadius: '12px',
          padding: '1.25rem',
          textAlign: 'left',
        }}>
          <p style={{
            fontWeight: 600,
            marginBottom: '0.75rem',
            fontSize: '0.875rem',
          }}>
            受け取り手順
          </p>
          <ol style={{
            color: '#94a3b8',
            fontSize: '0.8125rem',
            lineHeight: 1.8,
            paddingLeft: '1.25rem',
            margin: 0,
          }}>
            <li>パスアラームをインストール</li>
            <li>このページのリンクをもう一度タップ</li>
            <li>アプリが開いたらサインイン</li>
            <li>「特典を受け取る」をタップ</li>
          </ol>
        </div>
      </div>
    </div>
  )
}
