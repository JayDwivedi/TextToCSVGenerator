package com.bbi.weatherapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLOutput;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final String[] regionList={"UK","England","Scotland","Wales"};
        final String[] weatherList={"Tmax","Tmin","Tmean","Sunshine","Rainfall"};
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    for (int k = 0; k < regionList.length; k++) {
                        for (int j = 0; j < weatherList.length; j++) {

                            System.out.println("Region: "+regionList[k]+"  "+"weather code: "+weatherList[j]);
                            String data = "";
                            Document doc = Jsoup.connect("http://www.metoffice.gov.uk/pub/data/weather/uk/climate/datasets/"+weatherList[j]+"/ranked/"+regionList[k]+".txt").get();

                            Elements body = doc.getElementsByTag("body");

                            for (Element element : body) {
                                System.out.println();


                                data = element.getElementsByTag("body").text();
                            }


                            data = data.substring(data.indexOf("ANN Year") + 8, data.length());

                            String[] dataArray = data.split(" ");


                            //  String[] copyArray = Arrays.copyOfRange(dataArray, 3500,dataArray.length);


                            int constValue = 0;
                            for (int i = 1; i < dataArray.length; i++) {


                                switch (i - constValue) {
                                    case 1:
                                        System.out.println(dataArray[i+1] + " " + dataArray[i]+ " JAN "  );
                                        i++;
                                        break;
                                    case 3:
                                        System.out.println(dataArray[i+1] + " " + dataArray[i] + " FEB");
                                        i++;
                                        break;
                                    case 5:
                                        System.out.println(dataArray[i+1] + " " + dataArray[i] + " MAR");
                                        i++;
                                        break;
                                    case 7:
                                        System.out.println(dataArray[i+1] + " " + dataArray[i] + " APR");
                                        i++;
                                        break;
                                    case 9:
                                        System.out.println(dataArray[i+1] + " " + dataArray[i] + " MAY");
                                        i++;
                                        break;
                                    case 11:
                                        System.out.println(dataArray[i+1] + " " + dataArray[i] + " JUN");
                                        i++;
                                        break;
                                    case 13:
                                        System.out.println(dataArray[i+1] + " " + dataArray[i] + " JUL");
                                        i++;
                                        break;
                                    case 15:
                                        System.out.println(dataArray[i+1] + " " + dataArray[i] + " AUG");
                                        i++;
                                        break;
                                    case 17:
                                        System.out.println(dataArray[i+1] + " " + dataArray[i] + " SEP");
                                        i++;
                                        break;
                                    case 19:
                                        System.out.println(dataArray[i+1] + " " + dataArray[i] + " OCT");
                                        i++;
                                        break;
                                    case 21:
                                        System.out.println(dataArray[i+1] + " " + dataArray[i] + " NOV");
                                        i++;
                                        break;
                                    case 23:
                                        System.out.println(dataArray[i+1] + " " + dataArray[i] + " DEC");
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


                    }
                catch(Exception e)
                    {
                        e.printStackTrace();
                    }

                }

        }).start();
    }
}


