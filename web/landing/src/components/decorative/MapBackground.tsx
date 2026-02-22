'use client'

export function MapBackground() {
  return (
    <div className="absolute inset-0 overflow-hidden -z-10">
      <div
        className="absolute -inset-5 animate-float-slow"
        style={{
          backgroundImage: `
            repeating-linear-gradient(
              0deg,
              transparent, transparent 80px,
              hsl(220 30% 90% / 0.3) 80px, hsl(220 30% 90% / 0.3) 81px
            ),
            repeating-linear-gradient(
              90deg,
              transparent, transparent 80px,
              hsl(220 30% 90% / 0.3) 80px, hsl(220 30% 90% / 0.3) 81px
            ),
            repeating-linear-gradient(
              0deg,
              transparent, transparent 240px,
              hsl(220 30% 85% / 0.4) 240px, hsl(220 30% 85% / 0.4) 242px
            ),
            repeating-linear-gradient(
              90deg,
              transparent, transparent 240px,
              hsl(220 30% 85% / 0.4) 240px, hsl(220 30% 85% / 0.4) 242px
            ),
            radial-gradient(ellipse 200px 150px at 15% 25%, hsl(160 50% 88% / 0.5), transparent),
            radial-gradient(ellipse 300px 200px at 75% 35%, hsl(160 40% 85% / 0.4), transparent),
            radial-gradient(ellipse 180px 250px at 45% 70%, hsl(160 45% 90% / 0.35), transparent),
            radial-gradient(ellipse 400px 200px at 85% 80%, hsl(200 60% 88% / 0.4), transparent),
            radial-gradient(ellipse 250px 300px at 25% 65%, hsl(30 50% 92% / 0.25), transparent)
          `,
          backgroundColor: 'hsl(220 20% 97%)',
        }}
      />

      {/* Decorative pin markers */}
      <div className="absolute top-[20%] left-[15%] w-4 h-4 rounded-full bg-brand-400/30" />
      <div className="absolute top-[40%] right-[25%] w-3 h-3 rounded-full bg-pastel-mint/60" />
      <div className="absolute bottom-[30%] left-[60%] w-5 h-5 rounded-full bg-pastel-lilac/40" />
      <div className="absolute top-[60%] left-[30%] w-3 h-3 rounded-full bg-pastel-peach/50" />
      <div className="absolute top-[15%] right-[40%] w-4 h-4 rounded-full bg-pastel-sky/50" />
    </div>
  )
}
