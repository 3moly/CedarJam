package com.moly3.cedarjam.pages.page_tab

import androidx.compose.runtime.Immutable
import com.moly3.cedarjam.navigation.NavigationComponent
import com.moly3.cedarjam.pages.page_collection.CollectionComponent
import com.moly3.cedarjam.pages.page_collection_row.CollectionRowComponent
import com.moly3.cedarjam.pages.page_file.FileComponent
import com.moly3.cedarjam.pages.page_graph.GraphComponent
import com.moly3.cedarjam.pages.page_home.HomeComponent
import com.moly3.cedarjam.ui.pages.tag.TagComponent
import com.moly3.cedarjam.ui.pages.tags.TagsComponent
import com.moly3.cedarjam.core.ui.model.PageNameData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Immutable
interface TabComponent : NavigationComponent<TabComponent.Child> {
    val nameFlow: Flow<PageNameData?>
    val state: StateFlow<State>
    fun onIntent(intent: Intent)
    val labels: Flow<Label>
    sealed class Child {

        class Home(val component: HomeComponent) : Child()
        class Graph(val component: GraphComponent) : Child()
        class File(val component: FileComponent) : Child()
        class Collection(val component: CollectionComponent) : Child()
        class CollectionRow(val component: CollectionRowComponent) : Child()
        class Tags(val component: TagsComponent) : Child()
        class Tag(val component: TagComponent) : Child()
    }
}