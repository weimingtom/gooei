package de.ofahrt.gooei.demo;

import de.ofahrt.gooei.impl.ThinletDesktop;
import de.ofahrt.gooei.lwjgl.LwjglDesktop;
import gooei.UIController;

/**
 * Simple demonstration of widgets and events
 */
public class Test implements UIController
{

public static void main(String[] args) throws Exception
{
	ThinletDesktop desktop = new LwjglDesktop();
	desktop.parseAndAdd(new Test(), "de/ofahrt/gooei/demo/test.xml");
	desktop.show();
}

public Test()
{/*OK*/}

}