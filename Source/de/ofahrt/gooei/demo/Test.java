package de.ofahrt.gooei.demo;

import gooei.UIController;
import de.ofahrt.gooei.lwjgl.LwjglDesktop;

/**
 * Simple demonstration of widgets and events
 */
public class Test implements UIController
{

public static void main(String[] args) throws Exception
{
	LwjglDesktop desktop = new LwjglDesktop();
	desktop.parseAndAdd(new Test(), "de/ofahrt/gooei/demo/test.xml");
	desktop.show();
}

public Test()
{/*OK*/}

}