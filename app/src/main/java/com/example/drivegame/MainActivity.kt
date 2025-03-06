package com.example.drivegame

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.drivegame.ui.theme.DriveGameTheme
import kotlin.math.min
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DriveGameTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DriveGame(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

data class GameState (
    val time: Float = 0f,

    val carSize: Size = Size(10f, 20f),
    val car: Rect = Rect(offset = Offset(45f, 20f), size = carSize)
)

data class KeyState(
    var keys: Set<Key> = emptySet(),
    var isLeft: Boolean = false,
    var isRight: Boolean = false
)

fun Offset.toIntOffset(): IntOffset {
    return IntOffset(x.roundToInt(), y.roundToInt())
}

fun Size.toInt(): IntSize {
    return IntSize(width.roundToInt(), height.roundToInt())
}

fun updateGameState(gameState: GameState, inputs: KeyState): GameState {

    var move = Offset.Zero
    if (inputs.keys.contains(Key.DirectionLeft)) {
        move = Offset(-1f, 0f)
    }
    if (inputs.keys.contains(Key.DirectionRight)) {
        move = Offset(1f, 0f)
    }

    var newCar = gameState.car.translate(translateX = move.x, translateY = 1.0f)

    if (newCar.overlaps(Rect(-10f, 0f, 0f, newCar.bottomRight.y))) {
        newCar = newCar.translate(translateX = -move.x, translateY = 0f)
    }
    if (newCar.overlaps(Rect(100f, 0f, 110f, newCar.bottomRight.y))) {
        newCar = newCar.translate(translateX = -move.x, translateY = 0f)
    }

    return gameState.copy(car = newCar, time = gameState.time + 1f)
}

fun DrawScope.drawRoad() {

    val spacing = 10f
    val markingHeight = 10f

    for (i in 0..100) {
        drawRect(
            Color.White,
            topLeft = Offset(49f, (markingHeight + spacing) * i),
            size = Size(2f, markingHeight)
        )
    }
}

fun handleKeyEvent(event: KeyEvent, inputState: KeyState): KeyState {

    if (event.type == KeyEventType.KeyUp) {
        Log.i("key", "${event.key} isUp")
        return inputState.copy(keys = inputState.keys - event.key)
    }
    if (event.type == KeyEventType.KeyDown) {
        Log.i("key", "${event.key} isDown")
        return inputState.copy(keys = inputState.keys  + event.key)
    }
    return inputState
}

@Composable
fun DriveGame(modifier: Modifier = Modifier) {

    var gameState by remember { mutableStateOf(GameState(time=0f)) }
    var inputState by remember { mutableStateOf(KeyState()) }
    var car: ImageBitmap = ImageBitmap.imageResource(R.drawable.red_car)

    LaunchedEffect("physics") {
        while(true) {
            gameState = updateGameState(gameState, inputState)
            delay(1000/60)
        }
    }

    Box(modifier = modifier.background(Color.Gray)
        .focusable()
        .onKeyEvent { event ->
            inputState = handleKeyEvent(event, inputState)
            false
        }) {
        Canvas(modifier = Modifier.fillMaxSize()) {

            val internalSize = Size(100f, 200f)
            val scaleFactor = min(size.width / internalSize.width, size.height / internalSize.height)

            print(scaleFactor)
            withTransform({
                scale(scaleX = 1.0f, scaleY = -1.0f, pivot = Offset.Zero)
                scale(scaleFactor, pivot = Offset.Zero)
                translate(left = 0f, top = -200f)
                translate(left = 0f, top = -gameState.car.center.y + 10f)
            }) {
                drawRoad()
                // drawRect(Color.Blue, topLeft = gameState.car.topLeft, size = gameState.car.size)
                drawImage(car,
                    dstOffset = gameState.car.topLeft.toIntOffset(),
                    dstSize = gameState.car.size.toInt())
            }
        }
        Text("Hello world!", color = Color.White)
    }
}

fun ImageBitmap.Companion.imageResource(i: Int) {}

@Preview
@Composable
fun GreetingPreview() {
    DriveGame(modifier = Modifier.fillMaxSize())
}