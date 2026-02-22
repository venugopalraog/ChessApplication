package com.venug.chessapp.domain.models

enum class Player {
    WHITE, BLACK;

    fun opponent(): Player = if (this == WHITE) BLACK else WHITE
}

enum class PieceType {
    PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING
}

data class Piece(
    val player: Player,
    val type: PieceType,
    val hasMoved: Boolean = false // Important for Castling and Pawn initial double-step
)

data class Position(val row: Int, val col: Int) {
    init {
        require(row in 0..7 && col in 0..7) { "Position out of bounds: $row, $col" }
    }
}

data class Move(
    val from: Position,
    val to: Position,
    val piece: Piece,
    val capturedPiece: Piece? = null,
    val isEnPassant: Boolean = false,
    val isCastling: Boolean = false,
    val promotionTo: PieceType? = null
)

