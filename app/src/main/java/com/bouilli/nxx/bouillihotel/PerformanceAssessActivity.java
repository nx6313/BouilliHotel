package com.bouilli.nxx.bouillihotel;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.asyncTask.InitMonthTurnoverTask;
import com.bouilli.nxx.bouillihotel.asyncTask.InitUserTurnoverTask;
import com.bouilli.nxx.bouillihotel.util.ComFun;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executors;

public class PerformanceAssessActivity extends AppCompatActivity {
    public static Handler mHandler = null;
    public static final int MSG_INIT_USER_TURNOVER = 1;
    private WebView wbPerformanceWeekReport;
    private WebView wbPerformanceMonthReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performance);

        mHandler = new PerformanceAssessActivity.mHandler();

        CookieManager.getInstance().setAcceptCookie(true);

        wbPerformanceWeekReport = (WebView) findViewById(R.id.wbPerformanceWeekReport);

        wbPerformanceWeekReport.getSettings().setJavaScriptEnabled(true);
        wbPerformanceWeekReport.getSettings().setAllowFileAccess(true);
        wbPerformanceWeekReport.getSettings().setNeedInitialFocus(false);
        wbPerformanceWeekReport.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        wbPerformanceWeekReport.setWebViewClient(new MyWebViewClient());

        wbPerformanceWeekReport.loadUrl("file:///android_asset/echart/line.html");


        wbPerformanceMonthReport = (WebView) findViewById(R.id.wbPerformanceMonthReport);

        wbPerformanceMonthReport.getSettings().setJavaScriptEnabled(true);
        wbPerformanceMonthReport.getSettings().setAllowFileAccess(true);
        wbPerformanceMonthReport.getSettings().setNeedInitialFocus(false);
        wbPerformanceMonthReport.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        wbPerformanceMonthReport.setWebViewClient(new MyWebViewClient());

        wbPerformanceMonthReport.loadUrl("file:///android_asset/echart/line.html");

        setupActionBar();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            PerformanceAssessActivity.this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            new InitUserTurnoverTask(PerformanceAssessActivity.this).executeOnExecutor(Executors.newCachedThreadPool());
        }
    }

    class mHandler extends Handler {
        public mHandler() {
        }

        public mHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            switch (msg.what) {
                case MSG_INIT_USER_TURNOVER:
                    // 隐藏加载动画
                    String initUserTurnoverResult = b.getString("initUserTurnoverResult");
                    if (initUserTurnoverResult.equals("true")) {
                        // 周的数据
                        if(b.containsKey("weakUserDataJson")){
                            String weakUserDataJson = b.getString("weakUserDataJson");
                            try {
                                JSONObject js = new JSONObject(weakUserDataJson);
                                wbPerformanceWeekReport.loadUrl("javascript:setData("+js.get("lineRoot")+");");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        // 月的数据
                        if(b.containsKey("monthUserDataJson")){
                            String monthUserDataJson = b.getString("monthUserDataJson");
                            try {
                                JSONObject js = new JSONObject(monthUserDataJson);
                                wbPerformanceMonthReport.loadUrl("javascript:setData("+js.get("lineRoot")+");");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }else if (initUserTurnoverResult.equals("false")) {
                        ComFun.showToast(PerformanceAssessActivity.this, "初始化员工个人业绩数据失败，请联系管理员", Toast.LENGTH_SHORT);
                    }else if (initUserTurnoverResult.equals("time_out")) {
                        ComFun.showToast(PerformanceAssessActivity.this, "初始化员工个人业绩数据超时，请稍后重试", Toast.LENGTH_SHORT);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }
}
