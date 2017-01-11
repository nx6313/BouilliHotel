package com.bouilli.nxx.bouillihotel;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.asyncTask.InitMonthTurnoverTask;
import com.bouilli.nxx.bouillihotel.util.ComFun;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executors;

public class BusinessActivity extends AppCompatActivity {
    public static Handler mHandler = null;
    public static final int MSG_INIT_MONTH_TURNOVER = 1;
    private WebView wbMonthReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business);

        mHandler = new BusinessActivity.mHandler();

        wbMonthReport = (WebView) findViewById(R.id.wbMonthReport);

        wbMonthReport.getSettings().setJavaScriptEnabled(true);
        wbMonthReport.getSettings().setAllowFileAccess(true);
        CookieManager.getInstance().setAcceptCookie(true);
        wbMonthReport.getSettings().setNeedInitialFocus(false);
        wbMonthReport.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        wbMonthReport.setWebViewClient(new MyWebViewClient());

        wbMonthReport.loadUrl("file:///android_asset/echart/line.html");

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
            BusinessActivity.this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            new InitMonthTurnoverTask(BusinessActivity.this).executeOnExecutor(Executors.newCachedThreadPool());
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
                case MSG_INIT_MONTH_TURNOVER:
                    // 隐藏加载动画
                    String initMonthTurnoverResult = b.getString("initMonthTurnoverResult");
                    if (initMonthTurnoverResult.equals("true")) {
                        if(b.containsKey("dataJson")){
                            String dataJson = b.getString("dataJson");
                            try {
                                JSONObject js = new JSONObject(dataJson);
                                wbMonthReport.loadUrl("javascript:setData("+js.get("lineRoot")+");");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }else if (initMonthTurnoverResult.equals("false")) {
                        ComFun.showToast(BusinessActivity.this, "初始化月报表数据失败，请联系管理员", Toast.LENGTH_SHORT);
                    }else if (initMonthTurnoverResult.equals("time_out")) {
                        ComFun.showToast(BusinessActivity.this, "初始化月报表数据超时，请稍后重试", Toast.LENGTH_SHORT);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }
}
