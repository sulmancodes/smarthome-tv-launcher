# Smart Home TV

A standalone Android TV home launcher for a living-room dashboard. It is kept in its own Gradle project so the CleanYourTV reference launcher and its upstream constraints remain untouched.

## Features in this first implementation

- TV-first home screen with D-pad focus, animated tiles, a dock and a low-cost ambient background.
- Optional muted RTSP or HLS camera preview on Home.
- Camera full-screen idle screen and an Android DreamService screensaver.
- Cricket live scores plus upcoming and recent match lists through a configurable CricketData API key.
- Configurable Media Vision app shortcut, with optional deep-link URIs for Live TV, Movies and Series.
- On-TV settings for camera, cricket, clock and idle timeout.

## Build

Requires JDK 17 and Android SDK platform 34 / build-tools 34.0.0.

```bash
cd smart-home-tv
./gradlew --no-daemon testDebugUnitTest assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Camera setup

Enter an RTSP URL (for example a local NVR/go2rtc stream) or an HLS `.m3u8` URL in Settings. The URL is stored encrypted with an Android Keystore AES-GCM key. Preview audio is muted. Camera streaming starts only while Home or the screensaver is visible.

## Cricket data

The initial provider adapter uses CricketData/CricAPI:
- Live matches: `https://api.cricapi.com/v1/currentMatches`
- Match schedule/results: `https://api.cricapi.com/v1/matches`

Add your own API key in Settings. The default live refresh interval is 15 minutes; schedule/results are cached for 24 hours. This is deliberately conservative to stay close to the provider's 100-request/day free allowance. CricketData's terms say free access is for personal, non-commercial use; commercial deployments must use an appropriately paid plan. The app does not ship an API key.

## Media Vision

Choose the installed Media Vision app in Settings. The Live TV, Movies and Series shortcuts launch that app. If Media Vision exposes a supported deep-link URI, enter it in the matching route field; without a URI, the shortcut opens Media Vision's normal start screen. No package name is hard-coded.

## Screensaver

The launcher starts the camera screensaver after the configured idle period while Home is visible. The app also registers a DreamService; to use it while another app is open, select **CCTV Camera** in Android TV's system screensaver settings.

## Notes

- RTSP/HLS support depends on the camera/NVR stream format and the TV's available hardware/software decoders.
- The first version targets Android TV / Fire TV devices with Android API 26 or newer. It is not a Tizen, webOS, Roku or VegaOS app.
- No camera URL, API key, Media Vision package or user credentials are committed to this repository.
