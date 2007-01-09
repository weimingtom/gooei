package gooei;

import java.awt.Rectangle;

public interface ToolTipOwner
{

String getToolTip();
Rectangle getToolTipBounds();
void setToolTipBounds(int x, int y, int width, int height);
void removeToolTipBounds();

}
