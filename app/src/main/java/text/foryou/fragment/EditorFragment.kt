package text.foryou.fragment

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import text.foryou.*
import text.foryou.adapter.BackgroundListAdapter
import text.foryou.adapter.FontColorListAdapter
import text.foryou.adapter.FontStyleListAdapter
import text.foryou.data.model.NoteEntity
import text.foryou.databinding.EditorFragmentBinding
import text.foryou.viewmodel.EditorViewModel


class EditorFragment: Fragment() {

    private lateinit var viewModel: EditorViewModel

    private val args: EditorFragmentArgs by navArgs()

    private lateinit var binding: EditorFragmentBinding

    private lateinit var adapter: BackgroundListAdapter
    private lateinit var fontColorAdapter: FontColorListAdapter
    private lateinit var fontStyleAdapter: FontStyleListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        (activity as AppCompatActivity).supportActionBar?.let {
            it.setHomeButtonEnabled(true)
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_check)
            it.show()
        }
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setHasOptionsMenu(true)

        requireActivity().title =
                if(args.noteid == NEW_NOTE_ID) {
                    "New Text Board"
                }
                else {
                    "Edit Text Board"
                }

        viewModel = ViewModelProvider(this).get(EditorViewModel::class.java)
        viewModel.getNoteById(args.noteid)


        binding = EditorFragmentBinding.inflate(inflater, container, false)

        binding.editor.setText("")

        val adRequest = viewModel.requestAd()
        binding.addViewBanner.loadAd(adRequest)

        requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {

                    override fun handleOnBackPressed() {
                        saveAndReturn()
                    }
                })

        viewModel.setList?.observe(viewLifecycleOwner, Observer {
            adapter = BackgroundListAdapter(it, viewModel.currentNote)
            binding.recyclerViewBackStyle.adapter = adapter
            binding.recyclerViewBackStyle.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            viewModel.getSelectedBackPosition()
        })

        viewModel.selectedBackground?.observe(viewLifecycleOwner, Observer {
            binding.recyclerViewBackStyle.layoutManager?.scrollToPosition(viewModel.returnBackPosition()-1)
        })

        viewModel.fontList?.observe(viewLifecycleOwner, Observer {
            fontColorAdapter = FontColorListAdapter(it, viewModel.currentNote)

            binding.recyclerViewFontColor.adapter = fontColorAdapter
            binding.recyclerViewFontColor.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            viewModel.getSelectedFontColorPosition()
        })

        viewModel.selectedFontColor?.observe(viewLifecycleOwner, Observer {
            binding.recyclerViewFontColor.layoutManager?.scrollToPosition(viewModel.returnFontColorPosition()-1)
        })

        viewModel.fontStyleList?.observe(viewLifecycleOwner, Observer {
            fontStyleAdapter = FontStyleListAdapter(it, viewModel.currentNote)
            binding.recyclerViewFontStyle.adapter = fontStyleAdapter
            binding.recyclerViewFontStyle.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            viewModel.getSelectedFontStylePosition()
        })

        viewModel.selectedFontStyle?.observe(viewLifecycleOwner, Observer {
            binding.recyclerViewFontStyle.layoutManager?.scrollToPosition(viewModel.returnFontStylePosition()-1)
        })


        viewModel.currentNote.observe(viewLifecycleOwner, Observer {
            val savedString = savedInstanceState?.getString(NOTE_TEXT_KEY)
            val curusorPosition = savedInstanceState?.getInt(CURSOR_POSITION_KEY) ?: 0
            binding.editor.setText(savedString ?: it.text)
            binding.editor.setSelection(curusorPosition)
        })


        touchListener(binding.root)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val menuId = R.menu.menu_delete
        inflater.inflate(menuId, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            android.R.id.home -> saveAndReturn()
            R.id.action_delete -> deleteAndReturn()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveAndReturn(): Boolean {
        val imm = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.editor.windowToken, 0)

        var selectedBack = adapter.getCheckedRB()
        var selectedFont = fontColorAdapter.getCheckedRBfont()
        var selectedFontStyle = fontStyleAdapter.getCheckedRBfontStyle()

        if(selectedBack == null || selectedFont == null || selectedFontStyle == null) {
            var errMessage = "You should select the option"
            viewModel.setToast(errMessage)
        }

        else {

        viewModel.currentNote.value?.text = binding.editor.text.toString()
        viewModel.currentNote.value?.fontColor = selectedFont?.getTag().toString()
        viewModel.currentNote.value?.fontStyle = selectedFontStyle?.getTag().toString()
        viewModel.findSetAndAddToNote(Integer.parseInt(selectedBack?.getTag().toString()))

        Handler(Looper.getMainLooper()).postDelayed({
            findNavController().navigateUp()

        }, 100) }

        return true
    }

    private fun deleteAndReturn(): Boolean {
        val imm = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)

        val note = viewModel?.currentNote?.value?: NoteEntity()

        viewModel.deleteNote(note)

        findNavController().navigateUp()
        return true
    }


    override fun onSaveInstanceState(outState: Bundle) {
        with(binding.editor) {
            outState.putString(NOTE_TEXT_KEY, text.toString())
            outState.putInt(CURSOR_POSITION_KEY, selectionStart)
        }
        super.onSaveInstanceState(outState)
    }



    private fun touchListener(view: View) {
        view.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent): Boolean {

                if(binding.editor.isFocused) {
                    binding.editor.clearFocus()
                    val imm = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
                }
                return true
            }
        })
    }

}