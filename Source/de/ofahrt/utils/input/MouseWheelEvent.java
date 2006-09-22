package de.ofahrt.utils.input;

public final class MouseWheelEvent extends MouseEvent
{

private final int dwheel;

public MouseWheelEvent(long time, int modifiers, int x, int y, int dwheel)
{
	super(InputEventType.MOUSE_WHEEL, time, modifiers, x, y);
	this.dwheel = dwheel;
}

public final int getWheelRotation()
{ return dwheel; }

}
