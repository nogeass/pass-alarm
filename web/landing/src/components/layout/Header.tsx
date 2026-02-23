'use client'

import { useEffect, useState } from 'react'
import Image from 'next/image'
import { ja } from '@/i18n/ja'

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
      <div className="max-w-6xl mx-auto px-4 h-16 flex items-center">
        <a href="/" className="flex items-center gap-2">
          <Image
            src="/icon-512.png"
            alt="パスアラーム"
            width={36}
            height={36}
            className="rounded-lg"
          />
          <div className="flex flex-col leading-tight">
            <span className="font-black text-base md:text-lg text-text tracking-tight">
              {ja.header.logoSub}
              <span className="hidden md:inline">「{ja.header.logo}」</span>
            </span>
            <span className="text-[10px] text-text-muted md:hidden">
              「{ja.header.logo}」
            </span>
          </div>
        </a>
      </div>
    </header>
  )
}
