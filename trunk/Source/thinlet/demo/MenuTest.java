package thinlet.demo;

import thinlet.ThinletDesktop;
import thinlet.api.UIController;
import thinlet.lwjgl.LwjglThinletDesktop;

public class MenuTest implements UIController
{

public static void main(String[] args) throws Exception
{
	if (args.length == 0)
		System.setProperty("org.lwjgl.librarypath", "load_from_resource");
	
	ThinletDesktop desktop = new LwjglThinletDesktop();
	desktop.parseAndAdd(new MenuTest(desktop), "thinlet/demo/menu.xml");
	desktop.show();
}

@SuppressWarnings("unused")
private final ThinletDesktop desktop;

public MenuTest(ThinletDesktop desktop)
{ this.desktop = desktop; }

}