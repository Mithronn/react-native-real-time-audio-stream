declare module "react-native-real-time-audio-stream" {
    export interface DataEvent {
        raw_data: string,
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
        on: (event: "data", callback: (data: DataEvent) => void) => void
        on: (event: "error", callback: (data: string) => void) => void
    }

    export interface Options {
        /**
         * Sample Rate in Hz
         * @default 44100
         */
        sampleRate: number
        /**
         * - `1 | 2` 
         * @default 1
         */
        channels: 1 | 2
        /**
         * - `8 | 16` 
         * @default 16
         */
        bitsPerSample: 8 | 16
        /**
         * - `6` 
         * @default 6
         * @android only
         */
        audioSource?: number
        /**
         * bufferSize
         * @default 2048
         */
        bufferSize?: number
    }

    const AudioRecordStream: AudioRecordStreamInterface

    export default AudioRecordStream;
}