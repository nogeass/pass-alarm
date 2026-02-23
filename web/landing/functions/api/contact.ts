interface Env {
  GITHUB_TOKEN: string
  TURNSTILE_SECRET_KEY: string
}

interface ContactBody {
  name: string
  email: string
  message: string
  turnstileToken: string
}

export const onRequestPost: PagesFunction<Env> = async (context) => {
  const corsHeaders = {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'POST, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type',
  }

  try {
    const body = (await context.request.json()) as ContactBody
    const { name, email, message, turnstileToken } = body

    if (!name || !email || !message) {
      return new Response(
        JSON.stringify({ error: '必須項目を入力してください。' }),
        { status: 400, headers: { 'Content-Type': 'application/json', ...corsHeaders } },
      )
    }

    // Verify Turnstile token
    if (context.env.TURNSTILE_SECRET_KEY) {
      const turnstileRes = await fetch(
        'https://challenges.cloudflare.com/turnstile/v0/siteverify',
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body: new URLSearchParams({
            secret: context.env.TURNSTILE_SECRET_KEY,
            response: turnstileToken || '',
            remoteip: context.request.headers.get('CF-Connecting-IP') || '',
          }),
        },
      )
      const turnstileData = (await turnstileRes.json()) as { success: boolean }
      if (!turnstileData.success) {
        return new Response(
          JSON.stringify({ error: '認証に失敗しました。もう一度お試しください。' }),
          { status: 403, headers: { 'Content-Type': 'application/json', ...corsHeaders } },
        )
      }
    }

    // Create GitHub Issue
    const issueBody = `## お問い合わせ

**お名前:** ${name}
**メールアドレス:** ${email}

### 内容
${message}

---
_このIssueはお問い合わせフォームから自動作成されました。_`

    const ghRes = await fetch(
      'https://api.github.com/repos/nogeass/pass-alarm/issues',
      {
        method: 'POST',
        headers: {
          Authorization: `token ${context.env.GITHUB_TOKEN}`,
          Accept: 'application/vnd.github.v3+json',
          'Content-Type': 'application/json',
          'User-Agent': 'pass-alarm-contact-form',
        },
        body: JSON.stringify({
          title: `[お問い合わせ] ${name}`,
          body: issueBody,
          labels: ['contact'],
        }),
      },
    )

    if (!ghRes.ok) {
      const errText = await ghRes.text()
      console.error('GitHub API error:', errText)
      return new Response(
        JSON.stringify({ error: '送信に失敗しました。しばらくしてからお試しください。' }),
        { status: 500, headers: { 'Content-Type': 'application/json', ...corsHeaders } },
      )
    }

    return new Response(
      JSON.stringify({ success: true, message: 'お問い合わせを受け付けました。' }),
      { status: 200, headers: { 'Content-Type': 'application/json', ...corsHeaders } },
    )
  } catch {
    return new Response(
      JSON.stringify({ error: 'サーバーエラーが発生しました。' }),
      { status: 500, headers: { 'Content-Type': 'application/json', ...corsHeaders } },
    )
  }
}

export const onRequestOptions: PagesFunction = async () => {
  return new Response(null, {
    headers: {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'POST, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type',
    },
  })
}
