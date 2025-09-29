package com.example.notas;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.notas.databinding.ActivityAddEditNoteBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddEditNoteActivity extends AppCompatActivity {

    private ActivityAddEditNoteBinding binding;
    private NotesViewModel viewModel;
    private long noteId = -1;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding
        binding = ActivityAddEditNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar toolbar
        setupToolbar();

        // Configurar ViewModel
        viewModel = new ViewModelProvider(this).get(NotesViewModel.class);

        // Verificar si estamos editando una nota existente
        checkEditMode();

        // Configurar botón guardar
        binding.btnSave.setOnClickListener(v -> saveNote());
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void checkEditMode() {
        if (getIntent().hasExtra("NOTE_ID")) {
            noteId = getIntent().getLongExtra("NOTE_ID", -1);
            if (noteId != -1) {
                isEditMode = true;
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.edit_note);
                }
                loadNote();
            } else {
                setNewNoteMode();
            }
        } else {
            setNewNoteMode();
        }
    }

    private void setNewNoteMode() {
        isEditMode = false;
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.new_note);
        }
    }

    private void loadNote() {
        viewModel.getNoteById(noteId).observe(this, note -> {
            if (note != null) {
                binding.etTitle.setText(note.title);
                binding.etContent.setText(note.content);
            }
        });
    }

    private void saveNote() {
        String title = binding.etTitle.getText().toString().trim();
        String content = binding.etContent.getText().toString().trim();

        // Validar que el contenido no esté vacío
        if (TextUtils.isEmpty(content)) {
            binding.etContent.setError("El contenido de la nota no puede estar vacío");
            binding.etContent.requestFocus();
            return;
        }

        // Generar título automático si está vacío
        final String finalTitle;
        if (TextUtils.isEmpty(title)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            finalTitle = "Nota " + dateFormat.format(new Date());
        } else {
            finalTitle = title;
        }

        if (isEditMode) {
            updateExistingNote(finalTitle, content);
        } else {
            createNewNote(finalTitle, content);
        }
    }

    private void updateExistingNote(String title, String content) {
        viewModel.getNoteById(noteId).observe(this, note -> {
            if (note != null) {
                note.update(title, content);
                viewModel.update(note);

                showToastAndFinish("Nota actualizada correctamente");
            }
        });
    }

    private void createNewNote(String title, String content) {
        Note newNote = new Note(title, content);
        viewModel.insert(newNote);

        showToastAndFinish("Nota guardada correctamente");
    }

    private void showToastAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Regresar a pantalla principal sin modificaciones (EXTENSIÓN REQUERIDA)
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}