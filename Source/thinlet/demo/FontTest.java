package thinlet.demo;

import thinlet.ThinletDesktop;
import thinlet.api.UIController;
import thinlet.lwjgl.LwjglThinletDesktop;

public class FontTest implements UIController
{

public static void main(String[] args) throws Exception
{
	if (args.length == 0)
		System.setProperty("org.lwjgl.librarypath", "load_from_resource");
	
	ThinletDesktop desktop = new LwjglThinletDesktop();
	desktop.parseAndAdd(new FontTest(desktop), "thinlet/demo/font.xml");
	desktop.show();
}

@SuppressWarnings("unused")
private final ThinletDesktop desktop;

public FontTest(ThinletDesktop desktop)
{ this.desktop = desktop; }

}