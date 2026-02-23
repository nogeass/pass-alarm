'use client'

import Image from 'next/image'
import { ja } from '@/i18n/ja'
import { SECTION_IDS } from '@/lib/constants'
import { images } from '@/lib/assets'
import { SectionWrapper } from '@/components/ui/SectionWrapper'
import { Card } from '@/components/ui/Card'
import { FadeIn } from '@/components/ui/FadeIn'
import { DeviceFrame } from '@/components/decorative/DeviceFrame'

const featureScreenshots = [
  images.ssIosAlarmList,
  images.ssIosQueue,
  images.ssIosSettings,
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
            <Card className="h-full text-center flex flex-col items-center">
              <div className="mb-4 scale-75 origin-top">
                <DeviceFrame>
                  <Image
                    src={featureScreenshots[i]}
                    alt={feature.title}
                    width={280}
                    height={600}
                    className="w-full h-full object-cover"
                  />
                </DeviceFrame>
              </div>
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
