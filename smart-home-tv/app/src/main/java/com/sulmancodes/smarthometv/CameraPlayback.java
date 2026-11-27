package com.sulmancodes.smarthometv;

import android.content.Context;
import android.net.Uri;

import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.rtsp.RtspMediaSource;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

/** Muted, low-buffer camera playback. The caller owns the player and must release it with the view lifecycle. */
@UnstableApi
final class CameraPlayback {
    private CameraPlayback() { }

    static ExoPlayer start(Context context, PlayerView view, String streamUrl, Player.Listener listener) {
        DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(1000, 3000, 500, 500)
                .build();
        ExoPlayer player = new ExoPlayer.Builder(context).setLoadControl(loadControl).build();
        player.setVolume(0f);
        player.setRepeatMode(Player.REPEAT_MODE_ONE);
        view.setUseController(false);
        view.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        view.setPlayer(player);
        if (listener != null) player.addListener(listener);

        MediaItem item = MediaItem.fromUri(Uri.parse(streamUrl));
        if (streamUrl.regionMatches(true, 0, "rtsp://", 0, 7)) {
            player.setMediaSource(new RtspMediaSource.Factory()
                    .setForceUseRtpTcp(true)
                    .createMediaSource(item));
        } else {
            // The Media3 HLS module handles .m3u8 camera URLs through the default media-source factory.
            player.setMediaItem(item);
        }
        player.prepare();
        player.play();
        return player;
    }

    static void stop(PlayerView view, ExoPlayer player) {
        if (view != null) view.setPlayer(null);
        if (player != null) player.release();
    }
}
