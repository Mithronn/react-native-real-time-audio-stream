declare module "react-native-real-time-audio-stream" {
    export interface DataEvent {
        data: string,
        amplitude: number,
        decibel_level: number,
        decibel_raw_level: number,
    }

    export interface AudioRecordStreamInterface {
        init: (options: Options) => void
        start: () => void
        stop: () => Promise<string>
        pause: () => Promise<string>
        resume: () => Promise<string>
        on: (event: "data", callback: (data: DataEvent) => void) => void
    }

    export interface Options {
        sampleRate: number
        /**
         * - `1 | 2`
         */
        channels: 1 | 2
        /**
         * - `8 | 16`
         */
        bitsPerSample: 8 | 16
        /**
         * - `6`
         */
        audioSource?: number
        bufferSize?: number
    }

    const AudioRecordStream: AudioRecordStreamInterface

    export default AudioRecordStream;
}