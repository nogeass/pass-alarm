import { ja } from '@/i18n/ja'

export default function TermsPage() {
  return (
    <div className="max-w-3xl mx-auto px-4 py-32">
      <h1 className="heading-lg mb-8">{ja.termsPage.title}</h1>
      <div className="body-text whitespace-pre-line">{ja.termsPage.body}</div>
    </div>
  )
}
