package com.wave.audiorecording.audioplayer.trim

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer
import java.util.Arrays

class SoundFile  // A SoundFile object should only be created using the static methods create() and record().
private constructor() {
    private var mProgressListener: ProgressListener? = null
    private var mInputFile: File? = null

    // Member variables representing frame data
    private var mFileType: String? = null
    private var mFileSize = 0
    private var mAvgBitRate // Average bit rate in kbps.
            = 0
    private var mSampleRate = 0
    private var mChannels = 0
    private var mNumSamples // total number of samples per channel in audio file
            = 0
    private var mDecodedBytes // Raw audio data
            : ByteBuffer? = null
    private var mDecodedSamples // shared buffer with mDecodedBytes.
            : ShortBuffer? = null

    // mDecodedSamples has the following format:
    // {s1c1, s1c2, ..., s1cM, s2c1, ..., s2cM, ..., sNc1, ..., sNcM}
    // where sicj is the ith sample of the jth channel (a sample is a signed short)
    // M is the number of channels (e.g. 2 for stereo) and N is the number of samples per channel.
    // Member variables for hack (making it work with old version, until app just uses the samples).
    private var mNumFrames = 0
    private var mFrameGains: IntArray? = null
    private var mFrameLens: IntArray? = null
    private var mFrameOffsets: IntArray? = null
    fun getFiletype(): String? {
        return mFileType
    }

    fun getSampleRate(): Int {
        return mSampleRate
    }

    fun getChannels(): Int {
        return mChannels
    }

    fun getNumSamples(): Int {
        return mNumSamples // Number of samples per channel.
    }

    // Should be removed when the app will use directly the samples instead of the frames.
    fun getNumFrames(): Int {
        return mNumFrames
    }

    // Should be removed when the app will use directly the samples instead of the frames.
    fun getSamplesPerFrame(): Int {
        return 1024 // just a fixed value here...
    }

    // Should be removed when the app will use directly the samples instead of the frames.
    fun getFrameGains(): IntArray? {
        return mFrameGains
    }

    fun getSamples(): ShortBuffer? {
        return if (mDecodedSamples != null) {
            mDecodedSamples
            //            return mDecodedSamples.asReadOnlyBuffer();
        } else {
            null
        }
    }

    private fun setProgressListener(progressListener: ProgressListener) {
        mProgressListener = progressListener
    }

    @Throws(IOException::class, InvalidInputException::class)
    private fun ReadFile(inputFile: File) {
        var extractor: MediaExtractor? = MediaExtractor()
        var format: MediaFormat? = null
        var i: Int
        mInputFile = inputFile
        val components = mInputFile?.path?.split("\\.".toRegex())?.toTypedArray()
        mFileType = components?.get(components.size - 1)
        mFileSize = (mInputFile?.length() ?: 0).toInt()
        extractor?.setDataSource(mInputFile?.path.toString())
        val numTracks = extractor?.trackCount ?: 0
        // find and select the first audio track present in the file.
        i = 0
        while (i < numTracks) {
            format = extractor?.getTrackFormat(i)
            if (format?.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) {
                extractor?.selectTrack(i)
                break
            }
            i++
        }
        if (i == numTracks) {
            throw InvalidInputException("No audio track found in $mInputFile")
        }
        mChannels = format?.getInteger(MediaFormat.KEY_CHANNEL_COUNT) ?: 0
        mSampleRate = format?.getInteger(MediaFormat.KEY_SAMPLE_RATE) ?: 0
        // Expected total number of samples per channel.
        val expectedNumSamples = ((format?.getLong(MediaFormat.KEY_DURATION)
                ?: 0.toLong()) / 1000000f * mSampleRate + 0.5f).toInt()
        var codec: MediaCodec? = format?.getString(MediaFormat.KEY_MIME)?.let { MediaCodec.createDecoderByType(it) }
        codec?.configure(format, null, null, 0)
        codec?.start()
        var decodedSamplesSize = 0 // size of the output buffer containing decoded samples.
        var decodedSamples: ByteArray? = null
        val inputBuffers = codec?.inputBuffers
        var outputBuffers = codec?.outputBuffers
        var sample_size: Int
        val info = MediaCodec.BufferInfo()
        var presentation_time: Long
        var tot_size_read = 0
        var done_reading = false

        // Set the size of the decoded samples buffer to 1MB (~6sec of a stereo stream at 44.1kHz).
        // For longer streams, the buffer size will be increased later on, calculating a rough
        // estimate of the total size needed to store all the samples in order to resize the buffer
        // only once.
        mDecodedBytes = ByteBuffer.allocate(1 shl 20)
        var firstSampleData = true
        while (true) {
            // read data from file and feed it to the decoder input buffers.
            val inputBufferIndex = codec?.dequeueInputBuffer(100) ?: 0
            if (!done_reading && inputBufferIndex >= 0) {
                sample_size = inputBuffers?.get(inputBufferIndex)?.let { extractor?.readSampleData(it, 0) }
                        ?: 0
                if (firstSampleData
                        && format?.getString(MediaFormat.KEY_MIME) == "audio/mp4a-latm" && sample_size == 2) {
                    // For some reasons on some devices (e.g. the Samsung S3) you should not
                    // provide the first two bytes of an AAC stream, otherwise the MediaCodec will
                    // crash. These two bytes do not contain music data but basic info on the
                    // stream (e.g. channel configuration and sampling frequency), and skipping them
                    // seems OK with other devices (MediaCodec has already been configured and
                    // already knows these parameters).
                    extractor?.advance()
                    tot_size_read += sample_size
                } else if (sample_size < 0) {
                    // All samples have been read.
                    codec?.queueInputBuffer(
                            inputBufferIndex, 0, 0, -1, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    done_reading = true
                } else {
                    presentation_time = extractor?.sampleTime ?: 0
                    codec?.queueInputBuffer(inputBufferIndex, 0, sample_size, presentation_time, 0)
                    extractor?.advance()
                    tot_size_read += sample_size
                    if (mProgressListener != null) {
                        if (mProgressListener?.reportProgress((tot_size_read.toFloat() / mFileSize).toDouble())?.not() == true) {
                            // We are asked to stop reading the file. Returning immediately. The
                            // SoundFile object is invalid and should NOT be used afterward!
                            extractor?.release()
                            extractor = null
                            codec?.stop()
                            codec?.release()
                            codec = null
                            return
                        }
                    }
                }
                firstSampleData = false
            }

            // Get decoded stream from the decoder output buffers.
            val outputBufferIndex = codec?.dequeueOutputBuffer(info, 100) ?: 0
            if (outputBufferIndex >= 0 && info.size > 0) {
                if (decodedSamplesSize < info.size) {
                    decodedSamplesSize = info.size
                    decodedSamples = ByteArray(decodedSamplesSize)
                }
                outputBuffers?.get(outputBufferIndex)?.get(decodedSamples, 0, info.size)
                outputBuffers?.get(outputBufferIndex)?.clear()
                // Check if buffer is big enough. Resize it if it's too small.
                if ((mDecodedBytes?.remaining() ?: 0) < info.size) {
                    // Getting a rough estimate of the total size, allocate 20% more, and
                    // make sure to allocate at least 5MB more than the initial size.
                    val position = (mDecodedBytes?.position() ?: 0)
                    var newSize = (position * (1.0 * mFileSize / tot_size_read) * 1.2).toInt()
                    if (newSize - position < info.size + 5 * (1 shl 20)) {
                        newSize = position + info.size + 5 * (1 shl 20)
                    }
                    var newDecodedBytes: ByteBuffer? = null
                    // Try to allocate memory. If we are OOM, try to run the garbage collector.
                    var retry = 10
                    while (retry > 0) {
                        try {
                            newDecodedBytes = ByteBuffer.allocate(newSize)
                            break
                        } catch (oome: OutOfMemoryError) {
                            // setting android:largeHeap="true" in <application> seem to help not
                            // reaching this section.
                            retry--
                        }
                    }
                    if (retry == 0) {
                        // Failed to allocate memory... Stop reading more data and finalize the
                        // instance with the data decoded so far.
                        break
                    }
                    //ByteBuffer newDecodedBytes = ByteBuffer.allocate(newSize);
                    mDecodedBytes?.rewind()
                    newDecodedBytes?.put(mDecodedBytes)
                    mDecodedBytes = newDecodedBytes
                    mDecodedBytes?.position(position)
                }
                mDecodedBytes?.put(decodedSamples, 0, info.size)
                codec?.releaseOutputBuffer(outputBufferIndex, false)
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                outputBuffers = codec?.outputBuffers
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // Subsequent data will conform to new format.
                // We could check that codec.getOutputFormat(), which is the new output format,
                // is what we expect.
            }
            if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0
                    || (mDecodedBytes?.position() ?: 0) / (2 * mChannels) >= expectedNumSamples) {
                // We got all the decoded data from the decoder. Stop here.
                // Theoretically dequeueOutputBuffer(info, ...) should have set info.flags to
                // MediaCodec.BUFFER_FLAG_END_OF_STREAM. However some phones (e.g. Samsung S3)
                // won't do that for some files (e.g. with mono AAC files), in which case subsequent
                // calls to dequeueOutputBuffer may result in the application crashing, without
                // even an exception being thrown... Hence the second check.
                // (for mono AAC files, the S3 will actually double each sample, as if the stream
                // was stereo. The resulting stream is half what it's supposed to be and with a much
                // lower pitch.)
                break
            }
        }
        mNumSamples = (mDecodedBytes?.position() ?: 0) / (mChannels * 2) // One sample = 2 bytes.
        mDecodedBytes?.rewind()
        mDecodedBytes?.order(ByteOrder.LITTLE_ENDIAN)
        mDecodedSamples = mDecodedBytes?.asShortBuffer()
        mAvgBitRate = (mFileSize * 8 * (mSampleRate.toFloat() / mNumSamples) / 1000).toInt()
        extractor?.release()
        extractor = null
        codec?.stop()
        codec?.release()
        codec = null

        // Temporary hack to make it work with the old version.
        mNumFrames = mNumSamples / getSamplesPerFrame()
        if (mNumSamples % getSamplesPerFrame() != 0) {
            mNumFrames++
        }
        mFrameGains = IntArray(mNumFrames)
        mFrameLens = IntArray(mNumFrames)
        mFrameOffsets = IntArray(mNumFrames)
        var j: Int
        var gain: Int
        var value: Int
        val frameLens = (1000 * mAvgBitRate / 8 *
                (getSamplesPerFrame().toFloat() / mSampleRate)).toInt()
        i = 0
        while (i < mNumFrames) {
            gain = -1
            j = 0
            while (j < getSamplesPerFrame()) {
                value = 0
                for (k in 0 until mChannels) {
                    if ((mDecodedSamples?.remaining() ?: 0) > 0) {
                        value += Math.abs(mDecodedSamples?.get()?.toInt() ?: 0)
                    }
                }
                value /= mChannels
                if (gain < value) {
                    gain = value
                }
                j++
            }
            mFrameGains?.set(i, Math.sqrt(gain.toDouble()).toInt()) // here gain = sqrt(max value of 1st channel)...
            mFrameLens?.set(i, frameLens) // totally not accurate...
            mFrameOffsets?.set(i, (i * (1000 * mAvgBitRate / 8) *  //  = i * frameLens
                    (getSamplesPerFrame().toFloat() / mSampleRate)).toInt())
            i++
        }
        mDecodedSamples?.rewind()
        // DumpSamples();  // Uncomment this line to dump the samples in a TSV file.
    }

    private fun RecordAudio() {
        if (mProgressListener == null) {
            // A progress listener is mandatory here, as it will let us know when to stop recording.
            return
        }
        mInputFile = null
        mFileType = "raw"
        mFileSize = 0
        mSampleRate = 44100
        mChannels = 1 // record mono audio.
        val buffer = ShortArray(1024) // buffer contains 1 mono frame of 1024 16 bits samples
        var minBufferSize = AudioRecord.getMinBufferSize(
                mSampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        // make sure minBufferSize can contain at least 1 second of audio (16 bits sample).
        if (minBufferSize < mSampleRate * 2) {
            minBufferSize = mSampleRate * 2
        }
        val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.DEFAULT,
                mSampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize
        )

        // Allocate memory for 20 seconds first. Reallocate later if more is needed.
        mDecodedBytes = ByteBuffer.allocate(20 * mSampleRate * 2)
        mDecodedBytes?.order(ByteOrder.LITTLE_ENDIAN)
        mDecodedSamples = mDecodedBytes?.asShortBuffer()
        audioRecord.startRecording()
        while (true) {
            // check if mDecodedSamples can contain 1024 additional samples.
            if ((mDecodedSamples?.remaining() ?: 0) < 1024) {
                // Try to allocate memory for 10 additional seconds.
                val newCapacity = (mDecodedBytes?.capacity() ?: 0) + 10 * mSampleRate * 2
                var newDecodedBytes: ByteBuffer? = null
                newDecodedBytes = try {
                    ByteBuffer.allocate(newCapacity)
                } catch (oome: OutOfMemoryError) {
                    break
                }
                val position = mDecodedSamples?.position() ?: 0
                mDecodedBytes?.rewind()
                newDecodedBytes?.put(mDecodedBytes)
                mDecodedBytes = newDecodedBytes
                mDecodedBytes?.order(ByteOrder.LITTLE_ENDIAN)
                mDecodedBytes?.rewind()
                mDecodedSamples = mDecodedBytes?.asShortBuffer()
                mDecodedSamples?.position(position)
            }
            // TODO(nfaralli): maybe use the read method that takes a direct ByteBuffer argument.
            audioRecord.read(buffer, 0, buffer.size)
            mDecodedSamples?.put(buffer)
            // Let the progress listener know how many seconds have been recorded.
            // The returned value tells us if we should keep recording or stop.
            if (mProgressListener?.reportProgress((
                            (mDecodedSamples?.position()
                                    ?: 0).toFloat() / mSampleRate).toDouble())?.not() == true) {
                break
            }
        }
        audioRecord.stop()
        audioRecord.release()
        mNumSamples = mDecodedSamples?.position() ?: 0
        mDecodedSamples?.rewind()
        mDecodedBytes?.rewind()
        mAvgBitRate = mSampleRate * 16 / 1000

        // Temporary hack to make it work with the old version.
        mNumFrames = mNumSamples / getSamplesPerFrame()
        if (mNumSamples % getSamplesPerFrame() != 0) {
            mNumFrames++
        }
        mFrameGains = IntArray(mNumFrames)
        mFrameLens = null // not needed for recorded audio
        mFrameOffsets = null // not needed for recorded audio
        var i: Int
        var j: Int
        var gain: Int
        var value: Int
        i = 0
        while (i < mNumFrames) {
            gain = -1
            j = 0
            while (j < getSamplesPerFrame()) {
                value = if ((mDecodedSamples?.remaining() ?: 0) > 0) {
                    Math.abs(mDecodedSamples?.get()?.toInt() ?: 0)
                } else {
                    0
                }
                if (gain < value) {
                    gain = value
                }
                j++
            }
            mFrameGains?.set(i, Math.sqrt(gain.toDouble()).toInt()) // here gain = sqrt(max value of 1st channel)...
            i++
        }
        mDecodedSamples?.rewind()
        // DumpSamples();  // Uncomment this line to dump the samples in a TSV file.
    }

    // should be removed in the near future...
    @Throws(IOException::class)
    fun WriteFile(outputFile: File?, startFrame: Int, numFrames: Int) {
        val startTime = startFrame.toFloat() * getSamplesPerFrame() / mSampleRate
        val endTime = (startFrame + numFrames).toFloat() * getSamplesPerFrame() / mSampleRate
        WriteFile(outputFile, startTime, endTime)
    }

    @Throws(IOException::class)
    fun WriteFile(outputFile: File?, startTime: Float, endTime: Float) {
        val startOffset = (startTime * mSampleRate).toInt() * 2 * mChannels
        var numSamples = ((endTime - startTime) * mSampleRate).toInt()
        // Some devices have problems reading mono AAC files (e.g. Samsung S3). Making it stereo.
        val numChannels = if (mChannels == 1) 2 else mChannels
        val mimeType = "audio/mp4a-latm"
        val bitrate = 64000 * numChannels // rule of thumb for a good quality: 64kbps per channel.
        var codec: MediaCodec? = MediaCodec.createEncoderByType(mimeType)
        val format = MediaFormat.createAudioFormat(mimeType, mSampleRate, numChannels)
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
        codec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codec?.start()

        // Get an estimation of the encoded data based on the bitrate. Add 10% to it.
        var estimatedEncodedSize = ((endTime - startTime) * (bitrate / 8) * 1.1).toInt()
        var encodedBytes = ByteBuffer.allocate(estimatedEncodedSize)
        val inputBuffers = codec?.inputBuffers
        var outputBuffers = codec?.outputBuffers
        val info = MediaCodec.BufferInfo()
        var done_reading = false
        var presentation_time: Long = 0
        val frame_size = 1024 // number of samples per frame per channel for an mp4 (AAC) stream.
        var buffer = ByteArray(frame_size * numChannels * 2) // a sample is coded with a short.
        mDecodedBytes?.position(startOffset)
        numSamples += 2 * frame_size // Adding 2 frames, Cf. priming frames for AAC.
        var tot_num_frames = 1 + numSamples / frame_size // first AAC frame = 2 bytes
        if (numSamples % frame_size != 0) {
            tot_num_frames++
        }
        val frame_sizes = IntArray(tot_num_frames)
        var num_out_frames = 0
        var num_frames = 0
        var num_samples_left = numSamples
        var encodedSamplesSize = 0 // size of the output buffer containing the encoded samples.
        var encodedSamples: ByteArray? = null
        while (true) {
            // Feed the samples to the encoder.
            val inputBufferIndex = codec?.dequeueInputBuffer(100) ?: 0
            if (!done_reading && inputBufferIndex >= 0) {
                if (num_samples_left <= 0) {
                    // All samples have been read.
                    codec?.queueInputBuffer(
                            inputBufferIndex, 0, 0, -1, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    done_reading = true
                } else {
                    inputBuffers?.get(inputBufferIndex)?.clear()
                    if (buffer.size > inputBuffers?.get(inputBufferIndex)?.remaining() ?: 0) {
                        // Input buffer is smaller than one frame. This should never happen.
                        continue
                    }
                    // bufferSize is a hack to create a stereo file from a mono stream.
                    val bufferSize = if (mChannels == 1) buffer.size / 2 else buffer.size
                    if ((mDecodedBytes?.remaining() ?: 0) < bufferSize) {
                        for (i in (mDecodedBytes?.remaining() ?: 0) until bufferSize) {
                            buffer[i] = 0 // pad with extra 0s to make a full frame.
                        }
                        mDecodedBytes?.get(buffer, 0, mDecodedBytes?.remaining() ?: 0)
                    } else {
                        mDecodedBytes?.get(buffer, 0, bufferSize)
                    }
                    if (mChannels == 1) {
                        var i = bufferSize - 1
                        while (i >= 1) {
                            buffer[2 * i + 1] = buffer[i]
                            buffer[2 * i] = buffer[i - 1]
                            buffer[2 * i - 1] = buffer[2 * i + 1]
                            buffer[2 * i - 2] = buffer[2 * i]
                            i -= 2
                        }
                    }
                    num_samples_left -= frame_size
                    inputBuffers?.get(inputBufferIndex)?.put(buffer)
                    presentation_time = (num_frames++ * frame_size * 1e6 / mSampleRate).toLong()
                    codec?.queueInputBuffer(
                            inputBufferIndex, 0, buffer.size, presentation_time, 0)
                }
            }

            // Get the encoded samples from the encoder.
            val outputBufferIndex = codec?.dequeueOutputBuffer(info, 100) ?: 0
            if (outputBufferIndex >= 0 && info.size > 0 && info.presentationTimeUs >= 0) {
                if (num_out_frames < frame_sizes.size) {
                    frame_sizes[num_out_frames++] = info.size
                }
                if (encodedSamplesSize < info.size) {
                    encodedSamplesSize = info.size
                    encodedSamples = ByteArray(encodedSamplesSize)
                }
                outputBuffers?.get(outputBufferIndex)?.get(encodedSamples, 0, info.size)
                outputBuffers?.get(outputBufferIndex)?.clear()
                codec?.releaseOutputBuffer(outputBufferIndex, false)
                if (encodedBytes.remaining() < info.size) {  // Hopefully this should not happen.
                    estimatedEncodedSize = (estimatedEncodedSize * 1.2).toInt() // Add 20%.
                    val newEncodedBytes = ByteBuffer.allocate(estimatedEncodedSize)
                    val position = encodedBytes.position()
                    encodedBytes.rewind()
                    newEncodedBytes.put(encodedBytes)
                    encodedBytes = newEncodedBytes
                    encodedBytes.position(position)
                }
                encodedBytes.put(encodedSamples, 0, info.size)
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                outputBuffers = codec?.outputBuffers
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // Subsequent data will conform to new format.
                // We could check that codec.getOutputFormat(), which is the new output format,
                // is what we expect.
            }
            if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                // We got all the encoded data from the encoder.
                break
            }
        }
        val encoded_size = encodedBytes.position()
        encodedBytes.rewind()
        codec?.stop()
        codec?.release()
        codec = null

        // Write the encoded stream to the file, 4kB at a time.
        buffer = ByteArray(4096)
        try {
            val outputStream = FileOutputStream(outputFile)
            outputStream.write(
                    MP4Header.Companion.getMP4Header(mSampleRate, numChannels, frame_sizes, bitrate))
            while (encoded_size - encodedBytes.position() > buffer.size) {
                encodedBytes[buffer]
                outputStream.write(buffer)
            }
            val remaining = encoded_size - encodedBytes.position()
            if (remaining > 0) {
                encodedBytes[buffer, 0, remaining]
                outputStream.write(buffer, 0, remaining)
            }
            outputStream.close()
        } catch (e: IOException) {
            Log.e("Ringdroid", "Failed to create the .m4a file.")
            Log.e("Ringdroid", getStackTrace(e))
        }
    }

    // Return the stack trace of a given exception.
    private fun getStackTrace(e: Exception): String {
        val writer = StringWriter()
        e.printStackTrace(PrintWriter(writer))
        return writer.toString()
    }

    // Progress listener interface.
    interface ProgressListener {
        /**
         * Will be called by the SoundFile class periodically
         * with values between 0.0 and 1.0.  Return true to continue
         * loading the file or recording the audio, and false to cancel or stop recording.
         */
        fun reportProgress(fractionComplete: Double): Boolean
    }

    // Custom exception for invalid inputs.
    inner class InvalidInputException(message: String?) : Exception(message) {

    }

    companion object {
        // TODO(nfaralli): what is the real list of supported extensions? Is it device dependent?
        fun getSupportedExtensions(): Array<String> {
            return arrayOf("mp3", "wav", "3gpp", "3gp", "amr", "aac", "m4a", "ogg")
        }

        fun isFilenameSupported(filename: String): Boolean {
            val extensions = getSupportedExtensions()
            for (i in extensions.indices) {
                if (filename.endsWith("." + extensions[i])) {
                    return true
                }
            }
            return false
        }

        // Create and return a SoundFile object using the file fileName.
        @JvmStatic
        @Throws(IOException::class, InvalidInputException::class)
        fun create(fileName: String?,
                   progressListener: ProgressListener): SoundFile? {
            // First check that the file exists and that its extension is supported.
            val f = File(fileName)
            if (!f.exists()) {
                throw FileNotFoundException(fileName)
            }
            val name = f.name.toLowerCase()
            val components = name.split("\\.".toRegex()).toTypedArray()
            if (components.size < 2) {
                return null
            }
            if (!Arrays.asList(*getSupportedExtensions()).contains(components[components.size - 1])) {
                return null
            }
            val soundFile = SoundFile()
            soundFile.setProgressListener(progressListener)
            soundFile.ReadFile(f)
            return soundFile
        }

        // Create and return a SoundFile object by recording a mono audio stream.
        fun record(progressListener: ProgressListener?): SoundFile? {
            if (progressListener == null) {
                // must have a progessListener to stop the recording.
                return null
            }
            val soundFile = SoundFile()
            soundFile.setProgressListener(progressListener)
            soundFile.RecordAudio()
            return soundFile
        }
    }
}