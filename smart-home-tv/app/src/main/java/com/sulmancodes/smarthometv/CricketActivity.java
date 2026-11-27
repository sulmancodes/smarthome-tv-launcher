package com.sulmancodes.smarthometv;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

public class CricketActivity extends Activity {
    private LinearLayout list;
    @Override protected void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(0xFF080D18);getWindow().setNavigationBarColor(0xFF080D18);
        ScrollView scroll=new ScrollView(this); list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);list.setPadding(dp(48),dp(30),dp(48),dp(30));scroll.addView(list);setContentView(scroll);
        add("CRICKET CENTRE",30,0xFFFFFFFF); add("Live scores · Upcoming fixtures · Recent results",15,0xFF9EB0C8);
        TextView refresh=button("REFRESH SCORES",()->load());list.addView(refresh);load();
    }
    private void load(){list.removeViews(3,Math.max(0,list.getChildCount()-3));CricketRepository.load(this,(live,upcoming,recent,error)->{
        if(error!=null){section("Setup");add(error,16,0xFFFFC47A);return;}
        section("LIVE NOW"); rows(live);
        section("UPCOMING"); rows(upcoming);
        section("COMPLETED"); rows(recent);
        if(live.isEmpty()&&upcoming.isEmpty()&&recent.isEmpty())add("No matches were returned by the provider.",15,0xFF9EB0C8);
    });}
    private void rows(List<CricketRepository.Match> matches){if(matches.isEmpty()){add("No matches in this section.",14,0xFF8FA2BA);return;}for(CricketRepository.Match m:matches){add(m.title,18,Color.WHITE);add(m.status+(m.venue.isEmpty()?"":" · "+m.venue),13,0xFF9EB0C8);}}
    private void section(String s){TextView t=button(s,()->{});t.setTextColor(0xFF69DCCB);list.addView(t);}
    private void add(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(0,dp(8),0,dp(8));list.addView(t);}
    private TextView button(String s,Runnable r){TextView t=new TextView(this);t.setText(s);t.setTextColor(Color.WHITE);t.setTextSize(14);t.setGravity(Gravity.CENTER);t.setPadding(dp(16),dp(12),dp(16),dp(12));t.setFocusable(true);t.setOnClickListener(v->r.run());return t;}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}
