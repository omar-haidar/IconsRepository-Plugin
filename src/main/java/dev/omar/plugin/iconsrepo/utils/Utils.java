package dev.omar.plugin.iconsrepo.utils;
import android.content.ClipboardManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class Utils {
    
	private Utils() {
		
	}
    @Nullable
    public static CharSequence getClipboardText(@NonNull Context context) {
    	ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if(cm.hasPrimaryClip()){
            return cm.getPrimaryClip().getItemAt(0).getText();
        }
        return null;
    }


}