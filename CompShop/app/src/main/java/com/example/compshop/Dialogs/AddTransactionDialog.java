package com.example.compshop.Dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.compshop.Admin.AddCategory;
import com.example.compshop.Admin.AddItem;
import com.example.compshop.R;

public class AddTransactionDialog extends DialogFragment {

    private RelativeLayout item, category;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_transaction, null);
        item = view.findViewById(R.id.addItemRelLayout);
        category = view.findViewById(R.id.addCategoryRelLayout);

        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent x6 = new Intent(getActivity(), AddItem.class);
                startActivity(x6);
            }
        });

        category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent x2 = new Intent(getActivity(), AddCategory.class);
                startActivity(x2);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Add Transaction")
                .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setView(view);

        return builder.create();
    }
}
