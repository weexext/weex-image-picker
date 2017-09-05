package com.imagepicker;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Patterns;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.imagepicker.media.ImageConfig;
import com.imagepicker.permissions.OnImagePickerPermissionsCallback;
import com.imagepicker.permissions.PermissionUtils;
import com.imagepicker.utils.MediaUtils.ReadExifResult;
import com.imagepicker.utils.RealPathUtil;
import com.imagepicker.utils.UI;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXModule;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import static com.imagepicker.utils.MediaUtils.RolloutPhotoResult;
import static com.imagepicker.utils.MediaUtils.createNewFile;
import static com.imagepicker.utils.MediaUtils.fileScan;
import static com.imagepicker.utils.MediaUtils.getResizedImage;
import static com.imagepicker.utils.MediaUtils.readExifInterface;
import static com.imagepicker.utils.MediaUtils.removeUselessFiles;
import static com.imagepicker.utils.MediaUtils.rolloutPhotoFromCamera;

public class ImagePickerModule extends WXModule {

    public static final int REQUEST_LAUNCH_IMAGE_CAPTURE = 13001;
    public static final int REQUEST_LAUNCH_IMAGE_LIBRARY = 13002;
    public static final int REQUEST_LAUNCH_VIDEO_LIBRARY = 13003;
    public static final int REQUEST_LAUNCH_VIDEO_CAPTURE = 13004;
    public static final int REQUEST_PERMISSIONS_FOR_CAMERA = 14001;
    public static final int REQUEST_PERMISSIONS_FOR_LIBRARY = 14002;

    private  Application application = null;
    private final int dialogThemeId = R.style.DefaultExplainingPermissionsTheme;

    protected JSCallback callback;
    private JSONObject options;
    protected Uri cameraCaptureURI;
    private Boolean noData = false;
    private Boolean pickVideo = false;
    private ImageConfig imageConfig = new ImageConfig(null, null, 0, 0, 100, 0, false);

    @Override
    public void onActivityCreate() {
        super.onActivityCreate();
    }

    @Deprecated
    private int videoQuality = 1;

    @Deprecated
    private int videoDurationLimit = 0;

    private ResponseHelper responseHelper = new ResponseHelper();

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean permissionsGranted = true;

        for (int i = 0; i < permissions.length; i++) {
            final boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
            permissionsGranted = permissionsGranted && granted;
        }

        if (callback == null || options == null) {
            return ;
        }

        if (!permissionsGranted) {
            responseHelper.invokeError(callback, "Permissions weren't granted");
            return ;
        }

