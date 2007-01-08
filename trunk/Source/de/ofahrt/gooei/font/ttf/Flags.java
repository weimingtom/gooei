package de.ofahrt.gooei.font.ttf;

class Flags
{

public static final Flags ON_POINT = new Flags(0x001);
public static final Flags X_SHORT  = new Flags(0x002);
public static final Flags Y_SHORT  = new Flags(0x004);
public static final Flags REPEAT   = new Flags(0x008);
public static final Flags X        = new Flags(0x010);
public static final Flags Y        = new Flags(0x020);

private int bit;

private Flags(int bit)
{ this.bit = bit; }

public int getBit()
{ return bit; }

public boolean isSet(int value)
{ return (value & bit) != 0; }

public int clear(int value)
{ return value & ~bit; }

}