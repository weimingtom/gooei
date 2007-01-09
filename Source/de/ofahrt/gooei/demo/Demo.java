package de.ofahrt.gooei.demo;

import gooei.UIController;
import gooei.Widget;
import gooei.utils.SelectionType;

import java.awt.Color;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import de.ofahrt.gooei.impl.*;
import de.ofahrt.gooei.lwjgl.LwjglDesktop;

/**
 * Simple demonstration of widgets and events
 */
public class Demo implements UIController
{

public static void main(String[] args) throws Exception
{
	ThinletDesktop desktop = new LwjglDesktop();
	desktop.parseAndAdd(new Demo(desktop), "de/ofahrt/gooei/demo/demo.xml");
	desktop.show();
}

private final ThinletDesktop desktop;

public Demo(ThinletDesktop desktop)
{ this.desktop = desktop; }

/**
 * Called if the demo.xml was loaded,
 * it fills the textarea from a resource file
 */
public void loadText(TextAreaWidget textarea) throws Exception
{
	BufferedReader reader = new BufferedReader(new InputStreamReader(
		getClass().getResourceAsStream("demodialog.xml")));
	StringBuffer text = new StringBuffer();
	for (int c = reader.read(); c != -1; c = reader.read())
	{
		if (((c > 0x1f) && (c < 0x7f)) ||
			((c > 0x9f) && (c < 0xffff)) || (c == '\n'))
		{
			text.append((char) c);
		}
		else if (c == '\t')
		{
			text.append("  ");
		}
	}
	reader.close();
	textarea.setText(text.toString());
}

/**
 * Updates textarea's editable property depending on a checkbox state
 */
public void changeEditable(boolean editable, Widget w)
{
	TextAreaWidget textarea = (TextAreaWidget) w;
	textarea.setEditable(editable);
}

/**
 * Updates textarea's enabled property
 */
public void changeEnabled(boolean enabled, TextAreaWidget textarea)
{ textarea.setEnabled(enabled); }

DialogWidget dialog;

/**
 * Shows the modal find dialog, creates only one dialog instance
 */
public void showDialog() throws Exception
{
	if (dialog == null)
		dialog = (DialogWidget) desktop.parse(this, "thinlet/demo/demodialog.xml");
	desktop.addChild(dialog, 0);
}

/**
 * Updates the textarea's selection range,
 * and add the search string to the history 
 */
public void findText(Widget w, String what, boolean match, boolean down)
{
	ComboBoxWidget combobox = (ComboBoxWidget) w;
	closeDialog();
	if (what.length() == 0) { return; }
	
	boolean cacheditem = false;
	for (int i = combobox.getElementCount() - 1; i >= 0; i--)
	{
		String choicetext = combobox.getChild(i).getText();
		if (what.equals(choicetext)) { cacheditem = true; break; }
	}
	if (!cacheditem)
	{
		ComboBoxItem choice = new ComboBoxItem();
		choice.setText(what);
		combobox.addChild(choice, 0);
	}

	TextAreaWidget textarea = (TextAreaWidget) desktop.findWidget("textarea");
	int end = textarea.getEnd();
	String text = textarea.getText();
	
	if (!match)
	{
		what = what.toLowerCase();
		text = text.toLowerCase();
	}

	int index = text.indexOf(what, down ? end : 0);
	if (!down && (index != -1) && (index >= end)) index = -1;
	if (index != -1)
	{
		textarea.setStart(index);
		textarea.setEnd(index + what.length());
		textarea.requestFocus();
	}
	else
	{
		Toolkit.getDefaultToolkit().beep();
	}
}

/**
 * Closes the dialog
 */
public void closeDialog()
{ dialog.remove(); }

/**
 * Insert a new item into the list
 */
public void insertList(Widget w)
{
	ListWidget list = (ListWidget) w;
	ListItem item = new ListItem();
	item.setText("New item");
	item.setIcon(desktop.loadIcon("/icon/library.gif"));
	list.addChild(item, 0);
//	System.out.println("> click " + System.currentTimeMillis());
//	try { Thread.sleep(5000); } catch (InterruptedException ie) {}
}

/**
 * Removes the selected items from the list
 */
public void deleteList(ButtonWidget deleteButton, ListWidget list)
{
	for (int i = list.getElementCount() - 1; i >= 0; i--)
	{
		ListItem item = list.getChild(i);
		if (item.isSelected())
			item.remove();
	}
	deleteButton.setEnabled(false);
}

/**
 * Delete button's state depends on the list selection
 */
public void changeSelection(ListWidget list, ButtonWidget deleteButton)
{ deleteButton.setEnabled(list.getSelectedIndex() != -1); }

/**
 * Clears list selection and updates the selection model
 */
public void setSelection(ListWidget list, String selection, ButtonWidget deleteButton)
{
	for (int i = list.getElementCount() - 1; i >= 0; i--)
		list.getChild(i).setSelected(false);
	list.setSelection(SelectionType.valueOf(selection.toUpperCase()));
	deleteButton.setEnabled(false);
}

public void sliderChanged(int value, Widget w)
{
	SpinBoxWidget spinbox = (SpinBoxWidget) w;
	spinbox.setText(String.valueOf(value));
	hsbChanged();
}

public void spinboxChanged(String text, Widget w)
{
	SliderWidget slider = (SliderWidget) w;
	try
	{
		int value = Integer.parseInt(text);
		if ((value >= 0) && (value <= 255))
		{
			slider.setValue(value);
			hsbChanged();
		}
	}
	catch (NumberFormatException nfe)
	{ Toolkit.getDefaultToolkit().beep(); }
}

private SliderWidget sl_red, sl_green, sl_blue;
private TextFieldWidget tf_hue, tf_saturation, tf_brightness;
private ProgressBarWidget pb_hue, pb_saturation, pb_brightness;
private LabelWidget rgb_label;
	
public void storeWidgets(SliderWidget redSlider, SliderWidget greenSlider, SliderWidget blueSlider,
		TextFieldWidget ntf_hue, TextFieldWidget ntf_saturation, TextFieldWidget ntf_brightness,
		ProgressBarWidget npb_hue, ProgressBarWidget npb_saturation, ProgressBarWidget npb_brightness,
		LabelWidget nrgb_label)
{
	this.sl_red = redSlider;
	this.sl_green = greenSlider;
	this.sl_blue = blueSlider;
	this.tf_hue = ntf_hue;
	this.tf_saturation = ntf_saturation;
	this.tf_brightness = ntf_brightness;
	this.pb_hue = npb_hue;
	this.pb_saturation = npb_saturation;
	this.pb_brightness = npb_brightness;
	this.rgb_label = nrgb_label;
}

private void hsbChanged()
{
	int red = sl_red.getValue();
	int green = sl_green.getValue();
	int blue = sl_blue.getValue();
	
	float[] hsb = Color.RGBtoHSB(red, green, blue, null);
	tf_hue.setText(String.valueOf(hsb[0]));
	tf_saturation.setText(String.valueOf(hsb[1]));
	tf_brightness.setText(String.valueOf(hsb[2]));
	
	pb_hue.setValue((int) (100f * hsb[0]));
	pb_saturation.setValue((int) (100f * hsb[1]));
	pb_brightness.setValue((int) (100f * hsb[2]));
	
	rgb_label.setBackground(desktop.createColor(red, green, blue));
	rgb_label.setForeground(desktop.createColor(255 - red, 255 - green, 255 - blue));
}

}