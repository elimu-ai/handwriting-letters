package org.literacyapp.handwriting.lang.common;

import org.literacyapp.handwriting.entity.CharData;
import org.literacyapp.handwriting.entity.Engine;
import org.literacyapp.handwriting.entity.LanguageProcessor;

public abstract class LatinLangProcessor implements LanguageProcessor {

	Engine engine;
	
	public LatinLangProcessor(Engine engine) {
		this.engine = engine;
	}
			
	public String getStack() {
		return "";
	}
	
	public String[] process(String previous, CharData current) {
		return new String[] {current.getSymbol()};
	}		
}


