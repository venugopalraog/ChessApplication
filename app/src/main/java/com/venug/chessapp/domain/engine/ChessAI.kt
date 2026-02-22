package com.venug.chessapp.domain.engine

import com.venug.chessapp.domain.models.*
import javax.inject.Inject

class ChessAI @Inject constructor(private val gameEngine: GameEngine) {

    // Simple piece values for heuristic evaluation
    private val pieceValues = mapOf(
        PieceType.PAWN to 10,
        PieceType.KNIGHT to 30,
        PieceType.BISHOP to 30,
        PieceType.ROOK to 50,
        PieceType.QUEEN to 90,
        PieceType.KING to 900
    )

    fun getBestMove(gameState: GameState, depth: Int = 2): Move? {
        val player = gameState.currentPlayer
        val allValidMoves = getAllValidMoves(gameState, player)
        
        if (allValidMoves.isEmpty()) return null
        
        var bestMove = allValidMoves.first()
        var bestEval = Int.MIN_VALUE
        
        for (move in allValidMoves) {
            val nextState = gameEngine.applyMove(gameState, move)
            val eval = minimax(nextState, depth - 1, Int.MIN_VALUE, Int.MAX_VALUE, false)
            
            if (eval > bestEval) {
                bestEval = eval
                bestMove = move
            }
        }
        
        return bestMove
    }

    private fun minimax(state: GameState, depth: Int, alpha: Int, beta: Int, isMaximizing: Boolean): Int {
        if (depth == 0 || state.isGameOver) {
            return evaluateBoard(state, state.currentPlayer)
        }
        
        var currentAlpha = alpha
        var currentBeta = beta
        
        if (isMaximizing) {
            var maxEval = Int.MIN_VALUE
            val moves = getAllValidMoves(state, state.currentPlayer)
            for (move in moves) {
                val nextState = gameEngine.applyMove(state, move)
                val eval = minimax(nextState, depth - 1, currentAlpha, currentBeta, false)
                maxEval = maxOf(maxEval, eval)
                currentAlpha = maxOf(currentAlpha, eval)
                if (currentBeta <= currentAlpha) break // Alpha-Beta Pruning
            }
            return maxEval
        } else {
            var minEval = Int.MAX_VALUE
            val moves = getAllValidMoves(state, state.currentPlayer)
            for (move in moves) {
                val nextState = gameEngine.applyMove(state, move)
                val eval = minimax(nextState, depth - 1, currentAlpha, currentBeta, true)
                minEval = minOf(minEval, eval)
                currentBeta = minOf(currentBeta, eval)
                if (currentBeta <= currentAlpha) break // Alpha-Beta Pruning
            }
            return minEval
        }
    }

    private fun evaluateBoard(state: GameState, maximizingPlayer: Player): Int {
        if (state.isCheckmate) {
            return if (state.winner == maximizingPlayer) 10000 else -10000
        }
        if (state.isStalemate) return 0

        var score = 0
        for (r in 0..7) {
            for (c in 0..7) {
                val piece = state.board.getPiece(Position(r, c))
                if (piece != null) {
                    val value = pieceValues[piece.type] ?: 0
                    if (piece.player == maximizingPlayer) {
                        score += value
                    } else {
                        score -= value
                    }
                }
            }
        }
        return score
    }

    private fun getAllValidMoves(state: GameState, player: Player): List<Move> {
        val moves = mutableListOf<Move>()
        for (r in 0..7) {
            for (c in 0..7) {
                val pos = Position(r, c)
                val piece = state.board.getPiece(pos)
                if (piece != null && piece.player == player) {
                    val validPositions = gameEngine.getValidMoves(state, pos)
                    for (toPos in validPositions) {
                        moves.add(Move(pos, toPos, piece, state.board.getPiece(toPos)))
                    }
                }
            }
        }
        // Basic move ordering: captures first to improve alpha-beta pruning efficiency
        return moves.sortedByDescending { it.capturedPiece != null }
    }
}
