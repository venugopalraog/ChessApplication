package com.venug.chessapp.domain.engine

import com.venug.chessapp.domain.models.*
import org.junit.Assert.*
import org.junit.Test

class GameEngineTest {

    private val engine = GameEngine()

    @Test
    fun `initial board setup generates correct valid moves for white pawn`() {
        val state = GameState()
        // White pawns are on row 6 
        val pawnPos = Position(6, 4) // e2 (0-indexed: row 6 is rank 2, col 4 is 'e')
        val validMoves = engine.getValidMoves(state, pawnPos)

        assertEquals(2, validMoves.size)
        assertTrue(validMoves.contains(Position(5, 4))) // e3
        assertTrue(validMoves.contains(Position(4, 4))) // e4
    }

    @Test
    fun `initial board setup prevents other pieces from moving`() {
        val state = GameState()
        // White rook on a1
        val rookPos = Position(7, 0)
        val validMoves = engine.getValidMoves(state, rookPos)
        
        assertTrue(validMoves.isEmpty()) // Blocked by pawn
    }

    @Test
    fun `knight can jump over pieces`() {
        val state = GameState()
        // White knight on g1
        val knightPos = Position(7, 6)
        val validMoves = engine.getValidMoves(state, knightPos)
        
        assertEquals(2, validMoves.size)
        assertTrue(validMoves.contains(Position(5, 5))) // f3
        assertTrue(validMoves.contains(Position(5, 7))) // h3
    }
}
