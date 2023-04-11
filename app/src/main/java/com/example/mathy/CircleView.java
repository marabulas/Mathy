package com.example.mathy;

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
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import com.example.sketchy.R;
import com.jawnnypoo.physicslayout.Physics;
import com.jawnnypoo.physicslayout.PhysicsRelativeLayout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class CircleView extends FrameLayout {
    int lifeTime;
    boolean dying = false;
    boolean isBomb = false;

    public void setAlreadyAffectedBySpread(boolean alreadyAffectedBySpread) {
        this.alreadyAffectedBySpread = alreadyAffectedBySpread;
    }

    public boolean isAlreadyAffectedBySpread() {
        return this.alreadyAffectedBySpread;
    }

    boolean alreadyAffectedBySpread = false;

    int level;

    int spreadEffectDelay = 100;

    int pointsObtained = 1;

    int maxLifetime = 750;

    private TextView valueTextView;
    private View circleBackground;
    private final android.os.Handler handler = new android.os.Handler();

    private final Runnable spreadRunnable = new Runnable() {
        @Override
        public void run() {
            System.out.println("Spread started");
            spreadEffect(((ViewGroup)CircleView.this.getParent()),CircleView.this);
        }
    };

    private final Runnable multiplyRunnable = new Runnable() {
        @Override
        public void run() {
            System.out.println("Multiply started");
            ((MainActivity)getContext()).startMultiplyTime();
        }
    };

    private final Runnable multiplierRunnable = new Runnable() {
        @Override
        public void run() {
            System.out.println("Multiplier started");
            pointsObtained = pointsObtained * 10;
        }
    };
    private final Runnable destroyDangerRunnable = new Runnable() {
        @Override
        public void run() {
            System.out.println("Destroy danger started");
            destroyDangerEffect(((ViewGroup)CircleView.this.getParent()),CircleView.this);
        }
    };

    public CircleView(Context context, int size, int x, int y, int level) {
        super(context);
        init(context);


        double probability = 0.05;
        if (size <= 175) {
            probability = 0.0;
        }
        Pair<String, Integer> values = generateMathProblem(probability, -20, 20, 1);

        setValue(values);

        this.lifeTime = 3500;

        this.level = level;

        int color = getRandomBrightColor();
        int color2 = getRandomBrightColor();

        if (((MainActivity)getContext()).isMultiplierTime) {
            color = Color.RED;
            color2 = Color.YELLOW;
        }


        executeWithProbability(0.0015 * level, new Runnable() {
            @Override
            public void run() {
                becomeBomb();
            }
        });

        // Create a new circle shape

        setTextColor(color);


        setCircleColor(new Pair<>(color, color2));

        setX(x);
        setY(y);

        setLayoutParams(new RelativeLayout.LayoutParams(size, size));




        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                touchReceived();
            }
        });

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                killView(CircleView.this, false);
            }
        }, lifeTime);

        bubbleSpawn(this);
    }

    public void touchReceived() {
        tryLaunchSkills();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                killView(CircleView.this, true);
            }
        }, spreadEffectDelay);
    }

    public void tryLaunchSkills() {

        System.out.println("trying to use skill");

        ArrayList<Ability> abilities = ((MainActivity)getContext()).getAbilities();

        for (int i = 0; i < abilities.size(); i++) {
            Ability ability = abilities.get(i);

            if (ability.getAbilityLevel() == 0) {
                break;
            }

            //Spread
            if (ability.getAbilityName().equals("Spread")) {
                System.out.println("Trying to activate " + ability.getAbilityName() + " - probability: " + ability.getAbilityProbability() + "%");
                executeWithProbability(ability.getAbilityProbability(), spreadRunnable);
            }

            //Destroy Danger
            if (ability.getAbilityName().equals("Destroy Danger")) {
                executeWithProbability(ability.getAbilityProbability(), destroyDangerRunnable);
            }

            //Multiplier Danger
            if (ability.getAbilityName().equals("Multiplier")) {
                executeWithProbability(ability.getAbilityProbability(), multiplierRunnable);
            }

            //Multiply Danger
            if (ability.getAbilityName().equals("Multiply")) {
                executeWithProbability(ability.getAbilityProbability(), multiplyRunnable);
            }

        }


    }

    public void killView(View view, boolean increasePoint) {
        if (dying) {
            return;
        }
        if (increasePoint) {
            ((MainActivity)getContext()).increasePoint(pointsObtained);
        }
        vibrateDevice(5);

        //animateViewDismantling(view);

       // animateView(view, false);
        bubblePop(view);

      //  setVisibility(View.GONE);

        ((MainActivity)getContext()).removePosition(view);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (((ViewGroup)getParent()) != null) {
                    ((ViewGroup)getParent()).removeView(view);
                }
            }
        }, 250);

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

        ObjectAnimator scaleXAnimator1 = ObjectAnimator.ofFloat(view, "scaleX", 0.9f);
        scaleXAnimator1.setDuration(50);
        ObjectAnimator scaleYAnimator1 = ObjectAnimator.ofFloat(view, "scaleY", 0.9f);
        scaleYAnimator1.setDuration(50);

        ObjectAnimator scaleXAnimator2 = ObjectAnimator.ofFloat(view, "scaleX", 1.2f);
        scaleXAnimator2.setDuration(50);
        ObjectAnimator scaleYAnimator2 = ObjectAnimator.ofFloat(view, "scaleY", 1.2f);
        scaleYAnimator2.setDuration(50);

        ObjectAnimator sXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 0f);
        sXAnimator.setDuration(50);

        ObjectAnimator sYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 0f);
        sYAnimator.setDuration(50);

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 0f);
        alphaAnimator.setDuration(40);

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

        ObjectAnimator scaleXAnimator1 = ObjectAnimator.ofFloat(view, "scaleX", 1f);
        scaleXAnimator1.setDuration(50);
        ObjectAnimator scaleYAnimator1 = ObjectAnimator.ofFloat(view, "scaleY", 1f);
        scaleYAnimator1.setDuration(50);

        ObjectAnimator scaleXAnimator2 = ObjectAnimator.ofFloat(view, "scaleX", 1.2f);
        scaleXAnimator2.setDuration(50);
        ObjectAnimator scaleYAnimator2 = ObjectAnimator.ofFloat(view, "scaleY", 1.2f);
        scaleYAnimator2.setDuration(50);

        ObjectAnimator sXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 1f);
        sXAnimator.setDuration(50);

        ObjectAnimator sYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 1f);
        sYAnimator.setDuration(50);

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 1f);
        alphaAnimator.setDuration(20);

        animatorSet.play(scaleXAnimator1).with(scaleYAnimator1);
        animatorSet.play(scaleXAnimator2).with(scaleYAnimator2).after(scaleXAnimator1);
        animatorSet.play(sXAnimator).with(sYAnimator).after(scaleXAnimator2);
        //animatorSet.play(alphaAnimator).after(scaleXAnimator2);

        animatorSet.start();
    }



    public void vibrateDevice(long milliseconds) {
        Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(milliseconds);
        }
    }


    public void animateViewDismantling(final View view) {
        // Get the size of the view
        int width = view.getWidth();
        int height = view.getHeight();
        if (width == 0 | height == 0 | view.getParent() == null) {
            return;
        }
        // Create a new bitmap and canvas to draw the view
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        // Create an array of particle views to represent the dismantled view
        int particleSize = 60;
        int numParticles = (width * height) / (particleSize * particleSize);
        final View[] particles = new View[numParticles];
        for (int i = 0; i < numParticles; i++) {
            int x = (i * particleSize) % width;
            int y = (i * particleSize) / width * particleSize;
            int w = Math.min(particleSize, width - x);
            int h = Math.min(particleSize, height - y);
            particles[i] = new View(view.getContext());
            particles[i].setBackground(new BitmapDrawable(view.getResources(), Bitmap.createBitmap(bitmap, x, y, w, h)));
            particles[i].setLayoutParams(new ViewGroup.LayoutParams(w, h));
            particles[i].setTranslationX(view.getX() + x);
            particles[i].setTranslationY(view.getY() + y);
            if (view.getParent() != null) {
              //  ((ViewGroup)view.getParent()).addView(particles[i]);
            }
        }

        // Animate the dismantling effect
        for (int i = 0; i < numParticles; i++) {
            // Add a random offset to the translation values
            int offsetX = new Random().nextInt(200);
            int offsetY = new Random().nextInt(200);
            particles[i].animate()
                    .translationX(view.getX() + offsetX)
                    .translationY(view.getY() + offsetY)
                    .rotation(new Random().nextInt(720) - 360)
                    .alpha(0)
                    .setDuration(150)
                    .setInterpolator(new AccelerateInterpolator())
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            ViewGroup parent = (ViewGroup) CircleView.this.getParent();
                            for (View particle : particles) {
                                if (parent != null) {
                                    parent.removeView(particle);
                                }
                            }
                        }
                    })
                    .start();
        }
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

    public void spreadEffect(ViewGroup container, CircleView clickedView) {
        if (container == null) {
            return;
        }
        int childCount = container.getChildCount();
        List<CircleView> viewsToAnimate = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            if (container.getChildAt(i) instanceof CircleView) {
                CircleView childView = ((CircleView) container.getChildAt(i));
                if (childView != clickedView && !isChildBeingAnimated(childView) && !childView.isAlreadyAffectedBySpread()) {
                    viewsToAnimate.add(childView);
                    ((CircleView) container.getChildAt(i)).setAlreadyAffectedBySpread(true);

                }
            }

        }
        // Sort views based on distance from clicked view
        viewsToAnimate.sort(new Comparator<View>() {
            @Override
            public int compare(View view1, View view2) {
                float distance1 = getDistance(clickedView, view1);
                float distance2 = getDistance(clickedView, view2);
                return Float.compare(distance1, distance2);
            }
        });

        // Draw lines and animate views
        int numViewsToAnimate = Math.min(viewsToAnimate.size(), 3);
        for (int index = 0; index < numViewsToAnimate; index++) {

            CircleView nextView = viewsToAnimate.get(index);

            int startX = (int) (clickedView.getX() + clickedView.getWidth() / 2);
            int startY = (int) (clickedView.getY() + clickedView.getHeight() / 2);
            int endX = (int) (nextView.getX() + nextView.getWidth() / 2);
            int endY = (int) (nextView.getY() + nextView.getHeight() / 2);
            drawLine(container, clickedView, nextView, startX, startY, endX, endY);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    nextView.touchReceived();
                }
            }, spreadEffectDelay + 25);
        }
    }

    public void destroyDangerEffect(ViewGroup container, CircleView clickedView) {
        if (container == null) {
            return;
        }
        int childCount = container.getChildCount();
        List<CircleView> viewsToAnimate = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            if (container.getChildAt(i) instanceof CircleView) {
                CircleView childView = ((CircleView) container.getChildAt(i));
                if (childView != clickedView && !isChildBeingAnimated(childView) && !childView.isAlreadyAffectedBySpread()) {
                    if (childView.isBomb) {
                        viewsToAnimate.add(childView);
                        ((CircleView) container.getChildAt(i)).setAlreadyAffectedBySpread(true);
                    }

                }
            }

        }
        // Sort views based on distance from clicked view
        viewsToAnimate.sort(new Comparator<View>() {
            @Override
            public int compare(View view1, View view2) {
                float distance1 = getDistance(clickedView, view1);
                float distance2 = getDistance(clickedView, view2);
                return Float.compare(distance1, distance2);
            }
        });

        // Draw lines and animate views
        int numViewsToAnimate = Math.min(viewsToAnimate.size(), 3);
        for (int index = 0; index < numViewsToAnimate; index++) {

            CircleView nextView = viewsToAnimate.get(index);

            int startX = (int) (clickedView.getX() + clickedView.getWidth() / 2);
            int startY = (int) (clickedView.getY() + clickedView.getHeight() / 2);
            int endX = (int) (nextView.getX() + nextView.getWidth() / 2);
            int endY = (int) (nextView.getY() + nextView.getHeight() / 2);
            drawLine(container, clickedView, nextView, startX, startY, endX, endY);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    nextView.touchReceived();
                }
            }, spreadEffectDelay + 25);
        }
    }

    private boolean isChildBeingAnimated(View childView) {
        Animation animation = childView.getAnimation();
        return animation != null && !animation.hasEnded();
    }

    private float getDistance(View view1, View view2) {
        float x1 = view1.getX() + view1.getWidth() / 2;
        float y1 = view1.getY() + view1.getHeight() / 2;
        float x2 = view2.getX() + view2.getWidth() / 2;
        float y2 = view2.getY() + view2.getHeight() / 2;
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    private void drawLine(ViewGroup container, View clickedView, View nextView, int startX, int startY, int endX, int endY) {
        LineView lineView = new LineView(getContext(), getMediumColor(clickedView));
        ((MainActivity)getContext()).container.addView(lineView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
       // container.addView(lineView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        lineView.setPath(createPath(startX, startY, endX, endY));
        animateLine(lineView, container, startX, startY);
    }

    public int getMediumColor(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        int pixelCount = bitmap.getWidth() * bitmap.getHeight();
        int[] pixels = new int[pixelCount];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int sumR = 0;
        int sumG = 0;
        int sumB = 0;
        for (int i = 0; i < pixelCount; i++) {
            sumR += Color.red(pixels[i]);
            sumG += Color.green(pixels[i]);
            sumB += Color.blue(pixels[i]);
        }
        int mediumR = sumR / pixelCount;
        int mediumG = sumG / pixelCount;
        int mediumB = sumB / pixelCount;
        return Color.rgb(mediumR, mediumG, mediumB);
    }


    private void animateLine(final LineView lineView, ViewGroup container, int startX, int startY) {
        // Calculate the coordinates of the centers of the start and end views

        ScaleAnimation shrinkAnim = new ScaleAnimation(
                0f, 1f, 0f, 1f,
                Animation.RELATIVE_TO_PARENT, (float) startX / container.getWidth(),
                Animation.RELATIVE_TO_PARENT, (float) startY / container.getHeight()
        );
        shrinkAnim.setDuration(spreadEffectDelay);
        shrinkAnim.setInterpolator(new AccelerateInterpolator());
        shrinkAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                enablePhysics(false);


            }

            @Override
            public void onAnimationEnd(Animation animation) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        enablePhysics(true);
                    }
                }, 2000);
                container.removeView(lineView);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        lineView.startAnimation(shrinkAnim);
    }

    private Path createPath(float startX, float startY, float endX, float endY) {
        Path path = new Path();
        path.moveTo(startX, startY);
        path.lineTo(endX, endY);
        return path;
    }

    public static void runAsync(Runnable runnable, Consumer<Object> onPostExecute) {
        new AsyncTask<Void, Void, Object>() {
            @Override
            protected Object doInBackground(Void... voids) {
                runnable.run();
                return null;
            }

            @Override
            protected void onPostExecute(Object result) {
                onPostExecute.accept(result);
            }
        }.execute();
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

    public void setValue(Pair<String, Integer> pair) {
        pointsObtained = pair.second;
        valueTextView.setText(pair.first);
    }

    public static int getOppositeColor(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        int oppositeRed = 255 - red;
        int oppositeGreen = 255 - green;
        int oppositeBlue = 255 - blue;

        // Calculate the luminance of the original color and the opposite color
        double originalLuminance = ColorUtils.calculateLuminance(color);
        double oppositeLuminance = ColorUtils.calculateLuminance(Color.rgb(oppositeRed, oppositeGreen, oppositeBlue));

        // If the luminance of the opposite color is too close to the original color,
        // adjust the opposite color to be slightly darker or lighter.
        double luminanceDifference = Math.abs(originalLuminance - oppositeLuminance);
        if (luminanceDifference < 0.2) {
            if (originalLuminance > 0.5) {
                oppositeLuminance = Math.max(0, oppositeLuminance - 0.2);
            } else {
                oppositeLuminance = Math.min(1, oppositeLuminance + 0.2);
            }
            int oppositeColor = ColorUtils.blendARGB(Color.BLACK, Color.WHITE, (float) oppositeLuminance);
            return oppositeColor;
        }

        return Color.rgb(oppositeRed, oppositeGreen, oppositeBlue);
    }

    public static int getContrastingColor(int color) {
        // Get the RGB components of the color
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        // Calculate the luminance of the color using the formula from the WCAG
        double luminance = (0.2126 * red + 0.7152 * green + 0.0722 * blue) / 255;

        // Calculate the luminance thresholds for determining black or white text
        double darkLuminanceThreshold = 0.179;
        double lightLuminanceThreshold = 0.737;

        // Determine whether to use black or white text based on the luminance of the color
        int textColor = Color.BLACK;
        if (luminance > lightLuminanceThreshold) {
            textColor = Color.BLACK;
        } else if (luminance < darkLuminanceThreshold) {
            textColor = Color.WHITE;
        } else {
            // If the luminance is in between the thresholds, use the color with the highest contrast
            double contrastToBlack = calculateContrast(Color.BLACK, color);
            double contrastToWhite = calculateContrast(Color.WHITE, color);
            if (contrastToBlack > contrastToWhite) {
                textColor = Color.BLACK;
            } else {
                textColor = Color.WHITE;
            }
        }

        return textColor;
    }

    public static double calculateContrast(int foregroundColor, int backgroundColor) {
        // Get the luminance of the foreground and background colors
        double foregroundLuminance = (0.2126 * Color.red(foregroundColor) + 0.7152 * Color.green(foregroundColor) + 0.0722 * Color.blue(foregroundColor)) / 255;
        double backgroundLuminance = (0.2126 * Color.red(backgroundColor) + 0.7152 * Color.green(backgroundColor) + 0.0722 * Color.blue(backgroundColor)) / 255;

        // Determine the lightest and darkest luminance values
        double lightestLuminance = Math.max(foregroundLuminance, backgroundLuminance);
        double darkestLuminance = Math.min(foregroundLuminance, backgroundLuminance);

        // Calculate the contrast ratio using the luminance values
        double contrastRatio = (lightestLuminance + 0.05) / (darkestLuminance + 0.05);

        return contrastRatio;
    }





    public void setTextColor(int color) {
        valueTextView.setTextColor(getContrastingColor(color));
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

    private void enablePhysics(boolean enable) {
        PhysicsRelativeLayout physicsLayout = ((MainActivity)getContext()).circleContainer;
        Physics physics = physicsLayout.getPhysics();


        if (enable) {
            // Change the gravity
            physics.setGravity(0, 5f);
        } else {
            // Change the gravity
            physics.setGravity(0, 0.0f);
        }

// Apply the changes
        physicsLayout.setPhysics(physics);
    }




}
