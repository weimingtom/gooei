package de.ofahrt.gooei.demo;

import de.ofahrt.gooei.impl.ThinletDesktop;
import de.ofahrt.gooei.lwjgl.LwjglDesktop;
import gooei.UIController;

public class MenuTest implements UIController
{

public static void main(String[] args) throws Exception
{
	ThinletDesktop desktop = new LwjglDesktop();
	desktop.parseAndAdd(new MenuTest(desktop), "thinlet/demo/menu.xml");
	desktop.show();
}

@SuppressWarnings("unused")
private final ThinletDesktop desktop;

public MenuTest(ThinletDesktop desktop)
{ this.desktop = desktop; }

}