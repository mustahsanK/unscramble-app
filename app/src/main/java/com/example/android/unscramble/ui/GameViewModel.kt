package com.example.android.unscramble.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.android.unscramble.data.MAX_NO_OF_WORDS
import com.example.android.unscramble.data.SCORE_INCREASE
import com.example.android.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {
    private var _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    var userGuess by mutableStateOf("")
        private set

    private lateinit var currentWord: String
    private var usedWords = mutableSetOf<String>()

    init {
        resetGame()
    }

    fun resetGame() {
        usedWords.clear()
        _uiState = MutableStateFlow(GameUiState(currentScrambledWord = pickRandomWordAndShuffle()))
    }

    private fun pickRandomWordAndShuffle(): String {
        currentWord = allWords.random()
        return if (usedWords.contains(currentWord)) {
            pickRandomWordAndShuffle()
        } else {
            usedWords.add(currentWord)
            shuffleCurrentWord(currentWord)
        }
    }

    private fun shuffleCurrentWord(word: String): String {
        val tempWord = word.toCharArray()
        tempWord.shuffle()
        while (word == String(tempWord)) {
            tempWord.shuffle()
        }
        return String(tempWord)
    }

    fun updateUserGuessedWord(guessedWord: String) {
        userGuess = guessedWord
    }

    fun checkUserGuess() {
        if (userGuess.equals(currentWord, ignoreCase = true)) {
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            val updatedCount = _uiState.value.currentWordCount.inc()
            updateGameState(updatedScore, updatedCount)
        } else {
            _uiState.update { currentState->
                currentState.copy(isGuessedWordWrong = true)
            }
        }
    }

    fun skipWord() {
        updateGameState(_uiState.value.score, _uiState.value.currentWordCount.inc())
        updateUserGuessedWord("")
    }

    private fun updateGameState(updatedScore: Int, updatedCount: Int) {
        if (updatedCount == MAX_NO_OF_WORDS) {
            _uiState.update { currentState->
                currentState.copy(
                    isGameOver = true
                )
            }
        } else {
            _uiState.update { currentState->
                currentState.copy(
                    isGuessedWordWrong = false,
                    currentScrambledWord = pickRandomWordAndShuffle(),
                    score = updatedScore,
                    currentWordCount = updatedCount
                )
            }
        }
    }
}