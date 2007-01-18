package de.ofahrt.gooei.tilt;

import gooei.UIController;
import de.ofahrt.gooei.lwjgl.GLColor;
import de.ofahrt.gooei.lwjgl.LwjglDesktop;

public class TiltDemo implements UIController
{

public static void main(String[] args) throws Exception
{
	LwjglDesktop desktop = new LwjglDesktop(new TiltWidgetFactory(), 800, 800);
	desktop.setBackground(GLColor.BLACK);
	desktop.parseAndAdd(new TiltDemo(desktop), "de/ofahrt/gooei/tilt/demo.xml");
	desktop.show();
}

private final LwjglDesktop desktop;

public TiltDemo(LwjglDesktop desktop)
{ this.desktop = desktop; }

public void setActive(int i)
{
	((TiltStack) desktop.findWidget("menu")).setActive(i);
}

}