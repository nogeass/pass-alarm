import { ja } from '@/i18n/ja'
import { EXTERNAL_LINKS } from '@/lib/constants'

export default function OssPage() {
  return (
    <div className="max-w-3xl mx-auto px-4 py-32">
      <h1 className="heading-lg mb-8">{ja.ossPage.title}</h1>
      <p className="body-text mb-6">{ja.ossPage.intro}</p>
      <a
        href={EXTERNAL_LINKS.github}
        target="_blank"
        rel="noopener noreferrer"
        className="inline-flex items-center gap-2 bg-brand-500 text-white font-bold px-6 py-3 rounded-2xl hover:bg-brand-600 transition mb-8"
      >
        {ja.ossPage.githubLabel}
        <span>→</span>
      </a>
      <div className="glass-card p-6">
        <p className="text-text-muted text-sm leading-relaxed">
          {ja.ossPage.brandNotice}
        </p>
      </div>
    </div>
  )
}
