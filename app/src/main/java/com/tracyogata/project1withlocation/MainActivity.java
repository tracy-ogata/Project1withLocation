package com.tracyogata.project1withlocation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    /* Global variables */
    ImageView imageField;
    String safetyStatus;
    private static final int CAM_REQUEST =1313;
    File pic;
    Bitmap bitmap;
    private LocationManager locationManager;
    private LocationListener listener;
    TextView Location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /*finds coordinates using gps when button is pressed. locationManager gives access to system location services.
  locationListener receives notifications from locationManager when location changes*/
    public void GetLocation(View view) {
        Location = (TextView) findViewById(R.id.latitude_longtitude);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                Location.append("\nLong: " + location.getLongitude() + " Lat: " + location.getLatitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent checkGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(checkGPS);
            }
        };
        configure_location();
    }

    /*makes sure all of the user permissions are granted and requests a single location update*/
    public void configure_location() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, null);
            }
        }
    }


    /*take photo button */
    public void TakePhoto(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,CAM_REQUEST);
    }

    /*get the photo */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageField = (ImageView)findViewById(R.id.Image_Field);
        if(requestCode == CAM_REQUEST){
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imageField.setImageBitmap(bitmap);
        }

        Bundle ext = data.getExtras();
        bitmap = (Bitmap)ext.get("data");
        try {
            File root = Environment.getExternalStorageDirectory();
            if (root.canWrite()){
                pic = new File(root, "pic.png");
                FileOutputStream out = new FileOutputStream(pic);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
            }
        } catch (IOException e) {
            Log.e("BROKEN", "Could not write file " + e.getMessage());
        }
    }

    /* submit button */
    public void SubmitButton (View view) {
        EditText nameField = (EditText) findViewById(R.id.name_field);
        String name = nameField.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please fill in your name.", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText phoneField = (EditText) findViewById(R.id.phone_field);
        String phone = phoneField.getText().toString();
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please fill in your phone number.", Toast.LENGTH_SHORT).show();
            return;
        }

        CheckBox YesCheckbox = (CheckBox) findViewById(R.id.yes_checkbox);
        boolean yesCheck = YesCheckbox.isChecked();

        CheckBox NoCheckbox = (CheckBox) findViewById(R.id.no_checkbox);
        boolean noCheck = NoCheckbox.isChecked();

        if(yesCheck == false && noCheck == false) {
            Toast.makeText(this, "Must check a safety option.", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(yesCheck == true && noCheck == true) {
            Toast.makeText(this, "Can only check one safety option.", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(yesCheck == true) {
            safetyStatus = "safe.";
        }
        else if(noCheck == true) {
            safetyStatus = "need medical attention.";
        }

        ImageView IsImageThere = (ImageView) findViewById(R.id.Image_Field);
        if (IsImageThere.getDrawable()== null) {
            Toast.makeText(this, "Please get photo.", Toast.LENGTH_SHORT).show();
            return;
        }

        TextView Coords = (TextView) findViewById(R.id.latitude_longtitude);
        String coords = Coords.getText().toString();
        if (TextUtils.isEmpty(coords)) {
            Toast.makeText(this, "Please get location.", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText FullMessage = (EditText) findViewById(R.id.long_message);
        String fullMessage = FullMessage.getText().toString();

        String report = fullSummary(name, phone, safetyStatus, coords, fullMessage);

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "tyogata@uci.edu" });
        intent.putExtra(Intent.EXTRA_SUBJECT, "Disaster Report from: " + name);
        intent.putExtra(Intent.EXTRA_TEXT, report);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(pic));
        if(intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /* function for full summary report */
    private String fullSummary(String name, String phone, String safetyStatus, String coords, String fullMessage) {
        String summary = "Name:  " + name;
        summary += "\nPhone:  " + phone;
        summary += "\nSafe?:  " + safetyStatus;
        summary += "\nLocation: " + coords;
        summary += "\nMessage:  " + fullMessage;
        return summary;
    }
}

