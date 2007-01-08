package gooei;

import gooei.font.Font;
import gooei.utils.Alignment;
import gooei.utils.Icon;
import gooei.utils.TLColor;

public interface IconAndText
{

String getText();
Icon getIcon();
Alignment getAlignment();
Font getFont(Font defaultFont);
TLColor getForeground(TLColor def);
TLColor getBackground(TLColor def);

}
