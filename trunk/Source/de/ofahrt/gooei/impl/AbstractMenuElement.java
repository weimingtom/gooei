package de.ofahrt.gooei.impl;

import gooei.MenuElement;

import java.awt.Dimension;

import de.ofahrt.gooei.lwjgl.LwjglRenderer;

public abstract class AbstractMenuElement extends AbstractElement implements MenuElement
{

protected AbstractMenuElement()
{/*OK*/}

public abstract Dimension getSize(ThinletDesktop desktop, int dx, int dy);
public abstract void paint(LwjglRenderer renderer, boolean armed);

}
