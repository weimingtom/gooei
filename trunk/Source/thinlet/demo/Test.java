package thinlet.demo;

import thinlet.UIController;
import thinlet.impl.ThinletDesktop;
import thinlet.lwjgl.LwjglThinletDesktop;

/**
 * Simple demonstration of widgets and events
 */
public class Test implements UIController
{

public static void main(String[] args) throws Exception
{
	ThinletDesktop desktop = new LwjglThinletDesktop();
	desktop.parseAndAdd(new Test(), "thinlet/demo/test.xml");
	desktop.show();
}

public Test()
{/*OK*/}

}