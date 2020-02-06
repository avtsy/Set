package com.avital.set;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class openScreen extends AppCompatActivity

    {
        LinearLayout main;
        Dialog d;


        @Override
        protected void onCreate (Bundle savedInstanceState)
        {
            super.onCreate (savedInstanceState);
            setContentView (R.layout.activity_open_screen);

            main = (LinearLayout) findViewById (R.id.activity_open_screen);

            ((ImageView) findViewById (R.id.scores)).setOnLongClickListener (deleteScores);
        }


        @Override
        public void onBackPressed () // exit application on back button
        {
            SharedPreferences file = getSharedPreferences ("MyFile", Context.MODE_PRIVATE); // set directions for next time
            SharedPreferences.Editor edit_file = file.edit ();
            edit_file.putBoolean ("first", true);
            edit_file.commit ();

            super.onBackPressed ();
        }

        public void goToGame (View view)
        {
            Intent i = new Intent ();
            i.setClass (this, MainActivity.class);
            startActivity (i);
            finish ();
        }


        public void goToInstructions (View view)
        {
            makeDialog (getResources ().getString (R.string.instructions), R.drawable.smily_happy);
        }


        public void goToCredits (View view)
        {
            makeDialog (getResources ().getString (R.string.credits), R.drawable.smily_tnx);
        }


        public void goToSettings (View view)
        {
            Intent i = new Intent ();
            i.setClass (this, Settings.class);
            startActivity (i);
            finish ();
        }


        public void goToScores (View view)
        {
            SharedPreferences file = getSharedPreferences ("MyFile", Context.MODE_PRIVATE);

            String scores = file.getString ("scores", null);

            if (scores == null) // scores are empty
            {
                clearScores();
                scores = file.getString ("scores", null);
            }

            String str = getResources ().getString (R.string.scores) + "\n\n";


            String [] parts = scores.split ("/");

            String [] temp;

            for (int i = 0 ; i < 5 ; i++)
            {
                temp = parts[i].split ("!");

                if (temp [2].equals ("99:99")) // empty score
                    temp [2] = "00:00";

                str +=    temp [0] + "  "
                        + temp [1] + " " + getResources ().getString (R.string.scores_sets) + " ";
                str +=    temp [3] + " " + getResources ().getString (R.string.clue) + " "
                        + temp [2] +  "      ";

                str += "\n";
            }

            makeDialog (str, R.drawable.smily_tnx);
        }



        public void goToAbout (View view)
        {
            makeDialog (getResources ().getString (R.string.about), R.drawable.smily_idea);
        }


        public void makeDialog (String text, int pic)
        {
            d = new Dialog (this);
            d.setCancelable (true);
            d.setContentView (R.layout.credits_layout);

            TextView tv = (TextView) d.findViewById (R.id.text);
            tv.setText (text);

            ImageView img = (ImageView) d.findViewById (R.id.close_btn);
            img.setImageDrawable (getResources ().getDrawable (pic));

            if (pic == R.drawable.smily_tnx) // smaller text size for scores
                tv.setTextSize(18);


            View.OnClickListener close = new View.OnClickListener () {@Override
            public void onClick (View v) {d.dismiss ();}};
            LinearLayout l = (LinearLayout) d.findViewById (R.id.l);
            l.setOnClickListener (close);

            d.show ();
        }


        View.OnLongClickListener deleteScores = new View.OnLongClickListener () {
            @Override
            public boolean onLongClick (View v)
            {
                clearScores ();
                return false;
            }
        };


        public void clearScores () // clear all scores
        {
            String str = "";

            for (int i = 0; i < 5; i++)
            {
                Score temp = new Score ();
                str += temp.getAll ();
                str += "/";
            }

            SharedPreferences file = getSharedPreferences ("MyFile", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit_file = file.edit ();
            edit_file.putString ("scores", str);
            edit_file.commit ();

            Toast.makeText(this, getResources().getString(R.string.deleteScores), Toast.LENGTH_SHORT).show();
        }
    }

