package in.foodmash.app.commons;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;

import in.foodmash.app.custom.ComboDish;
import in.foodmash.app.custom.ComboOption;

/**
 * Created by Zeke on Aug 30 2015.
 */
public class Alerts {


    public static void internetConnectionErrorAlert(final Context context, DialogInterface.OnClickListener onClickTryAgainButton) {
        String message = "Sometimes the internet gets a bit sleepy and takes a nap. Make sure its up and running then we'll give it another go";
        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle("Network Error")
                .setMessage(message)
                .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("Turn Internet On", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(Settings.ACTION_SETTINGS);
                        ((Activity) context).startActivityForResult(i, 0);
                    }
                })
                .show();
    }

    public static void timeoutErrorAlert(Context context, DialogInterface.OnClickListener onClickTryAgainButton) {
        String message = "Are you connected to internet? We guess you aren't. Turn it on and we'll rock and roll!";
        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle("Network Error")
                .setMessage(message)
                .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    public static void unknownErrorAlert(Context context) {
        new AlertDialog.Builder(context)
                .setCancelable(false)
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

    public static void commonErrorAlert(Context context, String title, String message, String buttonName, DialogInterface.OnClickListener onClickPositiveButton, boolean cancelable) {
        new AlertDialog.Builder(context)
                .setCancelable(cancelable)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(buttonName,onClickPositiveButton)
                .show();
    }

    public static void commonErrorAlert(Context context, String title, String message, String buttonName, DialogInterface.OnClickListener onClickPositiveButton) {
        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(buttonName,onClickPositiveButton)
                .show();
    }

    public static void commonErrorAlert(Context context, String title, String message, String buttonName) {
        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(buttonName, new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) { } })
                .show();
    }

    public static void validityAlert(Context context) {
        new AlertDialog.Builder(context)
                .setCancelable(false)
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
                .setPositiveButton("Login", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) { Actions.logout(context); } })
                .show();
    }

    public static void minCountAlert(final Context context, ComboDish comboDish) {
        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle("Could not decrement")
                .setMessage("This combo should contain minimum "+comboDish.getMinCount()+" "+comboDish.getDish().getName())
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) {} })
                .show();
    }

    public static void minCountAlert(final Context context, ComboOption comboOption) {
        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle("Could not decrement")
                .setMessage("This combo should contain "+comboOption.getMinCount()+" or more "+comboOption.getContents())
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) {} })
                .show();
    }

    public static void maxCountAlert(final Context context, ComboDish comboDish) {
        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle("Could not increment")
                .setMessage("For placing bulk order (with 10 or more) " + comboDish.getDish().getName() + " contact Customer Care")
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    public static void maxCountAlert(final Context context, ComboOption comboOption) {
        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle("Could not increment")
                .setMessage("For placing bulk order (with 10 or more) "+comboOption.getContents()+" contact Customer Care")
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) {} })
                .show();
    }

}
