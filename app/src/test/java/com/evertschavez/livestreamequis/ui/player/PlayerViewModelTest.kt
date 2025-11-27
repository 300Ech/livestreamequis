package com.evertschavez.livestreamequis.ui.player

import androidx.media3.common.Player
import com.evertschavez.livestreamequis.player.core.controller.VideoPlayerController
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {
    private val mockController = mockk<VideoPlayerController>(relaxed = true)
    private val mockPlayer = mockk<Player>(relaxed = true)

    private lateinit var vm: PlayerViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { mockController.getPlayer() } returns mockPlayer
        every { mockController.metrics } returns mockk(relaxed = true)
        every { mockController.state } returns mockk(relaxed = true)

        vm = PlayerViewModel(mockController)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startPlayback calls prepare and play on controller`() = runTest {
        val url = "http://test.com/video.m3u8"

        vm.startPlayback(url, null)

        verify {
            mockController.prepare(match { it.url == url })
            mockController.play()
        }
    }
}