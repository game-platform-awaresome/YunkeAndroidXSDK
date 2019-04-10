package com.shykad.yunke.sdk.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;

/**
 * Create by wanghong.he on 2019/3/6.
 * description：
 */
public class GlidImageManager {
    private static GlidImageManager instance;

    private GlidImageManager(){

    }

    public static GlidImageManager getInstance(){
        if(instance == null){
            synchronized (GlidImageManager.class){
                if(instance == null) instance = new GlidImageManager();
            }
        }
        return instance;
    }

    public void loadImageUri(Context context, String img_url, ImageView imageView, int default_img) {

        Glide.with(context)                             //配置上下文
                .load(Uri.fromFile(new File(img_url)))      //设置图片路径(fix #8,文件名包含%符号 无法识别和显示)
                .error(default_img)           //设置错误图片
                .placeholder(default_img)     //设置占位图片
                .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存全尺寸
                .into(imageView);
    }


    public void loadImage(Context context, int res_id, ImageView imageView, int default_img) {
        Glide.with(context)                             //配置上下文
                .load(res_id)      //设置图片路径(fix #8,文件名包含%符号 无法识别和显示)
                .error(default_img)           //设置错误图片
                .placeholder(default_img)     //设置占位图片
                .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存全尺寸
                .into(imageView);
    }

    public void loadImageView(Context context, String imageUrl,ImageView imageView, int default_img){
        Glide.with(context)
                .load(imageUrl)
                .placeholder(default_img)
                .error(default_img)
                .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存全尺寸
                .into(imageView);
    }

    public interface GlideLoadBitmapCallback{
        void getBitmapCallback(Bitmap bitmap);
    }

    public void getBitmap(Context context, String uri,int default_img, final GlideLoadBitmapCallback callback) {
        Glide.with(context)
                .load(uri)
                .asBitmap()
                .placeholder(default_img)
                .error(default_img)
                .centerCrop()
                .override(150, 150)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                        callback.getBitmapCallback(bitmap);
                    }
                });
    }

    public void getBitmap(Context context, int res_id,int default_img, final GlideLoadBitmapCallback callback) {
        Glide.with(context)
                .load(res_id)
                .asBitmap()
                .placeholder(default_img)
                .error(default_img)
                .centerCrop()
                .override(150, 150)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                        callback.getBitmapCallback(bitmap);
                    }
                });
    }
}
