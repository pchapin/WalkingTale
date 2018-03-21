package com.MapPost;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.view.View;
import android.widget.ImageView;

import com.ConstantsKt;
import com.MapPost.vo.Post;
import com.MapPost.vo.PostType;
import com.bumptech.glide.Glide;

import static com.MapPost.vo.PostKt.getDrawableForPost;

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
        imageView.setImageResource(getDrawableForPost(post));
    }
}
