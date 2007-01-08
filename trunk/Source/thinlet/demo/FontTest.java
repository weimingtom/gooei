package thinlet.demo;

import thinlet.UIController;
import thinlet.impl.ThinletDesktop;
import thinlet.lwjgl.LwjglThinletDesktop;

public class FontTest implements UIController
{

public static void main(String[] args) throws Exception
{
	ThinletDesktop desktop = new LwjglThinletDesktop();
	desktop.parseAndAdd(new FontTest(desktop), "thinlet/demo/font.xml");
	desktop.show();
}

@SuppressWarnings("unused")
private final ThinletDesktop desktop;

public FontTest(ThinletDesktop desktop)
{ this.desktop = desktop; }

}