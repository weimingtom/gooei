package gooei.input;

public final class MouseButtonEvent extends MouseEvent
{

public static final int NOBUTTON = 0;
public static final int BUTTON1 = 1;
public static final int BUTTON2 = 2;
public static final int BUTTON3 = 3;

private static InputEventType determineType(boolean down)
{ return down ? InputEventType.MOUSE_PRESSED : InputEventType.MOUSE_RELEASED; }

private final int numButton;

public MouseButtonEvent(long time, int modifiers, int x, int y, int numButton, boolean down)
{
	super(determineType(down), time, modifiers, x, y);
	this.numButton = numButton;
}

public final int getButton()
{ return numButton; }

@Override
public boolean isPopupTrigger()
{ return (getType() == InputEventType.MOUSE_PRESSED) && (numButton == BUTTON2); }

@Override
public int getClickCount()
{ return 0; }

@Override
public String toString()
{ return type+" "+getX()+" "+getY()+" "+getButton(); }

}
