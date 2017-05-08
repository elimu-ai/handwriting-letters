package org.literacyapp.handwriting.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import org.literacyapp.handwriting.FinalActivity;
import org.literacyapp.handwriting.ocr.Classifier;
import org.literacyapp.handwriting.ocr.TensorFlowImageClassifier;

import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by sladomic on 05.05.17.
 */

public class DrawViewOnTouchListener implements View.OnTouchListener {
    private PointF mTmpPoint = new PointF();

    private float mLastX;
    private float mLastY;

    private DrawView mDrawView;
    private DrawModel mModel;

    private Classifier classifier;

    private String characterToWrite;

    private Context context;

    public DrawViewOnTouchListener(DrawView mDrawView, DrawModel mModel, Classifier classifier, String characterToWrite, Context context) {
        this.mDrawView = mDrawView;
        this.mModel = mModel;
        this.classifier = classifier;
        this.characterToWrite = characterToWrite;
        this.context = context;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_DOWN) {
            processTouchDown(event);
            return true;

        } else if (action == MotionEvent.ACTION_MOVE) {
            processTouchMove(event);
            return true;

        } else if (action == MotionEvent.ACTION_UP) {
            processTouchUp();
            return true;
        }
        return false;
    }

    private void processTouchDown(MotionEvent event) {
        mLastX = event.getX();
        mLastY = event.getY();
        mDrawView.calcPos(mLastX, mLastY, mTmpPoint);
        float lastConvX = mTmpPoint.x;
        float lastConvY = mTmpPoint.y;
        mModel.startLine(lastConvX, lastConvY);
    }

    private void processTouchMove(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        mDrawView.calcPos(x, y, mTmpPoint);
        float newConvX = mTmpPoint.x;
        float newConvY = mTmpPoint.y;
        mModel.addLineElem(newConvX, newConvY);

        mLastX = x;
        mLastY = y;
        mDrawView.invalidate();
    }

    private void processTouchUp() {
        mModel.endLine();
        float pixels[] = mDrawView.getPixelData();

        final List<Classifier.Recognition> results = classifier.recognizeImage(pixels);

        if (results.size() > 0) {
            String toast = "";
            for (int i=0; i < results.size(); i++){
                toast = toast + results.get(i).getTitle() + " " + results.get(i).getConfidence() + "\n";
            }
            Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
            /*if (results.get(0).getTitle().equals(characterToWrite)){
                Intent intent = new Intent(context, FinalActivity.class);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }*/
        }
    }
}
