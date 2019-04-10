package com.shykad.yunke.sdk.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shykad.yunke.sdk.R;
import com.shykad.yunke.sdk.config.HttpConfig;
import com.shykad.yunke.sdk.engine.VideoEngine;
import com.shykad.yunke.sdk.engine.YunKeEngine;
import com.shykad.yunke.sdk.manager.ShykadManager;
import com.shykad.yunke.sdk.okhttp.bean.GetAdResponse;
import com.shykad.yunke.sdk.utils.BaseRealVisibleUtil;
import com.shykad.yunke.sdk.utils.StringUtils;
import com.shykad.yunke.sdk.utils.SystemUtils;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Create by wanghong.he on 2019/4/8.
 * description：时评广告
 */
public class YunkeVideoView extends RelativeLayout {

    private Context mContext;
    private String adId;
    private GetAdResponse.AdCotent adCotent;
    private TextView nativeTitle,nativeDesc;
    private ImageView nativeClose,nativeIcon;
    private SurfaceView nativeContainer;
    private VideoViewCallBack videoViewCallBack;
    private IjkMediaPlayer ijkMediaPlayer;//由ijkplayer提供，用于播放视频，需要给他传入一个surfaceView
    private SurfaceHolder surfaceHolder;
    private String mPath;//视频文件地址
    private VideoPlayerListener listener;

    public YunkeVideoView(Context context) {
        this(context,null);
    }

