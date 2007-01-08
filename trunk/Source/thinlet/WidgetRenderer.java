package thinlet;

import java.awt.Rectangle;

public interface WidgetRenderer
{

void pushState();
void popState();
boolean moveCoordSystem(Rectangle bounds);

}
