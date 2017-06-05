package org.literacyapp.handwriting;

import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import org.literacyapp.handwriting.util.MediaPlayerHelper;


public class FinalActivity extends AppCompatActivity {

    private ImageView mFinalCheckmarkImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final);

        mFinalCheckmarkImageView = (ImageView) findViewById(R.id.final_checkmark);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Animate checkmark
        mFinalCheckmarkImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mFinalCheckmarkImageView.setVisibility(View.VISIBLE);
                Drawable drawable = mFinalCheckmarkImageView.getDrawable();
                ((Animatable) drawable).start();

                MediaPlayer mediaPlayer = MediaPlayerHelper.playLessonCompleted(getApplicationContext());
                if (mediaPlayer != null){
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            HandwritingApplication handwritingApplication = (HandwritingApplication) getApplicationContext();
                            handwritingApplication.setCompletionCounter(handwritingApplication.getCompletionCounter() + 1);
                            if (handwritingApplication.getCompletionCounter() < 5){
                                Intent intent = new Intent(getApplicationContext(), WriteLetterActivity.class);
                                startActivity(intent);
                            } else {
                                finishAffinity();
                            }
                        }
                    });
                }
            }
        }, MediaPlayerHelper.DEFAULT_PLAYER_DELAY);
    }
}
