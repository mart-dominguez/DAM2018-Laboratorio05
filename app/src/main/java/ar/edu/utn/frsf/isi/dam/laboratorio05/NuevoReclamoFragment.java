package ar.edu.utn.frsf.isi.dam.laboratorio05;


import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.MyDatabase;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.ReclamoDao;

import static android.app.Activity.RESULT_OK;

public class NuevoReclamoFragment extends Fragment {

    private Reclamo reclamoActual;
    private ReclamoDao reclamoDao;
    private EditText reclamoDesc;
    private EditText mail;
    private Spinner tipoReclamo;
    private TextView tvCoord;
    private Button buscarCoord;
    private Button btnGuardar;
    private OnNuevoLugarListener listener;
    private ArrayAdapter<Reclamo.TipoReclamo> tipoReclamoAdapter;


    private String direccionDeFoto;
    private ImageView imageView;
    private Button btnTomarFoto;



    private Button btnGrabar;
    private Button btnReproducir;
    private static final String LOG_TAG = "AudioRecord";
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private String mFileName;
    private Boolean grabando = false;
    private Boolean reproduciendo = false;


    public NuevoReclamoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        reclamoDao = MyDatabase.getInstance(this.getActivity()).getReclamoDao();

        View v = inflater.inflate(R.layout.fragment_nuevo_reclamo, container, false);

        reclamoDesc = v.findViewById(R.id.reclamo_desc);
        mail = v.findViewById(R.id.reclamo_mail);
        tipoReclamo = v.findViewById(R.id.reclamo_tipo);
        tvCoord = v.findViewById(R.id.reclamo_coord);
        buscarCoord = v.findViewById(R.id.btnBuscarCoordenadas);
        btnGuardar = v.findViewById(R.id.btnGuardar);
        btnTomarFoto = v.findViewById(R.id.btnTomarFoto);
        imageView = v.findViewById(R.id.imageView);

