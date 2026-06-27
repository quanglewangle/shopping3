package com.quanglewangle.peter.shoppingBeta;

import android.Manifest;
import android.app.TabActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import androidx.core.content.FileProvider;
import java.io.File;

public class AndroidTabAndListView extends TabActivity {
    private static final String INBOX_SPEC = "Cupboard";
    private static final String OUTBOX_SPEC = "List";
    private static final String PROFILE_SPEC = "Basket";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 2;
    private File cameraImageFile;

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
        if (Build.VERSION.SDK_INT >= 35) {
            LinearLayout tabsRoot = (LinearLayout) tabHost.getChildAt(0);
            TypedValue tv = new TypedValue();
            int actionBarHeight = 0;
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(
                        tv.data, getResources().getDisplayMetrics());
            }
            int statusBarHeight = 0;
            int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resId > 0) {
                statusBarHeight = getResources().getDimensionPixelSize(resId);
            }
            final int topPadding = statusBarHeight + actionBarHeight;
            tabsRoot.post(() -> tabsRoot.setPadding(0, topPadding, 0, 0));
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
            String version = "";
            try {
                version = " v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            } catch (Exception ignored) {}
            getActionBar().setTitle(qsm ? "Shopping  Quick Shop" + version : "Shopping" + version);
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
        boolean onCupboard = getTabHost().getCurrentTab() == 0;
        menu.findItem(R.id.add_item).setVisible(onCupboard);
        menu.findItem(R.id.scan_coupon).setVisible(onCupboard);
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
        if (item.getItemId() == R.id.add_item) {
            android.app.Activity current = getLocalActivityManager().getCurrentActivity();
            if (current instanceof CupboardActivity) {
                ((CupboardActivity) current).showAddItemDialog();
            }
            return true;
        }
        if (item.getItemId() == R.id.scan_coupon) {
            checkAndLaunchCamera();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkAndLaunchCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        }
    }

    private void launchCamera() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        cameraImageFile = new File(dir, "coupon.jpg");
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", cameraImageFile);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            android.app.Activity current = getLocalActivityManager().getCurrentActivity();
            if (current instanceof CupboardActivity) {
                ((CupboardActivity) current).processCouponPhoto(cameraImageFile);
            }
        }
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
