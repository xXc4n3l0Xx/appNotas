package com.example.notas;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.notas.databinding.ActivityMainBinding;
import model.Note;
import vm.NotesViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NotesViewModel vm;

    private static final String PREFS_NAME = "notes_prefs";
    private static final String KEY_NOTES = "notes_serialized";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ViewModel
        vm = new ViewModelProvider(this).get(NotesViewModel.class);

        // Observa los cambios para refrescar UI (contador y listado)
        vm.getNotes().observe(this, notes -> {
            binding.txtCount.setText(getString(R.string.count_template, notes.size()));
            binding.txtNotes.setText(formatNotes(notes));
        });

        // Cargar de SharedPreferences SOLO si el ViewModel está vacío (primera vez)
        List<Note> current = vm.getNotes().getValue();
        if (current == null || current.isEmpty()) {
            List<Note> loaded = loadFromPrefs();
            if (!loaded.isEmpty()) vm.setNotes(loaded);
        }

        // Guardar nota
        binding.btnAdd.setOnClickListener(v -> {
            String text = binding.edtNote.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "Escribe una nota", Toast.LENGTH_SHORT).show();
                return;
            }
            vm.addNote(text);
            binding.edtNote.setText("");
        });

        // Limpiar todas las notas
        binding.btnClear.setOnClickListener(v -> vm.clear());
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Persistir siempre que la Activity pase a segundo plano (incluye cierre)
        List<Note> notes = vm.getNotes().getValue();
        saveToPrefs(notes != null ? notes : new ArrayList<>());
    }

    // --- Serialización simple (id|texto + \n por cada nota) ---

    private void saveToPrefs(List<Note> notes) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_NOTES, serialize(notes)).apply();
    }

    private List<Note> loadFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String data = prefs.getString(KEY_NOTES, "");
        return deserialize(data);
    }

    private String serialize(List<Note> notes) {
        StringBuilder sb = new StringBuilder();
        for (Note n : notes) {
            // Evita romper el formato: reemplaza saltos y la barra vertical
            String safeText = n.text.replace("\n", " ").replace("|", " ");
            sb.append(n.id).append("|").append(safeText).append("\n");
        }
        return sb.toString();
    }

    private List<Note> deserialize(String data) {
        List<Note> list = new ArrayList<>();
        if (data == null || data.isEmpty()) return list;

        String[] lines = data.split("\n");
        for (String line : lines) {
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\|", 2);
            if (parts.length == 2) {
                list.add(new Note(parts[0], parts[1]));
            }
        }
        return list;
    }

    private String formatNotes(List<Note> notes) {
        StringBuilder sb = new StringBuilder();
        for (Note n : notes) {
            String shortId = n.id.length() >= 8 ? n.id.substring(0, 8) : n.id;
            sb.append("• [").append(shortId).append("] ").append(n.text).append("\n");
        }
        return sb.toString();
    }
}
