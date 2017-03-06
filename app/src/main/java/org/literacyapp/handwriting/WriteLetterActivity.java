package org.literacyapp.handwriting;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.literacyapp.handwriting.entity.Engine;
import org.literacyapp.handwriting.entity.LanguageProcessor;
import org.literacyapp.handwriting.entity.LetterBuffer;
import org.literacyapp.handwriting.lang.EnglishProcessor;
import org.literacyapp.handwriting.ocr.Ocr;

import java.lang.reflect.Constructor;

public class WriteLetterActivity extends AppCompatActivity {

    private Ocr ocr;

    private LanguageProcessor langProc;

    private LetterBuffer lBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(getClass().getName(), "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_write_letter);

        ocr = (Ocr) findViewById(R.id.writePad);
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
