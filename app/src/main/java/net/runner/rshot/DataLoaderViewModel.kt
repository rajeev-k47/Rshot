package net.runner.rshot

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

data class DataClass(val tag:String, val image:String)
class DataLoaderViewModel : ViewModel() {
    private val _dataLoaded = MutableLiveData(false)
    val dataLoaded: LiveData<Boolean> get() = _dataLoaded

    private val _fetchedData = MutableLiveData<List<DataClass>>()
    val fetchedData: LiveData<List<DataClass>> get() = _fetchedData

    init {
        viewModelScope.launch {
            loadDataFromDatabase()
            _dataLoaded.postValue(true)
        }
    }

    private suspend fun loadDataFromDatabase() {
        val db = Firebase.firestore
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val dataList = mutableListOf<DataClass>()
                for (document in result) {
                    for ((key, value) in document.data) {
                        dataList.add(DataClass(key, value.toString()))
                    }
                    Log.d("TAG", "${document.id} => ${document.data} ,${document.data.keys}")
                }
                _fetchedData.postValue(dataList)
            }
            .addOnFailureListener { exception ->
                Log.w("TAG", "Error getting documents.", exception)
                _fetchedData.postValue(emptyList())
            }
    }
}
