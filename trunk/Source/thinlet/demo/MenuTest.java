package thinlet.demo;

import thinlet.UIController;
import thinlet.impl.ThinletDesktop;
import thinlet.lwjgl.LwjglThinletDesktop;

public class MenuTest implements UIController
{

public static void main(String[] args) throws Exception
{
	ThinletDesktop desktop = new LwjglThinletDesktop();
	desktop.parseAndAdd(new MenuTest(desktop), "thinlet/demo/menu.xml");
	desktop.show();
}

@SuppressWarnings("unused")
private final ThinletDesktop desktop;

public MenuTest(ThinletDesktop desktop)
{ this.desktop = desktop; }

}