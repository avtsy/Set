package com.avital.set;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {


    LinearLayout crdLayout; // game layouts
    ImageView[][] mat; // the table 4*3 on the screen
    ArrayList<Card> cards; // 81 cards
    ArrayList<Card> chs3crd; // 3 choosen cards
    ArrayList <Score> scoresList; // for scores

    String clues; // get tip?
    String sound; // sound effects?
    String timer; // timer?
    int cluesNum; // number of clues

    MediaPlayer media_yes, media_no; // for sound effects


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getSettings ();

        SharedPreferences file = getSharedPreferences ("MyFile", Context.MODE_PRIVATE);

        if (!file.contains ("first"))
        {
            showDirections ();

            SharedPreferences.Editor edit_file = file.edit ();
            edit_file.putInt ("first", 1);
            edit_file.commit ();
        }

        else
            StartGame (true);

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
            if (timer.equals ("true"))
                clock_timer.cancel ();

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
            d.setCancelable(true);
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

            if (timer.equals ("true"))
                clock_timer.cancel ();

            finish ();
        }
    };


    public void onBackPressed () // back button
    {
        if (timer.equals ("true"))
            clock_timer.cancel ();

        Intent i = new Intent ();
        i.setClass (this, openScreen.class);
        startActivity (i);
        finish ();
    }


    public void getSettings ()
    {
        Intent i = getIntent ();

        if (i.getStringExtra ("clues") != null) // we came from settings page
        {
            clues = i.getStringExtra ("clues");
            sound = i.getStringExtra ("sound");
            timer = i.getStringExtra ("timer");

            if (sound != null && sound.equals ("true")) // yes, prepare sound effects
            {
                setVolumeControlStream(AudioManager.STREAM_MUSIC);
                media_yes = MediaPlayer.create(this, R.raw.sound_yes);
                media_no = MediaPlayer.create(this, R.raw.sound_no);

                try
                {
                    media_yes.prepare();
                    media_no.prepare();

                }
                catch (Exception ex) {}
            }
        }

        else
        {
            clues = "true";
            sound = "false";
            timer = "true";
        }
    }


    public void StartGame (boolean restart) // BEGIN THE GAME
    {
        createCards(); // create the card collection
        drawTableCardsScrn(); // create the images-matritsa on the screen
        setCards (); // set cards inside the mat, on the screen
        setEasterEgg (); // help...
        cluesNum = 0;

        if (!restart) // it wasn't restart game, it is a new game after win
            setListeners (true);

        if (timer.equals ("true")) // yes, create timer
            createTimer ();

    }


    boolean egg = false;
    public void setEasterEgg () // long click gets tip
    {
        ImageView img = (ImageView) findViewById (R.id.tip_btn);

        img.setOnLongClickListener (Egg);
    }


    View.OnLongClickListener Egg = new View.OnLongClickListener () { // long click gets tip
        @Override
        public boolean onLongClick (View view)
        {
            removeChsBg ();
            egg = true;
            boolean b = IsSetOnScr (true, null, true);
            return b;
        }
    };


    public void startAgain (View view) // restart a new game
    {
        AlertDialog.Builder d = new AlertDialog.Builder (this);
        d.setCancelable (true);
        d.setMessage (getResources ().getString (R.string.restart));
        d.setPositiveButton (getResources ().getString (R.string.yes), restart);
        d.setNegativeButton (getResources ().getString (R.string.no), cancel);
        AlertDialog ad = d.create ();
        ad.show ();
    }

    DialogInterface.OnClickListener restart = new DialogInterface.OnClickListener () {
        @Override
        public void onClick (DialogInterface dialog, int which)
        {
            startNewGame (true);
        }
    };

    DialogInterface.OnClickListener cancel;


    public void startNewGame (boolean restart)
    {
        cards = null;
        chs3crd = null;
        mat = null;
        crdLayout.removeAllViews ();

        TextView cc = (TextView) findViewById (R.id.cards_counter);
        cc.setText ("81");

        cc = (TextView) findViewById (R.id.set_counter);
        cc.setText ("0");

        Toast.makeText (this, getResources().getString (R.string.newgame), Toast.LENGTH_SHORT).show();

        StartGame (restart);
    }


    View.OnClickListener click_lis = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            clickImg(v);
        }
    };


    public void clickImg (View img) // when clicking on img
    {
        Card crd = (Card) img.getTag();

        if (crd.isChoosen() == false) // choose it
        {
            crd.setChoosen(true);
            img.setBackgroundColor(getResources().getColor(R.color.border));
            chs3crd.add(crd);

            if (chs3crd.size() == 3) // 3 cards
            {
                if (checkSet(chs3crd) == true) // SET!
                    set();

                else // NOT SET
                    notSet ();
            }
        }

        else // cancel choose
        {
            for (Iterator<Card> iterator = cards.iterator (); iterator.hasNext ();)//(Card c : chs3crd)
            {
                Card c = iterator.next();
                if (c.getId() == crd.getId())
                {
                    chs3crd.remove (c);
                    break;
                }
            }

            crd.setChoosen(false);
            img.setBackgroundColor(0);
        }
    }


    public void checkIfEnd () // GAME OVER
    {
        if (!IsSetOnScr (false, null, true)) // screen doesnt have a set anymore. game over
            EndGame (getResources ().getString (R.string.end1) + " " + getResources ().getString (R.string.end2));
    }


    public void EndGame (String str)
    {
        if (timer.equals ("true"))
            clock_timer.cancel (); // stop timer

        setListeners (false); // stop listeners

        String info = "\n" + str + "\n\n";
        info += getResources ().getString (R.string.info1) + " "; // you did
        info += ((TextView) findViewById (R.id.set_counter)).getText () + " "; // #
        info += getResources ().getString (R.string.scores_sets) + "\n"; // sets

        if (timer.equals ("true"))
        {
            info += getResources ().getString (R.string.info2) + " "; // by
            info += ((TextView) findViewById (R.id.timer)).getText () + " "; // ##:##
            info += getResources ().getString (R.string.info3) + "\n"; // minuets
            info += getResources ().getString (R.string.info4); // and
        }

        info += Integer.toString (cluesNum) + " "; // #
        info += getResources ().getString (R.string.clue) + "\n\n"; // by
        info += getResources ().getString (R.string.shcoyech); // kol hacavod!!!

        makeEndMsg (info);
    }


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
    }


    public void handleScores () // insert score
    {
        SharedPreferences file = getSharedPreferences ("MyFile", Context.MODE_PRIVATE);
        String scores = file.getString ("scores", null);

        if (scores == null) // first score ever
        {
            clearScores();
            scores = file.getString ("scores", null);
        }

        String [] parts = scores.split ("/"); // splits the scores string
        String [] s; // string for each score
        scoresList = new ArrayList<> ();

        for (int i = 0; i < 5; i++) // initialzies the list from file
        {
            s = parts [i].split ("!");
            Score temp = new Score (s[0], s[2], Integer.parseInt (s[1]), Integer.parseInt (s[3]));
            scoresList.add (temp);
        }

        int current_time = Score.calculateSeconds (((TextView) findViewById (R.id.timer)).getText ().toString ()); // gets the time

        boolean newScore = false; // flag

        for (int i = 0; i < 5; i++) // check if gets into scores
        {
            if (scoresList.get (i).getTime () > current_time) // insert new score
            {
                index = i;
                newScore = true;
                break;
            }
        }

        if (newScore) // indeed, new score at i index
        {
            d = new Dialog (this);
            d.setContentView(R.layout.add_score_name);
            d.findViewById(R.id.ok).setOnClickListener(setName);
            d.setCancelable(false);
            d.show();
        }

        else
            startNewGame(false);
    }

    int index;

    View.OnClickListener setName = new View.OnClickListener () {
        @Override public void onClick (View v) {checkName ();}};


    public void checkName () // name for score
    {
        d.dismiss();

        String name = (((EditText) d.findViewById (R.id.name)).getText ()).toString ();

        if (name.isEmpty ()) // empty name
            name = getResources().getString (R.string.score1);

        for (int a = 0; name.length() != 8; a++) // add spaces
            name += " ";


        String t = ((TextView)(findViewById (R.id.timer))).getText().toString ();
        String c = ((TextView)(findViewById (R.id.set_counter))).getText().toString ();
        int s = Integer.parseInt (c);

        Score newS = new Score (name, t, s, cluesNum);

        scoresList.add (index, newS);
        scoresList.remove (5);

        String str = ""; // initializes the scores file

        for (Score sc : scoresList)
        {
            str += sc.getAll();
            str += "/";
        }

        SharedPreferences file = getSharedPreferences ("MyFile", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit_file = file.edit ();
        edit_file.putString ("scores", str);
        edit_file.commit ();

        showScores ();
    }

    private void showScores()
    {
        SharedPreferences file = getSharedPreferences ("MyFile", Context.MODE_PRIVATE);

        String scores = file.getString ("scores", null);

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

        d = new Dialog (this);
        d.setContentView (R.layout.credits_layout);
        d.setCancelable(false);

        TextView tv = (TextView) d.findViewById (R.id.text);
        tv.setText (str);
        tv.setTextSize(18);

        ((ImageView) d.findViewById (R.id.close_btn)).setImageDrawable (getResources ().getDrawable (R.drawable.smily_tnx));

        LinearLayout l = (LinearLayout) d.findViewById (R.id.l);
        l.setOnClickListener (closeFinalDialog);

        d.show ();
    }



    public void makeEndMsg (String str) // show end message with information
    {
        d = new Dialog (this);
        d.setContentView (R.layout.credits_layout);
        d.setCancelable(false);

        TextView tv = (TextView) d.findViewById (R.id.text);
        tv.setText (str);

        ImageView img = (ImageView) d.findViewById (R.id.close_btn);
        img.setImageDrawable (getResources ().getDrawable (R.drawable.smily_happy));

        LinearLayout l = (LinearLayout) d.findViewById (R.id.l);
        l.setOnClickListener (close);

        d.show ();
    }


    View.OnClickListener close = new View.OnClickListener ()
    {
        @Override
        public void onClick (View v)
        {
            d.dismiss ();

            if (timer.equals ("true")) // if timer, set score
                handleScores ();

            else
                startNewGame (false);
        }
    };

    public void notSet () // not a set
    {
        if (sound.equals ("true"))
            media_no.start ();

        setAlert (getResources().getString(R.string.notset), "", getResources ().getDrawable (R.drawable.smily_youdidit));

        new CountDownTimer (1000, 1000) // delay
        {
            public void onFinish()
            {
                for (Card c : chs3crd)
                {
                    mat[c.getX ()][c.getY ()].setBackgroundColor(0);

                    for (Card c1 : cards)
                    {
                        if (c.getId() == c1.getId())
                        {
                            c1.setChoosen(false);
                            break;
                        }
                    }
                }

                alert.dismiss();
                chs3crd.clear();
            }
            public void onTick(long millisUntilFinished) {}}.start();
    }


    public void set () // there is a set!
    {
        if (sound.equals ("true"))
            media_yes.start ();

        blinkANDdelete ();
    }


    public boolean checkSet(ArrayList<Card> chs3crd) // 3 cards are set?
    {
        if (equalORdiff (chs3crd.get(0).getColor(), chs3crd.get(1).getColor(), chs3crd.get(2).getColor())) // colors
            if (equalORdiff (chs3crd.get(0).getFill(), chs3crd.get(1).getFill(), chs3crd.get(2).getFill())) // fills
                if (equalORdiff (chs3crd.get(0).getShape(), chs3crd.get(1).getShape(), chs3crd.get(2).getShape())) // shapes
                    if (equalORdiff (chs3crd.get(0).getNum(), chs3crd.get(1).getNum(), chs3crd.get(2).getNum())) // nums
                        return true;

        return false;
    }


    public boolean equalORdiff (char c1, char c2, char c3) // checks: equal or different
    {
        if ((c1 == c2) && (c2 == c3))
            return true;

        if ((c1 != c2) && (c2 != c3) && (c1 != c3))
            return true;

        return false;
    }


    public void setCards () // sets 12 begining cards inside the mat, on the screen
    {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 3; j++)
                defineSingleCard (i, j);

        for (int j = 0; j < 3; j++)
            mat [4][j].setTag (null);

    }


    public void defineSingleCard (int i, int j) // make 1 card each time
    {
        Random rnd = new Random ();
        int r;

        do
        {
            r = rnd.nextInt (cards.size ());

        } while (cards.get (r).isOnScrn ()); // while its already on the screen

        setSingleCard (r, i, j, true);
    }


    public void setSingleCard (int r, int i, int j, boolean c15) // update a card in the matrix & cards array & inside the class
    {
        cards.get (r).setOnScrn (true); // sets onScreen variable
        mat[i][j].setImageDrawable (getResources ().getDrawable (cards.get (r).getSrc ())); // set the img
        mat[i][j].setTag (cards.get (r)); // adds the Card obj
        ((Card) mat[i][j].getTag ()).setX (i); // sets the x
        ((Card) mat[i][j].getTag ()).setY (j); // sets the y


        if (c15) // only if its a new card (not only switched)
        {
            TextView cc = (TextView) findViewById (R.id.cards_counter); // update the cards counter
            cc.setText (Integer.toString (Integer.parseInt (cc.getText ().toString ()) - 1));
        }
    }


    public boolean IsSetOnScr (boolean flag, ArrayList<Card> temp, boolean scr) // checks if group of cards has set
    { // flag -> to choose 1 card (for tip). temp -> add more cards to the check list. scr -> check cards from screen

        ArrayList<Card> temp12crd = new ArrayList<>();
        ArrayList<Card> temp3crd = new ArrayList<>();

        if (scr) // add from the screen. almost always (exept rare situation)
        {
            for (Card c : cards)
                if (c.isOnScrn ())
                    temp12crd.add (c); // initilizes the temp list from the screen
        }

        if (temp != null) // adds temp card/s for the check
            for (Card t : temp)
                temp12crd.add (t);


        for (int i = 0; i < temp12crd.size (); i++)
        {
            for (int j = i + 1; j < temp12crd.size (); j++)
            {
                for (int k = j + 1; k < temp12crd.size (); k++)
                {
                    temp3crd.add (temp12crd.get (i));
                    temp3crd.add (temp12crd.get (j));
                    temp3crd.add (temp12crd.get (k));

                    if (checkSet (temp3crd)) // set found
                    {
                        if (flag) // want to choose 1 card of the set (for the tip)
                        {
                            if (cluesNum < 100) // stop at 99...
                                cluesNum++;

                            clickImg (mat[temp3crd.get (0).getX ()][temp3crd.get (0).getY ()]); // set the new card as clicked

                            if (egg) // easter egg...
                            {
                                clickImg (mat[temp3crd.get (1).getX ()][temp3crd.get (1).getY ()]);
                                clickImg (mat[temp3crd.get (2).getX ()][temp3crd.get (2).getY ()]);
                                egg = false;
                            }
                        }

                        if (scr == false) // want to set the 3 cards on screen (adding 3 to 12 that must be a set)//
                        {
                            int y = 0;

                            for (Card t3 : temp3crd)
                            {
                                setSingleCard (cards.indexOf (t3), 4, y, true);
                                y++;
                            }
                            //Toast.makeText (this, "added 3 that makes set", Toast.LENGTH_SHORT).show (); ///////
                        }

                        return true;
                    }

                    else
                        temp3crd.clear ();
                }
            }
        }
        return false;
    }


    public void getTip (View view) // give a set hint
    {
        if (clues.equals ("false")) // settings: no clues
        {
            setAlert (getResources ().getString (R.string.hello), getResources ().getString (R.string.noclues), getResources ().getDrawable (R.drawable.smily_youdidit));

            return;
        }
        removeChsBg ();

        if (! IsSetOnScr (true, null, true)) // really, no set on screen
            setAlert (getResources ().getString (R.string.noset1), getResources ().getString (R.string.noset2), getResources ().getDrawable (R.drawable.smily_sad));
    }


    Dialog alert;

    public void setAlert (String str1, String str2, Drawable dr) // make alert
    {
        alert = new Dialog (this);
        alert.setContentView (R.layout.alert_dialog);

        TextView text = (TextView) alert.findViewById (R.id.text);
        text.setText (str1 + "\n" + str2);

        ImageView img = (ImageView) alert.findViewById (R.id.img);
        img.setImageDrawable (dr);

        alert.show ();

        new CountDownTimer(1000, 2000) // delay
        {
            public void onFinish()
            {
                alert.dismiss();
            }
            public void onTick(long millisUntilFinished) {}
        }.start();
    }


    public void removeChsBg () // unChoose all
    {
        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                if (mat [i][j].getTag () != null) // not empty (less than 12 cards)
                {
                    Card crd = ((Card) mat[i][j].getTag ());

                    if (crd.isChoosen ())
                    {
                        crd.setChoosen (false);
                        mat[i][j].setBackgroundColor (0);
                    }
                }
            }
        }

        if (mat[4][0].getTag () != null) // unChoose also the 4th row
        {
            for (int j = 0; j < 3; j++)
            {
                Card crd = ((Card) mat[4][j].getTag ());

                if (crd.isChoosen ())
                {
                    crd.setChoosen(false);
                    mat[4][j].setBackgroundColor(0);
                }
            }
        }

        chs3crd.clear ();
    }


    public void addCrd (View view) // add 3 more buttons on the screen
    {
        if (mat [4][0].getTag () != null) // 15 cards already on screen
        {
            setAlert (getResources ().getString (R.string.already), "", getResources ().getDrawable (R.drawable.smily_youdidit));
            return;
        }

        if (cards.size () - 12 <= 0)// no more cards
        {
            setAlert (getResources ().getString (R.string.over), "", getResources ().getDrawable (R.drawable.smily_sad));
            return;
        }


        removeChsBg (); // first, unChoose all


        if (IsSetOnScr (false, null, true))// if 12 cards already have a set, add 3 random cards
        {
            for (int i = 0; i < 3; i++)
                defineSingleCard (4, i);

            return;
        }


        // adds 3 new cards, must make sure it creates set

        ArrayList <Card> temp = new ArrayList<>(); // temp single or array of cards to add
        Random rnd = new Random();
        int r;

        // trys to add only 1 card //
        for (Card c : cards)
        {
            if (c.isOnScrn() == false) // takes only from the box
            {
                temp.add(c);

                if (IsSetOnScr(false, temp, true) == true) // yes, we have a set. need to add 2 more random cards
                {
                    r = rnd.nextInt (2); // x == 4, y is random

                    setSingleCard (cards.indexOf(c), 4, r, true); // update the new card

                    for (int y = 0; y < 3; y++) // add 2 more randon cards
                        if (y != r)
                            defineSingleCard (4, y);

                    //Toast.makeText (this, "added 1 at " + Integer.toString (r), Toast.LENGTH_SHORT).show (); ///////

                    return;
                }
            }
        }

        temp.clear ();

        // trys to add 2 cards //
        for (Card c1 : cards)
        {
            if (c1.isOnScrn() == false) // takes only from the box
            {
                temp.add (c1);

                for (Card c2 : cards)
                {
                    temp.add (c2);

                    if (IsSetOnScr(false, temp, true) == true) // we have a set with those 2 cards
                    {
                        int y1, y2, y3;

                        y1 = rnd.nextInt (2); // random for the 1st card

                        do
                        {
                            y2 = rnd.nextInt (2); // random for 2nd card
                        } while (y2 == y1);


                        do
                        {
                            y3 = rnd.nextInt (2); // random for 3rd card
                        } while ((y3 == y2) || (y3 == y1));


                        setSingleCard (cards.indexOf (c1), 4, y1, true);
                        setSingleCard (cards.indexOf (c2), 4, y2, true);

                        defineSingleCard (4, y3);

                        //Toast.makeText (this, "added 2 at " + Integer.toString (y1) + " & " + Integer.toString (y2), Toast.LENGTH_SHORT).show (); ///////

                        return;
                    }
                }

            }
        }

        temp.clear ();

        // have to add set of 3 cards //
        for (Card t : cards) // add to temp all the cards in the box
            if (t.isOnScrn () == false)
                temp.add (t);

        if (IsSetOnScr (false, temp ,false) == false)// if have set in box, ok and add cards. else set a msg & end game
        {
            setAlert (getResources ().getString (R.string.rare1), getResources ().getString (R.string.rare2), getResources ().getDrawable (R.drawable.smily_sad));
            EndGame ("");
        }
    }


    public void goSettings (View view)
    {
        if (timer.equals ("true"))
            clock_timer.cancel ();

        Intent i = new Intent ();

        i.putExtra ("sound", sound);
        i.putExtra ("timer", timer);
        i.putExtra ("clues", clues);

        i.setClass (this, Settings.class);
        startActivity (i);
        this.finish ();
    }


    public void drawTableCardsScrn () // creates the images-matritsa on the screen
    {
        mat = new ImageView[5][3];

        crdLayout = new LinearLayout (this);
        crdLayout.setOrientation (LinearLayout.HORIZONTAL);
        crdLayout.setLayoutParams (new ViewGroup.LayoutParams (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout main = (LinearLayout) findViewById(R.id.cards_layout);
        main.addView (crdLayout);

        ImageView img;

        for (int i = 0; i < 5; i++)
        {
            LinearLayout l = new LinearLayout (this);
            l.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            l.setOrientation(LinearLayout.VERTICAL);
            l.setWeightSum(1);

            crdLayout.addView (l);

            for (int j = 0; j < 3; j++)
            {
                img = new ImageView(this);
                img.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                img.setPadding (4,4,4,4);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins (2,2,2,2);
                img.setLayoutParams(lp);
                img.setOnClickListener(click_lis);

                l.addView (img);
                mat [i][j] = img;
            }
        }
    }


    public void createCards() // creates the card collection at the begining
    {
        int[] images = new int[]{R.drawable.c0, R.drawable.c1, R.drawable.c2, R.drawable.c3, R.drawable.c4, R.drawable.c5, R.drawable.c6, R.drawable.c7, R.drawable.c8, R.drawable.c9, R.drawable.c10, R.drawable.c11, R.drawable.c12, R.drawable.c13, R.drawable.c14, R.drawable.c15, R.drawable.c16, R.drawable.c17, R.drawable.c18, R.drawable.c19, R.drawable.c20, R.drawable.c21, R.drawable.c22, R.drawable.c23, R.drawable.c24, R.drawable.c25, R.drawable.c26, R.drawable.c27, R.drawable.c28, R.drawable.c29, R.drawable.c30, R.drawable.c31, R.drawable.c32, R.drawable.c33, R.drawable.c34, R.drawable.c35, R.drawable.c36, R.drawable.c37, R.drawable.c38, R.drawable.c39, R.drawable.c40, R.drawable.c41, R.drawable.c42, R.drawable.c43, R.drawable.c44, R.drawable.c45, R.drawable.c46, R.drawable.c47, R.drawable.c48, R.drawable.c49, R.drawable.c50, R.drawable.c51, R.drawable.c52, R.drawable.c53, R.drawable.c54, R.drawable.c55, R.drawable.c56, R.drawable.c57, R.drawable.c58, R.drawable.c59, R.drawable.c60, R.drawable.c61, R.drawable.c62, R.drawable.c63, R.drawable.c64, R.drawable.c65, R.drawable.c66, R.drawable.c67, R.drawable.c68, R.drawable.c69, R.drawable.c70, R.drawable.c71, R.drawable.c72, R.drawable.c73, R.drawable.c74, R.drawable.c75, R.drawable.c76, R.drawable.c77, R.drawable.c78, R.drawable.c79, R.drawable.c80};
        cards = new ArrayList <> ();
        chs3crd = new ArrayList <> ();

        Card crd;
        int x = 0;

        for (int i = 0; i < 27; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                int n = (j + 1);

                crd = new Card (images[x], x, 0, 0, (char)n, Card.get_color(x), Card.get_fill(x), Card.get_shape(x));
                cards.add (crd);

                x++;
            }
        }
    }


    public void blinkANDdelete ()
    {
        flag4blink = 0;
        blinkTimer.start();
    }

    int flag4blink; // global variable for the blinking

    final CountDownTimer blinkTimer = new CountDownTimer(2000, 250) { // blinking the set
        @Override
        public void onTick(long millisUntilFinished)
        {
            setListeners (false);

            if (flag4blink == 0)
            {
                for (Card c : chs3crd)
                    for (Card c1 : cards)
                        if (c1.getId() == c.getId())
                            mat[c.getX ()][c.getY ()].setBackgroundColor(getResources ().getColor (R.color.border));

                flag4blink = 1;
            }

            else
            {
                for (Card c : chs3crd)
                    for (Card c1 : cards)
                        if (c1.getId() == c.getId())
                            mat[c.getX ()][c.getY ()].setBackgroundColor(0);


                flag4blink = 0;
            }

        }


        @Override
        public void onFinish() // remove the set cards
        {
            if (cards.size () <= 12)// no more cards at box
            {
                for (Card c : chs3crd) // delete the 3 cards
                {
                    for (Iterator <Card> iterator = cards.iterator (); iterator.hasNext ();) //for //(Card c1 : cards)
                    {
                        Card c1 = iterator.next();

                        if (c1.getId() == c.getId())
                        {
                            mat [c1.getX ()][c1.getY ()].setBackgroundColor (0);
                            mat [c1.getX ()][c1.getY ()].setImageDrawable (getResources ().getDrawable (R.drawable.empty_card));
                            mat [c1.getX ()][c1.getY ()].setTag (null);

                            cards.remove (c1);
                            break;
                        }
                    }
                }


                setListeners (true);
                chs3crd.clear();

                TextView cc = ((TextView) findViewById (R.id.set_counter)); // update the set counter
                cc.setText (Integer.toString (Integer.parseInt (cc.getText ().toString ()) + 1));

                checkIfEnd (); // game over?

                return;
            }


            int c15;
            ArrayList <Integer> locations = new ArrayList<> (); // saves the locations of the deleted cards to be filled later

            if (mat [4][0].getTag () == null) // if only 12 cards on screen
                c15 = 0;
            else
                c15 = 1;


            for (Card c : chs3crd) // delete
            {
                for  (Iterator <Card> iterator = cards.iterator (); iterator.hasNext ();)//(Card c1 : cards)
                {
                    Card c1 = iterator.next();
                    if (c1.getId() == c.getId())
                    {
                        if (c15 == 1 && c1.getX () < 4) // saves the location of deleted card for later
                        {
                            locations.add (c1.getX ());
                            locations.add (c1.getY ());
                        }

                        mat [c1.getX ()][c1.getY ()].setTag (null);
                        cards.remove (c1); // removes the card from cards


                        if (c15 == 0) // if only 12 cards on screen, define a new card
                        {
                            defineSingleCard (c.getX (), c.getY ()); // gets a new card
                            mat[c.getX ()][c.getY ()].setBackgroundColor (0); // cancels the border
                        }

                        break;
                    }
                }
            }


            if (c15 == 1) // 15 cards on screen. make only 12
            {
                if (locations.size () != 0) // the set was not only at thr 4th row
                {
                    int x, y;

                    for (int i = 0; i < locations.size (); i += 2) // gets the locations
                    {
                        x = locations.get (i);
                        y = locations.get (i + 1);

                        for (int j = 0; j < 3; j++) // takes from the 4th row
                        {
                            if (mat [4][j].getTag () != null) // not empty (was part of the set)
                            {
                                setSingleCard (cards.indexOf (mat [4][j].getTag ()), x, y, false); // switch places
                                mat [4][j].setTag (null);
                                mat[4][j].setImageDrawable (null);

                                break;
                            }
                        }
                    }
                }

                for (int i = 0; i < 3; i++) // remove the last row
                {
                    if (mat[4][i].getDrawable () != null)
                    {
                        mat[4][i].setTag (null);
                        mat[4][i].setImageDrawable (null);
                    }
                }
            }


            chs3crd.clear();

            TextView cc = (TextView) findViewById (R.id.set_counter); // update the set counter
            cc.setText (Integer.toString (Integer.parseInt (cc.getText ().toString ())+1));

            setListeners (true);

            if (cards.size() == 12) // game over?
                checkIfEnd ();
        }
    };


    public void setListeners (boolean l) // remove or reback listeners (for blinking)
    {
        if (l) // yes, put back listneres
        {
            for (int i = 0; i < 5; i++) // put back listneres for cards
                for (int j = 0; j < 3; j++)
                    mat[i][j].setOnClickListener (click_lis);


            //return buttons listeners

            View.OnClickListener getTipListener = new View.OnClickListener () {@Override public void onClick (View v) {getTip (v);}};
            findViewById (R.id.tip_btn).setOnClickListener (getTipListener);

            View.OnClickListener addCrdListener = new View.OnClickListener () {@Override public void onClick (View v) {addCrd (v);}};
            findViewById (R.id.add_btn).setOnClickListener (addCrdListener);

            View.OnClickListener backListener = new View.OnClickListener () {@Override public void onClick (View v) {goSettings (v);}};
            findViewById (R.id.back_btn).setOnClickListener (backListener);

            View.OnClickListener getRestrtListener = new View.OnClickListener () {@Override public void onClick (View v) {startAgain (v);}};
            findViewById (R.id.restrart_btn).setOnClickListener (getRestrtListener);

            View.OnClickListener getTimerListener = new View.OnClickListener () {@Override public void onClick (View v) {switchClock (v);}};
            findViewById (R.id.timer).setOnClickListener (getTimerListener);
        }

        else // temporary remove listeners
        {
            for (int i = 0; i < 5; i++)
                for (int j = 0; j < 3; j++)
                    mat[i][j].setOnClickListener (null);


            // cancel buttons listeners

            findViewById (R.id.back_btn).setOnClickListener (null);
            findViewById (R.id.add_btn).setOnClickListener (null);
            findViewById (R.id.tip_btn).setOnClickListener (null);
            findViewById (R.id.restrart_btn).setOnClickListener (null);
            findViewById (R.id.timer).setOnClickListener (null);
        }
    }


    public void cardsCount (View view) // click on cards counter
    {
        Toast.makeText(this, getResources().getString(R.string.box), Toast.LENGTH_SHORT).show();
    }


    public void setsCount (View view) // click on sets counter
    {
        Toast.makeText(this, getResources().getString(R.string.sets), Toast.LENGTH_SHORT).show();
    }


    Dialog d;

    public void showDirections ()
    {
        d = new Dialog (this);
        d.setCancelable (true);
        d.setContentView (R.layout.credits_layout);

        TextView tv = (TextView) d.findViewById (R.id.text);
        tv.setText (getResources ().getString (R.string.wellcome));

        ImageView img = (ImageView) d.findViewById (R.id.close_btn);
        img.setImageDrawable (getResources ().getDrawable (R.drawable.smily_tnx));

        LinearLayout l = (LinearLayout) d.findViewById (R.id.l);
        l.setOnClickListener (closeDirectionDialog);

        d.show ();
    }


    View.OnClickListener closeDirectionDialog = new View.OnClickListener () {
        @Override
        public void onClick (View v)
        {
            d.dismiss ();

            StartGame (true);        }
    };


    View.OnClickListener closeDialog = new View.OnClickListener () {@Override
    public void onClick (View v) {
        d.dismiss ();}};


    View.OnClickListener closeFinalDialog = new View.OnClickListener () {@Override
    public void onClick (View v) {
        d.dismiss ();
        startNewGame(false);
    }};


    // timer functions //

    Handler handler;
    Timer clock_timer;
    long startTime;
    TextView timer_tv;

    public void createTimer ()
    {
        timer_tv = (TextView) findViewById (R.id.timer);
        timer_tv.setVisibility (View.VISIBLE);

        startTime = System.currentTimeMillis();
        handler = new Handler();
        clock_timer = new Timer ();

        clock_timer.scheduleAtFixedRate(new stopperTime (), 0, 50);
    }

    class stopperTime extends TimerTask
    {
        public void run() {

            handler.post(new Runnable() {

                @Override
                public void run()
                {
                    long timePassed = System.currentTimeMillis() - startTime; // time passed from the game start

                    timer_tv.setText(stringOfTime (timePassed)); // Updating the stopper
                }
            });
        }
    }

    public String stringOfTime (long m)
    {
        String str;

        int sec = (int) (m / 1000) % 60 ;
        int min = (int) ((m / (1000*60)) % 60);

        if (min > 59)
            EndGame (getResources ().getString (R.string.end3));

        str = getClockString (min) + ":" + getClockString (sec);

        return str;
    }

    String getClockString(int x)
    {
        if (x < 10)
            return "0" + x;

        return String.valueOf(x);
    }


    boolean clock = false; // show clock or timer

    public void switchClock (View view)
    {
        if (!clock)
        {
            TextView cl = (TextView) findViewById (R.id.timer);
            cl.setBackgroundDrawable (getResources ().getDrawable (R.drawable.clock));
            cl.setTextColor (0);
            clock = true;
        }

        else
        {
            TextView cl = (TextView) findViewById (R.id.timer);
            cl.setBackgroundDrawable (null);
            cl.setTextColor (getResources ().getColor (R.color.black));
            clock = false;
        }
    }
}
