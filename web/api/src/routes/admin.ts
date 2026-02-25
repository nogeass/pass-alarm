import type { Env } from "../types"
import { generateTokens } from "../lib/tokens"
import {
  getAllBackers,
  getAllTokens,
  getAllEntitlements,
  getAuditLogs,
  getAllConfigs,
  setConfig,
  insertAuditLog,
} from "../lib/db"

export function isAdminAuthorized(request: Request, env: Env): boolean {
  const key = request.headers.get("X-Admin-Key")
  return !!key && key === env.ADMIN_API_KEY
}

export async function handleAdmin(
  request: Request,
  env: Env,
  path: string,
): Promise<Response> {
  if (!isAdminAuthorized(request, env)) {
    return json({ error: "Unauthorized" }, 401)
  }

  const db = env.DB
  const method = request.method

  // POST /api/admin/backers/import
  if (path === "/api/admin/backers/import" && method === "POST") {
    return handleBackersImport(request, env)
  }

  // GET /api/admin/backers
  if (path === "/api/admin/backers" && method === "GET") {
    const backers = await getAllBackers(db)
    return json({ backers })
  }

  // POST /api/admin/tokens/generate
  if (path === "/api/admin/tokens/generate" && method === "POST") {
    return handleTokensGenerate(request, env)
  }

  // GET /api/admin/tokens
  if (path === "/api/admin/tokens" && method === "GET") {
    const tokens = await getAllTokens(db)
    return json({ tokens })
  }

  // POST /api/admin/tokens/:id/revoke
  const revokeMatch = path.match(/^\/api\/admin\/tokens\/(\d+)\/revoke$/)
  if (revokeMatch && method === "POST") {
    return handleTokenRevoke(env, parseInt(revokeMatch[1]))
  }

  // GET /api/admin/entitlements
  if (path === "/api/admin/entitlements" && method === "GET") {
    const entitlements = await getAllEntitlements(db)
    return json({ entitlements })
  }

  // POST /api/admin/entitlements/grant
  if (path === "/api/admin/entitlements/grant" && method === "POST") {
    return handleEntitlementGrant(request, env)
  }

  // GET /api/admin/audit
  if (path === "/api/admin/audit" && method === "GET") {
    const logs = await getAuditLogs(db)
    return json({ logs })
  }

  // PUT /api/admin/config/:key
  const configMatch = path.match(/^\/api\/admin\/config\/(.+)$/)
  if (configMatch && method === "PUT") {
    return handleConfigUpdate(request, env, configMatch[1])
  }

  return json({ error: "Not Found" }, 404)
}

async function handleBackersImport(request: Request, env: Env): Promise<Response> {
  const db = env.DB

  let body: { backers: { email: string; name?: string; tier?: string; source?: string; notes?: string }[] }
  try {
    body = await request.json()
  } catch {
    return json({ error: "Invalid JSON" }, 400)
  }

  if (!Array.isArray(body.backers) || body.backers.length === 0) {
    return json({ error: "backers array is required" }, 400)
  }

  const stmts = body.backers.map((b) =>
    db
      .prepare(
        "INSERT INTO backers (email, name, tier, source, notes) VALUES (?, ?, ?, ?, ?) ON CONFLICT(email) DO UPDATE SET name = ?, tier = ?, source = ?, notes = ?",
      )
      .bind(
        b.email.toLowerCase().trim(),
        b.name ?? "",
        b.tier ?? "pro",
        b.source ?? "crowdfund",
        b.notes ?? null,
        b.name ?? "",
        b.tier ?? "pro",
        b.source ?? "crowdfund",
        b.notes ?? null,
      ),
  )

  stmts.push(
    db
      .prepare("INSERT INTO audit_logs (action, actor, target, detail) VALUES (?, ?, ?, ?)")
      .bind("backers_imported", "admin", null, `${body.backers.length} backers`),
  )

  await db.batch(stmts)

  return json({ ok: true, imported: body.backers.length })
}

async function handleTokensGenerate(request: Request, env: Env): Promise<Response> {
  const db = env.DB

  let body: { backer_ids?: number[]; all_backers?: boolean; expires_at?: string }
  try {
    body = await request.json()
  } catch {
    return json({ error: "Invalid JSON" }, 400)
  }

  let backerIds: number[]

  if (body.all_backers) {
    const backers = await getAllBackers(db)
    backerIds = backers.map((b) => b.id)
  } else if (Array.isArray(body.backer_ids) && body.backer_ids.length > 0) {
    backerIds = body.backer_ids
  } else {
    return json({ error: "backer_ids or all_backers required" }, 400)
  }

  if (backerIds.length === 0) {
    return json({ error: "No backers found" }, 400)
  }

  const tokens = generateTokens(backerIds.length)
  const expiresAt = body.expires_at ?? null

  const stmts = backerIds.map((backerId, i) =>
    db
      .prepare("INSERT INTO tokens (token, backer_id, expires_at) VALUES (?, ?, ?)")
      .bind(tokens[i], backerId, expiresAt),
  )

  stmts.push(
    db
      .prepare("INSERT INTO audit_logs (action, actor, target, detail) VALUES (?, ?, ?, ?)")
      .bind("tokens_generated", "admin", null, `${backerIds.length} tokens`),
  )

  await db.batch(stmts)

  // Return tokens with backer IDs for email sending
  const result = backerIds.map((id, i) => ({ backer_id: id, token: tokens[i] }))
  return json({ ok: true, tokens: result })
}

async function handleTokenRevoke(env: Env, tokenId: number): Promise<Response> {
  const db = env.DB

  const result = await db
    .prepare("UPDATE tokens SET status = 'revoked' WHERE id = ? AND status = 'pending'")
    .bind(tokenId)
    .run()

  if (result.meta.changes === 0) {
    return json({ error: "Token not found or already claimed/revoked" }, 404)
  }

  await insertAuditLog(db, "token_revoked", "admin", `token:${tokenId}`)
  return json({ ok: true })
}

async function handleEntitlementGrant(request: Request, env: Env): Promise<Response> {
  const db = env.DB

  let body: { firebase_uid: string; tier?: string; source?: string; expires_at?: string }
  try {
    body = await request.json()
  } catch {
    return json({ error: "Invalid JSON" }, 400)
  }

  if (!body.firebase_uid) {
    return json({ error: "firebase_uid is required" }, 400)
  }

  const now = new Date().toISOString()
  await db
    .prepare("INSERT INTO entitlements (firebase_uid, tier, source, granted_at, expires_at) VALUES (?, ?, ?, ?, ?)")
    .bind(body.firebase_uid, body.tier ?? "pro", body.source ?? "manual", now, body.expires_at ?? null)
    .run()

  await insertAuditLog(db, "entitlement_granted", "admin", `uid:${body.firebase_uid}`, JSON.stringify(body))

  return json({ ok: true })
}

async function handleConfigUpdate(request: Request, env: Env, key: string): Promise<Response> {
  const db = env.DB

  let body: { value: string }
  try {
    body = await request.json()
  } catch {
    return json({ error: "Invalid JSON" }, 400)
  }

  if (body.value === undefined) {
    return json({ error: "value is required" }, 400)
  }

  await setConfig(db, key, body.value)
  await insertAuditLog(db, "config_updated", "admin", key, body.value)

  return json({ ok: true, key, value: body.value })
}

function json(data: unknown, status = 200): Response {
  return new Response(JSON.stringify(data), {
    status,
    headers: { "Content-Type": "application/json" },
  })
}
