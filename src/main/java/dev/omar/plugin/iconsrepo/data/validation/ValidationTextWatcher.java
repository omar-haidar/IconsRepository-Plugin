package dev.omar.plugin.iconsrepo.data.validation;

import android.text.Editable;
import android.text.TextWatcher;
import com.google.android.material.textfield.TextInputEditText;
import dev.omar.plugin.iconsrepo.data.validation.ValidationRule;
import java.util.List;

public class ValidationTextWatcher implements TextWatcher {

    private TextInputEditText inputEditText;
    private List<ValidationRule> rules;

    public ValidationTextWatcher(TextInputEditText inputEditText, List<ValidationRule> rules) {
        this.inputEditText = inputEditText;
        this.rules = rules;
    }

    @Override
    public void beforeTextChanged(CharSequence text, int start, int end, int length) {
        
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int end, int length) {}

    @Override
    public void afterTextChanged(Editable editable) {
        Validator.validate(inputEditText, rules);
    }
}
