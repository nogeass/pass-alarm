'use client'

import Image from 'next/image'
import { ja } from '@/i18n/ja'
import { SECTION_IDS } from '@/lib/constants'
import { images } from '@/lib/assets'
import { SectionWrapper } from '@/components/ui/SectionWrapper'
import { FadeIn } from '@/components/ui/FadeIn'
import { GridPattern } from '@/components/decorative/GridPattern'

export function HowItWorks() {
  return (
    <SectionWrapper
      id={SECTION_IDS.howItWorks}
      className="relative bg-surface-muted overflow-hidden"
      wide
    >
      <GridPattern className="opacity-50" />

      <div className="max-w-5xl mx-auto relative z-10">
        <FadeIn>
          <h2 className="heading-md text-center mb-8">
            {ja.howItWorks.title}
          </h2>
        </FadeIn>

        {/* 3-step illustration */}
        <FadeIn delay={0.1}>
          <div className="flex justify-center mb-12">
            <Image
              src={images.how3Steps}
              alt="3ステップ"
              width={600}
              height={400}
              className="rounded-3xl"
            />
          </div>
        </FadeIn>

        <div className="grid md:grid-cols-3 gap-8 md:gap-12">
          {ja.howItWorks.steps.map((step, i) => (
            <FadeIn key={step.num} delay={i * 0.2}>
              <div className="text-center">
                <span className="inline-flex items-center justify-center w-10 h-10 rounded-full bg-brand-500 text-white text-lg font-bold mb-4 shadow-lg">
                  {step.num}
                </span>
                <h3 className="font-bold text-lg mb-2 text-text">
                  {step.title}
                </h3>
                <p className="text-text-muted text-sm">{step.desc}</p>
              </div>
            </FadeIn>
          ))}
        </div>
      </div>
    </SectionWrapper>
  )
}
