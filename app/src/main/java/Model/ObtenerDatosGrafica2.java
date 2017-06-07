package Model;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.mand.myapplication.R;
import com.example.mand.myapplication.secciones;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import BaseDeDatos.sqlite;
import Fragments.DialogFragmentGeneral;

public class ObtenerDatosGrafica2 {
    private JSONObject valorJSON;
    private ArrayList<JSONObject> jsonValores;

    private LineChart linea;
    private String tabla,posicion,campo,mensaje;
    private Context context;
    private ArrayList<Entry> datos;
    private ArrayList<String> mensajes;
    private ArrayList<String> labels;
    private boolean flag;
    private int temperaturaMaxima,temperaturaMinima,version;
    public ObtenerDatosGrafica2(LineChart linea,String tabla,String posicion,String campo,String mensaje,Context context) throws JSONException {
        this.linea = linea;
        this.tabla = tabla;
        this.posicion = posicion;
        this.campo = campo;
        this.mensaje = mensaje;
        this.context = context;

        this.version = Integer.parseInt(context.getString(R.string.version_db));

        this.datos = new ArrayList<>();
        this.mensajes = new ArrayList<>();




        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage("Obteniendo datos del servidor...");
        dialog.setMax(100);
        dialog.setProgress(0);
        dialog.setCancelable(false);

       new AsynTask(context,dialog,this).execute();

        obtenerMaximosMinimos();
    }
    public void obtenerMaximosMinimos(){
        sqlite bh = new sqlite(context,"MMTable",null,version);
        SQLiteDatabase db = bh.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM MMTable WHERE variedad = '"+tabla+"'",null);
        long i = c.getCount();

        if(i>0){
            try{
                if(c.moveToFirst()){
                    flag = true;
                    temperaturaMaxima = Integer.parseInt(c.getString(1));
                    temperaturaMinima = Integer.parseInt(c.getString(2));
                    Toast.makeText(context,""+temperaturaMaxima+" "+temperaturaMinima,Toast.LENGTH_LONG).show();
                }
            }finally {

            }
        }else{
            flag = false;
        }

    }

