package com.tencent.calldemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cfk.xiaov.R;
import com.tencent.callsdk.ILVCallConfig;
import com.tencent.callsdk.ILVCallConstants;
import com.tencent.callsdk.ILVCallListener;
import com.tencent.callsdk.ILVCallManager;
import com.tencent.callsdk.ILVCallNotification;
import com.tencent.callsdk.ILVCallNotificationListener;
import com.tencent.callsdk.ILVIncomingListener;
import com.tencent.callsdk.ILVIncomingNotification;
import com.tencent.common.AccountMgr;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.ILiveSDK;
import com.tencent.ilivesdk.core.ILiveLoginManager;
import com.tencent.model.MySelfInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * 联系人界面
 */
public class ContactActivity extends Activity implements View.OnClickListener{
    private static String TAG = "ContactActivity";
    private TextView tvMyAddr, tvMsg;
    private EditText etDstAddr;
    private ListView lvCallList;
    ArrayList<String> callList = new ArrayList<String>();
    private ArrayAdapter adapterCallList;
    private LinearLayout llDstNums;

    // 多人视频控件列表
    private ArrayList<EditText> mEtNums = new ArrayList<>();

    private boolean bLogin; // 记录登录状态

    // 内部方法
    private void initView() {
        tvMsg = (TextView)findViewById(R.id.tv_msg);
        tvMyAddr = (TextView) findViewById(R.id.tv_my_address);
        tvMyAddr.setText(MySelfInfo.getInstance().getId());
        etDstAddr = (EditText) findViewById(R.id.et_dst_address);
        lvCallList = (ListView) findViewById(R.id.lv_call_list);

        llDstNums = (LinearLayout)findViewById(R.id.ll_dst_numbers);
        adapterCallList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                callList);
        lvCallList.setAdapter(adapterCallList);
        lvCallList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String strNums = (String) adapterCallList.getItem(position);
                String [] numArrs = strNums.split(",");
                ArrayList<String> nums = new ArrayList<String>();
                for (int i=0; i<numArrs.length; i++){
                    nums.add(numArrs[i]);
                }
                makeCall(ILVCallConstants.CALL_TYPE_VIDEO, nums);
            }
        });

    }

    private void addCallList(String remoteId) {
        if (!callList.contains(remoteId)) {
            if (callList.add(remoteId)) {
                adapterCallList.notifyDataSetChanged();
            }
        }
    }

    /**
     * 注销后处理
     */
    private void onLogout() {
        // 注销成功清除用户信息，并跳转到登陆界面
        //finish();
        bLogin = false;
        finish();
    }

    /**
     * 输出日志
     */
    private void addLogMessage(String strMsg){
        String msg = tvMsg.getText().toString();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        msg = msg + "\r\n["+formatter.format(curDate)+"] " + strMsg;
        tvMsg.setText(msg);
    }

    /**
     * 注销
     */
    private void logout() {
        ILiveLoginManager.getInstance().iLiveLogout(new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                onLogout();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                onLogout();
            }
        });
    }

    // 覆盖方法
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        initView();


    }

    @Override
    public void onBackPressed() {
        if (bLogin){
            ILiveLoginManager.getInstance().iLiveLogout(new ILiveCallBack() {
                @Override
                public void onSuccess(Object data) {
                    finish();
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {
                    finish();
                }
            });
        }else{
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    public void onClick(View v) {
        if (R.id.btn_logout == v.getId()){
            logout();
        }else if (R.id.btn_make_call == v.getId()){
            String remoteId = etDstAddr.getText().toString();
            if (TextUtils.isEmpty(remoteId)){
                Toast.makeText(this, R.string.toast_phone_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<String> nums = new ArrayList<>();
            String tmpNum;
            String calllist = "";
            nums.add(remoteId);
            for (EditText etNum : mEtNums){
                tmpNum = etNum.getText().toString();
                if (!TextUtils.isEmpty(tmpNum)){
                    nums.add(tmpNum);
                    calllist = calllist + tmpNum + ",";
                }
            }
            calllist = calllist + remoteId;

            // 添加通话记录
            addCallList(calllist);
            makeCall(ILVCallConstants.CALL_TYPE_VIDEO, nums);
        }else if (R.id.btn_add == v.getId()){
            addNewInputNumbers();
        }
    }


    /**
     * 添加新的用户号码输入
     */
    private void addNewInputNumbers(){
        if (mEtNums.size() >= 3){
            return;
        }
        final LinearLayout linearLayout = new LinearLayout(this);
        final EditText etNum = new EditText(this);
        mEtNums.add(etNum);
        Button btnDel = new Button(this);
        btnDel.setText("-");
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEtNums.remove(etNum);
                llDstNums.removeView(linearLayout);
            }
        });
        linearLayout.addView(btnDel, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(etNum, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        llDstNums.addView(linearLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    /**
     * 发起呼叫
     */
    private void makeCall(int callType, ArrayList<String> nums){
        Intent intent = new Intent();
        intent.setClass(this, CallActivity.class);
        intent.putExtra("HostId", ILiveLoginManager.getInstance().getMyUserId());
        intent.putExtra("CallId", 0);
        intent.putExtra("CallType", callType);
        intent.putStringArrayListExtra("CallNumbers", nums);
        startActivity(intent);
    }

}
