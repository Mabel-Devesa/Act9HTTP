package com.mdevesa.activitats.activitat9_http;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity{

    private ReceptorXarxa receptor;
    Button boton;
    EditText et_url;
    // Textview on es mostrarà la informació
    TextView tv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //A l'iniciar l'app fa una comprovació dels estats de xarxa del mòbil
        //Obtenim un gestor de les connexions de xarxa
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        //Obtenim l'estat de la xarxa (general)
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        //Si està connectat
        if (networkInfo != null && networkInfo.isConnected()) {
            //Xarxa OK
            Toast.makeText(this,"Xarxa ok", Toast.LENGTH_LONG).show();
        } else {
            //Xarxa no disponible
            Toast.makeText(this,"Xarxa no disponible", Toast.LENGTH_LONG).show();
        }

        //Obtenim l'estat de la xarxa mòbil
        networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean connectat3G = networkInfo.isConnected();
        if(connectat3G){
            Toast.makeText(this,"Xarxa del mòbil ok!", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this,"Xarxa del mòbil no connectat!", Toast.LENGTH_LONG).show();
        }
        //Obtenim l'estat de la xarxa Wi-Fi
        networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean connectatWifi = networkInfo.isConnected();
        if(connectatWifi){
            Toast.makeText(this,"Wifi connectat", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this,"Wifi no connectat!", Toast.LENGTH_LONG).show();
        }
        //

        //Instanciar una instancia de la classe receptora i registrar-la amb un filtre d'intent
        //per escoltar unicament els missatges de broadcast sobre la connectivitat del dispositiu
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receptor = new ReceptorXarxa();
        this.registerReceiver(receptor, filter);


        boton = (Button) findViewById(R.id.button);
        et_url = (EditText) findViewById(R.id.ET_url);

        tv = (TextView) findViewById(R.id.textView);
        tv.setMovementMethod(new ScrollingMovementMethod());


        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Executem l'AsyncTask amb la URL.
                String url = et_url.getText().toString();
                new DescarregaText().execute(url);
            }
        });


    }

    // AsyncTask que descarrega text de la xarxa
    private class DescarregaText extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            // Descarreguem el text passat per argument
            return descarregaText(params[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            // Quan ha acabat la tasca, posem el text al TextView
            tv.setText(s);
        }
    }

    private String descarregaText(String URL) {
        int BUFFER_SIZE = 2000;            //Mida del buffer de text
        BufferedInputStream in;    //Flux de dades de lectura

        try {
            //Obrim la connexió
            in = ObreConnexioHTTP(URL);
        } catch (IOException e) {
            //Error
            Looper.prepare();
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();

            e.printStackTrace();
            return "";
        }

        //Obtenim un flux de caracters
        InputStreamReader isr = new InputStreamReader(in);

        char[] inputBuffer = new char[BUFFER_SIZE];        //Buffer de caràcters
        int caractersLlegits;                            //Caràcters llegits
        String stringResultat = "";                        //String resultat


        try {
            //Mentre s'hagin llegit caràcters
            while ((caractersLlegits = isr.read(inputBuffer)) > 0) {
                //Convertim els caràcters a String
                String stringLlegit = String.copyValueOf(inputBuffer, 0, caractersLlegits);

                //Afegim els caracters llegits al resultat
                stringResultat += stringLlegit;

            }
            //Tanquem la connexió
            in.close();
        } catch (IOException e) {
            //Excepció
            Looper.prepare();
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();

            e.printStackTrace();
            return "";
        }

        //Retornem el resultat
        return stringResultat;

    }


    private BufferedInputStream ObreConnexioHTTP(String adrecaURL) throws IOException {
        BufferedInputStream in = null;
        int resposta;

        URL url = new URL(adrecaURL);
        URLConnection connexio = url.openConnection();

        if (!(connexio instanceof HttpURLConnection))
            throw new IOException("No connexió HTTP");

        try {
            HttpURLConnection httpConn = (HttpURLConnection) connexio;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();

            resposta = httpConn.getResponseCode();
            if (resposta == HttpURLConnection.HTTP_OK) {
                in = new BufferedInputStream(httpConn.getInputStream());
            }
        } catch (Exception ex) {
            throw new IOException("Error connectant");
        }

        return in;
    }

    public void onDestroy() {
        super.onDestroy();
        //Donem de baixa el receptor de broadcast quan es destrueix l'aplicació
        if (receptor != null) {
            this.unregisterReceiver(receptor);
        }
    }

    public class ReceptorXarxa extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            //Actualitzar l'estat de la xarxa
            ActualitzaEstatXarxa();
        }

        public void ActualitzaEstatXarxa(){
            //Obtenim un gestor de les connexions de xarxa
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            //Obtenim l'estat de la xarxa
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            //Si està connectat
            if (networkInfo != null && networkInfo.isConnected()) {
                //Xarxa OK
                Toast.makeText(getApplicationContext(),"Xarxa ok", Toast.LENGTH_LONG).show();
            } else {
                //Xarxa no disponible
                Toast.makeText(getApplicationContext(),"Xarxa no disponible", Toast.LENGTH_LONG).show();
            }

            //Obtenim l'estat de la xarxa mòbil
            networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            boolean connectat3G = networkInfo.isConnected();
            if(connectat3G){
                Toast.makeText(getApplicationContext(),"Xarxa del mòbil ok!", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(getApplicationContext(),"Xarxa del mòbil no connectat!", Toast.LENGTH_LONG).show();
            }
            //Obtenim l'estat de la xarxa Wi-Fi
            networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            boolean connectatWifi = networkInfo.isConnected();
            if(connectatWifi){
                Toast.makeText(getApplicationContext(),"Wifi connectat", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(getApplicationContext(),"Wifi no connectat!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
