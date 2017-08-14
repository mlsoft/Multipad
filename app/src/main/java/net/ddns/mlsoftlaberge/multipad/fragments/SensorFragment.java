package net.ddns.mlsoftlaberge.multipad.fragments;

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
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import net.ddns.mlsoftlaberge.multipad.R;

import java.util.ArrayList;

/**
 * Created by Martin Laberge on 2017-08-10.
 */
public class SensorFragment extends Fragment {


    public SensorFragment() {
    }

    // ======================================================================================
    // interface between fragment and activity to define callbacks
    public interface OnSensorInteractionListener {
        void sensorButtonsound();
        void sensorModeChange(int mode);
        void sensorSay(String text);
        String sensorLogs();
        void sensorSpeak(String text);
        void sensorListen();
    }

    // the handle to the calling activity
    private OnSensorInteractionListener mOnSensorInteractionListener;

    // attach to the calling activity, to point to callbacks functions
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            // Assign callback listener which the holding activity must implement.
            mOnSensorInteractionListener = (OnSensorInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSensorInteractionListener");
        }
    }

    // ======================================================================================

    // the preferences holder
    private SharedPreferences sharedPref;

    private boolean isChatty;

    // the handles for the widgets of the fragment
    // -------------------------------------------
    private TextView mFragmentTitle;

    private Button mBackButton;

    private Button mListenButton;

    private TextView mLogsConsole;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sensor_fragment, container, false);

        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        isChatty = sharedPref.getBoolean("pref_key_ischatty", false);

        // the title of the fragment
        mFragmentTitle = (TextView) view.findViewById(R.id.fragment_title);

        // the back button
        mBackButton = (Button) view.findViewById(R.id.back_button);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // do the job
                buttonsound();
                say("The BACK button is pressed");
                if(isChatty) speak("back");
                modechange(0);
            }
        });

        // the listen button
        mListenButton = (Button) view.findViewById(R.id.listen_button);
        mListenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // do the job
                listen();
            }
        });

        // the console to display logs history
        mLogsConsole = (TextView) view.findViewById(R.id.logs_console);
        mLogsConsole.setHorizontallyScrolling(true);
        mLogsConsole.setVerticalScrollBarEnabled(true);
        mLogsConsole.setMovementMethod(new ScrollingMovementMethod());
        mLogsConsole.setText(mOnSensorInteractionListener.sensorLogs());

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
        mBackButton.setTypeface(face2);
        mListenButton.setTypeface(face3);
        mLogsConsole.setTypeface(face1);
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
        mOnSensorInteractionListener.sensorButtonsound();
    }

    private void modechange(int mode) {
        mOnSensorInteractionListener.sensorModeChange(mode);
    }

    // display the message in status bar
    private void say(String text) {
        mOnSensorInteractionListener.sensorSay(text);
    }

    // speak out loud the text specified
    private void speak(String text) {
        mOnSensorInteractionListener.sensorSpeak(text);
    }

    // listen to the user speaking
    private void listen() {
        mOnSensorInteractionListener.sensorListen();
    }

    // interpret the sentence pronounced by the user in the listen function (called by activity)
    public void understood(ArrayList<String> dutexte) {
        if (dutexte != null && dutexte.size() > 0) {
            for (int i = 0; i < dutexte.size(); ++i) {
                String mSentence = dutexte.get(i);
                if (matchvoice(mSentence)) {
                    // the sentence has been matched, then return without further processing
                    return;
                }
            }
            speak(dutexte.get(0));
        }
    }

    // try to match some fragment commands
    private boolean matchvoice(String textein) {
        String texte = textein.toLowerCase();
        if(texte.contains("test")) {
            say("Understood 'Test'");
            speak("This is Fragment one");
            return(true);
        }
        return(false);
    }


    // display logs when they are changed (called by activity)
    public void logschanged(String logs) {
        mLogsConsole.setText(logs);
    }

}
