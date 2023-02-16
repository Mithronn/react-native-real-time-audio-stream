
# react-native-real-time-audio-stream

## Getting started

`$ npm install react-native-real-time-audio-stream --save`

### Mostly automatic installation

`$ react-native link react-native-real-time-audio-stream`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-real-time-audio-stream` and add `RNRealTimeAudioStream.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNRealTimeAudioStream.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNRealTimeAudioStreamPackage;` to the imports at the top of the file
  - Add `new RNRealTimeAudioStreamPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-real-time-audio-stream'
  	project(':react-native-real-time-audio-stream').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-real-time-audio-stream/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-real-time-audio-stream')
  	```


## Usage
```javascript
import RNRealTimeAudioStream from 'react-native-real-time-audio-stream';

const options = {
	sampleRate: 16000 // Default is 44100
    channels: 1 // Default is 1
    bitsPerSample: // Default is 16
    audioSource?: number // Default is 6 @android
    bufferSize?: number // Default is 2048
}

// You need to initialize before start recording
RNRealTimeAudioStream.init(options);

// 
RNRealTimeAudioStream.on('data',data => {
	/*
	data = {
		data: string -> base64-encoded audio chunk data
		amplitude: number -> amplitude value
		decibel_level: numbe -> sound level in decibels, -160 is a silence level
		decibel_raw_value: number -> 0 or amplitude value
	}
	*/
});

RNRealTimeAudioStream.start();

RNRealTimeAudioStream.stop();

// Its only stop data emitting but record object still record
RNRealTimeAudioStream.pause();

RNRealTimeAudioStream.resume();

```
  