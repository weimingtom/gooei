package gooei.input;

public abstract class InputEvent
{

protected final InputEventType type;
protected final long time;
protected final int modifiers;

protected InputEvent(InputEventType type, long time, int modifiers)
{
	this.type = type;
	this.time = time;
	this.modifiers = modifiers;
}

public final InputEventType getType()
{ return type; }

public final long getTime()
{ return time; }

public int getModifiers()
{ return modifiers; }

public final boolean isModifierDown(int modifier)
{ return (modifiers & modifier) == modifier; }

public final boolean isModifierUp(int modifier)
{ return (modifiers & modifier) == 0; }

public boolean isPopupTrigger()
{ return false; }

}
