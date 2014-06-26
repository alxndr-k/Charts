package com.example.piechart.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import com.example.piechart.R;
import com.larswerkman.holocolorpicker.ColorPicker;

public class ColorPickerDialogFragment extends DialogFragment implements DialogInterface.OnClickListener, ColorPicker.OnColorSelectedListener {

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

    private static final String ARG_COLOR = "ARG_COLOR";

    private int mColor;
    private OnColorSelectedListener mListener;


    public static ColorPickerDialogFragment newFragment(int color) {
        Bundle args = new Bundle();
        args.putInt(ARG_COLOR, color);

        ColorPickerDialogFragment fragment = new ColorPickerDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mColor = getArguments().getInt(ARG_COLOR);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.d_color_picker, null);
        ColorPicker picker = (ColorPicker) view.findViewById(R.id.picker);/*new ColorPicker(getActivity());*/
        picker.setOnColorSelectedListener(this);
        picker.setColor(mColor);
        picker.setOldCenterColor(mColor);
        picker.setNewCenterColor(mColor);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true)
                .setTitle(R.string.color_picker_title)
                .setView(view)
                .setPositiveButton(R.string.color_picker_positive_button, ColorPickerDialogFragment.this)
                .setNegativeButton(R.string.color_picker_negative_button, ColorPickerDialogFragment.this);
        return builder.create();
    }

    @Override
    public void onColorSelected(int color) {
        mColor = color;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mListener != null && which == DialogInterface.BUTTON_POSITIVE) {
            mListener.onColorSelected(mColor);
        }
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        mListener = listener;
    }
}
