package com.nexvary.emergencymesh;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public final class MainActivity extends Activity {
    private static final int BG=Color.rgb(5,9,19), CARD=Color.rgb(12,22,38), TEXT=Color.rgb(233,240,248), MUTED=Color.rgb(148,167,190);
    private static final int CYAN=Color.rgb(0,229,255), PURPLE=Color.rgb(156,77,255), GREEN=Color.rgb(50,255,137), GOLD=Color.rgb(255,196,64), RED=Color.rgb(255,63,94), BLUE=Color.rgb(55,145,255);
    private static final String[] LANGS={"ar","en","tr","es","de","it","fr","ur","fa","ru"};
    private String lang; private boolean rtl;

    @Override protected void onCreate(Bundle state){
        super.onCreate(state); configureLanguage();
        getWindow().setStatusBarColor(BG); getWindow().setNavigationBarColor(BG);
        setContentView(build());
    }

    private void configureLanguage(){
        String saved=getSharedPreferences("ui",MODE_PRIVATE).getString("language","ar"); lang=supported(saved)?saved:"ar";
        rtl="ar".equals(lang)||"ur".equals(lang)||"fa".equals(lang);
        Locale locale=new Locale(lang); Configuration cfg=new Configuration(getResources().getConfiguration()); cfg.setLocale(locale); cfg.setLayoutDirection(locale);
    }
    private boolean supported(String c){ if(c==null)return false; for(String x:LANGS)if(x.equals(c))return true; return false; }

    private View build(){
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(BG); scroll.setTag("page:home");
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(18),dp(16),dp(18),dp(32)); root.setLayoutDirection(rtl?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);
        scroll.addView(root,new ScrollView.LayoutParams(-1,-2));

        LinearLayout top=row();
        LinearLayout brandBox=new LinearLayout(this); brandBox.setOrientation(LinearLayout.VERTICAL); brandBox.setLayoutDirection(root.getLayoutDirection());
        TextView brand=text("YASLI KURT",30,TEXT,true); brand.setLetterSpacing(.10f); brandBox.addView(brand);
        brandBox.addView(text(rtl?"شبكة طوارئ محلية مشفّرة":"Offline encrypted emergency mesh",12,GOLD,true));
        top.addView(brandBox,new LinearLayout.LayoutParams(0,-2,1f));
        Button language=button(lang.toUpperCase(Locale.ROOT),PURPLE); language.setTag("language"); language.setOnClickListener(v->cycleLanguage()); top.addView(language,new LinearLayout.LayoutParams(-2,dp(46)));
        root.addView(top);

        TextView hero=text(rtl?"مركز القيادة الميداني":"Field command center",25,CYAN,true); hero.setPadding(0,dp(16),0,dp(6)); root.addView(hero);
        root.addView(text(rtl?"اتصالات طوارئ دون إنترنت • Bluetooth • Wi‑Fi Direct • Meshtastic/LoRa":"Emergency communications without internet • Bluetooth • Wi‑Fi Direct • Meshtastic/LoRa",14,MUTED,false));

        TextView badge=text("OFFLINE • ENCRYPTED • LOCAL-FIRST",11,GREEN,true); badge.setGravity(Gravity.CENTER); badge.setPadding(dp(10),dp(9),dp(10),dp(9)); badge.setBackground(panel(Color.rgb(7,37,31),GREEN));
        LinearLayout.LayoutParams badgeLp=new LinearLayout.LayoutParams(-1,-2); badgeLp.topMargin=dp(14); root.addView(badge,badgeLp);

        root.addView(statusGrid(),spaceTop(14));
        root.addView(identityCard(),spaceTop(12));
        root.addView(sosCard(),spaceTop(12));

        TextView navTitle=text(rtl?"التنقل الكامل":"Full navigation",18,TEXT,true); navTitle.setPadding(0,dp(22),0,dp(10)); root.addView(navTitle);
        addNavGrid(root, new String[]{NavigationRegistry.MESSAGES,NavigationRegistry.RADAR,NavigationRegistry.MAP,NavigationRegistry.DIAGNOSTICS,NavigationRegistry.RADIO,NavigationRegistry.PROFILE,NavigationRegistry.CHANNELS,NavigationRegistry.SETTINGS,NavigationRegistry.ABOUT});

        TextView integrity=text(NavigationRegistry.integrityOk()?(rtl?"سلامة التنقل: ناجح":"Navigation integrity: PASS"):(rtl?"سلامة التنقل: فشل":"Navigation integrity: FAIL"),13,NavigationRegistry.integrityOk()?GREEN:RED,true);
        integrity.setGravity(Gravity.CENTER); integrity.setPadding(0,dp(22),0,dp(8)); root.addView(integrity);
        return scroll;
    }

    private View statusGrid(){
        LinearLayout wrap=new LinearLayout(this); wrap.setOrientation(LinearLayout.VERTICAL); wrap.setLayoutDirection(rtl?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);
        LinearLayout a=row(); a.addView(stat("0",rtl?"العُقد":"Nodes",CYAN),weight()); a.addView(stat("0",rtl?"معلّقة":"Pending",PURPLE),weight()); wrap.addView(a);
        LinearLayout b=row(); b.addView(stat("0",rtl?"موثوقة":"Trusted",GREEN),weight()); b.addView(stat(rtl?"استعداد":"STANDBY",rtl?"الراديو":"Radio",GOLD),weight()); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.topMargin=dp(9); wrap.addView(b,lp); return wrap;
    }
    private View identityCard(){ LinearLayout box=card(CYAN); box.addView(text(rtl?"الهوية الميدانية":"Field identity",18,CYAN,true)); box.addView(kv(rtl?"اسم النداء":"Call sign","YK-01",PURPLE)); box.addView(kv(rtl?"الدور":"Role",rtl?"فريق إنقاذ":"Rescue team",GREEN)); box.addView(kv(rtl?"التشفير":"Encryption","AES-256-GCM • NEM3",GOLD)); return box; }
    private View sosCard(){ LinearLayout box=card(RED); box.setBackground(panel(Color.rgb(44,8,18),RED)); box.addView(text(rtl?"نداء استغاثة SOS":"SOS Emergency Beacon",22,RED,true)); TextView h=text(rtl?"إضافة نداء طوارئ عالي الأولوية إلى قائمة الإرسال المحلية":"Queue a high-priority emergency message for local delivery",14,TEXT,false); h.setPadding(0,dp(6),0,dp(12)); box.addView(h); Button b=button("SOS",RED); b.setTag("sos"); b.setOnClickListener(v->Toast.makeText(this,rtl?"تم تجهيز نداء الطوارئ":"SOS queued",Toast.LENGTH_LONG).show()); box.addView(b,new LinearLayout.LayoutParams(-1,dp(54))); return box; }

    private void addNavGrid(LinearLayout root,String[] routes){ for(int i=0;i<routes.length;i+=2){ LinearLayout line=row(); line.addView(navButton(routes[i]),weight()); if(i+1<routes.length)line.addView(navButton(routes[i+1]),weight()); else line.addView(new View(this),weight()); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); if(i>0)lp.topMargin=dp(9); root.addView(line,lp);} }
    private View navButton(String route){ Button b=button(routeTitle(route),routeColor(route)); b.setTag("nav:"+route); b.setContentDescription("nav:"+route); b.setOnClickListener(v->open(route)); return b; }
    private void open(String route){ Intent i=new Intent(this,PageActivity.class); i.putExtra(PageActivity.EXTRA_ROUTE,route); startActivity(i); }

    @Override public void onBackPressed(){ new AlertDialog.Builder(this).setTitle(rtl?"الخروج من Yasli kurt؟":"Exit Yasli kurt?").setMessage(rtl?"سيستمر حفظ حالة الشبكة محليًا.":"Mesh state remains stored locally.").setNegativeButton(rtl?"إلغاء":"Cancel",null).setPositiveButton(rtl?"خروج":"Exit",(d,w)->finish()).show(); }

    private void cycleLanguage(){ int idx=0; for(int i=0;i<LANGS.length;i++)if(LANGS[i].equals(lang))idx=i; String next=LANGS[(idx+1)%LANGS.length]; getSharedPreferences("ui",MODE_PRIVATE).edit().putString("language",next).apply(); recreate(); }
    private LinearLayout row(){ LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); l.setLayoutDirection(rtl?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR); return l; }
    private LinearLayout card(int accent){ LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(dp(16),dp(16),dp(16),dp(16)); l.setLayoutDirection(rtl?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR); l.setBackground(panel(CARD,accent)); return l; }
    private View kv(String k,String v,int c){ LinearLayout l=row(); l.setPadding(0,dp(10),0,0); l.addView(text(k,13,MUTED,false),new LinearLayout.LayoutParams(0,-2,1f)); l.addView(text(v,13,c,true)); return l; }
    private View stat(String v,String l,int c){ LinearLayout box=card(c); box.setGravity(Gravity.CENTER); TextView a=text(v,23,c,true); a.setGravity(Gravity.CENTER); TextView b=text(l,12,MUTED,false); b.setGravity(Gravity.CENTER); box.addView(a); box.addView(b); return box; }
    private Button button(String label,int accent){ Button b=new Button(this); b.setText(label); b.setAllCaps(false); b.setTextColor(TEXT); b.setTextSize(14); b.setTypeface(Typeface.DEFAULT,Typeface.BOLD); b.setBackground(panel(Color.rgb(16,27,46),accent)); return b; }
    private TextView text(String s,int sp,int c,boolean bold){ TextView t=new TextView(this); t.setText(s); t.setTextColor(c); t.setTextSize(sp); t.setTypeface(Typeface.DEFAULT,bold?Typeface.BOLD:Typeface.NORMAL); t.setTextDirection(rtl?View.TEXT_DIRECTION_RTL:View.TEXT_DIRECTION_LTR); t.setGravity((rtl?Gravity.END:Gravity.START)|Gravity.CENTER_VERTICAL); return t; }
    private GradientDrawable panel(int fill,int stroke){ GradientDrawable g=new GradientDrawable(); g.setColor(fill); g.setCornerRadius(dp(18)); g.setStroke(dp(1),stroke); return g; }
    private LinearLayout.LayoutParams weight(){ LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1f); lp.setMarginStart(dp(5)); lp.setMarginEnd(dp(5)); return lp; }
    private LinearLayout.LayoutParams spaceTop(int n){ LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.topMargin=dp(n); return lp; }
    private int routeColor(String r){ switch(r){ case NavigationRegistry.MESSAGES:return PURPLE; case NavigationRegistry.RADAR:return GREEN; case NavigationRegistry.MAP:return CYAN; case NavigationRegistry.DIAGNOSTICS:return GOLD; case NavigationRegistry.RADIO:return RED; case NavigationRegistry.PROFILE:return BLUE; case NavigationRegistry.CHANNELS:return PURPLE; case NavigationRegistry.SETTINGS:return GOLD; default:return GREEN; } }
    private String routeTitle(String r){ if(rtl){ switch(r){ case NavigationRegistry.MESSAGES:return "المحادثات"; case NavigationRegistry.RADAR:return "الرادار"; case NavigationRegistry.MAP:return "الخريطة"; case NavigationRegistry.DIAGNOSTICS:return "التشخيص"; case NavigationRegistry.RADIO:return "الراديو"; case NavigationRegistry.PROFILE:return "الملف الميداني"; case NavigationRegistry.CHANNELS:return "القنوات"; case NavigationRegistry.SETTINGS:return "الإعدادات"; case NavigationRegistry.ABOUT:return "حول التطبيق"; } } switch(r){ case NavigationRegistry.MESSAGES:return "Messages"; case NavigationRegistry.RADAR:return "Radar"; case NavigationRegistry.MAP:return "Map"; case NavigationRegistry.DIAGNOSTICS:return "Diagnostics"; case NavigationRegistry.RADIO:return "Radio"; case NavigationRegistry.PROFILE:return "Field profile"; case NavigationRegistry.CHANNELS:return "Channels"; case NavigationRegistry.SETTINGS:return "Settings"; case NavigationRegistry.ABOUT:return "About"; default:return r; } }
    private int dp(int n){ return Math.round(n*getResources().getDisplayMetrics().density); }
}
