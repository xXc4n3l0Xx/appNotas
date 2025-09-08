package vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import model.Note;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NotesViewModel extends ViewModel {

    private final MutableLiveData<List<Note>> notes = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Note>> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> newNotes) {
        notes.setValue(newNotes != null ? newNotes : new ArrayList<>());
    }

    public void addNote(String text) {
        List<Note> current = new ArrayList<>(notes.getValue());
        current.add(new Note(UUID.randomUUID().toString(), text));
        notes.setValue(current);
    }

    public void clear() {
        notes.setValue(new ArrayList<>());
    }
}
