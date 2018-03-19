package com.MapPost;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.view.View;
import android.widget.ImageView;

import com.ConstantsKt;
import com.MapPost.vo.Post;
import com.MapPost.vo.PostType;
import com.bumptech.glide.Glide;

public class MapPostBindingAdapters {
    @BindingAdapter("visibleGone")
    public static void showHide(View view, boolean show) {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }


    @BindingAdapter("imageUrl")
    public static void setImageUrl(ImageView imageView, Post post) {
        if (post == null) return;
        if (post.getType() == PostType.PICTURE || post.getType() == PostType.VIDEO) {
            Context context = imageView.getContext();
            Glide.with(context).load(ConstantsKt.getS3HostName() + post.getContent()).into(imageView);
        }
    }

    @BindingAdapter("imageType")
    public static void setImageIcon(ImageView imageView, Post post) {
        switch (post.getType()) {
            case TEXT:
                imageView.setImageResource(R.drawable.ic_textsms_black_24dp);
                break;
            case AUDIO:
                imageView.setImageResource(R.drawable.ic_audiotrack_black_24dp);
                break;
            case PICTURE:
                imageView.setImageResource(R.drawable.ic_camera_alt_black_24dp);
                break;
            case VIDEO:
                imageView.setImageResource(R.drawable.ic_videocam_black_24dp);
                break;
        }
    }
}
