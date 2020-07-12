package com.github.ros.android.voicecommands;

/** Listener interface for dialog events. */
public interface OnDialogEventListener {
    /** Called when a location is created. */
    public void onCreate(String[] params);

    /** Called when a location is deleted. */
    public void onDelete(String name);

    /** Called when a location is edited. */
    public void onEdit(String[] params);
}