    static class AsynTask extends AsyncTask<Void,Void,Void>{
        Context context;
        ProgressDialog dialog;
        ObtenerDatosGrafica2 obj;
        public AsynTask(Context context,ProgressDialog dialog,ObtenerDatosGrafica2 obj){
            this.context = context;
            this.dialog = dialog;
            this.obj = obj;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                obj.obtenerValores();
                obj.filtroUltimos10();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onPreExecute(){
            Log.d("maicol","yea");
            dialog.show();
        }
        @Override
        public void onPostExecute(Void unused) {
            obj.graficar();
            dialog.dismiss();
        }
    }
    public void graficar(){
        LineDataSet dataSet = new LineDataSet(datos,"");

        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        dataSet.setDrawFilled(true);

        dataSet.setLineWidth(5);



        LineData lineData = new LineData(dataSet);

       if(flag){
           LimitLine limitLine = new LimitLine(temperaturaMaxima,"Temperatura maxima");

           linea.getAxisLeft().addLimitLine(limitLine);

           LimitLine limitLine1 = new LimitLine(temperaturaMinima, "Temperatura minima");

           linea.getAxisLeft().addLimitLine(limitLine1);

           limitLine.setLineWidth(4f);
           limitLine.setTextSize(12f);

           limitLine1.setLineWidth(4f);
           limitLine1.setTextSize(12f);
       }





        XAxis xAxis = linea.getXAxis();
        xAxis.setDrawLabels(false);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, AxisBase axisBase) {
                return mensajes.get((int) v % mensajes.size());
            }
        });

        linea.setDescription(null);

        linea.setData(lineData);


        linea.animateY(3000);

        linea.getLegend().setEnabled(false);

        linea.getAxisRight().setDrawLabels(false);

        linea.setOnChartValueSelectedListener(new oyenteGrafica());

    }
    public class oyenteGrafica implements OnChartValueSelectedListener{


        @Override
        public void onValueSelected(Entry entry, Highlight highlight) {
            Toast.makeText(context,""+entry.toString(),Toast.LENGTH_SHORT).show();
            FragmentManager fm = ((FragmentActivity)context).getSupportFragmentManager();
            DialogFragmentGeneral dialogFragmentGeneral = DialogFragmentGeneral.newInstance("resuengrafica");
            dialogFragmentGeneral.show(fm, "");
            Log.d("maickol", "" + highlight.toString());
        }

        @Override
        public void onNothingSelected() {

        }
    }


    public void obtenerValores(){
        //PREPARAMOS LA DIRECCION A DONDE SE VA A REALIZAR LA PETICION
        String dir = "http://207.249.127.215:1026/v2/entities?q=position=='"+posicion+"'&type="+tabla;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        URL url = null;
        HttpURLConnection conexion;


        try {
            //CRAMOS EL OBJETO DE URL PASANDOLE AL CONTRUCTOR LA  DIRECCION
            url = new URL(dir);
            //SE ABRE UNA CONEXION CON LA URL Y GUARDANDO LA CONEXION EN LA VARIABLE CONEXION
            conexion =(HttpURLConnection)url.openConnection();
            //LE INDICAMOS EL TUPO DE PETICION
            conexion.setRequestMethod("GET");
            //CONTECAMOS CON LA URL
            conexion.connect();
            //OBTENEMOS EL CODIGO DE RESPUESTRA
            int code = conexion.getResponseCode();
            //SI ES DEIFENTE EL CODIGO DE LA RESPUESTA A 200 ENTONCES MOSTRAMOS UN MENSAE DE ERROR Y PARAMOS LA EJECUCION CON RETURN
            if(code != HttpURLConnection.HTTP_OK){
                Toast.makeText(context, "Ocurrio un error " + code, Toast.LENGTH_LONG).show();
                return;
            }
            //CREAMOS UN BUFFER CON LOS DATOS OBTENIDOS
            BufferedReader in = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            String json = "";
            //GUARDAMOS LOS DATOS OBTENIDOS EN RESPONSE
            while((inputLine = in.readLine()) != null){
                response.append(inputLine);
            }
            //PASAMOS LOS DATOS OBTENIDOS DE RESPONSE A JSON
            json = response.toString();
            //PARSEAMOS A JSON LA VARIABLE JSON CON LOS DATOS DEL SERVIDOR
            JSONArray jsonArray = null;
            //CREAMOS UN ARRAY CON TODOS LOS DATOS OBTENIDOS DEL SERVIDOR
            jsonArray = new JSONArray(json);
            //OBTENEMOS EL ULTIMO VALOR DEL JSON QUE SERA EL QUE SE GRAFICA Y LO  GUSRAMOS EN valorJSON

            jsonValores = new ArrayList<>();
            int size = jsonArray.length();
            int index = 0;
            for(int i = 0;i<size;i++){
                valorJSON = jsonArray.getJSONObject(i);
                jsonValores.add(valorJSON);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void filtroUltimos10() throws JSONException {


        try{

            int size = jsonValores.size();



        int inicio = ((size-10)<1)?1:size-10;
        int contador = 0;
        for (int i = inicio;i<size;i++){
            //GUARDAMOS EL DATO ANTERIOR PARA HACER COMPARACIONES
            JSONObject jsonAnterior = jsonValores.get(i-1);
            JSONObject FechaHoraAnterior = new JSONObject(jsonAnterior.getString("fecha"));

            String[] horaAnterior = split(FechaHoraAnterior.getString("value"), 1);
            String[] fechaAnterior = split(FechaHoraAnterior.getString("value"),0);

            //GUARDANIS EL DATO ACTUAL PARA HACER COMPARACIONES CON EL ANTERIOR
            JSONObject jsonActual = jsonValores.get(i);
            JSONObject FechaHoraActual = new JSONObject(jsonActual.getString("fecha"));

            String[] horaActual = split(FechaHoraActual.getString("value"), 1);
            String[] fechaActual = split(FechaHoraActual.getString("value"),0);

            JSONObject temporalAnterior = new JSONObject(jsonAnterior.getString(campo));
            Log.d("maickolink",temporalAnterior.toString());
            if(parse(horaActual[0])==parse(horaAnterior[0])){
                if(parse(horaActual[1]) - parse(horaAnterior[1]) >= 5 ){
                    mensajes.add(horaAnterior[0]+":"+horaAnterior[1]);
                    datos.add(new Entry(contador,parse(temporalAnterior.getString("value"))));
                }
            }else{
                mensajes.add(horaAnterior[0]+" : "+horaAnterior[1]);
                datos.add(new Entry(contador, parse(temporalAnterior.getString("value"))));
            }
            contador++;

        }
        }

        catch (Exception ex)
        {
            Toast.makeText(context, "Error"+ex, Toast.LENGTH_SHORT).show();
        }

    }
    public String[] split(String value,int position){
        return value.split(" ")[position].split(":");
    }
    public int parse(String c){ return Integer.parseInt(c);}
}
