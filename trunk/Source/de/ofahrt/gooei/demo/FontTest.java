package de.ofahrt.gooei.demo;

import gooei.UIController;
import de.ofahrt.gooei.font.BitstreamVeraTriFontRegistry;
import de.ofahrt.gooei.lwjgl.LwjglDesktop;

public class FontTest implements UIController
{

public static void main(String[] args) throws Exception
{
	LwjglDesktop desktop = new LwjglDesktop(new BitstreamVeraTriFontRegistry());
	desktop.parseAndAdd(new FontTest(desktop), "de/ofahrt/gooei/demo/font.xml");
	desktop.show();
}

@SuppressWarnings("unused")
private final LwjglDesktop desktop;

public FontTest(LwjglDesktop desktop)
{ this.desktop = desktop; }

}