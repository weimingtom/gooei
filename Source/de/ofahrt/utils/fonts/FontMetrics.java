package de.ofahrt.utils.fonts;

public interface FontMetrics
{

int getLeading();
int getAscent();
int getDescent();
int getHeight();
int stringWidth(String str);
int charsWidth(char[] chars, int off, int len);
int charWidth(char c);

void drawString(FontDrawInterface graphics, int x, int y, CharSequence csq);
void drawString(FontDrawInterface graphics, int x, int y, char[] chars, int off, int len);
void drawString(FontTriangleInterface graphics, int x, int y, CharSequence csq);

}
