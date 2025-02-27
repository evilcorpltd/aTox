# aTox

[![IRC](https://img.shields.io/badge/libera-%23atox-brightgreen.svg)][libera]
[![License](https://img.shields.io/github/license/evilcorpltd/aTox)][license]
[![Last release](https://img.shields.io/github/v/release/evilcorpltd/aTox)][releases]
[![Translation status](https://hosted.weblate.org/widgets/atox/-/app/svg-badge.svg)][weblate]

[<img src="img/get-it-on-fdroid.png" alt="Get aTox on F-Droid" height="80">][fdroid]

The reasonable Tox client for Android.

<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.jpg" alt="chat screen" height="600"> <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" alt="contact screen" height="600">

## Building

### Clone!

`git clone https://github.com/evilcorpltd/aTox.git`

### Build tox4j!

Until [#730][publish-tox4j] is closed, you'll have to build and publish tox4j to a local repository:

```sh
./scripts/build-host -j$(nproc)
./scripts/build-aarch64-linux-android -j$(nproc) release
./scripts/build-arm-linux-androideabi -j$(nproc) release
./scripts/build-i686-linux-android -j$(nproc) release
./scripts/build-x86_64-linux-android -j$(nproc) release
```

### Build aTox!

`gradlew build`

## Translation

Want to see aTox in your language? Contribute a translation on [Weblate!][weblate]

[![Translation status](https://hosted.weblate.org/widgets/atox/-/app/multi-auto.svg)][weblate]

[publish-tox4j]: https://github.com/evilcorpltd/aTox/issues/730
[fdroid]: https://f-droid.org/packages/ltd.evilcorp.atox
[gplay]: https://play.google.com/store/apps/details?id=ltd.evilcorp.atox
[libera]: https://kiwiirc.com/nextclient/irc.libera.chat/atox
[license]: https://github.com/evilcorpltd/aTox/blob/master/LICENSE
[releases]: https://github.com/evilcorpltd/aTox/releases
[weblate]: https://hosted.weblate.org/engage/atox/
