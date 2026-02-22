type Props = {
  color?: string
  size?: number
  className?: string
}

export function BlobShape({
  color = 'bg-pastel-lilac/30',
  size = 300,
  className = '',
}: Props) {
  return (
    <div
      className={`absolute animate-blob blur-3xl pointer-events-none ${color} ${className}`}
      style={{
        width: size,
        height: size,
      }}
    />
  )
}
