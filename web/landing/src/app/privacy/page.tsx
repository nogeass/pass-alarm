import { ja } from '@/i18n/ja'

export default function PrivacyPage() {
  return (
    <div className="max-w-3xl mx-auto px-4 py-32">
      <h1 className="heading-lg mb-8">{ja.privacyPage.title}</h1>

      <div className="prose prose-gray max-w-none text-text space-y-6 text-sm leading-relaxed">
        <p>
          Nogeass Inc.（以下「当社」）は、パスアラーム（以下「本アプリ」）における利用者の個人情報およびプライバシーの保護に関し、以下のとおりプライバシーポリシー（以下「本ポリシー」）を定めます。
        </p>

        <section>
          <h2 className="text-lg font-bold mt-8 mb-3">1. 収集する情報</h2>
          <p>本アプリは、以下の情報を収集する場合があります。</p>

          <h3 className="text-base font-bold mt-4 mb-2">1-1. アラームデータ</h3>
          <p>
            利用者が設定したアラームの時刻・繰り返し設定・スキップ状態などのデータは、利用者の端末内にのみ保存されます。当社のサーバーに送信されることはありません。
          </p>

          <h3 className="text-base font-bold mt-4 mb-2">1-2. 利用状況データ（アナリティクス）</h3>
          <p>
            本アプリでは、サービス改善を目的として Google Analytics（Firebase向けGoogleアナリティクス）を使用し、以下の情報を匿名で収集する場合があります。
          </p>
          <ul className="list-disc pl-6 space-y-1 mt-2">
            <li>アプリの起動回数・利用時間</li>
            <li>画面遷移・機能の利用状況</li>
            <li>端末の種類・OSバージョン・言語設定</li>
            <li>クラッシュログ</li>
          </ul>
          <p className="mt-2">
            これらの情報は統計的に処理され、個人を特定することはできません。Googleによるデータの取り扱いについては、
            <a href="https://policies.google.com/privacy" target="_blank" rel="noopener noreferrer" className="text-brand-500 underline">Googleプライバシーポリシー</a>
            をご確認ください。
          </p>

          <h3 className="text-base font-bold mt-4 mb-2">1-3. 課金情報</h3>
          <p>
            本アプリのPro機能（アラーム上限解放等）はアプリ内課金により提供されます。課金処理はApple（App Store）またはGoogle（Google Play）の決済基盤を通じて行われ、当社がクレジットカード番号等の決済情報を直接取得・保持することはありません。
          </p>
          <p className="mt-2">
            当社は、課金の有無・購入した商品の種別・購入日時等のトランザクション情報を、サービス提供およびサポート対応の目的で参照する場合があります。
          </p>

          <h3 className="text-base font-bold mt-4 mb-2">1-4. 広告識別子</h3>
          <p>
            本アプリは、現時点において広告識別子（IDFA / GAID）を収集・利用しておりません。将来的に広告機能を導入する場合は、本ポリシーを更新のうえ事前に通知します。
          </p>
        </section>

        <section>
          <h2 className="text-lg font-bold mt-8 mb-3">2. 情報の利用目的</h2>
          <p>収集した情報は、以下の目的のために利用します。</p>
          <ul className="list-disc pl-6 space-y-1 mt-2">
            <li>本アプリの提供・運営・改善</li>
            <li>不具合の検出・修正</li>
            <li>利用状況の分析およびサービス品質の向上</li>
            <li>課金状態の管理およびサポート対応</li>
            <li>法令に基づく対応</li>
          </ul>
        </section>

        <section>
          <h2 className="text-lg font-bold mt-8 mb-3">3. 第三者への提供</h2>
          <p>
            当社は、以下の場合を除き、利用者の個人情報を第三者に提供することはありません。
          </p>
          <ul className="list-disc pl-6 space-y-1 mt-2">
            <li>利用者の同意がある場合</li>
            <li>法令に基づく場合</li>
            <li>人の生命・身体・財産の保護に必要な場合</li>
            <li>前項に定める外部サービス（Google Analytics、Apple/Googleの決済基盤）への情報送信</li>
          </ul>
        </section>

        <section>
          <h2 className="text-lg font-bold mt-8 mb-3">4. データの保管・管理</h2>
          <p>
            アラームデータは利用者の端末内にのみ保存されます。アプリをアンインストールすることで、端末内のデータは削除されます。
          </p>
          <p className="mt-2">
            アナリティクスデータはGoogleのサーバーに保管され、Googleのデータ保持ポリシーに従います。
          </p>
        </section>

        <section>
          <h2 className="text-lg font-bold mt-8 mb-3">5. 安全管理措置</h2>
          <p>
            当社は、収集した情報の漏洩・滅失・毀損を防止するため、合理的な安全管理措置を講じます。
          </p>
        </section>

        <section>
          <h2 className="text-lg font-bold mt-8 mb-3">6. お子様のプライバシー</h2>
          <p>
            本アプリは13歳未満の方を対象としたサービスではありません。13歳未満の方から意図的に個人情報を収集することはありません。万が一、13歳未満の方の個人情報が収集されたことが判明した場合は、速やかに削除します。
          </p>
        </section>

        <section>
          <h2 className="text-lg font-bold mt-8 mb-3">7. 利用者の権利</h2>
          <p>
            利用者は、当社に対して自己の個人情報の開示・訂正・削除を請求することができます。ご希望の場合は、下記のお問い合わせ先までご連絡ください。
          </p>
        </section>

        <section>
          <h2 className="text-lg font-bold mt-8 mb-3">8. 本ポリシーの変更</h2>
          <p>
            当社は、法令の改正やサービス内容の変更に伴い、本ポリシーを変更することがあります。重要な変更がある場合は、アプリ内通知またはウェブサイト上で事前にお知らせします。変更後の本ポリシーは、本ページに掲載した時点から効力を生じるものとします。
          </p>
        </section>

        <section>
          <h2 className="text-lg font-bold mt-8 mb-3">9. お問い合わせ</h2>
          <p>本ポリシーに関するお問い合わせは、以下のGitHub Issueよりお願いします。</p>
          <p className="mt-2">
            <a href="https://github.com/nogeass/pass-alarm/issues" target="_blank" rel="noopener noreferrer" className="text-brand-500 underline">GitHub Issues</a>
          </p>
        </section>

        <p className="mt-8 text-text-muted">制定日: 2025年1月1日</p>
        <p className="text-text-muted">最終更新日: 2026年2月23日</p>
      </div>
    </div>
  )
}
