package gooei.font;

import java.io.IOException;

public interface FontProvider
{

Font getFont(String name, int style, int pixelSize) throws IOException;

}
