/*
 * Copyright 2014 Google Inc. All Rights Reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eje_c.rajawalicardboard;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.vrtoolkit.cardboard.Viewport;

/**
 * Contains two sub-views to provide a simple stereo HUD.
 */
public class CardboardOverlayView extends RelativeLayout {
    private final CardboardOverlayEyeView leftView;
    private final CardboardOverlayEyeView rightView;
    private AlphaAnimation textFadeAnimation;
    private boolean cardboard = true;

    public CardboardOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //setOrientation(HORIZONTAL);


        LayoutParams params = new LayoutParams(1000, 1000);
        params.setMargins(0, 0, 0, 0);

        leftView = new CardboardOverlayEyeView(context, attrs);
        leftView.setLayoutParams(params);
        addView(leftView);

        rightView = new CardboardOverlayEyeView(context, attrs);
        rightView.setLayoutParams(params);
        addView(rightView);


        // Set some reasonable defaults.
        setDepthOffset(0.01f);
        setColor(context.getResources().getColor(R.color.ToastColor));
        setVisibility(View.VISIBLE);

        textFadeAnimation = new AlphaAnimation(1.0f, 0.0f);
        textFadeAnimation.setDuration(5000);
    }

    public void reLayout(Viewport left, Viewport right, Viewport one, int height) {
        /*measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        int widht = getMeasuredWidth();
        int height = getMeasuredHeight();*/
        Log.v("Overlay", "" + height);
        LayoutParams params = new LayoutParams(left.width, left.height);
        params.setMargins(left.x, height - left.height + left.y, 0, 0);
        leftView.setLayoutParams(params);

        params = new LayoutParams(right.width, right.height);
        params.setMargins(right.x, height - right.height + right.y, 0, 0);
        rightView.setLayoutParams(params);
    }

    public void show3DToast(String message) {
        setText(message);
        setTextAlpha(1f);
        textFadeAnimation.setAnimationListener(new EndAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                setTextAlpha(0f);
            }
        });
        startAnimation(textFadeAnimation);
    }

    public void setCardboard(boolean cardboard) {
        this.cardboard = cardboard;
    }

    public void clear() {
        clear3dToast();
        StopVideo();
        clearImage();
    }

    public void clear3dToast() {
        clearAnimation();
        setText("");
        setTextAlpha(0f);
    }

    public void PlayVideo(String path) {
        leftView.PlayVideo(path);
    }

    public void StopVideo() {
        leftView.StopVideo();
    }

    private abstract class EndAnimationListener implements Animation.AnimationListener {
        @Override public void onAnimationRepeat(Animation animation) {}
        @Override public void onAnimationStart(Animation animation) {}
    }

    private void setDepthOffset(float offset) {
        leftView.setOffset(offset);
        rightView.setOffset(-offset);
    }

    private void setText(String text) {
        leftView.setText(text);
        if(cardboard)
            rightView.setText(text);
    }

    private void setTextAlpha(float alpha) {
        leftView.setTextViewAlpha(alpha);
        rightView.setTextViewAlpha(alpha);
    }

    private void setColor(int color) {
        leftView.setColor(color);
        rightView.setColor(color);
    }

    public void setImage(int id) {
        leftView.setImage(id);
        if(cardboard)
            rightView.setImage(id);
    }

    public void setImage(String image) {
        leftView.setImage(image);
        if(cardboard)
            rightView.setImage(image);
    }

    public void clearImage() {
        leftView.clearImage();
        if(cardboard)
            rightView.clearImage();
    }

    public void setTextSize(float size) {
        leftView.setTextSize(size);
        if(cardboard)
            rightView.setTextSize(size);
    }

    /**
     * A simple view group containing some horizontally centered text underneath a horizontally
     * centered image.
     *
     * <p>This is a helper class for CardboardOverlayView.
     */
    private class CardboardOverlayEyeView extends ViewGroup {
        private final ImageView imageView;
        private final TextView textView;
        private float offset;

        private VideoView videoView;
        private MediaPlayer mediaPlayer;

        public CardboardOverlayEyeView(Context context, AttributeSet attrs) {
            super(context, attrs);
            imageView = new ImageView(context, attrs);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setAdjustViewBounds(true);  // Preserve aspect ratio.
            addView(imageView);

            textView = new TextView(context, attrs);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14.0f);
            textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
            textView.setGravity(Gravity.CENTER);
            textView.setShadowLayer(3.0f, 0.0f, 0.0f, Color.DKGRAY);
            addView(textView);
        }

        public void PlayVideo(String path) {
            videoView = new VideoView(getContext());
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer = mp;
                    mediaPlayer.setLooping(true);
                }
            });

            videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    show3DToast("Failed to Play Video");
                    return true;
                }
            });
            /*RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(1000, 1000);
            lp.addRule(RelativeLayout.);*/
            videoView.setMediaController(new MediaController(getContext()));
            videoView.setVideoPath(path);
            //videoView.requestFocus();

            videoView.start();
            addView(videoView);
            videoView.setZOrderOnTop(true);
            requestLayout();

        }

        public void setTextSize(float size) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
        }

        public void StopVideo() {
            if(videoView != null) {
                removeView(videoView);
                videoView.stopPlayback();
                videoView = null;
                mediaPlayer = null;
            }
        }

        public void setColor(int color) {
            //imageView.setImageResource(R.drawable.east_nx);
            //imageView.setColorFilter(color);
            textView.setTextColor(color);
        }

        public void setText(String text) {
            textView.setText(text);
        }

        public void setImage(int id) {
            imageView.setImageResource(id);
        }

        public void setImage(String image) {
            imageView.setImageURI(Uri.parse(image));
        }

        public void clearImage() {
            imageView.setImageResource(0);
        }

        public void setTextViewAlpha(float alpha) {
            textView.setAlpha(alpha);
        }

        public void setOffset(float offset) {
            this.offset = offset;
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            // Width and height of this ViewGroup.
            final int width = right - left;
            final int height = bottom - top;

            // The size of the image, given as a fraction of the dimension as a ViewGroup.
            // We multiply both width and heading with this number to compute the image's bounding
            // box. Inside the box, the image is the horizontally and vertically centered.
            final float imageSize = 0.9f;
            final float textSize = 0.9f;

            // The fraction of this ViewGroup's height by which we shift the image off the
            // ViewGroup's center. Positive values shift downwards, negative values shift upwards.
            final float verticalImageOffset = 0.0f;

            // Vertical position of the text, specified in fractions of this ViewGroup's height.
            final float verticalTextPos = 0.52f;

            // Layout ImageView
            float adjustedOffset = offset;
            // If the half screen width is bigger than 1000 pixels, that means it's a big screen
            // phone and we need to use a different offset value.
            if (width > 1000) {
                adjustedOffset = 3.8f * offset;
            }
            float imageMargin = 0;
            float leftMargin = 0;
            float topMargin = 0;
            imageView.layout(
                    (int) leftMargin, (int) topMargin,
                    (int) (leftMargin + width), (int) (topMargin + height));

            if(videoView != null) {
                int videoPlayerWidth = width;
                int videoPlayerHeight = height;
                int videoLeftMargin = (int) leftMargin;
                int videoTopMargin = (int) topMargin;
                if(mediaPlayer != null) {
                    int videoWidth = mediaPlayer.getVideoWidth();
                    int videoHeight = mediaPlayer.getVideoHeight();
                    videoPlayerWidth = height * videoWidth / videoHeight;
                    if(videoPlayerWidth > width) {
                        videoPlayerWidth = width;
                        videoPlayerHeight = width * videoHeight / videoWidth;
                    }
                    Log.v("Overlay", "Height: " + height+ " Width: "+width+ " Video Height: "+videoHeight+ " Width:"+videoWidth+ " Calculated Height: "+videoPlayerHeight+ " Width: "+videoPlayerWidth);

                    if (videoPlayerWidth > 1000) {
                        adjustedOffset = 3.8f * offset;
                    }
                    //videoLeftMargin = (int) (videoPlayerWidth * (imageMargin + adjustedOffset));
                    videoLeftMargin =  (width - videoPlayerWidth) / 2;
                    videoTopMargin = (height - videoPlayerHeight) / 2;
                }

                videoView.layout(
                        videoLeftMargin, videoTopMargin,
                        (videoLeftMargin + videoPlayerWidth), (videoTopMargin + videoPlayerHeight));
            }

            // Layout TextView
            leftMargin = adjustedOffset * width;
            topMargin = height * verticalTextPos;
            textView.layout(
                    (int) leftMargin, (int) topMargin,
                    (int) (leftMargin + width * textSize), (int) (topMargin + height * (1.0f - verticalTextPos)));
        }
    }
}
