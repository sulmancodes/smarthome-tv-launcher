# Smart Home TV implementation notes

## Current module structure

- `smart-home-tv/` is a standalone Android TV launcher project; it does not alter the CleanYourTV reference launcher.
- The home screen uses native Android views with D-pad focus and no heavy UI framework.
- RTSP uses Media3's RTSP source over RTP/TCP. HLS uses Media3's default source selection.
- Camera/API secrets are stored with AES-GCM and an Android Keystore key.
- Cricket data is fetched on a worker thread and cached locally. Live data refresh is configurable with a 15-minute minimum; schedule/results are cached for 24 hours.
- The Android DreamService can be selected from the device's system screensaver settings. The Home idle timer opens the camera saver only while Home is visible.

## Media Vision navigation

Select the installed app from Settings. If Media Vision has no published deep-link contract, shortcuts open its normal launch activity. The optional Live TV, Movies and Series route fields accept only documented URI routes supported by the installed Media Vision build; they are not guessed by the launcher.

## Camera prerequisites

Use an RTSP stream exposed by the camera/NVR (a go2rtc restream is suitable) or an HLS stream. Hardware decoding depends on the TV firmware, codec, profile, resolution and stream transport. Use H.264 baseline/main for older low-power TVs. The app mutes audio and uses small playback buffers.

## Cricket provider

The current adapter expects CricketData/CricAPI response envelopes for `currentMatches` and `matches`. Provider response fields and plan entitlements can change; keep the provider key private, verify API access and commercial-use terms before deployment, and do not commit a real key. A 15-minute default live refresh is a conservative cache policy, not a guarantee that a free plan permits every usage pattern.

## Device verification still required

Build and unit-test with JDK 17 and Android SDK 34. Then install on the target Android TV and test:
- Home / default launcher selection and return from apps.
- D-pad movement across every card and settings row.
- RTSP and HLS camera formats on the actual NVR and TV.
- DreamService selection, start, stop and recovery from stream loss.
- Media Vision app package selection and each deep-link route.
- Cricket provider key, errors, cached offline state and API usage.
- Idle timeout behavior after focus input and when an external app is foregrounded.

This project is Android TV / Android-based Fire TV only. It is not compatible with Tizen, webOS, Roku OS or VegaOS.
