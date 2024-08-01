package com.example.mysec

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.mysec.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 툴바 설정
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // 하단 네비게이션 설정
        setBottomNavigationView()

        // 앱 초기 실행 시 홈화면으로 설정
        if (savedInstanceState == null) {
            binding.bottomNavigationView.selectedItemId = R.id.fragment_home
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_navi_menu, menu)

        val myPageMenuItem = menu?.findItem(R.id.action_mypage)
        val actionView = createCustomMenuView()
        myPageMenuItem?.actionView = actionView

        // 널 가능성 체크 후 클릭 리스너 설정
        actionView?.setOnClickListener {
            onOptionsItemSelected(myPageMenuItem ?: return@setOnClickListener)
        }
        return true
    }

    private fun createCustomMenuView(): View {
        // LinearLayout 생성 및 설정
        val linearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER
            setPadding(30, 10, 30, 10)
        }

        // ImageView 생성 및 설정
        val imageView = ImageView(this).apply {
            setImageResource(R.drawable.mypage) // 아이콘 리소스 설정
        }

        // LinearLayout에 ImageView 추가
        linearLayout.addView(imageView)

        return linearLayout
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_mypage -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, MypageFragment())
                    .commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setBottomNavigationView() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.fragment_list -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, ListFragment())
                        .commit()
                    true
                }
                R.id.fragment_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, HomeFragment())
                        .commit()
                    true
                }
                R.id.fragment_map -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, MapFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }
}