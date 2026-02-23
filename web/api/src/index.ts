interface Env {
  GITHUB_TOKEN: string
  API_KEY: string
}

interface FeedbackBody {
  message: string
  appVersion?: string
  device?: string
  osVersion?: string
  platform?: string
}

const REPO_OWNER = "nogeass"
const REPO_NAME = "pass-alarm"

const CORS_HEADERS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, X-API-Key",
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    if (request.method === "OPTIONS") {
      return new Response(null, { status: 204, headers: CORS_HEADERS })
    }

    const url = new URL(request.url)

    if (url.pathname === "/feedback" && request.method === "POST") {
      return handleFeedback(request, env)
    }

    return json({ error: "Not Found" }, 404)
  },
}

async function handleFeedback(request: Request, env: Env): Promise<Response> {
  // Auth check
  const apiKey = request.headers.get("X-API-Key")
  if (apiKey !== env.API_KEY) {
    return json({ error: "Unauthorized" }, 401)
  }

  // Parse body
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

  // Build issue body
  const lines = [message, "", "---", ""]
  if (body.platform) lines.push(`**Platform:** ${body.platform}`)
  if (body.device) lines.push(`**Device:** ${body.device}`)
  if (body.osVersion) lines.push(`**OS:** ${body.osVersion}`)
  if (body.appVersion) lines.push(`**App version:** ${body.appVersion}`)

  const issueBody = lines.join("\n")
  const issueTitle = `[Feedback] ${message.slice(0, 60)}${message.length > 60 ? "…" : ""}`

  // Create GitHub issue
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
    headers: { "Content-Type": "application/json", ...CORS_HEADERS },
  })
}
