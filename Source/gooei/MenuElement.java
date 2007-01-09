package gooei;

import gooei.input.Keys;

import java.awt.Dimension;

import de.ofahrt.gooei.lwjgl.LwjglRenderer;

public interface MenuElement extends Element, IconAndText, Mnemonic
{

Dimension getSize(Desktop desktop, int dx, int dy);
void paint(LwjglRenderer renderer, boolean armed);
boolean checkMnemonic(Keys keycode, int modifiers);

}
