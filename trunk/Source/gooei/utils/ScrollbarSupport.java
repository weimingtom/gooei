package gooei.utils;

import gooei.Element;
import gooei.MouseInteraction;
import gooei.Renderer;
import gooei.input.MouseEvent;

public interface ScrollbarSupport
{

void scrollToVisible(int x, int y, int w, int h);

/** Determine if the mouse is inside one of the scrollbars. */
boolean findScroll(MouseInteraction mouseInteraction, int x, int y);

void repaintScrollablePart(Element part);

/**
 * Process scrollbar mouse event.
 * Returns true if the event was handled.
 */
boolean handleMouseEvent(Object part, MouseInteraction mouseInteraction, MouseEvent event);

/** Handle a scroll event for part. */
boolean handleScrollEvent(Object part);

/** Paint the scroll bars. */
void paintScrollbars(Renderer r, boolean pressed, boolean inside, boolean isenabled);

}
