package ar.edu.utn.frsf.isi.dam.laboratorio05;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.MyDatabase;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.ReclamoDao;

public class MapaFragment extends SupportMapFragment implements OnMapReadyCallback {
    private GoogleMap miMapa;
    private MainActivity listener;
    private ArrayList<Reclamo> listaReclamos = new ArrayList<>();
    private ReclamoDao reclamoDao;

    public MapaFragment() {
    }

    public void setListener(MainActivity listener) {
        this.listener = listener;
    }

    @Override
    public void onMapReady(GoogleMap map) {

        miMapa = map;
        miMapa.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                listener.coordenadasSeleccionadas(latLng);
            }
        });
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        miMapa.setMyLocationEnabled(true);
        if (!listaReclamos.isEmpty()) {
            for (Reclamo r : listaReclamos) {
                miMapa.addMarker(new MarkerOptions()
                        .position(new LatLng(r.getLatitud(), r.getLongitud()))
                        .title(r.getReclamo()));
            }
            LatLngBounds bounds = calcularExtremos();
            Integer padding = 300;
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            miMapa.animateCamera(cu);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup
            container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container,
                savedInstanceState);
        int tipoMapa = 0;
        Bundle argumentos = getArguments();
        if (argumentos != null) {
            tipoMapa = argumentos.getInt("tipo_mapa", 0);
        }
        getMapAsync(this);
        reclamoDao = MyDatabase.getInstance(this.getActivity()).getReclamoDao();
        switch (tipoMapa) {
            case 2:
                Runnable hiloCargarReclamos = new Runnable() {
                    @Override
                    public void run() {
                        listaReclamos.clear();
                        listaReclamos.addAll(reclamoDao.getAll());
                    }
                };
                Thread t1 = new Thread(hiloCargarReclamos);
                t1.start();
                break;
        }
        return rootView;
    }

    public interface OnMapaListener {
        void coordenadasSeleccionadas(LatLng c);
    }

    private LatLngBounds calcularExtremos() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Reclamo r : listaReclamos) {
            builder.include(new LatLng(r.getLatitud(), r.getLongitud()));
        }
        return builder.build();
    }
}

