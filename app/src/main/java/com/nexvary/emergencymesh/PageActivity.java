package com.nexvary.emergencymesh;

import android.app.Activity;
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

import java.util.Locale;

public final class PageActivity extends Activity {
    public static final String EXTRA_ROUTE="route";
    private static final int BG=Color.rgb(5,9,19), CARD=Color.rgb(12,22,38), TEXT=Color.rgb(233,240,248), MUTED=Color.rgb(148,167,190);
    private static final int CYAN=Color.rgb(0,229,255), PURPLE=Color.rgb(156,77,255), GREEN=Color.rgb(50,255,137), GOLD=Color.rgb(255,196,64), RED=Color.rgb(255,63,94);
    private boolean rtl; private String route; private String lang;

    @Override protected void onCreate(Bundle state){
        super.onCreate(state);
        route=getIntent().getStringExtra(EXTRA_ROUTE);
        if(!NavigationRegistry.isValid(route)||NavigationRegistry.HOME.equals(route)){ finish(); return; }
        configureLocale(); getWindow().setStatusBarColor(BG); getWindow().setNavigationBarColor(BG); setContentView(build());
    }
    private void configureLocale(){
        lang=getSharedPreferences("ui",MODE_PRIVATE).getString("language","ar"); if(lang==null)lang="ar";
        rtl="ar".equals(lang)||"fa".equals(lang)||"ur".equals(lang);
        Locale locale=new Locale(lang); Configuration c=new Configuration(getResources().getConfiguration()); c.setLocale(locale); c.setLayoutDirection(locale);
    }
    private View build(){
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(BG); scroll.setTag("page:"+route);
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(18),dp(18),dp(18),dp(28)); root.setLayoutDirection(rtl?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);
        scroll.addView(root,new ScrollView.LayoutParams(-1,-2));

        LinearLayout bar=row(); TextView title=text(routeTitle(route),24,CYAN,true); Button back=button(tt("back"),CYAN); back.setTag("back"); back.setContentDescription("back"); back.setOnClickListener(v->finish());
        bar.addView(title,new LinearLayout.LayoutParams(0,-2,1f)); bar.addView(back,new LinearLayout.LayoutParams(-2,dp(46))); root.addView(bar);

        TextView status=text(tt("page_ok"),14,MUTED,false); status.setPadding(0,dp(12),0,dp(16)); root.addView(status);
        LinearLayout info=card(accent(route)); info.addView(text(tt("integrity"),14,GREEN,true)); TextView rt=text("Route: "+route,12,GOLD,true); rt.setTextDirection(View.TEXT_DIRECTION_LTR); rt.setGravity(Gravity.START); rt.setPadding(0,dp(9),0,0); info.addView(rt); root.addView(info);

        TextView links=text(tt("links"),17,TEXT,true); links.setPadding(0,dp(22),0,dp(10)); root.addView(links);
        for(String target:NavigationRegistry.links(route)){
            Button b=button(routeTitle(target),accent(target)); b.setTag("nav:"+target); b.setContentDescription("nav:"+target); b.setOnClickListener(v->open(target));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(54)); lp.bottomMargin=dp(10); root.addView(b,lp);
        }
        return scroll;
    }
    private void open(String target){ Intent i=new Intent(this,PageActivity.class); i.putExtra(EXTRA_ROUTE,target); startActivity(i); }
    private String tt(String k){ return UiText.t(lang,k); }
    private String routeTitle(String r){ switch(r){ case NavigationRegistry.MESSAGES:return tt("messages"); case NavigationRegistry.RADAR:return tt("radar"); case NavigationRegistry.MAP:return tt("map"); case NavigationRegistry.DIAGNOSTICS:return tt("diagnostics"); case NavigationRegistry.RADIO:return tt("radio"); case NavigationRegistry.PROFILE:return tt("profile"); case NavigationRegistry.CHANNELS:return tt("channels"); case NavigationRegistry.SETTINGS:return tt("settings"); case NavigationRegistry.ABOUT:return tt("about"); default:return "Home"; } }
    private int accent(String r){ switch(r){ case NavigationRegistry.MESSAGES:return PURPLE; case NavigationRegistry.RADAR:return GREEN; case NavigationRegistry.MAP:return CYAN; case NavigationRegistry.DIAGNOSTICS:return GOLD; case NavigationRegistry.RADIO:return RED; case NavigationRegistry.PROFILE:return CYAN; case NavigationRegistry.CHANNELS:return PURPLE; case NavigationRegistry.SETTINGS:return GOLD; default:return GREEN; } }
    private LinearLayout row(){ LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); l.setLayoutDirection(rtl?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR); return l; }
    private LinearLayout card(int accent){ LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(dp(16),dp(16),dp(16),dp(16)); l.setLayoutDirection(rtl?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR); l.setBackground(panel(CARD,accent)); return l; }
    private TextView text(String s,int sp,int color,boolean bold){ TextView t=new TextView(this); t.setText(s); t.setTextColor(color); t.setTextSize(sp); t.setTypeface(Typeface.DEFAULT,bold?Typeface.BOLD:Typeface.NORMAL); t.setTextDirection(rtl?View.TEXT_DIRECTION_RTL:View.TEXT_DIRECTION_LTR); t.setGravity((rtl?Gravity.END:Gravity.START)|Gravity.CENTER_VERTICAL); return t; }
    private Button button(String s,int color){ Button b=new Button(this); b.setText(s); b.setAllCaps(false); b.setTextColor(TEXT); b.setTextSize(14); b.setTypeface(Typeface.DEFAULT,Typeface.BOLD); b.setBackground(panel(Color.rgb(16,27,46),color)); return b; }
    private GradientDrawable panel(int fill,int stroke){ GradientDrawable g=new GradientDrawable(); g.setColor(fill); g.setCornerRadius(dp(18)); g.setStroke(dp(1),stroke); return g; }
    private int dp(int n){ return Math.round(n*getResources().getDisplayMetrics().density); }
}
