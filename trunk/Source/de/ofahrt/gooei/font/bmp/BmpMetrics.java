package de.ofahrt.gooei.font.bmp;

import gooei.font.FontDrawInterface;
import gooei.font.FontMetrics;
import gooei.font.FontTriangleInterface;

public class BmpMetrics implements FontMetrics
{

private final BmpFont font;
private final BmpData data;

public BmpMetrics(BmpFont font)
{
	this.font = font;
	this.data = font.getData();
}

public int getAscent()
{ return data.base; }

public int getDescent()
{ return data.lineHeight-data.base; }

public int getLeading()
{ return 0; }

public int getHeight()
{ return data.lineHeight; }

public int charWidth(char c)
{
	CharInfo glyph = data.charInfos[c];
	return glyph.xadvance;
}

public int stringWidth(String csq)
{
	int width = 0;
	for (int i = 0; i < csq.length(); i++)
	{
		CharInfo glyph = data.charInfos[csq.charAt(i)];
		width += glyph.xadvance;
	}
	return width;
}

public int charsWidth(char[] chars, int off, int len)
{
	int width = 0;
	for (int i = off; i < off+len; i++)
	{
		CharInfo glyph = data.charInfos[chars[i]];
		width += glyph.xadvance;
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
