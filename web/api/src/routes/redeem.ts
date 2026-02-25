import type { Env, AuthUser } from "../types"
import { findTokenByCode, isRedeemDisabled, insertAuditLog } from "../lib/db"

export async function handleClaim(request: Request, env: Env, user: AuthUser): Promise<Response> {
  const db = env.DB

  // Kill switch check
  if (await isRedeemDisabled(db)) {
    return json({ error: "Redemption is temporarily disabled" }, 503)
  }

  let body: { token: string }
  try {
    body = await request.json()
  } catch {
    return json({ error: "Invalid JSON" }, 400)
  }

  const tokenCode = body.token?.trim()
  if (!tokenCode) {
    return json({ error: "token is required" }, 400)
  }

  // Find token
  const token = await findTokenByCode(db, tokenCode)
  if (!token) {
    return json({ error: "Invalid token" }, 404)
  }

  if (token.status === "claimed") {
    return json({ error: "Token already used" }, 409)
  }
  if (token.status === "revoked") {
    return json({ error: "Token has been revoked" }, 400)
  }
  if (token.expires_at && new Date(token.expires_at) < new Date()) {
    return json({ error: "Token has expired" }, 400)
  }

  // Atomic batch: claim token + create entitlement + audit log
  const now = new Date().toISOString()
  const batch = [
    db
      .prepare("UPDATE tokens SET status = 'claimed', claimed_by = ?, claimed_at = ? WHERE id = ? AND status = 'pending'")
      .bind(user.uid, now, token.id),
    db
      .prepare("INSERT INTO entitlements (firebase_uid, tier, source, granted_at, token_id) VALUES (?, 'pro', 'crowdfund', ?, ?)")
      .bind(user.uid, now, token.id),
    db
      .prepare("INSERT INTO audit_logs (action, actor, target, detail) VALUES ('token_claimed', ?, ?, ?)")
      .bind(user.uid, `token:${token.id}`, JSON.stringify({ token_code: tokenCode, backer_id: token.backer_id })),
  ]

  const results = await db.batch(batch)

  // Check if token was actually updated (race condition guard)
  const updateResult = results[0]
  if (!updateResult.meta.changed_db || updateResult.meta.changes === 0) {
    return json({ error: "Token already used" }, 409)
  }

  return json({
    ok: true,
    entitlement: {
      tier: "pro",
      source: "crowdfund",
      granted_at: now,
    },
  })
}

function json(data: unknown, status = 200): Response {
  return new Response(JSON.stringify(data), {
    status,
    headers: { "Content-Type": "application/json" },
  })
}
