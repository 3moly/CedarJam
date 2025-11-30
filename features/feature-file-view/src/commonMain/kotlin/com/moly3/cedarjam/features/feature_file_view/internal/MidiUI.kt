package com.moly3.cedarjam.features.feature_file_view.internal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.moly3.cedarjam.features.feature_browser.BrowserPreInitUI
import com.moly3.cedarjam.features.feature_file_view.MidiScreenGen
import com.moly3.cedarjam.features.feature_file_view.evaluate
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewStateWithHTMLData
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.FileType
import com.moly3.cedarjam.core.domain.model.MidiNote
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.ui.service.IJvmBrowserService
import com.moly3.cedarjam.core.domain.service.IUtilsService
import com.moly3.cedarjam.core.ui.uikit.CJButton
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import kotlin.collections.iterator

@Composable
internal fun MidiUI(
    jvmBrowserService: IJvmBrowserService,
    fl: FileType.MIDI,
    filesRepository: IFilesRepository,
    utilsService: IUtilsService
) {
    val scope = rememberCoroutineScope()
    BrowserPreInitUI(
        jvmBrowserService = jvmBrowserService
    ) {
        val htmlState = remember {
            mutableStateOf(
                MidiScreenGen.genHtml(
                    null,
                    listOf(55)
                )
            )
        }


        val notesState =
            remember { mutableStateOf<Map<Long, List<MidiNote>>>(mapOf()) }

        LaunchedEffect(fl.fileNode) {
            scope.launch(io) {
                val bytes = filesRepository.getNodeBytes(fl.fileNode)
                val notes = utilsService.readMidi(bytes)
                    .sortedBy { d -> d.tick }
                    .filter { d -> d.isNoteOn }
                    .groupBy { x -> x.tick }

                val mut = mutableMapOf<Long, List<MidiNote>>()
                for (item in notes) {
                    val hidden = item.value
                        .map { b -> b.note }
                        .sortedBy { b -> b }
                        .toImmutableList()
                    val hiddens = mut.asSequence()
                        .map { g ->
                            g.value
                                .map { x -> x.note }
                                .sortedBy { b -> b }
                                .toImmutableList()
                        }.toList()
                    if (hiddens.firstOrNull { g ->
                            g == hidden
                        } != null
                    ) {
                        continue
                    }

                    mut.put(item.key, item.value)
                }

                notesState.value = mut
            }
        }

        Column(Modifier.fillMaxSize()) {
            val state = rememberWebViewStateWithHTMLData(data = htmlState.value)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (note in notesState.value) {
                    CJButton(text = "m: ${note.key}") {
                        scope.launch {
                            val chords = note.value.map { d -> d.note }
                            val method = "refreshNotes('${"Db"}',${chords})"
//                                        val method = "refreshNotes()"
                            evaluate(state.nativeWebView, method)
//                                        htmlState.value = ""
//                                        delay(100L)
//                                        htmlState.value = MidiScreenGen.genHtml(
//                                            "Db",
//                                            note.value.map { d -> d.note })
                        }

                    }
                }
            }
            Column(Modifier.weight(1f)) {
                WebView(
                    modifier = Modifier.fillMaxSize().zIndex(1f),
                    state = state
                )
            }
        }
    }
}