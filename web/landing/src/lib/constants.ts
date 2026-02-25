export const APPSTORE_URL = process.env.NEXT_PUBLIC_APPSTORE_URL || 'https://apps.apple.com/us/app/off%E3%81%AB%E3%81%97%E3%81%AA%E3%81%84%E7%9B%AE%E8%A6%9A%E3%81%BE%E3%81%97-%E3%83%91%E3%82%B9%E3%82%A2%E3%83%A9%E3%83%BC%E3%83%A0/id6759545599'
export const PLAYSTORE_URL = process.env.NEXT_PUBLIC_PLAYSTORE_URL || 'https://play.google.com/store/apps/details?id=com.nogeass.passalarm'

export const SECTION_IDS = {
  hero: 'hero',
  pain: 'pain',
  features: 'features',
  howItWorks: 'how-it-works',
  pricing: 'pricing',
  appScreens: 'app-screens',
  faq: 'faq',
  finalCta: 'final-cta',
} as const

export const EXTERNAL_LINKS = {
  github: 'https://github.com/nogeass/pass-alarm',
  contact: '/contact',
  privacy: '/privacy',
  terms: '/terms',
  oss: '/oss',
} as const
