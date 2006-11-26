package thinlet.fonts;

import java.io.IOException;

import thinlet.help.TLFont;
import thinlet.lwjgl.GLFont;
import de.ofahrt.utils.fonts.tri.TriData;
import de.ofahrt.utils.fonts.tri.TriFont;
import de.ofahrt.utils.fonts.ttf.TtfData;

public class TriFontProvider implements FontProvider
{

public TLFont getFont(String name, int style, int pixelSize) throws IOException
{
	TriData data = new TriData(TtfData.load(name));
	float size = data.pixelToPointSize(pixelSize);
	return new GLFont(new TriFont(data, size));
}

}
