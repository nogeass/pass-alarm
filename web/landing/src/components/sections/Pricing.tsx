'use client'

import { ja } from '@/i18n/ja'
import { SECTION_IDS } from '@/lib/constants'
import { SectionWrapper } from '@/components/ui/SectionWrapper'
import { FadeIn } from '@/components/ui/FadeIn'

export function Pricing() {
  return (
    <SectionWrapper id={SECTION_IDS.pricing}>
      <FadeIn>
        <h2 className="heading-md text-center mb-2">{ja.pricing.title}</h2>
        <p className="text-center text-brand-500 font-bold text-sm mb-10">
          {ja.pricing.trial}
        </p>
      </FadeIn>

      <div className="grid md:grid-cols-3 gap-6 max-w-4xl mx-auto">
        {/* Free Tier */}
        <FadeIn delay={0.1}>
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
                <li key={f} className="flex items-center gap-2 text-text-muted text-sm">
                  <span className="text-brand-500 shrink-0">✓</span>
                  <span>{f}</span>
                </li>
              ))}
            </ul>
          </div>
        </FadeIn>

        {/* Pro Monthly */}
        <FadeIn delay={0.2}>
          <div className="glass-card p-6 md:p-8 h-full flex flex-col relative border-brand-200 border-2">
            <div className="absolute -top-3 left-1/2 -translate-x-1/2">
              <span className="bg-brand-500 text-white text-xs font-bold px-3 py-1 rounded-full">
                {ja.pricing.proMonthly.badge}
              </span>
            </div>
            <div className="text-center mb-6">
              <h3 className="font-bold text-xl text-text mb-1">
                {ja.pricing.proMonthly.name}
              </h3>
              <div className="text-4xl font-black text-brand-500">
                {ja.pricing.proMonthly.price}
                <span className="text-base font-normal text-text-muted">
                  {ja.pricing.proMonthly.period}
                </span>
              </div>
              <p className="text-xs text-brand-500 font-bold mt-1">
                {ja.pricing.proMonthly.trial}
              </p>
            </div>
            <ul className="space-y-3 flex-1">
              {ja.pricing.proMonthly.features.map((f) => (
                <li key={f} className="flex items-center gap-2 text-text-muted text-sm">
                  <span className="text-brand-500 shrink-0">✓</span>
                  <span>{f}</span>
                </li>
              ))}
            </ul>
          </div>
        </FadeIn>

        {/* Pro Yearly */}
        <FadeIn delay={0.3}>
          <div className="glass-card p-6 md:p-8 h-full flex flex-col relative">
            <div className="absolute -top-3 left-1/2 -translate-x-1/2">
              <span className="bg-pastel-mint text-text text-xs font-bold px-3 py-1 rounded-full">
                {ja.pricing.proYearly.badge}
              </span>
            </div>
            <div className="text-center mb-6">
              <h3 className="font-bold text-xl text-text mb-1">
                {ja.pricing.proYearly.name}
              </h3>
              <div className="text-4xl font-black text-brand-500">
                {ja.pricing.proYearly.price}
                <span className="text-base font-normal text-text-muted">
                  {ja.pricing.proYearly.period}
                </span>
              </div>
              <p className="text-xs text-brand-500 font-bold mt-1">
                {ja.pricing.proYearly.trial}
              </p>
              <p className="text-xs text-text-muted mt-1">
                {ja.pricing.proYearly.saving}
              </p>
            </div>
            <ul className="space-y-3 flex-1">
              {ja.pricing.proYearly.features.map((f) => (
                <li key={f} className="flex items-center gap-2 text-text-muted text-sm">
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
