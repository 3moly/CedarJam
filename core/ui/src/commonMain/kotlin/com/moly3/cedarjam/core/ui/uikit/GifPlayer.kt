package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.shakster.gifkt.GifDecoder
import com.shakster.gifkt.asRandomAccess
import com.shakster.gifkt.compose.createImageBitmap
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.io.files.Path
import kotlin.time.Duration.Companion.seconds

@Stable
data class GifSource(
    val images: ImmutableList<ImageBitmap>
)

@Stable
data class GifFrame(
    val frame: Int,
    val image: ImageBitmap
)

@Composable
fun GifPlayer(gifUrl: String) {
    val platformContext = LocalPlatformContext.current
    val scope = rememberCoroutineScope()
    val requester = remember {
        SingletonImageLoader.get(platformContext)
    }
    val imagesPlayer = remember {
        mutableStateOf<GifSource?>(null)
    }
    val currentImage = remember {
        mutableStateOf<GifFrame?>(null)
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        currentImage.value?.let {
            Image(bitmap = it.image, contentDescription = null, modifier = Modifier.size(450.dp))
        }
    }
    LaunchedEffect(imagesPlayer.value) {
        imagesPlayer.value?.let {
            if (it.images.isNotEmpty()) {
                while (true) {
                    val currentFrame = currentImage.value
                    val newImage = if (currentFrame == null) {
                        GifFrame(0, it.images[0])
                    } else {
                        val indexFrame = currentFrame.frame
                        if (it.images.size - 1 > indexFrame + 1) {
                            GifFrame(indexFrame + 1, it.images[indexFrame + 1])
                        } else {
                            GifFrame(0, it.images[0])
                        }
                    }
                    currentImage.value = newImage
                    delay(24L)
                }
            } else {
                currentImage.value = null
            }
        }
    }
    LaunchedEffect(gifUrl) {
        scope.launch {
            val request = ImageRequest.Builder(platformContext)
                .data(gifUrl)
                .size(256)
                .build()

            val result = requester.execute(request)

            if (result is SuccessResult) {
                val snapshot = requester.diskCache?.openSnapshot(result.memoryCacheKey?.key ?: "")
                val imgPath = snapshot?.data
                if (imgPath != null) {
                    val path: Path = Path(imgPath.toString())
                    val data = path.asRandomAccess()
                    val decoder = GifDecoder(data)
                    val images = mutableListOf<ImageBitmap>()
                    decoder.asSequence().forEach { frame ->
                        // Process each frame
                        val imageBitmap = createImageBitmap(frame.argb, frame.width, frame.height)
                        images.add(imageBitmap)
                    }
                    decoder.close()
                    imagesPlayer.value = GifSource(images.toPersistentList())
                }
            }
        }
    }
}