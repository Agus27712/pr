package id.or.oo.pr

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.connectbot.terminal.TerminalEmulator
import org.connectbot.terminal.VTermKey

@Composable
fun TerminalExtraKeysBar(
    onSendBytes: (ByteArray) -> Unit,
    modifier: Modifier = Modifier,
    terminalEmulator: TerminalEmulator? = null,
) {
    var ctrlActive by remember { mutableStateOf(false) }
    var altActive by remember { mutableStateOf(false) }

    val dispatchAction = remember(terminalEmulator, onSendBytes) {
        { vtermKey: Int?, char: Char?, rawPayload: String? ->
            var modifiers = 0
            if (altActive) modifiers = modifiers or 2
            if (ctrlActive) modifiers = modifiers or 4

            val em = terminalEmulator
            if (em != null) {
                if (vtermKey != null) {
                    em.dispatchKey(modifiers, vtermKey)
                } else if (char != null) {
                    em.dispatchCharacter(modifiers, char.code)
                } else if (rawPayload != null) {
                    rawPayload.codePoints().forEach { codepoint ->
                        em.dispatchCharacter(modifiers, codepoint)
                    }
                }
            } else {
                // Fallback direct byte writing if emulator reference is absent
                var bytesToSend: ByteArray? = null
                if (vtermKey != null) {
                    val escCode = when (vtermKey) {
                        VTermKey.UP -> "\u001b[A"
                        VTermKey.DOWN -> "\u001b[B"
                        VTermKey.RIGHT -> "\u001b[C"
                        VTermKey.LEFT -> "\u001b[D"
                        VTermKey.HOME -> "\u001b[H"
                        VTermKey.END -> "\u001b[F"
                        VTermKey.PAGEUP -> "\u001b[5~"
                        VTermKey.PAGEDOWN -> "\u001b[6~"
                        VTermKey.ESCAPE -> "\u001b"
                        VTermKey.TAB -> "\t"
                        else -> null
                    }
                    if (escCode != null) {
                        val rawBytes = escCode.toByteArray(Charsets.UTF_8)
                        bytesToSend = if (altActive) byteArrayOf(0x1B.toByte()) + rawBytes else rawBytes
                    }
                } else if (char != null || rawPayload != null) {
                    val str = char?.toString() ?: rawPayload ?: ""
                    val rawBytes = str.toByteArray(Charsets.UTF_8)
                    if (ctrlActive && rawBytes.size == 1) {
                        val firstChar = rawBytes[0].toInt().toChar()
                        bytesToSend = byteArrayOf((firstChar.code and 0x1F).toByte())
                    } else if (altActive) {
                        bytesToSend = byteArrayOf(0x1B.toByte()) + rawBytes
                    } else {
                        bytesToSend = rawBytes
                    }
                }
                bytesToSend?.let { onSendBytes(it) }
            }

            ctrlActive = false
            altActive = false
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
                    onClick = { dispatchAction(VTermKey.ESCAPE, null, null) },
                )
                KeyButton(
                    label = "TAB",
                    modifier = Modifier.weight(1f).testTag("key_tab"),
                    onClick = { dispatchAction(VTermKey.TAB, null, null) },
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
                    onClick = { dispatchAction(VTermKey.UP, null, null) },
                )
                KeyButton(
                    label = "|",
                    modifier = Modifier.weight(1f).testTag("key_pipe"),
                    onClick = { dispatchAction(null, '|', null) },
                )
                KeyButton(
                    label = "/",
                    modifier = Modifier.weight(1f).testTag("key_slash"),
                    onClick = { dispatchAction(null, '/', null) },
                )
                KeyButton(
                    label = "-",
                    modifier = Modifier.weight(1f).testTag("key_minus"),
                    onClick = { dispatchAction(null, '-', null) },
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
                    onClick = { dispatchAction(VTermKey.HOME, null, null) },
                )
                KeyButton(
                    label = "END",
                    modifier = Modifier.weight(1f).testTag("key_end"),
                    onClick = { dispatchAction(VTermKey.END, null, null) },
                )
                KeyButton(
                    label = "PGUP",
                    modifier = Modifier.weight(1f).testTag("key_pgup"),
                    onClick = { dispatchAction(VTermKey.PAGEUP, null, null) },
                )
                KeyButton(
                    label = "PGDN",
                    modifier = Modifier.weight(1f).testTag("key_pgdn"),
                    onClick = { dispatchAction(VTermKey.PAGEDOWN, null, null) },
                )
                KeyButton(
                    label = "◀",
                    modifier = Modifier.weight(1f).testTag("key_left"),
                    onClick = { dispatchAction(VTermKey.LEFT, null, null) },
                )
                KeyButton(
                    label = "▼",
                    modifier = Modifier.weight(1f).testTag("key_down"),
                    onClick = { dispatchAction(VTermKey.DOWN, null, null) },
                )
                KeyButton(
                    label = "▶",
                    modifier = Modifier.weight(1f).testTag("key_right"),
                    onClick = { dispatchAction(VTermKey.RIGHT, null, null) },
                )
                KeyButton(
                    label = "~",
                    modifier = Modifier.weight(1f).testTag("key_tilde"),
                    onClick = { dispatchAction(null, '~', null) },
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