package thinlet;

import java.awt.Dimension;

import de.ofahrt.utils.input.Keys;

import thinlet.impl.ThinletDesktop;
import thinlet.lwjgl.LwjglWidgetRenderer;

public interface MenuElement extends Element, Mnemonic
{

Dimension getSize(ThinletDesktop desktop, int dx, int dy);
void paint(LwjglWidgetRenderer renderer, boolean armed);
boolean checkMnemonic(Keys keycode, int modifiers);

}
