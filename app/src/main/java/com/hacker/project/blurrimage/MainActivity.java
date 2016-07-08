package com.hacker.project.blurrimage;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    Button blurBtn, viewChangeBtn, pickImgBtn;
    ImageView imageOne, imageTwo;
    RelativeLayout rlOne, rlTwo;
    LinearLayout viewHolder, imageHolder;
    String filePath;
    int PHOTO = 1;
    Bitmap mBitmapFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpView();

        blurBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!filePath.isEmpty()) {
                    Bitmap outBit = blur(mBitmapFile);
                    imageTwo.setImageBitmap(outBit);
                    Drawable drawable = new BitmapDrawable(getResources(), outBit);
                    rlTwo.setBackground(drawable);
                } else {
                    Toast.makeText(MainActivity.this, "Select image first pls", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private void setImageBlur(Bitmap photo) {
        final RenderScript rs = RenderScript.create(MainActivity.this);
        final Allocation input = Allocation.createFromBitmap(rs, photo, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        final Allocation output = Allocation.createTyped(rs, input.getType());
        final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setRadius(3f /* e.g. 3.f */);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(photo);
    }

    public Bitmap blur(Bitmap image) {
        if (null == image) return null;

        Bitmap outputBitmap = Bitmap.createBitmap(image);
        final RenderScript renderScript = RenderScript.create(this);
        Allocation tmpIn = Allocation.createFromBitmap(renderScript, image);
        Allocation tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap);

        //Intrinsic Gausian blur filter
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        theIntrinsic.setRadius(25f);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);
        return outputBitmap;
    }

    private void setUpView() {
        blurBtn = (Button) findViewById(R.id.blur_img_btn);
        viewChangeBtn = (Button) findViewById(R.id.change_img_btn);
        pickImgBtn = (Button) findViewById(R.id.pick_img_btn);
        imageOne = (ImageView) findViewById(R.id.first_image);
        imageTwo = (ImageView) findViewById(R.id.second_image);
        rlOne = (RelativeLayout) findViewById(R.id.first_rl);
        rlTwo = (RelativeLayout) findViewById(R.id.second_rl);
        viewHolder = (LinearLayout) findViewById(R.id.view_holder);
        imageHolder = (LinearLayout) findViewById(R.id.image_holder);


        viewChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (viewHolder.getVisibility() == View.VISIBLE) {
                    viewHolder.setVisibility(View.GONE);
                    imageHolder.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this, "Image Visible", Toast.LENGTH_SHORT).show();
                } else {
                    viewHolder.setVisibility(View.VISIBLE);
                    imageHolder.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Layout Visible", Toast.LENGTH_SHORT).show();
                }
            }
        });

        pickImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select picture"), PHOTO);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PHOTO) {
                Uri metadataURI = data.getData();
                filePath = getPath(this, metadataURI);
                File imgFile = new File(filePath);
                Drawable drawable = null;

                if (imgFile.exists()) {
                    mBitmapFile = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    imageOne.setImageBitmap(mBitmapFile);
                    drawable = new BitmapDrawable(getResources(), mBitmapFile);
                    rlOne.setBackground(drawable);
                } else {
                    Toast.makeText(MainActivity.this, "File don't exists", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(MainActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }


    }


    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
