package thinlet.help;

public interface TLFontMetrics
{

int getLeading();
int getAscent();
int getDescent();
int getHeight();
int stringWidth(String str);
int charsWidth(char[] chars, int off, int len);
int charWidth(char c);

}
