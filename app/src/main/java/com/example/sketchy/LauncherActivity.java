package com.example.sketchy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jawnnypoo.physicslayout.Physics;
import com.jawnnypoo.physicslayout.PhysicsRelativeLayout;
import com.yangp.ypwaveview.YPWaveView;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class LauncherActivity extends AppCompatActivity {

    private final Random random = new Random();
    public PhysicsRelativeLayout circleContainerLauncher;
    public BlurView blurView;
    private final Handler handler = new Handler();

    private boolean initiated = false;

    public boolean isMultiplierTime = false;

    public ArrayList<Ability> abilities = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the activity to full screen mode
        // Remove the status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

// Hide the navigation bar on devices that support it
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= 18) {
            uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.launcher_activity);
        Objects.requireNonNull(getSupportActionBar()).hide();

        // Generate a new circle every 2 seconds
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!initiated) {
                    generateCircle();
                    handler.postDelayed(this, 500);
                }


            }
        }, 500);



        TextView textView = findViewById(R.id.playButton);

        circleContainerLauncher = findViewById(R.id.circleContainerLauncher);
        blurView = findViewById(R.id.blurView);

        float radius = 20f;

        // ViewGroup you want to start blur from. Choose root as close to BlurView in hierarchy as possible.
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);

        // Optional:
        // Set drawable to draw in the beginning of each blurred frame.
        // Can be used in case your layout has a lot of transparent space and your content
        // gets a too low alpha value after blur is applied.
        Drawable windowBackground = decorView.getBackground();

        blurView.setupWith(rootView, new RenderScriptBlur(this)) // or RenderEffectBlur
                .setFrameClearDrawable(windowBackground) // Optional
                .setBlurRadius(radius);



    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }


    private void generateCircle() {
        // Generate a random size for the circle
        int size = dpToPx(random.nextInt(100) + 50);

        // Generate a random position for the circle
        if (circleContainerLauncher.getWidth() - size <= 0) {
            return;
        }
        int x = random.nextInt(circleContainerLauncher.getWidth() - size);
        int y = random.nextInt(circleContainerLauncher.getHeight() / 2 - size);

        // Create a new circle view
        CircleViewLauncher circle = new CircleViewLauncher(this, size, x, y, 50);

        // Add the circle to the container
        circleContainerLauncher.addView(circle);
    }

    public void startGame(View v) {
        initiated = true;
        enablePhysics();

    }

    private void enablePhysics() {
        Physics physics = circleContainerLauncher.getPhysics();

        // Change the gravity
        physics.setGravity(0, 0.0f);

// Apply the changes
        circleContainerLauncher.setPhysics(physics);
    }


}