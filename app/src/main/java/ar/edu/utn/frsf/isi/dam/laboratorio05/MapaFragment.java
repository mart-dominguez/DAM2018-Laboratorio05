package ar.edu.utn.frsf.isi.dam.laboratorio05;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapaFragment extends SupportMapFragment implements OnMapReadyCallback {
    private GoogleMap miMapa;
    private int tipoMapa;



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
        View rootView = super.onCreateView(inflater, container,
                savedInstanceState);
        tipoMapa = 0;
        Bundle argumentos = getArguments();
        if (argumentos != null) {
            tipoMapa = argumentos.getInt("tipo_mapa", 0);
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

        }

    }



}