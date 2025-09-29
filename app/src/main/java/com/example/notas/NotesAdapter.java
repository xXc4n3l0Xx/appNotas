package com.example.notas;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    private List<Note> notes = new ArrayList<>();
    private OnNoteClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public interface OnNoteClickListener {
        void onNoteClick(Note note, int position);
        void onNoteDoubleClick(Note note);
    }

    public void setOnNoteClickListener(OnNoteClickListener listener) {
        this.listener = listener;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes != null ? notes : new ArrayList<>();
        selectedPosition = RecyclerView.NO_POSITION; // Reset selection
        notifyDataSetChanged();
    }

    public Note getSelectedNote() {
        if (selectedPosition != RecyclerView.NO_POSITION && selectedPosition < notes.size()) {
            return notes.get(selectedPosition);
        }
        return null;
    }

    public void clearSelection() {
        int oldPosition = selectedPosition;
        selectedPosition = RecyclerView.NO_POSITION;
        if (oldPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(oldPosition);
        }
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note currentNote = notes.get(position);
        holder.bind(currentNote, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private TextView dateTextView;
        private long lastClickTime = 0;
        private static final long DOUBLE_CLICK_TIME_DELTA = 300; // milliseconds

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tvTitle);
            dateTextView = itemView.findViewById(R.id.tvDate);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    long currentTime = System.currentTimeMillis();

                    // Detectar doble clic
                    if (currentTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                        // Es doble clic
                        listener.onNoteDoubleClick(notes.get(position));
                        return;
                    }
                    lastClickTime = currentTime;

                    // Manejar selección simple
                    int oldPosition = selectedPosition;

                    if (selectedPosition == position) {
                        // Deseleccionar si ya está seleccionado
                        selectedPosition = RecyclerView.NO_POSITION;
                    } else {
                        // Seleccionar nueva posición
                        selectedPosition = position;
                    }

                    // Actualizar UI
                    if (oldPosition != RecyclerView.NO_POSITION) {
                        notifyItemChanged(oldPosition);
                    }
                    if (selectedPosition != RecyclerView.NO_POSITION) {
                        notifyItemChanged(selectedPosition);
                    }

                    listener.onNoteClick(notes.get(position), position);
                }
            });
        }

        public void bind(Note note, boolean isSelected) {
            titleTextView.setText(note.title != null && !note.title.trim().isEmpty()
                    ? note.title
                    : "Sin título");

            if (note.createdAt != null) {
                dateTextView.setText(dateFormat.format(note.createdAt));
            } else {
                dateTextView.setText("Fecha desconocida");
            }

            // Aplicar estilo de selección
            if (isSelected) {
                itemView.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.selected_item));
                itemView.setSelected(true);
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT);
                itemView.setSelected(false);
            }
        }
    }
}