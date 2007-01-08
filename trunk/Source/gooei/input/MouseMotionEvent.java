package gooei.input;


public final class MouseMotionEvent extends MouseEvent
{

private static InputEventType determineType(int modifiers)
{
	if ((modifiers & Modifiers.ANY_BUTTON_MASK) == 0)
		return InputEventType.MOUSE_MOVED;
	return InputEventType.MOUSE_DRAGGED;
}

public MouseMotionEvent(long time, int modifiers, int x, int y)
{
	super(determineType(modifiers), time, modifiers, x, y);
}

@Override
public String toString()
{ return getType()+" "+modifiers+" "+x+" "+y; }

}
