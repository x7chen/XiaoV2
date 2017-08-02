package com.cfk.xiaov;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;

public class MainActivity extends AppCompatActivity {


    RadioButton rbVideo;
    RadioButton rbPhoto;
    RadioButton rbFriend;
    RadioButton rbMe;

    private TabHost tabHost;
    private RadioGroup radiogroup;
    private int menuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rbVideo = (RadioButton) findViewById(R.id.radioButton);
        rbFriend=(RadioButton)findViewById(R.id.radioButton2);
        rbPhoto=(RadioButton)findViewById(R.id.radioButton3);
        rbMe=(RadioButton)findViewById(R.id.radioButton4);
//        rbVideo.setOnClickListener(this);
//        rbFriend.setOnClickListener(this);
//        rbPhoto.setOnClickListener(this);
//        rbMe.setOnClickListener(this);

        radiogroup = (RadioGroup) findViewById(R.id.radioGroup);
        tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("video").setIndicator("video")
                .setContent(R.id.fragment_video));
        tabHost.addTab(tabHost.newTabSpec("friend").setIndicator("friend")
                .setContent(R.id.fragment_friend));
        tabHost.addTab(tabHost.newTabSpec("photo").setIndicator("photo")
                .setContent(R.id.fragment_photo));
        tabHost.addTab(tabHost.newTabSpec("me").setIndicator("me")
                .setContent(R.id.fragment_me));
        radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                menuid = checkedId;
                int currentTab = tabHost.getCurrentTab();
                switch (checkedId) {
                    case R.id.radioButton:
                        tabHost.setCurrentTabByTag("video");
                        //如果需要动画效果就使用
                        setCurrentTabWithAnim(currentTab, 0, "video");
                        break;
                    case R.id.radioButton2:
                        //tabHost.setCurrentTabByTag("mycenter");
                        setCurrentTabWithAnim(currentTab, 1, "friend");
                        break;
                    case R.id.radioButton3:
                        tabHost.setCurrentTabByTag("photo");
                        setCurrentTabWithAnim(currentTab, 2, "photo");
                        break;
                    case R.id.radioButton4:
                        setCurrentTabWithAnim(currentTab, 3, "me");
                }
                // 刷新actionbar的menu
                getWindow().invalidatePanelMenu(Window.FEATURE_OPTIONS_PANEL);
            }
        });
    }
      /*
    @Override
    public void onClick(View view) {

        //步骤一：添加一个FragmentTransaction的实例
        FragmentManager fragmentManager =getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        switch (view.getId()){
            case R.id.radioButton:
                 //步骤二：用add()方法加上Fragment的对象rightFragment
                VideoFragment videoFragment = VideoFragment.newInstance("","");
                transaction.add(R.id.fragment_container,videoFragment);
                break;
            case R.id.radioButton2:
                FriendFragment friendFragment = FriendFragment.newInstance("","");
                transaction.add(R.id.fragment_container,friendFragment);
                break;
            case R.id.radioButton3:
                PhotoFragment photoFragment = PhotoFragment.newInstance("","");
                transaction.add(R.id.fragment_container,photoFragment);
                break;
            case R.id.radioButton4:
                MeFragment meFragment = MeFragment.newInstance("","");
                transaction.add(R.id.fragment_container,meFragment);
                break;
            default:
                break;
        }
        //步骤三：调用commit()方法使得FragmentTransaction实例的改变生效
        transaction.commit();

    }
*/
    // 这个方法是关键，用来判断动画滑动的方向
    private void setCurrentTabWithAnim(int now, int next, String tag) {
        if (now > next) {
            tabHost.getCurrentView().startAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out));
            tabHost.setCurrentTabByTag(tag);
            tabHost.getCurrentView().startAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in));
        } else {
            tabHost.getCurrentView().startAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out));
            tabHost.setCurrentTabByTag(tag);
            tabHost.getCurrentView().startAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in));
        }
    }
}
