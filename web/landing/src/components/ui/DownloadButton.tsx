'use client'

import Image from 'next/image'
import { APPSTORE_URL, PLAYSTORE_URL } from '@/lib/constants'

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
  const alt =
    platform === 'ios' ? 'Download on the App Store' : 'Get it on Google Play'
  const badge =
    platform === 'ios' ? '/badges/app-store.svg' : '/badges/google-play.png'

  const height = variant === 'primary' ? 48 : 36
  const width = platform === 'ios'
    ? Math.round(height * 3.375)
    : Math.round(height * 3.375)

  return (
    <a
      href={url}
      target="_blank"
      rel="noopener noreferrer"
      className={`inline-block transition-transform duration-200 hover:scale-[1.03] active:scale-[0.97] ${className}`}
    >
      <Image
        src={badge}
        alt={alt}
        width={width}
        height={height}
        style={{ height: variant === 'primary' ? 48 : 36, width: 'auto' }}
      />
    </a>
  )
}
