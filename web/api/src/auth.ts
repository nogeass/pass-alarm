import type { AuthUser } from "./types"

const GOOGLE_CERTS_URL = "https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com"
const FIREBASE_PROJECT_ID = "project-551854356501"

interface JWTHeader {
  alg: string
  kid: string
  typ: string
}

interface JWTPayload {
  iss: string
  aud: string
  auth_time: number
  sub: string
  iat: number
  exp: number
  email?: string
  email_verified?: boolean
  firebase?: { sign_in_provider: string }
}

let cachedCerts: Record<string, CryptoKey> = {}
let certsCachedAt = 0
const CACHE_DURATION = 3600_000 // 1 hour

async function getPublicKeys(): Promise<Record<string, CryptoKey>> {
  const now = Date.now()
  if (Object.keys(cachedCerts).length > 0 && now - certsCachedAt < CACHE_DURATION) {
    return cachedCerts
  }

  const res = await fetch(GOOGLE_CERTS_URL)
  if (!res.ok) throw new Error("Failed to fetch Google public keys")

  const certs: Record<string, string> = await res.json()
  const keys: Record<string, CryptoKey> = {}

  for (const [kid, pem] of Object.entries(certs)) {
    keys[kid] = await importPublicKey(pem)
  }

  cachedCerts = keys
  certsCachedAt = now
  return keys
}

async function importPublicKey(pem: string): Promise<CryptoKey> {
  const b64 = pem
    .replace("-----BEGIN CERTIFICATE-----", "")
    .replace("-----END CERTIFICATE-----", "")
    .replace(/\s/g, "")

  const binary = Uint8Array.from(atob(b64), (c) => c.charCodeAt(0))

  return crypto.subtle.importKey(
    "raw",
    extractPublicKeyFromCert(binary),
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["verify"],
  )
}

// Extract the SubjectPublicKeyInfo from an X.509 DER certificate
function extractPublicKeyFromCert(cert: Uint8Array): ArrayBuffer {
  // Simple ASN.1 DER parser to find SubjectPublicKeyInfo
  let offset = 0

  function readTag(): { tag: number; length: number; start: number } {
    const tag = cert[offset++]
    let length = cert[offset++]
    const start = offset

    if (length & 0x80) {
      const numBytes = length & 0x7f
      length = 0
      for (let i = 0; i < numBytes; i++) {
        length = (length << 8) | cert[offset++]
      }
      return { tag, length, start: offset }
    }
    return { tag, length, start }
  }

  // TBSCertificate is the first element of the SEQUENCE
  readTag() // outer SEQUENCE
  const tbs = readTag() // TBSCertificate SEQUENCE

  offset = tbs.start

  // version [0] EXPLICIT
  if (cert[offset] === 0xa0) {
    const v = readTag()
    offset = v.start + v.length
  }

  // serialNumber INTEGER
  const serial = readTag()
  offset = serial.start + serial.length

  // signature AlgorithmIdentifier SEQUENCE
  const sig = readTag()
  offset = sig.start + sig.length

  // issuer SEQUENCE
  const issuer = readTag()
  offset = issuer.start + issuer.length

  // validity SEQUENCE
  const validity = readTag()
  offset = validity.start + validity.length

  // subject SEQUENCE
  const subject = readTag()
  offset = subject.start + subject.length

  // subjectPublicKeyInfo SEQUENCE — this is what we want
  const spkiTag = cert[offset]
  const spkiStart = offset
  const spki = readTag()
  const spkiEnd = spki.start + spki.length

  // Re-encode the full SEQUENCE including tag+length
  return cert.slice(spkiStart, spkiEnd).buffer
}

function base64UrlDecode(str: string): Uint8Array {
  const padded = str.replace(/-/g, "+").replace(/_/g, "/")
  const padding = "=".repeat((4 - (padded.length % 4)) % 4)
  return Uint8Array.from(atob(padded + padding), (c) => c.charCodeAt(0))
}

export async function verifyFirebaseToken(idToken: string): Promise<AuthUser> {
  const parts = idToken.split(".")
  if (parts.length !== 3) throw new Error("Invalid token format")

  const headerJson = new TextDecoder().decode(base64UrlDecode(parts[0]))
  const payloadJson = new TextDecoder().decode(base64UrlDecode(parts[1]))

  const header: JWTHeader = JSON.parse(headerJson)
  const payload: JWTPayload = JSON.parse(payloadJson)

  // Verify claims
  if (header.alg !== "RS256") throw new Error("Unsupported algorithm")
  if (payload.aud !== FIREBASE_PROJECT_ID) throw new Error("Invalid audience")
  if (payload.iss !== `https://securetoken.google.com/${FIREBASE_PROJECT_ID}`)
    throw new Error("Invalid issuer")

  const now = Math.floor(Date.now() / 1000)
  if (payload.exp < now) throw new Error("Token expired")
  if (payload.iat > now + 300) throw new Error("Token issued in future")

  // Verify signature
  const keys = await getPublicKeys()
  const key = keys[header.kid]
  if (!key) throw new Error("Unknown key ID")

  const signatureInput = new TextEncoder().encode(`${parts[0]}.${parts[1]}`)
  const signature = base64UrlDecode(parts[2])

  const valid = await crypto.subtle.verify(
    "RSASSA-PKCS1-v1_5",
    key,
    signature,
    signatureInput,
  )

  if (!valid) throw new Error("Invalid signature")

  return { uid: payload.sub, email: payload.email ?? null }
}

export async function extractAuthUser(request: Request): Promise<AuthUser | null> {
  const authHeader = request.headers.get("Authorization")
  if (!authHeader?.startsWith("Bearer ")) return null

  try {
    return await verifyFirebaseToken(authHeader.slice(7))
  } catch {
    return null
  }
}
