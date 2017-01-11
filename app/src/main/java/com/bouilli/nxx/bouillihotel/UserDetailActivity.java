package com.bouilli.nxx.bouillihotel;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;

public class UserDetailActivity extends AppCompatActivity {
    private View view;
    private CollapsingToolbarLayout user_toolbar_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = View.inflate(this, R.layout.activity_user_detail, null);
        setContentView(view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 设置标题信息
        user_toolbar_layout = (CollapsingToolbarLayout) findViewById(R.id.user_toolbar_layout);
        String userLoginName = SharedPreferencesTool.getFromShared(UserDetailActivity.this, "BouilliProInfo", "userLoginName");
        String userDetailTitle;
        if(ComFun.strNull(userLoginName)){
            userDetailTitle = userLoginName;
        }else{
            userDetailTitle = "蜜糖丶小妖";
        }
        // 获取登录人的权限值
        String userPermission = SharedPreferencesTool.getFromShared(UserDetailActivity.this, "BouilliProInfo", "userPermission");
        if(ComFun.strNull(userPermission)){
            if(Integer.parseInt(userPermission) == 0){
                userDetailTitle += "(系统管理员)";
            }else if(Integer.parseInt(userPermission) == 1){
                userDetailTitle += "(副管理员)";
            }else if(Integer.parseInt(userPermission) == 2){
                userDetailTitle += "(员工)";
            }else if(Integer.parseInt(userPermission) == 3){
                userDetailTitle += "(传菜员)";
            }else if(Integer.parseInt(userPermission) == 4){
                userDetailTitle += "(后厨管理员)";
            }
        }else{
            userDetailTitle += "(测试账号)";
        }
        user_toolbar_layout.setTitle(userDetailTitle);
        user_toolbar_layout.setExpandedTitleColor(Color.parseColor("#181A1C"));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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
            UserDetailActivity.this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
