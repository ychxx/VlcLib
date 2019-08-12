/*
 * Copyright Copyright (C) 2016 Ma Tianlun.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.videolan.libvlc.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VlcLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by huanglin on 2017/3/1.
 * Email: huanglin@ffcs.cn
 */
public class VLCVideoView extends FrameLayout {
    private static final String TAG = VLCVideoView.class.getName();
    private Context mContext;
    //private SurfaceView mSurfaceView;
    // private SurfaceHolder mHolder;
    private TextureView mTextureView;
    private LibVLC mLibvlc;
    private String mFilePath;
    private MediaPlayer.EventListener mPlayerListener = new VLCPlayerListener(this);
    private IVLCVout.Callback mCallback;
    private MediaPlayer mMediaPlayer = null;
    private boolean mHWDecoderEnabled = true;
    private boolean mNeedSetHWDecoderEnabled = true;//是否添加媒体加速器
    private boolean mIsInitialized = false;//是否完成初始化
    /**
     * Current playing position
     */
    public long mPosition;

    public VLCVideoView(Context context) {
        this(context, null);
    }

    public VLCVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VLCVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initViews(mContext);
    }

    private void initViews(Context context) {
        /*mSurfaceView = new SurfaceView(context);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mSurfaceView.setLayoutParams(layoutParams);
        addView(mSurfaceView);*/

        mTextureView = new TextureView(context);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mTextureView.setLayoutParams(layoutParams);
        addView(mTextureView);
    }

    /**
     * Soft and hard decoding switch
     *
     * @param status
     */
    public void setHWDecoderEnabled(boolean status) {
        this.mHWDecoderEnabled = status;
    }

    public void setIVLCVoutCallback(IVLCVout.Callback callback) {
        this.mCallback = callback;
    }

    public void createPlayer(String media, boolean needSetHWDecoderEnabled) {
        releasePlayer();
        try {
            // Create LibVLC
            ArrayList<String> options = new ArrayList<String>();
            //options.add("--subsdec-encoding <encoding>");
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity

            options.add("--extraintf=");
            options.add("--rtsp-tcp");

            mLibvlc = new LibVLC(mContext, options);
            // Create media player
            mMediaPlayer = new MediaPlayer(mLibvlc);
            mMediaPlayer.setEventListener(mPlayerListener);
            // Set up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            /*mHolder.setKeepScreenOn(true);
            vout.setVideoView(mSurfaceView);
            vout.setWindowSize(mSurfaceView.getWidth(), mSurfaceView.getHeight());*/
            mTextureView.setKeepScreenOn(true);
            vout.setVideoView(mTextureView);
            vout.setWindowSize(mTextureView.getWidth(), mTextureView.getHeight());
            //vout.setSubtitlesView(mSurfaceSubtitles);
            if (mCallback != null) vout.addCallback(mCallback);
            vout.attachViews();
            mNeedSetHWDecoderEnabled = needSetHWDecoderEnabled;
            Media m = null;
            if (media.startsWith("rtsp://") || media.startsWith("http://"))
                m = new Media(mLibvlc, Uri.parse(media));
            else {
                m = new Media(mLibvlc, media);
            }
            if (mNeedSetHWDecoderEnabled) {
                m.setHWDecoderEnabled(true, true);
            } else {
                m.setHWDecoderEnabled(false, false);
            }
            mMediaPlayer.setMedia(m);
            mIsInitialized = true;
            //mMediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void releasePlayer() {
        mIsInitialized = false;
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.removeCallback(mCallback);
            vout.detachViews();
        }
        if (mLibvlc != null) {
            mLibvlc.release();
            mLibvlc = null;
        }
    }

    /**
     * Set Surface
     */
    public void setSize(int width, int height) {
        if (mMediaPlayer == null) return;
        mMediaPlayer.getVLCVout().setWindowSize(width, height);
    }

    private class VLCPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<VLCVideoView> mOwner;

        public VLCPlayerListener(VLCVideoView owner) {
            mOwner = new WeakReference<VLCVideoView>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            VLCVideoView player = mOwner.get();
            String comment = "";
            switch (event.type) {
                case MediaPlayer.Event.EndReached:
                    comment = "MediaPlayer.Event.EndReached";
                    player.releasePlayer();
                    if (mOnCompletionListener != null) {
                        mOnCompletionListener.onCompletion(mMediaPlayer);
                    }
                    break;
                case MediaPlayer.Event.Opening:
                    comment = "MediaPlayer.Event.Opening";
                    if (mOnPreparedListener != null) {
                        mOnPreparedListener.onPrepared(mMediaPlayer);
                    }
                    if (mPosition > 0) {
                        mMediaPlayer.setTime(mPosition);
                        mPosition = 0;
                    }
                    break;
                case MediaPlayer.Event.Buffering: // 缓存
                    comment = "MediaPlayer.Event.Buffering ; event.getBuffering() = " + event.getBuffering();
                    if (mOnBufferListener != null) {
                        mOnBufferListener.onBuffer(mMediaPlayer, event.getBuffering());
                    }
                    break;
                case MediaPlayer.Event.EncounteredError:
                    comment = "MediaPlayer.Event.EncounteredError";
                    releasePlayer();
                    if (mOnErrorListener != null) {
                        mOnErrorListener.onError(mMediaPlayer, 0, 0);
                    }
                    break;
                case MediaPlayer.Event.Playing:
                    comment = "MediaPlayer.Event.Playing";
                    if (mOnBufferListener != null) {
                        mOnBufferListener.onBuffer(mMediaPlayer, event.getBuffering());
                    }
                    break;
                case MediaPlayer.Event.TimeChanged: // 播放中
                    comment = "MediaPlayer.Event.TimeChanged";
                    if (mOnPlayingListener != null) {
                        mOnPlayingListener.onPlaying(mMediaPlayer);
                    }
                    break;
                case MediaPlayer.Event.Paused:
                    comment = "MediaPlayer.Event.Paused";
                    break;
                case MediaPlayer.Event.Stopped:
                    comment = "MediaPlayer.Event.Stopped";
                    break;
                default:
                    comment = "default";
                    break;
            }
            VlcLog.w(TAG, "VLC onEvent : event.type = " + event.type + "(" + comment + ")");
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    /*public SurfaceHolder getHolder() {
        return mHolder;
    }*/

    public void setVideoPath(String path) {
        mFilePath = path;
        //mHolder = mSurfaceView.getHolder();
        createPlayer(mFilePath, mHWDecoderEnabled);
    }

    public boolean isInitialized() {
        return mIsInitialized;
    }

    public void setVideoURI(Uri uri) {
        setVideoPath(uri.getPath());
    }

    public boolean isPlaying() {
        return mMediaPlayer == null ? false : mMediaPlayer.isPlaying();
    }

    public void refresh() {
        changeUrl(mFilePath);
    }

    public synchronized void changeUrl(String url) {
        if (TextUtils.isEmpty(url))
            return;
        if (mMediaPlayer != null) {
            mMediaPlayer.getMedia().clearSlaves();
            mMediaPlayer.stop();
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.removeCallback(mCallback);
        }
        Media m = null;
        if (mLibvlc == null) {
            ArrayList<String> options = new ArrayList<String>();
            //options.add("--subsdec-encoding <encoding>");
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity

            options.add("--extraintf=");
            options.add("--rtsp-tcp");

            mLibvlc = new LibVLC(mContext, options);
            // Create media player
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer(mLibvlc);
                mMediaPlayer.setEventListener(mPlayerListener);
            }
        }
        if (url.startsWith("rtsp://") || url.startsWith("http://"))
            m = new Media(mLibvlc, Uri.parse(url));
        else {
            m = new Media(mLibvlc, url);
        }
        if (mNeedSetHWDecoderEnabled) {
            m.setHWDecoderEnabled(true, true);
        } else {
            m.setHWDecoderEnabled(false, false);
        }
        mMediaPlayer.setMedia(m);
    }

    public void play() {
        if (mMediaPlayer == null) return;
        mMediaPlayer.play();
    }

    public void pause() {
        if (mMediaPlayer == null) return;
        mMediaPlayer.pause();
    }

    public void stop() {
        if (mMediaPlayer == null) return;
        mMediaPlayer.stop();
    }

    public void onPause() {
        if (mMediaPlayer == null) return;
        mPosition = mMediaPlayer.getTime();
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
    }

    public void onResume() {
        if (TextUtils.isEmpty(mFilePath)) return;
        onResume(mFilePath);
    }

    public void onResume(String pFilePath) {
        /** Resume playback */
        try {
            createPlayer(pFilePath, mHWDecoderEnabled);
            play();
        } catch (Exception e) {
            VlcLog.w(TAG, e.toString());
        }
    }

    public void onDestroy() {
        //if (mHolder != null) mHolder.setKeepScreenOn(false);
        mTextureView.setKeepScreenOn(false);
        releasePlayer();
    }

    public Bitmap getBitmap() {
        /*
        // 截图全屏
        View view = ((Activity) mContext).getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();*/
        /*
        // 截图 视频，但是却是黑屏的
        mSurface.setDrawingCacheEnabled(true);
        mSurface.buildDrawingCache();
        Bitmap bitmap = mSurface.getDrawingCache();*/
        //return mSurfaceView.getBitmap();

        /*Bitmap bitmap = Bitmap.createBitmap(320, 480, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        mSurfaceView.draw(canvas); // SurfaceHolder.lockCanvas() 返回的Canvas绘制什么内容，我们定义的也要绘制一遍
        return bitmap;*/
        return mTextureView.getBitmap(getWidth(), getHeight());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // add by huanglin 20170228
    public int getCurrentPosition() {
        return mMediaPlayer == null ? 0 : (int) mMediaPlayer.getTime();
    }

    public int getDuration() {
        return mMediaPlayer == null ? 0 : (int) mMediaPlayer.getLength();
    }

    public void seekTo(int mesc) {
        if (mMediaPlayer == null || mesc < 0 || mesc > mMediaPlayer.getLength()) return;
        mMediaPlayer.setTime(mesc);
    }

    public void setRate(float rate) {
        if (mMediaPlayer == null) return;
        mMediaPlayer.setRate(rate);
    }

    /////////////// interface
    public interface OnErrorListener {
        public boolean onError(MediaPlayer mp, int what, int extra);
    }

    public interface OnPreparedListener {
        public void onPrepared(MediaPlayer mp);
    }

    public interface OnBufferListener {
        public void onBuffer(MediaPlayer mp, float buffer);
    }

    public interface OnPlayingListener {
        public void onPlaying(MediaPlayer mp);
    }

    public interface OnInfoListener {
        public boolean onInfo(MediaPlayer mp, int what, int extra);
    }

    public interface OnCompletionListener {
        public void onCompletion(MediaPlayer mp);
    }

    private OnErrorListener mOnErrorListener;
    private OnPreparedListener mOnPreparedListener;
    private OnBufferListener mOnBufferListener;
    private OnPlayingListener mOnPlayingListener;
    private OnInfoListener mOnInfoListener;
    private OnCompletionListener mOnCompletionListener;

    public void setOnErrorListener(OnErrorListener onErrorListener) {
        this.mOnErrorListener = onErrorListener;
    }

    public void setOnPreparedListener(OnPreparedListener onPreparedListener) {
        this.mOnPreparedListener = onPreparedListener;
    }

    public void setOnBufferListener(OnBufferListener onBufferListener) {
        this.mOnBufferListener = onBufferListener;
    }

    public void setOnPlayingListener(OnPlayingListener onPlayingListener) {
        this.mOnPlayingListener = onPlayingListener;
    }

    public void setOnInfoListener(OnInfoListener onInfoListener) {
        this.mOnInfoListener = onInfoListener;
    }

    public void setOnCompletionListener(OnCompletionListener onCompletionListener) {
        this.mOnCompletionListener = onCompletionListener;
    }

//    public Point getViewHeightAndWight() {
//        Point point = new Point(this.getWidth(), this.getHeight());
//        return point;
//    }
}
