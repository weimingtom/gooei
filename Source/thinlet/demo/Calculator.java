package thinlet.demo;

import java.awt.Toolkit;

import thinlet.UIController;
import thinlet.Widget;
import thinlet.impl.TextFieldWidget;
import thinlet.impl.ThinletDesktop;
import thinlet.lwjgl.LwjglThinletDesktop;

public class Calculator implements UIController
{

public static void main(String[] args) throws Exception
{
	ThinletDesktop desktop = new LwjglThinletDesktop();
	desktop.parseAndAdd(new Calculator(), "thinlet/demo/calculator.xml");
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