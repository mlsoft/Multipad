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
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import net.ddns.mlsoftlaberge.multipad.communication.CommunicationFragment;
import net.ddns.mlsoftlaberge.multipad.information.InformationFragment;
import net.ddns.mlsoftlaberge.multipad.sensor.SensorFragment;
import net.ddns.mlsoftlaberge.multipad.settings.SettingsFragment;
import net.ddns.mlsoftlaberge.multipad.utils.Fetcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;

public class MultipadActivity extends Activity
        implements RecognitionListener ,
        MultipadFragment.OnMultipadInteractionListener,
        SensorFragment.OnSensorInteractionListener ,
        CommunicationFragment.OnCommunicationInteractionListener,
        InformationFragment.OnInformationInteractionListener {


    // =====================================================================================
    // implementation of interface from fragments

    // --- Multipad fragment ---
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

    // --- Sensor fragment ---
    @Override
    public void sensorButtonsound() {
        buttonsound();
    }

    @Override
    public void sensorModeChange(int mode) {
        switchfragment(mode);
    }

    @Override
    public void sensorSpeak(String text) {
        speak(text);
    }

    @Override
    public void sensorListen() {
        listen();
    }

    @Override
    public void sensorSay(String text) {
        say(text);
    }

    @Override
    public String sensorLogs() {
        return(logbuffer.toString());
    }

    // --- Communication fragment ---
    @Override
    public void communicationButtonsound() {
        buttonsound();
    }

    @Override
    public void communicationModeChange(int mode) {
        switchfragment(mode);
    }

    @Override
    public void communicationSpeak(String text) {
        speak(text);
    }

    @Override
    public void communicationListen() {
        listen();
    }

    @Override
    public void communicationSay(String text) {
        say(text);
    }

    @Override
    public String communicationLogs() {
        return(logbuffer.toString());
    }

    @Override
    public void communicationOpencomm() {
        initstarship();
    }

    @Override
    public void communicationClosecomm() {
        stopstarship();
    }

    @Override
    public void communicationSendcmd(String cmd) {
        sendtext(cmd);
    }

    // --- Information fragment ---
    @Override
    public void informationButtonsound() {
        buttonsound();
    }

    @Override
    public void informationSay(String text) {
        say(text);
    }

    @Override
    public void informationSpeak(String text) {
        speak(text);
    }

    // =====================================================================================

    // tag for fragment transactions
    private static String TAG = "Multipad";

    // handles to fragments displayable in the fragment block
    private MultipadFragment mMultipadFragment=null;
    private SensorFragment mSensorFragment=null;
    private CommunicationFragment mCommunicationFragment=null;
    private InformationFragment mInformationFragment=null;
    private SettingsFragment mSettingsFragment=null;

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

    // the preferences variables from preference screen
    private boolean isChatty = false;
    private boolean autoListen = false;

    // the two status lines
    private TextView mTextstatus_top;
    private TextView mTextstatus_bottom;

    // the button to exit operation
    private ImageButton mExitButton;

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
    private Button mMultipadButton;

    // the sensors fragment button
    private Button mSensorButton;

    // the communication fragment button
    private Button mCommButton;

    // the information fragment button
    private Button mInfoButton;


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
        autoListen = sharedPref.getBoolean("pref_key_auto_listen", false);
        String logs = sharedPref.getString("pref_key_logbuffer", "");
        logbuffer.setLength(0);
        logbuffer.insert(0,logs);
        logbuffer.insert(0,"-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=\n");

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
                say("Started");
                if(isChatty) speak("Start");
                startsensors();
            }
        });

        // the stop button
        mStopButton = (Button) findViewById(R.id.stop_button);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonsound();
                mRunStatus = false;
                say("Stopped");
                if(isChatty) speak("Stop");
                stopsensors();
            }
        });

        // the settings button
        mSettingsButton = (Button) findViewById(R.id.settings_button);
        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonsound();
                //settingsactivity();
                say("Settings");
                if(isChatty) speak("Settings");
                switchfragment(99);
            }
        });

        // the top status line
        mTextstatus_top = (TextView) findViewById(R.id.textstatus_top);
        mTextstatus_top.setText("");

        // ===================== bottom horizontal button grid ==========================
        // To Navigate Back
        mExitButton = (ImageButton) findViewById(R.id.exit_button);
        mExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonquit();
                say("Exit");
                if(isChatty) speak("Exit");
                finish();
            }
        });

        // to Navigate First
        mFirstButton = (Button) findViewById(R.id.first_button);
        mFirstButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonsound();
                say("First");
                if(isChatty) speak("First");
            }
        });

        // to Navigate Precedent
        mPrecButton = (Button) findViewById(R.id.prec_button);
        mPrecButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonsound();
                say("Precedent");
                if(isChatty) speak("Precedent");
            }
        });

        // to Navigate Next
        mNextButton = (Button) findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonsound();
                say("Next");
                if(isChatty) speak("Next");
            }
        });

        // to Navigate Last
        mLastButton = (Button) findViewById(R.id.last_button);
        mLastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonsound();
                say("Last");
                if(isChatty) speak("Last");
            }
        });

        // the bottom status line
        mTextstatus_bottom = (TextView) findViewById(R.id.textstatus_bottom);
        mTextstatus_bottom.setText("Ready");

        // ===================== Left Vertical button grid ==========================


        // the multipad fragment button
        mMultipadButton = (Button) findViewById(R.id.multipad_button);
        mMultipadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonsound();
                say("Pad");
                if(isChatty) speak("Pad");
                switchfragment(0);
            }
        });

        // the sensors fragment button
        mSensorButton = (Button) findViewById(R.id.sensor_button);
        mSensorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonsound();
                say("Sensors");
                if(isChatty) speak("Sensors");
                switchfragment(1);
            }
        });

        // the communication fragment button
        mCommButton = (Button) findViewById(R.id.comm_button);
        mCommButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonsound();
                say("Communication");
                if(isChatty) speak("Communication");
                switchfragment(2);
            }
        });

        // the information fragment button
        mInfoButton = (Button) findViewById(R.id.info_button);
        mInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonsound();
                say("Information");
                if(isChatty) speak("Information");
                switchfragment(3);
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
        mMultipadButton.setTypeface(face1);
        mSensorButton.setTypeface(face1);
        mCommButton.setTypeface(face1);
        mInfoButton.setTypeface(face1);

        // --------------------------------------------------------------------------
        // create the 1 initial fragment
        mMultipadFragment=new MultipadFragment();
        // start the fragment in the fragment box
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_block, mMultipadFragment, TAG);
        ft.commit();
        currentMode=0;
        // switch to the last used fragment if not the initial fragment
        if(lastmode!=0) {
            switchfragment(lastmode);
        }

    }

    // =====================================================================================

    // save the preferences before exiting
    @Override
    public void onStart() {
        super.onStart();
        // initialize the communications
        //initstarship();
    }

    // save the preferences before exiting
    @Override
    public void onStop() {
        // save the preferences before exiting
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("pref_key_last_mode", currentMode);
        editor.putBoolean("pref_key_sound_status", mSoundStatus);
        editor.putBoolean("pref_key_run_status", mRunStatus);
        editor.putString("pref_key_logbuffer",logbuffer.toString());
        editor.commit();
        // stop the communications
        stopstarship();
        super.onStop();
    }

    // =====================================================================================

    // switch fragment in the fragment block
    private void switchfragment(int mode) {
        // reread the preferences after exiting settings
        if(currentMode==99) {
            isChatty = sharedPref.getBoolean("pref_key_ischatty", false);
            autoListen = sharedPref.getBoolean("pref_key_auto_listen", false);
        }
        // prepare to switch to other fragment
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        switch(mode) {
            case 0:
                ft.setCustomAnimations(R.anim.slideinleft, R.anim.slideoutright);
                if(mMultipadFragment==null) mMultipadFragment=new MultipadFragment();
                ft.replace(R.id.fragment_block, mMultipadFragment, TAG);
                ft.commit();
                currentMode=mode;
                break;
            case 1:
                ft.setCustomAnimations(R.anim.slideinleft, R.anim.slideoutright);
                if(mSensorFragment==null) mSensorFragment=new SensorFragment();
                ft.replace(R.id.fragment_block, mSensorFragment, TAG);
                ft.commit();
                currentMode=mode;
                break;
            case 2:
                ft.setCustomAnimations(R.anim.slideinleft, R.anim.slideoutright);
                if(mCommunicationFragment==null) mCommunicationFragment=new CommunicationFragment();
                ft.replace(R.id.fragment_block, mCommunicationFragment, TAG);
                ft.commit();
                currentMode=mode;
                break;
            case 3:
                ft.setCustomAnimations(R.anim.slideinleft, R.anim.slideoutright);
                if(mInformationFragment==null) mInformationFragment=new InformationFragment();
                ft.replace(R.id.fragment_block, mInformationFragment, TAG);
                ft.commit();
                currentMode=mode;
                break;
            case 99:
                ft.setCustomAnimations(R.anim.slideinleft, R.anim.slideoutright);
                if(mSettingsFragment==null) mSettingsFragment=new SettingsFragment();
                ft.replace(R.id.fragment_block, mSettingsFragment, TAG);
                ft.commit();
                currentMode=mode;
                break;
        }
    }

    private void startsensors() {
        if(currentMode==1 && mSensorFragment!=null) mSensorFragment.startsensors();
    }

    private void stopsensors() {
        if(currentMode==1 && mSensorFragment!=null) mSensorFragment.stopsensors();
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

    private void buttonbad() {
        playsound(R.raw.denybeep1);
    }

    private void buttonquit() {
        playsound(R.raw.boop_beep);
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
        if(currentMode==1 && mSensorFragment != null) mSensorFragment.logschanged(text);
        if(currentMode==2 && mCommunicationFragment != null) mCommunicationFragment.logschanged(text);
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
        say("Speaked: "+texte+" ("+lng+")");
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
                    // the sentence has been matched, then return without further processing
                    return;
                }
            }
            // the sentence is passed to the active fragment(s) to be processed
            understood(dutexte);
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
    private void understood(ArrayList<String> dutexte) {
        if(currentMode==0 && mMultipadFragment!=null) mMultipadFragment.understood(dutexte);
        if(currentMode==1 && mSensorFragment!=null) mSensorFragment.understood(dutexte);
        if(currentMode==2 && mCommunicationFragment!=null) mCommunicationFragment.understood(dutexte);
    }



    // =====================================================================================
    // network operations.   ===   Hi Elvis!
    // =====================================================================================


    // ===================================================================================
    // send a message to the other machines

    public void sendtext(String text) {
        // start the client thread
        say("Send: " + text);
        Thread clientThread = new Thread(new ClientThread(text));
        clientThread.start();
    }

    class ClientThread implements Runnable {

        //private Socket clientSocket = null;

        private String mesg;

        public ClientThread(String str) {
            mesg = str;
        }

        @Override
        public void run() {
            // send to the tryserver machine
            serversend("mlsoftlaberge.ddns.net");
        }

        private void serversend(String destip) {
            // try to send the message thru the starship socket
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(starshipSocket.getOutputStream())), true);
                out.println(mesg);
                out.flush();
                Log.d("clientthread", "data sent: " + mesg);
            } catch (Exception e) {
                Log.d("clientthread", e.toString());
            }
        }

    }

    // ===================================================================================
    // connect with the starship server, announce myself, and wait for orders

    private Fetcher mFetcher = new Fetcher(this);

    private int SERVERPORT = 1701;

    private Handler mHandler = new Handler();

    private Thread starshipThread=null;
    private Socket starshipSocket=null;

    public void initstarship() {
        say("Initialize the starship network");
        // start the starship thread
        Thread starshipThread = new Thread(new StarshipThread());
        starshipThread.start();
    }

    public void stopstarship() {
        say("Stop the starship network");
        // stop the server thread
        try {
            starshipThread.interrupt();
        } catch (Exception e) {
            Log.d("stopstarshipthread", e.toString());
        }
        // close the socket of the server
        try {
            starshipSocket.close();
            starshipSocket=null;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    class StarshipThread implements Runnable {

        String identification="multipad";

        public StarshipThread() {
            identification="multipad:"+mFetcher.fetch_device_name()+
                    "/"+mFetcher.fetch_device_release()+
                    "/"+mFetcher.fetch_package_version();
        }

        @Override
        public void run() {
            // send to the tryserver machine
            starshipsend("192.168.3.101");
        }

        private void starshipsend(String destip) {
            while(!Thread.currentThread().isInterrupted()) {
                // try to connect to a socket
                try {
                    InetAddress serverAddr = InetAddress.getByName(destip);
                    starshipSocket = new Socket(serverAddr, SERVERPORT);
                    Log.d("starshipthread", "server connected " + destip);
                } catch (Exception e) {
                    Log.d("starshipthread", e.toString());
                }
                // try to send the identification
                try {
                    PrintWriter out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(starshipSocket.getOutputStream())), true);
                    out.println(identification);
                    out.flush();
                    Log.d("starshipthread", "data sent: " + identification);
                } catch (Exception e) {
                    Log.d("starshipthread", e.toString());
                }
                // prepare the input stream
                BufferedReader bufinput = null;
                try {
                    bufinput = new BufferedReader(new InputStreamReader(starshipSocket.getInputStream()));
                } catch (Exception e) {
                    Log.d("starshipthread", e.toString());
                }
                // continue to receive all answers from the starship server until it dies
                while (!Thread.currentThread().isInterrupted()) {
                    // try to receive the answer
                    try {
                        String read = bufinput.readLine();
                        if (read != null) {
                            mHandler.post(new updateUIThread(read,1));
                            Log.d("starshipthread", "answer received: " + read);
                        }
                    } catch (Exception e) {
                        Log.d("starshipthread", e.toString());
                        break;
                    }
                }
                // try to close the socket of the client
                try {
                    starshipSocket.close();
                    starshipSocket=null;
                    Log.d("starshipthread", "server closed " + destip);
                } catch (Exception e) {
                    Log.d("starshipthread", e.toString());
                }

            }
        }

    }

    // ===== thread used to update the ui of the running application with received text =====
    class updateUIThread implements Runnable {
        private String msg = null;
        private int rem=0;

        public updateUIThread(String str, int remote) {
            msg = str;
            rem = remote;
            Log.d("uithread", str);
        }

        @Override
        public void run() {
            if (msg != null) {
                say("Recv: " + msg);
            }
        }
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
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, 1);
        }
    }

}
