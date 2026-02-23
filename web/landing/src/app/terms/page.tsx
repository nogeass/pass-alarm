import { ja } from '@/i18n/ja'

export default function TermsPage() {
  return (
    <div className="max-w-3xl mx-auto px-4 py-32">
      <h1 className="heading-lg mb-8">{ja.termsPage.title}</h1>

      <div className="prose prose-gray max-w-none text-text space-y-6 text-sm leading-relaxed">
        <p>
          本利用規約（以下「本規約」）は、Nogeass Inc.（以下「当社」）が提供するパスアラーム（以下「本アプリ」）の利用条件を定めるものです。利用者は、本アプリをダウンロードまたは利用した時点で、本規約に同意したものとみなします。
        </p>

        <section>
          <h2 className="text-lg font-bold mt-8 mb-3">第1条（定義）</h2>
          <ul className="list-disc pl-6 space-y-1 mt-2">
            <li>「本アプリ」とは、当社が提供するアラームアプリケーション「パスアラーム」をいいます。</li>
            <li>「利用者」とは、本アプリをダウンロードまたは利用するすべての方をいいます。</li>
            <li>「Pro機能」とは、本アプリのアプリ内課金により利用可能となる追加機能をいいます。</li>
          </ul>
        </section>

        <section>
          <h2 className="text-lg font-bold mt-8 mb-3">第2条（本アプリの内容）</h2>
          <p>
            本アプリは、アラーム（目覚まし時計）機能を提供します。主な機能として、アラームの設定・スキップ機能・祝日スキップ・アラームキュー表示等があります。
          </p>
        </section>

        <section>
          <h2 className="text-lg font-bold mt-8 mb-3">第3条（利用条件）</h2>
          <ol className="list-decimal pl-6 space-y-2 mt-2">
            <li>本アプリの利用にあたり、利用者は端末の通知許可を有効にする必要があります。通知が無効の場合、アラームは正常に動作しません。</li>
            <li>本アプリのアラーム機能は、端末のOS・設定・状態（おやすみモード、集中モード、バッテリーセーバー、Do Not Disturb等）に依存します。これらの設定により、アラームが鳴動しない場合があります。</li>
            <li>当社は、アラームの確実な鳴動を保証するものではありません。重要な予定がある場合は、複数の手段を併用することを推奨します。</li>
          </ol>
        </section>

        <section>
          <h2 className="text-lg font-bold mt-8 mb-3">第4条（アプリ内課金）</h2>
          <ol className="list-decimal pl-6 space-y-2 mt-2">
            <li>Pro機能は、Apple App StoreまたはGoogle Playを通じたアプリ内課金により利用可能となります。</li>
            <li>課金の処理は、各プラットフォーム（Apple / Google）の決済基盤を通じて行われます。決済に関する規約は、各プラットフォームの規約に従います。</li>
            <li>購入後の返金は、各プラットフォームのポリシーに準じます。当社が直接返金処理を行うことはありません。</li>
            <li>サブスクリプション形式の課金がある場合、利用者が解約手続きを行わない限り自動的に更新されます。解約方法は各プラットフォームの設定画面をご確認ください。</li>
          </ol>
        </section>

        <section>
          <h2 className="text-lg font-bold mt-8 mb-3">第5条（知的財産権）</h2>
          <ol className="list-decimal pl-6 space-y-2 mt-2">
            <li>本アプリに関する著作権、商標権その他一切の知的財産権は、当社または正当な権利を有する第三者に帰属します。</li>
            <li>「Pass Alarm」「パスアラーム」の名称・ロゴ・アイコンは当社の商標です。</li>
            <li>本アプリのソースコードはApache License 2.0に基づきオープンソースとして公開されていますが、上記商標の使用は許諾されません。ソースコードをフォークする場合は、異なるブランド名・アイコンをご使用ください。</li>
          </ol>
        </section>

        <section>
          <h2 className="text-lg font-bold mt-8 mb-3">第6条（禁止事項）</h2>
          <p>利用者は、以下の行為を行ってはなりません。</p>
          <ul className="list-disc pl-6 space-y-1 mt-2">
            <li>本アプリの不正利用、逆コンパイル、リバースエンジニアリング（オープンソースライセンスに基づく範囲を除く）</li>
            <li>当社または第三者の権利を侵害する行為</li>
            <li>本アプリを利用した違法行為または公序良俗に反する行為</li>
            <li>当社のサーバーやネットワークに過度な負荷をかける行為</li>
            <li>その他、当社が不適切と判断する行為</li>
          </ul>
        </section>

        <section>
          <h2 className="text-lg font-bold mt-8 mb-3">第7条（免責事項）</h2>
          <ol className="list-decimal pl-6 space-y-2 mt-2">
            <li>本アプリは「現状有姿（AS IS）」で提供されます。当社は、本アプリの完全性・正確性・信頼性・特定の目的への適合性について、明示または黙示を問わず一切の保証をしません。</li>
            <li>当社は、以下に起因する損害について一切の責任を負いません。
              <ul className="list-disc pl-6 space-y-1 mt-1">
                <li>アラームの不鳴動（OS設定・端末状態・ネットワーク環境等に起因するものを含む）</li>
                <li>本アプリの利用に起因する遅刻・寝坊その他の損害</li>
                <li>端末の故障・紛失・データの消失</li>
                <li>第三者サービス（Apple、Google等）の障害</li>
                <li>天災地変・停電その他の不可抗力</li>
              </ul>
            </li>
            <li>当社の責任が認められる場合においても、当社の損害賠償額は、利用者が本アプリに支払った直近12か月分の利用料金を上限とします。</li>
          </ol>
        </section>

        <section>
          <h2 className="text-lg font-bold mt-8 mb-3">第8条（サービスの変更・中断・終了）</h2>
          <ol className="list-decimal pl-6 space-y-2 mt-2">
            <li>当社は、利用者への事前通知なく、本アプリの内容を変更し、または本アプリの提供を中断・終了することがあります。</li>
            <li>当社は、前項の変更・中断・終了に起因する損害について一切の責任を負いません。</li>
          </ol>
        </section>

        <section>
          <h2 className="text-lg font-bold mt-8 mb-3">第9条（利用者データ）</h2>
          <p>
            利用者のアラームデータは端末内にのみ保存されます。アプリのアンインストールや端末の初期化により、データが失われる場合があります。当社はデータのバックアップ義務を負いません。
          </p>
        </section>

        <section>
          <h2 className="text-lg font-bold mt-8 mb-3">第10条（本規約の変更）</h2>
          <p>
            当社は、必要に応じて本規約を変更することがあります。変更後の本規約は、本ページに掲載した時点から効力を生じるものとします。重要な変更がある場合は、アプリ内通知またはウェブサイト上でお知らせします。変更後も本アプリの利用を継続する場合、利用者は変更後の規約に同意したものとみなします。
          </p>
        </section>

        <section>
          <h2 className="text-lg font-bold mt-8 mb-3">第11条（準拠法・管轄裁判所）</h2>
          <ol className="list-decimal pl-6 space-y-2 mt-2">
            <li>本規約は日本法に準拠し、日本法に従って解釈されるものとします。</li>
            <li>本規約に関連する一切の紛争については、東京地方裁判所を第一審の専属的合意管轄裁判所とします。</li>
          </ol>
        </section>

        <section>
          <h2 className="text-lg font-bold mt-8 mb-3">第12条（お問い合わせ）</h2>
          <p>本規約に関するお問い合わせは、以下のGitHub Issueよりお願いします。</p>
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
