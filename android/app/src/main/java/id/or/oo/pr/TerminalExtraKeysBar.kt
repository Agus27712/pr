package id.or.oo.pr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TerminalExtraKeysBar(
    onSendBytes: (ByteArray) -> Unit,
    modifier: Modifier = Modifier,
) {
    var ctrlActive by remember { mutableStateOf(false) }
    var altActive by remember { mutableStateOf(false) }

    val handleKeyPayload = remember(onSendBytes) {
        { payload: Any ->
            var bytesToSend: ByteArray? = null

            when (payload) {
                is String -> {
                    val rawBytes = payload.toByteArray(Charsets.UTF_8)
                    if (ctrlActive && rawBytes.size == 1) {
                        val firstChar = rawBytes[0].toInt().toChar()
                        val ctrlChar = (firstChar.code and 0x1F).toByte()
                        bytesToSend = byteArrayOf(ctrlChar)
                    } else if (altActive) {
                        bytesToSend = byteArrayOf(0x1B.toByte()) + rawBytes
                    } else {
                        bytesToSend = rawBytes
                    }
                    ctrlActive = false
                    altActive = false
                }
                is ByteArray -> {
                    if (altActive) {
                        bytesToSend = byteArrayOf(0x1B.toByte()) + payload
                    } else {
                        bytesToSend = payload
                    }
                    ctrlActive = false
                    altActive = false
                }
            }

            bytesToSend?.let { onSendBytes(it) }
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF131320),
        tonalElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // Row 1: ESC, TAB, CTRL, ALT, UP, PIPE, SLASH, MINUS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                KeyButton(
                    label = "ESC",
                    modifier = Modifier.weight(1f).testTag("key_esc"),
                    onClick = { handleKeyPayload("\u001b") },
                )
                KeyButton(
                    label = "TAB",
                    modifier = Modifier.weight(1f).testTag("key_tab"),
                    onClick = { handleKeyPayload("\t") },
                )
                KeyButton(
                    label = "CTRL",
                    isActive = ctrlActive,
                    modifier = Modifier.weight(1f).testTag("key_ctrl"),
                    onClick = { ctrlActive = !ctrlActive },
                )
                KeyButton(
                    label = "ALT",
                    isActive = altActive,
                    modifier = Modifier.weight(1f).testTag("key_alt"),
                    onClick = { altActive = !altActive },
                )
                KeyButton(
                    label = "▲",
                    modifier = Modifier.weight(1f).testTag("key_up"),
                    onClick = { handleKeyPayload("\u001b[A") },
                )
                KeyButton(
                    label = "|",
                    modifier = Modifier.weight(1f).testTag("key_pipe"),
                    onClick = { handleKeyPayload("|") },
                )
                KeyButton(
                    label = "/",
                    modifier = Modifier.weight(1f).testTag("key_slash"),
                    onClick = { handleKeyPayload("/") },
                )
                KeyButton(
                    label = "-",
                    modifier = Modifier.weight(1f).testTag("key_minus"),
                    onClick = { handleKeyPayload("-") },
                )
            }

            // Row 2: HOME, END, PGUP, PGDN, LEFT, DOWN, RIGHT, TILDE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                KeyButton(
                    label = "HOME",
                    modifier = Modifier.weight(1f).testTag("key_home"),
                    onClick = { handleKeyPayload("\u001b[H") },
                )
                KeyButton(
                    label = "END",
                    modifier = Modifier.weight(1f).testTag("key_end"),
                    onClick = { handleKeyPayload("\u001b[F") },
                )
                KeyButton(
                    label = "PGUP",
                    modifier = Modifier.weight(1f).testTag("key_pgup"),
                    onClick = { handleKeyPayload("\u001b[5~") },
                )
                KeyButton(
                    label = "PGDN",
                    modifier = Modifier.weight(1f).testTag("key_pgdn"),
                    onClick = { handleKeyPayload("\u001b[6~") },
                )
                KeyButton(
                    label = "◄",
                    modifier = Modifier.weight(1f).testTag("key_left"),
                    onClick = { handleKeyPayload("\u001b[D") },
                )
                KeyButton(
                    label = "▼",
                    modifier = Modifier.weight(1f).testTag("key_down"),
                    onClick = { handleKeyPayload("\u001b[B") },
                )
                KeyButton(
                    label = "►",
                    modifier = Modifier.weight(1f).testTag("key_right"),
                    onClick = { handleKeyPayload("\u001b[C") },
                )
                KeyButton(
                    label = "~",
                    modifier = Modifier.weight(1f).testTag("key_tilde"),
                    onClick = { handleKeyPayload("~") },
                )
            }
        }
    }
}

@Composable
private fun KeyButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
) {
    val bgColor = if (isActive) Color(0xFF4E65FF) else Color(0xFF28283E)
    val textColor = if (isActive) Color.White else Color(0xFFE2E2EC)

    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = textColor,
        ),
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
        )
    }
}