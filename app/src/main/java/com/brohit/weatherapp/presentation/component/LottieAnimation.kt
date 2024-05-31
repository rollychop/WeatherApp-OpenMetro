package com.brohit.weatherapp.presentation.component

import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun LottieAnimationItem(
    @RawRes lottieFile: Int,
    modifier: Modifier = Modifier,
    restartOnPlay: Boolean = true
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieFile))
    LottieAnimation(
        composition = composition,
        modifier = modifier,
        restartOnPlay = restartOnPlay,
        iterations = Int.MAX_VALUE
    )

}