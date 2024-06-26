package com.example.gadgetsfuture;

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.gadgetsfuture.adapter.adapterHome
import com.example.gadgetsfuture.config.config
import com.example.gadgetsfuture.config.config.Companion.urlTienda
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [store_fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class store_fragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    lateinit var recycler: RecyclerView
    lateinit var searchProduct: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_store_fragment, container, false)
        searchProduct = view.findViewById(R.id.txtSearch)
        val buttonIds = listOf(
            R.id.btnPortatiles,
            R.id.btnMouses,
            R.id.btnTeclados,
            R.id.btnMonitores,
            R.id.btnComputadores,
            R.id.btnConsolas,
            R.id.btnSillas
        )

        for (buttonId in buttonIds) {
            val btnCategoria: Button = view.findViewById(buttonId)

            btnCategoria.setOnClickListener {
                // Cambiar el estado seleccionado del botón
                btnCategoria.isSelected = !btnCategoria.isSelected

                // Cambiar el fondo y el color del texto según el estado seleccionado
                if (btnCategoria.isSelected) {
                    btnCategoria.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.black))
                    btnCategoria.setTextColor(resources.getColor(R.color.white))
                } else {
                    btnCategoria.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.white))
                    btnCategoria.setTextColor(resources.getColor(R.color.black))
                }
            }
        }

        //Error
        recycler= view.findViewById(R.id.RVCategorias)

        searchProduct.doAfterTextChanged {
            if (searchProduct.text.toString() != "")
            searchProductos()
            else
                llamarPeticionCategorias()
        }

        llamarPeticionCategorias()

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment store_fragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            store_fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    fun searchProductos () {
        GlobalScope.launch (Dispatchers.Main){
            try {
                peticionSearch()
            } catch (error:Exception){
                Toast.makeText(activity, "Error en el servidor, por favor conectate a internet", Toast.LENGTH_LONG).show()
            }
        }
    }

    suspend fun peticionSearch(){
        var url= config.urlTienda+"search_product/?txtBusqueda="+searchProduct.text
        var queue= Volley.newRequestQueue(activity)
        var request= JsonArrayRequest(
            Request.Method.POST,
            url,
            null,
            {response->
                cargarLista(response)
            },
            {error->
                Toast.makeText(activity, "Producto no encontrado en el inventario", Toast.LENGTH_LONG).show()
            }
        )
        queue.add(request)
    }

    fun llamarPeticionCategorias(){
        GlobalScope.launch(Dispatchers.Main) {
            try {
                peticionListaProductosC()
            }catch (error: Exception){
                Toast.makeText(activity, "Error en el servidor, por favor conectate a internet", Toast.LENGTH_LONG).show()
            }
        }
    }


    suspend fun peticionListaProductosC(){
        // http://192.168.153.200:8000/api/list_product/v1/
        var url= config.urlBase+"api/list_product/v1/"
        var queue= Volley.newRequestQueue(activity)
        var request= JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            {response->
                cargarLista(response)
            },
            {error->
                Toast.makeText(activity, "Error en la solicitud", Toast.LENGTH_LONG).show()
            }
        )
        queue.add(request)
    }


    fun cargarLista(listaProductos: JSONArray){
        recycler.layoutManager= LinearLayoutManager(activity)
        var adapter= adapterHome(activity, listaProductos)
        // Cambio de fragmento desde otro
        adapter.onclick= {
            val bundle=Bundle()
            bundle.putInt("id_productoH",it.getInt("id"))
            val transaction=requireFragmentManager().beginTransaction()
            var fragmento=detalle_producto()
            fragmento.arguments=bundle
            transaction.replace(R.id.container, fragmento).commit()
            transaction.addToBackStack(null)
        }
        recycler.adapter=adapter
    }
}