package de.ofahrt.gooei.impl;

import gooei.Desktop;
import gooei.input.Keys;

import java.awt.Dimension;
import java.awt.Rectangle;

import de.ofahrt.gooei.lwjgl.LwjglRenderer;

public final class SeparatorMenuElement extends AbstractMenuElement
{

public SeparatorMenuElement()
{/*OK*/}

@Override
public boolean isEnabled()
{ return false; }

@Override
public Dimension getSize(Desktop desktop, int dx, int dy)
{ return new Dimension(1, 1); }

@Override
public void paint(LwjglRenderer renderer, boolean armed)
{
	Rectangle r = getBounds();
	renderer.setColor(renderer.c_border);
	renderer.fillRect(r.x, r.y, r.width, r.height);
}

public int getMnemonic()
{ return -1; }

public boolean checkMnemonic(Keys keycode, int modifiers)
{ return false; }

}
