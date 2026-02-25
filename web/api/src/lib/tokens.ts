const CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789"
const TOKEN_LENGTH = 12

export function generateToken(): string {
  const bytes = new Uint8Array(TOKEN_LENGTH)
  crypto.getRandomValues(bytes)
  return Array.from(bytes, (b) => CHARS[b % CHARS.length]).join("")
}

export function generateTokens(count: number): string[] {
  return Array.from({ length: count }, () => generateToken())
}
