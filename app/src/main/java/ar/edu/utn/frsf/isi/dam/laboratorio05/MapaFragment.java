package ar.edu.utn.frsf.isi.dam.laboratorio05;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.MyDatabase;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.ReclamoDao;

public class MapaFragment extends SupportMapFragment implements OnMapReadyCallback {
    private GoogleMap miMapa;
    private MainActivity listener;
    private ArrayList<Reclamo> listaReclamos = new ArrayList<>();
    private ReclamoDao reclamoDao;
    private Reclamo reclamoId;

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
        switch (getArguments().getInt("tipo_mapa")) {
            case 2:
                setearMarcadores();
                break;
            case 3:
                dibujarIndividual();
                break;
            case 4:
                ArrayList<LatLng> list = new ArrayList<>();
                for (Reclamo r : listaReclamos) {
                    list.add(new LatLng(r.getLatitud(), r.getLongitud()));
                }
                HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder().data(list)
                        .radius(49)
                        .build();
                miMapa.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                break;
            case 5: {
                setearMarcadores();
                dibujarLineas();
                break;
            }
        }
        return;
    }

    private void dibujarLineas() {
        ArrayList<LatLng> listaPos = new ArrayList<>();
        for (Reclamo r : listaReclamos) {
            listaPos.add(new LatLng(r.getLatitud(), r.getLongitud()));
        }
        miMapa.addPolyline(new PolylineOptions()
                .addAll(listaPos)
                .width(4)
                .color(Color.RED)
        );
    }

    private void dibujarIndividual() {
        if (reclamoId != null) {
            LatLng pos = new LatLng(reclamoId.getLatitud(), reclamoId.getLongitud());
            miMapa.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(reclamoId.getReclamo()));
            miMapa.addCircle(new CircleOptions()
                    .center(pos)
                    .radius(500)
                    .strokeColor(Color.RED)
                    .fillColor(0x22ff000d)
                    .strokeWidth(5));
            miMapa.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
        }
        return;
    }

    private void setearMarcadores() {
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
                listarReclamos();
                break;
            case 3:
                buscarPorId(getArguments().getInt("idReclamo"));
                break;
            case 4:
                listarReclamos();
                break;
            case 5:
                buscarPorTipo((String) getArguments().get("tipo_reclamo"));
                break;
        }
        return rootView;
    }

    private void buscarPorId(int idReclamo) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                reclamoId = reclamoDao.getById(idReclamo);
                if (reclamoId == null) return;
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    private void listarReclamos() {
        Runnable hiloCargarReclamos = new Runnable() {
            @Override
            public void run() {
                listaReclamos.clear();
                listaReclamos.addAll(reclamoDao.getAll());
            }
        };
        Thread t1 = new Thread(hiloCargarReclamos);
        t1.start();
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



    private void buscarPorTipo(String tipoReclamo) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (tipoReclamo == null) return;
                listaReclamos = (ArrayList<Reclamo>) reclamoDao.getByTipo(tipoReclamo);
                if (listaReclamos.isEmpty()) return;
            }
        };
        Thread t = new Thread(r);
        t.start();
    }
}

