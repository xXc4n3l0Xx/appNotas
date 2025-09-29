package com.example.notas;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.notas.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity implements NotesAdapter.OnNoteClickListener {

    private ActivityMainBinding binding;
    private NotesViewModel viewModel;
    private NotesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar toolbar (EXTENSIÓN REQUERIDA)
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mis Notas");
        }

        // ViewModel
        viewModel = new ViewModelProvider(this).get(NotesViewModel.class);

        // Configurar RecyclerView (EXTENSIÓN REQUERIDA)
        setupRecyclerView();

        // Observar cambios en las notas
        observeNotes();
    }

    private void setupRecyclerView() {
        adapter = new NotesAdapter();
        adapter.setOnNoteClickListener(this);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    private void observeNotes() {
        viewModel.getAllNotes().observe(this, notes -> {
            adapter.setNotes(notes);

            // Actualizar contador
            if (notes != null) {
                binding.tvNotesCount.setText(getString(R.string.count_template, notes.size()));
            }

            // Mostrar mensaje si no hay notas
            if (notes == null || notes.isEmpty()) {
                binding.tvEmptyMessage.setText(R.string.no_notes_message);
                binding.tvEmptyMessage.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.tvEmptyMessage.setVisibility(android.view.View.GONE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflar menú con acciones: agregar y eliminar (EXTENSIÓN REQUERIDA)
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_note) {
            // Acción agregar nota - Abrir AddEditNoteActivity (EXTENSIÓN REQUERIDA)
            Intent intent = new Intent(this, AddEditNoteActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_delete_note) {
            // Acción eliminar nota (EXTENSIÓN REQUERIDA)
            deleteSelectedNote();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteSelectedNote() {
        Note selectedNote = adapter.getSelectedNote();

        // Validar que hay una selección (EXTENSIÓN REQUERIDA)
        if (selectedNote == null) {
            Toast.makeText(this, "Selecciona una nota para eliminar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Preguntar confirmación con Snackbar (EXTENSIÓN REQUERIDA)
        Snackbar.make(binding.getRoot(),
                        "¿Eliminar la nota \"" + selectedNote.title + "\"?",
                        Snackbar.LENGTH_LONG)
                .setAction("ELIMINAR", v -> {
                    // Eliminar registro de BD y actualizar lista (EXTENSIÓN REQUERIDA)
                    viewModel.delete(selectedNote);
                    adapter.clearSelection();
                    Toast.makeText(this, "Nota eliminada", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    @Override
    public void onNoteClick(Note note, int position) {
        // Solo manejar selección visual para eliminar
        // El adapter ya maneja la selección internamente
    }

    @Override
    public void onNoteDoubleClick(Note note) {
        // Doble clic para editar - Abrir AddEditNoteActivity (EXTENSIÓN REQUERIDA)
        Intent intent = new Intent(this, AddEditNoteActivity.class);
        intent.putExtra("NOTE_ID", note.id);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Limpiar selección cuando regresamos a la actividad
        if (adapter != null) {
            adapter.clearSelection();
        }
    }
}