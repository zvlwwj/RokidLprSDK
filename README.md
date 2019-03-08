Rokid Plate Recognition SDK and Demo Project.

***	
|SDK Author|Email|
|---|---|
|cmxnono|cmxnono@rokid.com|
***

## cirtus_lpr_sdk

Version：1.0

### SDK接口说明

* 初始化

```
public long init(Context context)
```

* 相机预览识别

```
public int[] detect(byte[] data, int w, int h, int method, long  object)
返回值为车牌位置[x, y, width, height]，当有多个车牌时可能有多组数据

public String recogAll(byte[] data, int w, int h, int method, int[] rects, long  object)
返回值为车牌号
```

* BGR数据输入识别

```
public String recognizationBGR(byte[] data, int w, int h, int method, long  object)
```

* 模型更新

```
public String updateModel(Context context)

会将assets下的Citrus文件夹中的文件拷贝到应用程序的内部存储路径/data/data/<application package>/files/Citrus下
```

## android_demo

	可运行在普通安卓手机上，对预览界面内车牌进行自动识别。

## glass_demo

	运行在glass上，对固定视线区域内车牌进行自动识别。

	
