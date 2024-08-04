package net.runner.rshot

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import javax.security.auth.Subject

data class DataClass(val tag:String, val image:String,val time:String,val subject: String)
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
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        db.collection("users").document(userId!!).collection("data")
            .get()
            .addOnSuccessListener { result ->
                val dataList = mutableListOf<DataClass>()
                for (document in result) {
                    val data = document.data
                    val tag = data["imageName"] as? String ?: ""
                    val image = data["imageUrl"] as? String ?: ""
                    val time = data["imageTime"] as? String ?: ""
                    val subject = data["imageSubject"] as? String ?: ""
                    if (tag.isNotEmpty() && image.isNotEmpty() && time.isNotEmpty()) {
                        dataList.add(DataClass(tag, image, time,subject))
                    }

                    Log.d("TAG", "${document.id} => ${data}")
                }
                _fetchedData.postValue(dataList)
            }
            .addOnFailureListener { exception ->
                Log.w("TAG", "Error getting documents.", exception)
                _fetchedData.postValue(emptyList())
            }
    }
}
