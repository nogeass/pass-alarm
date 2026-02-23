export const ja = {
  meta: {
    title: 'パスアラーム - OFFにしない目覚まし',
    description:
      'OFFしない。今日だけスキップ。祝日も連続アラームも、もう迷わない。',
    ogTitle: 'パスアラーム',
    ogDescription: 'OFFにしない目覚まし。今日だけスキップ。',
  },

  header: {
    logo: 'パスアラーム',
    logoSub: 'OFFにしない目覚まし',
  },

  hero: {
    headline: 'OFFにしない目覚まし\n「パスアラーム」',
    sub: '鳴らしたくない日は、OFFじゃなく"スキップ"。',
    note: '',
  },

  pain: {
    title: 'OFFの問題、知ってますか？',
    off: {
      label: 'OFF',
      title: '今日OFFにすると…',
      desc: '明日も鳴らない。\n気づくのは寝坊した朝。',
    },
    skip: {
      label: 'スキップ',
      title: '今日スキップすると…',
      desc: '今日だけ静か。\n明日はちゃんと鳴る。',
    },
  },

  features: {
    title: '3つの特長',
    items: [
      {
        emoji: '📅',
        title: '今日だけスキップ',
        desc: 'OFFにしないから、戻し忘れがない。',
      },
      {
        emoji: '👀',
        title: '次が見えるキュー',
        desc: '次のアラームをスワイプでスキップ。',
      },
      {
        emoji: '🎌',
        title: '祝日は鳴らさない',
        desc: '日本の祝日に対応。休日に起こされない。',
      },
    ],
  },

  howItWorks: {
    title: '使い方はかんたん',
    steps: [
      {
        num: '1',
        title: 'アラームを作る',
        desc: '時間と曜日を選ぶだけ。',
      },
      {
        num: '2',
        title: '鳴らない日はスキップ',
        desc: '今日休みなら「今日だけスキップ」。',
      },
      {
        num: '3',
        title: '明日も自動で鳴る',
        desc: 'OFFにしてないから、明日も安心。',
      },
    ],
  },

  pricing: {
    title: '料金',
    free: {
      name: '無料',
      price: '¥0',
      features: [
        'アラーム3つまで',
        '今日だけスキップ',
        '祝日スキップ',
        'キュー表示',
      ],
    },
    pro: {
      name: 'Pro',
      badge: 'アプリ内課金',
      price: 'Coming Soon',
      features: [
        'アラーム無制限',
        '連続アラーム',
        'カスタムサウンド',
        '優先サポート',
      ],
    },
  },

  appScreens: {
    title: 'アプリ画面',
    placeholder: 'Coming Soon',
  },

  faq: {
    title: 'よくある質問',
    items: [
      {
        q: 'なぜOFFではなくスキップ？',
        a: 'OFFにすると明日のアラームも消えてしまいます。スキップなら今日だけ無効になり、明日は自動で鳴ります。',
      },
      {
        q: '通知をOFFにしていても鳴りますか？',
        a: 'いいえ。パスアラームはOSの通知機能を使用します。通知許可が必須です。',
      },
      {
        q: 'iOSで必ず鳴りますか？',
        a: '設定により通知・音が抑制される場合があります。おやすみモード・集中モード中は鳴らない場合があります。',
      },
      {
        q: '祝日対応は？',
        a: '日本の祝日に対応しています。祝日は自動でスキップされます。',
      },
      {
        q: '本当に無料ですか？',
        a: 'はい。基本機能は完全無料です。Pro機能（目覚まし上限解放）のみアプリ内課金が必要です。',
      },
    ],
  },

  finalCta: {
    headline: 'もう、祝日の前夜に慌てない。',
    sub: '今すぐ無料ダウンロード',
  },

  download: {
    appStore: 'iOSで無料ダウンロード',
    playStore: 'Androidで無料ダウンロード',
    appStoreShort: 'App Store',
    playStoreShort: 'Google Play',
  },

  footer: {
    privacy: 'プライバシーポリシー',
    terms: '利用規約',
    oss: 'オープンソース',
    contact: 'お問い合わせ',
    copyright: '© 2025 nogeass.com',
  },

  privacyPage: {
    title: 'プライバシーポリシー',
    body: 'パスアラーム（以下「本アプリ」）は、利用者のプライバシーを尊重します。本アプリは個人情報を収集・送信しません。アラームデータは端末内にのみ保存されます。',
  },

  termsPage: {
    title: '利用規約',
    body: '本アプリは現状有姿で提供されます。アラームの動作はOS・端末設定に依存するため、確実な動作を保証するものではありません。通知許可が必須です。',
  },

  ossPage: {
    title: 'オープンソース',
    intro:
      'Pass Alarmのソースコードはオープンソース（Apache License 2.0）です。',
    brandNotice:
      '「Pass Alarm」「パスアラーム」の名称・ロゴ・アイコンはNogeass Inc.の商標です。フォークする場合は異なるブランド名をご使用ください。',
    githubLabel: 'GitHubで見る',
  },
} as const

export type I18n = typeof ja
