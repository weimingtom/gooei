package de.ofahrt.utils.fonts;

public interface Font
{

Font deriveFontByPointSize(int newPointSize);
Font deriveFontByPixelSize(int pixelSize);
FontMetrics getMetrics();
String getName();
int getSize();

}
