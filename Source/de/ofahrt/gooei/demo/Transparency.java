package de.ofahrt.gooei.demo;

import de.ofahrt.gooei.impl.ThinletDesktop;
import de.ofahrt.gooei.lwjgl.LwjglDesktop;
import gooei.UIController;

public class Transparency implements UIController
{

public static void main(String[] args) throws Exception
{
	ThinletDesktop desktop = new LwjglDesktop();
	desktop.setBackground(desktop.createColor(0, 0, 0, 0));
	desktop.parseAndAdd(new Transparency(), "thinlet/demo/transparency.xml");
	desktop.show();
}

public Transparency()
{/*OK*/}

}