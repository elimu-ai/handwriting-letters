package org.literacyapp.handwriting;

import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import org.literacyapp.contentprovider.ContentProvider;
import org.literacyapp.contentprovider.dao.AudioDao;
import org.literacyapp.contentprovider.dao.DaoSession;
import org.literacyapp.contentprovider.dao.LetterDao;
import org.literacyapp.contentprovider.model.content.Letter;
import org.literacyapp.contentprovider.model.content.multimedia.Audio;
import org.literacyapp.contentprovider.util.MultimediaHelper;
import org.literacyapp.handwriting.entity.Engine;
import org.literacyapp.handwriting.entity.LanguageProcessor;
import org.literacyapp.handwriting.entity.LetterBuffer;
import org.literacyapp.handwriting.lang.EnglishProcessor;
import org.literacyapp.handwriting.ocr.Ocr;
import org.literacyapp.handwriting.util.MediaPlayerHelper;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.List;

public class WriteLetterActivity extends AppCompatActivity {

    private Ocr ocr;

    private LanguageProcessor langProc;

    private LetterBuffer lBuffer;

    private AudioDao audioDao;
    private Letter letter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(getClass().getName(), "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_write_letter);

        ocr = (Ocr) findViewById(R.id.writePad);

        DaoSession daoSession = ContentProvider.getDaoSession();
        audioDao = daoSession.getAudioDao();

        List<Letter> unlockedLetters = ContentProvider.getUnlockedLetters();
        letter = unlockedLetters.get((int)(Math.random() * unlockedLetters.size()));
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(letter.getText());
        Log.i(getClass().getName(), "letter: " + letter);
    }

    @Override
    protected void onStart() {
        Log.i(getClass().getName(), "onStart");
        super.onStart();

        try {
            loadProcessor(EnglishProcessor.class.getName());
        } catch (Exception e) {
            Log.e(getClass().getName(), null, e);
        }

        setDelay(500);
    }

    @Override
    protected void onResume() {
        Log.i(getClass().getName(), "onResume");
        super.onResume();

        MediaPlayer mediaPlayer = MediaPlayerHelper.playInstructionSound(getApplicationContext());
        if (mediaPlayer != null){
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    MediaPlayerHelper.playLetterSound(getApplicationContext(), audioDao, letter);
                }
            });
        }
    }

    private void loadProcessor(String classname) throws Exception {
        Log.i(getClass().getName(), "loadProcessor");

        Class<LanguageProcessor> c = (Class<LanguageProcessor>) Class.forName(classname);
        Constructor<LanguageProcessor> ct = c.getConstructor(Engine.class);

        langProc = ct.newInstance(ocr.getEngine());
        lBuffer = new LetterBuffer(langProc);
        ocr.loadEngine(langProc, lBuffer);
    }

    private void setDelay(long delay) {
        Log.i(getClass().getName(), "setDelay");

        ocr.setDelay(delay);
    }
}
