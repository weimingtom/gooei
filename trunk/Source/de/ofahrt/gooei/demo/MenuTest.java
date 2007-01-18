package de.ofahrt.gooei.demo;

import gooei.UIController;
import de.ofahrt.gooei.lwjgl.LwjglDesktop;

public class MenuTest implements UIController
{

public static void main(String[] args) throws Exception
{
	LwjglDesktop desktop = new LwjglDesktop();
	desktop.parseAndAdd(new MenuTest(desktop), "de/ofahrt/gooei/demo/menu.xml");
	desktop.show();
}

@SuppressWarnings("unused")
private final LwjglDesktop desktop;

public MenuTest(LwjglDesktop desktop)
{ this.desktop = desktop; }

}