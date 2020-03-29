package com.mut_jaeryo.mltest.translate

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mut_jaeryo.mltest.R
import kotlinx.android.synthetic.main.fragment_translate.*


/**
 * A simple [Fragment] subclass.
 * Use the [TranslateFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TranslateFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_translate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider(this).get(TranslateViewModel::class.java)

        val adapter = ArrayAdapter(
            context!!,
            android.R.layout.simple_spinner_dropdown_item, viewModel.availableLanguages
        )

        // SourceLangSelector
        sourceLangSelector.adapter = adapter
        sourceLangSelector.setSelection(adapter.getPosition(TranslateViewModel.Language("en")))
        sourceLangSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                setProgressText(targetText)
                viewModel.sourceLang.value = adapter.getItem(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                targetText.text = ""
            }
        }

        targetLangSelector.adapter = adapter
        targetLangSelector.setSelection(adapter.getPosition(TranslateViewModel.Language("es")))
        targetLangSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                setProgressText(targetText)
                viewModel.targetLang.value = adapter.getItem(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                targetText.text = ""
            }
        }

        // Set up Switch Language Button
        buttonSwitchLang.setOnClickListener {
            setProgressText(targetText)
            val sourceLangPosition = sourceLangSelector.selectedItemPosition
            sourceLangSelector.setSelection(targetLangSelector.selectedItemPosition)
            targetLangSelector.setSelection(sourceLangPosition)
        }

        // Set up toggle buttons to delete or download remote models locally.
        buttonSyncSource.setOnCheckedChangeListener { _, isChecked ->
            val language = adapter.getItem(sourceLangSelector.selectedItemPosition)
            language?.let {
                if (isChecked) {
                    viewModel.downloadLanguage(language)
                } else {
                    viewModel.deleteLanguage(language)
                }
            }
        }
        buttonSyncTarget.setOnCheckedChangeListener { _, isChecked ->
            val language = adapter.getItem(targetLangSelector.selectedItemPosition)
            language?.let {
                if (isChecked) {
                    viewModel.downloadLanguage(language)
                } else {
                    viewModel.deleteLanguage(language)
                }
            }
        }

        sourceText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                setProgressText(targetText)
                viewModel.sourceText.postValue(s.toString())
            }
        })

        viewModel.translatedText.observe(viewLifecycleOwner, Observer { resultOrError ->
            resultOrError.let {
                if (resultOrError.error != null) {
                    sourceText.error = resultOrError.error?.localizedMessage
                } else {
                    targetText.text = resultOrError.result
                }
            }
        })

        // Update sync toggle button states based on downloaded models list.
        viewModel.availableModels.observe(viewLifecycleOwner, Observer { firebaseTranslateRemoteModels ->
            val output = context!!.getString(
                R.string.downloaded_models_label,
                firebaseTranslateRemoteModels
            )
            downloadedModels.text = output
            firebaseTranslateRemoteModels?.let {
                buttonSyncSource.isChecked = it.contains(
                    adapter.getItem(sourceLangSelector.selectedItemPosition)!!.code
                )
                buttonSyncTarget.isChecked = it.contains(
                    adapter.getItem(targetLangSelector.selectedItemPosition)!!.code
                )
            }
        })
    }

    private fun setProgressText(tv: TextView) {
        tv.text = context!!.getString(R.string.translate_progress)
    }

    companion object {
        fun newInstance():TranslateFragment{
            return TranslateFragment()
        }

    }
}
