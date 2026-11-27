package com.sulmancodes.smarthometv;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public class CameraScreensaverActivity extends Activity {
    private PlayerView view; private ExoPlayer player;
    @Override protected void onCreate(@Nullable Bundle b){super.onCreate(b);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        String url=CameraConfig.cameraUrl(this); if(url.isEmpty()){finish();return;}
        FrameLayout root=new FrameLayout(this);root.setBackgroundColor(0xFF000000);
        view=new PlayerView(this);root.addView(view,new FrameLayout.LayoutParams(-1,-1));
        TextView name=new TextView(this);name.setText(CameraConfig.cameraName(this)+"  ·  LIVE");name.setTextColor(0xCCFFFFFF);name.setTextSize(14);name.setPadding(24,18,24,18);
        FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(-2,-2,android.view.Gravity.BOTTOM|android.view.Gravity.START);root.addView(name,p);setContentView(root);
        player=CameraPlayback.start(this,view,url,null);
    }
    @Override public boolean dispatchKeyEvent(KeyEvent e){if(e.getAction()==KeyEvent.ACTION_DOWN){finish();return true;}return super.dispatchKeyEvent(e);}
    @Override protected void onDestroy(){CameraPlayback.stop(view,player);super.onDestroy();}
}
