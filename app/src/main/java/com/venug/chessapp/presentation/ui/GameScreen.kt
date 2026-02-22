package com.venug.chessapp.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.venug.chessapp.presentation.GameIntent
import com.venug.chessapp.presentation.GameViewModel

@Composable
fun GameScreen(viewModel: GameViewModel = hiltViewModel()) {
    val viewState = viewModel.viewState.collectAsState().value
    
    val gameState = viewState.gameState

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Chess",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Turn indicator
        Text(
            text = "Current Turn: ${gameState.currentPlayer.name}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Game Status
        val statusText = when {
            gameState.isCheckmate -> "Checkmate! ${gameState.winner?.name} wins!"
            gameState.isStalemate -> "Stalemate! Draw."
            gameState.isCheck -> "Check!"
            else -> ""
        }

        if (statusText.isNotEmpty()) {
            Text(
                text = statusText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Board View
        ChessBoardView(
            gameState = gameState,
            selectedPosition = viewState.selectedPosition,
            validMoves = viewState.validMovesForSelected,
            onSquareClick = { pos -> viewModel.processIntent(GameIntent.SelectSquare(pos)) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row {
            Button(onClick = { viewModel.processIntent(GameIntent.Restart) }) {
                Text("Restart Game")
            }
        }
    }
}
