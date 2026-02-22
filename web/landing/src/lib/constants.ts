export const APPSTORE_URL = process.env.NEXT_PUBLIC_APPSTORE_URL || '#'
export const PLAYSTORE_URL = process.env.NEXT_PUBLIC_PLAYSTORE_URL || '#'

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
  contact: 'mailto:support@nogeass.com',
  privacy: '/privacy',
  terms: '/terms',
  oss: '/oss',
} as const
