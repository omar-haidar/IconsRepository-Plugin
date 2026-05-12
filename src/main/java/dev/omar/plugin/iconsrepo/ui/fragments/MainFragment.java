package dev.omar.plugin.iconsrepo.ui.fragments;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.megatronking.svg.generator.svg.SvgSAXReader;
import com.github.megatronking.svg.generator.svg.model.Svg;
import com.github.megatronking.svg.generator.writer.impl.Svg2VectorTemplateWriter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.itsaky.androidide.plugins.extensions.IModule;
import com.itsaky.androidide.plugins.extensions.IProject;
import com.itsaky.androidide.plugins.extensions.ModuleType;
import com.itsaky.androidide.plugins.extensions.ProjectType;
import com.itsaky.androidide.plugins.extensions.SourceSet;
import com.itsaky.androidide.plugins.services.IdeProjectService;

import org.dom4j.DocumentException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.List;

import dev.omar.plugin.iconsrepo.Main;
import dev.omar.plugin.iconsrepo.data.validation.HexColorRule;
import dev.omar.plugin.iconsrepo.data.validation.IconNameRule;
import dev.omar.plugin.iconsrepo.data.validation.ValidationTextWatcher;
import dev.omar.plugin.iconsrepo.data.validation.Validator;
import dev.omar.plugin.iconsrepo.databinding.DialogImportIconBinding;
import dev.omar.plugin.iconsrepo.databinding.FragmentMainBinding;
import dev.omar.plugin.iconsrepo.models.IconModel;
import dev.omar.plugin.iconsrepo.repository.IconRepository;
import dev.omar.plugin.iconsrepo.ui.adapter.IconsAdapter;
import dev.omar.plugin.iconsrepo.utils.DynamicColorHelper;

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
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
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
                                    if (list != null) {
                                        binding.searchInputLayout.setHint(
                                                "Search in " + list.size() + " icon");
                                    }
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

    private void showImportIconDialog(@NonNull final IconModel model) {

        final MutableLiveData<Integer> colorPreview = new MutableLiveData<>(DynamicColorHelper.resolveDynamicColor(getContext(), DynamicColorHelper.getColorModel(com.google.android.material.R.attr.colorOnSurface)));

        final DialogImportIconBinding dialogBinding =
                DialogImportIconBinding.inflate(getLayoutInflater());
        dialogBinding.inputName.setText("ic_" + model.getIconName().replaceAll("-", "_"));
        dialogBinding.inputName.addTextChangedListener(
                new ValidationTextWatcher(
                        dialogBinding.inputName, List.of(new IconNameRule())));
        dialogBinding.inputColor.addTextChangedListener(
                new ValidationTextWatcher(
                        dialogBinding.inputColor, List.of(new HexColorRule()), (text, state) -> {
                    if (state) {
                        colorPreview.postValue(Color.parseColor(text));
                    }
                }));


        colorPreview.observeForever(value -> {
            dialogBinding.iconPreview.setColorFilter(value, PorterDuff.Mode.SRC_IN);
        });
        List<String> colorsNames = DynamicColorHelper.getDisplayNames();
        colorsNames.add(0, "None");
        colorsNames.add(1, "Custom");
        dialogBinding.txtListColors.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, colorsNames));
        dialogBinding.txtListColors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dialogBinding.inputColorLayout.setVisibility((position == 1) ? View.VISIBLE : View.GONE);
                try {
                    if (position == 0) {
                        colorPreview.postValue(Color.BLACK);
                    } else if (position == 1) {
                        colorPreview.postValue(Color.parseColor(String.valueOf(dialogBinding.inputColor.getText())));
                    } else {
                        colorPreview.postValue(DynamicColorHelper.resolveDynamicColor(getContext(), DynamicColorHelper.getColorModel(DynamicColorHelper.getDynamicColors().get(Math.max(0, position - 1)).getAttrId())));
                    }
                } catch (Exception e) {
                    colorPreview.postValue(Color.BLACK);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final IdeProjectService projectService = Main.getInstance().getProjectService();
        final IProject currentProject = projectService.getCurrentProject();


        final MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(requireContext());
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
                                            dialogBinding.inputName,
                                            List.of(new IconNameRule()));

                            if (isValidName) {
                                try {
                                    importVectorIcon(model);
                                    alertDialog.dismiss();
                                } catch (Exception err) {
                                    Toast.makeText(getContext(), "Error : " + err.getMessage(), Toast.LENGTH_LONG)
                                            .show();
                                }
                            }
                        });
    }

    private void importVectorIcon(IconModel model) {


    }

    @NonNull
    private String getDrawablePath(IProject project) {
        return new File(getResDir(project), "drawable").getAbsolutePath();
    }

    @Nullable
    private IModule getAppModule(@NonNull IProject project) {
        if (project.getType() == ProjectType.GRADLE_PLUGIN) {
            return new IModule() {
                @NonNull
                @Override
                public String getName() {
                    return project.getName();
                }

                @NonNull
                @Override
                public ModuleType getType() {
                    return ModuleType.ANDROID_APP;
                }

                @NonNull
                @Override
                public File getProjectDir() {
                    return project.getRootDir();
                }

                @NonNull
                @Override
                public List<SourceSet> getSourceSets() {
                    File main = new File(getProjectDir(), "src/main");
                    return Collections.singletonList(
                            new SourceSet(
                                    main.getName(),
                                    List.of(new File(main, "java")),
                                    List.of(new File(main, "res"))));
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

    @Nullable
    private SourceSet getMainSourceSet(@NonNull IModule module) {
        for (SourceSet source : module.getSourceSets()) {
            if (source.getName().equals("main")) {
                return source;
            }
        }
        return null;
    }

    @Nullable
    private File getResDir(IProject project) {
        SourceSet source = getMainSourceSet(getAppModule(project));
        for (File file : source.getResourceDirs()) {
            if (file.getName().equals("res")) {
                return file;
            }
        }
        return null;
    }

    @Nullable
    public static String parseSvgToXml(
            InputStream inputStream, OutputStream outputStream, int width, int height) {
        // Check input params.
        if (inputStream == null || outputStream == null) {
            return "Invalid input params!";
        }

        SvgSAXReader reader = new SvgSAXReader();
        try {
            Svg svg = reader.read(inputStream);
            if (width > 0) {
                svg.w = width;
            }
            if (height > 0) {
                svg.h = height;
            }
            Svg2VectorTemplateWriter writer = new Svg2VectorTemplateWriter(svg);
            BufferedWriter bufferedWriter =
                    new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(bufferedWriter);
        } catch (IOException | DocumentException e) {
            return "Exception when parsing :\n" + e.getMessage();
        }
        return null;
    }
}
