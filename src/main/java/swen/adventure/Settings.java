package swen.adventure;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 7/10/15.
 */
public class Settings {
    public static final float MouseSensitivity = 500.f;
    public static final int EventsTillServerBackup = 4096;
    public static final boolean IntelGraphicsWorkaround = !Boolean.getBoolean("swen.adventure.NoIntelGraphics");
    public static final boolean DeferredShading = Boolean.getBoolean("swen.adventure.DeferredShading");
}
