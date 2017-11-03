package com.bouilli.nxx.bouillihotel.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.EditOrderActivity;
import com.bouilli.nxx.bouillihotel.MainActivity;
import com.bouilli.nxx.bouillihotel.MyApplication;
import com.bouilli.nxx.bouillihotel.R;
import com.bouilli.nxx.bouillihotel.asyncTask.okHttpTask.AllRequestUtil;
import com.bouilli.nxx.bouillihotel.customview.FlowLayout;
import com.bouilli.nxx.bouillihotel.entity.TableGroup;
import com.bouilli.nxx.bouillihotel.entity.TableInfo;
import com.bouilli.nxx.bouillihotel.entity.build.TableGroupDao;
import com.bouilli.nxx.bouillihotel.entity.build.TableInfoDao;
import com.bouilli.nxx.bouillihotel.okHttpUtil.request.RequestParams;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.DisplayUtil;
import com.bouilli.nxx.bouillihotel.util.SerializableMap;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;

/**
 * Created by 18230 on 2016/11/5.
 */

public class MainFragment extends Fragment {
    int mNum;// 页号
    private RefDataBroadCastReceive refDataBroadCastReceive;// 刷新数据广播实例
    public static String MSG_REFDATA = "requestNewDataBouilliHotel";
    private FlowLayout dining_table_layout;// 存放所有桌子的容器实例

