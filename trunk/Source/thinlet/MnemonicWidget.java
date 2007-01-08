package thinlet;

import de.ofahrt.utils.input.Keys;

public interface MnemonicWidget extends Widget
{

boolean checkMnemonic(Keys keycode, int modifiers);

}
