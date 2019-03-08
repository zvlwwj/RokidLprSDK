package com.rokid.camera.lpr;

import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

public class LPRModel {
    public class LP {
        private Rect rect;
        private String name;

        public LP(Rect r, String n)
        {
            rect = r;
            name = n;
        }

        public void setRect(Rect r)
        {
            rect = r;
        }

        public void setName(String n)
        {
            name = n;
        }

        public Rect getRect()
        {
            return rect;
        }
        public String getName()
        {
            return name;
        }
    }

    private List<LP> lps = new ArrayList<>();

    public void addLP(LP lp)
    {
        if (lps != null) {
            lps.add(lp);
        }
    }

    public void clear()
    {
        if (lps != null) {
            lps.clear();
        }
    }

    public int size()
    {
        if (lps != null){
            return lps.size();
        }
        return 0;
    }

    public LP getLP(int index)
    {
        if (lps != null && index < lps.size() && index >= 0)
        {
            return lps.get(index);
        }
        return null;
    }
}
