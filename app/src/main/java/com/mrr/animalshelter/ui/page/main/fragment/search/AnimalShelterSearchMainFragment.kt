package com.mrr.animalshelter.ui.page.main.fragment.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrr.animalshelter.R
import com.mrr.animalshelter.data.AnimalFilter
import com.mrr.animalshelter.data.element.AnimalArea
import com.mrr.animalshelter.data.element.AnimalShelter
import com.mrr.animalshelter.core.base.BaseFragment
import com.mrr.animalshelter.ui.page.main.MainViewModel
import com.mrr.animalshelter.ui.page.main.fragment.search.adapter.AnimalShelterSearchAdapter
import kotlinx.android.synthetic.main.fragment_search_main.*

class AnimalShelterSearchMainFragment : BaseFragment() {

    companion object {
        const val TAG = "TAG_FRAGMENT_ANIMAL_SHELTER_SEARCH_MAIN"
        fun newInstance(): AnimalShelterSearchMainFragment {
            return AnimalShelterSearchMainFragment()
        }
    }

    private val mViewModel: MainViewModel by activityViewModels()
    private var mAdapter = AnimalShelterSearchAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initAnimalAdapter()
        observe()
        mViewModel.pullAnimals()
    }

    private fun initView() {
        initToolbar()

        val gridLayoutManager = GridLayoutManager(context, 3)
        rvAnimals.layoutManager = gridLayoutManager
        rvAnimals.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastVisibleItemPosition: Int = gridLayoutManager.findLastVisibleItemPosition()
                if (lastVisibleItemPosition >= gridLayoutManager.itemCount - lastVisibleItemPosition) {
                    mViewModel.pullAnimals()
                }
            }
        })
    }

    private fun initToolbar() {
        toolbar.title = getString(R.string.toolbar_title_search)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_filter -> mViewModel.launchSearchFilter()
            }
            true
        }
    }

    private fun initAnimalAdapter() {
        rvAnimals.adapter = mAdapter.apply {
            onItemClickListener = { animal -> mViewModel.launchAnimalShelterSearchDetail(animal) }
            onItemLongClickListener = { animal -> mViewModel.changeAnimalCollection(animal) }
        }
    }

    private fun observe() {
        mViewModel.animals.observe(viewLifecycleOwner, Observer {
            mAdapter.submitList(it)
        })
        mViewModel.collectedAnimalIds.observe(viewLifecycleOwner, Observer { collectionAnimalIds ->
            mAdapter.onCollectedAnimalsChanged(collectionAnimalIds)
        })
        mViewModel.onScrollAnimalShelterSearchToPositionEvent.observe(viewLifecycleOwner, Observer { position ->
            position?.let { rvAnimals.scrollToPosition(if (it > 3 && it % 3 == 0) it - 1 else it) }
        })
        mViewModel.animalFilter.observe(viewLifecycleOwner, Observer { filter ->
            toolbar.apply {
                title = when {
                    filter.shelter != AnimalShelter.All -> getString(filter.shelter.nameResourceId)
                    filter.area != AnimalArea.All -> getString(R.string.toolbar_title_search_area_format, getString(filter.area.nameResourceId))
                    else -> getString(R.string.toolbar_title_search)
                }
                menu.findItem(R.id.item_filter).apply {
                    context?.run {
                        setIcon(icon.apply { setTint(getColor(if (filter != AnimalFilter()) R.color.colorAccent else android.R.color.black)) })
                    }
                }
            }
        })
    }
}