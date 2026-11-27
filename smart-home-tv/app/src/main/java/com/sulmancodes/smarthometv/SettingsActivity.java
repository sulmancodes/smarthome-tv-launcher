package com.sulmancodes.smarthometv;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/** Remote-friendly settings. Secrets are encrypted with Android Keystore before being persisted. */
public class SettingsActivity extends Activity {
    private LinearLayout rows;
    @Override protected void onCreate(Bundle b){super.onCreate(b);ScrollView scroll=new ScrollView(this);rows=new LinearLayout(this);rows.setOrientation(LinearLayout.VERTICAL);rows.setPadding(dp(48),dp(28),dp(48),dp(32));scroll.addView(rows);setContentView(scroll);build();}
    private void build(){
        rows.removeAllViews();heading("SMART HOME SETTINGS");
        heading("CAMERA");
        row("Camera preview",CameraConfig.cameraEnabled(this)?"Enabled":"Disabled",()->{
            CameraConfig.setCameraEnabled(this,!CameraConfig.cameraEnabled(this));build();
        });
        row("Camera name",CameraConfig.cameraName(this),()->edit("Camera name",CameraConfig.cameraName(this),InputType.TYPE_CLASS_TEXT,v->{CameraConfig.saveCameraName(this,v);build();}));
        row("RTSP / HLS stream URL",CameraConfig.cameraUrl(this).isEmpty()?"Not configured":"Configured · stored encrypted",()->edit("Camera stream URL",CameraConfig.cameraUrl(this),InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI,v->{CameraConfig.saveCameraUrl(this,v);build();}));
        heading("CRICKET");
        row("Cricket scores",CameraConfig.cricketEnabled(this)?"Enabled":"Disabled",()->{CameraConfig.setCricketEnabled(this,!CameraConfig.cricketEnabled(this));build();});
        row("CricketData API key",CameraConfig.cricketApiKey(this).isEmpty()?"Not configured":"Configured · stored encrypted",()->edit("CricketData API key",CameraConfig.cricketApiKey(this),InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD,v->{CameraConfig.saveCricketApiKey(this,v);build();}));
        row("Refresh interval",CameraConfig.cricketRefreshMinutes(this)+" minutes",()->new AlertDialog.Builder(this).setTitle("Refresh interval").setItems(new String[]{"15 minutes","30 minutes","60 minutes"},(d,w)->{CameraConfig.setCricketRefreshMinutes(this,new int[]{15,30,60}[w]);build();}).show());
        heading("MEDIA VISION");
        row("Select Media Vision app",CameraConfig.mediaVisionPackage(this).isEmpty()?"Choose installed app":CameraConfig.mediaVisionPackage(this),this::chooseMediaVision);
        route("Live TV route","route_live");route("Movies route","route_movies");route("Series route","route_series");
        heading("SCREENSAVER");
        row("Idle timeout",CameraConfig.screensaverMinutes(this)==0?"Off":CameraConfig.screensaverMinutes(this)+" minutes",()->new AlertDialog.Builder(this).setTitle("Idle timeout").setItems(new String[]{"Off","5 minutes","10 minutes","15 minutes","30 minutes","60 minutes"},(d,w)->{CameraConfig.setScreensaverMinutes(this,new int[]{0,5,10,15,30,60}[w]);build();}).show());
        row("Clock display",CameraConfig.clockMode(this),()->new AlertDialog.Builder(this).setTitle("Clock display").setItems(new String[]{"System","12-hour","24-hour"},(d,w)->{CameraConfig.setClockMode(this,new String[]{"system","12","24"}[w]);build();}).show());
        heading("ABOUT");
        row("Cricket data usage","Cached · 15-minute minimum refresh",()->new AlertDialog.Builder(this).setTitle("Cricket data").setMessage("CricketData's free plan is limited to 100 requests per day and is for personal, non-commercial use. For commercial deployments, use a suitable paid plan. The API key is supplied by you and is not included in this app.").setPositiveButton("OK",null).show());
        row("Done","Return to Home",this::finish);
    }
    private void route(String label,String key){row(label,CameraConfig.route(this,key).isEmpty()?"Open app start screen":"Custom deep link",()->edit(label,CameraConfig.route(this,key),InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI,v->{CameraConfig.setRoute(this,key,v);build();}));}
    private void chooseMediaVision(){
        List<android.content.pm.ApplicationInfo> apps=getPackageManager().getInstalledApplications(0);
        List<android.content.pm.ApplicationInfo> launchable=new ArrayList<>();for(android.content.pm.ApplicationInfo a:apps)if(getPackageManager().getLaunchIntentForPackage(a.packageName)!=null)launchable.add(a);
        String[] names=new String[launchable.size()+1];names[0]="Clear selection";for(int i=0;i<launchable.size();i++)names[i]=getPackageManager().getApplicationLabel(launchable.get(i))+"  ·  "+launchable.get(i).packageName;
        new AlertDialog.Builder(this).setTitle("Choose Media Vision").setItems(names,(d,w)->{CameraConfig.setMediaVisionPackage(this,w==0?"":launchable.get(w-1).packageName);build();}).show();
    }
    private void edit(String title,String start,int type,Done done){
        EditText field=new EditText(this);field.setSingleLine(true);field.setInputType(type);field.setText(start);field.setSelection(start.length());
        new AlertDialog.Builder(this).setTitle(title).setView(field).setPositiveButton("Save",(d,w)->done.run(field.getText().toString())).setNegativeButton("Cancel",null).show();
    }
    private interface Done{void run(String value);}
    private void heading(String s){TextView t=text(s,14,0xFF69DCCB);t.setPadding(0,dp(22),0,dp(8));rows.addView(t);}
    private void row(String title,String value,Runnable action){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),dp(12),dp(16),dp(12));box.setBackgroundColor(0xFF17263A);box.setFocusable(true);box.setClickable(true);
        TextView a=text(title,18,0xFFFFFFFF),b=text(value,13,0xFF9EB0C8);box.addView(a);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(4);box.addView(b,p);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(7);rows.addView(box,lp);box.setOnClickListener(v->action.run());box.setOnFocusChangeListener((v,f)->v.setAlpha(f?1f:0.84f));
    }
    private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}
