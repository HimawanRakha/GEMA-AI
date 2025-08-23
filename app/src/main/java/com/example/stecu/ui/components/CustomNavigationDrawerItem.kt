package com.example.stecu.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun CustomNavigationDrawerItem(
    label: String,
    iconResId: Int,
    onClick: () -> Unit,
    // Inilah kuncinya: kita definisikan padding sendiri
    padding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick) // Membuat seluruh baris bisa diklik
            .padding(padding), // Terapkan padding yang bisa kita atur
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = label,
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp)) // Jarak antara ikon dan teks
        Text(
            text = label,
            color = Color.Black
        )
    }
}