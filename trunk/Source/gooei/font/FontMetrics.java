package gooei.font;

public interface FontMetrics
{

int getLeading();
int getAscent();
int getDescent();
int getHeight();
int stringWidth(CharSequence str, int off, int len);
int stringWidth(CharSequence str);
int charWidth(char c);

void drawString(FontDrawInterface graphics, int x, int y, CharSequence csq);
void drawString(FontDrawInterface graphics, int x, int y, CharSequence csq, int off, int len);

}
