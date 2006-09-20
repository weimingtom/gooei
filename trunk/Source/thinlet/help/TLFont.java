package thinlet.help;

public interface TLFont
{

int BOLD   = 1;
int ITALIC = 2;

String getName();
int getSize();
TLFontMetrics getFontMetrics();

}
