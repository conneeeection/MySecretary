package com.example.mysec

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.mysec.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), ListDialogFragment.OnProjectCreatedListener{

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    // SharedPreferences에 사용할 파일 이름과 키 정의
    private val PREFS_NAME = "MyPrefs"
    private val KEY_PROJECT_CREATED = "projectCreated"

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

        // '마이페이지' 메뉴 아이템의 커스텀 뷰 생성
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
                // '마이페이지' 메뉴 클릭 시 MypageFragment로 교체
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
                    if (isProjectCreated()) {
                        // 프로젝트가 생성된 경우 ProjectDialogFragment를 호출
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_container, ProjectFragment())
                            .addToBackStack(null)
                            .commit()
                    } else {
                        // 프로젝트가 생성되지 않은 경우 ListFragment를 호출
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_container, ListFragment())
                            .addToBackStack(null)
                            .commit()
                    }
                    true
                }
                R.id.fragment_home -> {
                    // '홈' 버튼 클릭 시 HomeFragment로 교체
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, HomeFragment())
                        .addToBackStack(null)
                        .commit()
                    true
                }
                R.id.fragment_map -> {
                    // '지도' 버튼 클릭 시 MapFragment로 교체
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, MapFragment())
                        .addToBackStack(null)
                        .commit()
                    true
                }
                else -> false
            }
        }
    }

    override fun onProjectCreated() {
        // 프로젝트가 생성된 경우 상태를 저장하고 ProjectFragment로 교체
        setProjectCreated(true)
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, ProjectFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun setProjectCreated(created: Boolean) {
        // SharedPreferences에 프로젝트 생성 상태 저장
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean(KEY_PROJECT_CREATED, created)
            apply()
        }
    }

    private fun isProjectCreated(): Boolean {
        // SharedPreferences에서 프로젝트 생성 상태 읽기
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_PROJECT_CREATED, false)
    }

    private fun showProjectDialog() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, ProjectFragment())
            .addToBackStack(null)
            .commit()
    }
}