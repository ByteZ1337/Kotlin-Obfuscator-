package xyz.xenondevs.vetric.utils

object StringUtils {
    
    fun encrypt(text: String, key: String) = text.mapIndexed { index, c ->
        (c.toInt() xor key[index % key.length].toInt()).toChar()
    }.joinToString("")
    
}