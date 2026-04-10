package com.s1g1.tomatoro.ui

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.s1g1.tomatoro.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTopBar(

){
    CenterAlignedTopAppBar(
        modifier = Modifier.statusBarsPadding(),
        title={
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
    )
}