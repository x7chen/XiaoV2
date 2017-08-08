package com.cfk.xiaov;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;

import com.tencent.calldemo.CallActivity;
import com.tencent.callsdk.ILVCallConfig;
import com.tencent.callsdk.ILVCallListener;
import com.tencent.callsdk.ILVCallManager;
import com.tencent.callsdk.ILVCallNotification;
import com.tencent.callsdk.ILVCallNotificationListener;
import com.tencent.callsdk.ILVIncomingListener;
import com.tencent.callsdk.ILVIncomingNotification;

import java.util.ArrayList;

public class VideoService extends Service implements ILVIncomingListener, ILVCallListener, ILVCallNotificationListener {
    String TAG = getClass().getSimpleName();
    Handler handler;
    private AlertDialog mIncomingDlg;
    private int mCurIncomingId;
    ArrayList<String> callList = new ArrayList<String>();

    public VideoService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        handler = new Handler();
        super.onCreate();
        ILVCallManager.getInstance().init(new ILVCallConfig()
                .setNotificationListener(this)
                .setAutoBusy(true));

        // 设置通话回调
        ILVCallManager.getInstance().addIncomingListener(this);
        ILVCallManager.getInstance().addCallListener(this);
        Log.i(TAG, "Init CallSDK...");
    }

    @Override
    public void onDestroy() {
        ILVCallManager.getInstance().removeIncomingListener(this);
        ILVCallManager.getInstance().removeCallListener(this);
        super.onDestroy();
    }

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    private void acceptCall(int callId, String hostId, int callType) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), CallActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("HostId", hostId);
        intent.putExtra("CallId", mCurIncomingId);
        intent.putExtra("CallType", callType);
        startActivity(intent);
    }

    private void addCallList(String remoteId) {
        if (!callList.contains(remoteId)) {
            if (callList.add(remoteId)) {
                // adapterCallList.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onCallEstablish(int callId) {

    }

    @Override
    public void onCallEnd(int callId, int endResult, String endInfo) {
        if (mCurIncomingId == callId) {
            mIncomingDlg.dismiss();
        }
        Log.i(TAG, "End Call:" + endResult + "-" + endInfo + "/" + callId);
        Log.e("XDBG_END", "onCallEnd->id: " + callId + "|" + endResult + "|" + endInfo);
    }

    @Override
    public void onException(int iExceptionId, int errCode, String errMsg) {

    }

    @Override
    public void onRecvNotification(int callid, ILVCallNotification notification) {
        Log.i(TAG, "onRecvNotification->notify id:" + notification.getNotifId() + "|" + notification.getUserInfo() + "/" + notification.getSender());
    }

    @Override
    public void onNewIncomingCall(final int callId, final int callType, final ILVIncomingNotification notification) {
        Log.i(TAG, "New Call from:" + notification.getSender() + "/" + callId + "-" + notification);
        if (null != mIncomingDlg) {  // 关闭遗留来电对话框
            mIncomingDlg.dismiss();
        }
        mCurIncomingId = callId;
        mIncomingDlg = new AlertDialog.Builder(this)
                .setTitle("New Call From " + notification.getSender())
                .setMessage(notification.getNotifDesc())
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        acceptCall(callId, notification.getSponsorId(), callType);
                        Log.i(TAG, "Accept Call :" + mCurIncomingId);
                    }
                })
                .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int ret = ILVCallManager.getInstance().rejectCall(mCurIncomingId);
                        Log.i(TAG, "Reject Call:" + ret + "/" + mCurIncomingId);
                    }
                })
                .create();
        mIncomingDlg.setCanceledOnTouchOutside(false);
        mIncomingDlg.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        mIncomingDlg.show();
        Log.i(TAG, "Dialog show!");
        //


        addCallList(notification.getSender());
    }

}
