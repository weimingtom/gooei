package thinlet.api;

import de.ofahrt.utils.games.Keys;

public interface MnemonicWidget
{

int getMnemonic();
String getText();
boolean hasMnemonic(Keys keycode, int modifiers);

}
