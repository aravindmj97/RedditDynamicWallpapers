package com.a.redditfetch;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.List;
import java.util.Random;

public class TimeReceiver extends BroadcastReceiver {

    ImageLoader imageLoader;
    @Override
    public void onReceive(Context context, Intent intent) {
        List<ImagePojo> imagePojos = ImagePojo.listAll(ImagePojo.class);

        imageLoader = ImageLoader.getInstance();

        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(context));

        Random random = new Random();
        int i = random.nextInt((imagePojos.size()-1)+1)+1;

        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);

        try{

            imageLoader.loadImage(imagePojos.get(i).getUrl(), new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    try{
                        wallpaperManager.setBitmap(loadedImage);
                        wallpaperManager.setBitmap((loadedImage), null, true, WallpaperManager.FLAG_LOCK);
                        Log.d("Wall Changed", "newWallAt: "+i);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
