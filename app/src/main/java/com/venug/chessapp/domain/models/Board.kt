package com.venug.chessapp.domain.models

class Board(private val grid: Array<Array<Piece?>> = Array(8) { Array(8) { null } }) {

    init {
        // Initialize standard chess setup if grid is empty
        if (isEmpty()) {
            setupInitialPosition()
        }
    }

    private fun isEmpty(): Boolean {
        for (i in 0 until 8) {
            for (j in 0 until 8) {
                if (grid[i][j] != null) return false
            }
        }
        return true
    }

    private fun setupInitialPosition() {
        val standardOrder = arrayOf(PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN, PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK)

        // Setup Black pieces
        for (i in 0 until 8) {
            grid[0][i] = Piece(Player.BLACK, standardOrder[i])
            grid[1][i] = Piece(Player.BLACK, PieceType.PAWN)
        }

        // Setup White pieces
        for (i in 0 until 8) {
            grid[6][i] = Piece(Player.WHITE, PieceType.PAWN)
            grid[7][i] = Piece(Player.WHITE, standardOrder[i])
        }
    }

    fun getPiece(position: Position): Piece? {
        if (position.row !in 0..7 || position.col !in 0..7) return null
        return grid[position.row][position.col]
    }

    fun setPiece(position: Position, piece: Piece?) {
        if (position.row in 0..7 && position.col in 0..7) {
            grid[position.row][position.col] = piece
        }
    }

    fun copy(): Board {
        val newGrid = Array(8) { row ->
            Array(8) { col ->
                grid[row][col]?.copy()
            }
        }
        return Board(newGrid)
    }
}
