package dev.omar.plugin.iconsrepo.ui.fragments;

import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import com.itsaky.androidide.plugins.base.PluginFragmentHelper;
import com.itsaky.androidide.plugins.extensions.IModule;
import com.itsaky.androidide.plugins.extensions.IProject;
import com.itsaky.androidide.plugins.extensions.ModuleType;
import dev.omar.plugin.iconsrepo.Main;
import dev.omar.plugin.iconsrepo.R;
import dev.omar.plugin.iconsrepo.data.validation.IconNameRule;
import dev.omar.plugin.iconsrepo.data.validation.Validator;
import dev.omar.plugin.iconsrepo.models.IconModel;
import dev.omar.plugin.iconsrepo.repository.IconRepository;
import dev.omar.plugin.iconsrepo.ui.adapter.IconsAdapter;

import dev.omar.plugin.iconsrepo.utils.ImageUtils;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;

public class MainFragment extends PluginFragment {

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    @MainThread
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final RecyclerView recyclerView = setupRecyclerView(view);

        final ProgressBar progressBar = view.findViewById(R.id.loadingProgressBar);
        final MaterialTextView txt = view.findViewById(R.id.emptyStateText);
        final TextInputEditText edittext = view.findViewById(R.id.searchEditText);
        final TextInputLayout edittextLayout = view.findViewById(R.id.searchInputLayout);
        final IconsAdapter adapter = new IconsAdapter();
        adapter.setOnItemClickListener(
                (model, itemView, position) -> {
                    showImportIconDialog(model);
                });

        recyclerView.setAdapter(adapter);

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        edittext.setEnabled(false);

        Main.getInstance()
                .getIconRepository()
                .loadAllIconsAsync(
                        new IconRepository.LoadIconsCallback() {

                            @Override
                            public void onLoadded(List<IconModel> list) {
                                edittext.setEnabled(true);
                                progressBar.setVisibility(View.GONE);
                                if (list != null && list.isEmpty()) {
                                    txt.setVisibility(View.VISIBLE);
                                } else {
                                    recyclerView.setVisibility(View.VISIBLE);
                                    adapter.setOriginalList(list);
                                    edittextLayout.setHint("Search in " + list.size() + " icon");
                                }
                            }

                            @Override
                            public void onError(Throwable th) {
                                progressBar.setVisibility(View.GONE);
                                txt.setVisibility(View.VISIBLE);
                                txt.setText("Error: " + th.getMessage());
                            }
                        });

        edittext.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence arg0, int arg1, int arg2, int arg3) {}

                    @Override
                    public void onTextChanged(CharSequence text, int arg1, int arg2, int arg3) {}

                    @Override
                    public void afterTextChanged(Editable editable) {
                        adapter.search(editable.toString());
                    }
                });
    }

    private RecyclerView setupRecyclerView(View view) {
        final RecyclerView recyclerView = view.findViewById(R.id.iconsRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);

        final RecyclerView.RecycledViewPool pool = new RecyclerView.RecycledViewPool();
        pool.setMaxRecycledViews(0, 30);
        recyclerView.setRecycledViewPool(pool);

        return recyclerView;
    }

    private void showImportIconDialog(final IconModel model) {

        final View root = getLayoutInflater().inflate(R.layout.dialog_import_icon, null);
        final TextInputEditText inputName = root.findViewById(R.id.inputName);
        final TextInputEditText inputPath = root.findViewById(R.id.inputPath);

        inputName.setText("ic_" + model.getIconName().replaceAll("-", "_"));

        inputName.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence text, int arg1, int arg2, int arg3) {
                        Validator.validate(inputName,Arrays.asList(new IconNameRule()));
                    }

                    @Override
                    public void beforeTextChanged(
                            CharSequence arg0, int arg1, int arg2, int arg3) {}

                    @Override
                    public void afterTextChanged(Editable arg0) {}
                });

        final MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(getContext());
        dialog.setIcon(new PictureDrawable(model.getSvgIcon().renderToPicture()));
        dialog.setTitle("Import vector icon");
        dialog.setView(root);
        dialog.setPositiveButton("Import", null);
        dialog.setNegativeButton("Cancel", null);
        final AlertDialog alertDialog = dialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            boolean isValidName = Validator.validate(inputName,Arrays.asList(new IconNameRule()));
            if(!isValidName){
                
            }else{
                alertDialog.dismiss();
            }
            
        });
    }
}
