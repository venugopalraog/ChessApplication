package com.venug.chessapp.domain.engine

import com.venug.chessapp.domain.models.*
import kotlin.math.abs

import javax.inject.Inject

class GameEngine @Inject constructor() {

    fun getValidMoves(state: GameState, position: Position): List<Position> {
        val board = state.board
        val piece = board.getPiece(position) ?: return emptyList()

        if (piece.player != state.currentPlayer) return emptyList()

        val pseudoLegalMoves = getPseudoLegalMoves(board, position, piece)

        // Filter out moves that leave the king in check
        return pseudoLegalMoves.filter { movePosition ->
            val move = Move(position, movePosition, piece, board.getPiece(movePosition))
            val nextState = applyMoveSafely(state, move, validateCheck = false)
            !isCheck(nextState.board, piece.player)
        }
    }

    private fun getPseudoLegalMoves(board: Board, pos: Position, piece: Piece): List<Position> {
        return when (piece.type) {
            PieceType.PAWN -> getPawnMoves(board, pos, piece)
            PieceType.KNIGHT -> getKnightMoves(board, pos, piece)
            PieceType.BISHOP -> getBishopMoves(board, pos, piece)
            PieceType.ROOK -> getRookMoves(board, pos, piece)
            PieceType.QUEEN -> getQueenMoves(board, pos, piece)
            PieceType.KING -> getKingMoves(board, pos, piece)
        }
    }

    private fun getPawnMoves(board: Board, pos: Position, piece: Piece): List<Position> {
        val moves = mutableListOf<Position>()
        val direction = if (piece.player == Player.WHITE) -1 else 1
        
        // Forward 1
        val f1Row = pos.row + direction
        val f1Col = pos.col
        if (f1Row in 0..7) {
            val forward1 = Position(f1Row, f1Col)
            if (board.getPiece(forward1) == null) {
                moves.add(forward1)
                
                // Forward 2 (only if starting pos)
                val isStartingRow = (piece.player == Player.WHITE && pos.row == 6) || (piece.player == Player.BLACK && pos.row == 1)
                if (isStartingRow) {
                    val f2Row = pos.row + direction * 2
                    val forward2 = Position(f2Row, f1Col)
                    if (board.getPiece(forward2) == null) {
                        moves.add(forward2)
                    }
                }
            }
        }

        // Capture diagonal
        for (colOffset in listOf(-1, 1)) {
            val dRow = pos.row + direction
            val dCol = pos.col + colOffset
            if (dRow in 0..7 && dCol in 0..7) {
                val diagPos = Position(dRow, dCol)
                val target = board.getPiece(diagPos)
                if (target != null && target.player != piece.player) {
                    moves.add(diagPos)
                }
            }
        }
        
        // TODO: En Passant
        return moves
    }

    private fun getKnightMoves(board: Board, pos: Position, piece: Piece): List<Position> {
        val moves = mutableListOf<Position>()
        val offsets = listOf(
            Pair(-2, -1), Pair(-2, 1), Pair(-1, -2), Pair(-1, 2),
            Pair(1, -2), Pair(1, 2), Pair(2, -1), Pair(2, 1)
        )
        for ((dr, dc) in offsets) {
            val r = pos.row + dr
            val c = pos.col + dc
            if (r in 0..7 && c in 0..7) {
                val target = board.getPiece(Position(r, c))
                if (target == null || target.player != piece.player) {
                    moves.add(Position(r, c))
                }
            }
        }
        return moves
    }

    private fun getSlidingMoves(board: Board, pos: Position, piece: Piece, dirs: List<Pair<Int, Int>>): List<Position> {
        val moves = mutableListOf<Position>()
        for ((dr, dc) in dirs) {
            var r = pos.row + dr
            var c = pos.col + dc
            while (r in 0..7 && c in 0..7) {
                val p = Position(r, c)
                val target = board.getPiece(p)
                if (target == null) {
                    moves.add(p)
                } else {
                    if (target.player != piece.player) moves.add(p)
                    break
                }
                r += dr
                c += dc
            }
        }
        return moves
    }

