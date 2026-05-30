package com.android.purebilibili.feature.video.ui.section

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VideoPlayerCoverPolicyTest {

    @Test
    fun verticalVideo_fillsPlayerViewportDuringCoverPhase() {
        assertTrue(
            shouldFillPlayerViewportForManualStartCover(
                shouldKeepCoverForManualStart = false,
                forceCoverDuringReturnAnimation = false,
                isVerticalVideo = true
            )
        )
    }

    @Test
    fun returnCoverSharedBounds_doesNotForceViewportFill() {
        assertFalse(
            shouldFillPlayerViewportForManualStartCover(
                shouldKeepCoverForManualStart = true,
                forceCoverDuringReturnAnimation = true,
                isVerticalVideo = true
            )
        )
    }

    @Test
    fun forcedReturnCoverSharedBounds_doesNotLayerExtraSpringOnHomeReturn() {
        // Home 源：详情↔卡片 shell sharedBounds 已经完整接管 morph。
        // 这里若再挂一层封面 sharedBounds 会引入单边 spring 回弹（首页卡片侧无对端）。
        assertFalse(
            shouldEnableForcedReturnCoverSharedBounds(
                forceCoverDuringReturnAnimation = true,
                transitionEnabled = true,
                hasSharedTransitionScope = true,
                hasAnimatedVisibilityScope = true,
                sourceRoute = com.android.purebilibili.navigation.ScreenRoutes.Home.route
            )
        )
        assertFalse(
            shouldEnableForcedReturnCoverSharedBounds(
                forceCoverDuringReturnAnimation = true,
                transitionEnabled = true,
                hasSharedTransitionScope = true,
                hasAnimatedVisibilityScope = true,
                sourceRoute = "${com.android.purebilibili.navigation.ScreenRoutes.Home.route}?from=tab"
            )
        )
    }

    @Test
    fun forcedReturnCoverSharedBounds_stillActiveForNonHomeCardReturnTargets() {
        // 非 home 源（如 dynamic / search / history）此次不调整。
        listOf("dynamic", "search", "history", "favorite", "watch_later", "partition").forEach { route ->
            assertTrue(
                shouldEnableForcedReturnCoverSharedBounds(
                    forceCoverDuringReturnAnimation = true,
                    transitionEnabled = true,
                    hasSharedTransitionScope = true,
                    hasAnimatedVisibilityScope = true,
                    sourceRoute = route
                ),
                "expected forced cover sharedBounds to remain enabled for sourceRoute=$route"
            )
        }
        assertTrue(shouldUseReturnLandingMotionForForcedReturnCover(true))
        assertFalse(shouldUseReturnLandingMotionForForcedReturnCover(false))
    }

    @Test
    fun homeSourceRouteHelperRemainsForLegacyCallSitesEvenAfterShellTakeover() {
        // 此辅助函数本身用于产出 sharedElement key 的 source 段。即便 home 源不再挂载
        // forced cover sharedBounds，该函数依旧应当能正确归一化（用于其它路径/未来扩展）。
        val homeRoute = com.android.purebilibili.navigation.ScreenRoutes.Home.route

        assertTrue(resolveForcedReturnCoverSharedElementSourceRoute(homeRoute) == homeRoute)
        assertTrue(resolveForcedReturnCoverSharedElementSourceRoute("$homeRoute?from=tab") == homeRoute)
        assertTrue(resolveForcedReturnCoverSharedElementSourceRoute("search") == null)
    }
}
