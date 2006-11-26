package thinlet.fonts;

import java.io.IOException;

import thinlet.help.TLFont;

public interface FontProvider
{

TLFont getFont(String name, int style, int pixelSize) throws IOException;

}
