package com.avital.set;

public class Card
{
    private int src;
    private int id;
    private int x;
    private int y;
    private char num;
    private char color;
    private char fill;
    private char shape;
    private boolean choosen;
    private boolean onScrn;


    public Card (int src, int id, int x, int y, char num, char color, char fill, char shape)
    {
        this.src = src;
        this.id = id;
        this.x = x;
        this.y = y;
        this.num = num;
        this.color = color;
        this.fill = fill;
        this.shape = shape;
        choosen = false;
        onScrn = false;
    }


    //getter & setter methods

    public boolean isOnScrn() {return onScrn;}

    public int getId() {
        return id;
    }

    public int getSrc() {
        return src;
    }

    public char getNum() {
        return num;
    }

    public char getColor() {
        return color;
    }

    public char getFill() {
        return fill;
    }

    public char getShape() {
        return shape;
    }

    public boolean isChoosen() {return choosen;}

    public void setChoosen(boolean b) {choosen = b;}

    public void setOnScrn(boolean b) {onScrn = b;}

    public int getX () {return x;}

    public int getY () {return y;}

    public void setX (int x) {this.x = x;}

    public void setY (int y) {this.y = y;}


    // design checkings
    static public char get_fill(int i) // returns card fill by id
    {
        if ((i >= 0 && i <= 8) ||
                (i >= 27 && i <= 35) ||
                (i >= 54 && i <= 62))
            return 'e';

        if ((i >= 9 && i <= 17) ||
                (i >= 36 && i <= 44) ||
                (i >= 63 && i <= 71))
            return 'l';

        else
            return 'f';
    }


    static public char get_color(int i) // returns card color by id
    {
        if (i >= 0 && i <= 26)
            return 'r';

        if (i > 26 && i < 54)
            return 'g';

        else
            return 'p';
    }


    static public char get_shape(int id) // returns card shape by id
    {
        for (int i = 0; i < 81; i += 9) {
            for (int j = 0; j < 3; j++) {
                if (j + i == id)
                    return 'm';
            }
        }

        for (int i = 3; i < 81; i += 9) {
            for (int j = 0; j < 3; j++) {
                if (j + i == id)
                    return 'e';
            }
        }

        return 'w';
    }
}
