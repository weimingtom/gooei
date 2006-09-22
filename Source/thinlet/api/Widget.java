package thinlet.api;

import java.awt.Dimension;
import java.awt.Rectangle;

import thinlet.PopupMenuElement;
import thinlet.help.MouseInteraction;
import thinlet.lwjgl.LwjglWidgetRenderer;
import de.ofahrt.utils.input.Keys;
import de.ofahrt.utils.input.MouseEvent;

public interface Widget
{

// Parent Widget
ContainerWidget parent();
void setParent(ContainerWidget widget);

// PopupMenu
PopupMenuElement getPopupMenuWidget();
void setPopupMenuWidget(PopupMenuElement popupmenuElement);

// Properties
String getName();
boolean isVisible();
boolean isEnabled();

Rectangle getBounds();
void setBounds(int x, int y, int width, int height);

// Layout and Rendering
Dimension getPreferredSize();
void doLayout();
void render(LwjglWidgetRenderer renderer, boolean enabled);
void repaint();
void update(String mode);

// Mouse Interaction
boolean findComponent(MouseInteraction mouseInteraction, int x, int y);
void handleMouseEvent(Object part, MouseInteraction mouseInteraction, MouseEvent event);

// Keyboard Interaction
boolean checkMnemonic(Object checked, Keys keycode, int modifiers);

}
