'use client'

import Image from 'next/image'
import { ja } from '@/i18n/ja'
import { SECTION_IDS } from '@/lib/constants'
import { images } from '@/lib/assets'
import { SectionWrapper } from '@/components/ui/SectionWrapper'
import { Card } from '@/components/ui/Card'
import { FadeIn } from '@/components/ui/FadeIn'

const featureImages = [
  images.heroBell,
  images.featureQueueCards,
  images.featureHoliday,
]

export function Features() {
  return (
    <SectionWrapper id={SECTION_IDS.features} className="relative">
      <FadeIn>
        <h2 className="heading-md text-center mb-12">{ja.features.title}</h2>
      </FadeIn>

      <div className="grid md:grid-cols-3 gap-6">
        {ja.features.items.map((feature, i) => (
          <FadeIn key={feature.title} delay={i * 0.15}>
            <Card className="h-full text-center">
              <Image
                src={featureImages[i]}
                alt={feature.title}
                width={200}
                height={200}
                className="mx-auto mb-4 rounded-2xl"
              />
              <h3 className="font-bold text-lg mb-2 text-text">
                {feature.title}
              </h3>
              <p className="text-text-muted text-sm leading-relaxed">
                {feature.desc}
              </p>
            </Card>
          </FadeIn>
        ))}
      </div>
    </SectionWrapper>
  )
}
