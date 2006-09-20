package thinlet.demo;

import thinlet.ThinletDesktop;
import thinlet.api.UIController;
import thinlet.lwjgl.LwjglThinletDesktop;

public class Transparency implements UIController
{

public static void main(String[] args) throws Exception
{
	if (args.length == 0)
		System.setProperty("org.lwjgl.librarypath", "load_from_resource");
	
	ThinletDesktop desktop = new LwjglThinletDesktop();
	desktop.setBackground(desktop.createColor(0, 0, 0, 0));
	desktop.parseAndAdd(new Transparency(), "thinlet/demo/transparency.xml");
	desktop.show();
}

public Transparency()
{/*OK*/}

}