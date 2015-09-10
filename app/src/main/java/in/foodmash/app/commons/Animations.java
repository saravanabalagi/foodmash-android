package in.foodmash.app.commons;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Zeke on Apr 08 2015.
 */
public class Animations {

    public static void fadeOut(View v, int duration) {
        if(v.getVisibility()==View.GONE) return;
        final View vf = v;
        v.animate()
                .setDuration(duration)
                .alpha(0.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        vf.setVisibility(View.GONE);
                    }
                });
    }

    public static void fadeIn(View v, int duration) {
        final View vf = v;
        v.animate()
                .setDuration(duration)
                .alpha(1.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        vf.setAlpha(0);
                        vf.setVisibility(View.VISIBLE);
                    }
                });
    }

    public static void fadeInOnlyIfInvisible(View v, int duration) {
        if(v.getVisibility()==View.VISIBLE) return;
        final View vf = v;
        v.animate()
                .setDuration(duration)
                .alpha(1.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        vf.setAlpha(0);
                        vf.setVisibility(View.VISIBLE);
                    }
                });
    }

    public static void fadeOutAndFadeIn(View v1, View v2,int duration) {
        final View vf = v1;
        final View vn = v2;
        final int d = duration;
        v1.animate()
                .setDuration(duration)
                .alpha(0.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        vf.setVisibility(View.GONE);
                        Animations.fadeIn(vn, d);
                    }
                });
    }

    public static void leftPad(View v, int leftPaddingInPixels, int duration) {
        final View vf = v;
        ValueAnimator animator = ValueAnimator.ofInt(v.getPaddingLeft(), leftPaddingInPixels);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator){
                vf.setPadding((Integer) valueAnimator.getAnimatedValue(), vf.getPaddingTop(), vf.getPaddingRight(), vf.getPaddingBottom());
            }
        });
        animator.setDuration(duration);
        animator.start();
    }

    public static void rightPad(View v, int rightPaddingInPixels, int duration) {
        final View vf = v;
        ValueAnimator animator = ValueAnimator.ofInt(v.getPaddingRight(), rightPaddingInPixels);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator){
                vf.setPadding(vf.getPaddingLeft(), vf.getPaddingTop(), (Integer) valueAnimator.getAnimatedValue(), vf.getPaddingBottom());
            }
        });
        animator.setDuration(duration);
        animator.start();
    }

    public static void topPad(View v, int topPaddingInPixels, int duration) {
        final View vf = v;
        ValueAnimator animator = ValueAnimator.ofInt(v.getPaddingTop(), topPaddingInPixels);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator){
                vf.setPadding(vf.getPaddingLeft(), (Integer) valueAnimator.getAnimatedValue(), vf.getPaddingRight(), vf.getPaddingBottom());
            }
        });
        animator.setDuration(duration);
        animator.start();
    }

    public static void bottomPad(View v, int bottomPaddingInPixels, int duration) {
        final View vf = v;
        ValueAnimator animator = ValueAnimator.ofInt(v.getPaddingBottom(), bottomPaddingInPixels);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator){
                vf.setPadding(vf.getPaddingLeft(), vf.getPaddingTop(), vf.getPaddingRight(), (Integer) valueAnimator.getAnimatedValue());
            }
        });
        animator.setDuration(duration);
        animator.start();
    }

    public static void leftMargin(View v, int leftMarginInPixels, int duration) {
        final ViewGroup.MarginLayoutParams p;
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)
            p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        else return;
        ValueAnimator animator = ValueAnimator.ofInt(p.leftMargin, leftMarginInPixels);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator){
                p.setMargins((Integer) valueAnimator.getAnimatedValue(), p.topMargin, p.rightMargin, p.bottomMargin);
            }
        });
        animator.setDuration(duration);
        animator.start();
    }

    public static void rightMargin(View v, int rightMarginInPixels, int duration) {
        final ViewGroup.MarginLayoutParams p;
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)
            p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        else return;
        ValueAnimator animator = ValueAnimator.ofInt(p.rightMargin, rightMarginInPixels);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator){
                p.setMargins(p.leftMargin, p.topMargin,(Integer) valueAnimator.getAnimatedValue(), p.bottomMargin);
            }
        });
        animator.setDuration(duration);
        animator.start();
    }

    public static void topMargin(View v, int topMarginInPixels, int duration) {
        final ViewGroup.MarginLayoutParams p;
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)
            p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        else return;
        ValueAnimator animator = ValueAnimator.ofInt(p.topMargin, topMarginInPixels);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator){
                p.setMargins(p.leftMargin, (Integer) valueAnimator.getAnimatedValue(), p.rightMargin, p.bottomMargin);
            }
        });
        animator.setDuration(duration);
        animator.start();
    }

    public static void bottomMargin(View v, int bottomMarginInPixels, int duration) {
        final ViewGroup.MarginLayoutParams p;
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)
            p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        else return;
        ValueAnimator animator = ValueAnimator.ofInt(p.bottomMargin, bottomMarginInPixels);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator){
                p.setMargins(p.leftMargin, p.topMargin, p.rightMargin, (Integer) valueAnimator.getAnimatedValue());
            }
        });
        animator.setDuration(duration);
        animator.start();
    }



}
