package de.ofahrt.gooei.tilt;

import gooei.xml.AbstractWidgetFactory;

public class TiltWidgetFactory extends AbstractWidgetFactory
{

public TiltWidgetFactory()
{ init(); }

private void init()
{
	add("panel", TiltPanel.class);
	add("label", TiltLabel.class);
	add("stack", TiltStack.class);
}

}
