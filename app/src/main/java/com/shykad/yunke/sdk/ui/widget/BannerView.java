package com.shykad.yunke.sdk.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.shykad.yunke.sdk.R;
import com.shykad.yunke.sdk.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * Create by wanghong.he on 2019/3/6.
 * description：
 */
public class BannerView extends FrameLayout {

    private View mView;
    //广告image
    private Context mContext;
    //ViewPager
    private ViewPager mViewPage;
    //指针显示容器
    private LinearLayout mLinearLayout;
    //指针控件容器
    private List<View> mPointView = new ArrayList<>();
    //指针个数
    private int pointSize = 0;
    //轮播间隔(ms)
    private final int DIV_TIME = 3000;
    private Handler mHandler = new Handler();
    private MRunnable mMRunnable;
    //滑动方向
    private boolean isPageOrientation = true;

    public BannerView(Context context) {
        this(context, null);
    }

    public BannerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    /**
     * 初始化加载UI
     */
    private void initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.yunke_view_banner_item, this);
        mViewPage = view.findViewById(R.id.cu_viewPage);
        mLinearLayout = view.findViewById(R.id.cu_pointer);
        mLinearLayout.setGravity(Gravity.CENTER);
        mViewPage.addOnPageChangeListener(new MPageChangeListener());
    }


    /**
     * 设置轮播UI
     *
     * @param data
     */
    public void setImageData(List<View> data) {
        pointSize = data.size();
        if (data != null && pointSize > 0) {
            MPageAdapter mMaxPageAdapter = new MPageAdapter(data);
            mViewPage.setAdapter(mMaxPageAdapter);
            initPoint();
        }
    }

    /**
     * 开启轮播
     */
    public void start() {
        if (mMRunnable == null) {
            mMRunnable = new MRunnable();
        }
        mHandler.postDelayed(mMRunnable, DIV_TIME);
    }

    /**
     * 停止轮播
     */
    public void stop() {
        if (mMRunnable != null) {
            mHandler.removeCallbacks(mMRunnable);
        }
    }

    /**
     * 初始化指针
     */
    private void initPoint() {
        for (int i = 0; i < pointSize; i++) {
            View view = new View(mContext);
            int viewSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
            LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(viewSize, viewSize);
            mLayoutParams.leftMargin = mLayoutParams.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
            view.setLayoutParams(mLayoutParams);
            view.setBackground(ContextCompat.getDrawable(mContext, R.drawable.yunke_banner_point_background));
            view.setSelected(false);
            mLinearLayout.addView(view);
            mPointView.add(view);
        }
        mPointView.get(0).setSelected(true);
    }

    /**
     * ViewPage适配器
     */
    private class MPageAdapter extends PagerAdapter {
        private List<View> data;

        public MPageAdapter(List<View> data) {
            this.data = data;
        }

        @Override
        public int getCount() {
            return data != null ? data.size() : 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            mView = data.get(position);
            ViewGroup parent = (ViewGroup) mView.getParent();
            if (parent != null) {
                parent.removeAllViews();
            }
            container.addView(mView);
            return mView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    /**
     * ViewPage滑动监听
     */
    private class MPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (position == 0) {
                isPageOrientation = true;
            }
            if (position == (pointSize - 1)) {
                isPageOrientation = false;
            }
            //将所有指针置为默认状态
            for (int i = 0; i < pointSize; i++) {
                mPointView.get(i).setSelected(false);
            }
            //将当前界面对面的指针置为选中状态
            mPointView.get(position).setSelected(true);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    /**
     * 定时任务
     */
    private class MRunnable implements Runnable {

        @Override
        public void run() {
            mHandler.postDelayed(mMRunnable, DIV_TIME);
            int mCurrentItem = mViewPage.getCurrentItem();
            if (isPageOrientation) {
                mCurrentItem++;
            } else {
                mCurrentItem--;
            }
            mViewPage.setCurrentItem(mCurrentItem);
        }
    }
    private List<View> bannerList;
    private BannerViewListener bannerViewListener;
    /**
     * @param activity
     * @param slotId 广告位id
     * @param type 广告类型：0 展示 1 点击
     */
    public void lanchBanner(final Activity activity, String slotId,int type,List<View> bannerList,BannerViewListener bannerViewListener) {
        this.bannerList = bannerList;
        this.bannerViewListener = bannerViewListener;
        if (!StringUtils.isEmpty(slotId) && !StringUtils.isEmpty(type) && activity!=null){
            setImageData(this.bannerList);
            start();
//            ShykadManager.INIT_EXECUTOR.execute(new Runnable() {
//                @Override
//                public void run() {
//                    (new Handler(Looper.getMainLooper())).post(new Runnable() {
//                        @Override
//                        public void run() {
//                            setImageData(bannerList);
//                            start();
//
////                            mView.setOnClickListener(new OnClickListener() {
////                                @Override
////                                public void onClick(View v) {
////                                    if (bannerViewListener!=null){
////                                        bannerViewListener.bannerClickListener(mViewPage.getCurrentItem());
////                                    }
////                                }
////                            });
//
//
//                        }
//                    });
//                }
//            });
        }

    }

    public interface BannerViewListener{
        void bannerClickListener(int position);
    }



}
