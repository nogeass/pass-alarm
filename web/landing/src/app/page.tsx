import { Hero } from '@/components/sections/Hero'
import { PainSolution } from '@/components/sections/PainSolution'
import { Features } from '@/components/sections/Features'
import { HowItWorks } from '@/components/sections/HowItWorks'
import { Pricing } from '@/components/sections/Pricing'
import { FinalCta } from '@/components/sections/FinalCta'

export default function Home() {
  return (
    <>
      <Hero />
      <PainSolution />
      <Features />
      <HowItWorks />
      <Pricing />
      <FinalCta />
    </>
  )
}
