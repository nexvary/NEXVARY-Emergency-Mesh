package com.nexvary.emergencymesh;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
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
 private static final int BG=Color.rgb(5,9,19),CARD=Color.rgb(12,22,38),TEXT=Color.rgb(233,240,248),MUTED=Color.rgb(148,167,190),CYAN=Color.rgb(0,229,255),GREEN=Color.rgb(50,255,137),GOLD=Color.rgb(255,196,64),RED=Color.rgb(255,63,94),PURPLE=Color.rgb(156,77,255);
 private static final String[] LANGS={"ar","en","tr","es","de","it","fr","ur","fa","ru"}; private String lang; private boolean rtl;
 @Override protected void onCreate(Bundle b){super.onCreate(b); lang=getSharedPreferences("ui",MODE_PRIVATE).getString("language","ar"); rtl="ar".equals(lang)||"ur".equals(lang)||"fa".equals(lang); getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);setContentView(build());}
 private View build(){ScrollView s=new ScrollView(this);s.setFillViewport(true);s.setBackgroundColor(BG);s.setTag("page:home");LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setPadding(dp(18),dp(28),dp(18),dp(28));r.setLayoutDirection(rtl?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);s.addView(r);
  LinearLayout bar=row(); Button menu=button("☰",CYAN);menu.setTag("menu");menu.setContentDescription("menu");menu.setOnClickListener(v->showMenu());bar.addView(menu,new LinearLayout.LayoutParams(dp(58),dp(48)));LinearLayout brand=new LinearLayout(this);brand.setOrientation(LinearLayout.VERTICAL);TextView n=text("YASLI KURT",22,TEXT,true);n.setLetterSpacing(.08f);brand.addView(n);brand.addView(text(UiText.t(lang,"subtitle"),11,GOLD,true));bar.addView(brand,new LinearLayout.LayoutParams(0,-2,1));Button l=button(lang.toUpperCase(Locale.ROOT),PURPLE);l.setTag("language");l.setOnClickListener(v->cycle());bar.addView(l,new LinearLayout.LayoutParams(dp(64),dp(48)));r.addView(bar);
  TextView title=text(rtl?"الرئيسية":"Home",24,CYAN,true);title.setPadding(0,dp(26),0,dp(6));r.addView(title);r.addView(text(rtl?"شبكة طوارئ محلية مشفرة — جاهزة للعمل دون إنترنت":"Encrypted local emergency mesh — ready offline",13,MUTED,false));
  LinearLayout status=card(GREEN);LinearLayout sr=row();TextView st=text(rtl?"حالة الشبكة":"Mesh status",15,TEXT,true);sr.addView(st,new LinearLayout.LayoutParams(0,-2,1));TextView off=text("OFFLINE • READY",12,GREEN,true);off.setTextDirection(View.TEXT_DIRECTION_LTR);sr.addView(off);status.addView(sr);TextView tech=text("Bluetooth • Wi‑Fi Direct • Meshtastic/LoRa",12,MUTED,false);tech.setTextDirection(View.TEXT_DIRECTION_LTR);tech.setPadding(0,dp(8),0,0);status.addView(tech);add(r,status,18);
  LinearLayout quick=row();quick.addView(compactStat("0",rtl?"العقد":"Nodes",CYAN),weight());quick.addView(compactStat("0",rtl?"المعلقة":"Pending",PURPLE),weight());quick.addView(compactStat("0",rtl?"موثوقة":"Trusted",GREEN),weight());add(r,quick,10);
  Button connect=button(rtl?"الاتصال والأجهزة":"Connection & devices",CYAN);connect.setTag("nav:radio");connect.setOnClickListener(v->open(NavigationRegistry.RADIO));add(r,connect,20,52);
  Button messages=button(rtl?"المحادثات والقنوات":"Messages & channels",PURPLE);messages.setTag("nav:messages");messages.setOnClickListener(v->open(NavigationRegistry.MESSAGES));add(r,messages,9,52);
  Button map=button(rtl?"الخريطة والرادار":"Map & radar",GREEN);map.setTag("nav:map");map.setOnClickListener(v->open(NavigationRegistry.MAP));add(r,map,9,52);
  LinearLayout sos=card(RED);sos.setBackground(panel(Color.rgb(43,8,18),RED));sos.addView(text(rtl?"نداء استغاثة SOS":"Emergency SOS",19,RED,true));sos.addView(text(rtl?"إرسال نداء طوارئ محلي إلى العقد القريبة":"Send a local emergency alert to nearby nodes",12,TEXT,false));Button sb=button("SOS",RED);sb.setTag("sos");sb.setOnClickListener(v->Toast.makeText(this,"SOS",Toast.LENGTH_LONG).show());add(sos,sb,12,52);add(r,sos,18);
  TextView hint=text(rtl?"استخدم زر ☰ للوصول إلى الإعدادات، الملف الميداني، التشخيص وعن التطبيق.":"Use ☰ for Settings, Field profile, Diagnostics and About.",12,MUTED,false);hint.setPadding(0,dp(18),0,0);r.addView(hint);return s;}
 private void showMenu(){final String[] labels=rtl?new String[]{"الرئيسية","الاتصال","المحادثات","القنوات","الخريطة","الرادار","الملف الميداني","التشخيص","الإعدادات","عن التطبيق"}:new String[]{"Home","Connection","Messages","Channels","Map","Radar","Field profile","Diagnostics","Settings","About"};final String[] routes={"home",NavigationRegistry.RADIO,NavigationRegistry.MESSAGES,NavigationRegistry.CHANNELS,NavigationRegistry.MAP,NavigationRegistry.RADAR,NavigationRegistry.PROFILE,NavigationRegistry.DIAGNOSTICS,NavigationRegistry.SETTINGS,NavigationRegistry.ABOUT};new AlertDialog.Builder(this).setTitle(rtl?"القائمة الرئيسية":"Main menu").setItems(labels,(d,w)->{if(w>0)open(routes[w]);}).show();}
 private void open(String route){Intent i=new Intent(this,PageActivity.class);i.putExtra(PageActivity.EXTRA_ROUTE,route);startActivity(i);}
 @Override public void onBackPressed(){new AlertDialog.Builder(this).setTitle(rtl?"الخروج":"Exit").setMessage(rtl?"هل تريد الخروج من Yasli kurt؟":"Exit Yasli kurt?").setNegativeButton(rtl?"إلغاء":"Cancel",null).setPositiveButton(rtl?"خروج":"Exit",(d,w)->finish()).show();}
 private void cycle(){int x=0;for(int i=0;i<LANGS.length;i++)if(LANGS[i].equals(lang))x=i;getSharedPreferences("ui",MODE_PRIVATE).edit().putString("language",LANGS[(x+1)%LANGS.length]).apply();recreate();}
 private LinearLayout compactStat(String value,String label,int c){LinearLayout b=card(c);b.setPadding(dp(8),dp(10),dp(8),dp(10));b.setGravity(Gravity.CENTER);TextView v=text(value,20,c,true);v.setGravity(Gravity.CENTER);TextView l=text(label,10,MUTED,false);l.setGravity(Gravity.CENTER);b.addView(v);b.addView(l);return b;}
 private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);l.setLayoutDirection(rtl?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);return l;} private LinearLayout card(int c){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(dp(14),dp(14),dp(14),dp(14));l.setBackground(panel(CARD,c));return l;}
 private Button button(String x,int c){Button b=new Button(this);b.setText(x);b.setAllCaps(false);b.setTextColor(TEXT);b.setTextSize(14);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setBackground(panel(Color.rgb(16,27,46),c));return b;} private TextView text(String x,int sp,int c,boolean bold){TextView t=new TextView(this);t.setText(x);t.setTextColor(c);t.setTextSize(sp);t.setTypeface(Typeface.DEFAULT,bold?Typeface.BOLD:Typeface.NORMAL);t.setGravity((rtl?Gravity.END:Gravity.START)|Gravity.CENTER_VERTICAL);t.setTextDirection(rtl?View.TEXT_DIRECTION_RTL:View.TEXT_DIRECTION_LTR);return t;}
 private GradientDrawable panel(int f,int s){GradientDrawable g=new GradientDrawable();g.setColor(f);g.setCornerRadius(dp(15));g.setStroke(dp(1),s);return g;} private LinearLayout.LayoutParams weight(){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.setMarginStart(dp(4));p.setMarginEnd(dp(4));return p;} private void add(LinearLayout r,View v,int top){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(top);r.addView(v,p);} private void add(LinearLayout r,View v,int top,int h){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(h));p.topMargin=dp(top);r.addView(v,p);} private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
}