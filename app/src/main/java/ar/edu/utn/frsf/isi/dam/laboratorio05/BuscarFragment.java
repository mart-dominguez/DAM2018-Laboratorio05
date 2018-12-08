package ar.edu.utn.frsf.isi.dam.laboratorio05;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;


public class BuscarFragment extends Fragment {

    Spinner tipoReclamo;
    ArrayAdapter<Reclamo.TipoReclamo> tipoReclamoAdapter;
    private NuevoReclamoFragment.OnNuevoLugarListener listener;
    View v;
    private Button btnBuscar;

    public void setListener(NuevoReclamoFragment.OnNuevoLugarListener listener) {
        this.listener = listener;
    }
    public BuscarFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_buscar, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tipoReclamo = (Spinner) v.findViewById(R.id.sp_tipo_buscar);
        btnBuscar = (Button) v.findViewById(R.id.btn_buscar);
        tipoReclamoAdapter = new ArrayAdapter<Reclamo.TipoReclamo>(getActivity(), android.R.layout.simple_spinner_item, Reclamo.TipoReclamo.values());
        tipoReclamoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoReclamo.setAdapter(tipoReclamoAdapter);
        tipoReclamo.setSelection(0, false);
        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.mapaPorTipos(tipoReclamo.getSelectedItemPosition());
            }
        });

    }

}
