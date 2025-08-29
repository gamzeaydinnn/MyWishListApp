package eu.tutorials.mywishlistapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.tutorials.mywishlistapp.data.Wish
import eu.tutorials.mywishlistapp.data.WishRepository
import eu.tutorials.mywishlistapp.data.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class WishViewModel( private val wishRepository:WishRepository=Graph.wishRepository
):ViewModel(){


    var wishTitleState by mutableStateOf("")
    var wishDescriptionState by mutableStateOf("")
    var wishPriorityState by mutableStateOf(Priority.LOW)

    // Sıralama tercihi
    var sortByPriority by mutableStateOf(false)


    fun onWishTitleChanged(newString:String){
        wishTitleState = newString
    }

    fun onWishDescriptionChanged(newString: String){
        wishDescriptionState = newString
    }

    fun onWishPriorityChanged(newPriority: Priority){
        wishPriorityState = newPriority
    }

    fun toggleSortByPriority() {
        sortByPriority = !sortByPriority
        getAllWishes = if (sortByPriority) {
            wishRepository.getWishesByPriority()
        } else {
            wishRepository.getWishes()
        }
    }


    lateinit var getAllWishes: Flow<List<Wish>>
    init{
        viewModelScope.launch {
            getAllWishes=wishRepository.getWishes()
        }
    }
    fun addWish(wish:Wish){
        viewModelScope.launch(Dispatchers.IO) {
            wishRepository.addWish(wish = wish)
        }
    }

    fun getAWishById(id:Long):Flow<Wish>{
        return wishRepository.getAWishById(id)
    }

    fun updateWish(wish: Wish){
        viewModelScope.launch(Dispatchers.IO) {
            wishRepository.updateAWish(wish = wish)
        }
    }
    fun deleteWish(wish:Wish){
        viewModelScope.launch(Dispatchers.IO)  {
            wishRepository.deleteAWish(wish=wish)
        }
    }
}