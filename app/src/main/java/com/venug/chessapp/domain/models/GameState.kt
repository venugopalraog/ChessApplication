package com.venug.chessapp.domain.models

data class GameState(
    val board: Board = Board(),
    val currentPlayer: Player = Player.WHITE,
    val moveHistory: List<Move> = emptyList(),
    val isCheck: Boolean = false,
    val isCheckmate: Boolean = false,
    val isStalemate: Boolean = false,
    val winner: Player? = null
) {
    val isGameOver: Boolean
        get() = isCheckmate || isStalemate
}
