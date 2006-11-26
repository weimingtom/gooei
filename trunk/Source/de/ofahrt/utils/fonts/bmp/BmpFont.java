package de.ofahrt.utils.fonts.bmp;

import java.io.IOException;
import java.io.InputStream;

import de.ofahrt.utils.fonts.Font;
import de.ofahrt.utils.fonts.FontDrawInterface;
import de.ofahrt.utils.fonts.FontMetrics;
import de.yvert.jingle.impl.reader.ImageReader_tga;
import de.yvert.jingle.ldr.LdrImage2D;

public class BmpFont implements Font
{

private final BmpData data;
private final LdrImage2D image;

public BmpFont(BmpData data, LdrImage2D image)
{
	this.data = data;
	this.image = image;
}

BmpData getData()
{ return data; }

public String getName()
{ return data.name; }

public int getSize()
{ return data.pixelSize; }

public float getPointSize()
{ throw new UnsupportedOperationException(); }

public Font deriveFontByPointSize(int newPointSize)
{ throw new UnsupportedOperationException(); }

public Font deriveFontByPixelSize(int pixelSize)
{ throw new UnsupportedOperationException(); }

public FontMetrics getMetrics()
{ return new BmpMetrics(this); }

public int drawGlyph(FontDrawInterface graphics, char c, int x, int y)
{
	CharInfo info = data.charInfos[c];
	for (int i = 0; i < info.width; i++)
		for (int j = 0; j < info.height; j++)
		{
			float frac = image.getAlpha(i+info.x, j+info.y)/255.0f;
			graphics.drawPixel(x+i+info.xoffset, y+data.lineHeight-(j+info.yoffset), frac);
		}
	return info.xadvance;
}

public static BmpFont load(String name) throws IOException
{
	InputStream in = Test.class.getClassLoader().getResourceAsStream("de/ofahrt/fonts/kevsdemo/demo.fnt");
	BmpData data = new BmpFontParser().parse(in);
	in = Test.class.getClassLoader().getResourceAsStream("de/ofahrt/fonts/kevsdemo/demo_00.tga");
	LdrImage2D image = new ImageReader_tga().load(in);
	return new BmpFont(data, image);
}

}
