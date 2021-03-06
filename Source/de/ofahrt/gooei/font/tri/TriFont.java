package de.ofahrt.gooei.font.tri;

import gooei.font.Font;
import gooei.font.FontDrawInterface;
import gooei.font.FontMetrics;

import java.io.IOException;

import de.ofahrt.gooei.font.ttf.TtfData;

public class TriFont implements Font
{

final TriData fontData;
private final float pointSize;

public TriFont(TriData fontData, float pointSize)
{
	this.fontData = fontData;
	this.pointSize = pointSize;
}

public String getName()
{ return fontData.getName(); }

public int getSize()
{ return Math.round(pointSize); }

public float getPointSize()
{ return pointSize; }

public FontMetrics getMetrics()
{ return new TriMetrics(this); }

public Font deriveFontByPointSize(int newPointSize)
{ return new TriFont(fontData, newPointSize); }

public Font deriveFontByPixelSize(int pixelSize)
{
	float size = fontData.pixelToPointSize(pixelSize);
	return new TriFont(fontData, size);
}

int drawGlyph(FontDrawInterface graphics, char c, int x, int y)
{
	TriGlyph glyph = fontData.getGlyph(c);
	
	float cfak = (pointSize*96.0f)/(72.0f*fontData.getUpem());
//	int lsb = glyph.metric.leftSideBearing;
//	int baseline = Math.round(-fontData.getDescender()*cfak);
//	int height = Math.round((fontData.getAscender()-fontData.getDescender())*cfak);
//	int width = Math.round((glyph.maxx-glyph.minx)*cfak);
	int advanceWidth = Math.round(glyph.metric.advanceWidth*cfak+0.5f);
	
	graphics.drawTriangles(x, y, cfak, glyph);
	
	return advanceWidth;
}

public static TriFont load(String name, int pixelSize) throws IOException
{
	TriData data = new TriData(TtfData.load(name));
	float size = data.pixelToPointSize(pixelSize);
	return new TriFont(data, size);
}

}
