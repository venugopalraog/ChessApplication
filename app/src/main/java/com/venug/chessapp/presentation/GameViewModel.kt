package com.venug.chessapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venug.chessapp.domain.engine.ChessAI
import com.venug.chessapp.domain.engine.GameEngine
import com.venug.chessapp.domain.models.GameState
import com.venug.chessapp.domain.models.Move
import com.venug.chessapp.domain.models.Position
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class GameIntent {
    data class SelectSquare(val position: Position) : GameIntent()
    object Undo : GameIntent()
    object Restart : GameIntent()
}

data class GameViewState(
    val gameState: GameState = GameState(),
    val selectedPosition: Position? = null,
    val validMovesForSelected: List<Position> = emptyList()
)

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameEngine: GameEngine,
    private val chessAI: ChessAI
) : ViewModel() {

    private val _viewState = MutableStateFlow(GameViewState())
    val viewState: StateFlow<GameViewState> = _viewState.asStateFlow()

    fun processIntent(intent: GameIntent) {
        when (intent) {
            is GameIntent.SelectSquare -> handleSquareSelection(intent.position)
            is GameIntent.Undo -> handleUndo()
            is GameIntent.Restart -> handleRestart()
        }
    }

    private fun handleSquareSelection(position: Position) {
        val currentState = _viewState.value
        val game = currentState.gameState
        
        // If a piece is already selected, check if this click is a valid move
        if (currentState.selectedPosition != null && currentState.validMovesForSelected.contains(position)) {
            val piece = game.board.getPiece(currentState.selectedPosition)!!
            val move = Move(
                from = currentState.selectedPosition,
                to = position,
                piece = piece,
                capturedPiece = game.board.getPiece(position)
            )
            val newGameState = gameEngine.applyMove(game, move)
            _viewState.update { 
                it.copy(
                    gameState = newGameState,
                    selectedPosition = null,
                    validMovesForSelected = emptyList()
                )
            }
            
            // Trigger AI if it's currently Black's turn
            if (newGameState.currentPlayer == com.venug.chessapp.domain.models.Player.BLACK && !newGameState.isGameOver) {
                playAITurn(newGameState)
            }
        } else {
            // Otherwise, try to select a piece
            val piece = game.board.getPiece(position)
            if (piece != null && piece.player == game.currentPlayer) {
                val validMoves = gameEngine.getValidMoves(game, position)
                _viewState.update {
                    it.copy(
                        selectedPosition = position,
                        validMovesForSelected = validMoves
                    )
                }
            } else {
                // Clicked on empty square or opponent piece when nothing was selected
                _viewState.update {
                    it.copy(
                        selectedPosition = null,
                        validMovesForSelected = emptyList()
                    )
                }
            }
        }
    }

    private fun playAITurn(currentState: GameState) {
        viewModelScope.launch {
            val bestMove = withContext(Dispatchers.Default) {
                chessAI.getBestMove(currentState, depth = 3)
            }
            if (bestMove != null) {
                val newState = gameEngine.applyMove(currentState, bestMove)
                _viewState.update { it.copy(gameState = newState) }
            }
        }
    }

    private fun handleUndo() {
        // Advanced feature - not implemented in basics yet
    }

    private fun handleRestart() {
        _viewState.update { GameViewState() }
    }
}