        switch (requestCode) {
            case REQUEST_PERMISSIONS_FOR_CAMERA:
                launchCamera(options, callback);
                break;

            case REQUEST_PERMISSIONS_FOR_LIBRARY:
                launchImageLibrary(options, callback);
                break;

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @JSMethod
    public void test(){
        Toast.makeText(getContext(), "test", Toast.LENGTH_LONG).show();
    }
    @JSMethod
    public void showImagePicker(final JSONObject options, final JSCallback callback) {
        Activity currentActivity = getActivity();
        application = getActivity().getApplication();
        if (currentActivity == null) {
            responseHelper.invokeError(callback, "can't find current Activity");
            return;
        }

        this.callback = callback;
        this.options = options;
        imageConfig = new ImageConfig(null, null, 0, 0, 100, 0, false);

        final AlertDialog dialog = UI.chooseDialog(this, options, new UI.OnAction() {
            @Override
            public void onTakePhoto(@NonNull final ImagePickerModule module) {
                if (module == null) {
                    return;
                }
                module.launchCamera();
            }

            @Override
            public void onUseLibrary(@NonNull final ImagePickerModule module) {
                if (module == null) {
                    return;
                }
                module.launchImageLibrary();
            }

            @Override
            public void onCancel(@NonNull final ImagePickerModule module) {
                if (module == null) {
                    return;
                }
                module.doOnCancel();
            }

            @Override
            public void onCustomButton(@NonNull final ImagePickerModule module,
                                       @NonNull final String action) {
                if (module == null) {
                    return;
                }
                module.invokeCustomButton(action);
            }
        });
        dialog.show();
    }

    public void doOnCancel() {
        responseHelper.invokeCancel(callback);
    }

    public void launchCamera() {
        this.launchCamera(this.options, this.callback);
    }

    // NOTE: Currently not reentrant / doesn't support concurrent requests
    @JSMethod
    public void launchCamera(final JSONObject options, final JSCallback callback) {
        application = getActivity().getApplication();
        if (!isCameraAvailable()) {
            responseHelper.invokeError(callback, "Camera not available");
            return;
        }

        final Activity currentActivity = getActivity();
        if (currentActivity == null) {
            responseHelper.invokeError(callback, "can't find current Activity");
            return;
        }

        this.options = options;

        if (!permissionsCheck(currentActivity, callback, REQUEST_PERMISSIONS_FOR_CAMERA)) {
            return;
        }

        parseOptions(this.options);

        int requestCode;
        Intent cameraIntent;

        if (pickVideo) {
            requestCode = REQUEST_LAUNCH_VIDEO_CAPTURE;
            cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, videoQuality);
            if (videoDurationLimit > 0) {
                cameraIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, videoDurationLimit);
            }
        } else {
            requestCode = REQUEST_LAUNCH_IMAGE_CAPTURE;
            cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            final File original = createNewFile(application, this.options, false);
            imageConfig = imageConfig.withOriginalFile(original);

            cameraCaptureURI = RealPathUtil.compatUriFromFile(application, imageConfig.original);
            if (cameraCaptureURI == null) {
                responseHelper.invokeError(callback, "Couldn't get file path for photo");
                return;
            }
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraCaptureURI);
        }

        if (cameraIntent.resolveActivity(application.getPackageManager()) == null) {
            responseHelper.invokeError(callback, "Cannot launch camera");
            return;
        }

        this.callback = callback;

        try {
            currentActivity.startActivityForResult(cameraIntent, requestCode);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            responseHelper.invokeError(callback, "Cannot launch camera");
        }
    }

    public void launchImageLibrary() {
        this.launchImageLibrary(this.options, this.callback);
    }

    // NOTE: Currently not reentrant / doesn't support concurrent requests
    @JSMethod
    public void launchImageLibrary(final JSONObject options, final JSCallback callback) {
        application = getActivity().getApplication();
        final Activity currentActivity = getActivity();
        if (currentActivity == null) {
            responseHelper.invokeError(callback, "can't find current Activity");
            return;
        }

        this.options = options;

        if (!permissionsCheck(currentActivity, callback, REQUEST_PERMISSIONS_FOR_LIBRARY)) {
            return;
        }

        parseOptions(this.options);

        int requestCode;
        Intent libraryIntent;
        if (pickVideo) {
            requestCode = REQUEST_LAUNCH_VIDEO_LIBRARY;
            libraryIntent = new Intent(Intent.ACTION_PICK);
            libraryIntent.setType("video/*");
        } else {
            requestCode = REQUEST_LAUNCH_IMAGE_LIBRARY;
            libraryIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }

        if (libraryIntent.resolveActivity(application.getPackageManager()) == null) {
            responseHelper.invokeError(callback, "Cannot launch photo library");
            return;
        }

        this.callback = callback;

        try {
            currentActivity.startActivityForResult(libraryIntent, requestCode);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            responseHelper.invokeError(callback, "Cannot launch photo library");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //robustness code
        if (passResult(requestCode)) {
            return;
        }

        responseHelper.cleanResponse();

        // user cancel
        if (resultCode != Activity.RESULT_OK) {
            removeUselessFiles(requestCode, imageConfig);
            responseHelper.invokeCancel(callback);
            callback = null;
            return;
        }

        Uri uri = null;
        switch (requestCode) {
            case REQUEST_LAUNCH_IMAGE_CAPTURE:
                uri = cameraCaptureURI;
                break;

            case REQUEST_LAUNCH_IMAGE_LIBRARY:
                uri = data.getData();
                String realPath = getRealPathFromURI(uri);
                final boolean isUrl = !TextUtils.isEmpty(realPath) &&
                        Patterns.WEB_URL.matcher(realPath).matches();
                if (realPath == null || isUrl) {
                    try {
                        File file = createFileFromURI(uri);
                        realPath = file.getAbsolutePath();
                        uri = Uri.fromFile(file);
                    } catch (Exception e) {
                        // image not in cache
                        responseHelper.putString("error", "Could not read photo");
                        responseHelper.putString("uri", uri.toString());
                        responseHelper.invokeResponse(callback);
                        callback = null;
                        return;
                    }
                }
                imageConfig = imageConfig.withOriginalFile(new File(realPath));
                break;

            case REQUEST_LAUNCH_VIDEO_LIBRARY:
                responseHelper.putString("uri", data.getData().toString());
                responseHelper.putString("path", getRealPathFromURI(data.getData()));
                responseHelper.invokeResponse(callback);
                callback = null;
                return;

            case REQUEST_LAUNCH_VIDEO_CAPTURE:
                final String path = getRealPathFromURI(data.getData());
                responseHelper.putString("uri", data.getData().toString());
                responseHelper.putString("path", path);
                fileScan(application, path);
                responseHelper.invokeResponse(callback);
                callback = null;
                return;
        }

        final ReadExifResult result = readExifInterface(responseHelper, imageConfig);

        if (result.error != null) {
            removeUselessFiles(requestCode, imageConfig);
            responseHelper.invokeError(callback, result.error.getMessage());
            callback = null;
            return;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageConfig.original.getAbsolutePath(), options);
        int initialWidth = options.outWidth;
        int initialHeight = options.outHeight;
        updatedResultResponse(uri, imageConfig.original.getAbsolutePath());

        // don't create a new file if contraint are respected
        if (imageConfig.useOriginal(initialWidth, initialHeight, result.currentRotation)) {
            responseHelper.putInt("width", initialWidth);
            responseHelper.putInt("height", initialHeight);
            fileScan(application, imageConfig.original.getAbsolutePath());
        } else {
            imageConfig = getResizedImage(application, this.options, imageConfig, initialWidth, initialHeight, requestCode);
            if (imageConfig.resized == null) {
                removeUselessFiles(requestCode, imageConfig);
                responseHelper.putString("error", "Can't resize the image");
            } else {
                uri = Uri.fromFile(imageConfig.resized);
                BitmapFactory.decodeFile(imageConfig.resized.getAbsolutePath(), options);
                responseHelper.putInt("width", options.outWidth);
                responseHelper.putInt("height", options.outHeight);

                updatedResultResponse(uri, imageConfig.resized.getAbsolutePath());
                fileScan(application, imageConfig.resized.getAbsolutePath());
            }
        }

        if (imageConfig.saveToCameraRoll && requestCode == REQUEST_LAUNCH_IMAGE_CAPTURE) {
            final RolloutPhotoResult rolloutResult = rolloutPhotoFromCamera(imageConfig);

            if (rolloutResult.error == null) {
                imageConfig = rolloutResult.imageConfig;
                uri = Uri.fromFile(imageConfig.getActualFile());
                updatedResultResponse(uri, imageConfig.getActualFile().getAbsolutePath());
            } else {
                removeUselessFiles(requestCode, imageConfig);
                final String errorMessage = new StringBuilder("Error moving image to camera roll: ")
                        .append(rolloutResult.error.getMessage()).toString();
                responseHelper.putString("error", errorMessage);
                return;
            }
        }

        responseHelper.invokeResponse(callback);
        callback = null;
        this.options = null;
    }

    public void invokeCustomButton(@NonNull final String action) {
        responseHelper.invokeCustomButton(this.callback, action);
    }


    public Context getContext() {
        return mWXSDKInstance.getContext();
    }

    public
    @StyleRes
    int getDialogThemeId() {
        return this.dialogThemeId;
    }

    public
    @NonNull
    Activity getActivity() {
        return (Activity) mWXSDKInstance.getContext();
    }


    private boolean passResult(int requestCode) {
        return callback == null || (cameraCaptureURI == null && requestCode == REQUEST_LAUNCH_IMAGE_CAPTURE)
                || (requestCode != REQUEST_LAUNCH_IMAGE_CAPTURE && requestCode != REQUEST_LAUNCH_IMAGE_LIBRARY
                && requestCode != REQUEST_LAUNCH_VIDEO_LIBRARY && requestCode != REQUEST_LAUNCH_VIDEO_CAPTURE);
    }

    private void updatedResultResponse(@Nullable final Uri uri,
                                       @NonNull final String path) {
        responseHelper.putString("uri", uri.toString());
        responseHelper.putString("path", path);

//        if (!noData) {
//            responseHelper.putString("data", getBase64StringFromFile(path));
//        }

        putExtraFileInfo(path, responseHelper);
    }

    private boolean permissionsCheck(@NonNull final Activity activity,
                                     @NonNull final JSCallback callback,
                                     @NonNull final int requestCode) {
        final int writePermission = ActivityCompat
                .checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        final int cameraPermission = ActivityCompat
                .checkSelfPermission(activity, Manifest.permission.CAMERA);

        final boolean permissionsGrated = writePermission == PackageManager.PERMISSION_GRANTED &&
                cameraPermission == PackageManager.PERMISSION_GRANTED;

        if (!permissionsGrated) {
            final Boolean dontAskAgain = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA);

            if (dontAskAgain) {
                final AlertDialog dialog = PermissionUtils
                        .explainingDialog(this, options, new PermissionUtils.OnExplainingPermissionCallback() {
                            @Override
                            public void onCancel(WeakReference<ImagePickerModule> moduleInstance,
                                                 DialogInterface dialogInterface) {
                                final ImagePickerModule module = moduleInstance.get();
                                if (module == null) {
                                    return;
                                }
                                module.doOnCancel();
                            }

                            @Override
                            public void onReTry(WeakReference<ImagePickerModule> moduleInstance,
                                                DialogInterface dialogInterface) {
                                final ImagePickerModule module = moduleInstance.get();
                                if (module == null) {
                                    return;
                                }
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", module.getContext().getPackageName(), null);
                                intent.setData(uri);
                                final Activity innerActivity = module.getActivity();
                                if (innerActivity == null) {
                                    return;
                                }
                                innerActivity.startActivityForResult(intent, 1);
                            }
                        });
                dialog.show();
                return false;
            } else {
                String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
                if (activity instanceof Activity) {
//                    (activity).requestPermissions(PERMISSIONS, requestCode);
                    ActivityCompat.requestPermissions(activity, PERMISSIONS, requestCode);
                }
//                else if (activity instanceof OnImagePickerPermissionsCallback) {
//                    ((OnImagePickerPermissionsCallback) activity).setPermissionListener(listener);
//                    ActivityCompat.requestPermissions(activity, PERMISSIONS, requestCode);
//                }
                else {
                    final String errorDescription = new StringBuilder(activity.getClass().getSimpleName())
                            .append(" must implement ")
                            .append(OnImagePickerPermissionsCallback.class.getSimpleName())
                            .toString();
                    throw new UnsupportedOperationException(errorDescription);
                }
                return false;
            }
        }
        return true;
    }

    private boolean isCameraAvailable() {
        return application.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)
                || application.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    private
    @NonNull
    String getRealPathFromURI(@NonNull final Uri uri) {
        return RealPathUtil.getRealPathFromURI(application, uri);
    }

    /**
     * Create a file from uri to allow image picking of image in disk cache
     * (Exemple: facebook image, google image etc..)
     *
     * @param uri
     * @return File
     * @throws Exception
     * @doc =>
     * https://github.com/nostra13/Android-Universal-Image-Loader#load--display-task-flow
     */
    private File createFileFromURI(Uri uri) throws Exception {
        File file = new File(application.getExternalCacheDir(), "photo-" + uri.getLastPathSegment());
        InputStream input = application.getContentResolver().openInputStream(uri);
        OutputStream output = new FileOutputStream(file);

        try {
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
        } finally {
            output.close();
            input.close();
        }

        return file;
    }

    private String getBase64StringFromFile(String absoluteFilePath) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(absoluteFilePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        byte[] bytes;
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        bytes = output.toByteArray();
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    private void putExtraFileInfo(@NonNull final String path,
                                  @NonNull final ResponseHelper responseHelper) {
        // size && filename
        try {
            File f = new File(path);
            responseHelper.putDouble("fileSize", f.length());
            responseHelper.putString("fileName", f.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // type
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        if (extension != null) {
            responseHelper.putString("type", MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension));
        }
    }

    private void parseOptions(final JSONObject options) {
        noData = false;
        if (options.containsKey("noData")) {
            noData = options.getBoolean("noData");
        }
        imageConfig = imageConfig.updateFromOptions(options);
        pickVideo = false;
        if (options.containsKey("mediaType") && options.getString("mediaType").equals("video")) {
            pickVideo = true;
        }
        videoQuality = 1;
        if (options.containsKey("videoQuality") && options.getString("videoQuality").equals("low")) {
            videoQuality = 0;
        }
        videoDurationLimit = 0;
        if (options.containsKey("durationLimit")) {
            videoDurationLimit = options.getInteger("durationLimit");
        }
    }
}
