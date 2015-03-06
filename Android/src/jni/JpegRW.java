package com.example.example.jni;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

public class JpegRW {
	private JpegRWNative nlib = null;

	public JpegRW() {
		nlib = new JpegRWNative();
	}
	
	public void compress(Bitmap image, String fileName, int quality) {
		nlib.compress(image, fileName, quality);
	}
	
	public void compress(Bitmap image, String fileName, int quality, int rotation) {
		nlib.rotateAndCompress(image, fileName, quality, rotation);
	}
	
	public Bitmap decompress(byte[] srcData) {
		int[] dim = new int[2];
		nlib.getCompressedJpegDim(srcData, dim);
		
		Bitmap dstBM = Bitmap.createBitmap(dim[0], dim[1], Config.RGB_565);
		
		nlib.decompressByteArr(srcData, dstBM);
		
		return dstBM;
	}

	public void rotateJPEG(final String lastSavedImagePath, int rotation) {
		nlib.rotateJPEG(lastSavedImagePath, rotation);
	}
}

class JpegRWNative {
	static {
		System.loadLibrary("jpegrw");
	}
	public native void compress(Bitmap image, String fileName, int quality);
	
	public native void rotateAndCompress(Bitmap image, String fileName, int quality, int rotatio);
	
	public native void rotateJPEG(String lastSavedImagePath, int rotation);

	public native void getCompressedJpegDim(byte[] srcData, int[] dim);

	public native void decompressByteArr(byte[] srcData, Bitmap dstBM);

}