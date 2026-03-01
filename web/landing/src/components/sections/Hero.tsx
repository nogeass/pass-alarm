'use client'

import Image from 'next/image'
import { motion, useReducedMotion, useScroll, useTransform } from 'framer-motion'
import { ja } from '@/i18n/ja'
import { SECTION_IDS } from '@/lib/constants'
import { images } from '@/lib/assets'
import { DownloadButton } from '@/components/ui/DownloadButton'
import { BlobShape } from '@/components/decorative/BlobShape'

export function Hero() {
  const shouldReduceMotion = useReducedMotion()
  const { scrollY } = useScroll()
  const bgY = useTransform(scrollY, [0, 800], ['0%', '30%'])

  const container = {
    hidden: {},
    show: {
      transition: {
        staggerChildren: shouldReduceMotion ? 0 : 0.15,
      },
    },
  }

  const item = {
    hidden: shouldReduceMotion ? {} : { opacity: 0, y: 20 },
    show: {
      opacity: 1,
      y: 0,
      transition: { duration: 0.6, ease: 'easeOut' },
    },
  }

  return (
    <section
      id={SECTION_IDS.hero}
      className="relative min-h-screen flex items-center justify-center overflow-hidden pt-16"
    >
      {/* Hero map background image with parallax */}
      <motion.div
        className="absolute inset-0 -z-10"
        style={{ y: shouldReduceMotion ? 0 : bgY }}
      >
        <Image
          src={images.heroMap}
          alt=""
          fill
          className="object-cover opacity-60 scale-[1.3]"
          priority
          sizes="100vw"
        />
        <div className="absolute inset-0 bg-gradient-to-b from-surface/30 via-surface/10 to-surface/80" />
      </motion.div>

      {/* Decorative blobs */}
      <BlobShape
        color="bg-pastel-lilac/20"
        size={400}
        className="top-10 -right-20"
      />
      <BlobShape
        color="bg-pastel-sky/20"
        size={350}
        className="-bottom-10 -left-20"
      />

      <motion.div
        variants={container}
        initial="hidden"
        animate="show"
        className="relative z-10 text-center px-4 max-w-3xl mx-auto"
      >
        {/* Pin + ripple illustration */}
        <motion.div variants={item} className="mb-6">
          <Image
            src={images.heroPinRipple}
            alt="パスアラーム"
            width={160}
            height={160}
            className="mx-auto drop-shadow-lg"
            priority
          />
        </motion.div>

        <motion.h1
          variants={item}
          className="text-4xl md:text-6xl lg:text-7xl font-black tracking-tight text-text leading-tight whitespace-pre-line text-balance"
        >
          {ja.hero.headline}
        </motion.h1>

        <motion.p
          variants={item}
          className="mt-6 text-lg md:text-xl text-text-muted max-w-lg mx-auto"
        >
          {ja.hero.sub}
        </motion.p>

        <motion.div
          variants={item}
          className="mt-10 flex flex-col sm:flex-row items-center justify-center gap-4"
        >
          <DownloadButton platform="ios" />
          <DownloadButton platform="android" />
        </motion.div>
      </motion.div>
    </section>
  )
}
