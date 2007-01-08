package de.ofahrt.gooei.font;

import gooei.font.Font;
import gooei.font.FontProvider;

import java.io.IOException;

import de.ofahrt.gooei.font.ttf.TtFont;
import de.ofahrt.gooei.font.ttf.TtfData;

public class TtFontProvider implements FontProvider
{

public Font getFont(String name, int style, int pixelSize) throws IOException
{
	TtfData data = TtfData.load(name);
	float size = data.pixelToPointSize(pixelSize);
	return new TtFont(data, size);
}

}
