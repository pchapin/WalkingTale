package com.talkingwhale;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.view.View;
import android.widget.ImageView;

import com.ConstantsKt;
import com.bumptech.glide.Glide;
import com.talkingwhale.vo.Post;
import com.talkingwhale.vo.PostType;

import static com.talkingwhale.vo.PostKt.getDrawableForPost;

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
