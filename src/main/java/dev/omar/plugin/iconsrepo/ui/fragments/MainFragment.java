package dev.omar.plugin.iconsrepo.ui.fragments;

import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.itsaky.androidide.plugins.extensions.IModule;
import com.itsaky.androidide.plugins.extensions.IProject;
import com.itsaky.androidide.plugins.extensions.ModuleType;
import com.itsaky.androidide.plugins.extensions.ProjectType;
import com.itsaky.androidide.plugins.extensions.SourceSet;
import com.itsaky.androidide.plugins.services.IdeProjectService;

import dev.omar.plugin.iconsrepo.Main;
import dev.omar.plugin.iconsrepo.R;
import dev.omar.plugin.iconsrepo.data.validation.IconNameRule;
import dev.omar.plugin.iconsrepo.data.validation.ValidationTextWatcher;
import dev.omar.plugin.iconsrepo.data.validation.Validator;
import dev.omar.plugin.iconsrepo.databinding.DialogImportIconBinding;
import dev.omar.plugin.iconsrepo.databinding.FragmentMainBinding;
import dev.omar.plugin.iconsrepo.models.IconModel;
import dev.omar.plugin.iconsrepo.repository.IconRepository;
import dev.omar.plugin.iconsrepo.ui.adapter.IconsAdapter;

import dev.omar.plugin.iconsrepo.utils.Utils;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainFragment extends PluginFragment {

    private FragmentMainBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    @MainThread
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();

        final IconsAdapter adapter = new IconsAdapter();
        adapter.setOnItemClickListener(
                (model, itemView, position) -> {
                    showImportIconDialog(model);
                });
        binding.searchEditText.addTextChangedListener(adapter);
        binding.iconsRecyclerView.setAdapter(adapter);

        binding.loadingProgressBar.setVisibility(View.VISIBLE);
        binding.iconsRecyclerView.setVisibility(View.GONE);

        binding.searchEditText.setEnabled(false);

        Main.getInstance()
                .getIconRepository()
                .loadAllIconsAsync(
                        new IconRepository.LoadIconsCallback() {

                            @Override
                            public void onLoadded(List<IconModel> list) {
                                binding.searchEditText.setEnabled(true);
                                binding.loadingProgressBar.setVisibility(View.GONE);
                                if (list != null && list.isEmpty()) {
                                    binding.emptyStateText.setVisibility(View.VISIBLE);
                                } else {
                                    binding.iconsRecyclerView.setVisibility(View.VISIBLE);
                                    adapter.setOriginalList(list);
                                    binding.searchInputLayout.setHint(
                                            "Search in " + list.size() + " icon");
                                }
                            }

                            @Override
                            public void onError(Throwable th) {
                                binding.loadingProgressBar.setVisibility(View.GONE);
                                binding.emptyStateText.setVisibility(View.VISIBLE);
                                binding.emptyStateText.setText("Error: " + th.getMessage());
                            }
                        });
    }

    private void setupRecyclerView() {
        final RecyclerView recyclerView = binding.iconsRecyclerView;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);

        final RecyclerView.RecycledViewPool pool = new RecyclerView.RecycledViewPool();
        pool.setMaxRecycledViews(0, 30);
        recyclerView.setRecycledViewPool(pool);
    }

    private void showImportIconDialog(final IconModel model) {
        
        
        final DialogImportIconBinding dialogBinding = DialogImportIconBinding.inflate(getLayoutInflater());
        
        
        final IdeProjectService projectService = Main.getInstance().getProjectService();

        dialogBinding.inputName.setText("ic_" + model.getIconName().replaceAll("-", "_"));
        IProject currentProject = projectService.getCurrentProject();

        dialogBinding.inputPath.setText(getDrawablePath(currentProject));

        dialogBinding.inputPath.setSingleLine(false);
        dialogBinding.inputPath.setMaxLines(3);

        dialogBinding.inputName.addTextChangedListener(
                new ValidationTextWatcher(dialogBinding.inputName, Arrays.asList(new IconNameRule())));
        dialogBinding.btnPastePath.setOnClickListener(v->{
            dialogBinding.inputPath.setText(Utils.getClipboardText(getContext()));
        });
        dialogBinding.inputNameLayout.setEndIconActivated(true);
        dialogBinding.inputNameLayout.setEndIconVisible(true);
        dialogBinding.inputNameLayout.setEndIconOnClickListener(v->{});

        final MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(getContext());
        dialog.setIcon(new PictureDrawable(model.getSvgIcon().renderToPicture()));
        dialog.setTitle("Import vector icon");
        dialog.setView(dialogBinding.getRoot());
        dialog.setPositiveButton("Import", null);
        dialog.setNegativeButton("Cancel", null);
        final AlertDialog alertDialog = dialog.show();
        alertDialog
                .getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(
                        v -> {
                            boolean isValidName =
                                    Validator.validate(
                                            dialogBinding.inputName, Arrays.asList(new IconNameRule()));

                            if (isValidName) {
                                alertDialog.dismiss();
                            }
                        });
    }

    private String getDrawablePath(IProject project) {
        return new File(getResDir(project), "drawable").getAbsolutePath();
    }

    private IModule getAppModule(IProject project) {
        if (project.getType() == ProjectType.GRADLE_PLUGIN) {
            return new IModule() {
                @Override
                public String getName() {
                    return project.getName();
                }

                @Override
                public ModuleType getType() {
                    return ModuleType.ANDROID_APP;
                }

                @Override
                public File getProjectDir() {
                    return project.getRootDir();
                }

                @Override
                public List<SourceSet> getSourceSets() {
                    File main = new File(getProjectDir(), "src/main");
                    return Collections.singletonList(
                            new SourceSet(
                                    main.getName(),
                                    Arrays.asList(new File(main, "java")),
                                    Arrays.asList(new File(main, "res"))));
                }
            };
        } else {
            for (IModule module : project.getModules()) {
                if (module.getName().equals("app")) {
                    return module;
                }
            }
        }

        return null;
    }

    private SourceSet getMainSourceSet(IModule module) {
        for (SourceSet source : module.getSourceSets()) {
            if (source.getName().equals("main")) {
                return source;
            }
        }
        return null;
    }

    private File getResDir(IProject project) {
        SourceSet source = getMainSourceSet(getAppModule(project));
        for (File file : source.getResourceDirs()) {
            if (file.getName().equals("res")) {
                return file;
            }
        }
        return null;
    }
}
