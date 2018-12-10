package ar.edu.utn.frsf.isi.dam.laboratorio05;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.MyDatabase;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.ReclamoDao;

import static android.app.Activity.RESULT_OK;

public class NuevoReclamoFragment extends Fragment {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_SAVE = 2;

    public interface OnNuevoLugarListener {
        public void obtenerCoordenadas();

        public void mapaPorTipos(int pos);
    }

    public void setListener(OnNuevoLugarListener listener) {
        this.listener = listener;
    }

    private boolean tieneAudio,tieneFoto;
    private String pathFotoVieja,pathAudioViejo;
    private Reclamo reclamoActual;
    private ReclamoDao reclamoDao;
    private ImageView ivFoto;
    private Button btnSacarFoto;
    private EditText reclamoDesc;
    private EditText mail;
    private Spinner tipoReclamo;
    private TextView tvCoord;
    private Button buscarCoord;
    private Button btnGuardar;
    private Button btnGrabar;
    private Button btnDetener;
    private Button btnReproducir;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private boolean reproduciendo = false;

    private OnNuevoLugarListener listener;

    private ArrayAdapter<Reclamo.TipoReclamo> tipoReclamoAdapter;

    public NuevoReclamoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        reclamoDao = MyDatabase.getInstance(this.getActivity()).getReclamoDao();

        View v = inflater.inflate(R.layout.fragment_nuevo_reclamo, container, false);

        reclamoDesc = (EditText) v.findViewById(R.id.reclamo_desc);
        mail = (EditText) v.findViewById(R.id.reclamo_mail);
        tipoReclamo = (Spinner) v.findViewById(R.id.reclamo_tipo);
        tvCoord = (TextView) v.findViewById(R.id.reclamo_coord);
        buscarCoord = (Button) v.findViewById(R.id.btnBuscarCoordenadas);
        btnGuardar = (Button) v.findViewById(R.id.btnGuardar);
        btnSacarFoto = (Button) v.findViewById(R.id.btnSacarFoto);
        ivFoto = (ImageView) v.findViewById(R.id.ivFoto);
        btnGrabar = (Button) v.findViewById(R.id.btnGrabar);
        btnDetener = (Button) v.findViewById(R.id.btnDetener);
        btnReproducir = (Button) v.findViewById(R.id.btnReproducir);

        tipoReclamoAdapter = new ArrayAdapter<Reclamo.TipoReclamo>(getActivity(), android.R.layout.simple_spinner_item, Reclamo.TipoReclamo.values());
        tipoReclamoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoReclamo.setAdapter(tipoReclamoAdapter);

        int idReclamo = 0;
        if (getArguments() != null) {
            idReclamo = getArguments().getInt("idReclamo", 0);
        }

