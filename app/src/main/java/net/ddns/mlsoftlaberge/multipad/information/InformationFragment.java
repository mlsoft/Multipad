package net.ddns.mlsoftlaberge.multipad.information;

/*
*  By Martin Laberge (mlsoftlaberge@gmail.com), From August 2017 to Now.
*  Licence: Can be shared with anyone, for non profit, provided my name stays in the comments.
*  This is a conglomerate of examples codes found in differents public forums on internet.
*  I just used the public knowledge to fit a special way to use an android phone functions.
*/

/* Copyright 2017 Martin Laberge
*
*        Licensed under the Apache License, Version 2.0 (the "License");
*        you may not use this file except in compliance with the License.
*        You may obtain a copy of the License at
*
*        http://www.apache.org/licenses/LICENSE-2.0
*
*        Unless required by applicable law or agreed to in writing, software
*        distributed under the License is distributed on an "AS IS" BASIS,
*        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*        See the License for the specific language governing permissions and
*        limitations under the License.
*/

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.ddns.mlsoftlaberge.multipad.R;
import net.ddns.mlsoftlaberge.multipad.utils.Fetcher;

import java.util.ArrayList;

/**
 * Created by Martin Laberge on 2017-08-10.
 */
public class InformationFragment extends Fragment {


    public InformationFragment() {
    }

    // ======================================================================================
    // interface between fragment and activity to define callbacks
    public interface OnInformationInteractionListener {
        void informationButtonsound();
        void informationSay(String text);
        void informationSpeak(String text);
    }

    // the handle to the calling activity
    private OnInformationInteractionListener mOnInformationInteractionListener;

    // attach to the calling activity, to point to callbacks functions
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            // Assign callback listener which the holding activity must implement.
            mOnInformationInteractionListener = (OnInformationInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnInformationInteractionListener");
        }
    }

    // ======================================================================================

    // the preferences holder
    private SharedPreferences sharedPref;

    private boolean isChatty;

    private Fetcher mFetcher;

    // the handles for the widgets of the fragment
    // -------------------------------------------
    private TextView mFragmentTitle;

    private TextView mPackVersion;

    private TextView mPackName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.information_fragment, container, false);

        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        isChatty = sharedPref.getBoolean("pref_key_ischatty", false);

        mFetcher = new Fetcher(getActivity());

        // the title of the fragment
        mFragmentTitle = (TextView) view.findViewById(R.id.fragment_title);

        // the version of package
        mPackVersion = (TextView) view.findViewById(R.id.pack_version);
        mPackName = (TextView) view.findViewById(R.id.pack_name);
        try {
            PackageManager pm = getActivity().getPackageManager();
            PackageInfo pi;
            pi = pm.getPackageInfo(getActivity().getPackageName(), 0);
            mPackVersion.setText(pi.versionName);
            mPackName.setText(pi.packageName);
        } catch (Exception e) {
            Log.e("CustomExceptionHandler", "Error", e);
        }



        // return the view just initialized
        return view;

    }

    // setup the fonts on every text-containing widgets
    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        Typeface face1 = Typeface.createFromAsset(getActivity().getAssets(), "sonysketchef.ttf");
        Typeface face2 = Typeface.createFromAsset(getActivity().getAssets(), "finalold.ttf");
        Typeface face3 = Typeface.createFromAsset(getActivity().getAssets(), "finalnew.ttf");
        // top buttons
        mFragmentTitle.setTypeface(face3);
    }

    // =====================================================================================

    @Override
    public void onResume() {
        super.onResume();
        // settings part of the preferences
        isChatty = sharedPref.getBoolean("pref_key_ischatty", false);
    }

    @Override
    public void onPause() {
        // save the current status
        //SharedPreferences.Editor editor = sharedPref.edit();
        //editor.putBoolean("pref_key_ischatty", isChatty);
        //editor.commit();
        super.onPause();
    }

    // =====================================================================================

    // beep
    private void buttonsound() {
        //MediaPlayer mediaPlayer = MediaPlayer.create(getActivity().getBaseContext(), R.raw.keyok2);
        //mediaPlayer.start(); // no need to call prepare(); create() does that for you
        mOnInformationInteractionListener.informationButtonsound();
    }

    // display the message in status bar
    private void say(String text) {
        mOnInformationInteractionListener.informationSay(text);
    }

    // speak out loud the text specified
    private void speak(String text) {
        mOnInformationInteractionListener.informationSpeak(text);
    }

}
