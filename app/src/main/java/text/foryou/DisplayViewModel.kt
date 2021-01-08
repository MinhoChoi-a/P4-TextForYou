package text.foryou

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import text.foryou.data.AppDatabase
import text.foryou.data.NoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DisplayViewModel(app: Application): AndroidViewModel(app) {

    private val database = AppDatabase.getInstance(app)
    val currentNote = MutableLiveData<NoteEntity>()

    fun getNoteById(noteId: Int) {

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val note =
                        if(noteId != NEW_NOTE_ID) {
                            database?.noteDao()?.getNoteById(noteId)
                        }
                        else{
                            NoteEntity()
                        }
                currentNote.postValue(note)
            }
        }
    }

    fun getContext(): Context {
        return getApplication()
    }

}