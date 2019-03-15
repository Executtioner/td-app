package com.example.test;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.os.VibrationEffect;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.blikoon.qrcodescanner.QrCodeActivity;
import com.example.test.model.Post;
import com.example.test.rest.ApiClient;
import com.example.test.rest.ApiInterface;
import com.example.test.util.CustomDialogClass;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.os.Vibrator;
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_QR_SCAN = 101;
    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView text;
    private ImageView img;
    //private final static String API_KEY = "";

    private static String APP_DIRECTORY = "test/";
    private static String MEDIA_DIRECTORY = APP_DIRECTORY + "PictureApp";

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 7;
    private final int PHOTO_CODE = 200;
    private final int SELECT_PICTURE = 300;
    private String mPath;
    private ImageView mSetImage;
    private Button scan;
    Vibrator v;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSetImage = (ImageView) findViewById(R.id.wall);
        scan = (Button)findViewById(R.id.button);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        checkAndroidVersion();
    }

    public void onClick(View v) {

            scan.setEnabled(false);
            Intent i = new Intent(MainActivity.this, QrCodeActivity.class);
            startActivityForResult(i, REQUEST_CODE_QR_SCAN);

    }

    public void onClick2(View v) {

        CustomDialogClass cdd = new CustomDialogClass(MainActivity.this);
        cdd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        cdd.show();

    }

    public void onClick3(View v) {

        showOptions();

    }

    private void showOptions() {

        final CharSequence[] option = {"Tomar foto", "Elegir de galería", "Cancelar"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Eleige una opción");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(option[which] == "Tomar foto"){

                    openCamera();

                }else if(option[which] == "Elegir de galería"){
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Selecciona app de imagen"), SELECT_PICTURE);
                }else {
                    dialog.dismiss();
                }
            }
        });
        builder.show();

    }

    private void openCamera() {

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        File file = new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
        boolean isDirectoryCreated = file.exists();

        if(!isDirectoryCreated)
            isDirectoryCreated = file.mkdirs();

        if(isDirectoryCreated){
            Long timestamp = System.currentTimeMillis() / 1000;
            String imageName = timestamp.toString() + ".jpg";

            mPath = Environment.getExternalStorageDirectory() + File.separator + MEDIA_DIRECTORY
                    + File.separator + imageName;

            File newFile = new File(mPath);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile));
            startActivityForResult(intent, PHOTO_CODE);

        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK) {
            scan.setEnabled(true);
            Toast.makeText(getApplicationContext(), "Cámara Cerrada", Toast.LENGTH_SHORT).show();
            if(data == null)
                return;
            String resultado = data.getStringExtra("com.blikoon.qrcodescanner.error_decoding_image");
            if (resultado != null) {
                Toast.makeText(getApplicationContext(), "No se pudo escanear el código QR", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if (requestCode == REQUEST_CODE_QR_SCAN) {
            if (data != null) {
                String lectura = data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
                scan.setEnabled(true);
                createLoginDialogo(lectura);
            }
        }
        if(resultCode == RESULT_CANCELED){
        }

        if(resultCode == RESULT_OK){
            switch (requestCode){
                case PHOTO_CODE:

                    Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(mPath), 400,450, true);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap = test(bitmap);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                    mSetImage.setImageBitmap(bitmap);


                    // Assume block needs to be inside a Try/Catch block.
                    OutputStream fOut = null;
                    File file = new File(mPath);
                    try {
                        fOut = new FileOutputStream(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut); // saving the Bitmap to a file compressed
                    try {
                        fOut.flush();
                        fOut.close();
                        //MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;

                case SELECT_PICTURE:
                    Uri path = data.getData();
                    mSetImage.setImageURI(path);
                    break;

            }
        }

    }


    public void create(String bn, Integer tp){

        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        Post post = new Post();
        post.setBooking_number(bn);
        post.setTravelers_present(tp);

            String j = "{\n" +
                    "    \"booking\":{\n" +
                    "        \"booking_number\":\""+post.getBooking_number()+"\",\n" +
                    "        \"travelers_present\": \""+post.getTravelers_present()+"\"\n" +
                    "    }\n" +
                    "}";

            RequestBody rq = RequestBody.create(MediaType.parse("application/json"), j);
            apiService.createPost(rq).enqueue(new Callback<Void>() {

                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {

                    if(response.code() == 200){
                        showToast( " Registro Exitoso", R.drawable.okay);
                    }else if(response.code() == 201){
                        showToast( " Sin Contenido", R.drawable.warning);
                    }else if(response.code() == 202){
                        showToast( " Error 202: Error al enviar Datos", R.drawable.warning);
                    }else if(response.code() == 203){
                        showToast( " Reserva no encontrada", R.drawable.warning);
                    }else if(response.code() == 204){
                        showToast( " Error 204: Error al enviar Datos", R.drawable.warning);
                    }else if(response.code() == 205){
                        showToast( " Codigo registrado anteriormente", R.drawable.warning);
                    }else if(response.code() == 404){
                        showToast( " Sin Conexion con el Servidor", R.drawable.warning);
                    }else{
                        showToast( "...? GG", R.drawable.warning);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {

                    showToast("Error: " + t.getMessage(), R.drawable.cancel);

                }
            });
    }

    public void createLoginDialogo(final String bn) {

        final TextView title = new TextView(this);
        title.setText("Confirmación");
        title.setGravity(Gravity.CENTER);
        title.setTextSize(30);

        final EditText travelers = new EditText(this);
        travelers.setGravity(Gravity.CENTER);
        travelers.setTextSize(30);
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(4);
        travelers.setFilters(FilterArray);
        travelers.setInputType(InputType.TYPE_CLASS_NUMBER);

        new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme))

                .setCustomTitle(title)
                .setMessage("Ingresar la cantidad de asistentes")
                .setView(travelers)
                .setCancelable(false)
                .setPositiveButton("Ingresar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        Integer trav = 0;
                        if(isOnline(getApplicationContext())!=false){

                            try {

                                trav = Integer.parseInt(travelers.getText().toString());

                            }catch (Exception e){

                                showToast("Debe ingresar un número", R.drawable.warning);

                            }

                            if(trav == 0 ){
                                showToast("El número debe ser mayor a 0", R.drawable.warning);
                            }else {
                                showToast("Registrando...", R.drawable.hiker);
                                create(bn, trav);
                            }

                        }else{

                            showToast("Sin conexión a la Red", R.drawable.cancel);
                            vibrate(500);

                        }
                    }
                }).setNegativeButton("Calcelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        }).show();
    }

    private void vibrate(int mill) {
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(mill, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(mill);
        }
    }

    private static boolean isOnline(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();

    }

    private void showToast(String message, int icon)
    {

        View toastView = getLayoutInflater().inflate(R.layout.custom_toast, null);

        // Initiate the Toast instance.
        Toast toast = new Toast(getApplicationContext());
        text = (TextView)toastView.findViewById(R.id.customToastText);
        img = (ImageView)toastView.findViewById(R.id.customToastImage);

        // Set custom view in toast.
        img.setImageResource(icon);
        text.setText(message);
        toast.setView(toastView);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0,0);
        toast.show();

    }


    private void checkAndroidVersion() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();

        } else {
            // code for lollipop and pre-lollipop devices
            showToast("OLD", R.drawable.okay);

        }

    }

    private boolean checkAndRequestPermissions() {

        int camera = ContextCompat.checkSelfPermission(getApplication(),
                Manifest.permission.CAMERA);
        int write = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.READ_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (write != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (camera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (read != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();

                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);

                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);

                    // Check for both permissions
                    if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    } else {

                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            showDialogOK("Los Permisos de Cámara y Almacenamiento son necesarios para el funcionamiento de esta aplicacion. Sin ellos, la app cerrará.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    System.exit(0);
                                                    break;
                                            }
                                        }
                                    });
                        }

                        else {

                            System.exit(0);

                        }
                    }
                }
            }
        }
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Continuar", okListener)
                .setNegativeButton("Salir", okListener)
                .create()
                .show();
    }

    public static Bitmap test(Bitmap src){
        Bitmap dest = Bitmap.createBitmap(
                src.getWidth(), src.getHeight(), src.getConfig());

        for(int x = 0; x < src.getWidth(); x++){
            for(int y = 0; y < src.getHeight(); y++){
                int pixelColor = src.getPixel(x, y);
                int pixelAlpha = Color.alpha(pixelColor);
                int pixelRed = Color.red(pixelColor);
                int pixelGreen = Color.green(pixelColor);
                int pixelBlue = Color.blue(pixelColor);

                int pixelBW = (pixelRed + pixelGreen + pixelBlue)/3;
                int newPixel = Color.argb(
                        pixelAlpha, pixelBW, pixelBW, pixelBW);

                dest.setPixel(x, y, newPixel);
            }
        }

        return dest;
    }

}