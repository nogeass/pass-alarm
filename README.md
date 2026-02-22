# Pass Alarm (パスアラーム)
**OFFにしない目覚まし。今日だけスキップ。**

## Structure

| Directory | Purpose |
|-----------|---------|
| `apps/ios` | iOS app (Swift / SwiftUI) |
| `apps/android` | Android app (Kotlin / Jetpack Compose) |
| `packages/docs` | Design docs |
| `packages/holidays` | Japanese holiday JSON data |
| `web/landing` | Landing page (Cloudflare Pages) |
| `fastlane/` | iOS & Android deployment |
| `.github/workflows/` | CI/CD pipelines |

## Development

```bash
# iOS
open apps/ios/PassAlarm.xcodeproj

# Android
cd apps/android && ./gradlew assembleDebug

# Landing page local dev
cd web/landing && npx wrangler pages dev .
```

## Open source, official brand protected
This codebase is open source under the [Apache License 2.0](LICENSE).
However, **"Pass Alarm / パスアラーム" name, logos, icons, and official distribution are trademarks of Nogeass Inc.**
See: [TRADEMARK.md](TRADEMARK.md)

## Official distribution
- Website: [pass-alarm.nogeass.com](https://pass-alarm.nogeass.com)
- Official builds are only those published by Nogeass Inc.

## Reliability note
Alarm behavior depends on OS/device settings.
We require notification permissions to use core features.

## Support
See [SUPPORT.md](SUPPORT.md) for support policy and bug reporting guidelines.

## Security
See [SECURITY.md](SECURITY.md) for reporting vulnerabilities.

## License
[Apache License 2.0](LICENSE) — see [TRADEMARK.md](TRADEMARK.md) for brand restrictions.
