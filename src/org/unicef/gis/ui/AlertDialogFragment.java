package org.unicef.gis.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class AlertDialogFragment extends DialogFragment {
	private int title;
	private int prompt;
	
	public void setTitle(int title) {
		this.title = title;
	}
	
	public void setPrompt(int prompt) {
		this.prompt = prompt;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(prompt)
               .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {}
               })
               .setTitle(title);
        // Create the AlertDialog object and return it
        return builder.create();
	}
}
