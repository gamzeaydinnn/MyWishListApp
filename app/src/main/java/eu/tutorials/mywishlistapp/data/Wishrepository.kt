package eu.tutorials.mywishlistapp.data

import kotlinx.coroutines.flow.Flow

class WishRepository (private val wishDao:WishDao){
    suspend fun addWish(wish:Wish){
        wishDao.addWish(wish)

    }
    fun getWishes(): Flow<List<Wish>> = wishDao.getAllWishes()

    fun getWishesByPriority(): Flow<List<Wish>> = wishDao.getAllWishesByPriority()

    fun getAWishById(id:Long) :Flow<Wish> {
        return wishDao.getWishById(id)
    }

    suspend fun updateAWish(wish: Wish){
        wishDao.updateAWish(wish)
    }
    suspend fun  deleteAWish(wish: Wish){
        wishDao.deleteAWish(wish)
    }
}