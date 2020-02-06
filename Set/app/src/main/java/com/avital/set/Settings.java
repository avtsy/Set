package com.avital.set;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class Settings extends AppCompatActivity
{
    Dialog d;


    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_settings);

        Switch sw_s = (Switch) findViewById (R.id.sound);
        sw_s.setOnCheckedChangeListener (changeColor);

        Switch sw_t = (Switch) findViewById (R.id.timer);
        sw_t.setOnCheckedChangeListener (changeColor);

        Switch sw_c = (Switch) findViewById (R.id.clue);
        sw_c.setOnCheckedChangeListener (changeColor);


        Intent i = getIntent ();

        if (i.getExtras () != null) // we came from game page
        {
            sw_c.setChecked (Boolean.valueOf (i.getStringExtra ("clues")));
            sw_s.setChecked (Boolean.valueOf (i.getStringExtra ("sound")));
            sw_t.setChecked (Boolean.valueOf (i.getStringExtra ("timer")));
        }
    }


    CompoundButton.OnCheckedChangeListener changeColor = new CompoundButton
            .OnCheckedChangeListener () {
        @Override
        public void onCheckedChanged (CompoundButton buttonView, boolean isChecked)
        {
            if (isChecked)
            {
                buttonView.setTextColor(Color.RED);

                if (buttonView.getId() == R.id.timer)
                    setToast ();
            }

            else
                buttonView.setTextColor (getResources ().getColor (R.color.settingsGrey));
        }
    };


    public void setToast ()
    {
        Toast.makeText (this, getResources ().getString (R.string.clock_toast), Toast.LENGTH_LONG).show ();
    }


    public void play (View view)
    {
        Switch sw = (Switch) findViewById (R.id.sound);
        boolean sound = sw.isChecked ();

        sw = (Switch) findViewById (R.id.timer);
        boolean timer = sw.isChecked ();

        sw = (Switch) findViewById (R.id.clue);
        boolean clues = sw.isChecked ();

        Intent i = new Intent ();

        i.putExtra ("sound", Boolean.toString (sound));
        i.putExtra ("timer", Boolean.toString (timer));
        i.putExtra ("clues", Boolean.toString (clues));

        i.setClass (this, MainActivity.class);
        startActivity (i);
        this.finish ();
    }


    public void onBackPressed () // back button
    {
        Intent i = new Intent ();
        i.setClass (this, openScreen.class);
        startActivity (i);
        finish ();
    }


    @Override
    public boolean onCreateOptionsMenu (Menu menu) // create menu
    {
        getMenuInflater ().inflate (R.menu.menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected (MenuItem item) // operate menu
    {
        if (item.getItemId () == R.id.backToMain) // go to first page
        {
            Intent i = new Intent ();
            i.setClass (this, openScreen.class);
            startActivity (i);
            this.finish ();
        }


        else // esc game
        {
            AlertDialog.Builder d = new AlertDialog.Builder (this);
            d.setMessage (getResources ().getString (R.string.sure_esc));
            d.setPositiveButton (getResources ().getString (R.string.yes), exit);
            d.setNegativeButton (getResources ().getString (R.string.no), cancel);
            AlertDialog ad = d.create ();
            ad.show ();
        }

        return super.onOptionsItemSelected (item);
    }


    DialogInterface.OnClickListener exit = new DialogInterface.OnClickListener () {
        @Override
        public void onClick (DialogInterface dialog, int which)
        {
            SharedPreferences file = getSharedPreferences ("MyFile", Context.MODE_PRIVATE); // set directions for next time
            SharedPreferences.Editor edit_file = file.edit ();
            edit_file.putBoolean ("first", true);
            edit_file.commit ();

            finish();
        }
    };

    DialogInterface.OnClickListener cancel;

}
