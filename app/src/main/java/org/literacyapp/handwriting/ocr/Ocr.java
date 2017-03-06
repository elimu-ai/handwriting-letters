package org.literacyapp.handwriting.ocr;

import android.content.Context;
import android.graphics.Typeface;
import android.inputmethodservice.InputMethodService;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.literacyapp.handwriting.entity.CharData;
import org.literacyapp.handwriting.entity.Engine;
import org.literacyapp.handwriting.entity.LanguageProcessor;
import org.literacyapp.handwriting.entity.LetterBuffer;
import org.literacyapp.handwriting.entity.MultiSOM;
import org.literacyapp.handwriting.view.SingleTouchEventView;

import java.util.ArrayList;
import java.util.List;

public class Ocr extends SingleTouchEventView {

	private static int DOWNSAMPLE_HEIGHT = 50;
	private static int DOWNSAMPLE_WIDTH = 50;
	private static final int MAX_SLICE_LENGTH = 70;
	private Entry entry;
	private CharData[] neuronMap;
	
	private Engine engine;
	private LanguageProcessor langProc;
	private LetterBuffer lBuffer;

	private InputMethodService imeService;
	/**
	 * The neural network.
	 */
	private MultiSOM net;
	//private SOM net;
	
	private int lettersToDelete=0;

	public Engine getEngine() {
		return engine;
	}
	public void setImeService(InputMethodService imeService) {
		this.imeService = imeService;
	}
	
	public Ocr(Context context, AttributeSet attrs) {
		super(context, attrs);
		/*externalDir = "/mnt/sdcard/Android/data/me.sinu.thulika.train/files";
		engineDir = externalDir.endsWith("/")? externalDir : externalDir+"/";
		engineDir = engineDir + ENGINEDIR;
		File eDir = new File(engineDir);
		if(!eDir.isDirectory()) {
			eDir.mkdir();
		}
		if(!eDir.isDirectory()) {
			Log.e(TAG, "Directory doesnt exist : " + engineDir);
		}*/
	}	
	
	public void init() {
		SampleData sampleData = new SampleData("?", DOWNSAMPLE_WIDTH, DOWNSAMPLE_HEIGHT);
		entry = new Entry(this.getWidth(), this.getHeight());
		entry.setSampleData(sampleData);
	}
	
	public void loadEngine(LanguageProcessor langProc, LetterBuffer lBuffer) {
		this.langProc = langProc;
		this.lBuffer = lBuffer;
		Engine en = new Engine();
		try {
			en.restore(getContext().getAssets().open(langProc.getEngineName()));
			this.engine = en;
			this.net = en.getNet();
			this.neuronMap = en.getNeuronMap();
			Ocr.DOWNSAMPLE_WIDTH = en.getSampleDataWidth();
			Ocr.DOWNSAMPLE_HEIGHT = en.getSampleDataHeight();
			
			SampleData sampleData = new SampleData("?", DOWNSAMPLE_WIDTH, DOWNSAMPLE_HEIGHT);
			/*if(entry==null) {
				entry = new Entry(this.getWidth(), this.getHeight());
			}*/
			entry.setSampleData(sampleData);
			
			//init();
			Toast msg = Toast.makeText(getContext(),"Keyboard:" + langProc.getEngineName(), Toast.LENGTH_SHORT);
			msg.show();
		} catch (Exception e) {
			//Log.e(TAG, "Cannot Load " + langProc.getEngineName()/*files[0].getPath()*/, e);
		}
		
		cleanAllViews();
	}
	
	private void cleanAllViews() {
		lBuffer.emptyBuffer();
//		suggestionsViewGroup.removeAllViews();
//		stackView.setText("");
		showSliceText();
	}
	
