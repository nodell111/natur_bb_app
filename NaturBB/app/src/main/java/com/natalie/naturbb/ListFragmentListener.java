package com.natalie.naturbb;

// ListFragmentListener.java

import android.database.Cursor;
import android.widget.ArrayAdapter;

public interface ListFragmentListener {

    void setListFragmentListener(ListFragmentListener listener);

    void handleSearch(String query);

    ArrayAdapter<CharSequence> createAdapterHtml(Cursor cursor);
}

