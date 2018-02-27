/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.binding;

import android.databinding.BindingAdapter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.R;
import com.github.aws.ConstantsKt;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

/**
 * Binding adapters that work with a fragment instance.
 */
public class FragmentBindingAdapters {
    private static final String s3HostName = ConstantsKt.getS3HostName();
    private final Fragment fragment;
    private final String TAG = this.getClass().getSimpleName();
    private MediaPlayer mp = new MediaPlayer();

    @Inject
    public FragmentBindingAdapters(Fragment fragment) {
        this.fragment = fragment;
    }

    @BindingAdapter("imageUrl")
    public void bindImage(ImageView imageView, String url) {
        Glide.with(fragment)
                .load(s3HostName + url)
                .apply(new RequestOptions().placeholder(R.drawable.white))
                .error(Glide.with(fragment).load(url))
                .into(imageView);
    }

    @BindingAdapter("audioUrl")
    public void bindAudio(View view, String url) {
        // https://developer.android.com/reference/android/media/MediaPlayer.html

        view.setOnClickListener(v -> {
            if (mp.isPlaying()) {
                mp.stop();
                return;
            }

            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.reset();
            try {
                if (new File(url).isFile()) {
                    mp.setDataSource(url);
                } else {
                    mp.setDataSource(s3HostName + url);
                }
            } catch (IOException e) {
                Log.i(TAG, "" + e);
            }
            mp.prepareAsync();
            mp.setOnPreparedListener(MediaPlayer::start);
        });
    }
}
