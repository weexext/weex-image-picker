# weex-image-picker
weex-image-picker是一个weex插件，可以通过weexpack快速集成，可以丰富weex功能

支持的weexpack版本： >= 0.2.0
支持的WeexSDK版本： >= 0.10.0

# 功能

# 快速使用
- 通过weexpack初始化一个测试工程 weextest
   ```
   weexpack create weextest
   ```
- 添加ios平台
  ```
  weexpack platform add ios
  ```
- 添加android平台
  ```
  weexpack platform add android
  ```
- 添加插件
  ```
  weexpack plugin add weex-image-picker
  ```
# 项目地址
[github](https://github.com/weexext/weex-image-picker.git)

# 已有工程集成
## iOS集成插件WeexImagePicker
- 命令行集成
  ```
  weexpack plugin add weex-image-picker
  ```
- 手动集成
  在podfile 中添加
  ```
  pod 'WeexImagePicker'
  ```

## 安卓集成插件weeximagepicker
- 命令行集成
  ```
  weexpack plugin add weex-image-picker
  ```
- 手动集成
  在相应工程的build.gradle文件的dependencies中添加
  ```
  compile '${groupId}:weeximagepicker:{$version}'
  ``` 
  注意：您需要自行指定插件的groupId和version并将构建产物发布到相应的依赖管理仓库内去（例如maven）, 您也可以对插件的name进行自定义，默认将使用插件工程的名称作为name


  
---
#详解:
# Weex Image Picker [![npm version](https://market.dotwe.org/#91)]

A Weex module that allows you to use native UI to select a photo/video from the device library or directly from the camera, like so:

iOS | Android
------- | ----
<img title="iOS" src="https://github.com/weexext/weex-image-picker/blob/master/images/ios-image.png"> | <img title="Android" src="https://github.com/weexext/weex-image-picker/blob/master/images/android-image.png">

#### _Before you open an issue_
This library started as a basic bridge of the native iOS image picker, and I want to keep it that way. As such, functionality beyond what the native `UIImagePickerController` supports will not be supported here. **Multiple image selection, more control over the crop tool, and landscape support** are things missing from the native iOS functionality - **not issues with my library**. If you need these things, [weex-image-crop-picker](https://github.com/weexext/weex-image-crop-picker) might be a better choice for you.   

## Table of contents
- [Install](#install)
- [Usage](#usage)
- [Direct launch](#directly-launching-the-camera-or-image-library)
- [Options](#options)
- [Response object](#the-response-object)

## Install

### NOTE: THIS PACKAGE IS NOW BUILT FOR REACT NATIVE 0.40 OR GREATER! IF YOU NEED TO SUPPORT REACT NATIVE < 0.40, YOU SHOULD INSTALL THIS PACKAGE `@0.24`

`npm install weex-image-picker@latest --save`

### Automatic Installation

`weex link`

IMPORTANT NOTE: You'll still need to perform step 4 for iOS and steps 2, 3, and 5 for Android of the manual instructions below.

### Manual Installation

#### iOS

1. In the XCode's "Project navigator", right click on your project's Libraries folder ➜ `Add Files to <...>`
2. Go to `node_modules` ➜ `weex-image-picker` ➜ `ios` ➜ select `RNImagePicker.xcodeproj`
3. Add `RNImagePicker.a` to `Build Phases -> Link Binary With Libraries`
4. For iOS 10+, Add the `NSPhotoLibraryUsageDescription`, `NSCameraUsageDescription`, and `NSMicrophoneUsageDescription` (if allowing video) keys to your `Info.plist` with strings describing why your app needs these permissions. **Note: You will get a SIGABRT crash if you don't complete this step**
5. Compile and have fun

#### Android
1. Add the following lines to `android/settings.gradle`:
    ```gradle
    include ':weex-image-picker'
    project(':weex-image-picker').projectDir = new File(rootProject.projectDir, '../node_modules/weex-image-picker/android')
    ```
    
2. Update the android build tools version to `2.2.+` in `android/build.gradle`:
    ```gradle
    buildscript {
        ...
        dependencies {
            classpath 'com.android.tools.build:gradle:2.2.+' // <- USE 2.2.+ version
        }
        ...
    }
    ...
    ``` 
    
3. Update the gradle version to `2.14.1` in `android/gradle/wrapper/gradle-wrapper.properties`:
    ```
    ...
    distributionUrl=https\://services.gradle.org/distributions/gradle-2.14.1-all.zip
    ```
    
4. Add the compile line to the dependencies in `android/app/build.gradle`:
    ```gradle
    dependencies {
        compile project('org.weex.plugin:weex-image-picker')
    }
    ```
    
5. Add the required permissions in `AndroidManifest.xml`:
    ```xml
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    ```
    
6. Add the import and link the package in `WXApplication.java`:
    ```java
    import com.imagepicker.ImagePickerPackage; // <-- add this import

    public class WXApplication extends Application  {
        WXSDKEngine.registerModule("imagePicker", ImagePickerModule.class);
    }
    ```

##### Android (Optional)

Customization settings of dialog `android/app/res/values/themes.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="DefaultExplainingPermissionsTheme" parent="Theme.AppCompat.Light.Dialog.Alert">
        <!-- Used for the buttons -->
        <item name="colorAccent">@color/your_color</item>

        <!-- Used for the title and text -->
        <item name="android:textColorPrimary">@color/your_color</item>

        <!-- Used for the background -->
        <item name="android:background">@color/your_color</item>
    </style>
<resources>
```

you need to extend `AbstractWeexActivity`:
```java
                public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
                  super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                  mInstance.onRequestPermissionsResult(requestCode, permissions, grantResults);
                }
  // To here
}
```
This code allows to pass result of request permissions to native part.

## Usage

```javascript
var ImagePicker =weex.requireModule('imagePicker');


// More info on all the options is below in the README...just some common use cases shown here
var options = {
  title: 'Select Avatar',
  customButtons: [
    {name: 'fb', title: 'Choose Photo from Facebook'},
  ],
  storageOptions: {
    skipBackup: true,
    path: 'images'
  }
};

/**
 * The first arg is the options object for customization (it can also be null or omitted for default options),
 * The second arg is the callback which sends object: response (more info below in README)
 */
ImagePicker.showImagePicker(options, (response) => {
  console.log('Response = ', response);

  if (response.didCancel) {
    console.log('User cancelled image picker');
  }
  else if (response.error) {
    console.log('ImagePicker Error: ', response.error);
  }
  else if (response.customButton) {
    console.log('User tapped custom button: ', response.customButton);
  }
  else {
    let source = { uri: response.uri };

    // You can also display the image using data:
    // let source = { uri: 'data:image/jpeg;base64,' + response.data };
  }
});
```
Then later, if you want to display this image in your render() method:
```javascript
 <image style="height: 300px ;width: 300px;" :src="imageUrl"></image>
```

### Directly Launching the Camera or Image Library

To Launch the Camera or Image Library directly (skipping the alert dialog) you can
do the following:
```javascript
// Launch Camera:
ImagePicker.launchCamera(options, (response)  => {
  // Same code as in above section!
});

// Open Image Library:
ImagePicker.launchImageLibrary(options, (response)  => {
  // Same code as in above section!
});
```


#### Note
On iOS, don't assume that the absolute uri returned will persist. See [#107](/../../issues/107)

### Options

option | iOS  | Android | Info
------ | ---- | ------- | ----
title | OK | OK | Specify `null` or empty string to remove the title
cancelButtonTitle | OK | OK | Specify `null` or empty string to remove this button (Android only)
takePhotoButtonTitle | OK | OK | Specify `null` or empty string to remove this button
chooseFromLibraryButtonTitle | OK | OK | Specify `null` or empty string to remove this button
customButtons | OK | OK | An array containing objects with the name and title of buttons
cameraType | OK | - | 'front' or 'back'
mediaType | OK | OK | 'photo', 'video', or 'mixed' on iOS, 'photo' or 'video' on Android
maxWidth | OK | OK | Photos only
maxHeight | OK | OK | Photos only
quality | OK | OK | 0 to 1, photos only
videoQuality | OK |  OK | 'low', 'medium', or 'high' on iOS, 'low' or 'high' on Android
durationLimit | OK | OK | Max video recording time, in seconds
rotation | - | OK | Photos only, 0 to 360 degrees of rotation
allowsEditing | OK | - | bool - enables built in iOS functionality to resize the image after selection
noData | OK | OK | If true, disables the base64 `data` field from being generated (greatly improves performance on large photos)
storageOptions | OK | OK | If this key is provided, the image will be saved in your app's `Documents` directory on iOS, or your app's `Pictures` directory on Android (rather than a temporary directory)
storageOptions.skipBackup | OK | - | If true, the photo will NOT be backed up to iCloud
storageOptions.path | OK | - | If set, will save the image at `Documents/[path]/` rather than the root `Documents`
storageOptions.cameraRoll | OK | OK | If true, the cropped photo will be saved to the iOS Camera Roll or Android DCIM folder.
storageOptions.waitUntilSaved | OK | - | If true, will delay the response callback until after the photo/video was saved to the Camera Roll. If the photo or video was just taken, then the file name and timestamp fields are only provided in the response object when this is true.
permissionDenied.title | - | OK | Title of explaining permissions dialog. By default `Permission denied`.
permissionDenied.text | - | OK | Message of explaining permissions dialog. By default `To be able to take pictures with your camera and choose images from your library.`.
permissionDenied.reTryTitle | - | OK | Title of re-try button. By default `re-try`
permissionDenied.okTitle | - | OK | Title of ok button. By default `I'm sure`

### The Response Object

key | iOS | Android | Description
------ | ---- | ------- | ----------------------
didCancel | OK | OK | Informs you if the user cancelled the process
error | OK | OK | Contains an error message, if there is one
customButton | OK | OK | If the user tapped one of your custom buttons, contains the name of it
data | OK | OK | The base64 encoded image data (photos only)
uri | OK | OK | The uri to the local file asset on the device (photo or video)
origURL | OK | - | The URL of the original asset in photo library, if it exists
isVertical | OK | OK | Will be true if the image is vertically oriented
width | OK | OK | Image dimensions
height | OK | OK | Image dimensions
fileSize | OK | OK | The file size (photos only)
type | - | OK | The file type (photos only)
fileName | OK (photos and videos) | OK (photos) | The file name
path | - | OK | The file path
latitude | OK | OK | Latitude metadata, if available
longitude | OK | OK | Longitude metadata, if available
timestamp | OK | OK | Timestamp metadata, if available, in ISO8601 UTC format
originalRotation | - | OK | Rotation degrees (photos only) *See [#109](/../../issues/199)*

