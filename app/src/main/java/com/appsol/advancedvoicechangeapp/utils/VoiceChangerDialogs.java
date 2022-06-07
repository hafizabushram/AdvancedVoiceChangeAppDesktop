package com.appsol.advancedvoicechangeapp.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.appsol.advancedvoicechangeapp.R;

import java.util.concurrent.atomic.AtomicInteger;

public class VoiceChangerDialogs {
    public interface OnAdsFree {
        void onClickPurchase();
    }

    public interface RenameInterFace {
        void onClickRename();
    }

    public interface SelectionInterFace {
        void onClickSelect(int someValue);
    }


    public static Dialog createPurchaseDailog(final Context context, final OnAdsFree listener) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rootView = null;
        rootView = inflater.inflate(R.layout.voice_changer_dialog_ads_free, null);
        final Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.MATCH_PARENT;

        dialog.setContentView(rootView);

        rootView.setOnClickListener(v -> dialog.dismiss());


        View cancelButton = rootView.findViewById(R.id.btnAdsFree);
        rootView.findViewById(R.id.btnRemindMeLater).setOnClickListener(view -> {
            dialog.dismiss();
        });
        cancelButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClickPurchase();
            }
            dialog.dismiss();
        });


        return dialog;
    }

    public static Dialog createExitDialog(final Context context, boolean isExit, final OnAdsFree listener) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rootView;
        if (isExit)
            rootView = inflater.inflate(R.layout.voice_changer_dialog_exit_screen, null);
        else
            rootView = inflater.inflate(R.layout.voice_changer_dialog_discard, null);

        final Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.MATCH_PARENT;

        dialog.setContentView(rootView);

        rootView.setOnClickListener(v -> dialog.dismiss());


        View okButton = rootView.findViewById(R.id.btnOk);
        okButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onClickPurchase();
            }
        });
//        FrameLayout frameLayout = rootView.findViewById(R.id.framlayourAds);
//        AdMobInterstitial.refreshAd(context, frameLayout);

        View cancelButton = rootView.findViewById(R.id.btnNo);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        return dialog;
    }

    public static Dialog createOppsDialog(Context context, final OnAdsFree listener) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rootView = null;

        rootView = inflater.inflate(R.layout.voice_changer_oppss_dialog, null);

        final Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.MATCH_PARENT;

        dialog.setContentView(rootView);

        rootView.setOnClickListener(v -> dialog.dismiss());


        View okButton = rootView.findViewById(R.id.btnOk);
        okButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (listener != null) {
                    listener.onClickPurchase();
                }
            }
        });

        View cancelButton = rootView.findViewById(R.id.btnNo);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        return dialog;
    }

    public static Dialog createDeleteDialog(Context context, final OnAdsFree listener) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rootView = null;

        rootView = inflater.inflate(R.layout.voice_changer_dialog_delete, null);

        final Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.MATCH_PARENT;

        dialog.setContentView(rootView);

        rootView.setOnClickListener(v -> dialog.dismiss());


        View okButton = rootView.findViewById(R.id.btn_delete);
        okButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (listener != null) {
                    listener.onClickPurchase();
                }
            }
        });

        View cancelButton = rootView.findViewById(R.id.btn_notDelete);
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        return dialog;
    }

    public static Dialog createRenameDialog(Context context, final RenameInterFace listener) {
        String MyPrefs = "Voice_ChangerPrefs";


        SharedPreferences sharedpreferences = context.getSharedPreferences(MyPrefs, Context.MODE_PRIVATE);


        SharedPreferences.Editor editor = sharedpreferences.edit();


        LayoutInflater inflater = LayoutInflater.from(context);
        View rootView = null;

        rootView = inflater.inflate(R.layout.voice_changer_dialog_rename, null);

        final Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.MATCH_PARENT;

        dialog.setContentView(rootView);

        rootView.setOnClickListener(v -> dialog.dismiss());


        View renameButton = rootView.findViewById(R.id.btn_rename);
        View cancelButton = rootView.findViewById(R.id.btn_notRename);
        View crossButton = rootView.findViewById(R.id.btn_clearEditText);
        EditText getEditText = rootView.findViewById(R.id.mEditTextFileName);

        String getName = sharedpreferences.getString("filenameForRecord", "");

        getEditText.setText(getName);

        renameButton.setOnClickListener(v -> {

            if (TextUtils.isEmpty(getEditText.getText().toString())) {
                getEditText.setError("Enter File Name First");
                crossButton.setVisibility(View.GONE);

            } else {
                dialog.dismiss();
                if (listener != null) {

                    editor.putString("textOfStringToRename", getEditText.getText().toString().trim());


                    editor.apply();


                    listener.onClickRename();
                }
            }
        });

        getEditText.addTextChangedListener(new TextWatcher() {

                                               @Override
                                               public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                               }

                                               @Override
                                               public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                               }

                                               @Override
                                               public void afterTextChanged(Editable editable) {

                                                   crossButton.setVisibility(View.VISIBLE);

                                               }
                                           }

        );

        crossButton.setOnClickListener(view -> getEditText.setText(""));


        cancelButton.setOnClickListener(v -> dialog.dismiss());
        return dialog;
    }

    public static Dialog createSavedDialog(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rootView;

        rootView = inflater.inflate(R.layout.voice_changer_dialog_saved, null);

        final Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.MATCH_PARENT;

        dialog.setContentView(rootView);

        rootView.setOnClickListener(v -> dialog.dismiss());

        return dialog;
    }


    public static Dialog createOptionsDialog(final Context context, final SelectionInterFace listener, String EffectName) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rootView;

        rootView = inflater.inflate(R.layout.voice_changer_dialog_options, null);

        final Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.MATCH_PARENT;

        dialog.setContentView(rootView);

        rootView.setOnClickListener(v -> dialog.dismiss());

        RadioButton ratioSelected = rootView.findViewById(R.id.rd_selected);
        ratioSelected.setText(EffectName + " Effect");


        TextView radioTextView = rootView.findViewById(R.id.tv_radioButtonText);
        TextView radioNote = rootView.findViewById(R.id.tv_radioButtonNote);


        RadioGroup radioGroup = rootView.findViewById(R.id.radioGroup);


        AtomicInteger position = new AtomicInteger(0);
        if (position.get() == 0) {
            radioTextView.setText(R.string.apply_effects_on_original_voice);
            radioNote.setVisibility(View.GONE);
        }
        radioGroup.setOnCheckedChangeListener((rGroup, checkedId) -> {

            int radioButtonID = rGroup.getCheckedRadioButtonId();

            View radioB = rGroup.findViewById(radioButtonID);

            position.set(radioGroup.indexOfChild(radioB));

            if (position.get() == 0) {
                radioTextView.setText(R.string.apply_effects_on_original_voice);
                radioNote.setVisibility(View.GONE);

            } else if (position.get() == 1) {
                radioTextView.setText("Add effects to your " + EffectName + " Effect");
                radioNote.setVisibility(View.VISIBLE);
                radioNote.setText("Note: adding more effects to this voice may affect your sound quality");
            } else if (position.get() == 2) {
                radioTextView.setText("record your new voice");
                radioNote.setVisibility(View.VISIBLE);
                radioNote.setText("Note: it will discard your current voice");

            }
        });


        View okButton = rootView.findViewById(R.id.btnOk);
        okButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onClickSelect(position.get());
            }
        });

//        FrameLayout frameLayout = rootView.findViewById(R.id.framlayourAds);
//        AdMobInterstitial.refreshAd(context, frameLayout);

        View cancelButton = rootView.findViewById(R.id.btnNo);
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        return dialog;
    }


}
