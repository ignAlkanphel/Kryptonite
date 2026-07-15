package net.alkanphel.kryptonite.util.apoli.keybind;

import java.util.HashSet;
import java.util.Set;

public class KeyBindActivity {

    private final Set<String> pressedKeys = new HashSet<>();
    private final Set<String> freshlyPressedKeys = new HashSet<>();

    public boolean isPressed(String keyId) {
        return pressedKeys.contains(keyId);
    }

    public boolean isFreshlyPressed(String keyId) {
        return freshlyPressedKeys.contains(keyId);
    }

    public void update(Set<String> currentlyPressed) {
        for (String keyId : currentlyPressed) {
            if (!pressedKeys.contains(keyId)) {
                freshlyPressedKeys.add(keyId);
            }
        }
        pressedKeys.clear();
        pressedKeys.addAll(currentlyPressed);
    }

    public void tick() {
        freshlyPressedKeys.clear();
    }

    public void clear() {
        pressedKeys.clear();
        freshlyPressedKeys.clear();
    }

}