package de.ofahrt.gooei.font;

import gooei.font.Font;
import gooei.font.FontRegistry;

import java.io.IOException;
import java.util.HashMap;

import de.ofahrt.gooei.font.bmp.BmpFont;

public class BmpFontRegistry implements FontRegistry
{

//private static final String DEFAULT_NAME = "SansSerif";
//private static final int DEFAULT_SIZE = 18;

private final HashMap<String,BmpFont> cache = new HashMap<String,BmpFont>();
private Font defaultFont;

public BmpFontRegistry() throws IOException
{
	cache.put("SansSerif", BmpFont.load("de/ofahrt/fonts/kevsdemo/demo.fnt"));
	cache.put("Monospaced", BmpFont.load("de/ofahrt/fonts/testfont/gooei-test-font.fnt"));
}

public Font getDefaultFont()
{
	if (defaultFont == null)
		defaultFont = getFont("Monospaced");
	return defaultFont;
}

public Font getFont(String value)
{
	BmpFont result = cache.get(value);
	if (result == null) return defaultFont;
	return result;
}

}
