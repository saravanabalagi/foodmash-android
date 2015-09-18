package in.foodmash.app.commons;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;

import in.foodmash.app.LoginActivity;

/**
 * Created by sarav on Aug 30 2015.
 */
public class Alerts {


    public static void internetConnectionErrorAlert(final Context context) {
        String message = "Sometimes the internet gets a bit sleepy and takes a nap. Make sure its up and running then we'll give it another go";
        new AlertDialog.Builder(context)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle("Network Error")
                .setMessage(message)
                .setPositiveButton("Turn Internet On", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) { Intent i = new Intent(Settings.ACTION_SETTINGS); ((Activity)context).startActivityForResult(i,0); } })
                .setNegativeButton("Ignore", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) { } })
                .show();
    }

    public static void timeoutErrorAlert(Context context, DialogInterface.OnClickListener onClickPositiveButton) {
        String message = "Are you connected to internet? We guess you aren't. Turn it on and we'll rock and roll!";
        new AlertDialog.Builder(context)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle("Network Error")
                .setMessage(message)
                .setPositiveButton("Try Again", onClickPositiveButton)
                .setNegativeButton("Ignore", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) { } })
                .show();
    }

    public static void unknownErrorAlert(Context context) {
        new AlertDialog.Builder(context)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle("Server Error")
                .setMessage("We all have bad days! We'll fix this soon...")
                .setPositiveButton("Hmm, I understand", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    public static void commonErrorAlert(Context context, String title, String message, String buttonName, DialogInterface.OnClickListener onClickPositiveButton) {
        new AlertDialog.Builder(context)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(buttonName,onClickPositiveButton)
                .show();
    }

    public static void commonErrorAlert(Context context, String title, String message, String buttonName) {
        new AlertDialog.Builder(context)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(buttonName, new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) { } })
                .show();
    }

    public static void validityAlert(Context context) {
        new AlertDialog.Builder(context)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle("Invalid Data Found")
                .setMessage("One or more data you have entered is invalid. Correct the fields with error sign before proceeding.")
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) { } })
                .show();
    }

    public static void requestUnauthorisedAlert(final Context context) {
        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle("Request Unauthorized")
                .setMessage("We are unable to process your request, as we found your request suspicious. Please login again!")
                .setPositiveButton("Login", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) { logout(context); } })
                .show();
    }

    private static void logout(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("session", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("logged_in", false);
        editor.remove("user_token");
        editor.remove("session_token");
        editor.remove("android_token");
        editor.apply();
        Intent intent = new Intent(context,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

}
