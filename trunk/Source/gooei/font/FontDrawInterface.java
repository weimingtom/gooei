package gooei.font;

import de.ofahrt.gooei.font.tri.TriGlyph;

public interface FontDrawInterface
{

void drawPixel(int i, int j, float frac);
void drawTriangles(float x, float y, float scale, TriGlyph glyph);

}
