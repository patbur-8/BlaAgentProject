package com.ismobile.blaagent;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * An activity for filtering the notification feed using two datepickers and a "filter today" checkbox
 */
public class FilterListview extends Activity {
    private int year;
    private int month;
    private int day;
    Calendar fromCalendar, toCalendar;
    EditText filterTo, filterFrom;
    Context context;
    Button cancelButton, saveButton;
    CheckBox filterTodayCheckBox;
    String prefFromDate;
    String prefToDate;
    SharedPreferences prefs;
    boolean filterToday;

    /**
     * Initiates all the elements and sets onclick listeners
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.filter_listview);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        filterToday = prefs.getBoolean("filterToday",true);

        cancelButton = (Button) findViewById( R.id.cancelButton );
        saveButton = (Button) findViewById( R.id.saveButton );

        filterTodayCheckBox = (CheckBox) findViewById(R.id.filterCheckBox);
        filterTodayCheckBox.setChecked(filterToday);

        filterTodayCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(filterTodayCheckBox.isChecked()) {
                    setFilterToday();
                } else {
                    setPreferenceFilter();
                }
            }
        });

        context = this;


        //If filter today is checked, from and to is todays date.
        //Else use dates stored in preferences
        String myFormatString = "yyyy-MM-dd";
        SimpleDateFormat df = new SimpleDateFormat(myFormatString);
        Date date = new Date();
        String defaultFromDate = df.format(date);
        String defaultToDate = df.format(date);
        String from;
        String to;
        if(filterToday) {
            from = defaultFromDate;
            to = defaultToDate;
        } else {
            from = prefs.getString("filterFrom",defaultFromDate);
            to = prefs.getString("filterTo",defaultToDate);
        }

        //Sets the text in the two textfields
        filterFrom = (EditText) findViewById( R.id.filterFrom );
        filterTo = (EditText) findViewById( R.id.filterTo );

        fromCalendar = Calendar.getInstance();
        toCalendar = Calendar.getInstance();
        String[] fromArray = from.split("-");
        String[] toArray = to.split("-");
        fromCalendar.set(Calendar.YEAR, Integer.parseInt(fromArray[0]));
        fromCalendar.set(Calendar.MONTH, Integer.parseInt(fromArray[1]));
        fromCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(fromArray[2]));

        toCalendar.set(Calendar.YEAR, Integer.parseInt(toArray[0]));
        toCalendar.set(Calendar.MONTH, Integer.parseInt(toArray[1]));
        toCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(toArray[2]));

        Log.d("Time - from", from);
        Log.d("Time - from default", defaultFromDate);
        Log.d("Time - to", to);
        Log.d("Time - to default", defaultToDate);

        filterFrom.setText(from);
        filterTo.setText(to);

        //From date datepicker
        final DatePickerDialog.OnDateSetListener fromDatePicker = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                fromCalendar.set(Calendar.YEAR, year);
                fromCalendar.set(Calendar.MONTH, monthOfYear);
                fromCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                filterTodayCheckBox.setChecked(false);
                updateFromLabel();
            }

        };

        filterFrom.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(context,fromDatePicker , fromCalendar
                        .get(Calendar.YEAR), fromCalendar.get(Calendar.MONTH),
                        fromCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        //To date datepicker
        final DatePickerDialog.OnDateSetListener toDatePicker = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                toCalendar.set(Calendar.YEAR, year);
                toCalendar.set(Calendar.MONTH, monthOfYear);
                toCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                filterTodayCheckBox.setChecked(false);
                updateToLabel();
            }

        };

        //Opens the datepicker so that the user can
        filterTo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(context,toDatePicker , toCalendar
                        .get(Calendar.YEAR), toCalendar.get(Calendar.MONTH),
                        toCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        //Finish the activity and saves the preference
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean filterToday = filterTodayCheckBox.isChecked();
                if(!filterToday) {
                    prefs.edit().putString("filterFrom",prefFromDate).commit();
                    prefs.edit().putString("filterTo",prefToDate).commit();
                }
                prefs.edit().putBoolean("filterToday",filterToday).commit();
                Intent returnIntent = new Intent();
                setResult(RESULT_OK,returnIntent);
                finish();
            }
        });

        //Finish the activity without saving preferences
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(RESULT_CANCELED, returnIntent);
                finish();
            }
        });


    }

    /**
     * Sets the filter to only list todays nofitications
     */
    public void setFilterToday() {
        String myFormatString = "yyyy-MM-dd";
        SimpleDateFormat df = new SimpleDateFormat(myFormatString);
        Date date = new Date();
        String defaultFromDate = df.format(date);
        String defaultToDate = df.format(date);

        String[] fromArray = defaultFromDate.split("-");
        String[] toArray = defaultFromDate.split("-");
        fromCalendar.set(Calendar.YEAR, Integer.parseInt(fromArray[0]));
        fromCalendar.set(Calendar.MONTH, Integer.parseInt(fromArray[1]));
        fromCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(fromArray[2]));

        toCalendar.set(Calendar.YEAR, Integer.parseInt(toArray[0]));
        toCalendar.set(Calendar.MONTH, Integer.parseInt(toArray[1]));
        toCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(toArray[2]));

        filterFrom.setText(defaultFromDate);
        filterTo.setText(defaultToDate);
    }

    /**
     * Sets the filter to list according to the user preference
     */
    public void setPreferenceFilter() {
        String myFormatString = "yyyy-MM-dd";
        SimpleDateFormat df = new SimpleDateFormat(myFormatString);
        Date date = new Date();
        String defaultFromDate = df.format(date);
        date.setDate(date.getDate() + 1);
        String defaultToDate = df.format(date);
        String from;
        String to;

        from = prefs.getString("filterFrom",defaultFromDate);
        to = prefs.getString("filterTo",defaultToDate);

        fromCalendar = Calendar.getInstance();
        toCalendar = Calendar.getInstance();
        String[] fromArray = from.split("-");
        String[] toArray = to.split("-");
        fromCalendar.set(Calendar.YEAR, Integer.parseInt(fromArray[0]));
        fromCalendar.set(Calendar.MONTH, Integer.parseInt(fromArray[1]));
        fromCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(fromArray[2]));

        toCalendar.set(Calendar.YEAR, Integer.parseInt(toArray[0]));
        toCalendar.set(Calendar.MONTH, Integer.parseInt(toArray[1]));
        toCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(toArray[2]));

        filterFrom.setText(from);
        filterTo.setText(to);
    }

    /**
     *Updates the date in the edittext element
     */
    private void updateFromLabel() {

        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
        String date = sdf.format(fromCalendar.getTime());
        prefFromDate = date;
        filterFrom.setText(date);
    }

    /**
     *Updates the date in the edittext element
     */
    private void updateToLabel() {

        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
        String date = sdf.format(toCalendar.getTime());
        prefToDate = date;
        filterTo.setText(date);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.filter_listview, menu);
        return true;
    }
    
}
