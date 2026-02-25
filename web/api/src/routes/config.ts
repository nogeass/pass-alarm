import type { Env } from "../types"
import { getAllConfigs } from "../lib/db"

export async function handleGetConfig(request: Request, env: Env): Promise<Response> {
  const configs = await getAllConfigs(env.DB)
  const publicKeys = ["REDEEM_DISABLED"]
  const filtered = configs.filter((c) => publicKeys.includes(c.key))

  const result: Record<string, string> = {}
  for (const c of filtered) {
    result[c.key] = c.value
  }

  return new Response(JSON.stringify(result), {
    status: 200,
    headers: { "Content-Type": "application/json" },
  })
}
