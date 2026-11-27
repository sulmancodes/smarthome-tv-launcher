package com.sulmancodes.smarthometv;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** TV-first home dashboard. All navigation is ordinary focusable Android views for reliable D-pad support. */
@UnstableApi
public class MainActivity extends Activity {
    private final Handler main = new Handler(Looper.getMainLooper());
    private final Runnable idle = () -> {
        if (CameraConfig.cameraEnabled(this) && CameraConfig.screensaverMinutes(this) > 0 && !CameraConfig.cameraUrl(this).isEmpty()) {
            startActivity(new Intent(this, CameraScreensaverActivity.class));
        }
    };
    private LinearLayout content;
    private TextView clock;
    private TextView cricketSummary;
    private PlayerView cameraPreview;
    private ExoPlayer cameraPlayer;
    private final Runnable tick = new Runnable() { @Override public void run() { refreshClock(); main.postDelayed(this, 30_000L); } };

    @Override protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setNavigationBarColor(Color.rgb(8,13,24));
        build();
        resetIdle();
    }

    @Override protected void onResume() {
        super.onResume();
        refreshClock();
        refreshCricket();
        startHomeCamera();
        main.removeCallbacks(tick); main.post(tick);
        resetIdle();
    }

    @Override protected void onPause() {
        main.removeCallbacksAndMessages(null);
        CameraPlayback.stop(cameraPreview,cameraPlayer); cameraPlayer=null;
        super.onPause();
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction()==KeyEvent.ACTION_DOWN) resetIdle();
        return super.dispatchKeyEvent(event);
    }

    private void resetIdle() {
        main.removeCallbacks(idle);
        int minutes=CameraConfig.screensaverMinutes(this);
        if (minutes>0) main.postDelayed(idle, minutes*60_000L);
    }

    private void build() {
        FrameLayout root=new FrameLayout(this);
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF101F35,0xFF0A1323,0xFF080D18}));
        content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(54),dp(30),dp(54),dp(26));
        root.addView(content,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout top=new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand=label("SMART HOME",13,0xFF69DCCB,Typeface.BOLD);
        top.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
        clock=label("",20,Color.WHITE,Typeface.BOLD);
        top.addView(clock);
        content.addView(top);
        TextView greeting=label("Your home, at a glance.",34,Color.WHITE,Typeface.BOLD);
        LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(-1,-2); gp.topMargin=dp(25); content.addView(greeting,gp);
        TextView subtitle=label("Everything you need, right from your TV.",16,0xFF9EB0C8,Typeface.NORMAL);
        content.addView(subtitle);

        LinearLayout row=new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,0,1);
        rp.topMargin=dp(24); rp.bottomMargin=dp(18); content.addView(row,rp);

        LinearLayout left=new LinearLayout(this); left.setOrientation(LinearLayout.VERTICAL);
        row.addView(left,new LinearLayout.LayoutParams(0,-1,1.05f));
        TextView cameraTitle=label("LIVE CAMERA",13,0xFF69DCCB,Typeface.BOLD);
        left.addView(cameraTitle);
        FrameLayout cameraBox=new FrameLayout(this);
        GradientDrawable cameraBg=new GradientDrawable(); cameraBg.setColor(0xFF18314A); cameraBg.setCornerRadius(dp(18)); cameraBox.setBackground(cameraBg); cameraBox.setClipToOutline(true);
        cameraBox.setFocusable(true); cameraBox.setClickable(true); cameraBox.setOnClickListener(v->openCamera());
        cameraPreview=new PlayerView(this); cameraPreview.setUseController(false); cameraPreview.setBackgroundColor(0xFF101D2D);
        cameraBox.addView(cameraPreview,new FrameLayout.LayoutParams(-1,-1));
        TextView cameraOverlay=label("CCTV  ·  OPEN FULL SCREEN",13,Color.WHITE,Typeface.BOLD);
        cameraOverlay.setBackgroundColor(0x99080D18); cameraOverlay.setPadding(dp(14),dp(10),dp(14),dp(10));
        FrameLayout.LayoutParams cop=new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM); cameraBox.addView(cameraOverlay,cop);
        cameraBox.setOnFocusChangeListener((v,focus)->{v.setScaleX(focus?1.02f:1f);v.setScaleY(focus?1.02f:1f);});
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,0,1); cp.topMargin=dp(10); left.addView(cameraBox,cp);
        TextView cameraHint=label(CameraConfig.cameraUrl(this).isEmpty()?"Add a camera stream in Settings":"Camera ready · muted preview",12,0xFF9EB0C8,Typeface.NORMAL);
        LinearLayout.LayoutParams ch=new LinearLayout.LayoutParams(-1,-2); ch.topMargin=dp(8); left.addView(cameraHint,ch);

        LinearLayout right=new LinearLayout(this); right.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rlp=new LinearLayout.LayoutParams(0,-1,1);
        rlp.leftMargin=dp(18); row.addView(right,rlp);
        TextView cricket=card("CRICKET","Live scores · Upcoming · Results",0xFF173C3A,()->startActivity(new Intent(this,CricketActivity.class)));
        right.addView(cricket,new LinearLayout.LayoutParams(-1,0,1));
        cricketSummary=label("Loading match information…",14,0xFFB8C9D8,Typeface.NORMAL);
        LinearLayout.LayoutParams csp=new LinearLayout.LayoutParams(-1,-2); csp.topMargin=dp(8); right.addView(cricketSummary,csp);
        TextView media=card("LIVE TV","Open Media Vision",0xFF28304D,()->openMedia("live"));
        LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(-1,0,1); mp.topMargin=dp(14); right.addView(media,mp);
        LinearLayout mediaRow=new LinearLayout(this);
        LinearLayout.LayoutParams mrp=new LinearLayout.LayoutParams(-1,dp(76)); mrp.topMargin=dp(10); right.addView(mediaRow,mrp);
        TextView movies=card("MOVIES","",0xFF24334A,()->openMedia("movies"));
        TextView series=card("SERIES","",0xFF24334A,()->openMedia("series"));
        LinearLayout.LayoutParams m1=new LinearLayout.LayoutParams(0,-1,1); mediaRow.addView(movies,m1);
        LinearLayout.LayoutParams m2=new LinearLayout.LayoutParams(0,-1,1); m2.leftMargin=dp(10); mediaRow.addView(series,m2);

        LinearLayout bottom=new LinearLayout(this);
        bottom.setGravity(Gravity.CENTER_VERTICAL);
        TextView settings=smallButton("SETTINGS",()->startActivity(new Intent(this,SettingsActivity.class)));
        TextView saver=smallButton("SCREENSAVER",()->startActivity(new Intent(this,CameraScreensaverActivity.class)));
        bottom.addView(settings); LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-2,-2); sp.leftMargin=dp(12); bottom.addView(saver,sp);
        TextView foot=label("Smart Home TV  ·  Home dashboard",12,0xFF647B97,Typeface.NORMAL);
        LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(0,-2,1); bottom.addView(foot,fp); foot.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        content.addView(bottom);
        setContentView(root);
        if (CameraConfig.cameraUrl(this).isEmpty()) cameraHint.setText("Configure RTSP or HLS in Settings");
        refreshClock();
    }

    private void startHomeCamera() {
        CameraPlayback.stop(cameraPreview,cameraPlayer); cameraPlayer=null;
        String url=CameraConfig.cameraEnabled(this)?CameraConfig.cameraUrl(this):"";
        if(cameraPreview!=null && !url.isEmpty()) {
            cameraPlayer=CameraPlayback.start(this,cameraPreview,url,null);
        }
    }
    private void refreshClock() {
        if(clock!=null) clock.setText(new SimpleDateFormat("EEE, d MMM  ·  h:mm a",Locale.getDefault()).format(new Date()));
    }
    private void refreshCricket() {
        if(cricketSummary==null || !CameraConfig.cricketEnabled(this)) return;
        CricketRepository.load(this,(live,upcoming,recent,error)->{
            if(cricketSummary==null) return;
            if(error!=null) cricketSummary.setText(error);
            else if(!live.isEmpty()) cricketSummary.setText(live.get(0).title+"\n"+live.get(0).status);
            else if(!upcoming.isEmpty()) cricketSummary.setText("Next: "+upcoming.get(0).title+"\n"+upcoming.get(0).status);
            else cricketSummary.setText("No match data yet · Open Cricket");
        });
    }
    private void openCamera() {
        if(!CameraConfig.cameraEnabled(this) || CameraConfig.cameraUrl(this).isEmpty()) startActivity(new Intent(this,SettingsActivity.class));
        else startActivity(new Intent(this,CameraScreensaverActivity.class).putExtra("preview",true));
    }
    private void openMedia(String route) {
        String pkg=CameraConfig.mediaVisionPackage(this);
        if(pkg.isEmpty()) { startActivity(new Intent(this,SettingsActivity.class)); return; }
        String uri=CameraConfig.route(this,"route_"+route);
        try {
            Intent i;
            if(!uri.isEmpty()) { i=new Intent(Intent.ACTION_VIEW,android.net.Uri.parse(uri)); i.setPackage(pkg); }
            else i=getPackageManager().getLaunchIntentForPackage(pkg);
            if(i==null) throw new IllegalStateException();
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i);
        } catch(Exception e) {
            android.widget.Toast.makeText(this,"Could not open Media Vision. Check the app selection and route.",android.widget.Toast.LENGTH_LONG).show();
        }
    }
    private TextView card(String title,String detail,int color,Runnable action) {
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER_VERTICAL);
        box.setPadding(dp(24),dp(18),dp(24),dp(18));
        GradientDrawable bg=new GradientDrawable(); bg.setColor(color); bg.setCornerRadius(dp(18)); bg.setStroke(dp(1),0x3358D8CB);
        box.setBackground(bg); box.setFocusable(true); box.setClickable(true);
        TextView t=label(title,24,Color.WHITE,Typeface.BOLD); box.addView(t);
        TextView d=label(detail,14,0xFFC1D1E2,Typeface.NORMAL); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.topMargin=dp(8); box.addView(d,p);
        box.setOnClickListener(v->action.run());
        box.setOnFocusChangeListener((v,focus)->{v.setScaleX(focus?1.025f:1f);v.setScaleY(focus?1.025f:1f);v.setAlpha(focus?1f:0.88f);});
        return box;
    }
    private TextView smallButton(String text,Runnable action) {
        TextView b=label(text,13,Color.WHITE,Typeface.BOLD); b.setGravity(Gravity.CENTER); b.setPadding(dp(18),dp(12),dp(18),dp(12));
        GradientDrawable bg=new GradientDrawable(); bg.setColor(0xFF1A2A40); bg.setCornerRadius(dp(30)); b.setBackground(bg); b.setFocusable(true); b.setOnClickListener(v->action.run()); return b;
    }
    private TextView label(String s,int size,int color,int style) {
        TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); t.setTypeface(Typeface.DEFAULT,style); t.setIncludeFontPadding(false); return t;
    }
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}
