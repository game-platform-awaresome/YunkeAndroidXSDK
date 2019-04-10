package com.shykad.yunke.sdk.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qq.e.ads.nativ.NativeExpressADView;
import com.shykad.yunke.sdk.R;
import com.shykad.yunke.sdk.config.HttpConfig;
import com.shykad.yunke.sdk.engine.InfoFlowEngine;
import com.shykad.yunke.sdk.engine.YunKeEngine;
import com.shykad.yunke.sdk.engine.permission.config.PermissionConfig;
import com.shykad.yunke.sdk.okhttp.bean.GetAdResponse;
import com.shykad.yunke.sdk.ui.widget.YunkeTemplateView;
import com.shykad.yunke.sdk.utils.LogUtils;
import com.shykad.yunke.sdk.utils.ShykadUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.REQUEST_INSTALL_PACKAGES;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Create by wanghong.he on 2019/3/20.
 * description：
 */
public class InfoFlowRecycleViewActivity extends PermissionActivity {

    public static final int MAX_ITEMS = 50;
    public static final int AD_COUNT = 3;    // 加载广告的条数，取值范围为[1, 10]
    public static int FIRST_AD_POSITION = 1; // 第一条广告的位置
    public static int ITEMS_PER_AD = 10;     // 每间隔10个条目插入一条广告
    private String slotId = "1086106659408973838";

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    private List<InfoFlowItem> mNormalDataList = new ArrayList<InfoFlowItem>();
    private InfoFlowAdapter infoFlowAdapter;
    private HashMap<ViewGroup, Integer> mAdViewPositionMap = new HashMap<ViewGroup, Integer>();
    private List<NativeExpressADView> mAdViewList;
    private List<YunkeTemplateView>  mAdYunkeViewList;
    private List<ViewGroup> mTTViewList;
    private GetAdResponse.AdCotent adCotent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.yunke_activity_infoflow_recycle_ad);
        initView();
        initData();
    }

    @SuppressLint("WrongConstant")
    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycleView);
        mRecyclerView.setHasFixedSize(true);
        mLinearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
    }

    private void initData() {
        for (int i=0;i<=MAX_ITEMS;++i){
            mNormalDataList.add(new InfoFlowItem("No." + i + " Normal Data"));
        }
        infoFlowAdapter = new InfoFlowAdapter(mNormalDataList);
        mRecyclerView.setAdapter(infoFlowAdapter);
        permissTask(slotId);
    }

    /**
     * 请求权限 强烈建议合适的时机调用 防止获取不了相应权限，下载广告应用没有安装或应用安装失败的问题
     * 权限请求如不满足需求，请自行开发
     */
    public void permissionInstallTask() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            boolean installs = getPackageManager().canRequestPackageInstalls();
            if (installs){
                //需要用户手动运行，拥有权限去执行
                LogUtils.i("已获取install package权限");
            }else {
                String[] perms = {REQUEST_INSTALL_PACKAGES};
                performCodeWithPermission(getString(R.string.rationale_install_package), PermissionConfig.REQUEST_INSTALL_PACKAGES_PERM, perms, new PermissionCallback() {
                    @Override
                    public void hasPermission(List<String> allPerms) {
                        //需要用户手动运行，拥有权限去执行
                        LogUtils.i("已获取install package权限");
                    }

                    @Override
                    public void noPermission(List<String> deniedPerms, List<String> grantedPerms, Boolean hasPermanentlyDenied) {
                        if (hasPermanentlyDenied) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                            startActivityForResult(intent, PermissionConfig.REQUEST_INSTALL_PACKAGES_PERM);
                            LogUtils.i("获取install package权限被拒");
                        }
                    }
                });
            }
        }

    }

    /**
     * 注意: 在信息流广告中，请先获取广告数据 再初始化adapter!!!
     * 请求权限 强烈建议合适的时机调用 防止获取不了相应权限，下载广告没有填充或者获取广告失败的问题
     * 权限请求如不满足需求，请自行开发
     */
    public void permissTask(String slotId) {
        String[] perms = {READ_PHONE_STATE,ACCESS_FINE_LOCATION,ACCESS_COARSE_LOCATION,WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE};
        performCodeWithPermission(getString(R.string.rationale_permissions), PermissionConfig.REQUEST_PERMISSIONS_PERM, perms, new PermissionCallback() {
            @Override
            public void hasPermission(List<String> allPerms) {
                getAd(slotId);//需要拥有权限去执行
                LogUtils.i("已获取phone、location、storage权限");
            }

            @Override
            public void noPermission(List<String> deniedPerms, List<String> grantedPerms, Boolean hasPermanentlyDenied) {
                if (hasPermanentlyDenied) {
                    alertAppSetPermission(getString(R.string.rationale_ask_again), PermissionConfig.REQUEST_PERMISSIONS_PERM);
                    LogUtils.i("获取phone、location、storage权限被拒");
                }
            }
        });
    }

    /**
     * 获取广告
     */
    private void getAd(String slotId) {

        YunKeEngine.getInstance(this).yunkeGetAd(HttpConfig.ADTYPE_FEED, slotId, ShykadUtils.getIMEI(this), new YunKeEngine.YunKeAdCallBack() {

            @Override
            public void getAdSuccess(Object response) {
                if (response instanceof GetAdResponse){
                    adCotent = ((GetAdResponse) response).getData();
                    launchInfoFlowAd(response);
                }

            }

            @Override
            public void getAdFailed(String err) {
                if (!TextUtils.isEmpty(err)){
                    LogUtils.d(err);
                }
            }
        });
    }

    /**
     * 加载信息流广告
     */
    private void launchInfoFlowAd(Object response) {
        InfoFlowEngine.getInstance(InfoFlowRecycleViewActivity.this).initEngine(response, new InfoFlowEngine.InfoFlowAdCallBack() {
            @Override
            public void onAdLoad(List<?> adList,int channel) {
                LogUtils.d("feed信息流广告数据加载完毕");
                if (channel==HttpConfig.AD_CHANNEL_TENCENT){

                    mAdViewList = (List<NativeExpressADView>) adList;
                    for (int i = 0; i < mAdViewList.size(); i++) {
                        int position = FIRST_AD_POSITION + ITEMS_PER_AD * i;
                        if (position < mNormalDataList.size()) {
                            NativeExpressADView view = mAdViewList.get(i);
                            mAdViewPositionMap.put(view, position); // 把每个广告在列表中位置记录下来
                            infoFlowAdapter.addAdToPosition(position, mAdViewList.get(i));
                        }
                    }
                }else if (channel == HttpConfig.AD_CHANNEL_YUNKE){
                    mAdYunkeViewList = (List<YunkeTemplateView>) adList;
                    for (int i = 0; i < mAdYunkeViewList.size(); i++) {
                        int position = FIRST_AD_POSITION + ITEMS_PER_AD * i;
                        if (position < mNormalDataList.size()) {
                            YunkeTemplateView view = mAdYunkeViewList.get(i);
                            mAdViewPositionMap.put(view, position);
                            infoFlowAdapter.addAdToPosition(position, mAdYunkeViewList.get(i));
                        }
                    }
                }else if (channel == HttpConfig.AD_CHANNEL_BYTEDANCE){
                    mTTViewList = (List<ViewGroup>) adList;
                    for (int i = 0; i < mTTViewList.size(); i++) {
                        int position = FIRST_AD_POSITION + ITEMS_PER_AD * i;
                        if (position < mNormalDataList.size()) {
                            ViewGroup view = mTTViewList.get(i);
                            mAdViewPositionMap.put(view, position);
                            infoFlowAdapter.addAdToPosition(position, mTTViewList.get(i));
                        }
                    }
                }
                infoFlowAdapter.notifyDataSetChanged();
            }

            @Override
            public void onAdShow() {
                LogUtils.d("feed信息流广告展示");
            }

            @Override
            public void onAdClick(boolean isJump) {
                if (isJump){
                    Intent intent = new Intent();
                    intent.setClass(InfoFlowRecycleViewActivity.this, WebviewActivity.class);
                    intent.putExtra("url",adCotent.getTarget());
                    intent.putExtra("title","广告");
                    startActivity(intent);
                }
            }

            @Override
            public void onAdError(String err) {
                LogUtils.d("feed信息流广告异常："+err);
            }

            @Override
            public void onAdCancel(Object adView) {
                LogUtils.d("feed信息流广告关闭");
                if (adView instanceof NativeExpressADView){
                    if (infoFlowAdapter != null) {
                        int removedPosition = mAdViewPositionMap.get(adView);
                        infoFlowAdapter.removeADView(removedPosition, (NativeExpressADView)adView);
                    }
                }else if (adView instanceof YunkeTemplateView){
                    if (infoFlowAdapter != null) {
                        int removedPosition = mAdViewPositionMap.get(adView);
                        infoFlowAdapter.removeADView(removedPosition, (YunkeTemplateView)adView);
                    }
                }else if (adView instanceof ViewGroup){
                    if (infoFlowAdapter != null) {
                        int removedPosition = mAdViewPositionMap.get(adView);
                        infoFlowAdapter.removeADView(removedPosition, (ViewGroup) adView);
                    }
                }


            }
        }).launchInfoFlow(infoFlowAdapter.getContainer(),AD_COUNT);
    }

    /**
     * RecyclerView的item
     */
    public class InfoFlowItem{
        private String title;

        public InfoFlowItem(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    /**
     * RecyclerView的adapter
     */
    public class InfoFlowAdapter extends RecyclerView.Adapter<InfoFlowAdapter.MyViewHolder>{

        private static final int TYPE_DATA = 0;
        private static final int TYPE_AD = 1;
        private List<Object> mData;
        private ViewGroup container;
        private MyViewHolder myViewHolder;

        public InfoFlowAdapter(List mData){
            this.mData = mData;
        }

        @NonNull
        @Override
        public InfoFlowAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.yunke_activity_infoflow_list_ad_item,null);
            myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull InfoFlowAdapter.MyViewHolder viewHolder, int position) {
            int type = getItemViewType(position);
            if (TYPE_AD == type) {
                viewHolder.title.setVisibility(View.GONE);
                viewHolder.container.setVisibility(View.VISIBLE);
                container = viewHolder.container;
                bindViewData(viewHolder,position,mData);

            }else {
                viewHolder.title.setVisibility(View.VISIBLE);
                viewHolder.container.setVisibility(View.GONE);
                viewHolder.title.setText(((InfoFlowItem) mData.get(position)).getTitle());
            }
        }

        @Override
        public int getItemCount() {
            if (mData != null) {
                return mData.size();
            } else {
                return 0;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (mData.get(position) instanceof NativeExpressADView){
                return TYPE_AD;
            }else if (mData.get(position) instanceof YunkeTemplateView){
                return TYPE_AD;
            }else if(mData.get(position) instanceof ViewGroup){
                return TYPE_AD;
            }else {
                return TYPE_DATA;
            }
        }

        private void bindViewData(MyViewHolder viewHolder, int position, List<Object> mData){

            if (adCotent.getChannel() == HttpConfig.AD_CHANNEL_TENCENT){
                NativeExpressADView adView = (NativeExpressADView) mData.get(position);
                mAdViewPositionMap.put(adView, position); // 广告在列表中的位置是可以被更新的
                if (viewHolder.container.getChildCount() > 0 && viewHolder.container.getChildAt(0) == adView) {
                    return;
                }

                if (viewHolder.container.getChildCount() > 0) {
                    viewHolder.container.removeAllViews();
                }

                if (adView.getParent() != null) {
                    ((ViewGroup) adView.getParent()).removeView(adView);
                }

                viewHolder.container.addView(adView);

                adView.render(); // 调用render方法后sdk才会开始展示广告
            }else if (adCotent.getChannel() == HttpConfig.AD_CHANNEL_YUNKE){
                YunkeTemplateView adView = (YunkeTemplateView) mData.get(position);
                mAdViewPositionMap.put(adView, position); // 广告在列表中的位置是可以被更新的
                if (viewHolder.container.getChildCount() > 0 && viewHolder.container.getChildAt(0) == adView) {
                    return;
                }

                if (viewHolder.container.getChildCount() > 0) {
                    viewHolder.container.removeAllViews();
                }

                if (adView.getParent() != null) {
                    ((ViewGroup) adView.getParent()).removeView(adView);
                }

                viewHolder.container.addView(adView);
            }else if (adCotent.getChannel() == HttpConfig.AD_CHANNEL_BYTEDANCE){
                ViewGroup adView = (ViewGroup) mData.get(position);
                mAdViewPositionMap.put(adView, position); // 广告在列表中的位置是可以被更新的
                if (viewHolder.container.getChildCount() > 0 && viewHolder.container.getChildAt(0) == adView) {
                    return;
                }

                if (viewHolder.container.getChildCount() > 0) {
                    viewHolder.container.removeAllViews();
                }

                if (adView.getParent() != null) {
                    ((ViewGroup) adView.getParent()).removeView(adView);
                }

                viewHolder.container.addView(adView);
            }


        }

        /**
         * 供外部调用 把返回的ADView添加到数据集里面去
         * @param position
         * @param adView
         */
        public void addAdToPosition(int position,ViewGroup adView){
            if(position >= 0 && position < mData.size() && adView != null){
                mData.add(position, adView);
            }
        }

        /**
         * 移除NativeExpressADView的时候是一条一条移除的
         * @param position
         * @param adView
         */
        public void removeADView(int position, ViewGroup adView) {
            mData.remove(position);
            infoFlowAdapter.notifyItemRemoved(position); // position为adView在当前列表中的位置
            infoFlowAdapter.notifyItemRangeChanged(0, mData.size() - 1);
        }

        /**
         * @return
         */
        public ViewGroup getContainer(){
            if (container!=null){
                return container;
            }else {
                return myViewHolder.container;
            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder{

            private TextView title;
            private ViewGroup container;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.infoflow_listview_ad_title);
                container = (ViewGroup) itemView.findViewById(R.id.infoflow_listview_ad_container);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        InfoFlowEngine.getInstance(this).destoryAd(mAdViewList);
    }
}
