package fr.tvbarthel.apps.simplethermometer.dialogfragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

import fr.tvbarthel.apps.simplethermometer.R;

/**
 * A simple dialog fragment used to displayed the about information
 */
public class AboutDialogFragment extends DialogFragment {

	/*
		DialogFragment override
	 */

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.about_title);
		builder.setCancelable(true);
		builder.setPositiveButton(R.string.alert_dialog_ok_button, null);
        builder.setInverseBackgroundForced(true);
		final TextView description = new TextView(getActivity());
		description.setMovementMethod(LinkMovementMethod.getInstance());
		final int paddingInPixelSize = getResources().getDimensionPixelSize(R.dimen.default_padding);
		description.setPadding(paddingInPixelSize, paddingInPixelSize, paddingInPixelSize, paddingInPixelSize);
		final SpannableString s = new SpannableString(getString(R.string.about_description));
		Linkify.addLinks(s, Linkify.WEB_URLS);
		description.setText(s);
		builder.setView(description);
		return builder.create();
	}
}
