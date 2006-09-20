package thinlet.awt;

import java.awt.Font;

import thinlet.help.TLFont;

final class AWTFont implements TLFont
{

private final Font font;

public AWTFont(Font font)
{
	this.font = font;
}

public Font getFont()
{ return font; }

public String getName()
{ return font.getName(); }

public int getSize()
{ return font.getSize(); }

}