    private fun getBishopMoves(board: Board, pos: Position, piece: Piece): List<Position> {
        val dirs = listOf(Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1))
        return getSlidingMoves(board, pos, piece, dirs)
    }

    private fun getRookMoves(board: Board, pos: Position, piece: Piece): List<Position> {
        val dirs = listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1))
        return getSlidingMoves(board, pos, piece, dirs)
    }

    private fun getQueenMoves(board: Board, pos: Position, piece: Piece): List<Position> {
        val dirs = listOf(
            Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1),
            Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1)
        )
        return getSlidingMoves(board, pos, piece, dirs)
    }

    private fun getKingMoves(board: Board, pos: Position, piece: Piece): List<Position> {
        val moves = mutableListOf<Position>()
        val dirs = listOf(
            Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1),
            Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1)
        )
        for ((dr, dc) in dirs) {
            val r = pos.row + dr
            val c = pos.col + dc
            if (r in 0..7 && c in 0..7) {
                val p = Position(r, c)
                val target = board.getPiece(p)
                if (target == null || target.player != piece.player) {
                    moves.add(p)
                }
            }
        }
        // TODO: Castling
        return moves
    }

    fun applyMove(state: GameState, move: Move): GameState {
        return applyMoveSafely(state, move, validateCheck = true)
    }

    private fun applyMoveSafely(state: GameState, move: Move, validateCheck: Boolean): GameState {
        val newBoard = state.board.copy()
        val pieceToMove = move.piece.copy(hasMoved = true)
        
        newBoard.setPiece(move.from, null)
        newBoard.setPiece(move.to, pieceToMove)

        val nextPlayer = state.currentPlayer.opponent()
        
        val isChecking = if (validateCheck) isCheck(newBoard, nextPlayer) else false
        val isCheckmating = if (validateCheck && isChecking) isCheckmate(newBoard, nextPlayer) else false
        val isStalemating = if (validateCheck && !isChecking) isStalemate(newBoard, nextPlayer) else false

        return state.copy(
            board = newBoard,
            currentPlayer = nextPlayer,
            moveHistory = state.moveHistory + move,
            isCheck = isChecking,
            isCheckmate = isCheckmating,
            isStalemate = isStalemating,
            winner = if (isCheckmating) state.currentPlayer else null
        )
    }

    private fun isCheck(board: Board, player: Player): Boolean {
        // 1. Find the player's king
        var kingPos: Position? = null
        for (r in 0..7) {
            for (c in 0..7) {
                val p = board.getPiece(Position(r, c))
                if (p != null && p.player == player && p.type == PieceType.KING) {
                    kingPos = Position(r, c)
                    break
                }
            }
            if (kingPos != null) break
        }
        if (kingPos == null) return false // Should logically not happen in valid chess

        // 2. See if any opponent piece can attack the king's position
        val opponent = player.opponent()
        for (r in 0..7) {
            for (c in 0..7) {
                val oppPos = Position(r, c)
                val p = board.getPiece(oppPos)
                if (p != null && p.player == opponent) {
                    val pseudoMoves = getPseudoLegalMoves(board, oppPos, p)
                    if (pseudoMoves.contains(kingPos)) return true
                }
            }
        }
        return false
    }

    private fun isCheckmate(board: Board, player: Player): Boolean {
        if (!isCheck(board, player)) return false
        return !playerHasAnyValidMoves(board, player)
    }

    private fun isStalemate(board: Board, player: Player): Boolean {
        if (isCheck(board, player)) return false
        return !playerHasAnyValidMoves(board, player)
    }

    private fun playerHasAnyValidMoves(board: Board, player: Player): Boolean {
        val tempState = GameState(board = board, currentPlayer = player)
        for (r in 0..7) {
            for (c in 0..7) {
                val pos = Position(r, c)
                val p = board.getPiece(pos)
                if (p != null && p.player == player) {
                    if (getValidMoves(tempState, pos).isNotEmpty()) {
                        return true
                    }
                }
            }
        }
        return false
    }
}
