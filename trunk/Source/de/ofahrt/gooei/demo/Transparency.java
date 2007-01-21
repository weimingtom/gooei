package de.ofahrt.gooei.demo;

import gooei.UIController;
import de.ofahrt.gooei.font.BitstreamVeraTriFontRegistry;
import de.ofahrt.gooei.lwjgl.LwjglDesktop;

public class Transparency implements UIController
{

public static void main(String[] args) throws Exception
{
	LwjglDesktop desktop = new LwjglDesktop(new BitstreamVeraTriFontRegistry());
	desktop.setBackground(desktop.createColor(0, 0, 0, 0));
	desktop.parseAndAdd(new Transparency(), "de/ofahrt/gooei/demo/transparency.xml");
	desktop.show();
}

public Transparency()
{/*OK*/}

}