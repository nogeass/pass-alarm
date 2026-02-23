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
        rounded-[1.8rem] overflow-hidden
        ring-[3px] ring-gray-800
        shadow-xl shadow-black/20
        ${className}
      `}
    >
      {/* Dynamic Island */}
      <div className="absolute top-[6px] left-1/2 -translate-x-1/2 w-[50px] h-[14px] bg-black rounded-full z-10" />

      {/* Screen */}
      <div className="w-full">
        {children || (
          <div className="aspect-[9/19.5] bg-gradient-to-b from-surface to-surface-muted flex flex-col items-center justify-center text-center text-text-subtle text-sm">
            <span className="text-4xl block mb-2">⏰</span>
            Coming Soon
          </div>
        )}
      </div>
    </div>
  )
}
