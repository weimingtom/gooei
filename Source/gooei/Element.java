package gooei;

import java.awt.Rectangle;

public interface Element //extends IconAndText
{

// Parent ElementContainer
ElementContainer<?> parent();
void setParent(ElementContainer<?> parent);

// Properties
String getName();
boolean isEnabled();
boolean isVisible();
Rectangle getBounds();
void setBounds(int x, int y, int width, int height);

String getText();

}
