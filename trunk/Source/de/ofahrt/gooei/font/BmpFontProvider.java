package de.ofahrt.gooei.font;

import gooei.font.Font;
import gooei.font.FontProvider;

import java.io.IOException;

import de.ofahrt.gooei.font.bmp.BmpFont;

public class BmpFontProvider implements FontProvider
{

public Font getFont(String name, int style, int pixelSize) throws IOException
{
	return BmpFont.load(name);
}

}
