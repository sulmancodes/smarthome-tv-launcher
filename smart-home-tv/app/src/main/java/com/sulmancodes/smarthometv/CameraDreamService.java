package com.sulmancodes.smarthometv;

import android.service.dreams.DreamService;
import android.view.View;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

/** System screensaver entry point. A stream is opened only while the dream is visible. */
@UnstableApi
public class CameraDreamService extends DreamService {
    private PlayerView view; private ExoPlayer player;
    @Override public void onAttachedToWindow(){super.onAttachedToWindow();setInteractive(false);setFullscreen(true);setScreenBright(false);}
    @Override public void onDreamingStarted(){super.onDreamingStarted();String url=CameraConfig.cameraUrl(this);if(url.isEmpty())return;
        view=new PlayerView(this);view.setUseController(false);setContentView(view);player=CameraPlayback.start(this,view,url,null);}
    @Override public void onDreamingStopped(){CameraPlayback.stop(view,player);view=null;player=null;super.onDreamingStopped();}
    @Override public void onDetachedFromWindow(){CameraPlayback.stop(view,player);super.onDetachedFromWindow();}
}
