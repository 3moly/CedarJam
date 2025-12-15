package com.moly3.cedarjam.features.feature_file_view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coil3.compose.rememberAsyncImagePainter
import com.moly3.cedarjam.core.domain.model.FileType
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.service.IUtilsService
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.ui.compositions.LocalImageLoader
import com.moly3.cedarjam.core.ui.service.IJvmBrowserService
import com.moly3.cedarjam.core.ui.service.MacTrackpadGestureService
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.CJZoomableViewLayout
import com.moly3.cedarjam.features.feature_file_view.internal.MidiUI
import com.moly3.cedarjam.features.feature_file_view.internal.PdfUI
import com.moly3.cedarjam.features.feature_file_view.internal.VideoUI
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
@Composable
fun FileView(
    macTrackpadGestureService: MacTrackpadGestureService,
    filesRepository: IFilesRepository,
    utilsService: IUtilsService,
    jvmBrowserService: IJvmBrowserService,
    workspaceSession: WorkspaceSession,
    modifier: Modifier,
    fileNode: FileType,
    contentCanvas: @Composable (FileType.Canvas) -> Unit,
    contentFileEdit: @Composable (FileType.Text) -> Unit,
    nextPage: (FileType.PDF) -> Unit = {},
    backPage: (FileType.PDF) -> Unit = {},
    toPage: (FileType.PDF, Int) -> Unit = { pdf, page -> }
) {

    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val fl = fileNode) {
            is FileType.Canvas -> contentCanvas(fl)

            is FileType.Video -> VideoUI(
                workspaceSession = workspaceSession,
                fl = fl
            )

            is FileType.MIDI -> MidiUI(
                jvmBrowserService = jvmBrowserService,
                fl = fl,
                filesRepository = filesRepository,
                utilsService = utilsService
            )

            is FileType.PDF -> {

                PdfUI(
                    fileType = fl,
                    macTrackpadGestureService = macTrackpadGestureService,
                    back = { backPage(fl) },
                    forward = { nextPage(fl) },
                    toPage = { toPage(fl, it) }
                )
            }

            is FileType.Image -> {
                CJZoomableViewLayout(
                    modifier = Modifier.fillMaxSize(),
                    macTrackpadGestureService = macTrackpadGestureService
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = fl.fileNode.getFullPath(),
                            imageLoader = LocalImageLoader.current
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            is FileType.Text -> contentFileEdit(fl)

            FileType.Unknown -> CJText(text = "Unknown")
        }
    }
}