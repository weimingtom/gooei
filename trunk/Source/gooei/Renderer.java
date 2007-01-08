package gooei;

import java.awt.Rectangle;

public interface Renderer
{

void pushState();
void popState();
boolean moveCoordSystem(Rectangle bounds);

}
