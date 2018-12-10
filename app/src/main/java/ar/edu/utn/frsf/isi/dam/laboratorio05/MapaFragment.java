package ar.edu.utn.frsf.isi.dam.laboratorio05;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.List;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.MyDatabase;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapaFragment extends SupportMapFragment implements OnMapReadyCallback {
    private GoogleMap miMapa;
    private OnAbrirMapaListener listener;
    List<Reclamo> reclamos;
    private boolean mostrarCosas = false;
    long id_reclamo = -1;
    int id_tipo = -1;

    public MapaFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        int tipoMapa =0;
        Bundle argumentos = getArguments();
        if(argumentos !=null) {
            tipoMapa = argumentos .getInt("tipo_mapa",0);
        }
        getMapAsync(this);
        if(tipoMapa == 2){
            traerReclamos();
            mostrarCosas = true;
        }else if (tipoMapa == 3){
            traerReclamos();
            mostrarCosas = true;
            id_reclamo = argumentos.getInt("id_reclamo",-1);
        }else if (tipoMapa == 4){
            traerReclamos();
            id_reclamo = 100;
            mostrarCosas = true;
        }else if (tipoMapa == 5){
            traerReclamos();
            id_tipo = argumentos.getInt("id_tipo",-1);
            mostrarCosas = true;
        }
        return rootView;
    }

    private void traerReclamos(){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                MapaFragment.this.reclamos = MyDatabase.getInstance(getActivity()).getReclamoDao().getAll();
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    private void addHeatMap() {
        List<LatLng> list = new ArrayList<>();
        for(Reclamo r: reclamos){
            list.add(new LatLng(r.getLatitud(), r.getLongitud()));
        }
        HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder().data(list)
                .build();
        TileOverlay mOverlay = miMapa.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
    }

    public void agregarMarcadorColor(Reclamo r){
        System.out.println("QUISE AGREGAR");
        if(miMapa!=null) {
            System.out.println("AGREGO");
            LatLng l = new LatLng(r.getLatitud(), r.getLongitud());
            BitmapDescriptor b;
            switch(Reclamo.TipoReclamo.valueOf(r.getTipo().name()).ordinal()){
                case 0: b = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE); break;
                case 1: b = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE); break;
                case 2: b = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE); break;
                case 3: b = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED); break;
                case 4: b = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN); break;
                case 5: b = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE); break;
                default: b = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW); break;
            }
            miMapa.addMarker(
                    new MarkerOptions()
                            .position(l)
                            .title(r.getReclamo())
                            .icon(b)
            );
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        miMapa = map;
        actualizarMapa();
        if(mostrarCosas){
            if(id_tipo>=0){
                List<LatLng> lista = new ArrayList<>();
                for(Reclamo rec : reclamos) {
                    if (Reclamo.TipoReclamo.valueOf(rec.getTipo().name()).ordinal() == id_tipo) {
                        this.agregarMarcadorColor(rec);
                        lista.add(new LatLng(rec.getLatitud(), rec.getLongitud()));
                    }
                }
                PolylineOptions rectOptions = new PolylineOptions();
                for(LatLng l: lista) {
                    rectOptions.add(l).color(Color.RED);
                }
                Polyline polyline = miMapa.addPolyline(rectOptions);
                if(lista.size()>0) {
                    LatLngBounds b = obtenerBounds();
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(b, 200,200,5);
                    miMapa.animateCamera(cu);
                }
                id_tipo = -1;
            }else if(id_reclamo<0){
                for(Reclamo rec : reclamos){
                    this.agregarMarcadorColor(rec);
                }
                if(reclamos.size()>0) {
                    LatLngBounds b = obtenerBounds();
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(b, 200,200,5);
                    miMapa.animateCamera(cu);
                }
            }else if (id_reclamo == 100) {
                if(reclamos.size()>0) {
                    addHeatMap();
                    LatLngBounds b = obtenerBounds();
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(b, 200,200,5);
                    miMapa.animateCamera(cu);
                }

            }else{
                Reclamo reclamo = null;
                for(Reclamo r: reclamos){
                    if (r.getId() == id_reclamo){
                        reclamo = r;
                    }
                }
                if(reclamo != null){
                    this.agregarMarcadorColor(reclamo);
                    CircleOptions circleOptions = new CircleOptions()
                            .center(new LatLng(reclamo.getLatitud(),reclamo.getLongitud()))
                            .radius(500)
                            .strokeColor(Color.RED)
                            .strokeWidth(5);
                    Circle circle = miMapa.addCircle(circleOptions);
                    LatLng l = new LatLng(reclamo.getLatitud(), reclamo.getLongitud());
                    miMapa.moveCamera(CameraUpdateFactory.newLatLngZoom(l, 15));
                }else{
                    System.out.println("No se encontró el reclamo");
                }
                id_reclamo= -1;
            }
            mostrarCosas = false;
        }else{
            miMapa.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng){
                    listener.coordenadasSeleccionadas(latLng);
                }
            });
        }
    }

    private LatLngBounds obtenerBounds() {

        LatLngBounds.Builder b = new LatLngBounds.Builder();
        for (Reclamo r : reclamos) {
            b.include(new LatLng(r.getLatitud(), r.getLongitud()));
        }
        LatLngBounds bound = b.build();
        return bound;
    }

    private void actualizarMapa() {
        if (ActivityCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((MainActivity)listener,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    9999);
            return;
        }
        miMapa.setMyLocationEnabled(true);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 9999: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    actualizarMapa();
                } else { // }
                    return;
                }
            }
        }
    }

    public void setListener(OnAbrirMapaListener listener) {
        this.listener = listener;
    }

    public interface OnAbrirMapaListener {
        public void obtenerCoordenadas();
        public void coordenadasSeleccionadas(LatLng c);
    }

}
