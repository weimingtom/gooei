package de.ofahrt.utils.input;

public final class Modifiers
{

public static final int ALT    = 0x0001;
public static final int ALT_GR = 0x0002;
public static final int CTRL   = 0x0004;
public static final int SHIFT  = 0x0008;
public static final int META   = 0x0010;

public static final int LEFT_SHIFT  = 0x0020;
public static final int RIGHT_SHIFT = 0x0040;
public static final int LEFT_CTRL   = 0x0080;
public static final int RIGHT_CTRL  = 0x0100;

public static final int BUTTON_1 = 0x1000;
public static final int BUTTON_2 = 0x2000;
public static final int BUTTON_3 = 0x4000;
public static final int BUTTON_4 = 0x8000;

public static final int ANY_BUTTON_MASK = 0xf000;

public static boolean isDown(int modifiers, int mask)
{ return (modifiers & mask) == mask; }

public static boolean isUp(int modifiers, int mask)
{ return (modifiers & mask) == 0; }

}
