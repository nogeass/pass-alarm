export interface Env {
  GITHUB_TOKEN: string
  API_KEY: string
  ADMIN_API_KEY: string
  DB: D1Database
}

export interface Backer {
  id: number
  email: string
  name: string
  tier: string
  source: string
  created_at: string
  notes: string | null
}

export interface Token {
  id: number
  token: string
  backer_id: number
  status: "pending" | "claimed" | "revoked"
  claimed_by: string | null
  claimed_at: string | null
  expires_at: string | null
  created_at: string
}

export interface Entitlement {
  id: number
  firebase_uid: string
  tier: string
  source: string
  granted_at: string
  expires_at: string | null
  revoked_at: string | null
  token_id: number | null
}

export interface AuditLog {
  id: number
  action: string
  actor: string
  target: string | null
  detail: string | null
  created_at: string
}

export interface Config {
  key: string
  value: string
  updated_at: string
}

export interface AuthUser {
  uid: string
  email: string | null
}
