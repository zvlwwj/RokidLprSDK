package com.rokid.camera.lpr;

import android.graphics.Rect;

public class Tools {
    private static Rect rect = new Rect(Constants.ALIGNMENT_LEFT, Constants.ALIGHTMENT_TOP,
                                        Constants.ALIGHTMENT_RIGHT, Constants.ALIGHTMENT_BOTTOM);
    private static final int val = 100;

    //相机中的rect到现实世界的映射
    public static Rect getScaleRect(Rect rectCache,int previewWidth,int previewHeight) {
        Rect currentRect = null;
        int w = rect.right - rect.left;
        int h = rect.bottom - rect.top;
        if (isNice(rectCache)) {
            currentRect = new Rect((int) ((rectCache.left - rect.left) * 1.0 / w * previewWidth),
                    (int) ((rectCache.top - rect.top) * 1.0 / h * previewHeight),
                    (int) ((rectCache.right - rect.left) * 1.0 / w * previewWidth),
                    (int) ((rectCache.bottom - rect.top) * 1.0 / h * previewHeight));
            return currentRect;
        }
        return null;
    }

    public static boolean isNice(Rect rectCache) {
        if (rectCache.left > rect.left - val && rectCache.left < rect.right + val &&
                rectCache.right > rect.left - val && rectCache.right < rect.right + val &&
                rectCache.top > rect.top - val && rectCache.top < rect.bottom + val &&
                rectCache.bottom > rect.left - val && rectCache.bottom < rect.right + val) {
            return true;
        }
        return false;
    }
}
