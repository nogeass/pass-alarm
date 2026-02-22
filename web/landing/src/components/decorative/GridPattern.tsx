type Props = {
  className?: string
}

export function GridPattern({ className = '' }: Props) {
  return (
    <div
      className={`absolute inset-0 pointer-events-none ${className}`}
      style={{
        backgroundImage: `radial-gradient(circle, hsl(220 20% 80% / 0.15) 1px, transparent 1px)`,
        backgroundSize: '24px 24px',
      }}
    />
  )
}
