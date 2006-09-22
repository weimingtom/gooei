package thinlet.api;

import de.ofahrt.utils.input.Keys;

public interface MnemonicWidget
{

int getMnemonic();
String getText();
boolean hasMnemonic(Keys keycode, int modifiers);

}
