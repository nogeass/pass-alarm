import Image from 'next/image'
import { ja } from '@/i18n/ja'
import { EXTERNAL_LINKS } from '@/lib/constants'
import { images } from '@/lib/assets'

export function Footer() {
  return (
    <footer className="relative border-t border-black/5 overflow-hidden">
      {/* Night map background */}
      <div className="absolute inset-0 -z-10">
        <Image
          src={images.footerMapNight}
          alt=""
          fill
          className="object-cover opacity-30"
          sizes="100vw"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-gray-900/90 to-gray-900/70" />
      </div>

      <div className="max-w-5xl mx-auto px-4 py-10 relative z-10">
        <div className="flex flex-col sm:flex-row items-center justify-center gap-4 sm:gap-6 mb-6 text-sm">
          <a
            href={EXTERNAL_LINKS.privacy}
            className="text-gray-300 hover:text-white transition"
          >
            {ja.footer.privacy}
          </a>
          <a
            href={EXTERNAL_LINKS.terms}
            className="text-gray-300 hover:text-white transition"
          >
            {ja.footer.terms}
          </a>
          <a
            href={EXTERNAL_LINKS.contact}
            className="text-gray-300 hover:text-white transition"
          >
            {ja.footer.contact}
          </a>
        </div>
        <p className="text-center text-sm text-gray-400">
          <a
            href="https://nogeass.com"
            target="_blank"
            rel="noopener noreferrer"
            className="hover:text-white transition"
          >
            {ja.footer.copyright}
          </a>
        </p>
      </div>
    </footer>
  )
}
