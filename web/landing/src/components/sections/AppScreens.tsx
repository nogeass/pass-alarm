'use client'

import Image from 'next/image'
import { ja } from '@/i18n/ja'
import { SECTION_IDS } from '@/lib/constants'
import { images } from '@/lib/assets'
import { SectionWrapper } from '@/components/ui/SectionWrapper'
import { FadeIn } from '@/components/ui/FadeIn'
import { BlobShape } from '@/components/decorative/BlobShape'

export function AppScreens() {
  return (
    <SectionWrapper
      id={SECTION_IDS.appScreens}
      className="relative overflow-hidden"
    >
      <BlobShape
        color="bg-pastel-peach/20"
        size={300}
        className="top-0 -right-20"
      />
      <BlobShape
        color="bg-pastel-sky/20"
        size={250}
        className="bottom-0 -left-20"
      />

      <FadeIn>
        <h2 className="heading-md text-center mb-12 relative z-10">
          {ja.appScreens.title}
        </h2>
      </FadeIn>

      <FadeIn delay={0.2}>
        <div className="flex flex-col sm:flex-row items-center justify-center gap-8 md:gap-12 relative z-10">
          <div className="transform rotate-[-3deg] shadow-2xl rounded-3xl overflow-hidden">
            <Image
              src={images.mockIphone}
              alt="iPhone"
              width={240}
              height={480}
              className="rounded-3xl"
            />
          </div>
          <div className="transform rotate-[3deg] shadow-2xl rounded-3xl overflow-hidden hidden sm:block">
            <Image
              src={images.mockAndroid}
              alt="Android"
              width={240}
              height={480}
              className="rounded-3xl"
            />
          </div>
        </div>
      </FadeIn>
    </SectionWrapper>
  )
}
