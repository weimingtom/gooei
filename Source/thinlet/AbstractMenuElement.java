package thinlet;

import java.awt.Dimension;

import thinlet.api.MenuElement;
import thinlet.lwjgl.LwjglWidgetRenderer;

public abstract class AbstractMenuElement extends AbstractElement implements MenuElement
{

protected AbstractMenuElement()
{/*OK*/}

public abstract Dimension getSize(ThinletDesktop desktop, int dx, int dy);
public abstract void paint(LwjglWidgetRenderer renderer, boolean armed);

}
