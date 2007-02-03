package gooei.font;

import gooei.utils.PreparedIcon;
import de.ofahrt.gooei.font.tri.TriGlyph;

public interface FontDrawInterface
{

void drawPixel(int i, int j, float frac);
void drawTriangles(float x, float y, float scale, TriGlyph glyph);
void drawTexturedQuad( PreparedIcon icon, int x, int y, int width, int height, float u0, float v0, float u1, float v1 );


}
