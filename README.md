#SydCamera
##简介
相机模块库,自定义相机，通过简单的调用即可实现拍照、图片裁剪、录像及录像抓拍功能;
实现图片压缩，减少图片体积；自定义相机可避免使用系统相机导致的照片或视频体积过大问题；
内置相机及sd卡权限获取的处理；

实现功能：
- 拍照
- 图片裁剪
- 录像
- 录像抓拍

![](/Users/oden/Documents/03.png)
![](/Users/oden/Documents/04.png)
![](/Users/oden/Documents/01.png)
## 使用

### 拍照
具体使用可参考demo中的CameraTestActivity

#### 启动相机
参照以下方式传入图片质量、照片最小宽度配置、相机预览界面最小宽度配置，直接跳转到拍照界面进行拍照;
相关的配置参数：
- picQuality：图片质量0~100，默认80
- picWidth：照片最小宽度配置，默认800
- previewWidth：相机预览界面最小宽度配置，默认1280
- pictureSize：照片尺寸限制，单位kb，不存入则仅根据图片质量进行压缩，实际压缩后的大小会比该值略大一些

```java
Intent intent = new Intent(CameraTestActivity.this, SydCameraActivity.class);
intent.putExtra(CameraParaUtil.picQuality, 70); //图片质量0~100
intent.putExtra(CameraParaUtil.picWidth, 1536);  //照片最小宽度配置，高度根据屏幕比例自动配置
intent.putExtra(CameraParaUtil.previewWidth, 1280);  //相机预览界面最小宽度配置，高度根据屏幕比例自动配置
startActivityForResult(intent, CameraParaUtil.cameraRequestCode);
```
#### 接收拍照返回
拍照返回中获取到存储的照片路径后可根据需求对照片进行处理，路径获取方式:picturePath = data.getStringExtra(CameraParaUtil.picturePath);

```java
@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Log.i(TAG, "onActivityResult resultCode:" + resultCode + ",requestCode: " + requestCode);

        if (resultCode == Activity.RESULT_CANCELED){
            Log.i(TAG, "拍照取消!");
            return;
        }
        if (resultCode != Activity.RESULT_OK){
            Log.w(TAG, "拍照失败!");
            return;
        }

        if (requestCode == CameraParaUtil.cameraRequestCode) {
            String picturePath;
            picturePath = data.getStringExtra(CameraParaUtil.picturePath);

            img_photo.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            Log.d(TAG, "onActivityResult picturePath: " + picturePath);
        }
    }
```
### 图片裁剪
#### 启动裁剪
参照以下方式传入图片质量、待裁剪图片路径，直接跳转到裁剪界面进行图片裁剪;
相关的配置参数：
- cropQuality：图片质量0~100，默认80
- cropTitle：裁剪界面标题
- cropDestPicPath：裁剪后图片所位于的文件夹名称
- cropSrcPicPath：待裁剪的源文件路径

```java
   private void startCrop(String path) {
        Intent intent = new Intent(CameraTestActivity.this, IcomwellCropActivity.class);
        intent.putExtra(CropParaUtil.cropQuality, 70); //图片质量0~100
        intent.putExtra(CropParaUtil.cropTitle, "添加封面");
        intent.putExtra(CropParaUtil.cropSrcPicPath, path);
        startActivityForResult(intent, CropParaUtil.REQUEST_CODE_FROM_CUTTING);
    }
```

#### 接收裁剪返回
裁剪返回中获取存储的裁剪后图片路径后可根据需求对图片进行处理，路径获取：data.getStringExtra(CropParaUtil.cropDestPicPath);
```java
     switch (requestCode) {
            case CropParaUtil.REQUEST_CODE_FROM_CUTTING:
                String cropDestPicPath;
                cropDestPicPath = data.getStringExtra(CropParaUtil.cropDestPicPath);
                img_photo.setImageBitmap(BitmapFactory.decodeFile(cropDestPicPath));
                Log.d(TAG, "onActivityResult cropDestPicPath: " + cropDestPicPath);
                break;
            default:
                break;
        }

```
#### 自定义图片裁剪UI
若需要自定义裁剪界面U,可参考SydCropActivity，自定义布局

### 录像功能
参照以下方式传入相关参数，直接跳转到录像界面进行录像;
相关的配置参数：
- picQuality：录像质量0~100，默认80
- picWidth：视频最小宽度配置，默认800
- previewWidth：相机预览界面最小宽度配置，默认1280
- pictureSize：照片尺寸限制，单位kb，不存入则仅根据图片质量进行压缩，实际压缩后的大小会比该值略大一些
- picDuration：自动抓拍时间间隔，单位秒，默认3600秒,传入小于等于0的数则为关闭自动抓拍
- videoDuration：录像分段间隔，单位秒，默认1800秒
照片和录像分别位于：sydPhoto、sydVideo文件夹下

```
 private void startVideo() {
        Intent intent = new Intent(CameraTestActivity.this, SydVideoActivity.class);
        intent.putExtra(CameraParaUtil.picQuality, 70); //图片质量0~100
        intent.putExtra(CameraParaUtil.picWidth, 1536);  //照片最小宽度配置，高度根据屏幕比例自动配置
        intent.putExtra(CameraParaUtil.previewWidth, 1280);  //相机预览界面最小宽度配置，高度根据屏幕比例自动配置
        startActivityForResult(intent, CameraParaUtil.REQUEST_CODE_FROM_VIDEO);
    }
```

### 特殊情况兼容处理
三星A8出现无权限存储文件的问题，处理方式为将拍照或裁剪的图片存于Bitmap中，直接返回Bitmap

```java

    //裁剪后的图片，文件存储失败的情况下存入croppedBitmap
    public static Bitmap croppedBitmap;
    //拍照后的图片，文件存储失败的情况下存入pictureBitmap，裁剪时取不到本地文件则也作为图片裁剪的来源
    public static Bitmap pictureBitmap;

    /**
     * 兼容三星A8出现无权限存储文件的处理，
     * 将手机重启后可正常存储文件..
     * @param resultCode
     */
    private void onResultExceptionHandle(int resultCode) {
        //拍照失败处理，针对三星A8无法存储文件处理，拍照失败后检查CameraParaUtil.pictureBitmap是否有数据
        if (resultCode == CameraParaUtil.REQUEST_CODE_FROM_CAMERA_FAIL && CameraParaUtil.pictureBitmap != null) {
            if (Invoke.isPicNeedDeal) {
                //使用pictureBitmap进行裁剪
                startCropForBitmap();
            } else {
                //不需要裁剪
                uploadPicture(CameraParaUtil.pictureBitmap);
            }
        }

        //裁剪失败处理，针对三星A8无法存储文件处理，失败后检查CropParaUtil.croppedBitmap是否有数据
        if (resultCode == CropParaUtil.REQUEST_CODE_FROM_CUTTING_FAIL && CropParaUtil.croppedBitmap != null) {
            uploadPicture(CropParaUtil.croppedBitmap);
        }
    }
```


