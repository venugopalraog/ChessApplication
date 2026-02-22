package com.venug.chessapp.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.venug.chessapp.domain.models.*

@Composable
fun ChessBoardView(
    gameState: GameState,
    selectedPosition: Position?,
    validMoves: List<Position>,
    onSquareClick: (Position) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(Color.DarkGray)
    ) {
        for (row in 0 until 8) {
            Row(modifier = Modifier.weight(1f)) {
                for (col in 0 until 8) {
                    val pos = Position(row, col)
                    val piece = gameState.board.getPiece(pos)
                    val isLightSquare = (row + col) % 2 == 0
                    val isSelected = pos == selectedPosition
                    val isHint = validMoves.contains(pos)

                    SquareView(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        piece = piece,
                        isLightSquare = isLightSquare,
                        isSelected = isSelected,
                        isHint = isHint,
                        onClick = { onSquareClick(pos) }
                    )
                }
            }
        }
    }
}

@Composable
fun SquareView(
    modifier: Modifier = Modifier,
    piece: Piece?,
    isLightSquare: Boolean,
    isSelected: Boolean,
    isHint: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when {
        isSelected -> Color(0xFFFFF59D) // Yellow-ish
        isHint -> Color(0xFFA5D6A7) // Green-ish
        isLightSquare -> Color(0xFFF0D9B5)
        else -> Color(0xFFB58863)
    }

    Box(
        modifier = modifier
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (piece != null) {
            Text(
                text = getPieceUnicode(piece),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if (piece.player == Player.WHITE) Color.White else Color.Black,
                textAlign = TextAlign.Center
            )
        } else if (isHint) {
            // Draw a small dot for valid move on empty square
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color.Black.copy(alpha = 0.2f), shape = androidx.compose.foundation.shape.CircleShape)
            )
        }
    }
}

// Map Piece to filled Unicode characters 
// We use filled characters (Black set) for both, but color them via Text(color = ...)
private fun getPieceUnicode(piece: Piece): String {
    return when (piece.type) {
        PieceType.KING -> "♚"
        PieceType.QUEEN -> "♛"
        PieceType.ROOK -> "♜"
        PieceType.BISHOP -> "♝"
        PieceType.KNIGHT -> "♞"
        PieceType.PAWN -> "♟"
    }
}
