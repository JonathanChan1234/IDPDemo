package com.jonathan.idpdemo2;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by user on 2018-04-14.
 */

public class VerificationDialog {

        public void showDialog(Activity activity){
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialoglayout);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

            EditText verificationCodeText = (EditText) dialog.findViewById(R.id.verificationCodeText);
            String verificationCode =  verificationCodeText.getText().toString();
            if(TextUtils.isEmpty(verificationCode)){
                Toast.makeText(dialog.getContext(), "No code is typed", Toast.LENGTH_SHORT);
            }
            else{
                Toast.makeText(dialog.getContext(), "Verification Code is " + verificationCode, Toast.LENGTH_SHORT);
            }
            dialog.show();
        }
    }

