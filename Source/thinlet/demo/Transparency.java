package thinlet.demo;

import thinlet.UIController;
import thinlet.impl.ThinletDesktop;
import thinlet.lwjgl.LwjglThinletDesktop;

public class Transparency implements UIController
{

public static void main(String[] args) throws Exception
{
	ThinletDesktop desktop = new LwjglThinletDesktop();
	desktop.setBackground(desktop.createColor(0, 0, 0, 0));
	desktop.parseAndAdd(new Transparency(), "thinlet/demo/transparency.xml");
	desktop.show();
}

public Transparency()
{/*OK*/}

}