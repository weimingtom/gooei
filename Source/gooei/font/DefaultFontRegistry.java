package gooei.font;

import java.util.HashMap;

public final class DefaultFontRegistry implements FontRegistry
{

private Font defaultFont;
private final HashMap<String,Font> fonts = new HashMap<String,Font>();

public DefaultFontRegistry()
{/*OK*/}

public Font getDefaultFont()
{ return defaultFont; }

public void addFont(String name, Font font)
{
	if (defaultFont == null)
		defaultFont = font;
	fonts.put(name, font);
}

public Font getFont(String name)
{ return fonts.get(name); }

}