	/**
	 * Called when the recognize button is pressed.
	 */
	public CharData[] recognizeAction(int count) {
		if (this.net == null) {
			//Log.e(TAG, "I need to be trained first!");
			return null;
		}
		if(count>this.engine.getNeuronMap().length) {
			count = this.engine.getNeuronMap().length;
		}
		if(entry==null) {
			init();
		}
		this.entry.downsample(getImagePixels());

		final MLData input = new BasicMLData(Ocr.DOWNSAMPLE_HEIGHT * Ocr.DOWNSAMPLE_WIDTH);
		int idx = 0;
		final SampleData ds = this.entry.getSampleData();
		for (int y = 0; y < ds.getHeight(); y++) {
			for (int x = 0; x < ds.getWidth(); x++) {
				input.setData(idx++, ds.getData(x, y) ? .5 : -.5);
			}
		}

		final int[] result = this.net.matches(input, count); 
		//CharData[] ret = new CharData[count];
		//for(int i=0; i<count; i++) {
				//ret[i] = neuronMap[result[i]];
		//}
		
		List<CharData> retList = new ArrayList<CharData>();
		for(int i=0; i<count; i++) {
			CharData newData = neuronMap[result[i]];
			if(!retList.contains(newData)) {
				retList.add(newData);
			}
		}
		CharData[] ret = retList.toArray(new CharData[]{});
		
		clearAction();
		//return neuronMap[best];
		return ret;
	}
	
	@Override
	protected void onTouchUp() {
		Log.i(getClass().getName(), "onTouchUp");

		if(super.isSmallPath()) {
			if(imeService!=null) {
				imeService.requestHideSelf(0);
				super.clear();
				hideView = true;
				return;
			}
		}
		
		CharData[] cData = this.recognizeAction(10);
		Log.i(getClass().getName(), "cData: " + cData);
		
		showCandidates();
	}

	private void clearAction() {
		this.entry.clear();
		super.clear();
	}

	public void dumbProcess(CharData c) {
		Log.i(getClass().getName(), "dumbProcess");

		showSliceText();
		showCandidates();
	}
	
	private void showCandidates() {
		Log.i(getClass().getName(), "showCandidates");

		Log.i(getClass().getName(), "langProc: " + langProc);
		if (langProc != null) {
			Log.i(getClass().getName(), "langProc.getStack(): " + langProc.getStack());
		}

		Log.i(getClass().getName(), "lBuffer: " + lBuffer);
		Log.i(getClass().getName(), "lBuffer.isEmpty(): " + lBuffer.isEmpty());

		if(!lBuffer.isEmpty()) {
			/*List<CharData>*/CharData[] suggestions = lBuffer.getSuggestions();
			Log.i(getClass().getName(), "suggestions: " + suggestions);
			if(suggestions!=null /*&& !suggestions.isEmpty()*/) {
				Context ctx = this.getContext();
				
				Typeface font = null;
				if(langProc.getFontName()!=null) {
					font = Typeface.createFromAsset(this.getContext().getAssets(), langProc.getFontName());
//					stackView.setTypeface(font);
				} else {
//					stackView.setTypeface(Typeface.SANS_SERIF);
				}
				
				for(final CharData suggestion : suggestions) {
					TextView suggestView = new TextView(ctx);
					suggestView.setMinimumHeight(30);
					suggestView.setMinimumWidth(70);
//					suggestView.setTextAppearance(ctx, R.style.suggestText);
					if(suggestion == suggestions[0]) {
						suggestView.setPressed(true);
					}
					suggestView.setText(suggestion.getSymbol());
					suggestView.setGravity(Gravity.CENTER);
//					suggestView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.line, 0);
					
					if(font!=null) {
						suggestView.setTypeface(font);
					}
					
					suggestView.setOnClickListener(new View.OnClickListener() {
		    			public void onClick(View v) {
		    				String[] result = lBuffer.replace(suggestion);
		    				
		    				showSliceText();
		    				showCandidates();
		    			}
		    		});
//					suggestionsViewGroup.addView(suggestView);
				}
			}
		}
		//suggestionsViewGroup.invalidate();
//		((HorizontalScrollView) suggestionsViewGroup.getParent()).scrollTo(0, 0);
	}
	
	private void putText(String previous, String[] result) {
		Log.i(getClass().getName(), "putText");

		if(result==null) {
			return;
		}
	}
	
	public void showSliceText() {
		Log.i(getClass().getName(), "showSliceText");
	}
}
