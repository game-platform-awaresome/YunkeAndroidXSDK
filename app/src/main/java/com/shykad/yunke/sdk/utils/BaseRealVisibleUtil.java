package com.shykad.yunke.sdk.utils;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Create by wanghong.he on 2019/3/15.
 * description：
 */
public class BaseRealVisibleUtil implements RealVisibleInterface {

    private HashMap<WeakReference<View>, OnRealVisibleListener> mTotalViewHashMap = new HashMap<>();

    private HashMap<WeakReference<View>, OnRealVisibleListener> mHaveVisibleViewHashMap = new HashMap<>();

    private HashMap<WeakReference<View>, ArrayList<Integer>> mTotalParentViewHashMap = new HashMap<>();

    private static Context mContext;

    private static BaseRealVisibleUtil instance;

    private BaseRealVisibleUtil() {

    }

    public static BaseRealVisibleUtil getInstance(Context context) {
        mContext = context;
        if (instance == null) {
            synchronized (BaseRealVisibleUtil.class) {
                if (instance == null) instance = new BaseRealVisibleUtil();
            }
        }
        return instance;
    }


    @Override
    public void registerView(View v, OnRealVisibleListener listener) {
        if (listener != null) {
            mTotalViewHashMap.put(new WeakReference<View>(v), listener);
        }
    }

    /**
     * 尽量保证 注册的view 在每次页面刷新的时候 不会被重新添加, 否则map会越来越大.
     *
     * @param view
     * @param listener
     */
    @Override
    public void registerParentView(View view, OnRealVisibleListener listener) {
        if (listener != null) {
            view.setTag(listener);
            mTotalParentViewHashMap.put(new WeakReference<View>(view), new ArrayList<Integer>());
        }
    }

    @Override
    public void calculateRealVisible() {
        Iterator iterator = mTotalViewHashMap.entrySet().iterator();
        // 下面这个写法  在遍历的时候若要对map 删除 要使用 Iterator.remove() 否则会出现ConcurrentModificationException  ;
        while (iterator.hasNext()) {
            Map.Entry<WeakReference<View>, OnRealVisibleListener> entry = (Map.Entry<WeakReference<View>, OnRealVisibleListener>) iterator.next();
            View view = entry.getKey().get();
            if (view != null) {
                if (isVisible(view)) {
                    if (view.getTag() != null && view.getTag() instanceof Integer) {
                        entry.getValue().onRealVisible((Integer) view.getTag());
                    } else {
                        entry.getValue().onRealVisible(-1); // 正常view 不需要这个参数
                    }
                    mHaveVisibleViewHashMap.put(entry.getKey(), entry.getValue());
                    iterator.remove();
                }
            } else {
                iterator.remove();
            }
        }

        for (Map.Entry<WeakReference<View>, ArrayList<Integer>> entry : mTotalParentViewHashMap.entrySet()) {
            View view = entry.getKey().get();
            if (view == null) continue;

            if (view instanceof ListView) {
                calculateListView((ListView) view, entry);
            } else if (view instanceof RecyclerView) {
                calculateRecyclerView((RecyclerView) view, entry);
            } else if (view instanceof LinearLayout) {
                calculateLinearLayout((LinearLayout) view, entry);
            }
        }
    }

    private BaseRealVisibleUtil calculateListView(ListView listView, Map.Entry<WeakReference<View>, ArrayList<Integer>> entry) {
        OnRealVisibleListener listener = (OnRealVisibleListener) listView.getTag();
        int firstVisible = listView.getFirstVisiblePosition();
        for (int i = 0; i < listView.getChildCount(); i++) {
            if (isVisible(listView) && isVisible(listView.getChildAt(i))) {
                if (!entry.getValue().contains(i + firstVisible)) {
                    if (listView.getHeaderViewsCount() > 0) { // 证明有headerview 那么第0个是headerview, 减去
                        if (i > 0) {
                            listener.onRealVisible(i + firstVisible - 1);
                        }
                    } else { // footview 的时候可能有数组越界  所以外面调用的时候一定要加判断
                        listener.onRealVisible(i + firstVisible);
                    }
                    entry.getValue().add(i + firstVisible);
                }
            }
        }
        return this;
    }

