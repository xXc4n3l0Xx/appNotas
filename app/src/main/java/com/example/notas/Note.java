package com.example.notas;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "notes")
public class Note {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title;
    public String content;
    public Date createdAt;
    public Date updatedAt;

    public Note(String title, String content) {
        this.title = title;
        this.content = content;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Constructor vacío requerido por Room
    public Note() {}

    // Método helper para actualizar
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
        this.updatedAt = new Date();
    }
}