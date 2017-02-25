package com.bouilli.nxx.bouillihotel;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;

public class UserDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 设置标题信息
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

        setupActionBar();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
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
