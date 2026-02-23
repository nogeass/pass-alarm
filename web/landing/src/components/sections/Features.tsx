'use client'

import Image from 'next/image'
import { ja } from '@/i18n/ja'
import { SECTION_IDS } from '@/lib/constants'
import { SectionWrapper } from '@/components/ui/SectionWrapper'
import { FadeIn } from '@/components/ui/FadeIn'

const featureScreenshots = [
  '/images/feature-alarm-list.webp',
  '/images/feature-queue.webp',
  '/images/feature-settings.webp',
]

export function Features() {
  return (
    <SectionWrapper id={SECTION_IDS.features} className="relative">
      <FadeIn>
        <h2 className="heading-md text-center mb-12">{ja.features.title}</h2>
      </FadeIn>

      <div className="grid md:grid-cols-3 gap-6 max-w-4xl mx-auto">
        {ja.features.items.map((feature, i) => (
          <FadeIn key={feature.title} delay={i * 0.15}>
            <div className="text-center flex flex-col items-center">
              <Image
                src={featureScreenshots[i]}
                alt={feature.title}
                width={280}
                height={606}
                className="w-full max-w-[220px] h-auto mb-4"
              />
              <h3 className="font-bold text-lg mb-2 text-text">
                {feature.title}
              </h3>
              <p className="text-text-muted text-sm leading-relaxed">
                {feature.desc}
              </p>
            </div>
          </FadeIn>
        ))}
      </div>
    </SectionWrapper>
  )
}
