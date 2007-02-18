package de.ofahrt.gooei.tilt;

import gooei.UIController;
import de.ofahrt.gooei.font.bmp.BmpFont;
import de.ofahrt.gooei.lwjgl.GLColor;
import de.ofahrt.gooei.lwjgl.LwjglDesktop;
import de.ofahrt.gooei.menu.MenuWidgetFactory;
import de.ofahrt.gooei.menu.Stack;

public class TiltDemo implements UIController
{

public static void main(String[] args) throws Exception
{
	LwjglDesktop desktop = new LwjglDesktop(new MenuWidgetFactory(), 800, 800);
	desktop.addFont("default", BmpFont.load("de/ofahrt/fonts/kevsdemo/demo.fnt"));
	desktop.setBackground(GLColor.BLACK);
	desktop.parseAndAdd(new TiltDemo(desktop), "de/ofahrt/gooei/tilt/demo.xml");
	desktop.show();
}

private final LwjglDesktop desktop;

public TiltDemo(LwjglDesktop desktop)
{ this.desktop = desktop; }

public void setActive(int i)
{
	((Stack) desktop.findWidget("menu")).setActive(i);
}

}