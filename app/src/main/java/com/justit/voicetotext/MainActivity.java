package com.justit.voicetotext;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.justit.voicetotext.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    InterstitialAd mInterstitialAd;
    private CheckBox checkBoxId;
    private Spinner fromSpinner, toSpinner, voiceLanguagespinner;
    private TextInputEditText sourceEdt, translateTv;
    private ImageView mic, sourseTexeShare, translatedTexeShare, fromSpeech,  toSpeech, appShare;
    TextToSpeech tts;
    private MaterialButton translateBtn;
    String languageCode = "0";
    private AdView mAdView;


    String[] fromLanguages = {"English", "Bengali", "Hindi", "Urdu", "Philippine", "Afrikaans", "Arabic", "Korean", "Japanese",
            "Catalan", "Spanish", "Swedish"};

    String[] toLanguages = {"Bengali", "English", "Hindi", "Urdu", "Afrikaans", "Arabic", "Korean", "Japanese",
            "Catalan", "Spanish", "Swedish"};

    private static final int REQUEST_PERMISSION_CODE = 0;
    int fromLanguageCode, toLanguageCode = 0;
    String voiceLanguageCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobileAds.initialize(MainActivity.this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.toolbar);
        getSupportActionBar().setElevation(0);
        View view = getSupportActionBar().getCustomView();
        appShare = view.findViewById(R.id.appShare);

        //        Share
        sourseTexeShare = findViewById(R.id.sourseTexeShare);
        translatedTexeShare = findViewById(R.id.translatedTexeShare);
        //ADDVIEW

        MobileAds.initialize(MainActivity.this);
        AdRequest adRequest = new AdRequest.Builder().build();
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-4459566286777302/7966254460");
        mAdView = findViewById(R.id.adView);
        mAdView.loadAd(adRequest);


        fromSpinner = findViewById(R.id.idFromSpinner);
        toSpinner = findViewById(R.id.idToSpinner);
        sourceEdt = findViewById(R.id.idEdtsource);
        mic = findViewById(R.id.idMic);
        checkBoxId = findViewById(R.id.checkBoxId);
//        appShare = findViewById(R.id.appShare);

        translateBtn = findViewById(R.id.idBtnTranslate);
        translateTv = findViewById(R.id.idEdttranslated);


        appShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Just Say");
                    String shareMessage= "I am using this application for voice to text conversion, also for translation to other languages. " +
                            "My fascination is that I can share the texts in social media and to messaging app like Whatsapp, Messenger, Imo etc." +
                            " It would be helpful for you.\n";
                    shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=com.justit.voicetotext";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "choose one"));
                } catch(Exception e) {
                    //e.toString();
                }
            }
        });

        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromLanguageCode = getLanguageCode(fromLanguages[position], true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ArrayAdapter fromAdapter = new ArrayAdapter(this, R.layout.spinner_item, fromLanguages);
        fromAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);

        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLanguageCode = getLanguageCode(toLanguages[position], false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sourseTexeShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = sourceEdt.getText().toString();
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));


                if (checkBoxId.isChecked()) {
                    sourceEdt.setText("");
                }

            }
        });

        translatedTexeShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = translateTv.getText().toString();
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));

                if (checkBoxId.isChecked()) {
                    translateTv.setText("");
                }
            }
        });

        ArrayAdapter toAdapter = new ArrayAdapter(this, R.layout.spinner_item, toLanguages);
        toAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);

        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mInterstitialAd != null) {
                    mInterstitialAd.show(MainActivity.this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                }

                translateTv.setText("");
                if (sourceEdt.getText().toString().isEmpty()) {

                    Toast.makeText(MainActivity.this, "Please enter text to translate", Toast.LENGTH_LONG).show();
                } else if (fromLanguageCode == 0) {
                    Toast.makeText(MainActivity.this, "Please Select source language", Toast.LENGTH_LONG).show();
                } else if (toLanguageCode == 0) {
                    Toast.makeText(MainActivity.this, "Please Select the language to make translation", Toast.LENGTH_LONG).show();
                } else {
                    translateText(fromLanguageCode, toLanguageCode, sourceEdt.getText().toString());
                }
            }
        });


        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, voiceLanguageCode);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 9000);
                i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 9000);
                Database database = new Database(getApplicationContext());
                try {
                    startActivityForResult(i, REQUEST_PERMISSION_CODE);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        translateTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String shareBody = translateTv.getText().toString();
                copytoClip(shareBody);
                return true;
            }
        });

        InterstitialAd.load(this, "ca-app-pub-4459566286777302/2399533752", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        mInterstitialAd = null;
                    }
                });
    }

    private void copytoClip(String text) {
        ClipboardManager clipBoard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Data", text);
        clipBoard.setPrimaryClip(clip);

        Toast.makeText(MainActivity.this, "Copied", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            sourceEdt.setText(result.get(0));
        }
    }

    private void translateText(int fromLanguageCode, int toLanguageCode, String source) {
        translateTv.setText("Translating..");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguageCode).build();

        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                translateTv.setText("Translating...");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        translateTv.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Fail to translate:" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Fail to download Language" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private int getLanguageCode(String language, boolean isFromSpinner) {

        int selectedLanguage = FirebaseTranslateLanguage.EN;
        switch (language) {
            case "English":
                languageCode = "en";
                selectedLanguage = FirebaseTranslateLanguage.EN;
                break;

            case "Bengali":
                languageCode = "bn";
                selectedLanguage = FirebaseTranslateLanguage.BN;
                break;

            case "Hindi":
                languageCode = "hi";
                selectedLanguage = FirebaseTranslateLanguage.HI;
                break;

            case "Belarusian":
                languageCode = "be";
                selectedLanguage = FirebaseTranslateLanguage.BE;
                break;

            case "Urdu":
                languageCode = "ur";
                selectedLanguage = FirebaseTranslateLanguage.UR;
                break;

            case "Afrikaans":
                languageCode = "af";
                selectedLanguage = FirebaseTranslateLanguage.AF;
                break;

            case "Arabic":
                languageCode = "ar";
                selectedLanguage = FirebaseTranslateLanguage.AR;
                break;

            case "Korean":
                languageCode = "ko";
                selectedLanguage = FirebaseTranslateLanguage.KO;
                break;

            case "Catalan":
                languageCode = "ca";
                selectedLanguage = FirebaseTranslateLanguage.CA;
                break;

            case "Spanish":
                languageCode = "es";
                selectedLanguage = FirebaseTranslateLanguage.ES;
                break;

            case "Japanese":
                languageCode = "ja";
                selectedLanguage = FirebaseTranslateLanguage.JA;
                break;

            case "Swedish":
                languageCode = "sv";
                selectedLanguage = FirebaseTranslateLanguage.SV;
                break;

            case "Philippine":
                languageCode = "fil";
                selectedLanguage = FirebaseTranslateLanguage.PL;
                break;
        }
        if (isFromSpinner)
            voiceLanguageCode = languageCode;
        return selectedLanguage;
    }


}