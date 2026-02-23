import type { ReactNode } from 'react'

type Props = {
  children?: ReactNode
  className?: string
}

export function DeviceFrame({ children, className = '' }: Props) {
  return (
    <div
      className={`
        relative w-[240px] md:w-[280px]
        bg-gray-900 rounded-[2.5rem] p-3
        shadow-2xl shadow-black/15
        ${className}
      `}
      style={{ aspectRatio: '9 / 19.5' }}
    >
      {/* Dynamic Island */}
      <div className="absolute top-[10px] left-1/2 -translate-x-1/2 w-[72px] h-[22px] bg-black rounded-full z-10" />

      {/* Screen */}
      <div className="w-full h-full bg-gradient-to-b from-surface to-surface-muted rounded-[2rem] overflow-hidden flex items-center justify-center">
        {children || (
          <div className="text-center text-text-subtle text-sm">
            <span className="text-4xl block mb-2">⏰</span>
            Coming Soon
          </div>
        )}
      </div>
    </div>
  )
}
