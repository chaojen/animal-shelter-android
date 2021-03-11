package com.mrr.animalshelter.ui.page.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.mrr.animalshelter.R
import com.mrr.animalshelter.core.AnimalRepository
import com.mrr.animalshelter.core.api.ApiManager
import com.mrr.animalshelter.core.db.AppDatabase
import com.mrr.animalshelter.ktx.switchFragment
import com.mrr.animalshelter.ui.base.AnyViewModelFactory
import com.mrr.animalshelter.ui.base.BaseActivity
import com.mrr.animalshelter.ui.page.main.fragment.CollectionHostFragment
import com.mrr.animalshelter.ui.page.main.fragment.GalleryHostFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    companion object {
        private const val TAG_FRAGMENT_GALLERY = "TAG_FRAGMENT_GALLERY"
        private const val TAG_FRAGMENT_COLLECTION = "TAG_FRAGMENT_COLLECTION"
    }

    private var mViewModel: MainViewModel? = null
    private var mCurrentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViewModel()
        initView()
        observe()
    }

    private fun initViewModel() {
        val factory = AnyViewModelFactory {
            val service = ApiManager.getShelterService()
            val dao = AppDatabase.getInstance(this).getAnimalDao()
            val repository = AnimalRepository(service, dao)
            MainViewModel(repository)
        }
        mViewModel = ViewModelProviders.of(this, factory).get(MainViewModel::class.java)
    }

    private fun initView() {
        setSupportActionBar(toolbar)

        layBottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.itemAnimalsGallery -> {
                    onSwitchGalleryFragment()
                    true
                }
                R.id.itemAnimalsCollection -> {
                    onSwitchCollectionFragment()
                    true
                }
                else -> false
            }
        }

        layBottomNavigation.selectedItemId = R.id.itemAnimalsGallery
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.item_filter -> {
                // TODO launch filter setting
            }
        }
        return true
    }

    private fun onSwitchGalleryFragment() {
        mCurrentFragment = switchFragment(
            R.id.layContainer,
            TAG_FRAGMENT_GALLERY,
            onNewInstance = {
                val galleryHostFragment = GalleryHostFragment.newInstance()
                galleryHostFragment
            },
            onFragmentAlreadyVisible = { fragment ->
                if (fragment.childFragmentManager.backStackEntryCount == 1) {
                    mViewModel?.scrollGallery(0)
                } else {
                    fragment.childFragmentManager.popBackStack(null, 0)
                }
            }
        )
    }

    private fun onSwitchCollectionFragment() {
        mCurrentFragment = switchFragment(
            R.id.layContainer,
            TAG_FRAGMENT_COLLECTION,
            onNewInstance = {
                val collectionHostFragment = CollectionHostFragment.newInstance()
                collectionHostFragment
            }
        )
    }

    private fun observe() {
        mViewModel?.error?.observe(this, Observer { error ->
            // TODO show error message snackbar
        })
        mViewModel?.isNoMoreData?.observe(this, Observer { isNoMore ->
            if (isNoMore) {
                Snackbar.make(layBottomNavigation, R.string.main_snackbar_message_nomoredata, Snackbar.LENGTH_INDEFINITE)
            }
        })
    }

    override fun onBackPressed() {
        if (mCurrentFragment?.childFragmentManager?.backStackEntryCount ?: 1 > 1) {
            mCurrentFragment?.childFragmentManager?.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}