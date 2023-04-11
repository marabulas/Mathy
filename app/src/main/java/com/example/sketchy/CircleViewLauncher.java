package com.example.sketchy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import com.jawnnypoo.physicslayout.Physics;
import com.jawnnypoo.physicslayout.PhysicsRelativeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class CircleViewLauncher extends FrameLayout {
    int lifeTime;
    boolean dying = false;
    boolean isBomb = false;

    int level;

    int pointsObtained = 1;

    private TextView valueTextView;
    private View circleBackground;
    private final android.os.Handler handler = new android.os.Handler();

    public CircleViewLauncher(Context context, int size, int x, int y, int level) {
        super(context);
        init(context);


        double probability = 0.05;
        if (size <= 175) {
            probability = 0.0;
        }
        Pair<String, Integer> values = generateMathProblem(probability, -20, 20, 1);

       // setValue(values);

        this.lifeTime = 15000;

        this.level = level;

        int color = getRandomBrightColor();
        int color2 = getRandomBrightColor();

       // if (((LauncherActivity)getContext()).isMultiplierTime) {
         //   color = Color.RED;
           // color2 = Color.YELLOW;
        //}


        executeWithProbability(0.0015 * level, new Runnable() {
            @Override
            public void run() {
                becomeBomb();
            }
        });

        // Create a new circle shape

       // setTextColor(color);


        setCircleColor(new Pair<>(color, color2));

        setX(x);
        setY(y);

        setLayoutParams(new RelativeLayout.LayoutParams(size, size));

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                killView(CircleViewLauncher.this);
            }
        }, lifeTime);

        bubbleSpawn(this);
    }

    public void killView(View view) {
        if (dying) {
            return;
        }

        //animateViewDismantling(view);

       // animateView(view, false);
        bubblePop(view);

      //  setVisibility(View.GONE);


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (((ViewGroup)getParent()) != null) {
                    ((ViewGroup)getParent()).removeView(view);
                }
            }
        }, 10000);

    }

    public static void animateView(final View view, boolean show) {
        if (view == null) {
            return;
        }
        if (show) {
            view.setScaleX(0);
            view.setScaleY(0);
            view.animate()
                    .scaleX(1)
                    .scaleY(1)
                    .setDuration(200)
                    .setInterpolator(new DecelerateInterpolator())
                    .setListener(null);
            view.setVisibility(View.VISIBLE);
        } else {
            view.animate()
                    .scaleX(0)
                    .scaleY(0)
                    .setDuration(200)
                    .setInterpolator(new DecelerateInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            view.setVisibility(View.GONE);
                        }
                    });
        }
    }

    public void bubblePop(View view) {
        AnimatorSet animatorSet = new AnimatorSet();
        int duration = 2500;

        ObjectAnimator scaleXAnimator1 = ObjectAnimator.ofFloat(view, "scaleX", 0.9f);
        scaleXAnimator1.setDuration(duration);
        ObjectAnimator scaleYAnimator1 = ObjectAnimator.ofFloat(view, "scaleY", 0.9f);
        scaleYAnimator1.setDuration(duration);

        ObjectAnimator scaleXAnimator2 = ObjectAnimator.ofFloat(view, "scaleX", 1.2f);
        scaleXAnimator2.setDuration(duration);
        ObjectAnimator scaleYAnimator2 = ObjectAnimator.ofFloat(view, "scaleY", 1.2f);
        scaleYAnimator2.setDuration(duration);

        ObjectAnimator sXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 0f);
        sXAnimator.setDuration(duration);

        ObjectAnimator sYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 0f);
        sYAnimator.setDuration(duration);

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 0f);
        alphaAnimator.setDuration(duration);

        animatorSet.play(scaleXAnimator1).with(scaleYAnimator1);
        animatorSet.play(scaleXAnimator2).with(scaleYAnimator2).after(scaleXAnimator1);
        animatorSet.play(sXAnimator).with(sYAnimator).after(scaleXAnimator2);
        animatorSet.play(alphaAnimator).after(scaleXAnimator2);

        animatorSet.start();
    }

    public void bubbleSpawn(View view) {
        AnimatorSet animatorSet = new AnimatorSet();
        view.setScaleX(0f);
        view.setScaleY(0f);
        view.setAlpha(1f);

        int duration = 2500;

        ObjectAnimator scaleXAnimator1 = ObjectAnimator.ofFloat(view, "scaleX", 1f);
        scaleXAnimator1.setDuration(duration);
        ObjectAnimator scaleYAnimator1 = ObjectAnimator.ofFloat(view, "scaleY", 1f);
        scaleYAnimator1.setDuration(duration);

        ObjectAnimator scaleXAnimator2 = ObjectAnimator.ofFloat(view, "scaleX", 1.2f);
        scaleXAnimator2.setDuration(duration);
        ObjectAnimator scaleYAnimator2 = ObjectAnimator.ofFloat(view, "scaleY", 1.2f);
        scaleYAnimator2.setDuration(duration);

        ObjectAnimator sXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 1f);
        sXAnimator.setDuration(duration);

        ObjectAnimator sYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 1f);
        sYAnimator.setDuration(duration);

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 1f);
        alphaAnimator.setDuration(duration);

        animatorSet.play(scaleXAnimator1).with(scaleYAnimator1);
        animatorSet.play(scaleXAnimator2).with(scaleYAnimator2).after(scaleXAnimator1);
        animatorSet.play(sXAnimator).with(sYAnimator).after(scaleXAnimator2);
        //animatorSet.play(alphaAnimator).after(scaleXAnimator2);

        animatorSet.start();
    }

    public static void executeWithProbability(double probability, Runnable runnable) {
        if (Math.random() <= probability) {
            runnable.run();
        }
    }

    private void becomeBomb() {
        isBomb = true;
        valueTextView.setVisibility(GONE);
        circleBackground.setVisibility(GONE);
        setBackgroundResource(R.drawable.skull_icon_new);
        pointsObtained = -25;
    }

    public static int getRandomNumber() {
        Random rand = new Random();
        int randomNum = rand.nextInt(20);
        if (rand.nextBoolean()) {
            randomNum *= -1;
        }
        return randomNum;
    }

    public static Pair<String, Integer> generateMathProblem(double probability, int numMin, int numMax, int maxOperations) {
        // Generate a random number between numMin and numMax
        int result = getRandomNumber();
        Random rand = new Random();

        // Check if we should generate a math problem or just return a random number
        if (Math.random() < probability) {
            // Generate the number of operations to include in the problem
            int numOperations = (int) (Math.random() * maxOperations) + 1;

            // Generate the operands
            List<Integer> operands = new ArrayList<>();
            operands.add(result);
            for (int i = 1; i <= numOperations; i++) {
                int operand = (int) (Math.random() * (numMax - numMin + 1)) + numMin;
                operands.add(operand);
            }

            StringBuilder sb = new StringBuilder();
            int num1 = rand.nextInt(numMax) - numMin;
            sb.append(num1);
            for (int i = 0; i < maxOperations; i++) {
                char operator = "+-*/".charAt(rand.nextInt(4));
                int num2 = rand.nextInt(numMax) - numMin;
                sb.append(" ");
                sb.append(operator);
                sb.append(" ");
                sb.append(num2);

                switch (operator) {
                    case '+':
                        num1 += num2;
                        break;
                    case '-':
                        num1 -= num2;
                        break;
                    case '*':
                        num1 *= num2;
                        break;
                    case '/':
                        num1 /= num2;
                        break;
                }
            }

            result = num1;

            return new Pair<>(sb.toString(), result);
        } else {
            return new Pair<>(String.valueOf(result), result);
        }
    }


    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.circle_view, this, true);
        valueTextView = view.findViewById(R.id.circle_text);
        circleBackground = view.findViewById(R.id.circle);
    }

    public void setCircleColor(Pair<Integer, Integer> colors) {
        // Get a reference to the view's background drawable
        GradientDrawable drawable = (GradientDrawable) circleBackground.getBackground();

// Change the gradient colors programmatically
        drawable.setColors(new int[] {colors.first, colors.second});

// Set the modified drawable as the view's background
        circleBackground.setBackground(drawable);
    }

    public static int getRandomBrightColor() {
        Random random = new Random();
        int red = random.nextInt(156) + 70; // generates a value between 100 and 255 for the red component
        int green = random.nextInt(156) + 70; // generates a value between 100 and 255 for the green component
        int blue = random.nextInt(156) + 70; // generates a value between 100 and 255 for the blue component


        return Color.rgb(red, green, blue);
    }


}
