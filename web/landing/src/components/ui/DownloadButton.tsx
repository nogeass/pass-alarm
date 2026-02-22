'use client'

import { APPSTORE_URL, PLAYSTORE_URL } from '@/lib/constants'
import { ja } from '@/i18n/ja'

type Props = {
  platform: 'ios' | 'android'
  variant?: 'primary' | 'compact'
  className?: string
}

export function DownloadButton({
  platform,
  variant = 'primary',
  className = '',
}: Props) {
  const url = platform === 'ios' ? APPSTORE_URL : PLAYSTORE_URL
  const label =
    variant === 'compact'
      ? platform === 'ios'
        ? ja.download.appStoreShort
        : ja.download.playStoreShort
      : platform === 'ios'
        ? ja.download.appStore
        : ja.download.playStore

  const icon = platform === 'ios' ? '🍎' : '🤖'

  return (
    <a
      href={url}
      target="_blank"
      rel="noopener noreferrer"
      className={`
        inline-flex items-center justify-center gap-2 font-bold
        rounded-2xl transition-all duration-200
        ${
          variant === 'primary'
            ? 'bg-brand-500 text-white px-6 py-3.5 text-base hover:bg-brand-600 hover:scale-[1.02] active:scale-[0.98] shadow-lg shadow-brand-500/20'
            : 'bg-white/80 backdrop-blur text-text px-4 py-2 text-sm hover:bg-white border border-white/60'
        }
        ${className}
      `}
    >
      <span className="text-lg">{icon}</span>
      <span>{label}</span>
    </a>
  )
}
