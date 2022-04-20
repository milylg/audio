package com.milylg.editarea.ui;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SongListBinding {

    @SuppressWarnings("unchecked")
    @BindingAdapter("songItems")
    public static void setSongItems(RecyclerView listView, List<SongItem> items) {
        SongAdapter adapter = (SongAdapter) listView.getAdapter();
        if (adapter != null) {
            adapter.flushData(items);
        }
    }
}