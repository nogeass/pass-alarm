import { ja } from '@/i18n/ja'

export function t(key: string): string {
  const keys = key.split('.')
  let value: unknown = ja
  for (const k of keys) {
    if (value && typeof value === 'object' && k in value) {
      value = (value as Record<string, unknown>)[k]
    } else {
      return key
    }
  }
  return typeof value === 'string' ? value : key
}

export function tArray<T = Record<string, string>>(key: string): T[] {
  const keys = key.split('.')
  let value: unknown = ja
  for (const k of keys) {
    if (value && typeof value === 'object' && k in value) {
      value = (value as Record<string, unknown>)[k]
    } else {
      return []
    }
  }
  return Array.isArray(value) ? (value as T[]) : []
}

export { ja }
