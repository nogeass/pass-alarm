'use client'

import Image from 'next/image'
import { ja } from '@/i18n/ja'
import { SECTION_IDS } from '@/lib/constants'
import { images } from '@/lib/assets'
import { SectionWrapper } from '@/components/ui/SectionWrapper'
import { Card } from '@/components/ui/Card'
import { FadeIn } from '@/components/ui/FadeIn'

export function PainSolution() {
  return (
    <SectionWrapper id={SECTION_IDS.pain} className="relative">
      <FadeIn>
        <h2 className="heading-md text-center mb-12">{ja.pain.title}</h2>
      </FadeIn>

      <div className="grid md:grid-cols-2 gap-6 md:gap-8 max-w-3xl mx-auto">
        {/* Pain: OFF */}
        <FadeIn delay={0.1} direction="left">
          <Card variant="danger" className="h-full">
            <div className="text-center">
              <span className="inline-block px-3 py-1 rounded-full bg-pastel-rose/50 text-sm font-bold text-red-600 mb-4">
                {ja.pain.off.label}
              </span>
              <Image
                src={images.painForgetOn}
                alt="OFF戻し忘れ"
                width={300}
                height={200}
                className="mx-auto mb-4 rounded-2xl"
              />
              <h3 className="font-bold text-lg mb-2 text-text">
                {ja.pain.off.title}
              </h3>
              <p className="text-text-muted text-sm whitespace-pre-line leading-relaxed">
                {ja.pain.off.desc}
              </p>
            </div>
          </Card>
        </FadeIn>

        {/* Solution: Skip */}
        <FadeIn delay={0.2} direction="right">
          <Card variant="success" className="h-full">
            <div className="text-center">
              <span className="inline-block px-3 py-1 rounded-full bg-pastel-mint/50 text-sm font-bold text-green-600 mb-4">
                {ja.pain.skip.label}
              </span>
              <Image
                src={images.solutionSkip}
                alt="スキップで解決"
                width={300}
                height={200}
                className="mx-auto mb-4 rounded-2xl"
              />
              <h3 className="font-bold text-lg mb-2 text-text">
                {ja.pain.skip.title}
              </h3>
              <p className="text-text-muted text-sm whitespace-pre-line leading-relaxed">
                {ja.pain.skip.desc}
              </p>
            </div>
          </Card>
        </FadeIn>
      </div>
    </SectionWrapper>
  )
}
