package com.example.mathy;

import static android.content.ContentValues.TAG;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sketchy.R;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.jawnnypoo.physicslayout.PhysicsRelativeLayout;
import com.yangp.ypwaveview.YPWaveView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class MainActivity extends AppCompatActivity {


    private final Handler handler = new Handler();
    public PhysicsRelativeLayout circleContainer;
    public RelativeLayout circleContainerLauncher;
    public boolean isMultiplierTime = false;

    private InterstitialAd mInterstitialAd;
    public RelativeLayout container;
    SharedPreferences prefs = null;
    private final ArrayList<Ability> abilities = new ArrayList<>();
    private String abilityName;
    private final boolean abilitiesOpen = false;
    private int score = 0;
    private int bubblesObtained = 0;
    private final int lifeTime = 200;
    private int spawnRate = 200;
    private boolean playing = false;
    private TextView scoreTextView;
    private final Random random = new Random();
    private TextView scoreValue;
    private final ArrayList<PointF> spawnedCirclePositions = new ArrayList<>();
    private long timestamp = 0;
    private PopupWindow popupWindowAbilities;
    private View popupViewAbilities;
    private PopupWindow popupWindowAbilitiesDetail;
    private View popupViewAbilitiesDetail;
    private PopupWindow popupWindowNextLevel;

    private View popupViewStartLevel;

    private PopupWindow popupWindowStartLevel;
    private View popupViewNextLevel;
    private int nextLevelScore = 0;
    private YPWaveView circleDrawable;

    private boolean isAbilitiesOpen = false;
    private boolean isNextLevelOpen = false;
    private boolean isAbilityDetailOpen = false;

    private final Runnable runnable_spread = new Runnable() {
        @Override
        public void run() {
            Ability ability = findAbility(abilityName);
            TextView name = popupViewAbilitiesDetail.findViewById(R.id.ability_name);
            ImageView image = popupViewAbilitiesDetail.findViewById(R.id.image_view);
            TextView description = popupViewAbilitiesDetail.findViewById(R.id.description_text_view);
            TextView cost = popupViewAbilitiesDetail.findViewById(R.id.ability_cost);
            image.setImageResource(ability.getImage());

            name.setText(ability.getAbilityName() + " lv." + ability.getAbilityLevel());
            description.setText(ability.getAbilityDescription());
            cost.setText(ability.getAbilityCost() + " bubbles to unlock");
        }
    };

    public static double getPercentage(int current, int max) {
        double d = 100 - ((double) current / (double) max * 100.0);
        return 100 - d;
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

    public static long calculateElapsedTimeInSeconds(long startTime, long endTime) {
        long elapsedTime = endTime - startTime;
        return elapsedTime / 1000;
    }

    public static String cutString(String str) {
        int decimalIndex = str.indexOf(".");
        if (decimalIndex != -1 && decimalIndex + 3 <= str.length()) {
            return str.substring(0, decimalIndex + 3);
        }
        return str;
    }

    public static String sanitizeString(String str) {
        String[] words = str.split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            if (word.length() > 1) {
                sb.append(word.substring(0, 1).toUpperCase());
                sb.append(word.substring(1).toLowerCase());
            } else {
                sb.append(word.toUpperCase());
            }
        }
        return sb.toString();
    }

    public static String unsanitizeString(String str) {
        String[] words = str.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (i > 0) {
                sb.append("_");
            }
            if (word.length() > 1) {
                sb.append(word.substring(0, 1).toLowerCase());
                sb.append(word.substring(1));
            } else {
                sb.append(word.toLowerCase());
            }
        }
        return sb.toString();
    }

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


        MobileAds.initialize(this, initializationStatus -> {});

        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();
        prefs = getSharedPreferences("MyGamePreferences", MODE_PRIVATE);

        scoreTextView = (TextView) findViewById(R.id.scoreTextView);
        circleContainer = (PhysicsRelativeLayout) findViewById(R.id.circleContainer);


        circleDrawable = findViewById(R.id.progress_bar);
        circleDrawable.startAnimation();


        container = findViewById(R.id.decoy);

        BlurView blurView = findViewById(R.id.blurViewGame);

        setupBlur(blurView, 25f);

        TextView textView = findViewById(R.id.playButton);
        View decoyView = findViewById(R.id.decoyStartView);
        animateTextView(textView);
        decoyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View logo = findViewById(R.id.logo);
                View animatedText = findViewById(R.id.animatedText);
                View playButton = findViewById(R.id.playButton);
                bubblePop(logo, 500);
                bubblePop(animatedText, 500);
                bubblePop(playButton, 500);
                decoyView.setVisibility(View.GONE);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showPopupStartLevel(circleContainer, new Runnable() {
                            @Override
                            public void run() {

                                TextView bubblesNeeded = popupViewStartLevel.findViewById(R.id.tv_bubbles_necessary);

                                TextView currentLevel = popupViewStartLevel.findViewById(R.id.tv_current_level);

                                bubblesNeeded.setText("Bubbles needed: " + readData("level") * 25);
                                currentLevel.setText("LEVEL " + readData("level"));

                            }
                        });
                    }
                }, 1500);

            }
        });

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = false; // if false, clicking outside will dismiss the popup

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        popupViewAbilities = inflater.inflate(R.layout.ability_window, null);
        popupWindowAbilities = new PopupWindow(popupViewAbilities, width, height, focusable);

        popupViewAbilitiesDetail = inflater.inflate(R.layout.ability_detail, null);
        popupWindowAbilitiesDetail = new PopupWindow(popupViewAbilitiesDetail, width, height, focusable);

        popupViewNextLevel = inflater.inflate(R.layout.level_up_popup, null);
        popupWindowNextLevel = new PopupWindow(popupViewNextLevel, width, height, focusable);

        circleContainerLauncher = findViewById(R.id.circleContainerLauncher);



        // Generate a new circle every 2 seconds
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (playing) {
                    generateCircle();
                }
                handler.postDelayed(this, spawnRate);


            }
        }, spawnRate);

        // Generate a new circle every 2 seconds
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                generateBackgroundCircle();
                handler.postDelayed(this, 500);


            }
        }, 500);

    }

    public void showFullcScreenAd() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,"ca-app-pub-6054036722628086/1777579898", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        if (mInterstitialAd != null) {
                            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){

                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    // Called when ad is dismissed.
                                    // Set the ad reference to null so you don't show the ad a second time.
                                    Log.d(TAG, "Ad dismissed fullscreen content.");
                                    mInterstitialAd = null;
                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(AdError adError) {
                                    // Called when ad fails to show.
                                    Log.e(TAG, "Ad failed to show fullscreen content.");
                                    mInterstitialAd = null;
                                }

                                @Override
                                public void onAdImpression() {
                                    // Called when an impression is recorded for an ad.
                                    Log.d(TAG, "Ad recorded an impression.");
                                }

                                @Override
                                public void onAdShowedFullScreenContent() {
                                    // Called when ad is shown.
                                    Log.d(TAG, "Ad showed fullscreen content.");
                                }
                            });

                            mInterstitialAd.show(MainActivity.this);
                        } else {
                            Log.d("TAG", "The interstitial ad wasn't ready yet.");
                        }
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d(TAG, loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });


    }
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";

    // Determine the screen width (less decorations) to use for the ad width.
    private AdSize getAdSize() {
        WindowMetrics windowMetrics = getWindowManager().getCurrentWindowMetrics();
        Rect bounds = windowMetrics.getBounds();

        float adWidthPixels = circleContainerLauncher.getWidth();

        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0f) {
            adWidthPixels = bounds.width();
        }

        float density = getResources().getDisplayMetrics().density;
        int adWidth = (int) (adWidthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    public void bubbleSpawn(View view, int duration) {
        AnimatorSet animatorSet = new AnimatorSet();
        view.setScaleX(0f);
        view.setScaleY(0f);
        view.setAlpha(1f);

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

    private void generateCircle() {
        // Generate a random size for the circle
        int size = dpToPx(random.nextInt(100) + 50);

        // Generate a random position for the circle
        if (circleContainer.getWidth() - size <= 0) {
            return;
        }
        int x = random.nextInt(circleContainer.getWidth() - size);
        int y = random.nextInt(circleContainer.getHeight() / 2 - size) + 105;

        int level = readData("level");

        // Create a new circle view
        CircleView circle = new CircleView(this, size, x, y, level);

        // Add the circle to the container
        circleContainer.addView(circle);
        spawnedCirclePositions.add(new PointF(x, y));
    }



    private void generateBackgroundCircle() {
        // Generate a random size for the circle
        int size = dpToPx(random.nextInt(1000) + 100);

        // Generate a random position for the circle
        if (circleContainerLauncher.getWidth() - size <= 0) {
            return;
        }
        int x = random.nextInt((int) (circleContainerLauncher.getWidth() - size / 2));
        int y = random.nextInt((int) (circleContainerLauncher.getHeight() - size / 2));


        // Create a new circle view
        CircleViewLauncher circle = new CircleViewLauncher(this, size, x, y, 50);

        // Add the circle to the container
        circleContainerLauncher.addView(circle);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    public void removePosition(View view) {
        spawnedCirclePositions.remove(new PointF(view.getX(), view.getY()));
    }

    private void print(String s) {
        System.out.println(s);
    }

    public void increasePoint(int points) {

        score = score + points;

        bubblesObtained = readData("bubbles") + points;
        saveData("bubbles", bubblesObtained);

        updateBubbleTexts(bubblesObtained);

        if (score < 0) {
            gameOver();
            return;
        }

        circleDrawable.setProgress((int) getPercentage(score, nextLevelScore));
        // If score >= nextLevelScore then level up
        if (score >= nextLevelScore) {
            increaseLevel();
            circleDrawable.setProgress(100);
        }
    }

    public void increaseLevel() {

        if (!playing) {
            return;
        }

        // Stop playing and update level
        playing = false;
        int nextLevel = readData("level") + 1;

        // Save level and score to database
        saveData("level", nextLevel);


        // Calculate the time spent in this level
        long finishedTime = System.currentTimeMillis();
        long elapsedTime = calculateElapsedTimeInSeconds(timestamp, finishedTime);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                // Find the "Next level" TextView
                TextView nextLevelTextView = popupViewNextLevel.findViewById(R.id.tv_next_level);
                TextView startsTextView = popupViewNextLevel.findViewById(R.id.tv_stars);
                TextView elapsedTimeTextView = popupViewNextLevel.findViewById(R.id.tv_time_elapsed);
                TextView levelUpTextView = popupViewNextLevel.findViewById(R.id.tv_level_up);

                // Update the text of the TextView
                // Replace with the actual next level number
                nextLevelTextView.setText("Next level: " + nextLevel);
                levelUpTextView.setText("LEVEL UP");
                elapsedTimeTextView.setText("Elapsed Time: " + elapsedTime + " seconds.");
                startsTextView.setText("Stars: " + 3);
                elapsedTimeTextView.setVisibility(View.VISIBLE);
                startsTextView.setVisibility(View.VISIBLE);
                nextLevelTextView.setVisibility(View.VISIBLE);
            }
        };

        showPopupLevelUp(circleContainer, runnable);


        for (int i = 0; i < circleContainer.getChildCount(); i++) {
            if (circleContainer.getChildAt(i) instanceof CircleView) {
                CircleView v = (CircleView) circleContainer.getChildAt(i);
                v.killView(v, false);
            }
        }

    }

    public void saveData(String name, int value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(name, value);
        editor.apply();
    }

    public int readData(String name) {
        return prefs.getInt(name, 1);

    }

    public int readAbility(String name) {
        return prefs.getInt(name, 0);

    }

    public void nextLevelButton(View view) {
        popupViewNextLevel.animate().scaleX(0).scaleY(0).alpha(0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isNextLevelOpen = false;
                popupWindowNextLevel.dismiss();
                startLevel();
                toggleButtonsVisibility(true, "");
            }
        });
    }

    public void startLevel() {
        circleContainer.removeAllViews();
        container.removeAllViews();
        loadAbilities();
        nextLevelScore = readData("level") * 25;
        int level = readData("level");
        score = 0;
        circleDrawable.setProgress((int) getPercentage(score, nextLevelScore));
        if (score == 1) {
            score = 0;
        }

        updateBubbleTexts(readData("bubbles"));

        int maxSpawnRate = 500;
        spawnRate = maxSpawnRate - (level * 5);

        playing = true;

        timestamp = System.currentTimeMillis();
    }

    private void setupBlur(BlurView blurView, float radius) {
        View decorView = getWindow().getDecorView();

        // ViewGroup you want to start blur from. Choose root as close to BlurView in hierarchy as possible.
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);

        // Optional:
        // Set drawable to draw in the beginning of each blurred frame.
        // Can be used in case your layout has a lot of transparent space and your content
        // gets a too low alpha value after blur is applied.
        Drawable windowBackground = decorView.getBackground();

        blurView.setupWith(rootView, new RenderScriptBlur(MainActivity.this)) // or RenderEffectBlur
                .setFrameClearDrawable(windowBackground) // Optional
                .setBlurRadius(radius);
    }

    public void gameOver() {
        // Stop playing and update level
        playing = false;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {



                // Find the "Next level" TextView
                TextView nextLevelTextView = popupViewNextLevel.findViewById(R.id.tv_next_level);
                TextView startsTextView = popupViewNextLevel.findViewById(R.id.tv_stars);
                TextView elapsedTimeTextView = popupViewNextLevel.findViewById(R.id.tv_time_elapsed);
                TextView levelUpTextView = popupViewNextLevel.findViewById(R.id.tv_level_up);
                Button buttonTryAgain = popupViewNextLevel.findViewById(R.id.button);

                AdView adView = popupViewNextLevel.findViewById(R.id.adView);
                AdRequest adRequest = new AdRequest.Builder()
                        .build();
                adView.loadAd(adRequest);

                //Show a full screen app with
                if (Math.random() <= 0.15f) {
                    showFullcScreenAd();
                }

                // Update the text of the TextView
                // Replace with the actual next level number
                levelUpTextView.setText("GAME OVER");
                nextLevelTextView.setVisibility(View.GONE);
                elapsedTimeTextView.setVisibility(View.GONE);
                startsTextView.setVisibility(View.GONE);
                buttonTryAgain.setText("Try again ?");
            }
        };

        showPopupLevelUp(circleContainer, runnable);


        killAllBubbles();
    }

    public void pause(View v) {
        View pausedText = findViewById(R.id.paused_text);
        Button pause = findViewById(R.id.pause_button);
        if (playing) {
            playing = false;

            toggleButtonsVisibility(false, String.valueOf(v.getTooltipText()));
            animateViewVisibility(pausedText, true);
            killAllBubbles();
            pause.setText("▶");
        } else {
            playing = true;

            toggleButtonsVisibility(true, String.valueOf(v.getTooltipText()));
            animateViewVisibility(pausedText, false);
            pause.setText("II");
        }
    }

    public static void animateViewVisibility(final View view, final boolean isVisible) {
        if (isVisible) {
            view.setVisibility(View.VISIBLE);
            AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
            alphaAnimation.setDuration(500);
            view.startAnimation(alphaAnimation);
        } else {
            AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
            alphaAnimation.setDuration(500);
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    view.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            view.startAnimation(alphaAnimation);
        }
    }


    public void abilityWindow(View v) {
        showPopupAbility(v, new Runnable() {
            @Override
            public void run() {
                scoreValue = popupViewAbilities.findViewById(R.id.scoreAmount);
                scoreValue.setText("Bubbles: " + readData("bubbles"));
            }
        });


    }

    private void killAllBubbles() {
        for (int i = 0; i < circleContainer.getChildCount(); i++) {
            if (circleContainer.getChildAt(i) instanceof CircleView) {
                CircleView v = (CircleView) circleContainer.getChildAt(i);
                v.killView(v, false);
            }
        }


    }

    public void startMultiplyTime() {

        isMultiplierTime = true;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isMultiplierTime = false;
            }
        }, 5000);
    }

    private void loadAbilities() {
        abilities.clear();
        int level = readData("level");
        int spreadLevel = readAbility("ability_spread");
        int multiplierLevel = readAbility("ability_multiplier");
        int multiplyLevel = readAbility("ability_multiply");
        int destroy_dangerLevel = readAbility("ability_destroy_danger");
        int abilityCost = 0;
        double abilityProbability = 0;
        int abilityImage;
        String abilityDescription = "None";

        if (spreadLevel <= 0) {
            spreadLevel = 0;

        }
        abilityCost = (int) (50 * (level + (spreadLevel / 2 * 2.25)));
        abilityProbability = 0.0015 + (((level / 3000f) + (spreadLevel / 1500f)) * 2.25);
        abilityDescription = "Has " + cutString(String.valueOf((abilityProbability * 100))) + "% chance to propagate. Bubbles affected can propagate too.";
        abilityImage = R.drawable.spread;
        abilities.add(new Ability("Spread", spreadLevel, abilityDescription, abilityCost, abilityProbability, abilityImage));

        if (multiplierLevel <= 0) {
            multiplierLevel = 0;
        }
        abilityCost = (int) (75 * (level + (multiplierLevel / 2 * 1.25)));
        abilityProbability = 0.002 + (((level / 3000f) + (multiplierLevel / 1500f)) * 1.25);
        abilityDescription = "Has " + cutString(String.valueOf((abilityProbability * 100))) + "% chance to double the points.";
        abilityImage = R.drawable.initial_ability;
        abilities.add(new Ability("Multiplier", multiplierLevel, abilityDescription, abilityCost, abilityProbability, abilityImage));

        if (multiplyLevel <= 0) {
            destroy_dangerLevel = 0;
        }
        abilityCost = (int) (100 * (level + (multiplyLevel / 2 * 1.55)));
        abilityProbability = 0.0005 + (((level / 3000f) + (multiplyLevel / 1500f)) * 1.55);
        abilityDescription = "Has " + cutString(String.valueOf((abilityProbability * 100))) + "% chance to activate a special time triplicating points.";
        abilityImage = R.drawable.multiply;
        abilities.add(new Ability("Multiply", multiplyLevel, abilityDescription, abilityCost, abilityProbability, abilityImage));

        if (destroy_dangerLevel <= 0) {
            destroy_dangerLevel = 0;
        }

        abilityCost = (int) (150 * (level + (destroy_dangerLevel / 2 * 3.25)));
        abilityProbability = 0.0015 + (((level / 3000f) + (destroy_dangerLevel / 1500f)) * 3.25);
        abilityDescription = "Has " + cutString(String.valueOf((abilityProbability * 100))) + "% chance to destroy nearby bombs, increasing points.";
        abilityImage = R.drawable.destroy_danger;
        abilities.add(new Ability("Destroy Danger", destroy_dangerLevel, abilityDescription, abilityCost, abilityProbability, abilityImage));
    }

    private Ability findAbility(String name) {

        Ability ability = null;
        for (int i = 0; i < abilities.size(); i++) {

            if (abilities.get(i).getAbilityName().equals(name)) {
                ability = abilities.get(i);
                break;
            }

        }
        return ability;
    }

    public void abilityClicked(View v) {
        abilityName = sanitizeString(getResources().getResourceName(v.getId()).split("id/")[1]);

        Ability ability = findAbility(abilityName);

        if (ability == null) {
            Toast.makeText(this, "Not available yet!", Toast.LENGTH_LONG).show();
            return;
        }

        showPopupAbilityDetail(circleContainer, runnable_spread);


    }

    public void abilityUpgraded(View v) {
        int currentBubbles = readData("bubbles");
        int newBubblesValue = currentBubbles;

        Ability ability = findAbility(abilityName);
        if (currentBubbles >= ability.getAbilityCost()) {
            newBubblesValue = Math.max(0, currentBubbles - ability.getAbilityCost());
            saveData("ability_" + unsanitizeString(abilityName), readAbility("ability_" + unsanitizeString(abilityName)) + 1);
            saveData("bubbles", Math.max(0, currentBubbles - ability.getAbilityCost()));
        } else {
            Toast.makeText(this, "Insuficient bubbles!", Toast.LENGTH_SHORT).show();
        }


        loadAbilities();
        updateBubbleTexts(newBubblesValue);
        runnable_spread.run();

    }

    public void showPopupAbility(final View view, Runnable doBeforeShowPopup) {

        View abilityButton = view;

        pause(abilityButton);

        if (isAbilitiesOpen) {
            popupWindowAbilities.dismiss();
            isAbilitiesOpen = false;
            return;
        }
        isAbilitiesOpen = true;

        // animate the popup show
        popupViewAbilities.setScaleX(0);
        popupViewAbilities.setScaleY(0);
        popupViewAbilities.setAlpha(0f);
        popupViewAbilities.animate().scaleX(1).scaleY(1).alpha(1f).setDuration(300).setListener(null);

        if (doBeforeShowPopup != null) {
            doBeforeShowPopup.run();
        }

        // show the popup window
        popupWindowAbilities.showAtLocation(view, Gravity.CENTER, 0, 0);

        popupWindowAbilities.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                popupViewAbilities.animate().scaleX(0).scaleY(0).alpha(0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        pause(abilityButton);
                        popupWindowAbilities.dismiss();
                        isAbilitiesOpen = false;
                    }
                });
            }
        });

        // animate the popup dismiss
        popupViewAbilities.findViewById(R.id.abilities_dismiss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupViewAbilities.animate().scaleX(0).scaleY(0).alpha(0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        pause(abilityButton);
                        popupWindowAbilities.dismiss();
                        isAbilitiesOpen = false;
                    }
                });
            }
        });
    }

    private void toggleButtonsVisibility(boolean show, String text) {
        TextView score = findViewById(R.id.scoreTextView);
        Button button = findViewById(R.id.scoreTextView1);
        Button pauseButton = findViewById(R.id.pause_button);
        YPWaveView progress = findViewById(R.id.progress_bar);
        View v = findViewById(R.id.decoy_circle);



        if (show) {
            switch (text) {
                case "pause":
                    bubbleSpawn(score, 100);
                    bubbleSpawn(button, 100);
                    bubbleSpawn(progress, 200);
                    bubbleSpawn(v, 200);
                    break;
                case "ability":
                    bubbleSpawn(score, 100);
                    bubbleSpawn(progress, 200);
                    bubbleSpawn(v, 200);
                    bubbleSpawn(pauseButton, 100);
                    break;
                case "":
                    bubbleSpawn(pauseButton, 100);
                    bubbleSpawn(button, 100);
                    bubbleSpawn(score, 100);
                    bubbleSpawn(progress, 200);
                    bubbleSpawn(v, 200);
                    break;
            }
        } else {
            switch (text) {
                case "pause":
                    bubblePop(score, 100);
                    bubblePop(button, 100);
                    bubblePop(progress, 200);
                    bubblePop(v, 200);
                    break;
                case "ability":
                    bubblePop(score, 100);
                    bubblePop(progress, 200);
                    bubblePop(v, 200);
                    bubblePop(pauseButton, 100);
                    break;
                case "":
                    bubblePop(pauseButton, 100);
                    bubblePop(button, 100);
                    bubblePop(score, 100);
                    bubblePop(progress, 200);
                    bubblePop(v, 200);
                    break;
            }
        }

    }

    public void bubblePop(View view, int duration) {
        AnimatorSet animatorSet = new AnimatorSet();

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


    public void showPopupLevelUp(final View view, Runnable doBeforeShowPopup) {
        if (isNextLevelOpen) {
            return;
        }
        isNextLevelOpen = true;


        // animate the popup show
        popupViewNextLevel.setScaleX(0);
        popupViewNextLevel.setScaleY(0);
        popupViewNextLevel.setAlpha(0f);
        popupViewNextLevel.animate().scaleX(1).scaleY(1).alpha(1f).setDuration(300).setListener(null);

        doBeforeShowPopup.run();

        // show the popup window
        popupWindowNextLevel.showAtLocation(view, Gravity.CENTER, 0, 0);

        toggleButtonsVisibility(false, "");
    }

    public void showPopupAbilityDetail(final View view, Runnable doBeforeShowPopup) {
        if (isAbilityDetailOpen) {
            popupWindowAbilitiesDetail.dismiss();
            isAbilityDetailOpen = false;
            return;
        }
        isAbilityDetailOpen = true;


        // animate the popup show
        popupViewAbilitiesDetail.setScaleX(0);
        popupViewAbilitiesDetail.setScaleY(0);
        popupViewAbilitiesDetail.setAlpha(0f);
        popupViewAbilitiesDetail.animate().scaleX(1).scaleY(1).alpha(1f).setDuration(300).setListener(null);

        doBeforeShowPopup.run();

        // show the popup window
        popupWindowAbilitiesDetail.showAtLocation(view, Gravity.CENTER, 0, 0);

        // animate the popup dismiss
        popupViewAbilitiesDetail.findViewById(R.id.button_dismiss_ability_detail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupViewAbilitiesDetail.animate().scaleX(0).scaleY(0).alpha(0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        popupWindowAbilitiesDetail.dismiss();
                        isAbilityDetailOpen = false;
                    }
                });
            }
        });
    }

    private void updateBubbleTexts(int s) {
        scoreTextView.setText(String.valueOf(s));
        if (scoreValue != null) {
            scoreValue.setText("Bubbles: " + s);
        }
    }

    public ArrayList<Ability> getAbilities() {
        return abilities;
    }

    private void initiateGame() {

    }

    int currentCountDownIndex = 0;
    private void startCountdown(View v) {
        final TextView tvCountdown = v.findViewById(R.id.tv_countdown);
        final List<String> countDownValues = Arrays.asList("4", "3", "2", "1", "GO!");

        tvCountdown.setText(countDownValues.get(currentCountDownIndex));
        tvCountdown.animate().alpha(1f).setDuration(1000).start();

        new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                currentCountDownIndex++;
                if (currentCountDownIndex < countDownValues.size()) {
                    tvCountdown.setText(countDownValues.get(currentCountDownIndex));
                    tvCountdown.animate().alpha(1f).setDuration(1000).start();
                }
            }

            @Override
            public void onFinish() {
                popupWindowStartLevel.dismiss();
                toggleButtonsVisibility(true, "");
                startLevel();
            }
        }.start();
    }

    public void showPopupStartLevel(final View view, Runnable doBeforeShowPopup) {

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = false; // if false, clicking outside will dismiss the popup

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        popupViewStartLevel = inflater.inflate(R.layout.start_level_layout, null);
        popupWindowStartLevel = new PopupWindow(popupViewStartLevel, width, height, focusable);

        popupWindowStartLevel.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                popupViewStartLevel.animate().scaleX(0).scaleY(0).alpha(0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        popupWindowStartLevel.dismiss();
                    }
                });
            }
        });


        // animate the popup show
        popupViewStartLevel.setScaleX(0);
        popupViewStartLevel.setScaleY(0);
        popupViewStartLevel.setAlpha(0f);
        popupViewStartLevel.animate().scaleX(1).scaleY(1).alpha(1f).setDuration(300).setListener(null);

        doBeforeShowPopup.run();

        // show the popup window
        popupWindowStartLevel.showAtLocation(view, Gravity.CENTER, 0, 0);
        startCountdown(popupViewStartLevel);
    }


    private void animateTextView(TextView textView) {
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(2000);
        anim.setStartOffset(500);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        textView.startAnimation(anim);
    }


}

