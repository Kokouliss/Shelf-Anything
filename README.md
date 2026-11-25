# Shelf Anything

One app for every collection: books, comics, manga and audiobooks; movies, series and anime; CDs, vinyl and cassettes; video games and board games. Each is a **shelf**, and the shelves are grouped by what you do with them: Read, Watch, Listen, Play.

- **Pick your shelves** at the first start, and change them later in Settings. A shelf you switch off keeps its items.
- **Add** by searching a title or scanning the barcode on the back. Every detail the site knows is kept and shown on the item page. Anything can also be typed in by hand.
- **Three flags on every item**, each on its own: Own, Want, and Read / Watched / Listened / Played. A book can be owned and unread, or read and not owned.
- **Physical or digital** on everything but board games, as a filter on each shelf. Movies on disc also record DVD, Blu-ray or 4K.
- **Series and anime** track where you watch them (Netflix, Disney+, a disc) and the season and episode you are at. Movies, series and anime count rewatches.
- **Notes and comments** on any item. **Share** one item as text, the whole collection as text, or **export** everything as a CSV file for a spreadsheet.
- Sign-in with Google, or with an email and password. The collection is stored in the cloud and works offline. The app is in English and Greek.
- **Delete account** in Settings removes everything and the sign-in. The Play Store requires this of apps with accounts.

The app is free, with no ads, for its first version on the Play Store.

## Set up Firebase (once, about ten minutes)

The app stores everything in a Firebase project that you own. The free Spark plan is enough and no payment card is needed.

1. Open the [Firebase console](https://console.firebase.google.com) and create a project, for example "Shelf Anything". Google Analytics can stay off.
2. Go to **Build > Authentication > Sign-in method** and enable **Google**. Pick a support email when asked. Then press **Add new provider** and enable **Email/Password** (leave "Email link" off).
3. Go to **Build > Firestore Database** and create a database in **production mode**. Choose a location close to you, for example `eur3 (Europe)`. The location cannot be changed later.
4. In Firestore, open the **Rules** tab, replace the text with the contents of [`firebase/firestore.rules`](firebase/firestore.rules), and press **Publish**. Without this step the app cannot read or write anything. Repeat it whenever `firebase/firestore.rules` changes.
5. Go to **Project settings > Your apps**, add an **Android app**, and enter:
   - Package name: `com.kokouliss.shelfanything`
   - SHA-1: `5E:BF:9C:15:BD:EF:A2:D2:88:F0:B2:39:01:8C:A5:5A:48:5E:2A:E8`. The console calls this field optional, but Google sign-in does not work without it. To add it later, open **Project settings > Your apps**, pick the Android app and press **Add fingerprint**.
6. Download `google-services.json` and put it in the `app` folder, so the file is `app/google-services.json`. Do this after step 2, because the file must contain the Google sign-in client id. The file is ignored by git on purpose.
7. For the GitHub builds, add the same file as a repository secret named `GOOGLE_SERVICES_JSON` (its whole text) under **Settings → Secrets and variables → Actions**.

The SHA-1 belongs to the debug key in `app/debug.keystore`, which is committed so that every machine builds an app Google sign-in accepts. A release build signed with another key needs that key's SHA-1 added in step 5 as well.

If you choose a Google account and land back on the sign-in screen, the SHA-1 is missing or belongs to another key. Add it as described in step 5 and try again after a few minutes. The app does not need to be rebuilt.

## Metadata sites

Each shelf searches one site. Most need nothing; three need a free key. A shelf without its key says so in the app, and items can still be added by hand.

| Shelf | Site | Key |
|---|---|---|
| Books, comics, manga, audiobooks | [Open Library](https://openlibrary.org/developers/api) | none |
| Movies | [TMDB](https://www.themoviedb.org/settings/api) | `TMDB_API_KEY` (free, instant) |
| Series | [TVmaze](https://www.tvmaze.com/api) | none |
| Anime | [Jikan](https://jikan.moe) (MyAnimeList) | none |
| CDs, vinyl, cassettes | [MusicBrainz](https://musicbrainz.org/doc/MusicBrainz_API) + Cover Art Archive | none |
| Video games | [RAWG](https://rawg.io/apidocs) | `RAWG_API_KEY` (free, instant) |
| Board games | [BoardGameGeek](https://boardgamegeek.com/wiki/page/BGG_XML_API2) | `BGG_API_TOKEN` (free, [register the app](https://boardgamegeek.com/applications), approval can take days) |

Barcodes: books use the ISBN on Open Library and music uses MusicBrainz. Movies, video games and board games have no barcode search on their sites, so the app asks [UPCitemdb](https://www.upcitemdb.com/wp/docs/main/development/getting-started/)'s free trial endpoint (about a hundred lookups a day per phone) for the product title and searches that.

Put the keys in `local.properties` at the root of the project (ignored by git), one per line, such as `TMDB_API_KEY=...`, or in GitHub as repository secrets with the same names. Attribution the sites ask for: TMDB wants "This product uses the TMDB API but is not endorsed or certified by TMDB" and its logo on any public listing; BoardGameGeek wants a "Powered by BGG" notice. Both belong on the Play Store page and in an About screen before the store release.

## Download

Every merge to `main` builds the app and puts it on a release page. The newest build is always at <https://github.com/Kokouliss/Shelf-Anything/releases/latest/download/shelf-anything.apk>: open the link on the phone, install, and allow installs from the browser when Android asks. The same link updates the app in place. The build number on the release page is also the app's version, so a crash report in the Firebase console (Crashlytics) names the build it came from.

## Signing key (once, before the Play Store)

Until the steps below are done, downloads are signed with the shared debug key from `app/debug.keystore`, which anyone can read. That is fine for trying the app out. For a store listing, and so that nobody else can build an "update" that installs over the app, make a key of your own:

1. Make the key, on any computer with a JDK (Android Studio brings one in its `jbr/bin` folder). Pick two passwords and keep them:
   ```
   keytool -genkeypair -v -keystore shelf-anything.keystore -alias shelf-anything -keyalg RSA -keysize 2048 -validity 10000
   ```
2. In the GitHub repository go to **Settings → Secrets and variables → Actions** and add four repository secrets: `RELEASE_KEYSTORE_BASE64` (the file as text: `base64 -w0 shelf-anything.keystore` on Linux, `base64 -i shelf-anything.keystore` on a Mac), `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS` (`shelf-anything`) and `RELEASE_KEY_PASSWORD`. From the next build on, the release page carries the optimized release build signed with this key, and `shelf-anything.aab`, the file you upload to Google Play.
3. Print the key's fingerprints with `keytool -list -v -keystore shelf-anything.keystore -alias shelf-anything`. Add the **SHA-1** in the Firebase console under Project settings → Your apps → Add fingerprint, or Google sign-in refuses the new build.
4. An app signed with a new key does not install over the old one. Everyone removes the app once and installs the new download.

## Build it yourself

Android Studio opens the project as it is. On the command line, `./gradlew :app:assembleDebug` builds `app/build/outputs/apk/debug/app-debug.apk`, and `./gradlew test` runs the unit tests (palette contrast, CSV export and the parsers for each metadata site).

The code is Kotlin with Jetpack Compose, Hilt and Navigation 3, in modules: `core/domain` (models, repository interfaces, the palette), `core/data` (Firebase and the metadata sites), `core/ui` (theme and shared components), `feature/auth` (sign-in and Settings), `feature/shelves` (the shelves, items, search and scanning) and `app`. The headings use [Manrope](https://github.com/sharanda/manrope) (SIL Open Font License, in `app/licenses`).
