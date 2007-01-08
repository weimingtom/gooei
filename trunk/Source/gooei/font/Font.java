package gooei.font;

public interface Font
{

int BOLD   = 1;
int ITALIC = 2;

String getName();
int getSize();
FontMetrics getMetrics();
Font deriveFontByPointSize(int newPointSize);
Font deriveFontByPixelSize(int pixelSize);

}
