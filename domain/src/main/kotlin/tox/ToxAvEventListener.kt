// SPDX-FileCopyrightText: 2020 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.tox

import im.tox.tox4j.av.callbacks.ToxAvEventListener
import im.tox.tox4j.av.enums.ToxavFriendCallState
import java.util.EnumSet
import scala.Option
import scala.Tuple3

typealias CallHandler = (pk: String, audioEnabled: Boolean, videoEnabled: Boolean) -> Unit
typealias CallStateHandler = (pk: String, callState: EnumSet<ToxavFriendCallState>) -> Unit
typealias VideoBitRateHandler = (pk: String, bitRate: Int) -> Unit
typealias VideoReceiveFrameHandler = (
    pk: String,
    width: Int,
    height: Int,
    y: ByteArray,
    u: ByteArray,
    v: ByteArray,
    yStride: Int,
    uStride: Int,
    vStride: Int
) -> Unit

typealias AudioReceiveFrameHandler = (pk: String, pcm: ShortArray, channels: Int, samplingRate: Int) -> Unit
typealias AudioBitRateHandler = (pk: String, bitRate: Int) -> Unit

object ToxAvEventListener : ToxAvEventListener<Unit> {
    var contactMapping: List<Pair<PublicKey, Int>> = listOf()

    var callHandler: CallHandler = { _, _, _ -> }
    var callStateHandler: CallStateHandler = { _, _ -> }
    var videoBitRateHandler: VideoBitRateHandler = { _, _ -> }
    var videoReceiveFrameHandler: VideoReceiveFrameHandler = { _, _, _, _, _, _, _, _, _ -> }
    var audioReceiveFrameHandler: AudioReceiveFrameHandler = { _, _, _, _ -> }
    var audioBitRateHandler: AudioBitRateHandler = { _, _ -> }

    private fun keyFor(friendNo: Int) =
        contactMapping.find { it.second == friendNo }!!.first.string()

    override fun call(friendNo: Int, audioEnabled: Boolean, videoEnabled: Boolean, s: Unit?) =
        callHandler(keyFor(friendNo), audioEnabled, videoEnabled)

    override fun videoBitRate(friendNo: Int, bitRate: Int, s: Unit?) =
        videoBitRateHandler(keyFor(friendNo), bitRate)

    override fun videoFrameCachedYUV(
        height: Int,
        yStride: Int,
        uStride: Int,
        vStride: Int
    ): Option<Tuple3<ByteArray, ByteArray, ByteArray>> = Option.empty()

    override fun videoReceiveFrame(
        friendNo: Int,
        width: Int,
        height: Int,
        y: ByteArray,
        u: ByteArray,
        v: ByteArray,
        yStride: Int,
        uStride: Int,
        vStride: Int,
        s: Unit?
    ) = videoReceiveFrameHandler(
        keyFor(friendNo),
        width, height,
        y, u, v,
        yStride, uStride, vStride
    )

    override fun callState(friendNo: Int, callState: EnumSet<ToxavFriendCallState>, s: Unit?) =
        callStateHandler(keyFor(friendNo), callState)

    override fun audioReceiveFrame(
        friendNo: Int,
        pcm: ShortArray,
        channels: Int,
        samplingRate: Int,
        s: Unit?
    ) = audioReceiveFrameHandler(keyFor(friendNo), pcm, channels, samplingRate)

    override fun audioBitRate(friendNo: Int, bitRate: Int, s: Unit?) =
        audioBitRateHandler(keyFor(friendNo), bitRate)
}
