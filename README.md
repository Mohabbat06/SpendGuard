SpendGuard
==========

Personal budget tracker for the **phone owner**. When you pay with a wallet or bank app, SpendGuard reads **your** payment notification and adds the amount. You set a monthly limit; the app alarms the first time you go over that month.

It does not control another person’s phone. Capture only works on the device where the owner installed it and turned on notification access.

What’s in this repo
-------------------

This is the Play Store Android app (`com.spendguard.app`). The laptop dashboard is a separate project and is not in this folder yet.

How it captures amounts
-----------------------

- Uses Android **Notification Listener** (not SMS read). That is the path Play Store will actually review.
- Parses debit / paid / spent messages for amounts in BDT, INR, USD, and similar.
- Skips OTPs and incoming credits.
- You can still add or delete an expense by hand.

Setup
-----

1. Install [Android Studio](https://developer.android.com/studio) (this Mac does not have a JDK/SDK yet).
2. Open this folder in Android Studio and let Gradle sync.
3. Run on a phone or emulator.

Optional Firebase (cloud backup of the **same** owner account)
--------------------------------------------------------------

1. Create a Firebase project and add an Android app with package `com.spendguard.app`.
2. Enable Email/Password auth and Cloud Firestore.
3. Download `google-services.json` into `app/google-services.json`.
4. Rebuild. Sign in from Settings.

Until that file is present, the app still works fully on the phone (Room database).

Play Store notes
----------------

- Declare notification access in the Data safety / permissions form: used only to read payment alerts the owner already sees.
- You will need a privacy policy URL before publishing.
- Do not request `READ_SMS`; Google rejects most apps that do.

Monthly alarm
-------------

Set a monthly limit in Settings. When captured + manual spend first exceeds that limit in the current calendar month, SpendGuard posts a high-priority notification.
