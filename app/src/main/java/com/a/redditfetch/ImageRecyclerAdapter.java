package com.a.redditfetch;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import static com.bumptech.glide.request.RequestOptions.centerCropTransform;
import static com.bumptech.glide.request.RequestOptions.fitCenterTransform;
import static com.bumptech.glide.request.RequestOptions.overrideOf;

public class ImageRecyclerAdapter extends RecyclerView.Adapter<ImageRecyclerAdapter.ViewHolder> {

    ArrayList<ImagePojo> imagePojos = new ArrayList<>();
    private Context context;

    public ImageRecyclerAdapter(ArrayList<ImagePojo> imagePojos, Context context) {
        this.imagePojos = imagePojos;
        this.context = context;
    }

    @NonNull
    @Override
    public ImageRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_layout, parent, false);
        return new ImageRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageRecyclerAdapter.ViewHolder holder, int position) {
        Glide.with(context).load(imagePojos.get(position).getUrl()) // resizes the image to these dimensions (in pixel)
                .apply(centerCropTransform()).apply(fitCenterTransform()).into(holder.imageView);

        holder.setItemClickListner(new RecyclerItemClickListner() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                Intent intent = new Intent(context, ImageShowActivity.class);
                intent.putExtra("url", imagePojos.get(position).getUrl());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagePojos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView imageView;

        private RecyclerItemClickListner recyclerItemClickListner;

        public ViewHolder(View itemView) {
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.images);

            itemView.setOnClickListener(this);
        }
        public void setItemClickListner(RecyclerItemClickListner recyclerItemClickListner){
            this.recyclerItemClickListner = recyclerItemClickListner;
        }

        @Override
        public void onClick(View view) {
            recyclerItemClickListner.onClick(view, getAdapterPosition(), false);
        }
    }
}
