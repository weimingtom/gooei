package gooei.input;

public class MouseEvent extends InputEvent
{

protected int x, y;

public MouseEvent(InputEventType type, long time, int modifiers, int x, int y)
{
	super(type, time, modifiers);
	this.x = x;
	this.y = y;
}

public int getX()
{ return x; }

public int getY()
{ return y; }

public void translate(int dx, int dy)
{
	x += dx;
	y += dy;
}

public int getClickCount()
{ return 0; }

@Override
public String toString()
{ return type+" "+x+" "+y; }

}
