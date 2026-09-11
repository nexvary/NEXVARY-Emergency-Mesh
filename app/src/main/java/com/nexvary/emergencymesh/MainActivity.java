package com.nexvary.emergencymesh;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public final class MainActivity extends Activity {
    private static final int BG = Color.rgb(5, 9, 19);
    private static final int CARD = Color.rgb(11, 20, 34);
    private static final int TEXT = Color.rgb(231, 239, 248);
    private static final int MUTED = Color.rgb(150, 168, 190);
    private static final int CYAN = Color.rgb(0, 229, 255);
    private static final int PURPLE = Color.rgb(156, 77, 255);
    private static final int GREEN = Color.rgb(50, 255, 137);
    private static final int GOLD = Color.rgb(255, 196, 64);
    private static final int RED = Color.rgb(255, 63, 94);

    private Context strings;
    private boolean rtl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureWindow();
        configureLanguage();
        setContentView(buildScreen());
    }

    private void configureWindow() {
        Window w = getWindow();
        w.setStatusBarColor(BG);
        w.setNavigationBarColor(BG);
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController c = w.getInsetsController();
            if (c != null) c.setSystemBarsAppearance(0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS |
                            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
        }
    }

    private void configureLanguage() {
        String code = getSharedPreferences("ui", MODE_PRIVATE).getString("language", "ar");
        Locale locale = new Locale(code == null ? "ar" : code);
        Configuration cfg = new Configuration(getResources().getConfiguration());
        cfg.setLocale(locale);
        cfg.setLayoutDirection(locale);
        strings = createConfigurationContext(cfg);
        rtl = "ar".equals(locale.getLanguage());
    }

    private View buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(16), dp(20), dp(28));
        root.setLayoutDirection(rtl ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        scroll.addView(root, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT));

        LinearLayout top = row();
        TextView brand = text(R.string.brand, 32, TEXT, true);
        brand.setLetterSpacing(0.12f);
        top.addView(brand, weighted());
        Button lang = compactButton(s(R.string.language), PURPLE);
        lang.setOnClickListener(v -> toggleLanguage());
        top.addView(lang, wrap());
        root.addView(top, matchWrap());

        TextView title = text(R.string.title, 25, CYAN, true);
        title.setPadding(0, dp(8), 0, 0);
        root.addView(title, matchWrap());

        TextView subtitle = text(R.string.subtitle, 15, MUTED, false);
        subtitle.setPadding(0, dp(5), 0, dp(14));
        root.addView(subtitle, matchWrap());

        TextView badge = plain(s(R.string.offline_badge), 12, GREEN, true);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(panel(Color.rgb(7, 37, 31), GREEN, 18, 1));
        badge.setPadding(dp(10), dp(9), dp(10), dp(9));
        LinearLayout.LayoutParams badgeLp = matchWrap();
        badgeLp.bottomMargin = dp(16);
        root.addView(badge, badgeLp);

        LinearLayout statRow1 = row();
        statRow1.addView(statCard("12", s(R.string.nodes), CYAN), weightedWithMargin(6));
        statRow1.addView(statCard("0", s(R.string.pending), PURPLE), weightedWithMargin(6));
        root.addView(statRow1, matchWrap());

        LinearLayout statRow2 = row();
        statRow2.addView(statCard("10", s(R.string.trusted), GREEN), weightedWithMargin(6));
        statRow2.addView(statCard("READY", s(R.string.radio), GOLD), weightedWithMargin(6));
        LinearLayout.LayoutParams stat2Lp = matchWrap();
        stat2Lp.topMargin = dp(10);
        root.addView(statRow2, stat2Lp);

        LinearLayout status = new LinearLayout(this);
        status.setOrientation(LinearLayout.VERTICAL);
        status.setPadding(dp(16), dp(15), dp(16), dp(15));
        status.setBackground(panel(CARD, CYAN, 18, 1));
        status.setLayoutDirection(root.getLayoutDirection());
        TextView st = text(R.string.mesh_status, 15, CYAN, true);
        TextView sv = text(R.string.mesh_status_value, 14, TEXT, false);
        sv.setPadding(0, dp(5), 0, 0);
        status.addView(st);
        status.addView(sv);
        LinearLayout.LayoutParams statusLp = matchWrap();
        statusLp.topMargin = dp(16);
        root.addView(status, statusLp);

        LinearLayout sos = new LinearLayout(this);
        sos.setOrientation(LinearLayout.VERTICAL);
        sos.setPadding(dp(18), dp(18), dp(18), dp(18));
        sos.setBackground(panel(Color.rgb(44, 8, 18), RED, 22, 2));
        sos.setLayoutDirection(root.getLayoutDirection());
        TextView sosTitle = text(R.string.sos, 24, RED, true);
        TextView sosHint = text(R.string.sos_hint, 14, TEXT, false);
        sosHint.setPadding(0, dp(5), 0, dp(13));
        Button sosButton = actionButton("SOS", RED);
        sosButton.setOnClickListener(v -> Toast.makeText(this, s(R.string.toast_sos), Toast.LENGTH_LONG).show());
        sos.addView(sosTitle);
        sos.addView(sosHint);
        sos.addView(sosButton, matchHeight(dp(54)));
        LinearLayout.LayoutParams sosLp = matchWrap();
        sosLp.topMargin = dp(16);
        root.addView(sos, sosLp);

        TextView section = plain(rtl ? "الوصول السريع" : "Quick access", 17, TEXT, true);
        section.setPadding(0, dp(22), 0, dp(10));
        align(section);
        root.addView(section, matchWrap());

        LinearLayout actions1 = row();
        actions1.addView(actionCard("✦", s(R.string.messages), PURPLE), weightedWithMargin(6));
        actions1.addView(actionCard("◉", s(R.string.radar), GREEN), weightedWithMargin(6));
        root.addView(actions1, matchWrap());

        LinearLayout actions2 = row();
        actions2.addView(actionCard("⌖", s(R.string.map), CYAN), weightedWithMargin(6));
        actions2.addView(actionCard("≋", s(R.string.diagnostics), GOLD), weightedWithMargin(6));
        LinearLayout.LayoutParams actions2Lp = matchWrap();
        actions2Lp.topMargin = dp(10);
        root.addView(actions2, actions2Lp);

        TextView ready = text(R.string.status_ready, 15, GREEN, true);
        ready.setGravity(Gravity.CENTER);
        ready.setPadding(0, dp(24), 0, dp(8));
        root.addView(ready, matchWrap());

        return scroll;
    }

    private LinearLayout statCard(String value, String label, int accent) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(10), dp(16), dp(10), dp(16));
        card.setBackground(panel(CARD, accent, 18, 1));
        TextView v = plain(value, value.length() > 5 ? 17 : 26, accent, true);
        v.setGravity(Gravity.CENTER);
        TextView l = plain(label, 13, MUTED, false);
        l.setGravity(Gravity.CENTER);
        l.setPadding(0, dp(5), 0, 0);
        card.addView(v);
        card.addView(l);
        return card;
    }

    private LinearLayout actionCard(String icon, String label, int accent) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(10), dp(15), dp(10), dp(15));
        card.setBackground(panel(CARD, accent, 18, 1));
        TextView i = plain(icon, 28, accent, true);
        i.setGravity(Gravity.CENTER);
        TextView t = plain(label, 14, TEXT, true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, dp(7), 0, 0);
        card.addView(i);
        card.addView(t);
        card.setOnClickListener(v -> Toast.makeText(this, s(R.string.toast_action), Toast.LENGTH_SHORT).show());
        return card;
    }

    private Button compactButton(String label, int accent) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(TEXT);
        b.setTextSize(13);
        b.setAllCaps(false);
        b.setMinWidth(dp(62));
        b.setMinHeight(0);
        b.setPadding(dp(13), dp(4), dp(13), dp(4));
        b.setBackground(panel(Color.rgb(24, 16, 44), accent, 16, 1));
        return b;
    }

    private Button actionButton(String label, int accent) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(Color.WHITE);
        b.setTextSize(20);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setAllCaps(false);
        GradientDrawable g = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.rgb(112, 0, 28), accent});
        g.setCornerRadius(dp(18));
        g.setStroke(dp(1), Color.rgb(255, 120, 140));
        b.setBackground(g);
        return b;
    }

    private TextView text(int res, int sp, int color, boolean bold) {
        return plain(s(res), sp, color, bold);
    }

    private TextView plain(String value, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextColor(color);
        t.setTextSize(sp);
        t.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        t.setLineSpacing(0f, 1.12f);
        align(t);
        return t;
    }

    private void align(TextView t) {
        t.setTextDirection(rtl ? View.TEXT_DIRECTION_RTL : View.TEXT_DIRECTION_LTR);
        t.setGravity((rtl ? Gravity.END : Gravity.START) | Gravity.CENTER_VERTICAL);
    }

    private LinearLayout row() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setLayoutDirection(rtl ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        return row;
    }

    private GradientDrawable panel(int fill, int stroke, int radiusDp, int strokeDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(radiusDp));
        g.setStroke(dp(strokeDp), stroke);
        return g;
    }

    private void toggleLanguage() {
        String next = rtl ? "en" : "ar";
        getSharedPreferences("ui", MODE_PRIVATE).edit().putString("language", next).apply();
        recreate();
    }

    private String s(int id) {
        return strings.getString(id);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams matchHeight(int height) {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
    }

    private LinearLayout.LayoutParams weighted() {
        return new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
    }

    private LinearLayout.LayoutParams weightedWithMargin(int marginDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMarginStart(dp(marginDp));
        lp.setMarginEnd(dp(marginDp));
        return lp;
    }

    private LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }
}
