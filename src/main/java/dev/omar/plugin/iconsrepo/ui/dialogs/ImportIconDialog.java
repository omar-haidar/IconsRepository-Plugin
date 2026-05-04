package dev.omar.plugin.iconsrepo.ui.dialogs;


import android.app.Dialog;
import android.content.Context;
import android.view.ViewGroup;
import android.view.View;
import android.view.LayoutInflater;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.dialog.MaterialDialogs;
import com.itsaky.androidide.plugins.base.PluginFragmentHelper;
import dev.omar.plugin.iconsrepo.Main;

public class ImportIconDialog extends DialogFragment {
    @Override
    public LayoutInflater onGetLayoutInflater(Bundle savedInstanceState) {
        var inflater = super.onGetLayoutInflater(savedInstanceState);
        return PluginFragmentHelper.getPluginInflater(Main.PLUGIN_ID,inflater);
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup containet, Bundle savedInstanceState) {
        
	    return super.onCreateView(inflater, containet, savedInstanceState);
	}
    
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(getContext());
        dialog.setTitle("Add icon to project");
        dialog.setPositiveButton("Add",null);
        dialog.show();
        
        return dialog.create();
    }
    
    public static void show(FragmentManager fm) {
    	new ImportIconDialog().show(fm,"");
    }
	
}