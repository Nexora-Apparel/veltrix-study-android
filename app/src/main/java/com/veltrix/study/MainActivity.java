package com.veltrix.study;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends Activity {
    private static final int NOTIFICATION_PERMISSION_REQUEST = 41;
    private static final String HOME = "https://veltrix-study.hatchable.site";
    private WebView webView;
    private ProgressBar progress;
    private boolean reminderPermissionPending = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(5, 6, 7));
        getWindow().setNavigationBarColor(Color.rgb(5, 6, 7));
        getWindow().getDecorView().setSystemUiVisibility(0);
        ReminderReceiver.createChannel(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(5, 6, 7));

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.rgb(200, 241, 105)));
        progress.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(30, 34, 31)));
        root.addView(progress, new LinearLayout.LayoutParams(-1, dp(2)));

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(5, 6, 7));
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setMediaPlaybackRequiresUserGesture(true);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, false);
        webView.addJavascriptInterface(new ReminderBridge(), "VeltrixNative");

        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int value) {
                progress.setProgress(value);
                progress.setVisibility(value >= 100 ? View.GONE : View.VISIBLE);
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override public void onReceivedError(WebView view, WebResourceRequest request, android.webkit.WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()) showOfflinePage();
            }
            @Override public void onReceivedHttpError(WebView view, WebResourceRequest request, android.webkit.WebResourceResponse response) {
                super.onReceivedHttpError(view, request, response);
                if (request.isForMainFrame() && response.getStatusCode() >= 500) showOfflinePage();
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String host = uri.getHost() == null ? "" : uri.getHost();
                if (uri.getScheme().equals("https") && (host.equals("veltrix-study.hatchable.site") || host.endsWith(".hatchable.site"))) return false;
                if (uri.getScheme().equals("https") || uri.getScheme().equals("http") || uri.getScheme().equals("mailto") || uri.getScheme().equals("tel")) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); } catch (Exception ignored) {}
                    return true;
                }
                return true;
            }
            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url != null && url.startsWith("https://veltrix-study-offline.local/")) {
                    progress.setVisibility(View.GONE);
                } else {
                    installStudyReminderButton();
                }
            }
        });

        root.addView(webView, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        if (savedInstanceState != null && webView.restoreState(savedInstanceState) != null) {
            // Keep the current page and scroll position when Android recreates the activity.
        } else if (!hasInternet()) {
            showOfflinePage();
        } else {
            webView.loadUrl(HOME);
        }
    }

    private boolean hasInternet() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (manager == null) return false;
        android.net.Network network = manager.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities capabilities = manager.getNetworkCapabilities(network);
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    private void showOfflinePage() {
        if (webView == null) return;
        progress.setVisibility(View.GONE);
        webView.stopLoading();
        try (InputStream input = getAssets().open("offline.html");
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            String page = output.toString("UTF-8");
            webView.loadDataWithBaseURL("https://veltrix-study-offline.local/", page, "text/html", "UTF-8", null);
        } catch (Exception error) {
            Toast.makeText(this, "Your offline study page could not be opened.", Toast.LENGTH_LONG).show();
        }
    }

    private void retryOnline() {
        if (!hasInternet()) {
            Toast.makeText(this, "You're still offline. Your study plan is ready here.", Toast.LENGTH_SHORT).show();
            return;
        }
        webView.loadUrl(HOME);
    }

    private void installStudyReminderButton() {
        String script = "(function(){if(document.getElementById('veltrix-reminder-launch'))return;" +
            "var b=document.createElement('button');b.id='veltrix-reminder-launch';b.type='button';b.textContent='◷  Study reminder';" +
            "b.setAttribute('aria-label','Set a study reminder');" +
            "b.style.cssText='position:fixed;z-index:2147483000;right:16px;bottom:88px;border:1px solid rgba(200,241,105,.35);border-radius:999px;padding:12px 16px;background:rgba(18,22,19,.96);color:#d7f69a;font:600 13px Inter,system-ui,sans-serif;letter-spacing:.1px;box-shadow:0 8px 28px rgba(0,0,0,.28);-webkit-tap-highlight-color:transparent;';" +
            "b.onclick=function(){if(window.VeltrixNative)window.VeltrixNative.openReminders();};document.body.appendChild(b);" +
            "var s=document.createElement('style');s.textContent='html{scroll-behavior:smooth;overscroll-behavior-y:contain}body{-webkit-overflow-scrolling:touch;scrollbar-width:none}*{-webkit-tap-highlight-color:transparent}';document.head.appendChild(s);" +
            "})();";
        webView.evaluateJavascript(script, null);
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    private class ReminderBridge {
        @android.webkit.JavascriptInterface public void openReminders() {
            runOnUiThread(() -> showReminderSettings());
        }
        @android.webkit.JavascriptInterface public void retryOnline() {
            runOnUiThread(() -> retryOnline());
        }
    }

    private void showReminderSettings() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            reminderPermissionPending = true;
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST);
            return;
        }

        int savedHour = getPreferences(MODE_PRIVATE).getInt("reminder_hour", 18);
        int savedMinute = getPreferences(MODE_PRIVATE).getInt("reminder_minute", 0);
        boolean[] savedDays = ReminderReceiver.readDays(this);
        final int[] time = {savedHour, savedMinute};

        LinearLayout panel = new LinearLayout(this);
        panel.setPadding(dp(22), dp(12), dp(22), dp(8));
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackgroundColor(Color.rgb(17, 20, 18));

        TextView title = new TextView(this);
        title.setText("Make time to study");
        title.setTextColor(Color.WHITE);
        title.setTextSize(21);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        panel.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Choose when you’d like VELTRIX to remind you.");
        subtitle.setTextColor(Color.rgb(170, 178, 169));
        subtitle.setTextSize(13);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(-1, -2);
        subParams.topMargin = dp(6);
        subParams.bottomMargin = dp(18);
        panel.addView(subtitle, subParams);

        Button timeButton = new Button(this);
        timeButton.setText(String.format(java.util.Locale.getDefault(), "Reminder time  ·  %02d:%02d", time[0], time[1]));
        timeButton.setTextColor(Color.rgb(214, 244, 158));
        timeButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(38, 48, 35)));
        panel.addView(timeButton, new LinearLayout.LayoutParams(-1, dp(52)));
        timeButton.setOnClickListener(v -> new TimePickerDialog(this, (picker, hour, minute) -> {
            time[0] = hour; time[1] = minute;
            timeButton.setText(String.format(java.util.Locale.getDefault(), "Reminder time  ·  %02d:%02d", hour, minute));
        }, time[0], time[1], true).show());

        TextView daysTitle = new TextView(this);
        daysTitle.setText("REPEAT ON");
        daysTitle.setTextColor(Color.rgb(160, 170, 158));
        daysTitle.setTextSize(11);
        daysTitle.setLetterSpacing(.12f);
        LinearLayout.LayoutParams dayTitleParams = new LinearLayout.LayoutParams(-1, -2);
        dayTitleParams.topMargin = dp(20);
        dayTitleParams.bottomMargin = dp(8);
        panel.addView(daysTitle, dayTitleParams);

        String[] names = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        CheckBox[] checks = new CheckBox[7];
        for (int i = 0; i < 7; i++) {
            checks[i] = new CheckBox(this);
            checks[i].setText(names[i]);
            checks[i].setTextColor(Color.rgb(232, 235, 229));
            checks[i].setButtonTintList(new android.content.res.ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                new int[]{Color.rgb(200, 241, 105), Color.rgb(115, 125, 113)}));
            checks[i].setChecked(savedDays[i]);
            checks[i].setPadding(0, 0, 0, 0);
            panel.addView(checks[i], new LinearLayout.LayoutParams(-1, dp(38)));
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(panel)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save reminder", null)
            .setNeutralButton("Turn off", null)
            .create();
        dialog.setOnShowListener(ignored -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.rgb(120, 160, 55));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.rgb(180, 185, 177));
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.rgb(210, 130, 120));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                boolean[] days = new boolean[7];
                boolean any = false;
                for (int i = 0; i < 7; i++) { days[i] = checks[i].isChecked(); any |= days[i]; }
                if (!any) {
                    Toast.makeText(this, "Choose at least one day.", Toast.LENGTH_SHORT).show();
                    return;
                }
                getPreferences(MODE_PRIVATE).edit().putInt("reminder_hour", time[0]).putInt("reminder_minute", time[1]).apply();
                ReminderReceiver.saveAndSchedule(this, time[0], time[1], days);
                Toast.makeText(this, "Your study reminder is set.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                ReminderReceiver.cancel(this);
                Toast.makeText(this, "Study reminders are off.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && reminderPermissionPending) {
                reminderPermissionPending = false;
                showReminderSettings();
            } else {
                reminderPermissionPending = false;
                Toast.makeText(this, "Allow notifications to receive study reminders.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override protected void onResume() {
        super.onResume();
        if (webView != null && !hasInternet()) {
            String current = webView.getUrl();
            if (current == null || !current.startsWith("https://veltrix-study-offline.local/")) showOfflinePage();
        }
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        if (webView != null) webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        if (webView != null) { webView.stopLoading(); webView.removeJavascriptInterface("VeltrixNative"); webView.destroy(); }
        super.onDestroy();
    }
}
