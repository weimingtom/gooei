package de.ofahrt.gooei.demo;

import de.ofahrt.gooei.impl.ThinletDesktop;
import de.ofahrt.gooei.lwjgl.LwjglDesktop;
import gooei.UIController;

public class FontTest implements UIController
{

public static void main(String[] args) throws Exception
{
	ThinletDesktop desktop = new LwjglDesktop();
	desktop.parseAndAdd(new FontTest(desktop), "thinlet/demo/font.xml");
	desktop.show();
}

@SuppressWarnings("unused")
private final ThinletDesktop desktop;

public FontTest(ThinletDesktop desktop)
{ this.desktop = desktop; }

}