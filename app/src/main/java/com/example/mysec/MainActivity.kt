package com.example.mysec

import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.mysec.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 인텐트에서 사용자 ID 가져오기
        userId = intent.getStringExtra(ARG_USER_ID)

        // 툴바 설정
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // 하단 네비게이션 설정
        setBottomNavigationView()

        // 앱 초기 실행 시 홈화면으로 설정
        if (savedInstanceState == null) {
            binding.bottomNavigationView.selectedItemId = R.id.fragment_home
        }

        // 기본 화면 설정
        if (userId != null) {
            // 로그인 후 첫 화면으로 기본 홈 화면을 설정
            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, HomeFragment())
                    .commit()
            }
        }
    }

    companion object {
        const val ARG_USER_ID = "user_id"
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
                // 사용자 ID가 있으면 MypageFragment를 추가
                if (userId != null) {
                    val fragment = MypageFragment.newInstance(userId!!)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, fragment)
                        .commit()
                } else {
                    Toast.makeText(this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
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