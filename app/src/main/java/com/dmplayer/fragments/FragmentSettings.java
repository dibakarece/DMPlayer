/*
 * This is the source code of DMPLayer for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry, 2015.
 */
package com.dmplayer.fragments;

import com.dmplayer.R;
import com.dmplayer.activities.DMPlayerBaseActivity;
import com.dmplayer.utility.ColorChooserDialog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class FragmentSettings extends Fragment implements View.OnClickListener {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public FragmentSettings() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_settings, null);
        setupInitialViews(rootview);
        return rootview;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private void setupInitialViews(View rootview) {
        sharedPreferences = getActivity().getSharedPreferences("VALUES", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        ((RelativeLayout) rootview.findViewById(R.id.relativeLayoutChooseTheme)).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.relativeLayoutChooseTheme:
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                ColorChooserDialog dialog = new ColorChooserDialog();
                dialog.setOnItemChoose(new ColorChooserDialog.OnItemChoose() {
                    @Override
                    public void onClick(int position) {
                        setThemeFragment(position);
                    }

                    @Override
                    public void onSaveChange() {
                        startActivity(new Intent(getActivity(), DMPlayerBaseActivity.class));
                        getActivity().finish();
                        getActivity().overridePendingTransition(0, 0);
                    }
                });
                dialog.show(fragmentManager, "fragment_color_chooser");
                break;
        }
    }

    public void setThemeFragment(int theme) {
        switch (theme) {
            case 1:
                editor = sharedPreferences.edit();
                editor.putInt("THEME", 1).apply();
                break;
            case 2:
                editor = sharedPreferences.edit();
                editor.putInt("THEME", 2).apply();
                break;
            case 3:
                editor = sharedPreferences.edit();
                editor.putInt("THEME", 3).apply();
                break;
            case 4:
                editor = sharedPreferences.edit();
                editor.putInt("THEME", 4).apply();
                break;
            case 5:
                editor = sharedPreferences.edit();
                editor.putInt("THEME", 5).apply();
                break;
            case 6:
                editor = sharedPreferences.edit();
                editor.putInt("THEME", 6).apply();
                break;
            case 7:
                editor = sharedPreferences.edit();
                editor.putInt("THEME", 7).apply();
                break;
            case 8:
                editor = sharedPreferences.edit();
                editor.putInt("THEME", 8).apply();
                break;
            case 9:
                editor = sharedPreferences.edit();
                editor.putInt("THEME", 9).apply();
                break;
            case 10:
                editor = sharedPreferences.edit();
                editor.putInt("THEME", 10).apply();
                break;
        }
    }
}
