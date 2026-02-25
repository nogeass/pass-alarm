import type { Env, AuthUser } from "../types"
import { getEntitlementsByUid } from "../lib/db"

export async function handleGetEntitlements(request: Request, env: Env, user: AuthUser): Promise<Response> {
  const entitlements = await getEntitlementsByUid(env.DB, user.uid)

  return new Response(
    JSON.stringify({
      entitlements: entitlements.map((e) => ({
        id: e.id,
        tier: e.tier,
        source: e.source,
        granted_at: e.granted_at,
        expires_at: e.expires_at,
      })),
    }),
    {
      status: 200,
      headers: { "Content-Type": "application/json" },
    },
  )
}
