package gooei.xml;

import gooei.font.Font;

import java.util.StringTokenizer;

import de.ofahrt.gooei.impl.ThinletDesktop;

public class FontParser
{

private final ThinletDesktop desktop;

public FontParser(ThinletDesktop desktop)
{
	this.desktop = desktop;
}

public Font parse(String value)
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
	return desktop.createFont(name, (bold ? Font.BOLD : 0) | (italic ? Font.ITALIC : 0), size);
}

}
