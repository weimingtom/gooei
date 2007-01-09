package de.ofahrt.gooei.demo;

import gooei.UIController;
import gooei.Widget;

import java.awt.Toolkit;

import de.ofahrt.gooei.impl.TextFieldWidget;
import de.ofahrt.gooei.impl.ThinletDesktop;
import de.ofahrt.gooei.lwjgl.LwjglDesktop;

public class Calculator implements UIController
{

public static void main(String[] args) throws Exception
{
	ThinletDesktop desktop = new LwjglDesktop();
	desktop.parseAndAdd(new Calculator(), "de/ofahrt/gooei/demo/calculator.xml");
	desktop.show();
}

public Calculator()
{/*OK*/}

public void calculate(String number1, String number2, Widget result)
{
	try
	{
		int i1 = Integer.parseInt(number1);
		int i2 = Integer.parseInt(number2);
		((TextFieldWidget) result).setText(String.valueOf(i1 + i2));
	}
	catch (NumberFormatException nfe)
	{
		Toolkit.getDefaultToolkit().beep();
	}
}

}