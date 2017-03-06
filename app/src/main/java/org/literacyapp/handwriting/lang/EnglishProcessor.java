package org.literacyapp.handwriting.lang;

import org.literacyapp.handwriting.entity.Engine;
import org.literacyapp.handwriting.lang.common.LatinLangProcessor;

public class EnglishProcessor extends LatinLangProcessor {

	private final String engineName = "engines/english";
	public EnglishProcessor(Engine engine) {
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
