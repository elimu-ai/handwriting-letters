package org.literacyapp.handwriting;

import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import ai.elimu.content_provider.utils.ContentProviderUtil;
import ai.elimu.model.v2.gson.content.LetterGson;

import org.literacyapp.handwriting.entity.Engine;
import org.literacyapp.handwriting.entity.LanguageProcessor;
import org.literacyapp.handwriting.entity.LetterBuffer;
import org.literacyapp.handwriting.lang.EnglishProcessor;
import org.literacyapp.handwriting.ocr.Ocr;
import org.literacyapp.handwriting.util.MediaPlayerHelper;

import java.lang.reflect.Constructor;
import java.util.List;

public class WriteLetterActivity extends AppCompatActivity {

    private static final String TAG = "WriteLetterActivity";

    private Ocr ocr;

    private LanguageProcessor langProc;

    private LetterBuffer lBuffer;

    private LetterGson letter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_write_letter);

        ocr = (Ocr) findViewById(R.id.writePad);

        List<LetterGson> letters = ContentProviderUtil.INSTANCE.getAllLetterGsons(
                this, BuildConfig.CONTENT_PROVIDER_APPLICATION_ID);
        for (LetterGson l : letters) {
            if ("a".equals(l.getText())) {
                letter = l;
                break;
            }
        }
        Log.d(TAG, "letter: " + letter);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();

        try {
            loadProcessor(EnglishProcessor.class.getName());
        } catch (Exception e) {
            Log.e(TAG, "loadProcessor failed", e);
        }

        setDelay(500);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        MediaPlayer mediaPlayer = MediaPlayerHelper.playInstructionSound(getApplicationContext());
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    MediaPlayerHelper.playLetterSound(getApplicationContext(), letter);
                }
            });
        }
    }

    private void loadProcessor(String classname) throws Exception {
        Log.d(TAG, "loadProcessor: " + classname);

        Class<LanguageProcessor> c = (Class<LanguageProcessor>) Class.forName(classname);
        Constructor<LanguageProcessor> ct = c.getConstructor(Engine.class);

        langProc = ct.newInstance(ocr.getEngine());
        lBuffer = new LetterBuffer(langProc);
        ocr.loadEngine(langProc, lBuffer);
    }

    private void setDelay(long delay) {
        Log.d(TAG, "setDelay: " + delay);

        ocr.setDelay(delay);
    }
}
