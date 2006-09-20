package thinlet.api;

import java.awt.Rectangle;

public interface ToolTipOwner
{

Rectangle getToolTipBounds();
String getToolTip();
void setToolTipBounds(int x, int y, int width, int height);
void setToolTipBounds(Rectangle rect);

}
