'use client'

import { ja } from '@/i18n/ja'
import { SECTION_IDS } from '@/lib/constants'
import { FadeIn } from '@/components/ui/FadeIn'
import { DownloadButton } from '@/components/ui/DownloadButton'
import { BlobShape } from '@/components/decorative/BlobShape'

export function FinalCta() {
  return (
    <section
      id={SECTION_IDS.finalCta}
      className="relative overflow-hidden section-padding"
    >
      <BlobShape
        color="bg-brand-200/30"
        size={400}
        className="top-[-100px] left-[-100px]"
      />
      <BlobShape
        color="bg-pastel-mint/30"
        size={350}
        className="bottom-[-50px] right-[-50px]"
      />

      <div className="relative z-10 max-w-2xl mx-auto text-center">
        <FadeIn>
          <h2 className="heading-lg mb-4 whitespace-pre-line">{ja.finalCta.headline}</h2>
        </FadeIn>
        <FadeIn delay={0.1}>
          <p className="body-text mb-10">{ja.finalCta.sub}</p>
        </FadeIn>
        <FadeIn delay={0.2}>
          <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
            <DownloadButton platform="ios" />
            <DownloadButton platform="android" />
          </div>
        </FadeIn>
      </div>
    </section>
  )
}
