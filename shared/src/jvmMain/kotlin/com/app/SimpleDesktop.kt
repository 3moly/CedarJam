package com.app

//@OptIn(ExperimentalDecomposeApi::class, ExperimentalComposeUiApi::class)
//@Composable
//fun ApplicationScope.SimpleDesktop(
//    root: Root,
//    lifecycle: LifecycleRegistry,
//    backDispatcher: BackDispatcher
//) {
//    val windowState = rememberWindowState()
//    SwingWindow(
//        onCloseRequest = {},
//        init = {},
//        title = "CedarJam",
//        state = windowState,
//        decoration = WindowDecoration.Undecorated()
////        create = {
////            val window = ComposeWindow(
////                graphicsConfiguration = null,
////                skiaLayerAnalytics = SkiaLayerAnalytics.Empty
////            )
////            window.isUndecorated = true
////            window.opacity = 0.5f
////            window
////        },
////        dispose = {}
//    ) {
//        var decoratedWindowState by remember { mutableStateOf(DecoratedWindowState.of(window)) }
//
//        DisposableEffect(window) {
//            val adapter =
//                object : WindowAdapter(), ComponentListener {
//                    override fun windowActivated(e: WindowEvent?) {
//                        decoratedWindowState = DecoratedWindowState.of(window)
//                    }
//
//                    override fun windowDeactivated(e: WindowEvent?) {
//                        decoratedWindowState = DecoratedWindowState.of(window)
//                    }
//
//                    override fun windowIconified(e: WindowEvent?) {
//                        decoratedWindowState = DecoratedWindowState.of(window)
//                    }
//
//                    override fun windowDeiconified(e: WindowEvent?) {
//                        decoratedWindowState = DecoratedWindowState.of(window)
//                    }
//
//                    override fun windowStateChanged(e: WindowEvent) {
//                        decoratedWindowState = DecoratedWindowState.of(window)
//                    }
//
//                    override fun componentResized(e: ComponentEvent?) {
//                        decoratedWindowState = DecoratedWindowState.of(window)
//                    }
//
//                    override fun componentMoved(e: ComponentEvent?) {
//                        // Empty
//                    }
//
//                    override fun componentShown(e: ComponentEvent?) {
//                        // Empty
//                    }
//
//                    override fun componentHidden(e: ComponentEvent?) {
//                        // Empty
//                    }
//                }
//
//            window.addWindowListener(adapter)
//            window.addWindowStateListener(adapter)
//            window.addComponentListener(adapter)
//
//            onDispose {
//                window.removeWindowListener(adapter)
//                window.removeWindowStateListener(adapter)
//                window.removeComponentListener(adapter)
//            }
//        }
//
//        CompositionLocalProvider(
//            LocalDecoratedWindowScope provides this
//        ){
//            val windowInfo = LocalWindowInfo.current
//            LifecycleController(
//                lifecycle,
//                windowState,
//                windowInfo = windowInfo
//            )
//            ActualPredictiveBackGestureOverlay(
//                backDispatcher = backDispatcher,
//                modifier = Modifier
//            ) {
//                val undecoratedWindowBorder =
//                    if (true && !decoratedWindowState.isMaximized) {
//                        Modifier.border(
//                            Stroke.Alignment.Inside,
//                            1.dp,
//                            Color.Red,
//                            RectangleShape,
//                        )
//                            .padding(40.dp)
//                    } else {
//                        Modifier
//                    }
//                val currentComponent = remember(window) { window.contentPane.components.filterIsInstance<JComponent>().first() }
//
//                CompositionLocalProvider(
//                    LocalComponent provides currentComponent,
////                    LocalTitleBarInfo provides TitleBarInfo(title, icon),
//                ) {
//                    Layout(
//                        content = {
//                            val scope =
//                                object : DecoratedWindowScope {
//                                    override val state: DecoratedWindowState
//                                        get() = decoratedWindowState
//
//                                    override val window: ComposeWindow
//                                        get() = this@SwingWindow.window
//                                }
//                            key(scope){
//                                MainApp(root = root)
//                                Row(Modifier.height(30.dp)){
//                                    Box(Modifier.size(30.dp).background(Color.Red)){
//
//                                    }
//                                    Box(Modifier.size(30.dp).background(Color.Yellow).clickable{
//                                        windowState.placement = WindowPlacement.Fullscreen
//                                    }){
//
//                                    }
//                                }
//                                if (!BuildConfig.IsRelease) {
//                                    Box(Modifier.fillMaxSize()) {
//                                        Box(
//                                            Modifier.align(Alignment.TopCenter)
//                                                .height(topStatusBarPadding.dp).fillMaxWidth()
//                                                .background(Color.Red.copy(alpha = 0.3f))
//                                        )
//                                        Box(
//                                            Modifier.align(Alignment.BottomCenter)
//                                                .height(bottomNavigationBarPadding.dp).fillMaxWidth()
//                                                .background(Color.Black.copy(alpha = 0.3f))
//                                        )
//                                    }
//                                }
//                            }
//
//                        },
//                        modifier = undecoratedWindowBorder.trackWindowActivation(window),
//                        measurePolicy = CedarJamWindowMeasurePolicy,
//                    )
//                }
//
//            }
//        }
//    }
//}
annotation class SimpleDesktop
