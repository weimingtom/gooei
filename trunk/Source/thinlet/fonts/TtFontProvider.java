package thinlet.fonts;

import java.io.IOException;

import thinlet.help.TLFont;
import thinlet.lwjgl.GLFont;
import de.ofahrt.utils.fonts.ttf.TtFont;
import de.ofahrt.utils.fonts.ttf.TtfData;

public class TtFontProvider implements FontProvider
{

public TLFont getFont(String name, int style, int pixelSize) throws IOException
{
	TtfData data = TtfData.load(name);
	float size = data.pixelToPointSize(pixelSize);
	return new GLFont(new TtFont(data, size));
}

}
