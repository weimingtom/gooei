package gooei.utils;

import gooei.UIController;

import java.lang.reflect.Method;


public final class MethodInvoker
{

private final UIController controller;
private Object[] data;

public MethodInvoker(UIController controller, Object[] data)
{
	this.controller = controller;
	this.data = data;
}

public void invoke(Object part)
{
	invokeImpl(controller, data, part);
}

/*private static Method findMethod(Class c, String mname)
{
	if (c == null) return null;
	Method[] ms = c.getDeclaredMethods();
	for (Method m : ms)
	{
		if (mname.equalsIgnoreCase(m.getName()))
			return m;
	}
	return findMethod(c.getSuperclass(), mname);
}

private static Object getParameter(Widget w, String key)
{
	Method m = findMethod(w.getClass(), "get"+key);
	try
	{ return m.invoke(w); }
	catch (Exception e)
	{ throw new RuntimeException(e); }
}*/

private static Object getParameter(Object w, Method m)
{
	try
	{ return m.invoke(w); }
	catch (Exception e)
	{ throw new RuntimeException(e); }
}

static void invokeImpl(UIController controller, Object[] data, Object part)
{
	Object[] args = (data.length > 2) ? new Object[(data.length - 2) / 3] : null;
	if (args != null) for (int i = 0; i < args.length; i++)
	{
		Object target = data[2 + 3 * i];
		if ("thinlet".equals(target))
			args[i] = controller;
		else if (("constant".equals(target)))
			args[i] = data[2 + 3 * i + 1];
		else
		{
			if ("item".equals(target)) target = part;
			String parametername = (String) data[2 + 3 * i + 1];
			if (parametername == null)
				args[i] = target;
			else
			{
				Method m = (Method) data[2 + 3 * i + 2];
				args[i] = target != null ? getParameter(target, m) : null;
				if (args[i] == null) args[i] = null;
			}
		}
	}
	try
	{
		((Method) data[1]).invoke(data[0], args);
	}
	catch (Throwable e)
	{ e.printStackTrace(); }
}

}
