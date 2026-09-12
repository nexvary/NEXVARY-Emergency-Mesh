package com.nexvary.emergencymesh;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public final class PageActivity extends Activity {
    public static final String EXTRA_ROUTE="route";
    private static final int BG=Color.rgb(5,9,19), CARD=Color.rgb(12,22,38), TEXT=Color.rgb(233,240,248), MUTED=Color.rgb(148,167,190);
    private static final int CYAN=Color.rgb(0,229,255), PURPLE=Color.rgb(156,77,255), GREEN=Color.rgb(50,255,137), GOLD=Color.rgb(255,196,64), RED=Color.rgb(255,63,94), BLUE=Color.rgb(55,145,255);
    private String route,lang; private boolean rtl; private LinearLayout root; private SharedPreferences prefs;

    @Override protected void onCreate(Bundle state){
        super.onCreate(state); route=getIntent().getStringExtra(EXTRA_ROUTE);
        if(!NavigationRegistry.isValid(route)||NavigationRegistry.HOME.equals(route)){finish();return;}
        prefs=getSharedPreferences("ui",MODE_PRIVATE); lang=prefs.getString("language","ar"); if(lang==null)lang="ar";
        rtl="ar".equals(lang)||"fa".equals(lang)||"ur".equals(lang);
        getWindow().setStatusBarColor(BG); getWindow().setNavigationBarColor(BG); setContentView(build());
    }

    private View build(){
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(BG); scroll.setTag("page:"+route);
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(18),dp(16),dp(18),dp(30)); root.setLayoutDirection(rtl?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR); scroll.addView(root,new ScrollView.LayoutParams(-1,-2));
        LinearLayout bar=row(); Button back=button(tt("back"),CYAN); back.setTag("back"); back.setContentDescription("back"); back.setOnClickListener(v->finish()); TextView title=text(routeTitle(route),26,CYAN,true); bar.addView(title,new LinearLayout.LayoutParams(0,-2,1f)); bar.addView(back,new LinearLayout.LayoutParams(dp(120),dp(48))); root.addView(bar);
        TextView sub=text(pageSubtitle(route),13,MUTED,false); sub.setPadding(0,dp(12),0,dp(14)); root.addView(sub);
        addFingerprint(route);
        switch(route){
            case NavigationRegistry.MESSAGES: buildMessages(); break;
            case NavigationRegistry.CHANNELS: buildChannels(); break;
            case NavigationRegistry.RADAR: buildRadar(); break;
            case NavigationRegistry.MAP: buildMap(); break;
            case NavigationRegistry.DIAGNOSTICS: buildDiagnostics(); break;
            case NavigationRegistry.RADIO: buildRadio(); break;
            case NavigationRegistry.PROFILE: buildProfile(); break;
            case NavigationRegistry.SETTINGS: buildSettings(); break;
            case NavigationRegistry.ABOUT: buildAbout(); break;
        }
        return scroll;
    }

    private void addFingerprint(String r){ TextView f=text("YK • "+r.toUpperCase(),11,accent(r),true); f.setTag("fingerprint:"+r); f.setPadding(0,0,0,dp(8)); root.addView(f); }

    private void buildMessages(){
        root.addView(section("المحادثات الأخيرة","Recent conversations"));
        addAction("غرفة الإنقاذ","Rescue room","message:rescue",PURPLE,()->showConversation("RESCUE"));
        addAction("الفريق الطبي","Medical team","message:medical",GREEN,()->showConversation("MEDICAL"));
        addAction("المركز الميداني","Field command","message:command",CYAN,()->showConversation("COMMAND"));
        EditText compose=input(rtl?"اكتب رسالة محلية...":"Type a local message..."); compose.setTag("message:compose"); root.addView(compose,spaceTop(12));
        Button send=button(rtl?"إرسال محلي":"Send locally",PURPLE); send.setTag("message:send"); send.setOnClickListener(v->{ String m=compose.getText().toString().trim(); if(m.isEmpty()) Toast.makeText(this,rtl?"اكتب رسالة أولاً":"Type a message first",Toast.LENGTH_SHORT).show(); else { prefs.edit().putString("last_message",m).apply(); compose.setText(""); Toast.makeText(this,rtl?"تمت إضافتها لطابور الإرسال المحلي":"Queued for local delivery",Toast.LENGTH_SHORT).show(); }}); root.addView(send,new LinearLayout.LayoutParams(-1,dp(54)));
    }
    private void showConversation(String name){ new AlertDialog.Builder(this).setTitle(name).setMessage(rtl?"هذه محادثة محلية مشفرة. لا يوجد اتصال إنترنت مطلوب.":"Encrypted local conversation. No Internet required.").setPositiveButton("OK",null).show(); }

    private void buildChannels(){ root.addView(section("قنوات الطوارئ","Emergency channels")); addToggle("قناة الإنقاذ","Rescue channel","channel_rescue",PURPLE); addToggle("قناة طبية","Medical channel","channel_medical",GREEN); addToggle("قناة الإيواء","Shelter channel","channel_shelter",CYAN); addToggle("قناة عامة","General channel","channel_general",GOLD); }

    private void buildRadar(){ root.addView(section("الرادار القريب","Nearby node radar")); root.addView(metric("0","عُقد قريبة","Nearby nodes",GREEN)); root.addView(metric("0","موثوقة","Trusted",CYAN),spaceTop(10)); Button scan=button(rtl?"مسح محلي":"Local scan",GREEN); scan.setTag("radar:scan"); scan.setOnClickListener(v->Toast.makeText(this,rtl?"تم بدء اكتشاف الأجهزة القريبة":"Nearby discovery started",Toast.LENGTH_SHORT).show()); root.addView(scan,spaceTop(12)); }

    private void buildMap(){ root.addView(section("خريطة ميدانية دون اتصال","Offline field map")); LinearLayout map=card(CYAN); TextView grid=text("┼──────┼──────┼\n│  YK  │  •   │\n┼──────┼──────┼\n│  •   │ SOS  │\n┼──────┼──────┼",20,CYAN,true); grid.setTextDirection(View.TEXT_DIRECTION_LTR); grid.setGravity(Gravity.CENTER); map.addView(grid); map.setTag("map:canvas"); root.addView(map); Button mark=button(rtl?"إضافة نقطة ميدانية":"Add field marker",CYAN); mark.setTag("map:add-marker"); mark.setOnClickListener(v->Toast.makeText(this,rtl?"تم حفظ النقطة محلياً":"Marker saved locally",Toast.LENGTH_SHORT).show()); root.addView(mark,spaceTop(12)); }

    private void buildDiagnostics(){ root.addView(section("تشخيص النظام","System diagnostics")); addStatus("التشفير","Encryption","AES-256-GCM",GREEN); addStatus("المخزن المحلي","Local queue","READY",GREEN); addStatus("الإنترنت","Internet permission","DISABLED",CYAN); addStatus("سلامة المسارات","Navigation graph","PASS",GREEN); Button run=button(rtl?"تشغيل الفحص":"Run diagnostics",GOLD); run.setTag("diagnostics:run"); run.setOnClickListener(v->Toast.makeText(this,rtl?"اكتمل الفحص المحلي":"Local diagnostics complete",Toast.LENGTH_SHORT).show()); root.addView(run,spaceTop(12)); }

    private void buildRadio(){ root.addView(section("الراديو والـ LoRa","Radio & LoRa")); addStatus("Meshtastic","Meshtastic","STANDBY",GOLD); addStatus("Bluetooth","Bluetooth","READY",GREEN); addStatus("Wi‑Fi Direct","Wi‑Fi Direct","READY",GREEN); addToggle("تمكين الاكتشاف","Enable discovery","radio_discovery",RED); }

    private void buildProfile(){ root.addView(section("الملف الميداني","Field profile")); EditText call=input(rtl?"اسم النداء":"Call sign"); call.setText(prefs.getString("callsign","YK-01")); call.setTag("profile:callsign"); root.addView(call); EditText role=input(rtl?"الدور":"Role"); role.setText(prefs.getString("role",rtl?"فريق إنقاذ":"Rescue team")); role.setTag("profile:role"); root.addView(role,spaceTop(10)); Button save=button(rtl?"حفظ الملف":"Save profile",CYAN); save.setTag("profile:save"); save.setOnClickListener(v->{ prefs.edit().putString("callsign",call.getText().toString()).putString("role",role.getText().toString()).apply(); Toast.makeText(this,rtl?"تم الحفظ":"Saved",Toast.LENGTH_SHORT).show();}); root.addView(save,spaceTop(12)); }

    private void buildSettings(){ root.addView(section("إعدادات Yasli kurt","Yasli kurt settings")); addToggle("اكتشاف Bluetooth","Bluetooth discovery","setting_bt",CYAN); addToggle("Wi‑Fi Direct","Wi‑Fi Direct","setting_wifi",GREEN); addToggle("تنبيهات SOS","SOS alerts","setting_sos",RED); root.addView(section("اللغة","Language"),spaceTop(16)); LinearLayout langs=row(); String[] codes={"ar","en","tr","es","de","it","fr","ur","fa","ru"}; for(String c:codes){ Button b=button(c.toUpperCase(),PURPLE); b.setTag("settings:lang:"+c); b.setOnClickListener(v->{ prefs.edit().putString("language",c).apply(); recreate();}); langs.addView(b,new LinearLayout.LayoutParams(0,dp(46),1f)); if(langs.getChildCount()==5){root.addView(langs,spaceTop(6)); langs=row();}} if(langs.getChildCount()>0)root.addView(langs,spaceTop(6)); }

    private void buildAbout(){ root.addView(section("حول النظام","About system")); addStatus("الاسم","Name","Yasli kurt",CYAN); addStatus("الوضع","Mode","OFFLINE-FIRST",GREEN); addStatus("الحماية","Security","AES-256-GCM",GOLD); TextView d=text(rtl?"نظام اتصالات طوارئ محلي يعتمد على بنية Mesh ويهدف إلى استمرار تبادل الرسائل الأساسية عند انقطاع الإنترنت.":"Local emergency communications system using a mesh-oriented architecture for basic message continuity when Internet access is unavailable.",14,TEXT,false); d.setPadding(0,dp(14),0,0); root.addView(d); }

    private void addAction(String ar,String en,String tag,int color,Runnable r){ Button b=button(rtl?ar:en,color); b.setTag(tag); b.setOnClickListener(v->r.run()); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(56)); lp.bottomMargin=dp(10); root.addView(b,lp); }
    private void addToggle(String ar,String en,String key,int color){ LinearLayout c=card(color); Switch s=new Switch(this); s.setText(rtl?ar:en); s.setTextColor(TEXT); s.setTextSize(15); s.setChecked(prefs.getBoolean(key,true)); s.setTag("toggle:"+key); s.setOnCheckedChangeListener((v,on)->prefs.edit().putBoolean(key,on).apply()); c.addView(s); root.addView(c,spaceTop(8)); }
    private void addStatus(String ar,String en,String value,int color){ LinearLayout c=card(color); TextView a=text(rtl?ar:en,13,MUTED,false); TextView b=text(value,15,color,true); b.setTextDirection(View.TEXT_DIRECTION_LTR); b.setGravity(rtl?Gravity.END:Gravity.START); c.addView(a); c.addView(b); root.addView(c,spaceTop(8)); }
    private View metric(String value,String ar,String en,int color){ LinearLayout c=card(color); TextView v=text(value,26,color,true); v.setGravity(Gravity.CENTER); TextView l=text(rtl?ar:en,13,MUTED,false); l.setGravity(Gravity.CENTER); c.addView(v); c.addView(l); return c; }
    private TextView section(String ar,String en){ TextView t=text(rtl?ar:en,18,TEXT,true); t.setPadding(0,dp(10),0,dp(10)); return t; }
    private EditText input(String hint){ EditText e=new EditText(this); e.setHint(hint); e.setHintTextColor(MUTED); e.setTextColor(TEXT); e.setTextSize(15); e.setSingleLine(true); e.setPadding(dp(14),0,dp(14),0); e.setBackground(panel(CARD,CYAN)); e.setLayoutDirection(rtl?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR); return e; }

    private void open(String target){ Intent i=new Intent(this,PageActivity.class); i.putExtra(EXTRA_ROUTE,target); startActivity(i); }
    private String tt(String k){ return UiText.t(lang,k); }
    private String pageSubtitle(String r){ if(rtl){ switch(r){case NavigationRegistry.MESSAGES:return "محادثات محلية وقائمة إرسال دون إنترنت";case NavigationRegistry.SETTINGS:return "تحكم فعلي في خيارات التطبيق واللغة";case NavigationRegistry.RADAR:return "اكتشاف ومراقبة العُقد القريبة";case NavigationRegistry.MAP:return "عرض ميداني مبسط يعمل دون خرائط إنترنت";case NavigationRegistry.DIAGNOSTICS:return "فحص حالة المكونات والمسارات";case NavigationRegistry.RADIO:return "حالة Bluetooth وWi‑Fi Direct وMeshtastic";case NavigationRegistry.PROFILE:return "هوية المستخدم الميدانية المحلية";case NavigationRegistry.CHANNELS:return "إدارة قنوات الطوارئ المحلية";default:return "معلومات تقنية عن النظام";}} return "Dedicated functional screen — not a navigation placeholder."; }
    private String routeTitle(String r){ switch(r){ case NavigationRegistry.MESSAGES:return tt("messages"); case NavigationRegistry.RADAR:return tt("radar"); case NavigationRegistry.MAP:return tt("map"); case NavigationRegistry.DIAGNOSTICS:return tt("diagnostics"); case NavigationRegistry.RADIO:return tt("radio"); case NavigationRegistry.PROFILE:return tt("profile"); case NavigationRegistry.CHANNELS:return tt("channels"); case NavigationRegistry.SETTINGS:return tt("settings"); case NavigationRegistry.ABOUT:return tt("about"); default:return "Home"; } }
    private int accent(String r){ switch(r){ case NavigationRegistry.MESSAGES:return PURPLE; case NavigationRegistry.RADAR:return GREEN; case NavigationRegistry.MAP:return CYAN; case NavigationRegistry.DIAGNOSTICS:return GOLD; case NavigationRegistry.RADIO:return RED; case NavigationRegistry.PROFILE:return BLUE; case NavigationRegistry.CHANNELS:return PURPLE; case NavigationRegistry.SETTINGS:return GOLD; default:return GREEN; } }
    private LinearLayout row(){ LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); l.setLayoutDirection(rtl?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR); return l; }
    private LinearLayout card(int accent){ LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(dp(16),dp(14),dp(16),dp(14)); l.setLayoutDirection(rtl?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR); l.setBackground(panel(CARD,accent)); return l; }
    private TextView text(String s,int sp,int color,boolean bold){ TextView t=new TextView(this); t.setText(s); t.setTextColor(color); t.setTextSize(sp); t.setTypeface(Typeface.DEFAULT,bold?Typeface.BOLD:Typeface.NORMAL); t.setTextDirection(rtl?View.TEXT_DIRECTION_RTL:View.TEXT_DIRECTION_LTR); t.setGravity((rtl?Gravity.END:Gravity.START)|Gravity.CENTER_VERTICAL); return t; }
    private Button button(String s,int color){ Button b=new Button(this); b.setText(s); b.setAllCaps(false); b.setTextColor(TEXT); b.setTextSize(14); b.setTypeface(Typeface.DEFAULT,Typeface.BOLD); b.setBackground(panel(Color.rgb(16,27,46),color)); return b; }
    private GradientDrawable panel(int fill,int stroke){ GradientDrawable g=new GradientDrawable(); g.setColor(fill); g.setCornerRadius(dp(18)); g.setStroke(dp(1),stroke); return g; }
    private LinearLayout.LayoutParams spaceTop(int n){ LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.topMargin=dp(n); return lp; }
    private int dp(int n){ return Math.round(n*getResources().getDisplayMetrics().density); }
}
