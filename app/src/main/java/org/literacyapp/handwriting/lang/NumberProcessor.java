package org.literacyapp.handwriting.lang;

import org.literacyapp.handwriting.entity.Engine;
import org.literacyapp.handwriting.lang.common.LatinLangProcessor;

public class NumberProcessor extends LatinLangProcessor {

	private final String engineName = "engines/numbers";
	public NumberProcessor(Engine engine) {
		super(engine);
	}

	@Override
	public String getEngineName() {
		return engineName;
	}

	@Override
	public String getFontName() {
		return null;
	}
}
