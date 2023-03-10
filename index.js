import { NativeModules, NativeEventEmitter } from 'react-native';

const { RNRealTimeAudioStream } = NativeModules;
const EventEmitter = new NativeEventEmitter(RNRealTimeAudioStream);
const AudioRecordStream = {};

AudioRecordStream.init = options => RNRealTimeAudioStream.init(options);
AudioRecordStream.start = () => RNRealTimeAudioStream.start();
AudioRecordStream.stop = () => RNRealTimeAudioStream.stop();
AudioRecordStream.pause = () => RNRealTimeAudioStream.pause();
AudioRecordStream.resume = () => RNRealTimeAudioStream.resume();
AudioRecordStream.toggleNoiseSuppression = state => RNRealTimeAudioStream.toggleNoiseSuppression(state);

const eventsMap = {
    data: 'data',
    error: 'error'
};

AudioRecordStream.on = (event, callback) => {
    const nativeEvent = eventsMap[event];
    if (!nativeEvent) {
        throw new Error('Invalid event');
    }
    EventEmitter.removeAllListeners(nativeEvent);
    return EventEmitter.addListener(nativeEvent, callback);
};

export default AudioRecordStream;