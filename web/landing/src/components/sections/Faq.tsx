'use client'

import { ja } from '@/i18n/ja'
import { SECTION_IDS } from '@/lib/constants'
import { SectionWrapper } from '@/components/ui/SectionWrapper'
import { FadeIn } from '@/components/ui/FadeIn'
import { Accordion } from '@/components/ui/Accordion'

export function Faq() {
  return (
    <SectionWrapper id={SECTION_IDS.faq}>
      <FadeIn>
        <h2 className="heading-md text-center mb-12">{ja.faq.title}</h2>
      </FadeIn>

      <FadeIn delay={0.1}>
        <div className="max-w-2xl mx-auto glass-card p-6 md:p-8">
          {ja.faq.items.map((item) => (
            <Accordion key={item.q} question={item.q} answer={item.a} />
          ))}
        </div>
      </FadeIn>
    </SectionWrapper>
  )
}
