package de.ofahrt.gooei.font.ttf;

import gooei.font.FontDrawInterface;
import gooei.font.FontMetrics;

public class TtfMetrics implements FontMetrics
{

private final float cfak;
private final TtFont font;

private final int ascent, descent;
private final int leading;

public TtfMetrics(TtFont font)
{
	this.font = font;
	cfak = (font.getPointSize()*96.0f)/(72.0f*font.fontData.getUpem());
	ascent = Math.round(font.fontData.getAscender()*cfak)+1;
	descent = Math.round(-font.fontData.getDescender()*cfak)+1;
	leading = 0;
}

public int getAscent()
{ return ascent; }

public int getDescent()
{ return descent; }

public int getLeading()
{ return leading; }

public int getHeight()
{ return descent+ascent; }

public int charWidth(char c)
{
	TtfGlyph glyph = font.fontData.getGlyph(c);
	return Math.round(glyph.metric.advanceWidth*cfak+0.5f);
}

public int stringWidth(CharSequence csq, int off, int len)
{
	int width = 0;
	for (int i = off; i < off+len; i++)
	{
		TtfGlyph glyph = font.fontData.getGlyph(csq.charAt(i));
		int advanceWidth = Math.round(glyph.metric.advanceWidth*cfak+0.5f);
		width += advanceWidth;
	}
	return width;
}

public int stringWidth(CharSequence csq)
{ return stringWidth(csq, 0, csq.length()); }

public void drawString(FontDrawInterface graphics, int x, int y, CharSequence csq, int off, int len)
{
	for (int i = 0; i < len; i++)
		x += font.drawGlyph(graphics, csq.charAt(off+i), x, y);
}

public void drawString(FontDrawInterface graphics, int x, int y, CharSequence csq)
{ drawString(graphics, x, y, csq, 0, csq.length()); }

}
