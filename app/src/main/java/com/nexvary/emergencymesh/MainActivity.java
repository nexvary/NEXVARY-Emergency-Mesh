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
    private static final int CARD_2 = Color.rgb(14, 25, 43);
    private static final int TEXT = Color.rgb(231, 239, 248);
    private static final int MUTED = Color.rgb(150, 168, 190);
    private static final int CYAN = Color.rgb(0, 229, 255);
    private static final int BLUE = Color.rgb(55, 145, 255);
    private static final int PURPLE = Color.rgb(156, 77, 255);
    private static final int MAGENTA = Color.rgb(255, 67, 214);
    private static final int GREEN = Color.rgb(50, 255, 137);
    private static final int GOLD = Color.rgb(255, 196, 64);
    private static final int RED = Color.rgb(255, 63, 94);
    private static final String[] LANGS = {"ar", "en", "tr", "es", "de"};

    private Context strings;
    private boolean rtl;
    private String languageCode;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureWindowColors();
        configureLanguage();
        View screen = buildScreen();
        setContentView(screen);
        screen.post(this::applySystemBarAppearance);
    }

    private void configureWindowColors() {
        Window w = getWindow();
        w.setStatusBarColor(BG);
        w.setNavigationBarColor(BG);
    }

    private void applySystemBarAppearance() {
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            View decor = getWindow().getDecorView();
            if (decor != null) {
                WindowInsetsController controller = decor.getWindowInsetsController();
                if (controller != null) {
                    controller.setSystemBarsAppearance(0,
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS |
                                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
                }
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(0);
        }
    }

    private void configureLanguage() {
        String code = getSharedPreferences("ui", MODE_PRIVATE).getString("language", "ar");
        languageCode = isSupported(code) ? code : "ar";
        Locale locale = new Locale(languageCode);
        Configuration cfg = new Configuration(getResources().getConfiguration());
        cfg.setLocale(locale);
        cfg.setLayoutDirection(locale);
        strings = createConfigurationContext(cfg);
        rtl = "ar".equals(locale.getLanguage());
    }

    private boolean isSupported(String code) {
        if (code == null) return false;
        for (String value : LANGS) if (value.equals(code)) return true;
        return false;
    }

    private View buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(14), dp(18), dp(30));
        root.setLayoutDirection(rtl ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));

        LinearLayout top = row();
        LinearLayout brandBox = new LinearLayout(this);
        brandBox.setOrientation(LinearLayout.VERTICAL);
        brandBox.setLayoutDirection(root.getLayoutDirection());
        TextView brand = text(R.string.brand, 29, TEXT, true);
        brand.setLetterSpacing(0.10f);
        TextView stage = text(R.string.stage_label, 11, GOLD, true);
        stage.setPadding(0, dp(2), 0, 0);
        brandBox.addView(brand);
        brandBox.addView(stage);
        top.addView(brandBox, weighted());
        Button lang = compactButton(languageCode.toUpperCase(Locale.ROOT), PURPLE);
        lang.setOnClickListener(v -> cycleLanguage());
        top.addView(lang, wrap());
        root.addView(top, matchWrap());

        TextView title = text(R.string.title, 27, CYAN, true);
        title.setPadding(0, dp(14), 0, 0);
        root.addView(title, matchWrap());

        TextView subtitle = text(R.string.subtitle, 14, MUTED, false);
        subtitle.setPadding(0, dp(5), 0, dp(12));
        root.addView(subtitle, matchWrap());

        TextView badge = plain(s(R.string.offline_badge), 11, GREEN, true);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(panel(Color.rgb(7, 37, 31), GREEN, 18, 1));
        badge.setPadding(dp(10), dp(9), dp(10), dp(9));
        LinearLayout.LayoutParams badgeLp = matchWrap();
        badgeLp.bottomMargin = dp(14);
        root.addView(badge, badgeLp);

        addTelemetry(root);
        addIdentity(root);
        addTransportHealth(root);
        addSos(root);
        addQuickAccess(root);
        addChannels(root);
        addRecentActivity(root);

        TextView verified = plain(s(R.string.ci_verified), 11, CYAN, true);
        verified.setGravity(Gravity.CENTER);
        verified.setPadding(0, dp(24), 0, dp(3));
        root.addView(verified, matchWrap());

        TextView ready = text(R.string.status_ready, 14, GREEN, true);
        ready.setGravity(Gravity.CENTER);
        ready.setPadding(0, dp(3), 0, dp(8));
        root.addView(ready, matchWrap());
        return scroll;
    }

    private void addTelemetry(LinearLayout root) {
        LinearLayout statRow1 = row();
        statRow1.addView(statCard("12", s(R.string.nodes), CYAN), weightedWithMargin(5));
        statRow1.addView(statCard("0", s(R.string.pending), PURPLE), weightedWithMargin(5));
        root.addView(statRow1, matchWrap());

        LinearLayout statRow2 = row();
        statRow2.addView(statCard("10", s(R.string.trusted), GREEN), weightedWithMargin(5));
        statRow2.addView(statCard("READY", s(R.string.radio), GOLD), weightedWithMargin(5));
        LinearLayout.LayoutParams lp = matchWrap();
        lp.topMargin = dp(9);
        root.addView(statRow2, lp);
    }

    private void addIdentity(LinearLayout root) {
        LinearLayout box = sectionCard(CYAN);
        box.addView(text(R.string.field_identity, 17, CYAN, true));
        box.addView(keyValue(R.string.callsign, R.string.callsign_value, MAGENTA));
        box.addView(keyValue(R.string.role, R.string.role_value, GREEN));
        box.addView(keyValue(R.string.network_id, R.string.network_id_value, GOLD));
        box.addView(keyValue(R.string.encryption, R.string.encryption_value, CYAN));
        addSection(root, box, 14);
    }

    private void addTransportHealth(LinearLayout root) {
        LinearLayout box = sectionCard(BLUE);
        box.addView(text(R.string.transport_health, 17, BLUE, true));
        box.addView(healthRow("◉", R.string.bluetooth, R.string.available, GREEN));
        box.addView(healthRow("⌁", R.string.wifi_direct, R.string.standby, GOLD));
        box.addView(healthRow("⌁", R.string.meshtastic, R.string.not_connected, PURPLE));
        addSection(root, box, 12);
    }

    private void addSos(LinearLayout root) {
        LinearLayout sos = sectionCard(RED);
        sos.setBackground(panel(Color.rgb(44, 8, 18), RED, 22, 2));
        TextView sosTitle = text(R.string.sos, 23, RED, true);
        TextView sosHint = text(R.string.sos_hint, 14, TEXT, false);
        sosHint.setPadding(0, dp(5), 0, dp(13));
        Button sosButton = actionButton("SOS", RED);
        sosButton.setOnClickListener(v -> Toast.makeText(this, s(R.string.toast_sos), Toast.LENGTH_LONG).show());
        sos.addView(sosTitle);
        sos.addView(sosHint);
        sos.addView(sosButton, matchHeight(dp(54)));
        addSection(root, sos, 12);
    }

    private void addQuickAccess(LinearLayout root) {
        TextView section = text(R.string.quick_access, 17, TEXT, true);
        section.setPadding(0, dp(20), 0, dp(10));
        root.addView(section, matchWrap());

        LinearLayout actions1 = row();
        actions1.addView(actionCard("✦", s(R.string.messages), MAGENTA), weightedWithMargin(5));
        actions1.addView(actionCard("◉", s(R.string.radar), GREEN), weightedWithMargin(5));
        root.addView(actions1, matchWrap());
        LinearLayout actions2 = row();
        actions2.addView(actionCard("⌖", s(R.string.map), CYAN), weightedWithMargin(5));
        actions2.addView(actionCard("≋", s(R.string.diagnostics), GOLD), weightedWithMargin(5));
        LinearLayout.LayoutParams lp = matchWrap(); lp.topMargin = dp(9); root.addView(actions2, lp);
    }

    private void addChannels(LinearLayout root) {
        LinearLayout box = sectionCard(PURPLE);
        box.addView(text(R.string.messages, 17, PURPLE, true));
        box.addView(channelRow("✚", R.string.rescue_channel, RED));
        box.addView(channelRow("✚", R.string.medical_channel, MAGENTA));
        box.addView(channelRow("⌂", R.string.shelter_channel, GOLD));
        box.addView(channelRow("◎", R.string.general_channel, CYAN));
        addSection(root, box, 16);
    }

    private void addRecentActivity(LinearLayout root) {
        LinearLayout box = sectionCard(GREEN);
        box.addView(text(R.string.recent_activity, 17, GREEN, true));
        box.addView(activityRow("●", R.string.activity_one, CYAN));
        box.addView(activityRow("●", R.string.activity_two, GREEN));
        box.addView(activityRow("●", R.string.activity_three, PURPLE));
        addSection(root, box, 12);
    }

    private LinearLayout sectionCard(int accent) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), dp(15), dp(16), dp(15));
        box.setBackground(panel(CARD, accent, 18, 1));
        box.setLayoutDirection(rtl ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        return box;
    }

    private void addSection(LinearLayout root, View box, int topMargin) {
        LinearLayout.LayoutParams lp = matchWrap(); lp.topMargin = dp(topMargin); root.addView(box, lp);
    }

    private View keyValue(int keyRes, int valueRes, int accent) {
        LinearLayout line = row();
        line.setPadding(0, dp(9), 0, 0);
        TextView key = text(keyRes, 13, MUTED, false);
        TextView val = text(valueRes, 13, accent, true);
        line.addView(key, weighted());
        line.addView(val, wrap());
        return line;
    }

    private View healthRow(String icon, int labelRes, int statusRes, int accent) {
        LinearLayout line = row(); line.setPadding(0, dp(10), 0, 0);
        TextView ic = plain(icon, 18, accent, true); ic.setGravity(Gravity.CENTER); ic.setWidth(dp(30));
        TextView label = text(labelRes, 14, TEXT, true);
        TextView status = text(statusRes, 12, accent, true); status.setGravity(Gravity.CENTER);
        status.setPadding(dp(9), dp(4), dp(9), dp(4)); status.setBackground(panel(Color.rgb(17, 31, 49), accent, 12, 1));
        line.addView(ic, wrap()); line.addView(label, weighted()); line.addView(status, wrap());
        return line;
    }

    private View channelRow(String icon, int labelRes, int accent) {
        LinearLayout line = row(); line.setPadding(0, dp(10), 0, 0);
        TextView ic = plain(icon, 18, accent, true); ic.setGravity(Gravity.CENTER); ic.setWidth(dp(32));
        TextView label = text(labelRes, 14, TEXT, true);
        TextView arrow = plain(rtl ? "‹" : "›", 24, accent, true); arrow.setGravity(Gravity.CENTER);
        line.addView(ic, wrap()); line.addView(label, weighted()); line.addView(arrow, wrap());
        line.setOnClickListener(v -> Toast.makeText(this, s(R.string.toast_action), Toast.LENGTH_SHORT).show());
        return line;
    }

    private View activityRow(String icon, int labelRes, int accent) {
        LinearLayout line = row(); line.setPadding(0, dp(9), 0, 0);
        TextView dot = plain(icon, 10, accent, true); dot.setWidth(dp(24)); dot.setGravity(Gravity.CENTER);
        line.addView(dot, wrap()); line.addView(text(labelRes, 13, TEXT, false), weighted());
        return line;
    }

    private LinearLayout statCard(String value, String label, int accent) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL); card.setGravity(Gravity.CENTER);
        card.setPadding(dp(10), dp(15), dp(10), dp(15)); card.setBackground(panel(CARD_2, accent, 18, 1));
        TextView v = plain(value, value.length() > 5 ? 16 : 25, accent, true); v.setGravity(Gravity.CENTER);
        TextView l = plain(label, 12, MUTED, false); l.setGravity(Gravity.CENTER); l.setPadding(0, dp(4), 0, 0);
        card.addView(v); card.addView(l); return card;
    }

    private LinearLayout actionCard(String icon, String label, int accent) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL); card.setGravity(Gravity.CENTER);
        card.setPadding(dp(10), dp(15), dp(10), dp(15)); card.setBackground(panel(CARD_2, accent, 18, 1));
        TextView i = plain(icon, 27, accent, true); i.setGravity(Gravity.CENTER);
        TextView t = plain(label, 13, TEXT, true); t.setGravity(Gravity.CENTER); t.setPadding(0, dp(7), 0, 0);
        card.addView(i); card.addView(t);
        card.setOnClickListener(v -> Toast.makeText(this, s(R.string.toast_action), Toast.LENGTH_SHORT).show());
        return card;
    }

    private Button compactButton(String label, int accent) {
        Button b = new Button(this); b.setText(label); b.setTextColor(TEXT); b.setTextSize(12); b.setAllCaps(false);
        b.setMinWidth(dp(58)); b.setMinHeight(0); b.setPadding(dp(12), dp(4), dp(12), dp(4));
        b.setBackground(panel(Color.rgb(24, 16, 44), accent, 16, 1)); return b;
    }

    private Button actionButton(String label, int accent) {
        Button b = new Button(this); b.setText(label); b.setTextColor(Color.WHITE); b.setTextSize(20);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD); b.setAllCaps(false);
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.rgb(112, 0, 28), accent});
        g.setCornerRadius(dp(18)); g.setStroke(dp(1), Color.rgb(255, 120, 140)); b.setBackground(g); return b;
    }

    private TextView text(int res, int sp, int color, boolean bold) { return plain(s(res), sp, color, bold); }

    private TextView plain(String value, int sp, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(value); t.setTextColor(color); t.setTextSize(sp);
        t.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL); t.setLineSpacing(0f, 1.12f); align(t); return t;
    }

    private void align(TextView t) {
        t.setTextDirection(rtl ? View.TEXT_DIRECTION_RTL : View.TEXT_DIRECTION_LTR);
        t.setGravity((rtl ? Gravity.END : Gravity.START) | Gravity.CENTER_VERTICAL);
    }

    private LinearLayout row() {
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL);
        row.setLayoutDirection(rtl ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR); return row;
    }

    private GradientDrawable panel(int fill, int stroke, int radiusDp, int strokeDp) {
        GradientDrawable g = new GradientDrawable(); g.setColor(fill); g.setCornerRadius(dp(radiusDp)); g.setStroke(dp(strokeDp), stroke); return g;
    }

    private void cycleLanguage() {
        int index = 0;
        for (int i = 0; i < LANGS.length; i++) if (LANGS[i].equals(languageCode)) index = i;
        String next = LANGS[(index + 1) % LANGS.length];
        getSharedPreferences("ui", MODE_PRIVATE).edit().putString("language", next).apply(); recreate();
    }

    private String s(int id) { return strings.getString(id); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private LinearLayout.LayoutParams matchWrap() { return new LinearLayout.LayoutParams(-1, -2); }
    private LinearLayout.LayoutParams matchHeight(int height) { return new LinearLayout.LayoutParams(-1, height); }
    private LinearLayout.LayoutParams weighted() { return new LinearLayout.LayoutParams(0, -2, 1f); }
    private LinearLayout.LayoutParams weightedWithMargin(int marginDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1f); lp.setMarginStart(dp(marginDp)); lp.setMarginEnd(dp(marginDp)); return lp;
    }
    private LinearLayout.LayoutParams wrap() { return new LinearLayout.LayoutParams(-2, -2); }
}
