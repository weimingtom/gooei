package gooei;

import gooei.input.MouseEvent;

/**
 * This interface allows a widget to receive mouse events.
 * 
 * @see MouseInteraction
 * @see MouseRouterWidget
 * @see FocusableWidget
 */
public interface MouseableWidget extends Widget
{

/**
 * Handle mouse event.
 * The parameter part contains the part of this component, which is supposed
 * to handle this mouse event. Parts can only be objects that were returned
 * as {@link MouseInteraction#insidepart} by a previous
 * {@link #findComponent(MouseInteraction, int, int)} invocation.
 * The part parameter may differ from {@link MouseInteraction#insidepart}.
 */
void handleMouseEvent(Object part, MouseInteraction mouseInteraction, MouseEvent event);

}
