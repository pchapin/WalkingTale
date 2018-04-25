package com.talkingwhale.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.BindingAdapter;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.talkingwhale.R;
import com.talkingwhale.pojos.Post;
import com.talkingwhale.pojos.PostType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.talkingwhale.pojos.PostKt.getDrawableForPost;

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
            Glide.with(context).load(context.getResources().getString(R.string.s3_hostname) + post.getContent()).into(imageView);
        }
    }

    @BindingAdapter("imageType")
    public static void setImageIcon(ImageView imageView, Post post) {
        if (post == null) return;
        imageView.setImageResource(getDrawableForPost(post));
    }

    @BindingAdapter("dateTime")
    public static void setDateTime(TextView textView, Post post) {
        if (post == null) return;
        Context context = textView.getContext();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean displayDate = sp.getBoolean(context.getResources().getString(R.string.pref_key_date_display), false);
        if (displayDate) {
            Date date = new Date(Long.parseLong(post.getDateTime()));
            textView.setText(new SimpleDateFormat("MMM d", Locale.US).format(date));
        } else {
            textView.setText("");
        }
    }

    @BindingAdapter("userName")
    public static void setUserName(TextView textView, Post post) {
        if (post == null) return;
        Context context = textView.getContext();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean displayUsername = sp.getBoolean(context.getResources().getString(R.string.pref_key_user_display), true);
        if (displayUsername) {
            textView.setText(post.getUserName());
        } else {
            textView.setText("");
        }
    }
}
