package com.dmplayer.utility;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.dmplayer.R;


public class ColorChooserDialog extends DialogFragment implements View.OnClickListener {
    private   CardView cardView1, cardView2, cardView3, cardView4, cardView5, cardView6, cardView7, cardView8, cardView9, cardView10;
    private    Button buttonDisagree, buttonAgree;
    private View view;
    private  int currentTheme;
    private  SharedPreferences sharedPreferences;
    private  SharedPreferences.Editor editor;
    private ActivityOptions options;
    private  Boolean themeChanged = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Save current theme to use when user press dismiss inside dialog
        sharedPreferences = getActivity().getSharedPreferences("VALUES", Context.MODE_PRIVATE);
        currentTheme = sharedPreferences.getInt("THEME", 0);

        //inflate theme_dialog.xml
        view = inflater.inflate(R.layout.theme_dialog, container);

        // remove title (already defined in theme_dialog.xml)
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Declare buttons and onClick methods
        dialogButtons();

        return view;
    }

    private void dialogButtons() {
        cardView1 = (CardView) view.findViewById(R.id.card_view1);
        cardView2 = (CardView) view.findViewById(R.id.card_view2);
        cardView3 = (CardView) view.findViewById(R.id.card_view3);
        cardView4 = (CardView) view.findViewById(R.id.card_view4);
        cardView5 = (CardView) view.findViewById(R.id.card_view5);
        cardView6 = (CardView) view.findViewById(R.id.card_view6);
        cardView7 = (CardView) view.findViewById(R.id.card_view7);
        cardView8 = (CardView) view.findViewById(R.id.card_view8);
        cardView9 = (CardView) view.findViewById(R.id.card_view9);
        cardView10 = (CardView) view.findViewById(R.id.card_view10);
        buttonDisagree = (Button) view.findViewById(R.id.buttonDisagree);
        buttonAgree = (Button) view.findViewById(R.id.buttonAgree);

        cardView1.setOnClickListener(this);
        cardView2.setOnClickListener(this);
        cardView3.setOnClickListener(this);
        cardView4.setOnClickListener(this);
        cardView5.setOnClickListener(this);
        cardView6.setOnClickListener(this);
        cardView7.setOnClickListener(this);
        cardView8.setOnClickListener(this);
        cardView9.setOnClickListener(this);
        cardView10.setOnClickListener(this);
        buttonDisagree.setOnClickListener(this);
        buttonAgree.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.card_view1:
                sharedPreferences.edit().putBoolean("THEMECHANGED", true).apply();
                if (onItemChoose != null)
                    onItemChoose.onClick(1);
                break;
            case R.id.card_view2:
                sharedPreferences.edit().putBoolean("THEMECHANGED", true).apply();
                if (onItemChoose != null)
                    onItemChoose.onClick(2);
                break;
            case R.id.card_view3:
                sharedPreferences.edit().putBoolean("THEMECHANGED", true).apply();
                if (onItemChoose != null)
                    onItemChoose.onClick(3);
                break;
            case R.id.card_view4:
                sharedPreferences.edit().putBoolean("THEMECHANGED", true).apply();
                if (onItemChoose != null)
                    onItemChoose.onClick(4);
                break;
            case R.id.card_view5:
                sharedPreferences.edit().putBoolean("THEMECHANGED", true).apply();
                if (onItemChoose != null)
                    onItemChoose.onClick(5);
                break;
            case R.id.card_view6:
                sharedPreferences.edit().putBoolean("THEMECHANGED", true).apply();
                if (onItemChoose != null)
                    onItemChoose.onClick(6);
                break;
            case R.id.card_view7:
                sharedPreferences.edit().putBoolean("THEMECHANGED", true).apply();
                if (onItemChoose != null)
                    onItemChoose.onClick(7);
                break;
            case R.id.card_view8:
                sharedPreferences.edit().putBoolean("THEMECHANGED", true).apply();
                if (onItemChoose != null)
                    onItemChoose.onClick(8);
                break;
            case R.id.card_view9:
                sharedPreferences.edit().putBoolean("THEMECHANGED", true).apply();
                if (onItemChoose != null)
                    onItemChoose.onClick(9);
                break;
            case R.id.card_view10:
                sharedPreferences.edit().putBoolean("THEMECHANGED", true).apply();
                if (onItemChoose != null)
                    onItemChoose.onClick(10);
                break;
            case R.id.buttonDisagree:
                sharedPreferences.edit().putBoolean("THEMECHANGED", false).apply();
                if (onItemChoose != null)
                    onItemChoose.onClick(currentTheme);
                getDialog().dismiss();
                break;
            case R.id.buttonAgree:
                sharedPreferences.edit().putBoolean("THEMECHANGED", true).apply();
                getDialog().dismiss();
                if (onItemChoose != null)
                    onItemChoose.onSaveChange();
                break;
        }
    }


    public OnItemChoose getOnItemChoose() {
        return onItemChoose;
    }

    public void setOnItemChoose(OnItemChoose onItemChoose) {
        this.onItemChoose = onItemChoose;
    }

    public OnItemChoose onItemChoose;

    public interface OnItemChoose {
        public void onClick(int position);
        public void onSaveChange();
    }
}