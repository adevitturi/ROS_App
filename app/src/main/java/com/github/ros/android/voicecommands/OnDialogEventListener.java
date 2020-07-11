package com.github.ros.android.voicecommands;

public interface OnDialogEventListener {
    public void onCreate(String[] params);
    public void onDelete(String name);
    public void onEdit(String[] params);
}
