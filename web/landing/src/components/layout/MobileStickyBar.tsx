'use client'

import { DownloadButton } from '@/components/ui/DownloadButton'

export function MobileStickyBar() {
  return (
    <div
      className="
        fixed bottom-0 left-0 right-0 z-50 md:hidden
        bg-white/90 backdrop-blur-lg border-t border-black/5
        px-4 py-3
      "
      style={{ paddingBottom: 'max(0.75rem, env(safe-area-inset-bottom))' }}
    >
      <div className="flex gap-3">
        <DownloadButton platform="ios" variant="compact" className="flex-1" />
        <DownloadButton
          platform="android"
          variant="compact"
          className="flex-1"
        />
      </div>
    </div>
  )
}
