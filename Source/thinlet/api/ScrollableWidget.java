package thinlet.api;

import java.awt.Rectangle;

import thinlet.lwjgl.LwjglWidgetRenderer;

public interface ScrollableWidget extends Widget
{

Rectangle getPort();
Rectangle getView();
Rectangle getHorizontal();
Rectangle getVertical();

boolean processScroll(Object part);
void paintScrollableContent(LwjglWidgetRenderer renderer, boolean enabled);

}
