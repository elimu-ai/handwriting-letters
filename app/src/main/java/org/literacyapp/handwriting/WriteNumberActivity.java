package org.literacyapp.handwriting;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.literacyapp.contentprovider.ContentProvider;
import org.literacyapp.contentprovider.dao.AudioDao;
import org.literacyapp.contentprovider.dao.DaoSession;
import org.literacyapp.contentprovider.model.content.Number;
import org.literacyapp.handwriting.ocr.Classifier;
import org.literacyapp.handwriting.ocr.TensorFlowImageClassifier;
import org.literacyapp.handwriting.util.MediaPlayerHelper;
import org.literacyapp.handwriting.view.DrawModel;
import org.literacyapp.handwriting.view.DrawView;
import org.literacyapp.handwriting.view.DrawViewOnTouchListener;

import java.util.List;

public class WriteNumberActivity extends AppCompatActivity implements View.OnTouchListener {

    private AudioDao audioDao;
    private Number number;

    private static final int PIXEL_WIDTH = 28;

    private DrawModel mModel;
    private DrawView mDrawView;

    private static final int INPUT_SIZE = 28;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "output";

    private static final String MODEL_FILE = "file:///android_asset/expert-graph.pb";
    private static final String LABEL_FILE =
            "file:///android_asset/emnist_digits_labels.txt";

    private Classifier classifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(getClass().getName(), "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_write);

        DaoSession daoSession = ContentProvider.getDaoSession();
        audioDao = daoSession.getAudioDao();

        List<Number> unlockedNumbers = ContentProvider.getUnlockedNumbers();
        number = unlockedNumbers.get((int)(Math.random() * unlockedNumbers.size()));
        Log.i(getClass().getName(), "number: " + number);
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(number.getValue().toString());
        // Set on listener to restart the drawing with a blank screen
        textView.setOnTouchListener(this);

        initTensorFlowAndLoadModel();

        mModel = new DrawModel(PIXEL_WIDTH, PIXEL_WIDTH);

        mDrawView = (DrawView) findViewById(R.id.view_draw);
        mDrawView.setModel(mModel);
        DrawViewOnTouchListener listener = new DrawViewOnTouchListener(mDrawView, mModel, classifier, number.getValue().toString(), getApplicationContext());
        mDrawView.setOnTouchListener(listener);
    }

    private void initTensorFlowAndLoadModel() {
        Log.i(getClass().getName(), "initTensorFlowAndLoadModel");
        try {
            classifier = TensorFlowImageClassifier.create(
                    getAssets(),
                    MODEL_FILE,
                    LABEL_FILE,
                    INPUT_SIZE,
                    INPUT_NAME,
                    OUTPUT_NAME);
            Log.d(getClass().getName(), "Load Success");
        } catch (final Exception e) {
            throw new RuntimeException("Error initializing TensorFlow!", e);
        }
    }

    @Override
    protected void onResume() {
        Log.i(getClass().getName(), "onResume");
        super.onResume();

        mDrawView.onResume();

        MediaPlayer mediaPlayer = MediaPlayerHelper.playInstructionSound(getApplicationContext());
        if (mediaPlayer != null){
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    MediaPlayerHelper.playNumberSound(getApplicationContext(), audioDao, number);
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDrawView.onPause();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        mModel.clear();
        mDrawView.reset();
        mDrawView.invalidate();
        return true;
    }
}
