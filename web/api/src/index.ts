import type { Env } from "./types"
import { extractAuthUser } from "./auth"
import { handleClaim } from "./routes/redeem"
import { handleGetEntitlements } from "./routes/entitlements"
import { handleGetConfig } from "./routes/config"
import { handleAdmin } from "./routes/admin"

const CORS_HEADERS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, Authorization, X-API-Key, X-Admin-Key",
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    if (request.method === "OPTIONS") {
      return new Response(null, { status: 204, headers: CORS_HEADERS })
    }

    const url = new URL(request.url)
    const path = url.pathname

    let response: Response

    try {
      response = await route(request, env, path)
    } catch (err) {
      console.error("Unhandled error:", err)
      response = json({ error: "Internal Server Error" }, 500)
    }

    // Add CORS headers to all responses
    for (const [key, value] of Object.entries(CORS_HEADERS)) {
      response.headers.set(key, value)
    }

    return response
  },
}

async function route(request: Request, env: Env, path: string): Promise<Response> {
  // --- Legacy: Feedback endpoint ---
  if (path === "/feedback" && request.method === "POST") {
    return handleFeedback(request, env)
  }

  // --- Public: Config ---
  if (path === "/api/config" && request.method === "GET") {
    return handleGetConfig(request, env)
  }

  // --- Admin endpoints ---
  if (path.startsWith("/api/admin/")) {
    return handleAdmin(request, env, path)
  }

  // --- Authenticated endpoints ---
  if (path === "/api/redeem/claim" && request.method === "POST") {
    const user = await extractAuthUser(request)
    if (!user) return json({ error: "Authentication required" }, 401)
    return handleClaim(request, env, user)
  }

  if (path === "/api/me/entitlements" && request.method === "GET") {
    const user = await extractAuthUser(request)
    if (!user) return json({ error: "Authentication required" }, 401)
    return handleGetEntitlements(request, env, user)
  }

  return json({ error: "Not Found" }, 404)
}

// --- Legacy feedback handler (unchanged) ---
interface FeedbackBody {
  message: string
  appVersion?: string
  device?: string
  osVersion?: string
  platform?: string
}

const REPO_OWNER = "nogeass"
const REPO_NAME = "pass-alarm"

async function handleFeedback(request: Request, env: Env): Promise<Response> {
  const apiKey = request.headers.get("X-API-Key")
  if (apiKey !== env.API_KEY) {
    return json({ error: "Unauthorized" }, 401)
  }

  let body: FeedbackBody
  try {
    body = await request.json()
  } catch {
    return json({ error: "Invalid JSON" }, 400)
  }

  const message = body.message?.trim()
  if (!message || message.length === 0) {
    return json({ error: "message is required" }, 400)
  }
  if (message.length > 2000) {
    return json({ error: "message too long (max 2000)" }, 400)
  }

  const lines = [message, "", "---", ""]
  if (body.platform) lines.push(`**Platform:** ${body.platform}`)
  if (body.device) lines.push(`**Device:** ${body.device}`)
  if (body.osVersion) lines.push(`**OS:** ${body.osVersion}`)
  if (body.appVersion) lines.push(`**App version:** ${body.appVersion}`)

  const issueBody = lines.join("\n")
  const issueTitle = `[Feedback] ${message.slice(0, 60)}${message.length > 60 ? "…" : ""}`

  const res = await fetch(
    `https://api.github.com/repos/${REPO_OWNER}/${REPO_NAME}/issues`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${env.GITHUB_TOKEN}`,
        Accept: "application/vnd.github+json",
        "User-Agent": "pass-alarm-api",
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        title: issueTitle,
        body: issueBody,
        labels: ["feedback"],
      }),
    },
  )

  if (!res.ok) {
    const text = await res.text()
    console.error("GitHub API error:", res.status, text)
    return json({ error: "Failed to create issue" }, 502)
  }

  const issue = (await res.json()) as { number: number; html_url: string }
  return json({ ok: true, issueNumber: issue.number })
}

function json(data: unknown, status = 200): Response {
  return new Response(JSON.stringify(data), {
    status,
    headers: { "Content-Type": "application/json" },
  })
}
