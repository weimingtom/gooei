package thinlet.awt;

import java.awt.FontMetrics;

import thinlet.help.TLFontMetrics;

final class AWTFontMetrics implements TLFontMetrics
{

private final FontMetrics metrics;

public AWTFontMetrics(FontMetrics metrics)
{
	if (metrics == null) throw new NullPointerException();
	this.metrics = metrics;
}

public int stringWidth(String str)
{ return metrics.stringWidth(str); }

public int getLeading()
{ return metrics.getLeading(); }

public int getAscent()
{ return metrics.getAscent(); }

public int getDescent()
{ return metrics.getDescent(); }

public int getHeight()
{ return metrics.getHeight(); }

public int charsWidth(char[] data, int off, int len)
{ return metrics.charsWidth(data, off, len); }

public int charWidth(char ch)
{ return metrics.charWidth(ch); }

}
