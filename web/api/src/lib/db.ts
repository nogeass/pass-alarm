import type { Env, Backer, Token, Entitlement, AuditLog, Config } from "../types"

export async function getConfig(db: D1Database, key: string): Promise<string | null> {
  const row = await db.prepare("SELECT value FROM config WHERE key = ?").bind(key).first<Config>()
  return row?.value ?? null
}

export async function setConfig(db: D1Database, key: string, value: string): Promise<void> {
  await db
    .prepare("INSERT INTO config (key, value, updated_at) VALUES (?, ?, datetime('now')) ON CONFLICT(key) DO UPDATE SET value = ?, updated_at = datetime('now')")
    .bind(key, value, value)
    .run()
}

export async function isRedeemDisabled(db: D1Database): Promise<boolean> {
  const val = await getConfig(db, "REDEEM_DISABLED")
  return val === "true"
}

export async function findTokenByCode(db: D1Database, token: string): Promise<Token | null> {
  return db.prepare("SELECT * FROM tokens WHERE token = ?").bind(token).first<Token>()
}

export async function getEntitlementsByUid(db: D1Database, uid: string): Promise<Entitlement[]> {
  const result = await db.prepare("SELECT * FROM entitlements WHERE firebase_uid = ? AND revoked_at IS NULL ORDER BY granted_at DESC").bind(uid).all<Entitlement>()
  return result.results
}

export async function insertAuditLog(
  db: D1Database,
  action: string,
  actor: string,
  target?: string,
  detail?: string,
): Promise<void> {
  await db
    .prepare("INSERT INTO audit_logs (action, actor, target, detail) VALUES (?, ?, ?, ?)")
    .bind(action, actor, target ?? null, detail ?? null)
    .run()
}

export async function getAllBackers(db: D1Database, limit = 500, offset = 0): Promise<Backer[]> {
  const result = await db.prepare("SELECT * FROM backers ORDER BY id DESC LIMIT ? OFFSET ?").bind(limit, offset).all<Backer>()
  return result.results
}

export async function getAllTokens(db: D1Database, limit = 500, offset = 0): Promise<(Token & { backer_email?: string })[]> {
  const result = await db
    .prepare("SELECT t.*, b.email as backer_email FROM tokens t LEFT JOIN backers b ON t.backer_id = b.id ORDER BY t.id DESC LIMIT ? OFFSET ?")
    .bind(limit, offset)
    .all()
  return result.results as unknown as (Token & { backer_email?: string })[]
}

export async function getAllEntitlements(db: D1Database, limit = 500, offset = 0): Promise<Entitlement[]> {
  const result = await db.prepare("SELECT * FROM entitlements ORDER BY id DESC LIMIT ? OFFSET ?").bind(limit, offset).all<Entitlement>()
  return result.results
}

export async function getAuditLogs(db: D1Database, limit = 200, offset = 0): Promise<AuditLog[]> {
  const result = await db.prepare("SELECT * FROM audit_logs ORDER BY id DESC LIMIT ? OFFSET ?").bind(limit, offset).all<AuditLog>()
  return result.results
}

export async function getAllConfigs(db: D1Database): Promise<Config[]> {
  const result = await db.prepare("SELECT * FROM config ORDER BY key").all<Config>()
  return result.results
}
