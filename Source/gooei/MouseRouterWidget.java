package gooei;

/**
 * This interface allows a widget to participate in mouse event routing.
 * 
 * @see MouseableWidget
 * @see ContainerWidget
 */
public interface MouseRouterWidget extends Widget
{

/**
 * Finds the component that contains the mouse pointer.
 * The x,y coordinates are the mouse coordinates relative to this components
 * origin. The results of this method are stored in the given
 * {@link MouseInteraction}. An implementation of this method should check if
 * the current component is visible.
 * <p>
 * After the component that contains the mouse is determined, mouse events
 * are directly delivered to that component.
 */
void findComponent(MouseInteraction mouseInteraction, int x, int y);

}
