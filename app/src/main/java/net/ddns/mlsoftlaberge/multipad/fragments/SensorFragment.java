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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.ddns.mlsoftlaberge.multipad.R;
import net.ddns.mlsoftlaberge.multipad.sensors.AudSensorView;
import net.ddns.mlsoftlaberge.multipad.sensors.GraSensorView;
import net.ddns.mlsoftlaberge.multipad.sensors.MagSensorView;
import net.ddns.mlsoftlaberge.multipad.sensors.OriSensorView;
import net.ddns.mlsoftlaberge.multipad.sensors.TemSensorView;

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

    // handle for the gps
    private LocationManager mLocationManager = null;

    // the handle to the sensors
    private SensorManager mSensorManager;

    // the handles for the widgets of the fragment
    // -------------------------------------------

    private Button mMagneticButton;
    private LinearLayout mMagneticLayout;
    private MagSensorView mMagSensorView;
    private boolean mMagneticStatus = false;

    private Button mOrientationButton;
    private LinearLayout mOrientationLayout;
    private OriSensorView mOriSensorView;
    private boolean mOrientationStatus = false;

    private Button mGravityButton;
    private LinearLayout mGravityLayout;
    private GraSensorView mGraSensorView;
    private boolean mGravityStatus = false;

    private Button mTemperatureButton;
    private LinearLayout mTemperatureLayout;
    private TemSensorView mTemSensorView;
    private boolean mTemperatureStatus = false;

    private Button mAudioButton;
    private LinearLayout mAudioLayout;
    private AudSensorView mAudSensorView;
    private boolean mAudioStatus = false;

    private TextView mLogsConsole;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sensor_fragment, container, false);

        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        // global preference from preference screen
        isChatty = sharedPref.getBoolean("pref_key_ischatty", false);
        // local preferences for the fragment
        mMagneticStatus = sharedPref.getBoolean("pref_key_magnetic_status", false);
        mOrientationStatus = sharedPref.getBoolean("pref_key_orientation_status", false);
        mGravityStatus = sharedPref.getBoolean("pref_key_gravity_status", false);
        mTemperatureStatus = sharedPref.getBoolean("pref_key_temperature_status", false);
        mAudioStatus = sharedPref.getBoolean("pref_key_audio_status", false);

        // ============== Prepare the managers for sensors widgets ==============
        // create layout params for the created views
        final LinearLayout.LayoutParams tlayoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);

        // a sensor manager to obtain sensors data
        mSensorManager = (SensorManager) getActivity().getSystemService(getActivity().SENSOR_SERVICE);

        // a gps manager to obtain gps data
        mLocationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);

        // =============== the buttons to control the sensors display ================

        // the magnetic button
        mMagneticButton = (Button) view.findViewById(R.id.magnetic_button);
        mMagneticButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // do the job
                buttonsound();
                say("Magnetic sensors");
                if (isChatty) speak("Magnetic");
                if (mMagneticStatus) {
                    mMagneticStatus = false;
                    mMagneticLayout.setVisibility(View.GONE);
                    mMagneticButton.setBackgroundResource(R.drawable.trekbutton_gray);
                    mMagSensorView.stop();
                } else {
                    mMagneticStatus = true;
                    mMagneticLayout.setVisibility(View.VISIBLE);
                    mMagneticButton.setBackgroundResource(R.drawable.trekbutton_yellow);
                    mMagSensorView.start();
                }
            }
        });
        // ============== create a sensor display and incorporate in layout ==============
        mMagneticLayout = (LinearLayout) view.findViewById(R.id.magnetic_layout);
        // my sensorview that display the sensors data
        mMagSensorView = new MagSensorView(getActivity(), mSensorManager);
        // add my sensorview to the layout 1
        mMagneticLayout.addView(mMagSensorView, tlayoutParams);


        // the orientation button
        mOrientationButton = (Button) view.findViewById(R.id.orientation_button);
        mOrientationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // do the job
                buttonsound();
                say("Orientation sensors");
                if (isChatty) speak("Orientation");
                if (mOrientationStatus) {
                    mOrientationStatus = false;
                    mOrientationLayout.setVisibility(View.GONE);
                    mOrientationButton.setBackgroundResource(R.drawable.trekbutton_gray);
                    mOriSensorView.stop();
                } else {
                    mOrientationStatus = true;
                    mOrientationLayout.setVisibility(View.VISIBLE);
                    mOrientationButton.setBackgroundResource(R.drawable.trekbutton_yellow);
                    mOriSensorView.start();
                }
            }
        });
        // ============== create a sensor display and incorporate in layout ==============
        mOrientationLayout = (LinearLayout) view.findViewById(R.id.orientation_layout);
        // my sensorview that display the sensors data
        mOriSensorView = new OriSensorView(getActivity(), mSensorManager, mLocationManager);
        mOriSensorView.setClickable(true);
        mOriSensorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googlemapactivity();
                buttonsound();
            }
        });
        // add my sensorview to the layout 1
        mOrientationLayout.addView(mOriSensorView, tlayoutParams);


        // the gravity button
        mGravityButton = (Button) view.findViewById(R.id.gravity_button);
        mGravityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // do the job
                buttonsound();
                say("Gravity sensors");
                if (isChatty) speak("Gravity");
                if (mGravityStatus) {
                    mGravityStatus = false;
                    mGravityLayout.setVisibility(View.GONE);
                    mGravityButton.setBackgroundResource(R.drawable.trekbutton_gray);
                    mGraSensorView.stop();
                } else {
                    mGravityStatus = true;
                    mGravityLayout.setVisibility(View.VISIBLE);
                    mGravityButton.setBackgroundResource(R.drawable.trekbutton_yellow);
                    mGraSensorView.start();
                }
            }
        });
        // ============== create a sensor display and incorporate in layout ==============
        mGravityLayout = (LinearLayout) view.findViewById(R.id.gravity_layout);
        // my sensorview that display the sensors data
        mGraSensorView = new GraSensorView(getActivity(), mSensorManager);
        // add my sensorview to the layout 1
        mGravityLayout.addView(mGraSensorView, tlayoutParams);


        // the temperature button
        mTemperatureButton = (Button) view.findViewById(R.id.temperature_button);
        mTemperatureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // do the job
                buttonsound();
                say("Temperature sensors");
                if (isChatty) speak("Temperature");
                if (mTemperatureStatus) {
                    mTemperatureStatus = false;
                    mTemperatureLayout.setVisibility(View.GONE);
                    mTemperatureButton.setBackgroundResource(R.drawable.trekbutton_gray);
                    mTemSensorView.stop();
                } else {
                    mTemperatureStatus = true;
                    mTemperatureLayout.setVisibility(View.VISIBLE);
                    mTemperatureButton.setBackgroundResource(R.drawable.trekbutton_yellow);
                    mTemSensorView.start();
                }
            }
        });
        // ============== create a sensor display and incorporate in layout ==============
        mTemperatureLayout = (LinearLayout) view.findViewById(R.id.temperature_layout);
        // my sensorview that display the sensors data
        mTemSensorView = new TemSensorView(getActivity(), mSensorManager);
        // add my sensorview to the layout 1
        mTemperatureLayout.addView(mTemSensorView, tlayoutParams);


        // the audio button
        mAudioButton = (Button) view.findViewById(R.id.audio_button);
        mAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // do the job
                buttonsound();
                say("Audio sensors");
                if (isChatty) speak("Audio");
                if (mAudioStatus) {
                    mAudioStatus = false;
                    mAudioLayout.setVisibility(View.GONE);
                    mAudioButton.setBackgroundResource(R.drawable.trekbutton_gray);
                    mAudSensorView.stop();
                } else {
                    mAudioStatus = true;
                    mAudioLayout.setVisibility(View.VISIBLE);
                    mAudioButton.setBackgroundResource(R.drawable.trekbutton_yellow);
                    mAudSensorView.start();
                }
            }
        });
        // ============== create a sensor display and incorporate in layout ==============
        mAudioLayout = (LinearLayout) view.findViewById(R.id.audio_layout);
        // my sensorview that display the sensors data
        mAudSensorView = new AudSensorView(getActivity());
        // add my sensorview to the layout 1
        mAudioLayout.addView(mAudSensorView, tlayoutParams);


        // =============== the console logs of the application ===================
        // the console to display logs history
        mLogsConsole = (TextView) view.findViewById(R.id.logs_console);
        mLogsConsole.setHorizontallyScrolling(true);
        mLogsConsole.setVerticalScrollBarEnabled(true);
        mLogsConsole.setMovementMethod(new ScrollingMovementMethod());
        mLogsConsole.setText(mOnSensorInteractionListener.sensorLogs());

        // ================================
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
        mMagneticButton.setTypeface(face2);
        mOrientationButton.setTypeface(face2);
        mGravityButton.setTypeface(face2);
        mTemperatureButton.setTypeface(face2);
        mAudioButton.setTypeface(face2);
        // bottom logs console
        mLogsConsole.setTypeface(face1);
    }

    // =====================================================================================

    @Override
    public void onResume() {
        super.onResume();
        // settings part of the preferences
        isChatty = sharedPref.getBoolean("pref_key_ischatty", false);
        // local preferences for the fragment
        mMagneticStatus = sharedPref.getBoolean("pref_key_magnetic_status", false);
        mOrientationStatus = sharedPref.getBoolean("pref_key_orientation_status", false);
        mGravityStatus = sharedPref.getBoolean("pref_key_gravity_status", false);
        mTemperatureStatus = sharedPref.getBoolean("pref_key_temperature_status", false);
        mAudioStatus = sharedPref.getBoolean("pref_key_audio_status", false);

        // ======================================
        // select all sensors to view on start
        if (mMagneticStatus) {
            mMagneticLayout.setVisibility(View.VISIBLE);
            mMagneticButton.setBackgroundResource(R.drawable.trekbutton_yellow);
            mMagSensorView.start();
        } else {
            mMagneticLayout.setVisibility(View.GONE);
            mMagneticButton.setBackgroundResource(R.drawable.trekbutton_gray);
            mMagSensorView.stop();
        }
        if (mOrientationStatus) {
            mOrientationLayout.setVisibility(View.VISIBLE);
            mOrientationButton.setBackgroundResource(R.drawable.trekbutton_yellow);
            mOriSensorView.start();
        } else {
            mOrientationLayout.setVisibility(View.GONE);
            mOrientationButton.setBackgroundResource(R.drawable.trekbutton_gray);
            mOriSensorView.stop();
        }
        if (mGravityStatus) {
            mGravityLayout.setVisibility(View.VISIBLE);
            mGravityButton.setBackgroundResource(R.drawable.trekbutton_yellow);
            mGraSensorView.start();
        } else {
            mGravityLayout.setVisibility(View.GONE);
            mGravityButton.setBackgroundResource(R.drawable.trekbutton_gray);
            mGraSensorView.stop();
        }
        if (mTemperatureStatus) {
            mTemperatureLayout.setVisibility(View.VISIBLE);
            mTemperatureButton.setBackgroundResource(R.drawable.trekbutton_yellow);
            mTemSensorView.start();
        } else {
            mTemperatureLayout.setVisibility(View.GONE);
            mTemperatureButton.setBackgroundResource(R.drawable.trekbutton_gray);
            mTemSensorView.stop();
        }
        if (mAudioStatus) {
            mAudioLayout.setVisibility(View.VISIBLE);
            mAudioButton.setBackgroundResource(R.drawable.trekbutton_yellow);
            mAudSensorView.start();
        } else {
            mAudioLayout.setVisibility(View.GONE);
            mAudioButton.setBackgroundResource(R.drawable.trekbutton_gray);
            mAudSensorView.stop();
        }


    }

    @Override
    public void onPause() {
        // stop all sensors
        stopsensors();

        // save the current status
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("pref_key_magnetic_status", mMagneticStatus);
        editor.putBoolean("pref_key_orientation_status", mOrientationStatus);
        editor.putBoolean("pref_key_gravity_status", mGravityStatus);
        editor.putBoolean("pref_key_temperature_status", mTemperatureStatus);
        editor.putBoolean("pref_key_audio_status", mAudioStatus);
        editor.commit();

        super.onPause();
    }

    // =====================================================================================
    // start and stop all sensors at once

    public void startsensors() {
        mMagSensorView.start();
        mOriSensorView.start();
        mGraSensorView.start();
        mTemSensorView.start();
        mAudSensorView.start();
    }

    public void stopsensors() {
        mMagSensorView.stop();
        mOriSensorView.stop();
        mGraSensorView.stop();
        mTemSensorView.stop();
        mAudSensorView.stop();
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
        if (texte.contains("test")) {
            say("Understood 'Test'");
            speak("This is Fragment one");
            return (true);
        }
        return (false);
    }


    // display logs when they are changed (called by activity)
    public void logschanged(String logs) {
        mLogsConsole.setText(logs);
    }

    // =========================================================================================
    // map activity to see where we are on the map of this planet

    public void googlemapactivity() {
        float longitude = mOriSensorView.getLongitude();
        float latitude = mOriSensorView.getLatitude();

        String geopath = "geo:" + String.valueOf(latitude) + "," + String.valueOf(longitude);
        Uri geouri = Uri.parse(geopath);
        say("Open planetary mapping");
        say(geopath);
        final Intent viewIntent = new Intent(Intent.ACTION_VIEW, geouri);
        // A PackageManager instance is needed to verify that there's a default app
        // that handles ACTION_VIEW and a geo Uri.
        final PackageManager packageManager = getActivity().getPackageManager();
        // Checks for an activity that can handle this intent. Preferred in this
        // case over Intent.createChooser() as it will still let the user choose
        // a default (or use a previously set default) for geo Uris.
        if (packageManager.resolveActivity(viewIntent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
            Toast.makeText(getActivity(), "Start Mapping Application", Toast.LENGTH_SHORT).show();
            startActivity(viewIntent);
        } else {
            // If no default is found, displays a message that no activity can handle
            // the view button.
            Toast.makeText(getActivity(), "No application for mapping.", Toast.LENGTH_SHORT).show();
        }
    }


}
