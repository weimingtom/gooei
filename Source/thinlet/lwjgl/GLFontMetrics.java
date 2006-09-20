package thinlet.lwjgl;

import thinlet.help.TLFontMetrics;
import de.ofahrt.utils.fonts.Font;
import de.ofahrt.utils.fonts.FontMetrics;

public final class GLFontMetrics implements TLFontMetrics
{

FontMetrics metrics;

public GLFontMetrics(Font font)
{ metrics = font.getMetrics(); }

public int getAscent()
{ return metrics.getAscent(); }

public int getDescent()
{ return metrics.getDescent(); }

public int getHeight()
{ return metrics.getHeight(); }

public int getLeading()
{ return metrics.getLeading(); }

public int charWidth(char c)
{ return metrics.charWidth(c); }

public int charsWidth(char[] chars, int off, int len)
{ return metrics.charsWidth(chars, off, len); }

public int stringWidth(String str)
{ return metrics.stringWidth(str); }

}
