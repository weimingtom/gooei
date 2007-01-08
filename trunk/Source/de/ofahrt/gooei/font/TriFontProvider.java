package de.ofahrt.gooei.font;

import gooei.font.Font;
import gooei.font.FontProvider;

import java.io.IOException;

import de.ofahrt.gooei.font.tri.TriData;
import de.ofahrt.gooei.font.tri.TriFont;
import de.ofahrt.gooei.font.ttf.TtfData;

public class TriFontProvider implements FontProvider
{

public Font getFont(String name, int style, int pixelSize) throws IOException
{
	TriData data = new TriData(TtfData.load(name));
	float size = data.pixelToPointSize(pixelSize);
	return new TriFont(data, size);
}

}
