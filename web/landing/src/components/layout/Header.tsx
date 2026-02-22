'use client'

import { useEffect, useState } from 'react'
import { ja } from '@/i18n/ja'
import { DownloadButton } from '@/components/ui/DownloadButton'

export function Header() {
  const [scrolled, setScrolled] = useState(false)

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 50)
    }
    window.addEventListener('scroll', handleScroll, { passive: true })
    return () => window.removeEventListener('scroll', handleScroll)
  }, [])

  return (
    <header
      className={`
        fixed top-0 left-0 right-0 z-50 transition-all duration-300
        ${
          scrolled
            ? 'bg-white/80 backdrop-blur-lg shadow-sm border-b border-black/5'
            : 'bg-transparent'
        }
      `}
    >
      <div className="max-w-6xl mx-auto px-4 h-16 flex items-center justify-between">
        <a href="/" className="flex items-center gap-2">
          <span className="text-2xl">⏰</span>
          <div className="flex flex-col leading-tight">
            <span className="font-black text-lg text-text tracking-tight">
              {ja.header.logo}
            </span>
          </div>
        </a>

        <div className="hidden md:flex items-center gap-3">
          <DownloadButton platform="ios" variant="compact" />
          <DownloadButton platform="android" variant="compact" />
        </div>
      </div>
    </header>
  )
}
