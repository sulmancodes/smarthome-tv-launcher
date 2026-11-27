package com.sulmancodes.smarthometv;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Small CricketData/CricAPI adapter. Results are cached locally to reduce daily API consumption. */
final class CricketRepository {
    static final class Match {
        final String title,status,venue;
        Match(String title,String status,String venue){this.title=title;this.status=status;this.venue=venue;}
    }
    interface Callback { void done(List<Match> live,List<Match> upcoming,List<Match> recent,String error); }
    private static final ExecutorService IO=Executors.newSingleThreadExecutor();
    private static final Handler MAIN=new Handler(Looper.getMainLooper());
    private static final String PREFS="cricket_cache";
    private CricketRepository(){}

    static void load(Context context,Callback callback) {
        String key=CameraConfig.cricketApiKey(context);
        if(key.isEmpty()){ callback.done(new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),"Add your CricketData API key in Settings."); return; }
        Context app=context.getApplicationContext();
        IO.execute(()->{
            try {
                SharedPreferences p=app.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
                long now=System.currentTimeMillis();
                String liveJson=p.getString("live","");
                String otherJson=p.getString("other","");
                if(now-p.getLong("live_at",0)>CameraConfig.cricketRefreshMinutes(app)*60_000L || liveJson.isEmpty()){
                    liveJson=request("currentMatches",key);
                    p.edit().putString("live",liveJson).putLong("live_at",now).apply();
                }
                if(now-p.getLong("other_at",0)>24*60*60_000L || otherJson.isEmpty()){
                    otherJson=request("matches",key);
                    p.edit().putString("other",otherJson).putLong("other_at",now).apply();
                }
                List<Match> live=parse(liveJson,"live"), upcoming=parse(otherJson,"upcoming"), recent=parse(otherJson,"recent");
                MAIN.post(()->callback.done(live,upcoming,recent,null));
            } catch(Exception e) {
                MAIN.post(()->callback.done(new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),"Cricket data unavailable. Check the API key and connection."));
            }
        });
    }
    private static String request(String endpoint,String key)throws Exception{
        String address="https://api.cricapi.com/v1/"+endpoint+"?apikey="+java.net.URLEncoder.encode(key,"UTF-8")+"&offset=0";
        HttpURLConnection c=(HttpURLConnection)new URL(address).openConnection();
        c.setConnectTimeout(7000); c.setReadTimeout(7000); c.setRequestProperty("Accept","application/json");
        try {
            int status=c.getResponseCode();
            if(status<200||status>=300) throw new java.io.IOException("Cricket provider HTTP "+status);
            try(BufferedReader r=new BufferedReader(new InputStreamReader(c.getInputStream(),StandardCharsets.UTF_8))){
                StringBuilder b=new StringBuilder(); String line; while((line=r.readLine())!=null)b.append(line); return b.toString();
            }
        } finally {c.disconnect();}
    }
    private static List<Match> parse(String json,String wanted)throws Exception{
        List<Match> out=new ArrayList<>(); JSONObject root=new JSONObject(json);
        if(!"success".equalsIgnoreCase(root.optString("status"))) throw new java.io.IOException("Cricket API returned an error");
        JSONArray data=root.optJSONArray("data"); if(data==null)return out;
        for(int i=0;i<data.length();i++){
            JSONObject m=data.optJSONObject(i); if(m==null)continue;
            String status=m.optString("status","Scheduled");
            boolean live= m.optBoolean("matchStarted",false) && !m.optBoolean("matchEnded",false);
            boolean upcoming=!m.optBoolean("matchStarted",false);
            boolean recent=m.optBoolean("matchEnded",false);
            if(("live".equals(wanted)&&!live)||("upcoming".equals(wanted)&&!upcoming)||("recent".equals(wanted)&&!recent))continue;
            JSONArray teams=m.optJSONArray("teams"); String title=teams==null?"Cricket match":join(teams)+" · "+m.optString("name","");
            String venue=m.optString("venue","");
            out.add(new Match(title,status,venue));
        }
        return out;
    }
    private static String join(JSONArray a){StringBuilder b=new StringBuilder();for(int i=0;i<a.length();i++){if(i>0)b.append(" vs ");b.append(a.optString(i));}return b.toString();}
}
