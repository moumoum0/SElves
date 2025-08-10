package com.selves.xnn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * 字母表索引栏组件，支持中英文索引功能
 * @param availableLetters 可用的字母列表
 * @param selectedLetter 当前选中的字母
 * @param onLetterSelected 字母选择回调
 * @param modifier 修饰符
 */
@Composable
fun AlphabetIndexBar(
    availableLetters: List<String>,
    selectedLetter: String? = null,
    onLetterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var columnHeight by remember { mutableStateOf(0) }
    var isDragging by remember { mutableStateOf(false) }
    
    // 完整的字母表，包含数字和特殊字符
    val allLetters = listOf(
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
        "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"
    )
    
    // 计算触摸位置对应的字母
    fun getLetterAtPosition(y: Float): String? {
        if (columnHeight == 0) return null
        
        val itemHeight = columnHeight.toFloat() / allLetters.size
        val index = (y / itemHeight).roundToInt().coerceIn(0, allLetters.size - 1)
        val letter = allLetters[index]
        
        return if (availableLetters.contains(letter)) letter else null
    }
    
    Box {
        Column(
            modifier = modifier
                .height(400.dp) // 固定高度，避免被FloatingActionButton遮挡
                .width(24.dp)
                .onGloballyPositioned { coordinates ->
                    columnHeight = coordinates.size.height
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            getLetterAtPosition(offset.y)?.let { letter ->
                                onLetterSelected(letter)
                            }
                        },
                        onDragEnd = {
                            isDragging = false
                        },
                        onDrag = { change, _ ->
                            // 在拖拽过程中实时更新
                            getLetterAtPosition(change.position.y)?.let { letter ->
                                onLetterSelected(letter)
                            }
                        }
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            allLetters.forEach { letter ->
                val isAvailable = availableLetters.contains(letter)
                val isSelected = selectedLetter == letter
                
                AlphabetIndexItem(
                    letter = letter,
                    isAvailable = isAvailable,
                    isSelected = isSelected,
                    isDragging = isDragging,
                    onClick = {
                        if (isAvailable) {
                            onLetterSelected(letter)
                        }
                    }
                )
            }
        }
        
        // 拖拽时显示的字母提示气泡
        if (isDragging && selectedLetter != null) {
            LetterIndicator(
                letter = selectedLetter,
                modifier = Modifier
                    .offset(x = (-40).dp, y = 0.dp)
                    .align(Alignment.CenterStart)
            )
        }
    }
}

@Composable
private fun AlphabetIndexItem(
    letter: String,
    isAvailable: Boolean,
    isSelected: Boolean,
    isDragging: Boolean = false,
    onClick: () -> Unit
) {
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isAvailable -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    }
    
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        isDragging && isAvailable -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else -> Color.Transparent
    }
    
    val itemSize = if (isSelected && isDragging) 18.dp else 16.dp
    
    Box(
        modifier = Modifier
            .size(itemSize)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            fontSize = if (isSelected) 11.sp else 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
            textAlign = TextAlign.Center
                )
    }
}

@Composable
private fun LetterIndicator(
    letter: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

 
  
 