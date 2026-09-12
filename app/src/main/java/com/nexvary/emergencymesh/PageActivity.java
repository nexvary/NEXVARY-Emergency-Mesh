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
    private boolean rtl;
    private String route;

    @Override protected void onCreate(Bundle state){
        super.onCreate(state);
        route=getIntent().getStringExtra(EXTRA_ROUTE);
        if(!NavigationRegistry.isValid(route) || NavigationRegistry.HOME.equals(route)){ finish(); return; }
        configureLocale();
        getWindow().setStatusBarColor(BG); getWindow().setNavigationBarColor(BG);
        setContentView(build());
    }

    private void configureLocale(){
        String code=getSharedPreferences("ui",MODE_PRIVATE).getString("language","ar");
        rtl="ar".equals(code)||"fa".equals(code)||"ur".equals(code);
        Locale locale=new Locale(code==null?"ar":code);
        Configuration c=new Configuration(getResources().getConfiguration()); c.setLocale(locale); c.setLayoutDirection(locale);
    }

    private View build(){
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(BG); scroll.setTag("page:"+route);
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(18),dp(18),dp(18),dp(28)); root.setLayoutDirection(rtl?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);
        scroll.addView(root,new ScrollView.LayoutParams(-1,-2));

        LinearLayout bar=row();
        Button back=button(rtl?"رجوع":"Back",CYAN); back.setTag("back"); back.setOnClickListener(v->finish());
        TextView title=text(title(route),24,CYAN,true); title.setGravity((rtl?Gravity.END:Gravity.START)|Gravity.CENTER_VERTICAL);
        bar.addView(title,new LinearLayout.LayoutParams(0,-2,1f)); bar.addView(back,new LinearLayout.LayoutParams(-2,dp(46)));
        root.addView(bar,new LinearLayout.LayoutParams(-1,-2));

        TextView status=text(description(route),14,MUTED,false); status.setPadding(0,dp(12),0,dp(16)); root.addView(status,new LinearLayout.LayoutParams(-1,-2));
        root.addView(infoCard(),new LinearLayout.LayoutParams(-1,-2));

        TextView linksTitle=text(rtl?"روابط هذه الصفحة":"Page links",17,TEXT,true); linksTitle.setPadding(0,dp(22),0,dp(10)); root.addView(linksTitle);
        for(String target: NavigationRegistry.links(route)){
            Button b=button(title(target),accent(target)); b.setTag("nav:"+target); b.setContentDescription("nav:"+target); b.setOnClickListener(v->open(target));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(54)); lp.bottomMargin=dp(10); root.addView(b,lp);
        }
        return scroll;
    }

    private View infoCard(){
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(16),dp(16),dp(16),dp(16)); box.setBackground(panel(CARD,accent(route)));
        box.setLayoutDirection(rtl?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);
        box.addView(text(rtl?"الحالة: متصل بنظام التنقل الداخلي":"Status: connected to internal navigation",14,GREEN,true));
        TextView integrity=text(rtl?"Navigation Integrity: صالح":"Navigation Integrity: valid",13,GOLD,true); integrity.setPadding(0,dp(9),0,0); box.addView(integrity);
        TextView routeText=text("Route: "+route,12,MUTED,false); routeText.setTextDirection(View.TEXT_DIRECTION_LTR); routeText.setGravity(Gravity.START); routeText.setPadding(0,dp(9),0,0); box.addView(routeText);
        return box;
    }

    private void open(String target){ Intent i=new Intent(this,PageActivity.class); i.putExtra(EXTRA_ROUTE,target); startActivity(i); }
    private LinearLayout row(){ LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); l.setLayoutDirection(rtl?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR); return l; }
    private TextView text(String s,int sp,int color,boolean bold){ TextView t=new TextView(this); t.setText(s); t.setTextColor(color); t.setTextSize(sp); t.setTypeface(Typeface.DEFAULT,bold?Typeface.BOLD:Typeface.NORMAL); t.setTextDirection(rtl?View.TEXT_DIRECTION_RTL:View.TEXT_DIRECTION_LTR); t.setGravity((rtl?Gravity.END:Gravity.START)|Gravity.CENTER_VERTICAL); return t; }
    private Button button(String s,int color){ Button b=new Button(this); b.setText(s); b.setAllCaps(false); b.setTextColor(TEXT); b.setTextSize(14); b.setTypeface(Typeface.DEFAULT,Typeface.BOLD); b.setBackground(panel(Color.rgb(16,27,46),color)); return b; }
    private GradientDrawable panel(int fill,int stroke){ GradientDrawable g=new GradientDrawable(); g.setColor(fill); g.setCornerRadius(dp(18)); g.setStroke(dp(1),stroke); return g; }
    private int accent(String r){ switch(r){ case NavigationRegistry.MESSAGES:return PURPLE; case NavigationRegistry.RADAR:return GREEN; case NavigationRegistry.MAP:return CYAN; case NavigationRegistry.DIAGNOSTICS:return GOLD; case NavigationRegistry.RADIO:return RED; case NavigationRegistry.PROFILE:return CYAN; case NavigationRegistry.CHANNELS:return PURPLE; case NavigationRegistry.SETTINGS:return GOLD; default:return GREEN; } }
    private String title(String r){
        if(rtl){ switch(r){ case NavigationRegistry.MESSAGES:return "المحادثات"; case NavigationRegistry.RADAR:return "الرادار"; case NavigationRegistry.MAP:return "الخريطة"; case NavigationRegistry.DIAGNOSTICS:return "التشخيص"; case NavigationRegistry.RADIO:return "الراديو"; case NavigationRegistry.PROFILE:return "الملف الميداني"; case NavigationRegistry.CHANNELS:return "القنوات"; case NavigationRegistry.SETTINGS:return "الإعدادات"; case NavigationRegistry.ABOUT:return "حول التطبيق"; default:return "الرئيسية"; } }
        switch(r){ case NavigationRegistry.MESSAGES:return "Messages"; case NavigationRegistry.RADAR:return "Radar"; case NavigationRegistry.MAP:return "Map"; case NavigationRegistry.DIAGNOSTICS:return "Diagnostics"; case NavigationRegistry.RADIO:return "Radio"; case NavigationRegistry.PROFILE:return "Field profile"; case NavigationRegistry.CHANNELS:return "Channels"; case NavigationRegistry.SETTINGS:return "Settings"; case NavigationRegistry.ABOUT:return "About"; default:return "Home"; }
    }
    private String description(String r){ return rtl?"صفحة وظيفية مرتبطة فعليًا داخل تطبيق Yasli kurt. زر الرجوع يعيدك إلى الصفحة السابقة.":"Functional page connected inside Yasli kurt. Back returns to the previous page."; }
    private int dp(int n){ return Math.round(n*getResources().getDisplayMetrics().density); }
}
