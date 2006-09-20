package thinlet.xml;

import java.util.StringTokenizer;

import thinlet.ThinletDesktop;
import thinlet.help.TLFont;

public class FontParser
{

private final ThinletDesktop desktop;

public FontParser(ThinletDesktop desktop)
{
	this.desktop = desktop;
}

public TLFont parse(String value)
{
	String name = null;
	boolean bold = false; boolean italic = false;
	int size = 0;
	StringTokenizer st = new StringTokenizer(value);
	while (st.hasMoreTokens())
	{
		String token = st.nextToken();
		if ("bold".equalsIgnoreCase(token)) bold = true;
		else if ("italic".equalsIgnoreCase(token)) italic = true;
		else
		{
			try
			{ size = Integer.parseInt(token); }
			catch (NumberFormatException nfe)
			{ name = name == null ? new String(token) : (name + " " + token); }
		}
	}
	if (name == null) name = desktop.getDefaultFont().getName();
	if (size == 0) size = desktop.getDefaultFont().getSize();
	return desktop.createFont(name, (bold ? TLFont.BOLD : 0) | (italic ? TLFont.ITALIC : 0), size);
}

}
