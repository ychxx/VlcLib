package com.yc.vlclib;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.yc.yclibrary.base.YcAppCompatActivity;

import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.view.VLCVideoView;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 慧眼视频播放页面
 */

public class HuiYanVideoActivity extends YcAppCompatActivity {
    @BindView(R.id.ivHuXinBack)
    ImageView ivHuXinBack;
    @BindView(R.id.control_Main)
    ImageView imageView_main;
    @BindView(R.id.control_Speed)
    ImageView controlSpeed;
    @BindView(R.id.control_Setting)
    ImageView controlSetting;
    @BindView(R.id.control_Direction)
    ImageView controlDirection;
    @BindView(R.id.control_Up)
    ImageView controlUp;
    @BindView(R.id.control_Left)
    ImageView controlLeft;
    @BindView(R.id.control_Down)
    ImageView controlDown;
    @BindView(R.id.control_Right)
    ImageView controlRight;
    @BindView(R.id.vlc)
    VLCVideoView videoView;
    @BindView(R.id.layout_pross)
    public LinearLayout layoutPross;
    //    @BindView(R.id.txt_pross)
//    public TextView prossTV;
    private String rtsp = "rtsp://218.106.146.78:34000/35018100461327620155_01123_1";

    @Override
    protected int getLayoutId() {
        return R.layout.video_hui_yan_activity;
    }

    @Override
    protected void initView(Bundle bundle) {
        videoView.setOnBufferListener(new VLCVideoView.OnBufferListener() {
            @Override
            public void onBuffer(MediaPlayer mp, float buffer) {
                layoutPross.setVisibility(View.VISIBLE);
            }
        });
        videoView.setOnPlayingListener(new VLCVideoView.OnPlayingListener() {
            @Override
            public void onPlaying(org.videolan.libvlc.MediaPlayer mp) {
                layoutPross.setVisibility(View.GONE);
            }
        });
        videoView.postDelayed(new Runnable() {
            @Override
            public void run() {
                play(rtsp);
            }
        }, 3000);
//        mPresenter.getDevUrl(videoBean.getmIp(), videoBean.getmPort(), videoBean.getmUserName(), videoBean.getmPassword(), videoBean.getmDeviceNum());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView != null) {
            videoView.onResume();
        }
        if (videoView != null)
            videoView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null) videoView.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null) videoView.onDestroy();
        System.gc();
    }


    private void play(String url) {
        if (videoView.isInitialized()) {
            videoView.changeUrl(url);
        } else {
            //设置视频路径
            videoView.setVideoPath(url);
        }
        videoView.play();
    }
//    private void videoControl(@VideoControlType String videoControlType, final boolean isShowRemind) {
//        controlPre = System.currentTimeMillis();
//        if (!videoControlType.equals(VideoControlType.stop)) {
//            videoView.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (System.currentTimeMillis() - controlPre >= 999) {
//                        videoControl(VideoControlType.stop, false);
//                    }
//                }
//            }, 1000);
//        }
//        mPresenter.devicePtzs(videoBean.getmIp(), videoBean.getmPort(), videoBean.getmUserName(), videoBean.getmPassword(), videoBean.getmDeviceNum(), mSpeedNum + "", videoControlType, isShowRemind);
//    }

//    @Override
//    public void onGetDevUrlSuccess(GetDevUrlJson getDevUrlJson) {
//        play(getDevUrlJson.getRtspUrl());
//    }
//
//    @Override
//    public void onGetDevUrlFail(String msg) {
//        showToast(msg);
//    }
//
//    @Override
//    public void onDevicePtzsSuccess(DevicePtzJson devicePtzJson) {
//
//    }
//
//    @Override
//    public void onDevicePtzsFail(String msg) {
//        showToast(msg);
//    }

    @OnClick({R.id.ivHuXinBack, R.id.control_Main, R.id.control_Speed, R.id.control_Setting, R.id.control_Direction, R.id.control_Up, R.id.control_Left, R.id.control_Down, R.id.control_Right})
    void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivHuXinBack:
                finish();
                break;
            case R.id.control_Main:
                break;
            case R.id.control_Speed:
                break;
            case R.id.control_Setting:
                break;
            case R.id.control_Direction:
                break;
            case R.id.control_Up:

                break;
            case R.id.control_Left:
                break;
            case R.id.control_Down:
                break;
            case R.id.control_Right:
                break;
        }
    }
}
