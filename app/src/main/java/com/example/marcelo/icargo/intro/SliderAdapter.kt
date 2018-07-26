package com.example.marcelo.icargo.intro

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.marcelo.icargo.R

class SliderAdapter(private val context: Context): PagerAdapter() {

    val arrayImagens = arrayOf(R.drawable.intro1, R.drawable.intro2, R.drawable.intro3)
    val arrayTitulos = arrayOf(context.getString(R.string.title_fretes), context.getString(R.string.title_clientes), context.getString(R.string.title_financeiro))
    val arrayDescricoes = arrayOf(context.getString(R.string.desc_fretes), context.getString(R.string.desc_clientes), context.getString(R.string.desc_financeiro))

    override fun getCount(): Int {
        return arrayTitulos.size
    }

    override fun isViewFromObject(view: View, o: Any): Boolean {
        return view == o as LinearLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(R.layout.slide_layout, container, false)

        val imagemIntro = view.findViewById(R.id.ivIntro) as ImageView
        val tituloIntro = view.findViewById(R.id.tvTituloIntro) as TextView
        val descricaoIntro = view.findViewById(R.id.tvDescIntro) as TextView

        imagemIntro.setImageResource(arrayImagens[position])
        tituloIntro.text = arrayTitulos[position]
        descricaoIntro.text = arrayDescricoes[position]

        container.addView(view)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as LinearLayout)
    }


}