        btnTomarFoto.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.GONE);


        mFileName = Environment.getExternalStorageDirectory()
                .getAbsolutePath()+"/audiorecordtest.3gp";
        btnGrabar = (Button) v.findViewById(R.id.btnGrabarAudio);
        btnReproducir = (Button) v.findViewById(R.id.btnReproducir);
        btnGrabar.setOnClickListener(listenerPlayer);
        btnReproducir.setOnClickListener(listenerPlayer);


        tipoReclamoAdapter = new ArrayAdapter<Reclamo.TipoReclamo>(getActivity(),android.R.layout.simple_spinner_item,Reclamo.TipoReclamo.values());
        tipoReclamoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoReclamo.setAdapter(tipoReclamoAdapter);

        int idReclamo =0;
        if(getArguments()!=null)  {
            idReclamo = getArguments().getInt("idReclamo",0);
        }

        cargarReclamo(idReclamo);


        boolean edicionActivada = !tvCoord.getText().toString().equals("0;0");
        reclamoDesc.setEnabled(edicionActivada );
        mail.setEnabled(edicionActivada );
        tipoReclamo.setEnabled(edicionActivada);
        btnGuardar.setEnabled(edicionActivada);

        buscarCoord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.obtenerCoordenadas();

            }
        });

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveOrUpdateReclamo();
            }
        });

        btnTomarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sacarGuardarFoto();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        return v;
    }

    public void setListener(OnNuevoLugarListener listener) {
        this.listener = listener;
    }


    public interface OnNuevoLugarListener {
        void obtenerCoordenadas();
    }

    private void cargarReclamo(final int id){
        if( id >0){
            Runnable hiloCargaDatos = new Runnable() {
                @Override
                public void run() {
                    reclamoActual = reclamoDao.getById(id);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mail.setText(reclamoActual.getEmail());
                            tvCoord.setText(reclamoActual.getLatitud()+";"+reclamoActual.getLongitud());
                            reclamoDesc.setText(reclamoActual.getReclamo());
                            Reclamo.TipoReclamo[] tipos= Reclamo.TipoReclamo.values();
                            for(int i=0;i<tipos.length;i++) {
                                if(tipos[i].equals(reclamoActual.getTipo())) {
                                    tipoReclamo.setSelection(i);
                                    break;
                                }
                            }
                            if (!reclamoActual.getDireccionDeFoto().isEmpty() && reclamoActual.getDireccionDeFoto() != null) {
                                direccionDeFoto = reclamoActual.getDireccionDeFoto();
                                cargarFoto(reclamoActual.getDireccionDeFoto());
                            }
                        }
                    });
                }
            };
            Thread t1 = new Thread(hiloCargaDatos);
            t1.start();
        }else{
            String coordenadas = "0;0";
            if(getArguments()!=null) coordenadas = getArguments().getString("latLng","0;0");
            tvCoord.setText(coordenadas);
            reclamoActual = new Reclamo();
        }

    }

    private void saveOrUpdateReclamo(){
        reclamoActual.setEmail(mail.getText().toString());
        reclamoActual.setReclamo(reclamoDesc.getText().toString());
        reclamoActual.setDireccionDeFoto(direccionDeFoto);
        reclamoActual.setTipo(tipoReclamoAdapter.getItem(tipoReclamo.getSelectedItemPosition()));
        if(tvCoord.getText().toString().length()>0 && tvCoord.getText().toString().contains(";")) {
            String[] coordenadas = tvCoord.getText().toString().split(";");
            reclamoActual.setLatitud(Double.valueOf(coordenadas[0]));
            reclamoActual.setLongitud(Double.valueOf(coordenadas[1]));
        }
        Runnable hiloActualizacion = new Runnable() {
            @Override
            public void run() {

                if(reclamoActual.getId()>0) reclamoDao.update(reclamoActual);
                else reclamoDao.insert(reclamoActual);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // limpiar vista
                        mail.setText(R.string.texto_vacio);
                        tvCoord.setText(R.string.texto_vacio);
                        reclamoDesc.setText(R.string.texto_vacio);
                        btnTomarFoto.setVisibility(View.VISIBLE);
                        imageView.setVisibility(View.GONE);
                        direccionDeFoto=null;
                        imageView=null;
                        btnTomarFoto=null;
                        getActivity().getFragmentManager().popBackStack();
                    }
                });
            }
        };
        Thread t1 = new Thread(hiloActualizacion);
        t1.start();
    }


    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                dir /* directory */
        );
        direccionDeFoto = image.getAbsolutePath();
        return image;
    }

    private void sacarGuardarFoto() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            photoFile = createImageFile();
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this.getActivity(),
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 100);
            }
        }
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        if (reqCode == 100 && resCode == RESULT_OK) {
            cargarFoto(direccionDeFoto);
        }
        //99 = permiso de grabar audio
    }

    public void cargarFoto(String directorio) {
        File file = new File(directorio);
        Bitmap imageBitmap = null;
        try {
            imageBitmap = MediaStore.Images.Media
                    .getBitmap(getActivity().getContentResolver(),
                            Uri.fromFile(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (imageBitmap != null) {

            imageView.setImageBitmap(imageBitmap);
            btnTomarFoto.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
        }
    }


    View.OnClickListener listenerPlayer = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 99);

            } else {
                switch (view.getId()){
                    case R.id.btnReproducir:
                        if(reproduciendo){
                            ((Button) view).setText("Reproducir");
                            reproduciendo=false;
                            terminarReproducir();
                        }else{
                            ((Button) view).setText("pausar.....");
                            reproduciendo=true;
                            reproducir();
                        }
                        break;
                    case R.id.btnGrabarAudio:
                        if(grabando){
                            ((Button) view).setText("Grabar");
                            grabando=false;
                            terminarGrabar();
                        }else{
                            try {
                                ((Button) view).setText("grabando.....");
                                grabando=true;
                                grabar();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                }
            }
        }
    };


    private void grabar() throws IOException {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        File dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File audio = File.createTempFile(
                "REC_" + timeStamp + "_", /* prefix */
                ".jpg", /* suffix */
                dir /* directory */
        );
        mFileName = audio.getAbsolutePath();
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mRecorder.prepare();
        } catch (IOException e) { Log.e(LOG_TAG, "prepare() failed"); }
        mRecorder.start();
    }
    private void terminarGrabar() {
        reclamoActual.setDireccionDeAudio(mFileName);
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    private void reproducir() {
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                btnReproducir.setText("Reproducir");
                reproduciendo=false;
                terminarReproducir();
            }

        });
        try {
            mPlayer.setDataSource(reclamoActual.getDireccionDeAudio());
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "no hay audio para reproducir");
        }
    }
    private void terminarReproducir() {
        mPlayer.release();
        mPlayer = null;
    }

}
