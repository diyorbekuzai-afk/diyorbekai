package com.example.utils

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.math.sin

object AudioGenerator {
    private const val SAMPLE_RATE = 44100

    fun playBeep() {
        Thread {
            for (i in 0..2) {
                generateTone(800.0, 150)
                Thread.sleep(100)
            }
        }.start()
    }

    fun playBell() {
        Thread {
            for (i in 0..5) {
                generateTone(1200.0, 50)
                Thread.sleep(50)
            }
        }.start()
    }

    fun playBuzzer() {
        Thread {
            generateSawtooth(220.0, 1000)
        }.start()
    }

    fun playWhistle() {
        Thread {
            generateTone(2500.0, 600)
        }.start()
    }

    private fun generateTone(frequencyOfTone: Double, durationMs: Int) {
        val numSamples = Math.round(durationMs * SAMPLE_RATE / 1000.0).toInt()
        val sample = DoubleArray(numSamples)
        val generatedSnd = ByteArray(2 * numSamples)

        for (i in 0 until numSamples) {
            sample[i] = sin(2 * Math.PI * i / (SAMPLE_RATE / frequencyOfTone))
        }

        var idx = 0
        for (dVal in sample) {
            val valShort = (dVal * 32767).toInt().toShort()
            generatedSnd[idx++] = (valShort.toInt() and 0x00ff).toByte()
            generatedSnd[idx++] = ((valShort.toInt() and 0xff00) ushr 8).toByte()
        }

        playAudioTrack(generatedSnd)
    }

    private fun generateSawtooth(frequencyOfTone: Double, durationMs: Int) {
        val numSamples = Math.round(durationMs * SAMPLE_RATE / 1000.0).toInt()
        val generatedSnd = ByteArray(2 * numSamples)

        val period = SAMPLE_RATE / frequencyOfTone
        for (i in 0 until numSamples) {
            val phase = (i % period) / period
            val value = (phase * 2.0 - 1.0) * 32767
            val valShort = value.toInt().toShort()
            generatedSnd[i * 2] = (valShort.toInt() and 0x00ff).toByte()
            generatedSnd[i * 2 + 1] = ((valShort.toInt() and 0xff00) ushr 8).toByte()
        }

        playAudioTrack(generatedSnd)
    }

    private fun playAudioTrack(generatedSnd: ByteArray) {
        try {
            val audioTrack = AudioTrack(
                AudioManager.STREAM_ALARM,
                SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedSnd.size,
                AudioTrack.MODE_STATIC
            )
            audioTrack.write(generatedSnd, 0, generatedSnd.size)
            audioTrack.play()
            Thread.sleep((generatedSnd.size / (2.0 * SAMPLE_RATE) * 1000).toLong())
            audioTrack.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
