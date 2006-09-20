package thinlet.api;

import java.awt.Dimension;

import thinlet.ThinletDesktop;
import thinlet.lwjgl.LwjglWidgetRenderer;

public interface MenuElement extends Element, MnemonicWidget
{

Dimension getSize(ThinletDesktop desktop, int dx, int dy);
void paint(LwjglWidgetRenderer renderer, boolean armed);

}
