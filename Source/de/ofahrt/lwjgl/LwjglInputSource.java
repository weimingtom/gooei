package de.ofahrt.lwjgl;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.LinkedList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import de.ofahrt.utils.input.*;

public class LwjglInputSource implements InputSource
{

private static final Keys[] keyMap = createMap();

static Keys[] createMap()
{
	try
	{
		Keys[] result = new Keys[Keyboard.KEYBOARD_SIZE];
		Field[] fs = Keyboard.class.getFields();
		for (Field f : fs)
		{
			if (f.getName().startsWith("KEY_"))
			{
				String keyName = f.getName().substring(4);
				int num = f.getInt(null);
				if ((keyName.length() == 1) && (keyName.charAt(0) >= '0') && (keyName.charAt(0) <= '9'))
					keyName = "D"+keyName;
				result[num] = Keys.valueOf(keyName);
			}
		}
		return result;
	}
	catch (Exception e)
	{ throw new RuntimeException(e); }
}

private LinkedList<InputEvent> queue = new LinkedList<InputEvent>();

private void add(InputEvent event)
{ queue.add(event); }

public void update()
{
	while (Keyboard.next())
	{
		int modifiers = 0;
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
			modifiers |= Modifiers.LEFT_SHIFT | Modifiers.SHIFT;
		if (Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
			modifiers |= Modifiers.RIGHT_SHIFT | Modifiers.SHIFT;
		
		if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
			modifiers |= Modifiers.LEFT_CTRL | Modifiers.CTRL;
		if (Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
			modifiers |= Modifiers.RIGHT_CTRL | Modifiers.CTRL;
		
		if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
			modifiers |= Modifiers.ALT;
		if (Keyboard.isKeyDown(Keyboard.KEY_RMENU))
			modifiers |= Modifiers.ALT_GR;
		
		Keys keycode = keyMap[Keyboard.getEventKey()];
//		System.out.println(Keyboard.getKeyName(Keyboard.getEventKey())+" "+keycode);
		if (Keyboard.getEventKeyState())
		{
			char c = Keyboard.getEventCharacter();
			if (keycode == Keys.BACK) c = 0;
			if (keycode == Keys.RETURN) c = 0;
			if (keycode == Keys.DELETE) c = 0;
			add(new KeyboardEvent(Keyboard.getEventNanoseconds(), modifiers, true, keycode, c));
		}
		else
			add(new KeyboardEvent(Keyboard.getEventNanoseconds(), modifiers, false, keycode, (char) 0));
	}
	
	while (Mouse.next())
	{
		long time = Mouse.getEventNanoseconds();
		int mask = 0;
		for (int i = 0; i < 3; i++)
			if (Mouse.isButtonDown(i)) mask |= Modifiers.BUTTON_1 << i;
		
		if (Mouse.getEventButton() != -1)
		{
			if (Mouse.getEventButtonState())
				add(new MouseButtonEvent(time, 0, Mouse.getEventX(), Mouse.getEventY(), Mouse.getEventButton()+1, true));
			else
				add(new MouseButtonEvent(time, 0, Mouse.getEventX(), Mouse.getEventY(), Mouse.getEventButton()+1, false));
		}
		else
		{
			add(new MouseMotionEvent(time, mask, Mouse.getEventX(), Mouse.getEventY()));
		}
		
		int dwheel = -Mouse.getEventDWheel();
		if (dwheel != 0)
			add(new MouseWheelEvent(time, mask, Mouse.getEventX(), Mouse.getEventY(), dwheel));
	}
}

public boolean hasNext()
{ return queue.size() > 0; }

public InputEvent next()
{ return queue.remove(); }

protected int translate(int keycode)
{
	switch (keycode)
	{
		case Keyboard.KEY_0 : return KeyEvent.VK_0;
		case Keyboard.KEY_1 : return KeyEvent.VK_1;
		case Keyboard.KEY_2 : return KeyEvent.VK_2;
		case Keyboard.KEY_3 : return KeyEvent.VK_3;
		case Keyboard.KEY_4 : return KeyEvent.VK_4;
		case Keyboard.KEY_5 : return KeyEvent.VK_5;
		case Keyboard.KEY_6 : return KeyEvent.VK_6;
		case Keyboard.KEY_7 : return KeyEvent.VK_7;
		case Keyboard.KEY_8 : return KeyEvent.VK_8;
		case Keyboard.KEY_9 : return KeyEvent.VK_9;
		case Keyboard.KEY_BACK : return KeyEvent.VK_BACK_SPACE;
		case Keyboard.KEY_DELETE : return KeyEvent.VK_DELETE;
		case Keyboard.KEY_END : return KeyEvent.VK_END;
		case Keyboard.KEY_HOME : return KeyEvent.VK_HOME;
		case Keyboard.KEY_LEFT : return KeyEvent.VK_LEFT;
		case Keyboard.KEY_LMENU : return KeyEvent.VK_ALT;
		case Keyboard.KEY_LSHIFT : return KeyEvent.VK_SHIFT;
		case Keyboard.KEY_RSHIFT : return KeyEvent.VK_SHIFT;
		case Keyboard.KEY_RETURN : return KeyEvent.VK_ENTER;
		case Keyboard.KEY_RIGHT : return KeyEvent.VK_RIGHT;
		case Keyboard.KEY_SPACE : return KeyEvent.VK_SPACE;
		case Keyboard.KEY_TAB : return KeyEvent.VK_TAB;
		case Keyboard.KEY_COLON : return KeyEvent.VK_COLON;
		case Keyboard.KEY_SLASH : return KeyEvent.VK_SLASH;
/*		case Keyboard.KEY_A : return KeyEvent.VK_A;
		case Keyboard.KEY_B : return KeyEvent.VK_B;
		case Keyboard.KEY_C : return KeyEvent.VK_C;
		case Keyboard.KEY_D : return KeyEvent.VK_D;
		case Keyboard.KEY_E : return KeyEvent.VK_E;
		case Keyboard.KEY_F : return KeyEvent.VK_F;
		case Keyboard.KEY_G : return KeyEvent.VK_G;
		case Keyboard.KEY_H : return KeyEvent.VK_H;
		case Keyboard.KEY_I : return KeyEvent.VK_I;
		case Keyboard.KEY_J : return KeyEvent.VK_J;
		case Keyboard.KEY_K : return KeyEvent.VK_K;
		case Keyboard.KEY_L : return KeyEvent.VK_L;
		case Keyboard.KEY_M : return KeyEvent.VK_M;
		case Keyboard.KEY_N : return KeyEvent.VK_N;
		case Keyboard.KEY_O : return KeyEvent.VK_O;
		case Keyboard.KEY_P : return KeyEvent.VK_P;
		case Keyboard.KEY_Q : return KeyEvent.VK_Q;
		case Keyboard.KEY_R : return KeyEvent.VK_R;
		case Keyboard.KEY_S : return KeyEvent.VK_S;
		case Keyboard.KEY_T : return KeyEvent.VK_T;
		case Keyboard.KEY_U : return KeyEvent.VK_U;
		case Keyboard.KEY_V : return KeyEvent.VK_V;
		case Keyboard.KEY_W : return KeyEvent.VK_W;
		case Keyboard.KEY_X : return KeyEvent.VK_X;
		case Keyboard.KEY_Y : return KeyEvent.VK_Y;
		case Keyboard.KEY_Z : return KeyEvent.VK_Z;*/
		case Keyboard.KEY_A :
		case Keyboard.KEY_B :
		case Keyboard.KEY_C :
		case Keyboard.KEY_D :
		case Keyboard.KEY_E :
		case Keyboard.KEY_F :
		case Keyboard.KEY_G :
		case Keyboard.KEY_H :
		case Keyboard.KEY_I :
		case Keyboard.KEY_J :
		case Keyboard.KEY_K :
		case Keyboard.KEY_L :
		case Keyboard.KEY_M :
		case Keyboard.KEY_N :
		case Keyboard.KEY_O :
		case Keyboard.KEY_P :
		case Keyboard.KEY_Q :
		case Keyboard.KEY_R :
		case Keyboard.KEY_S :
		case Keyboard.KEY_T :
		case Keyboard.KEY_U :
		case Keyboard.KEY_V :
		case Keyboard.KEY_W :
		case Keyboard.KEY_X :
		case Keyboard.KEY_Y :
		case Keyboard.KEY_Z : return 0;
		default :
			System.out.println("UNTRANSLATED: "+keycode+" "+Keyboard.getKeyName(keycode));
			return 0;
	}
}

protected boolean isActionKey(int keycode)
{
	switch (keycode)
	{
		case Keyboard.KEY_HOME:
		case Keyboard.KEY_END:
		case Keyboard.KEY_PRIOR: //KEY_PAGE_UP
		case Keyboard.KEY_NEXT: //KEY_PAGE_DOWN
		case Keyboard.KEY_UP:
		case Keyboard.KEY_DOWN:
		case Keyboard.KEY_LEFT:
		case Keyboard.KEY_RIGHT:
//		case Keyboard.KEY_BEGIN:
		
//		case Keyboard.KEY_KP_LEFT: 
//		case Keyboard.KEY_KP_UP: 
//		case Keyboard.KEY_KP_RIGHT: 
//		case Keyboard.KEY_KP_DOWN: 
		
		case Keyboard.KEY_F1:
		case Keyboard.KEY_F2:
		case Keyboard.KEY_F3:
		case Keyboard.KEY_F4:
		case Keyboard.KEY_F5:
		case Keyboard.KEY_F6:
		case Keyboard.KEY_F7:
		case Keyboard.KEY_F8:
		case Keyboard.KEY_F9:
		case Keyboard.KEY_F10:
		case Keyboard.KEY_F11:
		case Keyboard.KEY_F12:
		case Keyboard.KEY_F13:
		case Keyboard.KEY_F14:
		case Keyboard.KEY_F15:
//		case Keyboard.KEY_F16:
//		case Keyboard.KEY_F17:
//		case Keyboard.KEY_F18:
//		case Keyboard.KEY_F19:
//		case Keyboard.KEY_F20:
//		case Keyboard.KEY_F21:
//		case Keyboard.KEY_F22:
//		case Keyboard.KEY_F23:
//		case Keyboard.KEY_F24:
//		case Keyboard.KEY_PRINTSCREEN:
		case Keyboard.KEY_SCROLL: // SCROLL_LOCK
//		case Keyboard.KEY_CAPS_LOCK:
		case Keyboard.KEY_NUMLOCK:
		case Keyboard.KEY_PAUSE:
		case Keyboard.KEY_INSERT:
		
//		case Keyboard.KEY_FINAL:
		case Keyboard.KEY_CONVERT:
		case Keyboard.KEY_NOCONVERT:
//		case Keyboard.KEY_ACCEPT:
//		case Keyboard.KEY_MODECHANGE:
		case Keyboard.KEY_KANA:
		case Keyboard.KEY_KANJI:
//		case Keyboard.KEY_ALPHANUMERIC:
//		case Keyboard.KEY_KATAKANA:
//		case Keyboard.KEY_HIRAGANA:
//		case Keyboard.KEY_FULL_WIDTH:
//		case Keyboard.KEY_HALF_WIDTH:
//		case Keyboard.KEY_ROMAN_CHARACTERS:
//		case Keyboard.KEY_ALL_CANDIDATES:
//		case Keyboard.KEY_PREVIOUS_CANDIDATE:
//		case Keyboard.KEY_CODE_INPUT:
//		case Keyboard.KEY_JAPANESE_KATAKANA:
//		case Keyboard.KEY_JAPANESE_HIRAGANA:
//		case Keyboard.KEY_JAPANESE_ROMAN:
//		case Keyboard.KEY_KANA_LOCK:
//		case Keyboard.KEY_INPUT_METHOD_ON_OFF:
		
//		case Keyboard.KEY_AGAIN:
//		case Keyboard.KEY_UNDO:
//		case Keyboard.KEY_COPY:
//		case Keyboard.KEY_PASTE:
//		case Keyboard.KEY_CUT:
//		case Keyboard.KEY_FIND:
//		case Keyboard.KEY_PROPS:
//		case Keyboard.KEY_STOP:
		
//		case Keyboard.KEY_HELP:
//		case Keyboard.KEY_WINDOWS:
//		case Keyboard.KEY_CONTEXT_MENU:
			return true;
	}
	return false;
}

}
