package com.quanglewangle.peter.shoppingBeta;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class AndroidTabAndListView extends TabActivity {
    private static final String INBOX_SPEC = "Cupboard";
    private static final String OUTBOX_SPEC = "List";
    private static final String PROFILE_SPEC = "Basket";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        TabHost tabHost = getTabHost();

        TabSpec inboxSpec = tabHost.newTabSpec(INBOX_SPEC);
        inboxSpec.setIndicator(INBOX_SPEC, getResources().getDrawable(R.drawable.icon_inbox));
        inboxSpec.setContent(new Intent(this, CupboardActivity.class));

        TabSpec outboxSpec = tabHost.newTabSpec(OUTBOX_SPEC);
        outboxSpec.setIndicator(OUTBOX_SPEC, getResources().getDrawable(R.drawable.icon_outbox));
        outboxSpec.setContent(new Intent(this, ListActivity.class));

        TabSpec profileSpec = tabHost.newTabSpec(PROFILE_SPEC);
        profileSpec.setIndicator(PROFILE_SPEC, getResources().getDrawable(R.drawable.icon_profile));
        profileSpec.setContent(new Intent(this, BasketActivity.class));

        tabHost.addTab(inboxSpec);
        tabHost.addTab(outboxSpec);
        tabHost.addTab(profileSpec);

        // Android 16+ forces edge-to-edge and ignores windowActionBarOverlay, so the
        // action bar overlays the tab widget. Apply top padding manually to push tabs down.
        if (Build.VERSION.SDK_INT >= 36) {
            LinearLayout tabsRoot = (LinearLayout) tabHost.getChildAt(0);
            tabsRoot.setOnApplyWindowInsetsListener((v, insets) -> {
                TypedValue tv = new TypedValue();
                int actionBarHeight = 0;
                if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                    actionBarHeight = TypedValue.complexToDimensionPixelSize(
                            tv.data, getResources().getDisplayMetrics());
                }
                v.setPadding(0, insets.getSystemWindowInsetTop() + actionBarHeight, 0, 0);
                return insets;
            });
            tabsRoot.requestApplyInsets();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateActionBarColor();
    }

    private void updateActionBarColor() {
        if (getActionBar() != null) {
            boolean qsm = isQuickShopMode();
            getActionBar().setBackgroundDrawable(new ColorDrawable(
                qsm ? Color.parseColor("#4CAF50") : Color.parseColor("#333333")));
            getActionBar().setTitle(qsm ? "Shopping5  Quick Shop" : "Shopping5");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.quick_shop_mode).setChecked(isQuickShopMode());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.quick_shop_mode) {
            boolean newState = !item.isChecked();
            item.setChecked(newState);
            setQuickShopMode(newState);
            updateActionBarColor();
            return true;
        }
        if (item.getItemId() == R.id.clear_all_quick_shop) {
            new LoadURL(result -> {}).execute(new String[]{Constants.CLEAR_ALL_QUICK_SHOP});
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isQuickShopMode() {
        return getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
                .getBoolean(Constants.PREF_QUICK_SHOP_MODE, false);
    }

    private void setQuickShopMode(boolean value) {
        getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE).edit()
                .putBoolean(Constants.PREF_QUICK_SHOP_MODE, value).apply();
    }
}