    private BaseRealVisibleUtil calculateRecyclerView(RecyclerView recyclerView, Map.Entry<WeakReference<View>, ArrayList<Integer>> entry) {
        OnRealVisibleListener listener = (OnRealVisibleListener) recyclerView.getTag();
        LinearLayoutManager layoutManager = null;
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        }
        if (layoutManager != null) {
            int firstItemPosition = layoutManager.findFirstVisibleItemPosition();
            for (int i = 0; i < layoutManager.getChildCount(); i++) {
                if (isVisible(recyclerView) && isVisible(layoutManager.getChildAt(i))) {
                    if (!entry.getValue().contains(i + firstItemPosition)) {
                        listener.onRealVisible(i + firstItemPosition);
                        entry.getValue().add(i + firstItemPosition);
                    }
                }
            }
        }

        return this;
    }

    private BaseRealVisibleUtil calculateLinearLayout(LinearLayout layout, Map.Entry<WeakReference<View>, ArrayList<Integer>> entry) {
        OnRealVisibleListener listener = (OnRealVisibleListener) layout.getTag();
        for (int i = 0; i < layout.getChildCount(); i++) {
            if (isVisible(layout) && isVisible(layout.getChildAt(i))) {
                if (!entry.getValue().contains(i)) {
                    listener.onRealVisible(i);
                    entry.getValue().add(i);
                }
            }
        }
        return this;
    }

    @Override
    public void clearRealVisibleTag() {
        mTotalViewHashMap.putAll(mHaveVisibleViewHashMap);
        for (Map.Entry<WeakReference<View>, ArrayList<Integer>> entry : mTotalParentViewHashMap.entrySet()) {
            entry.getValue().clear();
        }
    }

    /**
     * 在屏幕中是否展现
     *
     * @param v
     * @return
     */
    private boolean isVisible(View v) {
        return v.getLocalVisibleRect(new Rect());
    }

    public BaseRealVisibleUtil release() {
        mTotalViewHashMap.clear();
        mHaveVisibleViewHashMap.clear();
        mTotalParentViewHashMap.clear();
        return this;
    }

    public boolean isViewCovered(final View view) {
        View currentView = view;
        Rect currentViewRect = new Rect();
        boolean partVisible = currentView.getGlobalVisibleRect(currentViewRect);
        boolean totalHeightVisible = (currentViewRect.bottom - currentViewRect.top) >= view.getMeasuredHeight();
        boolean totalWidthVisible = (currentViewRect.right - currentViewRect.left) >= view.getMeasuredWidth();
        boolean totalViewVisible = partVisible && totalHeightVisible && totalWidthVisible;
        if (!totalViewVisible) {//if any part of the view is clipped by any of its parents,return true
            return true;
        }
        while (currentView.getParent() instanceof ViewGroup) {
            ViewGroup currentParent = (ViewGroup) currentView.getParent();
            if (currentParent.getVisibility() != View.VISIBLE) { //if the parent of view is not visible,return true
                return true;
            }
            int start = indexOfViewInParent(currentView, currentParent);
            for (int i = start + 1; i < currentParent.getChildCount(); i++) {
                Rect viewRect = new Rect();
                view.getGlobalVisibleRect(viewRect);
                View otherView = currentParent.getChildAt(i);
                Rect otherViewRect = new Rect();
                otherView.getGlobalVisibleRect(otherViewRect);
                if (Rect.intersects(viewRect, otherViewRect)) {//if view intersects its older brother(covered),return true
                    return true;
                }

            }
            currentView = currentParent;

        }
        return false;
    }

    private int indexOfViewInParent(View view, ViewGroup parent) {
        int index;
        for (index = 0; index < parent.getChildCount(); index++) {
            if (parent.getChildAt(index) == view) {
                break;
            }
        }
        return index;
    }
}