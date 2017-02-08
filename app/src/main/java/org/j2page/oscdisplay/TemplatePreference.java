package org.j2page.oscdisplay;


import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

public class TemplatePreference extends DialogPreference {

    private EditText textField;

    public TemplatePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.preference_template);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);
    }

    @Override
    protected void onBindDialogView(View view) {
        textField = (EditText) view.findViewById(R.id.template_text);
        textField.setText(getSharedPreferences().getString(getKey(), ""));
        super.onBindDialogView(view);
    }


    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            persistString(textField.getText().toString());
        }
    }

}
