package ar.edu.utn.frsf.isi.dam.laboratorio05;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FormBusqueda.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FormBusqueda#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FormBusqueda extends Fragment {

    private ArrayAdapter adapterTipoReclamo;
    private Spinner spinnerTipo;
    private Button btnBuscar;

    public FormBusqueda() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_form_busqueda, container, false);

        spinnerTipo = v.findViewById(R.id.spTipoReclamo);
        btnBuscar = v.findViewById(R.id.btnBuscar);

        adapterTipoReclamo = new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_item, Reclamo.TipoReclamo.values());
        adapterTipoReclamo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapterTipoReclamo);

        btnBuscar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String tag = "mapaReclamos";
                Fragment fragment = getActivity().getSupportFragmentManager().findFragmentByTag(tag);
                if (fragment == null) {
                    fragment = new MapaFragment();
                }
                Bundle bundle = new Bundle();
                bundle.putInt("tipo_mapa", 5);
                bundle.putString("tipo_reclamo", spinnerTipo.getSelectedItem().toString());
                fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.contenido, fragment)
                        .commit();
            }
        });
        return v;
    }
}
