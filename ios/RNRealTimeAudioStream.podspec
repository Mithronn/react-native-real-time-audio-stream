require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name         = "RNRealTimeAudioStream"
  s.version        = package['version']
  s.summary        = package['description']
  s.description    = package['description']
  s.license        = package['license']
  s.author         = package['author']
  s.homepage       = "https://github.com/Mithronn/react-native-real-time-audio-stream"
  s.source       = { :git => "https://github.com/Mithronn/react-native-real-time-audio-stream.git", :tag => "main" }
  s.source_files  = "RNRealTimeAudioStream/**/*.{h,m}"
  s.requires_arc = true

  s.preserve_paths = 'README.md', 'package.json', 'index.js'

  s.dependency "React"
end

  