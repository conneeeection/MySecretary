package com.example.mysec

import android.content.Context
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
import androidx.lifecycle.lifecycleScope
import com.example.mysec.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), ListDialogFragment.OnProjectCreatedListener {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var userId: String? = null

    // SharedPreferences에 사용할 파일 이름과 키 정의
    private val PREFS_NAME = "MyPrefs"
    private val KEY_PROJECT_CREATED = "projectCreated"

    // 데이터베이스 헬퍼 인스턴스
    private lateinit var dbHelper: ProjectDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 인텐트에서 사용자 ID 가져오기
        userId = intent.getStringExtra(ARG_USER_ID)

        // 데이터베이스 헬퍼 초기화
        dbHelper = ProjectDatabaseHelper(this)

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
                    .replace(R.id.main_container, HomeFragment.newInstance(userId!!))
                    .commit()
            }
        }
    }

    // 헤더 설정
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

    // 마이페이지
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

    // 푸터 설정
    private fun setBottomNavigationView() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.fragment_list -> {
                    lifecycleScope.launch {
                        try {
                            if (userId.isNullOrEmpty()) {
                                showToast("사용자 아이디가 설정되지 않았습니다.")
                                return@launch
                            }

                            // 사용자 ID를 기반으로 프로젝트 생성 여부 확인
                            val hasProjects = hasCreatedProjects(userId!!)

                            val fragment = if (hasProjects) {
                                ProjectFragment.newInstance(userId!!)
                            } else {
                                ListFragment.newInstance(userId!!)
                            }

                            supportFragmentManager.beginTransaction()
                                .replace(R.id.main_container, fragment)
                                .addToBackStack(null)
                                .commit()

                        } catch (e: Exception) {
                            showToast("오류가 발생했습니다: ${e.message}")
                        }
                    }
                    true
                }

                R.id.fragment_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, HomeFragment.newInstance(userId!!))
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

    private suspend fun hasCreatedProjects(userId: String): Boolean {
        return withContext(Dispatchers.IO) {
            val db = dbHelper.readableDatabase
            val cursor = db.query(
                ProjectDatabaseHelper.TABLE_NAME,
                arrayOf("COUNT(*)"),
                "${ProjectDatabaseHelper.COLUMN_USER_ID} = ?",
                arrayOf(userId),
                null,
                null,
                null
            )
            val projectExists = cursor.moveToFirst() && cursor.getInt(0) > 0
            cursor.close()
            projectExists
        }
    }

    // ListDialogFragment.OnProjectCreatedListener 인터페이스 구현
    override fun onProjectCreated() {
        // 프로젝트가 생성된 경우 상태를 저장하고 ProjectFragment로 교체
        setProjectCreated(true)
        if (userId != null) {
            val fragment = ProjectFragment.newInstance(userId!!)
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setProjectCreated(created: Boolean) {
        // SharedPreferences에 프로젝트 생성 상태 저장
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean(KEY_PROJECT_CREATED, created)
            apply()
        }
    }

    // 프로젝트 제목과 기간 넘겨주기 위한 메서드
    fun showProjectFragment(projectName: String, projectDateRange: String) {
        val fragment = ProjectFragment().apply {
            arguments = Bundle().apply {
                putString("project_title", projectName)
                putString("project_date_range", projectDateRange)
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val ARG_USER_ID = "user_id"
    }
}
