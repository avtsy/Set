package com.avital.set;

public class Score

{
    private String name; // name
    private int sets; // how many sets
    private String str_time; // string: 00:00
    private int clues; // number of clues
    private int time;


    public Score ()
    {
        setName ("--------");
        setStr_time ("99:99");
        setSets (0);
        setClues (0);
        time = 0;
    }

    public Score (String n, String t, int s, int c)
    {
        setName (n);
        setStr_time (t);
        setSets (s);
        setClues (c);
        time = calculateSeconds ();
    }

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public int getSets ()
    {
        return sets;
    }

    public int getTime ()
    {
        return time;
    }

    public void setSets (int sets)
    {
        this.sets = sets;
    }

    public String getStr_time ()
    {
        return str_time;
    }

    public void setStr_time (String str_time)
    {
        this.str_time = str_time;
    }

    public int getClues ()
    {
        return clues;
    }

    public void setClues (int clues)
    {
        this.clues = clues;
    }


    public String getAll ()
    {
        return getName () + "!" + Integer.toString (getSets ()) + "!" + getStr_time () + "!" + Integer.toString (getClues ());
    }

    public int calculateSeconds ()
    {
        int x = (getStr_time ().charAt (4)) - '0';
        x += 10 * ((getStr_time ().charAt (3)) - '0');
        x += 60 * ((getStr_time ().charAt (1)) - '0');
        x += 600 * ((getStr_time ().charAt (0)) - '0');

        return x;
    }


    public static int calculateSeconds (String getStr_time)
    {
        int x = (getStr_time.charAt (4)) - '0';
        x += 10 * ((getStr_time.charAt (3)) - '0');
        x += 60 * ((getStr_time.charAt (1)) - '0');
        x += 600 * ((getStr_time.charAt (0)) - '0');

        return x;
    }




}