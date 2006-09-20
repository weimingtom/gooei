package thinlet.api;

import java.awt.Rectangle;

import de.ofahrt.utils.games.Keys;

public interface Element extends IconWidget
{

// Parent Widget/Element
Object parent();
void setParent(Object object);

// Properties
String getName();
boolean isEnabled();
boolean isVisible();
Rectangle getBounds();
void setBounds(int x, int y, int width, int height);

// Keyboard Activation
boolean checkMnemonic(Object checked, Keys keycode, int modifiers);
void update(String mode);

}
