package com.example.mand.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class login extends AppCompatActivity {

    Button btnLogin;
    EditText txtUsuario;
    TextInputEditText txtPass;
    TextInputLayout TILusuario,TILcontrasenia;
    ProgressDialog progeesDialog;

    String parametros = "";
    String url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        txtUsuario = (EditText) findViewById(R.id.txtUsuario);
        txtPass = (TextInputEditText) findViewById(R.id.txtPass);

        TILusuario = (TextInputLayout)  findViewById(R.id.TILUsuario);
        TILcontrasenia = (TextInputLayout) findViewById(R.id.TILPassword);


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected()) {
                    String user = txtUsuario.getText().toString();
                    String pass = txtPass.getText().toString();

                    if(user.isEmpty() || pass.isEmpty()){
                        Toast.makeText(getApplicationContext(), "Ingrese los Datos Correspendientes...", Toast.LENGTH_LONG).show();
                    }else{
                        /*progeesDialog =  new ProgressDialog(login.this);
                        progeesDialog.setMessage("Verificando datos, Espere...");
                        progeesDialog.setCancelable(false);
                        progeesDialog.show();

                        url = "http://improntadsi.000webhostapp.com/WebServiceGreen/validarUsuario.php";
                        parametros = "usuario=" + user + "&contrasena=" + pass;
                        new SolicitaDados().execute(url);*/
                        if(user.equals("nature") && pass.equals("nature")){
                            Intent go = new Intent(login.this, navigation.class);
                            startActivity(go);
                            finish();
                        }



                    }

                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(login.this);
                    builder.setMessage("Verifica la Conexión a Internet")
                            .setTitle("ERROR");

                    builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.setCancelable(false);
                    dialog.show();
                }

            }
        });

    }
    public void auth(View v){

        if(!validate()){
            authFallo();
            return;
        }
        btnLogin.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(this,R.style.AppTheme_AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Autenticando");
        progressDialog.show();


        String user = "nature";
        String pass = "nature";
        String usuario = txtUsuario.getText().toString();
        String contrasena = txtPass.getText().toString();

        //Intent go2 = new Intent(login.this, navigation.class);
        //startActivity(go2);
        //sdfinish();

        if (usuario.equals(user) && contrasena.equals(pass)) {
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    Intent go = new Intent(login.this, navigation.class);
                    startActivity(go);
                    finish();
                }
            },4000);

        } else {
            progressDialog.dismiss();
            Snackbar.make(v,"Usuario/Contraseña incorrectos",Snackbar.LENGTH_SHORT).show();
            btnLogin.setEnabled(true);
        }

    }

    public void authFallo(){
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        btnLogin.setEnabled(true);
    }
    public boolean validate(){
        String usuario,contrasenia;
        boolean valid = true;
        usuario = txtUsuario.getText().toString();
        contrasenia = txtPass.getText().toString();

        if(usuario.isEmpty()){
            txtUsuario.requestFocus();
            TILusuario.setError("Usuario vacio");
            valid = false;
        }else{
            TILusuario.setError(null);
        }
        if(contrasenia.isEmpty() || contrasenia.length()<4 || contrasenia.length()>10){
            txtPass.requestFocus();
            TILcontrasenia.setError("Debe de contener entre 4 y 10 caracteres la contraseña!");
            valid = false;
        }else{
            TILcontrasenia.setError(null);
        }
        return valid;


    }
    @Override
    public void onBackPressed() {
        salir();
    }
    public boolean onKeyUp(int keyCode,KeyEvent keyEvent){
        if(keyCode == KeyEvent.KEYCODE_BACK){
            salir();
        }

        return true;
    }
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){
            case android.R.id.home:
                salir();
                break;
        }
        return true;
    }
    public void salir(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Estas seguro que deseas salir?");
        alert.setTitle("Alerta");
        alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                login.this.finish();
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        alert.setNegativeButton("Cancelar", null);
        alert.show();
    }


    private class SolicitaDados extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            return Conexion.postDados(urls[0], parametros);
        }
        protected void onPostExecute(String resultado) {
            //Toast.makeText(getApplicationContext(),resultado,Toast.LENGTH_LONG).show();
            int ini = resultado.length()-4;

            String rol = resultado.substring(ini,ini+1);
            if (resultado.contains("login_ok")){
                Intent go = new Intent(login.this, navigation.class);
                startActivity(go);
                finish();
                progeesDialog.dismiss();
                //progeesDialog.dismiss();
                String[] dados = resultado.split(",");
                // etEmail.setText(dados[0]+"-"+dados[1]+"+"+dados[2]);
                Intent abreInicio;
                if(rol.equals("1")){
                    //abreInicio = new Intent(login.this,menuprincipal.class);
                }else{}
                //abreInicio = new Intent(login.this,menualumno.class);

                //abreInicio.putExtra("rol",rol);
                //abreInicio.putExtra("contrasena",dados[1]);
                //Toast.makeText(getApplicationContext(),"YEAH PAPU",Toast.LENGTH_LONG).show();

                //startActivity(abreInicio);
                //finish();
            }else {
                progeesDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(login.this);
                builder.setMessage("Usuario o Contraseña Incorrectos")
                        .setTitle("ERROR");

                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.setCancelable(false);
                dialog.show();
                //Toast.makeText(getApplicationContext(),"Usuario o Contraseña incorrectos",Toast.LENGTH_LONG).show();
                //progeesDialog.dismiss();
            }
        }
    }

}
