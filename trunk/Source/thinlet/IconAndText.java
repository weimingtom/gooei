package thinlet;

import thinlet.help.Alignment;
import thinlet.help.Icon;
import thinlet.help.TLColor;
import thinlet.help.TLFont;

public interface IconAndText
{

String getText();
Icon getIcon();
Alignment getAlignment();
TLFont getFont(TLFont defaultFont);
TLColor getForeground(TLColor def);
TLColor getBackground(TLColor def);

}
