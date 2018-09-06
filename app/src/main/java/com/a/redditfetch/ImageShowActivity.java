package com.a.redditfetch;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.net.URI;


public class ImageShowActivity extends AppCompatActivity {

    String Url;
    PhotoView imageView;
    FloatingActionButton fab;
    ImageLoader imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_show);

         imageLoader = ImageLoader.getInstance();

        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(ImageShowActivity.this));

        imageView = (PhotoView) findViewById(R.id.showImg);
        fab = (FloatingActionButton) findViewById(R.id.makeWall);

        try{
            Bundle b = new Bundle();
            b = getIntent().getExtras();
            Url = b.getString("url");
        }catch (Exception e){
            e.printStackTrace();
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());

                try{

                    imageLoader.loadImage(Url, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            try{
                                wallpaperManager.setBitmap(loadedImage);
                                wallpaperManager.setBitmap((loadedImage), null, true, WallpaperManager.FLAG_LOCK);
                                Toast.makeText(ImageShowActivity.this, "Applied the Wallpaper", Toast.LENGTH_SHORT).show();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        Glide.with(this).load(Url) // resizes the image to these dimensions (in pixel)
                .into(imageView);
    }

    public void dismiss(View view) {
        finish();
    }
}
