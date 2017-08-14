package net.ddns.mlsoftlaberge.multipad;

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

import android.Manifest;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import net.ddns.mlsoftlaberge.multipad.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.Locale;

public class MultipadActivity extends Activity
        implements RecognitionListener ,
        MultipadFragment.OnMultipadInteractionListener {

    // =====================================================================================

    // implementation of interface from fragments
    @Override
    public void multipadButtonsound() {
        buttonsound();
    }

    @Override
    public void multipadModeChange(int mode) {
        switchfragment(mode);
    }

    @Override
    public void multipadSpeak(String text) {
        speak(text);
    }

    @Override
    public void multipadListen() {
        listen();
    }

    @Override
    public void multipadSay(String text) {
        say(text);
    }

    @Override
    public String multipadLogs() {
        return(logbuffer.toString());
    }

    // =====================================================================================

    // tag for fragment transactions
    private static String TAG = "Multipad";

    // handles to fragments displayable in the fragment block
    private MultipadFragment mMultipadFragment=null;

    // current fragment mode
    private int currentMode=0;

    // handle to shared preferences file
    private SharedPreferences sharedPref;

    // =====================================================================================

    // the button to talk to computer
    private ImageButton mTalkButton;

    // the button to switch sound on/off
    private Button mSoundButton;

    // the status of sound
    private boolean mSoundStatus = false;

    // the button to start it all
    private Button mStartButton;

    // the button to stop it all
    private Button mStopButton;

    // the status of running state started/stopped
    private boolean mRunStatus = false;

    // the button for settings
    private Button mSettingsButton;

    // the chatty mode
    private boolean isChatty = false;

    // the two status lines
    private TextView mTextstatus_top;
    private TextView mTextstatus_bottom;

    // the button to back operation
    private ImageButton mBackButton;

    // the button to Navigate First
    private Button mFirstButton;

    // the button to Navigate Precedent
    private Button mPrecButton;

    // the button to Navigate Next
    private Button mNextButton;

    // the button to Navigate Last
    private Button mLastButton;

    // --------------------------------------------

    // the sensors fragment button
    private Button mSensorButton;

    // the communication fragment button
    private Button mCommButton;


    // ==============================================================================

    // create the main screen, ask permissions, load preferences, init buttons
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // load main screen
        setContentView(R.layout.multipad_activity);

        // request all permissions to f**k with the system
        askpermissions();

        // load preferences from file
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int lastmode = sharedPref.getInt("pref_key_last_mode", 0);
        mSoundStatus = sharedPref.getBoolean("pref_key_sound_status", false);
        mRunStatus = sharedPref.getBoolean("pref_key_run_status", false);
        isChatty = sharedPref.getBoolean("pref_key_ischatty", false);

        // initialize the speak processor
        initspeak();

        // initialize widgets on screen
        // ===================== top horizontal button grid ==========================
        // the talk button
        mTalkButton = (ImageButton) findViewById(R.id.talk_button);
        mTalkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //buttonsound();
                listen();
            }
        });

        // the sound-effect button
        mSoundButton = (Button) findViewById(R.id.sound_button);
        mSoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //buttonsound();
                switchsound();
            }
        });
        // initialize the color of button
        if (mSoundStatus) {
            mSoundButton.setBackgroundResource(R.drawable.trekbutton_yellow_center);
        } else {
            mSoundButton.setBackgroundResource(R.drawable.trekbutton_gray_center);
        }

        // the start button
        mStartButton = (Button) findViewById(R.id.start_button);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonsound();
                mRunStatus = true;
                //startsensors(mSensormode);
            }
        });

        // the stop button
        mStopButton = (Button) findViewById(R.id.stop_button);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonsound();
                mRunStatus = false;
                //stopsensors();
            }
        });

        // the settings button
        mSettingsButton = (Button) findViewById(R.id.settings_button);
        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonsound();
                settingsactivity();
            }
        });

        // the top status line
        mTextstatus_top = (TextView) findViewById(R.id.textstatus_top);
        mTextstatus_top.setText("");

        // ===================== bottom horizontal button grid ==========================
        // To Navigate Back
        mBackButton = (ImageButton) findViewById(R.id.back_button);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonsound();
            }
        });

        // to Navigate First
        mFirstButton = (Button) findViewById(R.id.first_button);
        mFirstButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonsound();
            }
        });

        // to Navigate Precedent
        mPrecButton = (Button) findViewById(R.id.prec_button);
        mPrecButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonsound();
            }
        });

        // to Navigate Next
        mNextButton = (Button) findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonsound();
            }
        });

        // to Navigate Last
        mLastButton = (Button) findViewById(R.id.last_button);
        mLastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonsound();
            }
        });

        // the bottom status line
        mTextstatus_bottom = (TextView) findViewById(R.id.textstatus_bottom);
        mTextstatus_bottom.setText("Ready");

        // ===================== Left Vertical button grid ==========================


        // the sensors fragment button
        mSensorButton = (Button) findViewById(R.id.sensor_button);
        mSensorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonsound();
                switchfragment(1);
            }
        });

        // the communication fragment button
        mCommButton = (Button) findViewById(R.id.comm_button);
        mCommButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonsound();
                switchfragment(2);
            }
        });





        // ==========================================================================
        // set fonts on widgets
        // load fonts in mem
        Typeface face1 = Typeface.createFromAsset(getAssets(), "sonysketchef.ttf");
        Typeface face2 = Typeface.createFromAsset(getAssets(), "finalold.ttf");
        Typeface face3 = Typeface.createFromAsset(getAssets(), "finalnew.ttf");

        // top buttons
        mSoundButton.setTypeface(face2);
        mStartButton.setTypeface(face2);
        mStopButton.setTypeface(face2);
        mSettingsButton.setTypeface(face2);
        mTextstatus_top.setTypeface(face1);

        // bottom buttons
        mFirstButton.setTypeface(face2);
        mPrecButton.setTypeface(face2);
        mNextButton.setTypeface(face2);
        mLastButton.setTypeface(face2);
        mTextstatus_bottom.setTypeface(face3);

        // left column buttons
        mSensorButton.setTypeface(face1);
        mCommButton.setTypeface(face1);


        // --------------------------------------------------------------------------
        // create the 1 initial fragment
        mMultipadFragment=new MultipadFragment();
        // start the fragment in the fragment box
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.fragment_block, mMultipadFragment, TAG);
        ft.commit();
        currentMode=0;
        // switch to the last used fragment if not the initial fragment
        if(lastmode!=0) {
            switchfragment(lastmode);
        }

    }

    // =====================================================================================

    // switch fragment in the fragment block
    private void switchfragment(int mode) {
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        switch(mode) {
            case 0:
                if(mMultipadFragment==null) mMultipadFragment=new MultipadFragment();
                ft.replace(R.id.fragment_block, mMultipadFragment, TAG);
                ft.commit();
                currentMode=mode;
                break;
        }
    }

    // =====================================================================================

    // media player pointer
    private MediaPlayer soundmedia = null;

    // player for differents sounds
    private void playsound(int resourceid) {
        if (soundmedia != null) {
            soundmedia.release();
        }
        soundmedia = MediaPlayer.create(getBaseContext(), resourceid);
        soundmedia.start(); // no need to call prepare(); create() does that for you
    }

    // standard beep for button press
    private void buttonsound() {
        playsound(R.raw.keyok2);
    }

    // =====================================================================================

    // switch background sound on/off
    private void switchsound() {
        if (mSoundStatus) {
            mSoundStatus = false;
            mSoundButton.setBackgroundResource(R.drawable.trekbutton_gray_center);
        } else {
            mSoundStatus = true;
            mSoundButton.setBackgroundResource(R.drawable.trekbutton_yellow_center);
        }
    }

    // =====================================================================================
    // settings activity incorporation in the display

    public void settingsactivity() {
        say("Settings");
        if (isChatty) speak("Settings");
        // start the preferences activity
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
        // reload the modifiables preferences
        isChatty = sharedPref.getBoolean("pref_key_ischatty", false);
    }

    // =========================================================================
    // system log talker

    private StringBuffer logbuffer = new StringBuffer(1000);

    public void say(String texte) {
        mTextstatus_bottom.setText(texte);
        logbuffer.insert(0, texte + "\n");
        if (logbuffer.length() > 1000) logbuffer.setLength(1000);
        logschanged(logbuffer.toString());
    }

    private void logschanged(String text) {
        if(currentMode==0 && mMultipadFragment != null) mMultipadFragment.logschanged(text);
    }

    // =========================================================================
    // usage of text-to-speech to speak a sensence

    private TextToSpeech tts=null;

    private void initspeak() {
        if(tts==null) {
            tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status != TextToSpeech.ERROR) {
                        setspeaklang("EN");
                    }
                }
            });
        }
    }

    public void setspeaklang(String lng) {
        if (lng.equals("FR")) {
            if(tts!=null) tts.setLanguage(Locale.FRENCH);
        } else if (lng.equals("EN")) {
            if(tts!=null) tts.setLanguage(Locale.US);
        } else {
            // default prechoosen language
        }
    }

    public void speak(String texte) {
        initspeak();
        if(tts!=null) tts.speak(texte, TextToSpeech.QUEUE_FLUSH, null);
        say("Speaked: "+texte);
    }

    public void speak(String texte,String lng) {
        initspeak();
        setspeaklang(lng);
        if(tts!=null) tts.speak(texte, TextToSpeech.QUEUE_ADD, null);
        say("Speaked: "+texte);
    }

    // ========================================================================================
    // functions to control the speech process

    // handles for the conversation functions
    private SpeechRecognizer mSpeechRecognizer = null;
    private Intent mSpeechRecognizerIntent = null;

    public void listen() {
        if (mSpeechRecognizer == null) {
            // ============== initialize the audio listener and talker ==============

            //AudioManager mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

            mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            mSpeechRecognizer.setRecognitionListener(this);
            mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "net.ddns.mlsoftlaberge.multipad");
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
            //mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false);
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 50);
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 20);
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

            // produce a FC on android 4.0.3
            //mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, true);
        }

        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
        //mTextstatus_top.setText("");
        say("Listening...");
    }

    // =================================================================================
    // listener for the speech recognition service
    // ========================================================================================
    // functions to listen to the voice recognition callbacks

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
    }

    @Override
    public void onEndOfSpeech() {
    }

    @Override
    public void onError(int error) {
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
    }

    @Override
    public void onRmsChanged(float rmsdB) {
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> dutexte = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (dutexte != null && dutexte.size() > 0) {
            for (int i = 0; i < dutexte.size(); ++i) {
                String mSentence = dutexte.get(i);
                if (matchvoice(mSentence)) {
                    //say("Understood: " + mSentence);
                    return;
                }
            }
            //say("Understood: " + dutexte.get(0));
            //speak(dutexte.get(0));
            understood(dutexte.get(0));
        }
    }

    // try to match some main commands
    private boolean matchvoice(String textein) {
        String texte = textein.toLowerCase();
        if (texte.contains("fuck") || texte.contains("shit")) {
            say("Understood Fuck!");
            playsound(R.raw.donotaddressthisunitinthatmanner_clean);
            return(true);
        }
        if(texte.contains("hello")) {
            say("Understood Hello!");
            speak("Welcome to the Multipad");
            return(true);
        }
        return(false);
    }

    // send to fragment to interpret the sentence pronounced by the user in the listen function
    private void understood(String text) {
        if(currentMode==0 && mMultipadFragment!=null) mMultipadFragment.understood(text);
    }



    // =====================================================================================

    // save the preferences before exiting
    @Override
    public void onStop() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("pref_key_last_mode", currentMode);
        editor.putBoolean("pref_key_sound_status", mSoundStatus);
        editor.putBoolean("pref_key_run_status", mRunStatus);
        editor.commit();
        super.onStop();
    }

    // =====================================================================================

    // permits this activity to hide status and action bars, and proceed full screen
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    // ==========================================================================
    // request all permissions needed for the application

    private void askpermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_TASKS) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED
                ) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.GET_TASKS,
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.WRITE_CONTACTS,
                            Manifest.permission.RECEIVE_BOOT_COMPLETED
                    }, 1);
        }
    }

}
