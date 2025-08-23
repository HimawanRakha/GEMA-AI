package com.example.stecu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stecu.R

@Composable
fun CustomDrawerItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
) {

    val backgroundColor = if (selected) Color(0XFFD9F4FF) else Color.Transparent
    val textColor = if (selected) Color.Black else Color.Black
    val iconColor = if (selected) Color.Black else Color.Gray
    // Row adalah dasar dari item kita
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .clip(MaterialTheme.shapes.medium) // Memberi sudut melengkung pada background
            .background(backgroundColor)      // Menerapkan warna latar belakang
            .padding(padding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Teks Judul Percakapan
        Text( // Memberi weight agar teks memenuhi ruang
            text = label,
            maxLines = 1,
            fontSize = 18.sp,
            overflow = TextOverflow.Ellipsis,
            color = textColor,
            fontWeight = if (selected) FontWeight.Normal else FontWeight.Light,
            modifier = Modifier
                .weight(1f)          // ⬅️ Batasi lebar teks agar tidak menutupi ikon
                .padding(end = 8.dp)  // jarak kecil ke ikon
        )

        // Tombol Hapus (sama seperti sebelumnya)
        Icon(
            painter = painterResource(R.drawable.ic_delete),
            contentDescription = "Hapus Percakapan",
            tint = iconColor,
            modifier = Modifier
                .size(24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false, radius = 16.dp) // ⬅️ Ripple custom
                ) { onDelete() }
        )
    }
}