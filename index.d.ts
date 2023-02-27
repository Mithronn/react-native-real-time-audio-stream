declare module "react-native-real-time-audio-stream" {
    export interface DataEvent {
        opus_data: string,
        amplitude: number,
        frequency: number,
        decibel: number,
    }

    export interface AudioRecordStreamInterface {
        init: (options: Options) => void
        start: () => void
        stop: () => Promise<string>
        pause: () => Promise<string>
        resume: () => Promise<string>
        toggleNoiseSuppression: (state: boolean) => void
        on: (event: "data", callback: (data: DataEvent) => void) => void
    }

    export interface Options {
        /**
         * Sample Rate in Hz
         * - `NB 8000 | MB 12000 | WB 16000 | SWB 24000 | FB 48000`
         * @default 48000
         */
        sampleRate: 8000 | 12000 | 16000 | 24000 | 48000
        /**
         * Channel MONO or STEREO
         * - `1 | 2` 
         * @default MONO 1
         */
        channels: 1 | 2
        /**
         * ENCODING_PCM_8BIT or ENCODING_PCM_16BIT
         * - `8 | 16` 
         * @default ENCODING_PCM_16BIT 16
         */
        bitsPerSample: 8 | 16
        /**
         * - `VOICE_COMMUNICATION = 7`
         * - Other audio sources can find in https://developer.android.com/reference/android/media/MediaRecorder.AudioSource 
         * @default 7
         * @android only
         */
        audioSource?: number
        /**
         * Enable noise suppressor
         * @default false
         * @android only
         */
        noiseSuppression?: boolean
    }

    const AudioRecordStream: AudioRecordStreamInterface

    export default AudioRecordStream;
}