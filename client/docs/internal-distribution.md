# Internal distribution

Mapmory's internal distribution is a separate installed app, not an internal
track of the production app. This separation is intentional: a build installed
from an internal track under the production package still reports Analytics as
the production app.

| Platform | Production ID | Internal ID |
| --- | --- | --- |
| Android | `com.mapmory.android` | `com.mapmory.android.internal` |
| iOS | `com.mapmory.ios` | `com.mapmory.ios.internal` |

The internal apps use `https://dev-api.map-mory.com/api/v1` and show the name
`Mapmory Internal`. They can be installed alongside the production apps.

## Analytics guarantee

Internal builds have several independent safeguards:

- Firebase Analytics is permanently deactivated in the packaged manifest or
  Info.plist.
- The Android Firebase logger refuses to initialize for a package ending in
  `.internal`.
- Android removes Firebase's automatic initialization provider from the merged
  internal manifest.
- Android does not run the Google Services plugin for the internal build, so a
  production `google_app_id` cannot be packaged accidentally.
- iOS does not call `FirebaseApp.configure()` for the `INTERNAL` compilation
  condition, and excludes `GoogleService-Info.plist` from the app bundle.

These safeguards are compiled into the binary. Deleting and reinstalling the
internal app therefore cannot turn collection back on. The production apps are
unchanged and still collect Analytics.

## Build

Android App Bundle:

```sh
sh ./scripts/build-internal-android.sh
```

The generated bundle is
`androidApp/build/outputs/bundle/internal/androidApp-internal.aab`. The script
uses the dedicated `.signing/mapmory-internal-upload.jks` key and reads its
password from the macOS Keychain service
`com.mapmory.android.internal.upload-key`. Back up the keystore separately;
never commit it.

iOS:

1. Select the shared `Mapmory-Internal` scheme in Xcode.
2. Choose **Any iOS Device (arm64)**.
3. Archive and distribute only to the App Store Connect app whose bundle ID is
   `com.mapmory.ios.internal`.

## Store setup

- Create a separate Play Console app for `com.mapmory.android.internal` and use
  only an internal testing track. Do not add this package to Firebase.
- Create a separate App Store Connect app for `com.mapmory.ios.internal` and
  use TestFlight internal testing. Do not register this bundle ID as a Firebase
  app.
- Never upload an internal artifact to a production Mapmory store record. The
  differing application IDs normally make the stores reject this mistake.

Adding a tester is an access-control change. Reuse the team's existing tester
group where possible and review membership before granting access.
