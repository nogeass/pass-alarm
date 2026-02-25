-- Crowdfunding backer redemption schema

CREATE TABLE IF NOT EXISTS backers (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  email TEXT NOT NULL,
  name TEXT NOT NULL DEFAULT '',
  tier TEXT NOT NULL DEFAULT 'pro',
  source TEXT NOT NULL DEFAULT 'crowdfund',
  created_at TEXT NOT NULL DEFAULT (datetime('now')),
  notes TEXT
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_backers_email ON backers(email);

CREATE TABLE IF NOT EXISTS tokens (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  token TEXT NOT NULL UNIQUE,
  backer_id INTEGER NOT NULL REFERENCES backers(id),
  status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'claimed', 'revoked')),
  claimed_by TEXT,
  claimed_at TEXT,
  expires_at TEXT,
  created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_tokens_token ON tokens(token);
CREATE INDEX IF NOT EXISTS idx_tokens_backer_id ON tokens(backer_id);

CREATE TABLE IF NOT EXISTS entitlements (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  firebase_uid TEXT NOT NULL,
  tier TEXT NOT NULL DEFAULT 'pro',
  source TEXT NOT NULL DEFAULT 'crowdfund',
  granted_at TEXT NOT NULL DEFAULT (datetime('now')),
  expires_at TEXT,
  revoked_at TEXT,
  token_id INTEGER REFERENCES tokens(id)
);

CREATE INDEX IF NOT EXISTS idx_entitlements_uid ON entitlements(firebase_uid);

CREATE TABLE IF NOT EXISTS audit_logs (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  action TEXT NOT NULL,
  actor TEXT NOT NULL,
  target TEXT,
  detail TEXT,
  created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS config (
  key TEXT PRIMARY KEY,
  value TEXT NOT NULL,
  updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

INSERT OR IGNORE INTO config (key, value) VALUES ('REDEEM_DISABLED', 'false');
