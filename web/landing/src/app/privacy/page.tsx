import { ja } from '@/i18n/ja'

export default function PrivacyPage() {
  return (
    <div className="max-w-3xl mx-auto px-4 py-32">
      <h1 className="heading-lg mb-8">{ja.privacyPage.title}</h1>
      <div className="body-text whitespace-pre-line">{ja.privacyPage.body}</div>
    </div>
  )
}