    public YunkeVideoView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public YunkeVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        //获取控件
        View view = LayoutInflater.from(context).inflate(R.layout.yunke_video_ad_view, this);
        initView(view);
        initAttrs(context,attrs);

    }

    @SuppressLint({"Recycle", "CustomViewStyleable"})
    private void initView(View view) {

        nativeTitle = view.findViewById(R.id.tv_native_ad_title);
        nativeClose = view.findViewById(R.id.img_native_dislike);
        nativeContainer = view.findViewById(R.id.ad_native_surface);
        nativeIcon = view.findViewById(R.id.iv_native_icon);
        nativeDesc = view.findViewById(R.id.tv_native_ad_desc);
    }

    private void initAttrs(Context context, AttributeSet attrs){
        //获取自定义属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.YunkeVideoView);
        int indexCount = typedArray.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
            initAttr(typedArray.getIndex(i), typedArray);
        }
        typedArray.recycle();
    }

    private void initAttr(int attr, TypedArray typedArray){
        if (attr == R.styleable.YunkeVideoView_video_title){
            setVideoTitle(typedArray.getText(attr));
        }
        if (attr == R.styleable.YunkeVideoView_video_desc){
            setVideoDesc(typedArray.getText(attr));
        }
        if (attr == R.styleable.YunkeVideoView_video_content_drawable){
            setVideoContentDrawable(typedArray.getDrawable(attr));
        }
        if (attr == R.styleable.YunkeVideoView_video_logo_drawable){
            setVideoLogoDrawable(typedArray.getDrawable(attr));
        }
        if (attr == R.styleable.YunkeVideoView_video_cancel_drawable){
            setVideoCancel(typedArray.getDrawable(attr));
        }
    }

    public YunkeVideoView setVideoCancel(Drawable cancelDrawable) {
        nativeClose.setImageDrawable(cancelDrawable);
        return this;
    }

    public YunkeVideoView setVideoLogoDrawable(Drawable logoDrawable) {
        nativeIcon.setImageDrawable(logoDrawable);
        return this;
    }

    public YunkeVideoView setVideoContentDrawable(Drawable contentDrawable) {
        nativeContainer.setBackground(contentDrawable);
        return this;
    }

    public YunkeVideoView setVideoDesc(CharSequence videoDesc) {
        nativeDesc.setText(videoDesc);
        return this;
    }

    public YunkeVideoView setVideoTitle(CharSequence videoTitle) {
        nativeTitle.setText(videoTitle);
        return this;
    }

    public YunkeVideoView lanchVideo(ViewGroup container, Object response, VideoViewCallBack videoViewCallBack) {
        if (!SystemUtils.isFastDoubleClick()){
            this.videoViewCallBack = videoViewCallBack;
            if (videoViewCallBack!=null) {
                if (response instanceof GetAdResponse){
                    this.adCotent = ((GetAdResponse) response).getData();
                    this.adId = adCotent.getId();
                    if (!StringUtils.isEmpty(this.adCotent)){
                        videoViewCallBack.onAdLoad(YunkeVideoView.this);
                    }
                }

                if (!StringUtils.isEmpty(adId) && !StringUtils.isEmpty(adCotent.getDesc()) && !StringUtils.isEmpty(adCotent.getTitle()) && !StringUtils.isEmpty(adCotent.getSrc())) {

                    nativeContainer.getHolder().addCallback(new SurfaceHolder.Callback() {
                        @Override
                        public void surfaceCreated(SurfaceHolder holder) {
                            ijkMediaPlayer.setDisplay(holder);
                        }

                        @Override
                        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                            //surfaceview创建成功后，加载视频
                            load();
                        }

                        @Override
                        public void surfaceDestroyed(SurfaceHolder holder) {

                        }
                    });
//                    startPlay("rtmp://demo.easydss.com:10085/vlive/VKCiyQ8mg?k=VKCiyQ8mg.239ac76e75c23db937");
                    GlidImageManager.getInstance().loadImageView(mContext,adCotent.getSrc(),nativeIcon,R.drawable.yunke_ic_default_image);
                    nativeClose.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            container.removeAllViews();
                            videoViewCallBack.onAdCancel(YunkeVideoView.this);
                        }
                    });

                    nativeContainer.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            clickAdTask();
                        }
                    });

                    if (BaseRealVisibleUtil.getInstance(mContext).isViewCovered(nativeContainer) || nativeContainer.getLocalVisibleRect(new Rect())){
                        showAdTask();
                    }
                }
            }

        }
        return this;
    }

    /**
     * 展示广告
     */
    private void showAdTask(){
        ShykadManager.INIT_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                (new Handler(Looper.getMainLooper())).post(new Runnable() {

                    @Override
                    public void run() {// TODO: 2019/3/7 埋点

                        YunKeEngine.getInstance(mContext).yunkeFeedAd(adId, HttpConfig.AD_SHOW_YUNKE, new YunKeEngine.YunKeFeedCallBack() {
                            @Override
                            public void feedAdSuccess(String response) {
                                videoViewCallBack.onAdShow(YunkeVideoView.this);
                            }

                            @Override
                            public void feedAdFailed(String err) {
                                videoViewCallBack.onAdError(err);
                            }
                        });
                    }
                });
            }
        });
    }

    /**
     * 点击广告
     */
    private void clickAdTask() {
        ShykadManager.INIT_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                (new Handler(Looper.getMainLooper())).post(new Runnable() {

                    @Override
                    public void run() {// TODO: 2019/3/7 埋点

                        YunKeEngine.getInstance(mContext).yunkeFeedAd(adId, HttpConfig.AD_CLICK_YUNKE, new YunKeEngine.YunKeFeedCallBack() {
                            @Override
                            public void feedAdSuccess(String response) {
                                videoViewCallBack.onAdClicked(YunkeVideoView.this);
                            }

                            @Override
                            public void feedAdFailed(String err) {
                                videoViewCallBack.onAdError(err);
                            }
                        });
                    }
                });
            }
        });

    }

    /**
     * 播放视频
     *
     * @param uri rtmp地址
     */
    private void startPlay(String uri) {
        if (ijkMediaPlayer == null) {
            ijkMediaPlayer = new IjkMediaPlayer();
        }
        surfaceHolder = nativeContainer.getHolder();
        ijkMediaPlayer.setDisplay(surfaceHolder);
        try {
//            ijkMediaPlayer.setDataSource(mContext, Uri.parse(uri));
            String path = Environment.getExternalStorageDirectory().getPath()+"/V90408-184214.mp4";
            ijkMediaPlayer.setDataSource(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ijkMediaPlayer._prepareAsync();
        ijkMediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                iMediaPlayer.start();
            }
        });
        ijkMediaPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                iMediaPlayer.reset();
                return false;
            }
        });


    }

    /**
     * 设置视频地址。
     * 根据是否第一次播放视频，做不同的操作。
     *
     * @param path the path of the video.
     */
    public YunkeVideoView setVideoPath(String path) {
        if (TextUtils.equals("", mPath)) {
            //如果是第一次播放视频，那就创建一个新的surfaceView
            mPath = path;
        } else {
            //否则就直接load
            mPath = path;
            load();
        }
        return this;
    }

    /**
     * 加载视频
     */
    private void load() {
        //每次都要重新创建IMediaPlayer
        createPlayer();
        try {
            ijkMediaPlayer.setDataSource(mPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //给mediaPlayer设置视图
        ijkMediaPlayer.setDisplay(nativeContainer.getHolder());

        ijkMediaPlayer.prepareAsync();
    }

    /**
     * 创建一个新的player
     */
    private void createPlayer() {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.stop();
            ijkMediaPlayer.setDisplay(null);
            ijkMediaPlayer.release();
        }
        IjkMediaPlayer mediaPlayer = new IjkMediaPlayer();
        mediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG);

        //开启硬解码
        // ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);

        ijkMediaPlayer = mediaPlayer;

        if (listener != null) {
            ijkMediaPlayer.setOnPreparedListener(listener);
            ijkMediaPlayer.setOnInfoListener(listener);
            ijkMediaPlayer.setOnSeekCompleteListener(listener);
            ijkMediaPlayer.setOnBufferingUpdateListener(listener);
            ijkMediaPlayer.setOnErrorListener(listener);
        }
    }


    public void setListener(VideoPlayerListener listener) {
        this.listener = listener;
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.setOnPreparedListener(listener);
        }
    }

    public void start() {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.start();
        }
    }

    public void release() {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.reset();
            ijkMediaPlayer.release();
            ijkMediaPlayer = null;
        }
    }

    public void pause() {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.pause();
        }
    }

    public void stop() {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.stop();
        }
    }


    public void reset() {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.reset();
        }
    }


    public long getDuration() {
        if (ijkMediaPlayer != null) {
            return ijkMediaPlayer.getDuration();
        } else {
            return 0;
        }
    }


    public long getCurrentPosition() {
        if (ijkMediaPlayer != null) {
            return ijkMediaPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }


    public void seekTo(long l) {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.seekTo(l);
        }
    }

    public interface VideoViewCallBack{
        void onAdClicked(YunkeVideoView videoView);
        void onAdShow(YunkeVideoView videoView);
        void onAdError(String err);
        void onAdCancel(YunkeVideoView videoView);
        void onAdLoad(YunkeVideoView videoView);
    }

    public abstract class VideoPlayerListener implements IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnCompletionListener, IMediaPlayer.OnPreparedListener, IMediaPlayer.OnInfoListener, IMediaPlayer.OnVideoSizeChangedListener, IMediaPlayer.OnErrorListener, IMediaPlayer.OnSeekCompleteListener {
    }
}
