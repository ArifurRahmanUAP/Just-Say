package com.example.voicetotextandtranslate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
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
    private ImageView mic, sourseTexeShare, translatedTexeShare;
    private MaterialButton translateBtn;
    String languageCode = "0";
    private AdView mAdView;



    String[] fromLanguages = {"English", "Bengali", "Hindi", "Urdu", "Afrikaans", "Arabic", "Belarusian", "Bulgarian",
            "Catalan", "Czech", "Welsh"};

    String[] toLanguages = {"Bengali", "English", "Hindi", "Urdu", "Afrikaans", "Arabic", "Belarusian", "Bulgarian",
            "Catalan", "Czech", "Welsh"};

    private static final int REQUEST_PERMISSION_CODE = 0;
    int fromLanguageCode, toLanguageCode = 0;
    String voiceLanguageCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        //        Share
        sourseTexeShare = findViewById(R.id.sourseTexeShare);
        translatedTexeShare = findViewById(R.id.translatedTexeShare);
                        //ADDVIEW

        MobileAds.initialize(MainActivity.this);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView = findViewById(R.id.adView);
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-7148413509095909/4203091086");
        mAdView.loadAd(adRequest);


        fromSpinner = findViewById(R.id.idFromSpinner);
        toSpinner = findViewById(R.id.idToSpinner);
//      voiceLanguagespinner = findViewById(R.id.voiceLanguage);
        sourceEdt = findViewById(R.id.idEdtsource);
        mic = findViewById(R.id.idMic);
        checkBoxId = findViewById(R.id.checkBoxId);

        translateBtn = findViewById(R.id.idBtnTranslate);
        translateTv = findViewById(R.id.idEdttranslated);

//        ArrayAdapter language = new ArrayAdapter(this, R.layout.spinner_item, voiceLanguage);
//        language.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        voiceLanguagespinner.setAdapter(language);

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


                if (mInterstitialAd != null) {
                    mInterstitialAd.show(MainActivity.this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                }

                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, voiceLanguageCode);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);
                i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);

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

        InterstitialAd.load(this, "ca-app-pub-7148413509095909/1850762081", adRequest,
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

            case "Belarusian":
                languageCode = "hi";
                selectedLanguage = FirebaseTranslateLanguage.HI;
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

            case "Bulgarian":
                languageCode = "be";
                selectedLanguage = FirebaseTranslateLanguage.BE;
                break;

            case "Catalan":
                languageCode = "ca";
                selectedLanguage = FirebaseTranslateLanguage.CA;
                break;

            case "Czech":
                languageCode = "cs";
                selectedLanguage = FirebaseTranslateLanguage.CS;
                break;

            case "Welsh":
                languageCode = "cy";
                selectedLanguage = FirebaseTranslateLanguage.CY;
                break;
        }
        if (isFromSpinner)
            voiceLanguageCode = languageCode;
        return selectedLanguage;
    }

}