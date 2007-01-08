// arch-tag: 100cee85-4d02-486e-96c9-b96399f125c2
package de.ofahrt.gooei.font.ttf;

import java.nio.ByteOrder;

/**
 * A buffer for byte data. Kind of like <code>DataInput</code> crossed with
 * <code>ByteArrayInputStream</code> and <code>java.nio.ByteOrder</code>.
 * 
 * @author Ulf Ochsenfahrt
 */
public class Buffer
{

public static final ByteOrder BIG_ENDIAN = ByteOrder.BIG_ENDIAN;
public static final ByteOrder LITTLE_ENDIAN = ByteOrder.LITTLE_ENDIAN;

protected ByteOrder order = BIG_ENDIAN;
protected byte[] data;
protected int position = 0;

public Buffer(byte[] data, ByteOrder order)
{
	this.data = data;
	this.order = order;
}

public Buffer(byte[] data)
{ this(data, BIG_ENDIAN); }

public int length()
{ return data.length; }

public void seek(int pos)
{ position = pos; }

public byte[] toByteArray()
{
	byte[] temp = new byte[data.length];
	System.arraycopy(data, 0, temp, 0, data.length);
	return temp;
}

public void setByteOrder(ByteOrder order)
{ this.order = order; }

public ByteOrder getByteOrder()
{ return order; }

private int get(int pos)
{ return data[pos] & 0xff; }

public boolean readBoolean(int pos)
{ return get(pos) != 0; }

public byte readByte(int pos)
{ return (byte) get(pos); }

public double readDouble(int pos)
{ return Double.longBitsToDouble(readLong(pos)); }

public float readFloat(int pos)
{ return Float.intBitsToFloat(readInt(pos)); }

public short readShort(int pos)
{ return (short) readUnsignedShort(pos); }

public int readInt(int pos)
{ return (int) readUnsignedInt(pos); }

public long readLong(int pos)
{
	long a = readUnsignedInt(pos);
	long b = readUnsignedInt(pos+4);
	if (order == BIG_ENDIAN)
		return (a << 32) | b;
	else
		return (b << 32) | a;
}

public int readUnsignedByte(int pos)
{ return get(pos); }

public int readUnsignedShort(int pos)
{
	int a = get(pos);
	int b = get(pos+1);
	if (order == BIG_ENDIAN)
		return (a << 8) | b;
	else
		return (b << 8) | a;
}

public long readUnsignedInt(int pos)
{
	int a = get(pos);
	int b = get(pos+1);
	int c = get(pos+2);
	int d = get(pos+3);
	if (order == BIG_ENDIAN)
		return (a << 24) | (b << 16) | (c << 8) | d;
	else
		return (d << 24) | (c << 16) | (b << 8) | a;
}

public boolean readBoolean()
{ return readBoolean(position++); }

public byte readByte()
{ return readByte(position++); }

public double readDouble()
{ return Double.longBitsToDouble(readLong()); }

public float readFloat()
{ return Float.intBitsToFloat(readInt()); }

public int readInt()
{ return (int) readUnsignedInt(); }

public long readLong()
{
	long result = readLong(position);
	position += 8;
	return result;
}

public short readShort()
{ return (short) readUnsignedShort(); }

public int readUnsignedByte()
{
	int result = readUnsignedByte(position);
	position += 1;
	return result;
}

public long readUnsignedInt()
{
	long result = readUnsignedInt(position);
	position += 4;
	return result;
}

public int readUnsignedShort()
{
	int result = readUnsignedShort(position);
	position += 2;
	return result;
}

}
