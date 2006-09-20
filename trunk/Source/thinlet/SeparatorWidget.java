package thinlet;

import java.awt.Dimension;
import java.awt.Rectangle;

import thinlet.lwjgl.LwjglWidgetRenderer;

public final class SeparatorWidget extends AbstractMenuElement
{

public SeparatorWidget()
{/*OK*/}

@Override
public boolean isEnabled()
{ return false; }

public int getMnemonic()
{ return -1; }

@Override
public Dimension getSize(ThinletDesktop desktop, int dx, int dy)
{ return new Dimension(1, 1); }

@Override
public void paint(LwjglWidgetRenderer renderer, boolean armed)
{
	Rectangle r = getBounds();
	renderer.setColor(renderer.c_border);
	renderer.fillRect(r.x, r.y, r.width, r.height);
}

}
