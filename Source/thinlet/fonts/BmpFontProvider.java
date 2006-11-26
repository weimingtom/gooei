package thinlet.fonts;

import java.io.IOException;

import thinlet.help.TLFont;
import thinlet.lwjgl.GLFont;
import de.ofahrt.utils.fonts.bmp.BmpFont;

public class BmpFontProvider implements FontProvider
{

public TLFont getFont(String name, int style, int pixelSize) throws IOException
{
	return new GLFont(BmpFont.load(name));
}

}
