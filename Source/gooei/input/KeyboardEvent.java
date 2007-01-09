package gooei.input;

public final class KeyboardEvent extends InputEvent
{

private static InputEventType determineType(boolean down)
{ return down ? InputEventType.KEY_DOWN : InputEventType.KEY_UP; }

private final Keys keyCode;
private final char keyChar;

public KeyboardEvent(long time, int modifiers, boolean down, Keys keyCode, char keyChar)
{
	super(determineType(down), time, modifiers);
	this.keyCode = keyCode;
	this.keyChar = keyChar;
}

public final Keys getKeyCode()
{ return keyCode; }

public final char getKeyChar()
{
	if (type != InputEventType.KEY_DOWN)
		return (char) 0;
//		throw new IllegalStateException("Key char is only valid for KEY_DOWN events!");
	return keyChar;
}

@Override
public String toString()
{ return type+" "+keyCode+" "+modifiers; }

}
