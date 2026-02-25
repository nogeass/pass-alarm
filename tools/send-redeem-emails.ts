#!/usr/bin/env npx tsx
/**
 * Send redemption emails to crowdfunding backers.
 *
 * Usage:
 *   RESEND_API_KEY=re_xxx ADMIN_API_KEY=xxx npx tsx tools/send-redeem-emails.ts [--dry-run] [--batch N] [--delay MS]
 *
 * Prerequisites:
 *   - Backers imported & tokens generated via Admin dashboard
 *   - Resend API key with verified sender (info@nogeass.com)
 *
 * Flags:
 *   --dry-run   Print emails without sending
 *   --batch N   Send N emails per batch (default: 10)
 *   --delay MS  Delay between batches in ms (default: 2000)
 */

const API_BASE = "https://pass-alarm-api.nogeass-inc.workers.dev"
const RESEND_API = "https://api.resend.com/emails"
const FROM_EMAIL = "info@nogeass.com"
const FROM_NAME = "パスアラーム"
const REDEEM_BASE = "https://pass-alarm.nogeass.com/r"

interface TokenWithBacker {
  id: number
  token: string
  backer_id: number
  status: string
  backer_email?: string
}

interface Backer {
  id: number
  email: string
  name: string
}

async function main() {
  const ADMIN_API_KEY = process.env.ADMIN_API_KEY
  const RESEND_API_KEY = process.env.RESEND_API_KEY

  if (!ADMIN_API_KEY) {
    console.error("Error: ADMIN_API_KEY environment variable is required")
    process.exit(1)
  }
  if (!RESEND_API_KEY && !args.dryRun) {
    console.error("Error: RESEND_API_KEY environment variable is required (or use --dry-run)")
    process.exit(1)
  }

  const headers = {
    "Content-Type": "application/json",
    "X-Admin-Key": ADMIN_API_KEY,
  }

  // Fetch backers and tokens
  console.log("Fetching backers and tokens...")

  const backersRes = await fetch(`${API_BASE}/api/admin/backers`, { headers })
  const backersData = (await backersRes.json()) as { backers: Backer[] }

  const tokensRes = await fetch(`${API_BASE}/api/admin/tokens`, { headers })
  const tokensData = (await tokensRes.json()) as { tokens: TokenWithBacker[] }

  // Map backer_id -> email
  const backerMap = new Map<number, Backer>()
  for (const b of backersData.backers) {
    backerMap.set(b.id, b)
  }

  // Filter pending tokens only
  const pendingTokens = tokensData.tokens.filter((t) => t.status === "pending")
  console.log(`Found ${pendingTokens.length} pending tokens (${tokensData.tokens.length} total)`)

  if (pendingTokens.length === 0) {
    console.log("No pending tokens to send. Exiting.")
    return
  }

  // Build email list
  const emailList = pendingTokens
    .map((t) => {
      const backer = backerMap.get(t.backer_id)
      if (!backer) {
        console.warn(`  Warning: No backer found for token ${t.id} (backer_id=${t.backer_id})`)
        return null
      }
      return { backer, token: t.token }
    })
    .filter((e): e is { backer: Backer; token: string } => e !== null)

  console.log(`Preparing to send ${emailList.length} emails...`)

  // Process in batches
  let sent = 0
  let failed = 0

  for (let i = 0; i < emailList.length; i += args.batch) {
    const batch = emailList.slice(i, i + args.batch)
    console.log(`\nBatch ${Math.floor(i / args.batch) + 1}: sending ${batch.length} emails...`)

    for (const { backer, token } of batch) {
      const redeemUrl = `${REDEEM_BASE}/${token}`
      const name = backer.name || "支援者"

      if (args.dryRun) {
        console.log(`  [DRY RUN] ${backer.email} (${name}) → ${redeemUrl}`)
        sent++
        continue
      }

      try {
        const res = await fetch(RESEND_API, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${RESEND_API_KEY}`,
          },
          body: JSON.stringify({
            from: `${FROM_NAME} <${FROM_EMAIL}>`,
            to: backer.email,
            subject: "【パスアラーム】クラウドファンディング特典のご案内",
            html: buildEmailHtml(name, token, redeemUrl),
          }),
        })

        if (res.ok) {
          console.log(`  ✓ ${backer.email}`)
          sent++
        } else {
          const err = await res.text()
          console.error(`  ✗ ${backer.email}: ${res.status} ${err}`)
          failed++
        }
      } catch (e) {
        console.error(`  ✗ ${backer.email}: ${e}`)
        failed++
      }
    }

    // Delay between batches
    if (i + args.batch < emailList.length) {
      console.log(`  Waiting ${args.delay}ms...`)
      await new Promise((r) => setTimeout(r, args.delay))
    }
  }

  console.log(`\nDone! Sent: ${sent}, Failed: ${failed}`)
}

function buildEmailHtml(name: string, token: string, redeemUrl: string): string {
  return `
<!DOCTYPE html>
<html lang="ja">
<head><meta charset="UTF-8"></head>
<body style="margin:0;padding:0;background:#f8fafc;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;">
  <div style="max-width:560px;margin:0 auto;padding:40px 24px;">
    <div style="background:#0f172a;border-radius:16px;padding:32px;color:#f8fafc;">
      <h1 style="font-size:22px;margin:0 0 8px;">🎉 クラウドファンディング特典</h1>
      <p style="color:#94a3b8;margin:0 0 24px;">${name} 様、ご支援ありがとうございます！</p>

      <p style="margin:0 0 16px;">
        パスアラーム <strong>Pro（ライフタイム）</strong>をお受け取りいただけます。
      </p>

      <div style="background:rgba(56,189,248,0.1);border-radius:12px;padding:20px;text-align:center;margin:0 0 24px;">
        <p style="color:#94a3b8;font-size:13px;margin:0 0 8px;">あなたの特典コード</p>
        <p style="font-size:24px;font-weight:bold;letter-spacing:0.1em;color:#38bdf8;margin:0;font-family:monospace;">${token}</p>
      </div>

      <a href="${redeemUrl}"
         style="display:block;text-align:center;padding:16px;background:#1d4ed8;color:#fff;border-radius:10px;text-decoration:none;font-weight:600;font-size:16px;margin:0 0 24px;">
        特典を受け取る
      </a>

      <div style="background:rgba(255,255,255,0.05);border-radius:8px;padding:16px;font-size:13px;color:#94a3b8;line-height:1.8;">
        <p style="font-weight:600;color:#e2e8f0;margin:0 0 8px;">受け取り手順</p>
        <ol style="padding-left:20px;margin:0;">
          <li>パスアラームをインストール（iOS / Android）</li>
          <li>上のボタンをタップ</li>
          <li>アプリでサインイン</li>
          <li>「特典を受け取る」をタップ</li>
        </ol>
        <p style="margin:12px 0 0;font-size:12px;">
          ボタンでアプリが開かない場合は、アプリの設定画面から「クラファン特典を受け取る」を選び、上記コードを手動入力してください。
        </p>
      </div>
    </div>

    <p style="text-align:center;color:#94a3b8;font-size:12px;margin:24px 0 0;">
      パスアラーム — OFFにしない目覚まし<br>
      <a href="https://pass-alarm.nogeass.com" style="color:#64748b;">pass-alarm.nogeass.com</a>
    </p>
  </div>
</body>
</html>`
}

// Parse CLI args
const args = (() => {
  const argv = process.argv.slice(2)
  return {
    dryRun: argv.includes("--dry-run"),
    batch: parseInt(argv[argv.indexOf("--batch") + 1]) || 10,
    delay: parseInt(argv[argv.indexOf("--delay") + 1]) || 2000,
  }
})()

main().catch((e) => {
  console.error("Fatal:", e)
  process.exit(1)
})
