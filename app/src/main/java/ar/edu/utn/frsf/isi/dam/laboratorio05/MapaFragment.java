package ar.edu.utn.frsf.isi.dam.laboratorio05;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.AppDatabase;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.MyDatabase;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.ReclamoDao;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapaFragment extends SupportMapFragment implements OnMapReadyCallback {
    private GoogleMap miMapa;
    private int tipoMapa;
    private int idReclamo;
    private ReclamoDao reclamoDao;
    private List<Reclamo> listaReclamos;
    private Reclamo reclamo;
    private LatLngBounds.Builder builder;
    private CameraUpdate cu;




    /************** OnMapaListener *********************/
    private OnMapaListener listener;

    public interface OnMapaListener {
        public void coordenadasSeleccionadas(LatLng c);
    }

    public OnMapaListener getListener() {
        return listener;
    }

    public void setListener(OnMapaListener listener) {
        this.listener = listener;
    }

    /************** OnMapaListener *********************/


    private GoogleMap.OnMapLongClickListener listenerClickLargo = new GoogleMap.OnMapLongClickListener() {
        @Override
        public void onMapLongClick(LatLng latLng) {
            System.out.println("#################Click Largo###############");

                listener.coordenadasSeleccionadas(latLng);


        }
    };




    public MapaFragment() {
        // Required empty public constructor
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup
            container, Bundle savedInstanceState) {
        reclamoDao = MyDatabase.getInstance(this.getActivity()).getReclamoDao();
        builder = new LatLngBounds.Builder();

        View rootView = super.onCreateView(inflater, container,
                savedInstanceState);
        tipoMapa = 0;
        idReclamo= 0;


        Bundle argumentos = getArguments();
        if (argumentos != null) {
            tipoMapa = argumentos.getInt("tipo_mapa", 0);
            idReclamo = argumentos.getInt("idReclamo", 0);
        }




        getMapAsync(this);
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        miMapa = map;
        actualizarMapa();
    }

    private void actualizarMapa() {
        if (ActivityCompat.checkSelfPermission(MapaFragment.this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MapaFragment.this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapaFragment.this.getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    9999);
            return;
        }

        switch(tipoMapa) {
            case 0:
                miMapa.setMyLocationEnabled(true);
                break;
            case 1:
                miMapa.setOnMapLongClickListener(listenerClickLargo);
                break;
            case 2:
                Runnable hiloCargaDatos = new Runnable() {
                    @Override
                    public void run() {
                        listaReclamos = reclamoDao.getAll();


                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (Reclamo unReclamo : listaReclamos){
                                    LatLng posicion = new LatLng(unReclamo.getLatitud(),unReclamo.getLongitud());
                                    miMapa.addMarker(new MarkerOptions().position(posicion)
                                            .title(String.valueOf(unReclamo.getId()))
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

                                    builder.include(posicion);

                                }

                                int padding = 50;
                                LatLngBounds bounds = builder.build();
                                cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                                miMapa.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                                    @Override
                                    public void onMapLoaded() {
                                        miMapa.animateCamera(cu);
                                    }
                                });

                            }
                        });

                    }
                };

                Thread t1 = new Thread(hiloCargaDatos);
                t1.start();
                break;
            case 3:
                Runnable hiloMostarReclamo = new Runnable() {
                    @Override
                    public void run() {
                        reclamo = reclamoDao.getById(idReclamo);


                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LatLng posicion = new LatLng(reclamo.getLatitud(),reclamo.getLongitud());
                                miMapa.addMarker(new MarkerOptions().position(posicion)
                                        .title(String.valueOf(reclamo.getId()))
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                            miMapa.moveCamera(CameraUpdateFactory.newLatLngZoom(posicion,15));
                                CircleOptions circleOptions = new CircleOptions()
                                        .center(posicion)
                                        .radius(500)
                                        .strokeColor(Color.RED)
                                        .fillColor(0x220000FF)
                                        .strokeWidth(5);
                                Circle circle = miMapa.addCircle(circleOptions);

                            }
                        });

                    }
                };

                Thread t2 = new Thread(hiloMostarReclamo);
                t2.start();
                break;

        }

    }



}