        tipoReclamo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String tipo = parent.getItemAtPosition(position).toString();
                if((tipo.equals(Reclamo.TipoReclamo.VEREDAS.toString()) || tipo.equals(Reclamo.TipoReclamo.CALLE_EN_MAL_ESTADO.toString())) && tieneFoto)
                {
                    btnGuardar.setEnabled(true);
                }
                else if((reclamoDesc.getText().length()>=8 || tieneAudio) && !tipo.equals(Reclamo.TipoReclamo.VEREDAS.toString()) && !tipo.equals(Reclamo.TipoReclamo.CALLE_EN_MAL_ESTADO.toString()))
                {
                    btnGuardar.setEnabled(true);
                }
                else btnGuardar.setEnabled(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        cargarReclamo(idReclamo);


        boolean edicionActivada = !tvCoord.getText().toString().equals("0;0");
        reclamoDesc.setEnabled(edicionActivada);
        mail.setEnabled(edicionActivada);
        tipoReclamo.setEnabled(edicionActivada);
        btnSacarFoto.setEnabled(edicionActivada);
        btnGrabar.setEnabled(edicionActivada);

        buscarCoord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.obtenerCoordenadas();

            }
        });
        btnSacarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 1);

                    } else {
                        sacarFoto();
                    }
                }
            }
        });

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveOrUpdateReclamo();
            }
        });

        btnGrabar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.RECORD_AUDIO)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 2);

                    } else {
                        grabarAudio();
                    }
                }
            }
        });

        btnDetener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
                btnGrabar.setEnabled(true);
                btnReproducir.setEnabled(true);
                btnDetener.setEnabled(false);
                buscarCoord.setEnabled(true);
                tieneAudio=true;
                String tipo = tipoReclamo.getSelectedItem().toString();
                if(!tipo.equals(Reclamo.TipoReclamo.VEREDAS.toString()) && !tipo.equals(Reclamo.TipoReclamo.CALLE_EN_MAL_ESTADO.toString()))
                {
                    btnGuardar.setEnabled(true);
                }
            }
        });

        reclamoDesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String tipo = tipoReclamo.getSelectedItem().toString();
                if((tipo.equals(Reclamo.TipoReclamo.VEREDAS.toString()) || tipo.equals(Reclamo.TipoReclamo.CALLE_EN_MAL_ESTADO.toString())) && tieneFoto)
                {
                    btnGuardar.setEnabled(true);
                }
                else if((reclamoDesc.getText().length()>=8 || tieneAudio) && !tipo.equals(Reclamo.TipoReclamo.VEREDAS.toString()) && !tipo.equals(Reclamo.TipoReclamo.CALLE_EN_MAL_ESTADO.toString()))
                {
                    btnGuardar.setEnabled(true);
                }
                else btnGuardar.setEnabled(false);
            }
        });

        btnReproducir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnGrabar.setEnabled(false);
                btnReproducir.setEnabled(true);
                btnDetener.setEnabled(false);
                buscarCoord.setEnabled(false);
                if(!reproduciendo){
                    reproduciendo = true;
                    mPlayer = new MediaPlayer();
                    try {
                        mPlayer.setDataSource(reclamoActual.getPathAudio());
                        mPlayer.prepare();
                        mPlayer.start();
                        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                mPlayer.release();
                                mPlayer = null;
                                reproduciendo = false;
                                btnReproducir.setText("REPRODUCIR");
                                btnGrabar.setEnabled(true);
                                btnReproducir.setEnabled(true);
                                btnDetener.setEnabled(false);
                                buscarCoord.setEnabled(true);
                            }
                        });
                    } catch (IOException e) {
                        Log.e("AudioRecordTest", "prepare() failed");
                    }
                    btnReproducir.setText("STOP");

                }else{
                    mPlayer.release();
                    mPlayer = null;
                    reproduciendo = false;
                    btnReproducir.setText("REPRODUCIR");
                    btnGrabar.setEnabled(true);
                    btnReproducir.setEnabled(true);
                    btnDetener.setEnabled(false);
                    buscarCoord.setEnabled(true);
                }
            }
        });

        return v;
    }

    public void grabarAudio() {
        btnGrabar.setEnabled(false);
        btnReproducir.setEnabled(false);
        btnDetener.setEnabled(true);
        btnGuardar.setEnabled(false);
        buscarCoord.setEnabled(false);
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        try {
            createAudioFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecorder.setOutputFile(reclamoActual.getPathAudio());
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e("AudioRecordTest", "prepare() failed");
        }
        mRecorder.start();
    }

    public void sacarFoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity().getApplicationContext(),
                        "ar.edu.utn.frsf.isi.dam.laboratorio05.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_SAVE);
            }
        }
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
        reclamoActual.setPathFoto(image.getAbsolutePath());
        return image;
    }

    private File createAudioFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        String audioFileName = "3GP_" + timeStamp + "_";
        File dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File audio = File.createTempFile(
                audioFileName, /* prefix */
                ".3gp", /* suffix */
                dir /* directory */
        );
        reclamoActual.setPathAudio(audio.getAbsolutePath());
        return audio;
    }

    private void cargarReclamo(final int id) {
        if (id > 0) {
            Runnable hiloCargaDatos = new Runnable() {
                @Override
                public void run() {
                    reclamoActual = reclamoDao.getById(id);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(reclamoActual.getPathFoto()!=null) onActivityResult(REQUEST_IMAGE_SAVE, Activity.RESULT_OK, null);
                            if(reclamoActual.getPathAudio()!=null)
                            {
                                btnReproducir.setEnabled(true);
                                tieneAudio=true;
                            }
                            pathFotoVieja=reclamoActual.getPathFoto();
                            pathAudioViejo=reclamoActual.getPathAudio();
                            mail.setText(reclamoActual.getEmail());
                            tvCoord.setText(reclamoActual.getLatitud() + ";" + reclamoActual.getLongitud());
                            reclamoDesc.setText(reclamoActual.getReclamo());
                            Reclamo.TipoReclamo[] tipos = Reclamo.TipoReclamo.values();
                            for (int i = 0; i < tipos.length; i++) {
                                if (tipos[i].equals(reclamoActual.getTipo())) {
                                    tipoReclamo.setSelection(i);
                                    break;
                                }
                            }
                        }
                    });
                }
            };
            Thread t1 = new Thread(hiloCargaDatos);
            t1.start();
        } else {
            String coordenadas = "0;0";
            if (getArguments() != null) coordenadas = getArguments().getString("latLng", "0;0");
            tvCoord.setText(coordenadas);
            reclamoActual = new Reclamo();
        }

    }

    private void saveOrUpdateReclamo() {
        reclamoActual.setEmail(mail.getText().toString());
        reclamoActual.setReclamo(reclamoDesc.getText().toString());
        reclamoActual.setTipo(tipoReclamoAdapter.getItem(tipoReclamo.getSelectedItemPosition()));
        if (tvCoord.getText().toString().length() > 0 && tvCoord.getText().toString().contains(";")) {
            String[] coordenadas = tvCoord.getText().toString().split(";");
            reclamoActual.setLatitud(Double.valueOf(coordenadas[0]));
            reclamoActual.setLongitud(Double.valueOf(coordenadas[1]));
        }
        Runnable hiloActualizacion = new Runnable() {
            @Override
            public void run() {
                if(pathFotoVieja!=reclamoActual.getPathFoto() && pathFotoVieja!=null)
                {
                    File file = new File(pathFotoVieja);
                    if(file.delete()) System.out.println("Foto borrada");
                    else System.out.println("No se pudo borrar la foto");
                }
                if(pathAudioViejo!=reclamoActual.getPathAudio() && pathAudioViejo!=null)
                {
                    File file = new File(pathAudioViejo);
                    if(file.delete()) System.out.println("Audio borrado");
                    else System.out.println("No se pudo borrar el audio");
                }
                if (reclamoActual.getId() > 0) reclamoDao.update(reclamoActual);
                else reclamoDao.insert(reclamoActual);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // limpiar vista
                        mail.setText(R.string.texto_vacio);
                        tvCoord.setText(R.string.texto_vacio);
                        reclamoDesc.setText(R.string.texto_vacio);
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                });
            }
        };
        Thread t1 = new Thread(hiloActualizacion);
        t1.start();
    }

    public void actualizarCoordenadas(LatLng latLng) {
        this.tvCoord.setText(latLng.latitude + ", " + latLng.longitude);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ivFoto.setImageBitmap(imageBitmap);
        }
        if (requestCode == REQUEST_IMAGE_SAVE && resultCode == Activity.RESULT_OK) {


            Bitmap imageBitmap = null;
            try {
                File file = new File(reclamoActual.getPathFoto());
                imageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.fromFile(file));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException n){
                n.printStackTrace();
            }
            if (imageBitmap != null) {
                ivFoto.setImageBitmap(imageBitmap);
                tieneFoto=true;
                String tipo = tipoReclamo.getSelectedItem().toString();
                if(tipo.equals(Reclamo.TipoReclamo.VEREDAS.toString()) || tipo.equals(Reclamo.TipoReclamo.CALLE_EN_MAL_ESTADO.toString()))
                {
                    btnGuardar.setEnabled(true);
                }
            }

        }
    }


}
