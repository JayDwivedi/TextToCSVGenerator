package com.bbi.weatherapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bbi.weatherapp.Database.TemperatureTable;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int progress;
    private String TAG = "Log";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // This instantiates DBFlow
        FlowManager.init(new FlowConfig.Builder(this).build());


        Button btnDownload = findViewById(R.id.btnDownload);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isInternetAvailable())
                    downloadDataFromServer();

                else {
                    Toast.makeText(MainActivity.this, "Network Not Available", Toast.LENGTH_LONG).show();

                }


            }
        });

        Button btnExport = findViewById(R.id.btnExport);
        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    if (isStoragePermissionGranted())
                        exportDataIntoCSVFile();
                    else
                        Toast.makeText(getApplicationContext(), "Please give storage write permission", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Button btnSendFile = findViewById(R.id.btnSendFile);
        btnSendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (isStoragePermissionGranted())
                    shareFile();
                else
                    Toast.makeText(getApplicationContext(), "Please give storage write permission", Toast.LENGTH_SHORT).show();

            }
        });
        Button btnOpenFile = findViewById(R.id.btnOpenFile);
        btnOpenFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (isStoragePermissionGranted())
                    openFile();
                else
                    Toast.makeText(getApplicationContext(), "Please give storage write permission", Toast.LENGTH_SHORT).show();

            }
        });
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
            try {
                exportDataIntoCSVFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isInternetAvailable() {
        ConnectivityManager mgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(mgr!=null) {
            NetworkInfo netInfo = mgr.getActiveNetworkInfo();

            if (netInfo != null) {
                if (netInfo.isConnected()) {
                    // Internet Available
                    return true;
                } else {
                    return false;
                    //No internet
                }
            } else {
                return false;
                //No internet
            }
        }
        else
            return false;
    }

    void addTemperatureIntoDataBase(String region, String weatherType, String year, String month, String value) {
// Create temperature
        TemperatureTable temperatureTable = new TemperatureTable();

        temperatureTable.region_code = region;
        temperatureTable.weather_param = weatherType;
        temperatureTable.year = year;
        temperatureTable.key = month;
        temperatureTable.value = value;
        temperatureTable.save();


    }

    public List<TemperatureTable> getAllTemperatureList() {
        // Query all temperature
        List<TemperatureTable> tempratureList = SQLite.select().
                from(TemperatureTable.class).queryList();

        return tempratureList;
    }

    private void clearDataBase() {
        SQLite.delete(TemperatureTable.class);
    }

    private void downloadDataFromServer() {


        final String[] regionList = {"UK", "England", "Scotland", "Wales"};
        final String[] weatherList = {"Tmax", "Tmin", "Tmean", "Sunshine", "Rainfall"};


        // show waiting screen
        CharSequence contentTitle = getString(R.string.app_name);
        final ProgressDialog progDailog = ProgressDialog.show(
                MainActivity.this, contentTitle, "Data downloading and storing...",
                true);//please wait
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {


            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    clearDataBase();
                    for (int k = 0; k < regionList.length; k++) {
                        for (int j = 0; j < weatherList.length; j++) {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progDailog.setMessage("File Downloading...");
                                }
                            });
                            System.out.println("Region: " + regionList[k] + "  " + "weather code: " + weatherList[j]);
                            String data = "";

                            Document doc = Jsoup.connect("https://www.metoffice.gov.uk/pub/data/weather/uk/climate/datasets/" + weatherList[j] + "/ranked/" + regionList[k] + ".txt").get();

                            Elements body = doc.getElementsByTag("body");

                            for (Element element : body) {
                                System.out.println();


                                data = element.getElementsByTag("body").text();
                            }


                            data = data.substring(data.indexOf("ANN Year") + 8, data.length());

                            String[] dataArray = data.split(" ");


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progDailog.setMessage("Inserting data into database...");
                                }
                            });
                            int constValue = 0;
                            for (int i = 1; i < dataArray.length; i++) {


                                switch (i - constValue) {
                                    case 1:
                                        addTemperatureIntoDataBase(regionList[k], weatherList[j], dataArray[i + 1], "JAN", dataArray[1]);
                                        System.out.println(dataArray[i + 1] + " " + dataArray[i] + " JAN ");
                                        i++;
                                        break;
                                    case 3:
                                        addTemperatureIntoDataBase(regionList[k], weatherList[j], dataArray[i + 1], "FEB", dataArray[1]);

                                        System.out.println(dataArray[i + 1] + " " + dataArray[i] + " FEB");
                                        i++;
                                        break;
                                    case 5:
                                        addTemperatureIntoDataBase(regionList[k], weatherList[j], dataArray[i + 1], "MAR", dataArray[1]);

                                        System.out.println(dataArray[i + 1] + " " + dataArray[i] + " MAR");
                                        i++;
                                        break;
                                    case 7:
                                        addTemperatureIntoDataBase(regionList[k], weatherList[j], dataArray[i + 1], "APR", dataArray[1]);

                                        System.out.println(dataArray[i + 1] + " " + dataArray[i] + " APR");
                                        i++;
                                        break;
                                    case 9:
                                        addTemperatureIntoDataBase(regionList[k], weatherList[j], dataArray[i + 1], "MAY", dataArray[1]);

                                        System.out.println(dataArray[i + 1] + " " + dataArray[i] + " MAY");
                                        i++;
                                        break;
                                    case 11:
                                        addTemperatureIntoDataBase(regionList[k], weatherList[j], dataArray[i + 1], "JUN", dataArray[1]);

                                        System.out.println(dataArray[i + 1] + " " + dataArray[i] + " JUN");
                                        i++;
                                        break;
                                    case 13:
                                        addTemperatureIntoDataBase(regionList[k], weatherList[j], dataArray[i + 1], "JUL", dataArray[1]);

                                        System.out.println(dataArray[i + 1] + " " + dataArray[i] + " JUL");
                                        i++;
                                        break;
                                    case 15:
                                        addTemperatureIntoDataBase(regionList[k], weatherList[j], dataArray[i + 1], "AUG", dataArray[1]);

                                        System.out.println(dataArray[i + 1] + " " + dataArray[i] + " AUG");
                                        i++;
                                        break;
                                    case 17:
                                        addTemperatureIntoDataBase(regionList[k], weatherList[j], dataArray[i + 1], "SEP", dataArray[1]);

                                        System.out.println(dataArray[i + 1] + " " + dataArray[i] + " SEP");
                                        i++;
                                        break;
                                    case 19:
                                        addTemperatureIntoDataBase(regionList[k], weatherList[j], dataArray[i + 1], "OCT", dataArray[1]);

                                        System.out.println(dataArray[i + 1] + " " + dataArray[i] + " OCT");
                                        i++;
                                        break;
                                    case 21:
                                        addTemperatureIntoDataBase(regionList[k], weatherList[j], dataArray[i + 1], "NOV", dataArray[1]);

                                        System.out.println(dataArray[i + 1] + " " + dataArray[i] + " NOV");
                                        i++;
                                        break;
                                    case 23:
                                        addTemperatureIntoDataBase(regionList[k], weatherList[j], dataArray[i + 1], "DEC", dataArray[1]);

                                        System.out.println(dataArray[i + 1] + " " + dataArray[i] + " DEC");
                                        i++;
                                        break;

                                    case 25:
                                        i++;
                                        break;
                                    case 27:
                                        i++;
                                        break;
                                    case 29:
                                        i++;
                                        break;
                                    case 31:
                                        i++;
                                        break;
                                    case 33:
                                        i++;
                                        constValue = constValue + 34;
                                        break;

                                }
                            }


                            System.out.println(data);
                        }
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(0);

                progDailog.dismiss();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Data Downloading is completed", Toast.LENGTH_SHORT).show();

                    }
                });

                try {

                    if (isStoragePermissionGranted())
                        exportDataIntoCSVFile();
                    else
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Please give storage write permission", Toast.LENGTH_SHORT).show();

                            }
                        });
                           } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }

    /**
     * @throws IOException
     */
    public void exportDataIntoCSVFile() throws IOException {
        {
            progress = 1;
            final NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);

                // Configure the notification channel.
                notificationChannel.setDescription("Channel description");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                notificationChannel.enableVibration(true);
                mNotifyManager.createNotificationChannel(notificationChannel);
            }

            final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            mBuilder.setAutoCancel(true).setContentTitle("Data exporting...")
                    .setContentText("Exporting in progress")
                    .setSmallIcon(R.mipmap.ic_launcher);
            File folder = new File(Environment.getExternalStorageDirectory()
                    + "/jai");

            boolean var = false;
            if (!folder.exists())
                var = folder.mkdir();

            System.out.println("" + var);


            final String filename = folder.toString() + "/" + "Test.csv";


            new Thread() {
                public void run() {
                    try {
                        List<TemperatureTable> tempratureList = getAllTemperatureList();
                        if (tempratureList != null && tempratureList.size() > 0) {

                            mBuilder.setProgress(tempratureList.size(), progress++, false);
                            mNotifyManager.notify(1, mBuilder.build());


                            FileWriter fw = new FileWriter(filename);


                            fw.append("region_code");
                            fw.append(',');

                            fw.append("weather_param");
                            fw.append(',');

                            fw.append("year");
                            fw.append(',');

                            fw.append("key");
                            fw.append(',');

                            fw.append("value");


                            fw.append('\n');

                            for (TemperatureTable table : tempratureList
                                    ) {


                                fw.append(table.region_code);
                                fw.append(',');

                                fw.append(table.weather_param);
                                fw.append(',');

                                fw.append(table.year);
                                fw.append(',');

                                fw.append(table.key);
                                fw.append(',');

                                fw.append(table.value);


                                fw.append('\n');
                                System.out.println("value: " + table.value);
                                mBuilder.setContentText("Exporting... ")
                                        // Removes the progress bar
                                        .setProgress(tempratureList.size(), progress++, false);

                            }


                            fw.close();
// When the loop is finished, updates the notification
                            mBuilder.setContentText("Exporting complete")
                                    // Removes the progress bar
                                    .setProgress(100, 100, false);
                            mNotifyManager.notify(1, mBuilder.build());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Data has successfully exported. Please open the file.", Toast.LENGTH_SHORT).show();

                                }
                            });

                            openFile();
                        } else
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "No Data Available. Please download the data.", Toast.LENGTH_SHORT).show();

                                }
                            });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();

        }

    }

    void shareFile() {
        String filename = "Test.csv";
        File filePath = new File(Environment.getExternalStorageDirectory()
                + "/jai", filename);

        if (filePath.exists()) {
            Uri path = Uri.fromFile(filePath);
            Intent intent = new Intent(Intent.ACTION_SEND);


            intent.putExtra(Intent.EXTRA_STREAM, path);


            startActivity(Intent.createChooser(intent, "Send file..."));
        } else
            Toast.makeText(getApplicationContext(), "File not exist. Please export the file", Toast.LENGTH_SHORT).show();

    }

    void openFile() {
        String filename = "Test.csv";
        File filePath = new File(Environment.getExternalStorageDirectory()
                + "/jai", filename);
        if (filePath.exists()) {
            Uri path = Uri.fromFile(filePath);
            Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
            pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pdfOpenintent.setDataAndType(path, "text/plain");
            try {
                startActivity(pdfOpenintent);
            } catch (ActivityNotFoundException e) {

            }
        } else
            Toast.makeText(getApplicationContext(), "File not exist. Please export the file", Toast.LENGTH_SHORT).show();

    }
}


