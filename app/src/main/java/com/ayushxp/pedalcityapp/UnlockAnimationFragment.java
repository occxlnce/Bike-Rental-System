package com.ayushxp.pedalcityapp;

import android.animation.Animator;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airbnb.lottie.LottieAnimationView;


public class UnlockAnimationFragment extends Fragment {

    LottieAnimationView unlockAnimationView, unlockTextAnimationView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_unlock_animation, container, false);


        unlockAnimationView = view.findViewById(R.id.unlock_animation);
        unlockTextAnimationView = view.findViewById(R.id.unlock_text_animation);

        unlockAnimationView.setAnimation(R.raw.unlock_animation);
        unlockTextAnimationView.setAnimation(R.raw.unlock_text_animation);

        unlockAnimationView.playAnimation();
        unlockTextAnimationView.playAnimation();


        unlockAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                // No action needed
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                navigateToNewFragment();
            }
            @Override
            public void onAnimationCancel(Animator animation) {
                // No action needed
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
                // No action needed
            }
        });


        return view;
    }

    private void navigateToNewFragment() {
        Fragment ongoingRideFragment = new OngoingRideFragment(); // Replace with your actual fragment

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, ongoingRideFragment);
        transaction.commit();
    }

}