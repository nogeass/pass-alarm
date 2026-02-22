import type { ReactNode } from 'react'

type Props = {
  children: ReactNode
  variant?: 'default' | 'danger' | 'success'
  className?: string
}

const variantClass = {
  default: 'glass-card',
  danger: 'glass-card-danger',
  success: 'glass-card-success',
}

export function Card({ children, variant = 'default', className = '' }: Props) {
  return (
    <div className={`${variantClass[variant]} p-6 md:p-8 ${className}`}>
      {children}
    </div>
  )
}
