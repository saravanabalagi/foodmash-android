package in.foodmash.app.commons;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by sarav on Aug 30 2015.
 */
public class Alerts {

    public static void internetConnectionErrorAlert(Context context) {
        new AlertDialog.Builder(context)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle("Network Error")
                .setMessage("Sometimes the internet gets a bit sleepy and takes a nap. Make sure its up and running then we'll give it another go.")
                .setPositiveButton("Alright", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
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
                }).show();
    }

    public static void commonErrorAlert(Context context, String title, String message, String buttonName) {
        new AlertDialog.Builder(context)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(buttonName, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    public static void validityAlert(Context context) {
        new AlertDialog.Builder(context)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle("Invalid Data Found")
                .setMessage("One or more data you have entered is invalid. Correct them before procceding.")
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    public static void unableToProcessResponseAlert(Context context) {
        new AlertDialog.Builder(context)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle("Unable to process request")
                .setMessage("Something went wrong. We are unable to process your request. Try again!")
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

}