    public static MainFragment newInstance(int num, boolean nullFlag) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        if (!nullFlag) {
            args.putInt("num", num);
        } else {
            args.putInt("num", -1);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 注册广播接收器
        refDataBroadCastReceive = new RefDataBroadCastReceive();
        IntentFilter filter = new IntentFilter();
        filter.addAction("requestNewDataBouilliHotel");
        getActivity().registerReceiver(refDataBroadCastReceive, filter);
        mNum = getArguments() != null ? getArguments().getInt("num") : 1;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment_pager, null);
        dining_table_layout = (FlowLayout) view.findViewById(R.id.dining_table_layout);
        TableGroupDao tableGroupDao = MyApplication.getDaoSession().getTableGroupDao();
        List<TableGroup> tableGroupList = tableGroupDao.loadAll();
        if (ComFun.strNull(tableGroupList, true)) {
            TableInfoDao tableInfoDao = MyApplication.getDaoSession().getTableInfoDao();
            WhereCondition whereCondition = new WhereCondition.PropertyCondition(TableInfoDao.Properties.GroupCode, " = '" + tableGroupList.get(mNum).getTableGroupCode() + "'");
            List<TableInfo> tableInfos = tableInfoDao.queryBuilder().where(whereCondition).orderAsc(TableInfoDao.Properties.TableNo).list();
            String thisGroupTableInfo = SharedPreferencesTool.getFromShared(getActivity(), "BouilliTableInfo", "tableInfo" + tableGroupList.get(mNum).getTableGroupName());
            addTableView(tableInfos);
        } else {
            addTableViewForNull();
        }
        return view;
    }

    // 添加没有餐桌的标识
    private void addTableViewForNull() {
        LinearLayout tableObject = new LinearLayout(getContext());
        tableObject.setGravity(Gravity.CENTER);
        tableObject.setOrientation(LinearLayout.VERTICAL);
        tableObject.setFocusable(true);
        tableObject.setPadding(0, DisplayUtil.dip2px(getContext(), 140), 0, 0);
        LinearLayout.LayoutParams tableObjLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        tableObject.setLayoutParams(tableObjLp);

        ImageView noTableImg = new ImageView(getContext());
        ViewGroup.LayoutParams noTableImgLp = new ViewGroup.LayoutParams(DisplayUtil.dip2px(getContext(), 120), DisplayUtil.dip2px(getContext(), 120));
        noTableImg.setLayoutParams(noTableImgLp);
        noTableImg.setImageResource(R.drawable.msg_noitem);
        tableObject.addView(noTableImg);

        TextView noTableDes = new TextView(getContext());
        ViewGroup.LayoutParams noTableDesLp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        noTableDes.setLayoutParams(noTableDesLp);
        noTableDes.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        noTableDes.setText("暂无餐桌，请先添加");
        tableObject.addView(noTableDes);

        tableObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = new Message();
                msg.what = MainActivity.MSG_ADD_NEW_TABLES;
                MainActivity.mHandler.sendMessage(msg);
            }
        });

        dining_table_layout.addView(tableObject);
    }

    // 添加餐桌布局
    private void addTableView(List<TableInfo> tableInfos) {
        for (TableInfo tableInfo : tableInfos) {
            LinearLayout tableObject = new LinearLayout(getContext());
            tableObject.setTag("tableInfoKey_" + tableInfo.getId());
            tableObject.setGravity(Gravity.CENTER);
            tableObject.setOrientation(LinearLayout.VERTICAL);
            tableObject.setFocusable(true);
            LinearLayout.LayoutParams tableObjLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tableObjLp.setMargins(DisplayUtil.dip2px(getContext(), 10), DisplayUtil.dip2px(getContext(), 10), DisplayUtil.dip2px(getContext(), 10), DisplayUtil.dip2px(getContext(), 10));
            tableObject.setLayoutParams(tableObjLp);
            // ImageView
            ImageView tableImg = new ImageView(getContext());
            tableImg.setTag("tableImg_" + tableInfo.getId());
            tableImg.setTag(R.id.tag_table_state, tableInfo.getTableStatus());
            ViewGroup.LayoutParams tableImgLp = new ViewGroup.LayoutParams(DisplayUtil.dip2px(getContext(), 70), DisplayUtil.dip2px(getContext(), 70));
            tableImg.setLayoutParams(tableImgLp);
            if (tableInfo.getTableStatus() == 1) {// 空闲
                tableImg.setImageResource(R.drawable.desk_0);
            } else if (tableInfo.getTableStatus() == 2) {// 编辑
                tableImg.setImageResource(R.drawable.desk_2);
                // 闪烁显示动画
                AlphaAnimation aa1 = new AlphaAnimation(1.0f, 0.4f);
                aa1.setRepeatCount(-1);//设置重复次数
                aa1.setRepeatMode(Animation.REVERSE);//设置反方向执行
                aa1.setDuration(2300);
                aa1.setFillAfter(true);
                tableImg.startAnimation(aa1);
            } else {// 占用
                tableImg.setTag(R.id.tag_table_order_id, tableInfo.getId());
                tableImg.setImageResource(R.drawable.desk_1);
            }
            tableObject.addView(tableImg);
            // TextView
            TextView tableDes = new TextView(getContext());
            ViewGroup.LayoutParams tableDesLp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tableDes.setLayoutParams(tableDesLp);
            tableDes.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tableDes.setText(tableInfo.getTableName());
            tableDes.setTag("tableTxtInfo");
            tableDes.setTag(R.id.tag_table_txt_no, "tableTxtInfo_" + tableInfo.getTableNo());
            TextPaint tableDesTp = tableDes.getPaint();
            tableDesTp.setFakeBoldText(true);
            tableObject.addView(tableDes);
            dining_table_layout.addView(tableObject);
            // 只有员工可以进行点餐
            tableObject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int tableState = Integer.parseInt(((LinearLayout) v).getChildAt(0).getTag(R.id.tag_table_state).toString());
                    // 判断权限
                    String userPermission = SharedPreferencesTool.getFromShared(getActivity(), "BouilliProInfo", "userPermission");
                    if (ComFun.strNull(userPermission) && Integer.parseInt(userPermission) == 2) {
                        String tableDesInfo = ((TextView) ((LinearLayout) v).getChildAt(1)).getText().toString();
                        if (tableState == 2) {// 编辑中
                            ComFun.showToast(getActivity(), "该餐桌正在编辑中，选择别的餐桌吧", Toast.LENGTH_SHORT);
                        } else {
                            final ImageView clickTableImgView = (ImageView) ((LinearLayout) v).getChildAt(0);
                            changeLight(clickTableImgView, -80);
                            Handler tableImgHandler = new Handler();
                            tableImgHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    changeLight(clickTableImgView, 0);
                                }
                            }, 80);
                            if (tableState == 1) {// 空闲
                                Intent intentKongXian = new Intent(getActivity(), EditOrderActivity.class);
                                intentKongXian.putExtra("showType", 1);
                                intentKongXian.putExtra("tableNum", tableDesInfo);
                                startActivity(intentKongXian);
                            } else if (tableState == -1) {// 编辑中(本地状态)
                                SerializableMap tableHasNewOrderMap = new SerializableMap();
                                tableHasNewOrderMap.setMap(MainActivity.editBookMap.get(tableDesInfo));
                                Intent intentEdit = new Intent(getActivity(), EditOrderActivity.class);
                                intentEdit.putExtra("showType", -1);
                                intentEdit.putExtra("tableNum", tableDesInfo);
                                intentEdit.putExtra("hasOrderInEditBook", tableHasNewOrderMap);
                                startActivity(intentEdit);
                            } else {// 占用
                                String tag_table_order_id = ((LinearLayout) v).getChildAt(0).getTag(R.id.tag_table_order_id).toString();
                                Intent intentKongXian = new Intent(getActivity(), EditOrderActivity.class);
                                intentKongXian.putExtra("showType", 3);
                                intentKongXian.putExtra("tableNum", tableDesInfo);
                                intentKongXian.putExtra("tableOrderId", tag_table_order_id);
                                startActivity(intentKongXian);
                            }
                        }
                    } else {
                        // 显示预览信息
                        if (tableState == 2) {
                            ComFun.showToast(getActivity(), "该餐桌正在点餐哦", Toast.LENGTH_SHORT);
                        } else if (tableState == 1 || tableState == -1) {
                            // 通知首页显示加载动画
                            Message msg = new Message();
                            Bundle data = new Bundle();
                            data.putString("seeTableInfoType", "none");
                            msg.setData(data);
                            msg.what = MainActivity.MSG_SEE_TABLE_INFO_LOADING;
                            MainActivity.mHandler.sendMessage(msg);
                        } else {
                            String tableDesInfo = ((TextView) ((LinearLayout) v).getChildAt(1)).getText().toString();
                            String tag_table_order_id = ((LinearLayout) v).getChildAt(0).getTag(R.id.tag_table_order_id).toString();
                            // 通知首页显示加载动画
                            Message msg = new Message();
                            Bundle data = new Bundle();
                            data.putString("seeTableInfoType", "loading");
                            msg.setData(data);
                            msg.what = MainActivity.MSG_SEE_TABLE_INFO_LOADING;
                            MainActivity.mHandler.sendMessage(msg);
                            // 调用任务根据餐桌号获取该餐桌就餐信息数据
                            RequestParams params = new RequestParams();
                            params.put("tableOrderId", tag_table_order_id);
                            AllRequestUtil.GetMenuInThisTable(getActivity(), params, true, false, tag_table_order_id, tableDesInfo);
                        }
                    }
                }
            });
        }
    }

    // 为图片添加滤镜
    private void changeLight(ImageView imageview, int brightness) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.set(new float[]{1, 0, 0, 0, brightness, 0, 1, 0, 0,
                brightness, 0, 0, 1, 0, brightness, 0, 0, 0, 1, 0});
        imageview.setColorFilter(new ColorMatrixColorFilter(matrix));

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 取消注册广播
        getActivity().unregisterReceiver(refDataBroadCastReceive);
    }

    public class RefDataBroadCastReceive extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MSG_REFDATA)) {
                boolean newDataFlag = intent.getExtras().getBoolean("newData");
                // 更新餐桌信息
                if (newDataFlag) {
                    String tableFullInfo = SharedPreferencesTool.getFromShared(context, "BouilliTableInfo", "tableFullInfo");
                    if (ComFun.strNull(tableFullInfo)) {
                        String[] tableFullInfoArr = tableFullInfo.split(",");
                        for (String tableInfo : tableFullInfoArr) {
                            if (ComFun.strNull(tableInfo)) {
                                if (dining_table_layout != null) {
                                    ImageView tableImg = (ImageView) dining_table_layout.findViewWithTag("tableImg" + tableInfo.split("\\|")[0]);
                                    if (tableImg != null) {
                                        tableImg.clearAnimation();
                                        if (MainActivity.editBookMap.containsKey(tableInfo.split("\\|")[0])) {
                                            if (Integer.parseInt(tableInfo.split("\\|")[1]) == 3) {// 占用
                                                tableImg.setTag(R.id.tag_table_state, 3);
                                                tableImg.setTag(R.id.tag_table_order_id, tableInfo.split("\\|")[2]);
                                                tableImg.setImageResource(R.drawable.desk_1);
                                            } else {
                                                // 更新为草稿餐单状态
                                                tableImg.setTag(R.id.tag_table_state, -1);
                                                tableImg.setImageResource(R.drawable.desk_2);
                                            }
                                        } else {
                                            if (Integer.parseInt(tableInfo.split("\\|")[1]) == 1) {// 空闲
                                                tableImg.setTag(R.id.tag_table_state, 1);
                                                tableImg.setImageResource(R.drawable.desk_0);
                                            } else if (Integer.parseInt(tableInfo.split("\\|")[1]) == 2) {// 编辑
                                                tableImg.setTag(R.id.tag_table_state, 2);
                                                tableImg.setImageResource(R.drawable.desk_2);
                                                // 闪烁显示动画
                                                AlphaAnimation aa1 = new AlphaAnimation(1.0f, 0.6f);
                                                aa1.setRepeatCount(-1);//设置重复次数
                                                aa1.setRepeatMode(Animation.REVERSE);//设置反方向执行
                                                aa1.setDuration(2000);
                                                aa1.setFillAfter(true);
                                                tableImg.startAnimation(aa1);
                                            } else {// 占用
                                                tableImg.setTag(R.id.tag_table_state, 3);
                                                tableImg.setTag(R.id.tag_table_order_id, tableInfo.split("\\|")[2]);
                                                tableImg.setImageResource(R.drawable.desk_1);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
