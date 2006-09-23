package thinlet.xml;

import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import thinlet.*;
import thinlet.api.*;
import thinlet.help.Icon;
import thinlet.help.MethodInvoker;
import thinlet.help.TLColor;
import thinlet.help.TLFont;

public class SimpleXMLParser
{

	static class MethodFixup
	{
		public final Object w;
		public final String type;
		public final String key;
		public final String value;
		public final Method m;
		public MethodFixup(Object w, String type, String key, String value, Method m)
		{
			this.w = w;
			this.type = type;
			this.key = key;
			this.value = value;
			this.m = m;
		}
	}

private final ThinletDesktop desktop;
private final UIController controller;
private final WidgetFactory factory;
private final ResourceBundle bundle;
private final ArrayList<MethodFixup> methodFixups = new ArrayList<MethodFixup>();

public SimpleXMLParser(ThinletDesktop desktop, UIController container, ResourceBundle bundle)
{
	this.desktop = desktop;
	this.controller = container;
	this.factory = new WidgetFactory();
	this.bundle = bundle;
}

/**
 * Finds the first component from the root desktop by a specified name value
 *
 * @param name parameter value identifies the widget
 * @return the first suitable component, or null
 */
public Widget find(String name)
{ return desktop.findWidget(name); }

private Method findMethod(Class c, String mname)
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

/**
 * @return an object list including as follows:
 * - handler object,
 * - method,
 * - list of parameters including 3 values:
 * - ("thinlet", null, null) for the single thinlet component,
 * - (target component, null, null) for named widget as parameter, e.g. mybutton,
 * - (target, parameter name, default value) for a widget's given property, e.g. mylabel.enabled,
 * - ("item", null, null) for an item of the target component as parameter, e.g. tree node,
 * - ("item", parameter name, default value) for the item's given property e.g. list item's text,
 * - ("constant", string object, null) for constant number
 * (int, long, double, float) or string given as 'text'.
 */
private MethodInvoker parseMethod(Object thisWidget, String value, Widget root)
{
	StringTokenizer st = new StringTokenizer(value, "(, \r\n\t)");
	String methodname = st.nextToken();
	int n = st.countTokens();
	Object[] result = new Object[2 + 3 * n];
	Class[] parametertypes = (n > 0) ? new Class[n] : null;
	for (int i = 0; i < n; i++)
	{
		String arg = st.nextToken();
		if ("thinlet".equals(arg))
		{ // the target component
			result[2 + 3 * i] = "thinlet";
			parametertypes[i] = UIController.class;
		}
		else if ((arg.length() > 1) && (arg.charAt(0) == '\'') && (arg.charAt(arg.length() - 1) == '\''))
		{ // constant string value
			result[2 + 3 * i] = "constant";
			result[2 + 3 * i + 1] = new String(arg.substring(1, arg.length() - 1));
			parametertypes[i] = String.class;
		}
		else
		{
			int dot = arg.indexOf('.');
			String compname = (dot == -1) ? arg : arg.substring(0, dot);
			Object comp = null;
			Class<?> paramClass = null;
			if ("item".equals(compname))
			{
				comp = "item";
				if (thisWidget instanceof TreeWidget) paramClass = TreeNode.class;
				else if (thisWidget instanceof TableWidget) paramClass = TableWidget.class;
				else if (thisWidget instanceof ListWidget) paramClass = ListItem.class;
				else if (thisWidget instanceof ComboBoxWidget) paramClass = ComboBoxItem.class;
				else if (thisWidget instanceof TabbedPaneWidget) paramClass = TabWidget.class;
				else throw new IllegalArgumentException(thisWidget+" has no item");
			}
			else if ("this".equals(compname))
			{
				comp = thisWidget;
				paramClass = thisWidget.getClass();
			}
			else if ((comp = ThinletDesktop.findWidget(root, compname)) != null)
			{ // a widget's name
				paramClass = comp.getClass();
			}
			else
			{
				try
				{ // maybe constant number
					if (arg.regionMatches(true, arg.length() - 1, "F", 0, 1))
					{ // float
						result[2 + 3 * i + 1] = Float.valueOf(arg.substring(0, arg.length() - 1));
						parametertypes[i] = Float.TYPE;
					}
					else if (arg.regionMatches(true, arg.length() - 1, "L", 0, 1))
					{ // long
						result[2 + 3 * i + 1] = Long.valueOf(arg.substring(0, arg.length() - 1));
						parametertypes[i] = Long.TYPE;
					}
					else if (dot != -1)
					{ // double
						result[2 + 3 * i + 1] = Double.valueOf(arg);
						parametertypes[i] = Double.TYPE;
					}
					else
					{ // integer
						result[2 + 3 * i + 1] = Integer.valueOf(arg);
						parametertypes[i] = Integer.TYPE;
					}
					result[2 + 3 * i] = "constant";
					continue;
				}
				catch (NumberFormatException nfe)
				{ // widget's name not found nor constant
					throw new IllegalArgumentException("unknown " + arg);
				}
			}
			
			result[2 + 3 * i] = comp; // the target component
			if (dot == -1)
				parametertypes[i] = Widget.class;
			else
			{
				String key = arg.substring(dot+1);
				Method m = findMethod(paramClass, "get"+key);
				if (m == null) m = findMethod(paramClass, "is"+key);
				if (m == null) m = findMethod(paramClass, "has"+key);
				if (m == null) throw new IllegalArgumentException("Not found: "+key+" on "+paramClass);
				result[2 + 3 * i + 1] = key; // parameter name, e.g. enabled
				result[2 + 3 * i + 2] = m;   // method to get param from widget
				parametertypes[i] = m.getReturnType();
			}
		}
	}
	
	result[0] = controller;
	try
	{
		result[1] = findMethod(controller.getClass(), methodname); // parametertypes
		return new MethodInvoker(controller, result);
	}
	catch (Exception exc)
	{
		throw new IllegalArgumentException(exc);
	}
}

/**
 * @param methods methods and label's 'for' widgets are stored in this
 * vector because these may reference to widgets which are not parsed
 * at that time
 */
private void finishParse(Widget root)
{
	for (MethodFixup fixup : methodFixups)
	{
		Object component = fixup.w;
		String value = fixup.value;
		
		if ("method".equals(fixup.type))
		{
			MethodInvoker method = parseMethod(component, value, root);
			if ("init".equals(fixup.key))
				method.invoke(null);
			else
			{
				try
				{
					fixup.m.invoke(component, method);
				}
				catch (Exception e)
				{ throw new RuntimeException(e); }
			}
		}
		else
		{ // ("component" == definition[0])
			Widget reference = ThinletDesktop.findWidget(root, value); //+start find from the component
			if (reference == null) throw new IllegalArgumentException(value + " not found"); 
			try
			{ fixup.m.invoke(component, reference); }
			catch (Exception e)
			{ throw new RuntimeException(e); }
		}
	}
	
	// TODO: invoke all init methods
}

private Object addElement(Object parent, String name)
{
	Object child = factory.createWidget(desktop, name);
	if ((parent instanceof Widget) && (child instanceof PopupMenuElement))
		((Widget) parent).setPopupMenuWidget((PopupMenuElement) child);
	else if ((parent instanceof TableWidget) && (child instanceof TableHeader))
		((TableWidget) parent).setHeaderWidget((TableHeader) child);
	else if ((parent instanceof ContainerWidget) && (child instanceof Widget))
		((ContainerWidget) parent).addChild((Widget) child, -1);
	else if ((parent instanceof DataWidget) && (child instanceof Element))
		((DataWidget) parent).addChild((Element) child, -1);
	else if ((parent instanceof DataElement) && (child instanceof Element))
		((DataElement) parent).addChild((Element) child, -1);
	else
		throw new IllegalArgumentException("cannot add "+child+" to "+parent);
	return child;
}

private Object findEnum(Class<?> c, String value)
{
	Object[] enums = c.getEnumConstants();
	Object result = null;
	String name = value.toUpperCase();
	for (Object e : enums)
	{
		if (name.equals(((Enum) e).name()))
			result = e;
	}
	if (result == null) throw new IllegalArgumentException("no "+name+" in "+c);
	return result;
}

private void setAttribute(Object parent, Object component, String key, String value)
{
	// replace value found in the bundle
	if ((bundle != null) && value.startsWith("i18n."))
		value = bundle.getString(value.substring(5));
	
	Method m = findMethod(component.getClass(), "set"+key);
	if (m == null) m = findMethod(component.getClass(), "set"+key+"Widget");
	if (m != null)
	{
		Class[] cs = m.getParameterTypes();
		try
		{
			if (cs.length == 1)
			{
				Class<?> c = cs[0];
				if (c == int.class)
				{
					m.invoke(component, Integer.valueOf(value));
					return;
				}
				if (c == boolean.class)
				{
					m.invoke(component, Boolean.valueOf(value));
					return;
				}
				if (c == String.class)
				{
					m.invoke(component, new String(value));
					return;
				}
				if (c == Icon.class)
				{
					m.invoke(component, desktop.loadIcon(value));
					return;
				}
				if (c == TLColor.class)
				{
					TLColor color = new ColorParser(desktop).parse(value);
					m.invoke(component, color);
					return;
				}
				if (c == TLFont.class)
				{
					TLFont font = new FontParser(desktop).parse(value);
					m.invoke(component, font);
					return;
				}
				if (c.isEnum())
				{
					Object result = findEnum(c, value);
					m.invoke(component, result);
					return;
				}
				
				if (Widget.class.isAssignableFrom(c))
				{
					methodFixups.add(new MethodFixup(component, "component", key, value, m));
					return;
				}
				if (c == MethodInvoker.class)
				{
					methodFixups.add(new MethodFixup(component, "method", key, value, m));
					return;
				}
			}
		}
		catch (Exception e)
		{ throw new RuntimeException(e); }
	}
	
	if (parent != null)
	{
		m = findMethod(parent.getClass(), "set"+key);
		if (m != null)
		{
			Class[] cs = m.getParameterTypes();
			try
			{
				if ((cs.length == 2) && (Widget.class.isAssignableFrom(cs[0])))
				{
					Class<?> c = cs[1];
					if (c == int.class)
					{
						m.invoke(parent, component, Integer.valueOf(value));
						return;
					}
					if (c == boolean.class)
					{
						m.invoke(parent, component, Boolean.valueOf(value));
						return;
					}
					if (c == String.class)
					{
						m.invoke(parent, component, new String(value));
						return;
					}
					if (c == Icon.class)
					{
						m.invoke(parent, component, desktop.loadIcon(value));
						return;
					}
					if (c == TLColor.class)
					{
						TLColor color = new ColorParser(desktop).parse(value);
						m.invoke(parent, component, color);
						return;
					}
					if (c == TLFont.class)
					{
						TLFont font = new FontParser(desktop).parse(value);
						m.invoke(parent, component, font);
						return;
					}
					if (c.isEnum())
					{
						Object result = findEnum(c, value);
						m.invoke(parent, component, result);
						return;
					}
				}
			}
			catch (Exception e)
			{ throw new RuntimeException(e); }
		}
	}
	
/*	if ("property".equals(key) && (component instanceof Widget))
	{
		StringTokenizer st = new StringTokenizer(value, ";");
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			int equals = token.indexOf('=');
			if (equals == -1) throw new IllegalArgumentException(token);
			((Widget) component).putProperty(new String(token.substring(0, equals)),
				new String(token.substring(equals + 1)));
		}
		return;
	}*/
	
	throw new RuntimeException("no method found for "+key+" on "+component);
}

public Widget parse(InputStream inputstream) throws IOException
{
	Reader reader = new BufferedReader(new InputStreamReader(inputstream));
	try
	{
		Object[] parentlist = null;
		Object current = null;
		StringBuffer text = new StringBuffer();
//		String encoding = null; // encoding value of xml declaration
		for (int c = reader.read(); c != -1;)
		{
			if (c == '<')
			{
				if ((c = reader.read()) == '/')
				{ //endtag
					if ((text.length() > 0) && (text.charAt(text.length() - 1) == ' '))
					{
						text.setLength(text.length() - 1); // trim last space
					}
					if (text.length() > 0)
					{
						text.setLength(0);
					}
					String tagname = (String) parentlist[2];
					for (int i = 0; i < tagname.length(); i++)
					{ // check current tag's name
						if ((c = reader.read()) != tagname.charAt(i))
							throw new IllegalArgumentException(tagname);
					}
					while (" \t\n\r".indexOf(c = reader.read()) != -1) {/*read whitespace*/}
					if (c != '>') throw new IllegalArgumentException(); // read '>'
					if (parentlist[0] == null)
					{
						reader.close();
						finishParse((Widget) current);
						return (Widget) current;
					}
					c = reader.read();
					current = parentlist[0];
					parentlist = (Object[]) parentlist[1];
				}
				else if (c == '!')
				{ // doctype
					while ((c = reader.read()) != '>') {/*read to '>'*/}
					c = reader.read();
				}
				else
				{ // start or standalone tag
					text.setLength(0);
					boolean iscomment = false;
					while (">/ \t\n\r".indexOf(c) == -1)
					{ // to next whitespace or '/'
						text.append((char) c);
						if ((text.length() == 3) && (text.charAt(0) == '!') &&
								(text.charAt(1) == '-') && (text.charAt(2) == '-'))
						{ // comment
							int m = 0;
							while (true)
							{ // read to '-->'
								c = reader.read();
								if (c == '-') { m++; }
								else if ((c == '>') && (m >= 2)) { break; }
								else { m = 0; }
							}
							iscomment = true;
						}
						c = reader.read();
					}
					if (iscomment) continue;
					if (text.length() == 0) throw new IllegalArgumentException();
					boolean pi = (text.charAt(0) == '?'); // processing instruction
					String tagname = text.toString();
					if (!pi)
					{ // tagname is available
						parentlist = new Object[] { current, parentlist, tagname };
						current = (current != null) ? addElement(current, tagname) : factory.createWidget(desktop, tagname);
					}
					text.setLength(0);
					while (true)
					{ // read attributes
						boolean whitespace = false;
						while (" \t\n\r".indexOf(c) != -1)
						{ // read whitespaces
							c = reader.read();
							whitespace = true;
						}
						if (pi && (c == '?'))
						{ // end of processing instruction
							if ((c = reader.read()) != '>')
							{
								throw new IllegalArgumentException(); // read '>'
							}
						}
						else if (c == '>')
						{ // end of tag start
						}
						else if (c == '/')
						{ // standalone tag
							if ((c = reader.read()) != '>')
							{
								throw new IllegalArgumentException(); // read '>'
							}
							if (parentlist[0] == null)
							{
								reader.close();
								finishParse((Widget) current);
								return (Widget) current;
							}
							current = parentlist[0];
							parentlist = (Object[]) parentlist[1];
						}
						else if (whitespace)
						{
							while ("= \t\n\r".indexOf(c) == -1)
							{ // read to key's end
								text.append((char) c);
								c = reader.read();
							}
							String key = text.toString();
							text.setLength(0);
							while (" \t\n\r".indexOf(c) != -1) c = reader.read();
							if (c != '=') throw new IllegalArgumentException();
							while (" \t\n\r".indexOf(c = reader.read()) != -1) {/*Skip whitespace*/}
							char quote = (char) c;
							if ((c != '\"') && (c != '\'')) throw new IllegalArgumentException();
							while (quote != (c = reader.read()))
							{
								if (c == '&')
								{
									StringBuffer eb = new StringBuffer();
									while (';' != (c = reader.read())) { eb.append((char) c); }
									String entity = eb.toString();
									if ("lt".equals(entity)) { text.append('<'); }
									else if ("gt".equals(entity)) { text.append('>'); }
									else if ("amp".equals(entity)) { text.append('&'); }
									else if ("quot".equals(entity)) { text.append('"'); }
									else if ("apos".equals(entity)) { text.append('\''); }
									else if (entity.startsWith("#"))
									{
										boolean hexa = (entity.charAt(1) == 'x');
										text.append((char) Integer.parseInt(entity.substring(hexa ? 2 : 1), hexa ? 16 : 10));
									}
									else throw new IllegalArgumentException("unknown " + "entity " + entity);
								}
								else text.append((char) c);
							}
							if (pi)
							{
								if ("?xml".equals(tagname) && "encoding".equals(key))
								{
									try
									{
										String enc = text.toString();
										new String(new byte[0], 0, 0, enc);
//										encoding = new String(enc);
									}
									catch (UnsupportedEncodingException uee)
									{
										System.err.println(uee.getMessage());
									}
								}
							}
							else
							{ // GUI parser
								setAttribute(parentlist[0], current, key, text.toString());
							}
							//'<![CDATA[' ']]>'
							text.setLength(0);
							c = reader.read();
							continue;
						}
						else throw new IllegalArgumentException();
						c = reader.read();
						break;
					}
				}
			}
			else
			{
				if (" \t\n\r".indexOf(c) != -1)
				{
					if ((text.length() > 0) && (text.charAt(text.length() - 1) != ' '))
					{
						text.append(' ');
					}
				}
				else
				{
					text.append((char) c);
				}
				c = reader.read();
			} 
		}
		throw new IllegalArgumentException();
	}
	finally
	{
		try
		{ reader.close(); }
		catch (IOException e)
		{/*IGNORED*/}
	}
}

}
