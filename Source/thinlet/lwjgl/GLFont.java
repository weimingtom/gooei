package thinlet.lwjgl;

import thinlet.help.TLFont;
import de.ofahrt.utils.fonts.Font;

public final class GLFont implements TLFont
{

Font font;

public GLFont(Font font)
{ this.font = font; }

public String getName()
{ return font.getName(); }

public int getSize()
{ return font.getSize(); }

public GLFontMetrics getFontMetrics()
{ return new GLFontMetrics(font); }

}
