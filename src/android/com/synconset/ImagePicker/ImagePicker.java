/**
 * An Image Picker Plugin for Cordova/PhoneGap.
 */
package com.synconset;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

public class ImagePicker extends CordovaPlugin {
  public static String TAG = "ImagePicker";

  private CallbackContext callbackContext;
  private JSONObject params;

  public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
    this.callbackContext = callbackContext;
    this.params = args.getJSONObject(0);
    if (action.equals("getPictures")) {
      if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && !cordova.hasPermission("android.permission.WRITE_EXTERNAL_STORAGE")) {
        cordova.requestPermission(this, 105, "android.permission.WRITE_EXTERNAL_STORAGE");
      } else {
        getPictures(this.params);
      }
    }
    return true;
  }

  private void getPictures(final JSONObject params) throws JSONException {
    Intent intent = new Intent(cordova.getActivity(), MultiImageChooserActivity.class);
    int max = 20;
    int desiredWidth = 0;
    int desiredHeight = 0;
    int quality = 100;
    if (this.params.has("maximumImagesCount")) {
      max = this.params.getInt("maximumImagesCount");
    }
    if (this.params.has("width")) {
      desiredWidth = this.params.getInt("width");
    }
    if (this.params.has("height")) {
      desiredHeight = this.params.getInt("height");
    }
    if (this.params.has("quality")) {
      quality = this.params.getInt("quality");
    }
    intent.putExtra("MAX_IMAGES", max);
    intent.putExtra("WIDTH", desiredWidth);
    intent.putExtra("HEIGHT", desiredHeight);
    intent.putExtra("QUALITY", quality);
    if (this.cordova != null) {
      this.cordova.startActivityForResult((CordovaPlugin) this, intent, 0);
    }
  }

  /**
   * processes the result of permission request
   *
   * @param requestCode  The code to get request action
   * @param permissions  The collection of permissions
   * @param grantResults The result of grant
   */
  public void onRequestPermissionResult(int requestCode, String[] permissions,
                                        int[] grantResults) throws JSONException {

    switch (requestCode) {
      case 105:
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          getPictures(this.params);
        } else {
          this.callbackContext.error("授权失败，请重新授权！");
        }
        break;
    }
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == Activity.RESULT_OK && data != null) {
      ArrayList<String> fileNames = data.getStringArrayListExtra("MULTIPLEFILENAMES");
      JSONArray res = new JSONArray(fileNames);
      this.callbackContext.success(res);
    } else if (resultCode == Activity.RESULT_CANCELED && data != null) {
      String error = data.getStringExtra("ERRORMESSAGE");
      this.callbackContext.error(error);
    } else if (resultCode == Activity.RESULT_CANCELED) {
      JSONArray res = new JSONArray();
      this.callbackContext.success(res);
    } else {
      this.callbackContext.error("No images selected");
    }
  }
}
