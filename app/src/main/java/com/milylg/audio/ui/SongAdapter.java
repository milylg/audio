package com.milylg.audio.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.milylg.audio.databinding.ItemSongBinding;
import com.milylg.audio.viewmodel.AudioViewModel;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongItemViewHolder> {

    private final List<SongItem> songItems;
    private final AudioItemClickAction audioItemClickAction;
    private final AudioViewModel audioViewModel;

    public SongAdapter(AudioViewModel vm, AudioItemClickAction audioItemClickAction) {
        this.audioViewModel = vm;
        this.audioItemClickAction = audioItemClickAction;
        songItems = new ArrayList<>();
    }

    @NonNull
    @Override
    public SongItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Get Binding from Cache.
        ItemSongBinding binding = DataBindingUtil.getBinding(parent);
        // Create Binding if cache without it.
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        binding = ItemSongBinding.inflate(inflater, parent, false);
        binding.setClickAction(audioItemClickAction);
        binding.executePendingBindings();
        return new SongItemViewHolder(binding);
    }

    public void flushData(@NonNull List<SongItem> songs) {
        if (songs.isEmpty()) {
            return;
        }
        songItems.clear();
        songItems.addAll(songs);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.SongItemViewHolder holder,
                                 int position) {
        SongItem songItem = songItems.get(position);
        holder.getBinding().setItem(songItem);
        holder.getBinding().executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return songItems.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class SongItemViewHolder extends RecyclerView.ViewHolder {
        private final ItemSongBinding itemSongBinding;

        public SongItemViewHolder(ItemSongBinding binding) {
            super(binding.getRoot());
            this.itemSongBinding = binding;
        }

        public ItemSongBinding getBinding() {
            return itemSongBinding;
        }
    }
}
