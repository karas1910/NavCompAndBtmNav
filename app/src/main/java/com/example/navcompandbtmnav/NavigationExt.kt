package com.example.navcompandbtmnav

import android.util.SparseArray
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

fun BottomNavigationView.setupWithNavController(
    navGraphIds: List<Int>, // 管理するナビゲーション
    fragmentManager: FragmentManager, // フラグメントマネージャー
    containerId: Int, // コンテナID
) {

    // タグのマップ
    val graphIdToTagMap = SparseArray<String>()
    // 起動時のメニューのフラグメントのグラフID
    var firstFragmentGraphId = 0

    navGraphIds.forEachIndexed { index, navGraphId ->
        // fragmentTagをindexによって生成
        val fragmentTag = getFragmentTag(index)
        // navHostFragmentを入手
        val navHostFragment = obtainNavHostFragment(
            fragmentManager,
            fragmentTag,
            navGraphId,
            containerId
        )

        val graphId = navHostFragment.navController.graph.id
        if (index == 0) {
            firstFragmentGraphId = graphId
        }

        // mapに保存
        graphIdToTagMap[graphId] = fragmentTag

        // 起動時のアイテムのnavHostFragmentをアタッチし、他はデタッチする
        if (this.selectedItemId == graphId) {
            attachNavHostFragment(fragmentManager, navHostFragment, index == 0)
        } else {
            detachNavHostFragment(fragmentManager, navHostFragment)
        }
    }

    var selectedItemTag = graphIdToTagMap[this.selectedItemId]
    val firstFragmentTag = graphIdToTagMap[firstFragmentGraphId]
    var isOnFirstFragment = selectedItemTag == firstFragmentTag

    // ボトムメニューをタッチした時の処理
    setOnNavigationItemSelectedListener { item ->
        // 状態が保存されていれば何もしない
        if (fragmentManager.isStateSaved) {
            false
        } else {
            // 新しく選択したアイテムのTagを取得
            val newlySelectedItemTag = graphIdToTagMap[item.itemId]
            // 新しく選択したアイテムが選択中のアイテムじゃなかったら実行
            if (selectedItemTag != newlySelectedItemTag) {
                // 起動時のNavHostFragmentまでポップバック
                fragmentManager.popBackStack(firstFragmentTag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                // 新しく選択されたNavHostFragment
                val selectedFragment = fragmentManager.findFragmentByTag(newlySelectedItemTag) as NavHostFragment
                // 起動時のFragmentTagと新しく選択されたFragmentTagが同じでなければ実行
                if (firstFragmentTag != newlySelectedItemTag) {
                    fragmentManager.beginTransaction()
                        .attach(selectedFragment)
                        .setPrimaryNavigationFragment(selectedFragment)
                        .apply {
                            // 他のFragmentsをデタッチ
                            graphIdToTagMap.forEach { _, value ->
                                if (value != newlySelectedItemTag) {
                                    detach(fragmentManager.findFragmentByTag(firstFragmentTag)!!)
                                }
                            }
                        }
                        .addToBackStack(firstFragmentTag)
                        .setReorderingAllowed(true)
                        .commit()
                }
                // 選択中のTagを更新
                selectedItemTag = newlySelectedItemTag
                // 選択中のFragmentが起動時のFragmentかどうか
                isOnFirstFragment = selectedItemTag == firstFragmentTag
                true
            } else {
                false
            }
        }
    }

    // バックスタックが変更された時の処理
    fragmentManager.addOnBackStackChangedListener {
        // バックキーが押されて時に、現在のFragmentが起動時のFragmentではない かつ backStackに起動時のFragmentがない場合
        // 選択中のアイテムを起動時のアイテムにする
        if (!isOnFirstFragment && !fragmentManager.isOnBackStack(firstFragmentTag)) {
            this.selectedItemId = firstFragmentGraphId
        }
    }

    // 選択中のアイテムを再度選択した時の処理
    setupItemReselected(graphIdToTagMap, fragmentManager)
}

private fun BottomNavigationView.setupItemReselected(
    graphIdToTagMap: SparseArray<String>,
    fragmentManager: FragmentManager
) {
    setOnNavigationItemReselectedListener { item ->
        val newlySelectedItemTag = graphIdToTagMap[item.itemId]
        val selectedFragment = fragmentManager.findFragmentByTag(newlySelectedItemTag)
            as NavHostFragment
        val navController = selectedFragment.navController
        // 開始のDestinationまで戻る
        navController.popBackStack(
            navController.graph.startDestination, false
        )
    }
}

private fun FragmentManager.isOnBackStack(backStackName: String): Boolean {
    val backStackCount = backStackEntryCount
    for (index in 0 until backStackCount) {
        if (getBackStackEntryAt(index).name == backStackName) {
            return true
        }
    }
    return false
}

private fun detachNavHostFragment(
    fragmentManager: FragmentManager,
    navHostFragment: NavHostFragment
) {
    fragmentManager.beginTransaction()
        .detach(navHostFragment)
        .commitNow()
}

private fun attachNavHostFragment(
    fragmentManager: FragmentManager,
    navHostFragment: NavHostFragment,
    isPrimaryNavFragment: Boolean
) {
    fragmentManager.beginTransaction()
        .attach(navHostFragment)
        .apply {
            if (isPrimaryNavFragment) {
                setPrimaryNavigationFragment(navHostFragment)
            }
        }
        .commitNow()
}

private fun obtainNavHostFragment(
    fragmentManager: FragmentManager,
    fragmentTag: String,
    navGraphId: Int,
    containerId: Int
): NavHostFragment {
    //  ボトムメニューで管理するFragmentを包括するNavHostFragmentがあれば返す
    val existingFragment = fragmentManager.findFragmentByTag(fragmentTag) as NavHostFragment?
    existingFragment?.let { return it }

    // NavHostFragmentがなければ、作成する
    val navHostFragment = NavHostFragment.create(navGraphId)
    fragmentManager.beginTransaction()
        .add(containerId, navHostFragment, fragmentTag)
        .commitNow()
    return navHostFragment
}

private fun getFragmentTag(index: Int) = "bottomNavigation#$index"

