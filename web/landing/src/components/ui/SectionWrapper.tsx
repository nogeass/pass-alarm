type Props = {
  id?: string
  children: React.ReactNode
  className?: string
  wide?: boolean
}

export function SectionWrapper({
  id,
  children,
  className = '',
  wide = false,
}: Props) {
  return (
    <section
      id={id}
      className={`section-padding ${wide ? '' : 'max-w-5xl mx-auto'} ${className}`}
    >
      {children}
    </section>
  )
}
