package de.ofahrt.utils.fonts.ttf;

import de.ofahrt.utils.fonts.FontDrawInterface;
import de.ofahrt.utils.fonts.FontMetrics;
import de.ofahrt.utils.fonts.FontTriangleInterface;

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

public int stringWidth(String csq)
{
	int width = 0;
	for (int i = 0; i < csq.length(); i++)
	{
		TtfGlyph glyph = font.fontData.getGlyph(csq.charAt(i));
		int advanceWidth = Math.round(glyph.metric.advanceWidth*cfak+0.5f);
		width += advanceWidth;
	}
	return width;
}

public int charsWidth(char[] chars, int off, int len)
{
	int width = 0;
	for (int i = off; i < off+len; i++)
	{
		TtfGlyph glyph = font.fontData.getGlyph(chars[i]);
		int advanceWidth = Math.round(glyph.metric.advanceWidth*cfak+0.5f);
		width += advanceWidth;
	}
	return width;
}

public void drawString(FontDrawInterface graphics, int x, int y, CharSequence csq)
{
	for (int i = 0; i < csq.length(); i++)
		x += font.drawGlyph(graphics, csq.charAt(i), x, y);
}

public void drawString(FontDrawInterface graphics, int x, int y, char[] chars, int off, int len)
{
	for (int i = 0; i < len; i++)
		x += font.drawGlyph(graphics, chars[off+i], x, y);
}

public void drawString(FontTriangleInterface graphics, int x, int y, CharSequence csq)
{ throw new UnsupportedOperationException(); }

}
