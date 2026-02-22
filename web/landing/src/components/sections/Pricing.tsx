'use client'

import Image from 'next/image'
import { ja } from '@/i18n/ja'
import { SECTION_IDS } from '@/lib/constants'
import { images } from '@/lib/assets'
import { SectionWrapper } from '@/components/ui/SectionWrapper'
import { FadeIn } from '@/components/ui/FadeIn'

export function Pricing() {
  return (
    <SectionWrapper id={SECTION_IDS.pricing}>
      <FadeIn>
        <h2 className="heading-md text-center mb-4">{ja.pricing.title}</h2>
      </FadeIn>

      {/* Pricing illustration */}
      <FadeIn delay={0.1}>
        <div className="flex justify-center mb-10">
          <Image
            src={images.pricingFreePro}
            alt="料金プラン"
            width={400}
            height={260}
            className="rounded-3xl"
          />
        </div>
      </FadeIn>

      <div className="grid md:grid-cols-2 gap-6 max-w-2xl mx-auto">
        {/* Free Tier */}
        <FadeIn delay={0.15}>
          <div className="glass-card p-6 md:p-8 h-full flex flex-col">
            <div className="text-center mb-6">
              <h3 className="font-bold text-xl text-text mb-1">
                {ja.pricing.free.name}
              </h3>
              <div className="text-4xl font-black text-brand-500">
                {ja.pricing.free.price}
              </div>
            </div>
            <ul className="space-y-3 flex-1">
              {ja.pricing.free.features.map((f) => (
                <li key={f} className="flex items-center gap-2 text-text-muted">
                  <span className="text-brand-500 shrink-0">✓</span>
                  <span>{f}</span>
                </li>
              ))}
            </ul>
          </div>
        </FadeIn>

        {/* Pro Tier */}
        <FadeIn delay={0.25}>
          <div className="glass-card p-6 md:p-8 h-full flex flex-col relative border-brand-200 border-2">
            <div className="absolute -top-3 left-1/2 -translate-x-1/2">
              <span className="bg-brand-500 text-white text-xs font-bold px-3 py-1 rounded-full">
                {ja.pricing.pro.badge}
              </span>
            </div>
            <div className="text-center mb-6">
              <h3 className="font-bold text-xl text-text mb-1">
                {ja.pricing.pro.name}
              </h3>
              <div className="text-2xl font-bold text-text-muted">
                {ja.pricing.pro.price}
              </div>
            </div>
            <ul className="space-y-3 flex-1">
              {ja.pricing.pro.features.map((f) => (
                <li key={f} className="flex items-center gap-2 text-text-muted">
                  <span className="text-brand-500 shrink-0">✓</span>
                  <span>{f}</span>
                </li>
              ))}
            </ul>
          </div>
        </FadeIn>
      </div>
    </SectionWrapper>
  )
}
